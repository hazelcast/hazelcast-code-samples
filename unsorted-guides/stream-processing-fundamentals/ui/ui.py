import logging
import os
import sys
import threading
import typing

import hazelcast
from dash import Dash, html, dcc
from dash.dependencies import Input, Output
import pandas as pd
import numpy as np
import plotly.express as px
from hazelcast import HazelcastClient
from hazelcast.config import ReconnectMode
from hazelcast.proxy.base import EntryEvent
from hazelcast.proxy.map import BlockingMap
from hazelcast.serialization.api import CompactSerializer, CompactReader, CompactWriter

import bucket
import viridian


# the following environment variables are required for a local connection
# for Viridian connections, see "viridian.py"
#
# HZ_SERVERS
# HZ_CLUSTER_NAME
#

# Object Definitions

class MachineStatus:
    def __init__(self):
        self.serial_number = ""
        self.average_bit_temp_10s = 0
        self.event_time = np.datetime64(0, 'ms')


class MachineStatusSerializer(CompactSerializer[MachineStatus]):
    def write(self, writer: CompactWriter, obj: MachineStatus) -> None:
        writer.write_string("serialNumber", obj.serial_number)
        writer.write_int16("averageBitTemp10s", obj.average_bit_temp_10s)

        # converting from datetime64 to unix millis since the epoch
        writer.write_int64("eventTime", (obj.event_time - np.datetime64(0, 'ms')) / np.timedelta(1, 'ms'))

    def read(self, reader: CompactReader) -> MachineStatus:
        result = MachineStatus()
        result.serial_number = reader.read_string("serialNumber")
        result.average_bit_temp_10s = reader.read_int16("averageBitTemp10s")
        result.event_time = np.datetime64(reader.read_int64("eventTime"), 'ms')
        return result

    def get_type_name(self) -> str:
        return "hazelcast.platform.labs.machineshop.domain.MachineStatus"

    def get_class(self) -> typing.Type[MachineStatus]:
        return MachineStatus


def get_required_env(name: str) -> str:
    if name not in os.environ:
        sys.exit(f'Please provide the "{name} environment variable."')
    else:
        return os.environ[name]


# returns a map listener that listens for a certain value using closure
def wait_map_listener_fun(expected_val: str, done: threading.Event):
    def inner_func(e: EntryEvent):
        if e.value == expected_val:
            print("event with expected value observed", flush=True)
            done.set()

    return inner_func


def logging_entry_listener(entry: EntryEvent):
    print(f'GOT {entry.key}: {entry.value.average_bit_temp_10s}', flush=True)


def collecting_entry_listener(entry: EntryEvent[str, MachineStatus]):
    global data_bucket
    data_bucket.add(entry.value.serial_number, entry.value.average_bit_temp_10s, entry.value.event_time)


def wait_for(imap: BlockingMap, expected_key: str, expected_val: str, timeout: float) -> bool:
    done = threading.Event()
    imap.add_entry_listener(
        include_value=True,
        key=expected_key,
        added_func=wait_map_listener_fun(expected_val, done),
        updated_func=wait_map_listener_fun(expected_val, done)
    )

    curr_val = imap.get(expected_key)
    if curr_val is not None and curr_val == expected_val:
        return True

    return done.wait(timeout)


def empty_df() -> pd.DataFrame:
    t = np.datetime64(0, 'ms')
    newdf = pd.DataFrame({
        'serial_number': pd.Series([], dtype=str),
        'average_bit_temp_10s': pd.Series([], dtype=int),
        'event_time': pd.Series([], dtype=t.dtype)
    })
    return newdf


def graph(dataframe):
    return px.line(dataframe,
                   x='event_time',
                   y='average_bit_temp_10s',
                   color='serial_number',
                   labels={
                       'event_time': 'time',
                       'average_bit_temp_10s': 'average bit temp (rolling 10s window)',
                       'serial_number': 'serial number'
                   })


# global state
data_bucket = bucket.Bucket()
app = Dash(__name__, external_stylesheets=['https://fonts.googleapis.com/css?family=Raleway:400,300,600'])
pd.options.plotting.backend = "plotly"
df = empty_df()
fig = graph(df)
query_listener_id = None


@app.callback(Output('main-graph', 'figure'), Input('timer', 'n_intervals'))
def update(n: int):
    global df
    newdf = data_bucket.harvest()
    if newdf.shape[0] > 0:
        df = pd.concat([df, newdf])

    return graph(df)


@app.callback(Output('matching_sns', 'children'), Input('location_input', 'value'), Input('block_input', 'value'))
def requery(location: str, block: str):
    global df, data_bucket, query_listener_id

    if location is None or len(location) == 0 or block is None or len(block) == 0:
        return "Matching Serial Numbers: 0"

    if query_listener_id is not None and event_map is not None:
        event_map.remove_entry_listener(query_listener_id)

    df = empty_df()
    data_bucket = bucket.Bucket()

    selected_serial_nums = hz.sql.execute(
        f"""SELECT serialNum FROM machine_profiles WHERE
           location = '{location}' AND
           block = '{block}' """
    ).result()

    sn_list = "','".join([row["serialNum"] for row in selected_serial_nums])
    if len(sn_list) > 0:
        query = f"serialNumber in ('{sn_list}')"
        print(f'adding entry listener WHERE {query}', flush=True)
        query_listener_id = event_map.add_entry_listener(
            include_value=True,
            predicate=hazelcast.predicate.sql(query),
            added_func=collecting_entry_listener,
            updated_func=collecting_entry_listener)

    serial_num_count = sn_list.count(",") + 1 if len(sn_list) > 0 else 0
    return str(f'Matching Serial Numbers: {serial_num_count}')


app.layout = html.Div(children=[
    html.Img(src='assets/hazelcast-logo.png', className="centered-image", style={'width': '25%'}),
    html.H2(children='Machine Shop Monitor'),
    dcc.Graph(
        id='main-graph',
        figure=fig
    ),
    html.Label(children="Location", htmlFor='location_input'),
    dcc.Input(id='location_input', value='San Antonio', type='text', debounce=True),
    html.Label(children="Block", htmlFor='block_input'),
    dcc.Input(id='block_input', value='A', type='text', debounce=True),
    html.Div(id='matching_sns', children=""),
    dcc.Interval(id="timer", interval=2500, n_intervals=0)
], className='container')

if __name__ == '__main__':
    logging.basicConfig(level=logging.INFO)
    if viridian.viridian_config_present():
        hz = viridian.configure_from_environment(async_start=False,
                                                 reconnect_mode=ReconnectMode.ON,
                                                 compact_serializers=[MachineStatusSerializer()])
    else:
        hz_cluster_name = get_required_env('HZ_CLUSTER_NAME')
        hz_servers = get_required_env('HZ_SERVERS').split(',')
        hz = HazelcastClient(
            cluster_name=hz_cluster_name,
            cluster_members=hz_servers,
            async_start=False,
            reconnect_mode=ReconnectMode.ON,
            compact_serializers=[MachineStatusSerializer()]
        )

    print('Connected to Hazelcast', flush=True)
    machine_controls_map = hz.get_map('machine_controls').blocking()
    event_map = hz.get_map('machine_status').blocking()
    system_activities_map = hz.get_map('system_activities').blocking()
    wait_for(system_activities_map, 'LOADER_STATUS', 'FINISHED', 3 * 60 * 1000)
    print("The loader has finished, proceeding", flush=True)

    app.run_server(host='0.0.0.0', debug=True)
