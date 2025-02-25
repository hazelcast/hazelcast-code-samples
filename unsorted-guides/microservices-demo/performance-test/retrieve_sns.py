import logging
import os
import sys
import threading

from hazelcast import HazelcastClient
from hazelcast.config import ReconnectMode
from hazelcast.proxy.base import EntryEvent
from hazelcast.proxy.map import BlockingMap
from hazelcast.serialization.api import PortableReader, PortableWriter, Portable

import viridian


# TODO most of the Hazelcast related code here has been copied from the ui and  should be pulled out into a common
#  module
def get_required_env(name: str) -> str:
    if name not in os.environ:
        sys.exit(f'Please provide the "{name} environment variable."')
    else:
        return os.environ[name]


def wait_map_listener_fun(expected_val: str, done: threading.Event):
    def inner_func(e: EntryEvent):
        if e.value == expected_val:
            print("event with expected value observed", flush=True)
            done.set()

    return inner_func


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


def connect_to_hazelcast() -> HazelcastClient:
    if viridian.viridian_config_present():
        hz = viridian.configure_from_environment(async_start=False,
                                                 reconnect_mode=ReconnectMode.ON,
                                                 portable_factories={
                                                     1: portable_factory})
    else:
        print('CONFIGURE LOCAL')
        hz_cluster_name = get_required_env('HZ_CLUSTER_NAME')
        hz_servers = get_required_env('HZ_SERVERS').split(',')
        hz = HazelcastClient(
            cluster_name=hz_cluster_name,
            cluster_members=hz_servers,
            async_start=False,
            reconnect_mode=ReconnectMode.ON
        )

    return hz


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

logging.basicConfig(level=logging.INFO)
hz = connect_to_hazelcast()
print('Connected to Hazelcast', flush=True)
system_activities_map = hz.get_map('system_activities').blocking()
profile_map = hz.get_map('machine_profiles').blocking()
wait_for(system_activities_map, 'LOADER_STATUS', 'FINISHED', 3 * 60 * 1000)
serial_numbers = profile_map.key_set()
print(f'Retrieved {len(serial_numbers)} serial numbers')
temp_file = '/tmp/sns.txt'
with open(temp_file, 'w') as f:
    for sn in serial_numbers:
        print(sn, file=f)

print(f'Wrote SNs to {temp_file}')
hz.shutdown()
print('Disconnected from Hazelcast', flush=True)
