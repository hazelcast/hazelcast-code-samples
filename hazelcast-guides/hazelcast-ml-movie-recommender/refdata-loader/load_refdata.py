import os
import sys

import hazelcast
import hazelcast.config

HZ_MEMBERS_PROP = 'HZ_MEMBERS'
HZ_CLUSTER_NAME_PROP = 'HZ_CLUSTER_NAME'
MOVIE_POSTER_FILE_PROP = 'MOVIE_POSTER_FILE'


def get_required_env(envname):
    if envname not in os.environ:
        sys.exit(f'Please set the {envname} environment variable.')
    else:
        return os.environ[envname]


if __name__ == '__main__':
    print('STARTED load_refdata')
    hz_cluster_name = get_required_env(HZ_CLUSTER_NAME_PROP)
    hz_members = get_required_env(HZ_MEMBERS_PROP)
    movie_poster_file = get_required_env(MOVIE_POSTER_FILE_PROP)

    hz = hazelcast.HazelcastClient(
        cluster_name=hz_cluster_name,
        cluster_members=hz_members.split(','),
        async_start=False,
        reconnect_mode=hazelcast.config.ReconnectMode.ON
    )
    print("CONNECTED to Hazelcast")

    movie_poster_map = hz.get_replicated_map('movie_posters').blocking()

    count = 0
    lines = 0
    with open(movie_poster_file, 'rt') as f:
        for line in f:
            lines += 1
            words = line.split(",")
            if len(words) == 0:
                print(f"WARNING: line {lines} appears to be empty, ignoring")
                continue
            if len(words) != 2:
                print(f"ERROR: line {lines} does not have the expected format, ignoring")

            try:
                movie_poster_map.put(int(words[0]), words[1])
                count += 1
            except Exception as x:
                print(f"ERROR loading line {lines}: {x}")

    print(f'loaded {count} of {lines} lines')

    # signal completion
    system_map = hz.get_map("SYSTEM").blocking()
    system_map.put("LOADED", "TRUE")
    hz.shutdown()
