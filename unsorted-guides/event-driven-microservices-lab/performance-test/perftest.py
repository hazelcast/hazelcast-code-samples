import random

import locust

temp_file = '/tmp/sns.txt'
with open(temp_file, 'r') as f:
    serial_numbers = [line.strip() for line in f]

print(f'Read {len(serial_numbers)} serial numbers.')


class TestUser(locust.HttpUser):
    wait_time = locust.constant_throughput(1)

    @locust.task
    def get_random_status(self):
        sn = random.choice(serial_numbers)
        self.client.get(f'/machinestatus?sn={sn}', name='machinestatus?sn=[sn]')
