## About

During wan replication, only write events are replicated. This means
that if you use max idle feature with wan replication, entries on the
target cluster may expire, even if the source receives the read events.

As a workaround for this issue, if you don't care about:

 - Entries may live longer than configured max idle or,
 - You calculate the expiration yourself

You can use an `EntryProcessor`.

## Detail

Ideally to solve the issue, we can do write after each read. But that
would be too expensive. To improve, we can try to do write only if the
entry is close to expire. This is better, but we would need to do two
calls from the client if we need to write. To further improve, we can
use `EntryProcessor`.

Logic is simple, let say you want to simulate a five-minute max idle.
You can try to configure a ten-minute ttl and use `EntryProcessor`. This
processor would just return the value normally. But if the entry has
less than 5 minutes to expire, it would do re-write the existing entry
so that the ttl would refresh the expiration time. This way we would
ensure that if an entry is used in the source side, then entry would not
expire in less than five minutes in the target side.

The downsides are also clear. For each longer-than-five-minute-used
entry, for every five minutes, we do write together with read. However,
this write is done by `EntryProcessor` which already uses partition
threads, so the performance impact should be minimal.

## Implementation

In this sample, the above logic is implemented for your convenience. To
summarize:

- `AbstractMaxIdleSimulator`, is a documented class that holds most of 
  the simulation logic. Basically we do reads and writes in a for loop.
  And then see if any non-expired entry is absent in hazelcast map. This
  class is not to run, but to inherit.
- `SingleMaxIdleSimulator` this is basically the scenario described in
  the Details section.
- `MultipleMaxIdleSimulator` it can handle different expiry times in a
  single map compared to the above. Which means some entries have 5
  minute max idle and some others have 10 minutes.
- `BouncingSingleMaxIdleSimulator` during migration, writes are not
  allowed. And since entry processor counts as write, even if it only
  does a read, during migrations, unavailability could be a problem. If
  this unavailability is a problem for you, you can register migration
  listeners and do normal get during migrations.
