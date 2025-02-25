import argparse
import os.path
import sys
import time

import card_fraud_pb2

import hazelcast


def datafiles(data_dir):
    return [os.path.join(data_dir, f) for f in os.listdir(data_dir) if f != 'customers.csv']


class FileSource:
    def sendline(self, line):
        # order of fields in data:
        #    ssn,cc_num,first,last,gender,street,city,state,zip,lat,long,city_pop,job,dob,acct_num,profile,trans_num,trans_date,trans_time,unix_time,category,amt,is_fraud,merchant,merch_lat,merch_long
        #
        # fields in proto
        #        string gender = 1;
        #        string city = 2;
        #        string state = 3;
        #        string lat = 4;
        #        string long = 5;
        #        string city_pop = 6;
        #        string job = 7;
        #        string dob = 8;
        #        string category = 9;
        #        string amt = 10;
        #        string merchant = 11;
        #        string merch_lat = 12;
        #        string merch_long = 13;

        words = line.split(',')
        if len(words) != 26:
            print('WARNING: skipping malformed line')
            return

        auth_request = card_fraud_pb2.AuthRequestv01()
        auth_request.gender = words[4]
        auth_request.city = words[6]
        auth_request.state = words[7]
        auth_request.lat = words[9]
        auth_request.long = words[10]
        auth_request.city_pop = words[11]
        auth_request.job = words[12]
        auth_request.dob = words[13]
        auth_request.category = words[20]
        auth_request.amt = words[21]
        auth_request.merchant = words[23]
        auth_request.merch_lat = words[24]
        auth_request.merch_long = words[25]

        trans_num = words[16]

        # according to https://stackoverflow.com/questions/1859438/using-python-how-do-i-get-a-binary-serialization-of-my-google-protobuf-message
        # the SerializeToString method now returns a bytes object
        #
        # according to the Hazelcast python documentation, a bytearray python object is deserialized in java as a byte[]
        #
        auth_request_bytes = bytearray(auth_request.SerializeToString())
        self.hzmap.put(trans_num, auth_request_bytes)


    def has_more(self):
        return len(self.files) > 0

    def send_not_more_than(self, n):
        if n < len(self.files):
            return 0
        else:
            sent = 0
            batches = int(n/len(self.files))
            for i in range(batches):
                for f in self.files:
                    line = f.readline()
                    if line != '':
                        self.sendline(line)
                        sent += 1
                    else:
                        self.files.remove(f)
                        f.close()

        return sent

    # expects a list of filenames
    def __init__(self, filenames, hzmap):
        self.files = [open(filename, 'r') for filename in filenames]
        self.hzmap = hzmap

        # the first line of every file is a header so skip it
        for f in self.files:
            try:
                f.readline()
            except Exception:
                # this is a safeguard against undreadable junk files
                print(f'Could not read {f.name}. It will be ignored.', file=sys.stderr)
                self.files.remove(f)

    def __enter__(self):
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        for f in self.files:
            f.close()


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description="An simulator of credit card transactions")
    parser.add_argument("--dir", required=True, help='The directory where the data files are located')
    parser.add_argument("--tps", required=True, type=int, help='Transactions per second to generate')
    parser.add_argument("--hz-members", required = True, nargs='+', help='One or more hazelcast nodes in host:port format, space separated.')

    args = parser.parse_args()
    if not os.path.isdir(args.dir):
        sys.exit(f'{args.dir} does not point to an existing path')

    client = hazelcast.HazelcastClient(cluster_members=args.hz_members)
    hzmap = client.get_map("auth_requests").blocking()

    all_files = datafiles(args.dir)

    sent = 0
    start = time.time()
    with FileSource(all_files, hzmap) as file_source:
        while file_source.has_more():
            elapsed = time.time() - start
            due = (elapsed * args.tps) - sent
            sent_this_time = file_source.send_not_more_than(due)  # will generally return
            if sent_this_time == 0:
                time.sleep(.5)
            else:
                sent += sent_this_time

            # print(f'sent {sent} in {int(elapsed)}s due={due}')
