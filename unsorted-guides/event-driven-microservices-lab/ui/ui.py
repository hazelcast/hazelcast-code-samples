import logging
import os
import sys
import threading

import hazelcast
from dash import Dash, html, dcc
from dash.dependencies import Input, Output
import pandas as pd
from hazelcast import HazelcastClient
from hazelcast.config import ReconnectMode
from hazelcast.proxy.base import EntryEvent
from hazelcast.proxy.map import BlockingMap
from hazelcast.serialization.api import Portable, PortableWriter, PortableReader

import bucket
import viridian


# the following environment variables are required for a local connection
# for Viridian connections, see "viridian.py"
#
# HZ_SERVERS
# HZ_CLUSTER_NAME
#

# Object Definitions

class MachineStatusEvent(Portable):
    ID = 2

    def __init__(self):
        self.serial_num = ""
        self.event_time = 0
        self.bit_rpm = 0
        self.bit_temp = 0
        self.bit_position_x = 0
        self.bit_position_y = 0
        self.bit_position_z = 0

    def write_portable(self, writer: PortableWriter) -> None:
        writer.write_string("serialNum", self.serial_num)
        writer.write_long("eventTime", self.event_time)
        writer.write_int("bitRPM", self.bit_rpm)
        writer.write_short("bitTemp", self.bit_temp)
        writer.write_int("bitPositionX", self.bit_position_x)
        writer.write_int("bitPositionY", self.bit_position_y)
        writer.write_int("bitPositionZ", self.bit_position_z)

    def read_portable(self, reader: PortableReader) -> None:
        self.serial_num = reader.read_string("serialNum")
        self.event_time = reader.read_long("eventTime")
        self.bit_rpm = reader.read_int("bitRPM")
        self.bit_temp = reader.read_short("bitTemp")
        self.bit_position_x = reader.read_int("bitPositionX")
        self.bit_position_y = reader.read_int("bitPositionY")
        self.bit_position_z = reader.read_int("bitPositionZ")

    def get_factory_id(self) -> int:
        return 1

    def get_class_id(self) -> int:
        return MachineStatusEvent.ID


portable_factory = {MachineStatusEvent.ID: MachineStatusEvent}


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
    print(f'GOT {entry.key}: {entry.value.bit_temp}', flush=True)


def collecting_entry_listener(entry: EntryEvent[str, MachineStatusEvent]):
    global data_bucket
    data_bucket.add(entry.key, entry.value.bit_temp, entry.value.event_time)


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


# global state
data_bucket = bucket.Bucket()
app = Dash(__name__, external_stylesheets=['https://fonts.googleapis.com/css?family=Raleway:400,300,600'])
pd.options.plotting.backend = "plotly"
df = pd.DataFrame()
fig = df.plot(template='seaborn')
query_listener_id = None


@app.callback(Output('main-graph', 'figure'), Input('timer', 'n_intervals'))
def update(n: int):
    global df
    newdf = data_bucket.harvest()
    # print(newdf)
    df = pd.concat([df, newdf])

    # crucial because the time series don't align , without this there are gaps in the lines
    df.interpolate(inplace=True)
    return df.plot(template='seaborn')  # seaborn, plotly_dark


@app.callback(Output('matching_sns', 'children'), Input('location_input', 'value'), Input('block_input', 'value'))
def requery(location: str, block: str):
    global df, data_bucket, query_listener_id

    if location is None or len(location) == 0 or block is None or len(block) == 0:
        return "Matching Serial Numbers: 0"

    if query_listener_id is not None and event_map is not None:
        event_map.remove_entry_listener(query_listener_id)

    df = pd.DataFrame()
    data_bucket = bucket.Bucket()

    selected_serial_nums = hz.sql.execute(
        f"""SELECT serialNum FROM machine_profiles WHERE
           location = '{location}' AND
           block = '{block}' """
    ).result()

    sn_list = "','".join([r["serialNum"] for r in selected_serial_nums])
    if len(sn_list) > 0:
        query = f"serialNum in ('{sn_list}')"
        print(f'adding entry listener WHERE {query}', flush=True)
        query_listener_id = event_map.add_entry_listener(
            include_value=True,
            predicate=hazelcast.predicate.sql(query),
            added_func=collecting_entry_listener,
            updated_func=collecting_entry_listener)
        print("Listener added", flush=True)

    serial_num_count = sn_list.count(",") + 1 if len(sn_list) > 0 else 0
    return str(f'Matching Serial Numbers: {serial_num_count}')


app.layout = html.Div(children=[
    html.Img(src='assets/hazelcast-horz-flat-md.png', className="centered-image"),
    html.H2(children='Machine Shop Monitor'),
    dcc.Graph(
        id='main-graph',
        figure=fig
    ),
    html.Label(children="Location", htmlFor='location_input'),
    dcc.Input(id='location_input', value='San Antonio', type='text', debounce=True),
    html.Label(children="Block", htmlFor='block_input'),
    dcc.Input(id='block_input', value='A',  type='text', debounce=True),
    html.Div(id='matching_sns', children=""),
    dcc.Interval(id="timer", interval=2500, n_intervals=0)
], className='container')

if __name__ == '__main__':
    logging.basicConfig(level=logging.INFO)
    if viridian.viridian_config_present():
        hz = viridian.configure_from_environment(async_start=False,
                                                 reconnect_mode=ReconnectMode.ON,
                                                 portable_factories={
                                                     1: portable_factory
                                                 })
    else:
        hz_cluster_name = get_required_env('HZ_CLUSTER_NAME')
        hz_servers = get_required_env('HZ_SERVERS').split(',')
        hz = HazelcastClient(
            cluster_name=hz_cluster_name,
            cluster_members=hz_servers,
            async_start=False,
            reconnect_mode=ReconnectMode.ON,
            portable_factories={
                1: portable_factory
            }
        )

    print('Connected to Hazelcast', flush=True)
    machine_controls_map = hz.get_map('machine_controls').blocking()
    event_map = hz.get_map('machine_events').blocking()
    system_activities_map = hz.get_map('system_activities').blocking()
    wait_for(system_activities_map, 'LOADER_STATUS', 'FINISHED', 3 * 60 * 1000)
    print("The loader has finished, proceeding", flush=True)

    app.run_server(host='0.0.0.0', debug=True)
