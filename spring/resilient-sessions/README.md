# Resilient web sessions example

This example is associated with blog post (link will be posted post-release).

To start a sample cluster, go to `docker` folder, create `licensekey.env` with `HZ_LICENSEKEY=<your key here>` and then run `docker-compose up`.

Individual nodes can be started or stopped using `docker up/down <instance>`, where `<instance>` is either `london-cluster` or `nyc-cluster`.

After that, you can run `Main` class.
