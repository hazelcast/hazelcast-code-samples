import threading
import pandas as pd


class Bucket:

    # contents of the dict:
    # {
    #   "colname" : ( [templist], [timestamplist])
    # }
    #

    def __init__(self):
        self.lock = threading.Lock()
        self.data = dict()

    def add(self, sn: str, temp: int, event_epoch_time: int):
        with self.lock:
            temps, times = self.data.setdefault(sn, ([], []))
            temps.append(temp)

            # this rounding to the nearest second on purpose to
            # help the data align better
            times.append(pd.to_datetime(int(event_epoch_time/1000), unit='s'))

    def harvest(self) -> pd.DataFrame:
        with self.lock:
            new_data = dict()
            for k, v in self.data.items():
                new_data[k] = pd.Series(v[0], index=v[1])

            new_df = pd.DataFrame(new_data)
            self.data.clear()
            return new_df
