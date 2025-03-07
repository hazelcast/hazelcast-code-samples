import threading
import pandas as pd
import numpy as np


class Bucket:
    def __init__(self):
        self.lock = threading.Lock()
        self.serial_numbers = []
        self.average_bit_temp_10ss = []
        self.event_times = []

    def add(self, sn: str, temp: int, timestamp: np.datetime64):
        with self.lock:
            self.serial_numbers.append(sn)
            self.average_bit_temp_10ss.append(temp)
            self.event_times.append(timestamp)

    def harvest(self) -> pd.DataFrame:
        with self.lock:
            t = np.datetime64(0, 'ms')
            result = pd.DataFrame({
                'serial_number': pd.Series(self.serial_numbers, dtype=str),
                'average_bit_temp_10s': pd.Series(self.average_bit_temp_10ss, dtype=int),
                'event_time': pd.Series(self.event_times, dtype=t.dtype)
            })

            self.serial_numbers = []
            self.average_bit_temp_10ss = []
            self.event_times = []

            return result
