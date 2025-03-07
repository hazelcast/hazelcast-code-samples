## Hazelcast Movie Recommendation Pipeline Demo
___
This accelerator demonstrates how to turn a Python ML model into a scaleable, 
low-latency recommendation service.

Specific patterns demonstrated include
- Using a Pipeline deployed to a Hazelcast cluster as the back-end for 
a standard Spring Boot REST web service.
- Using Hazelcast to host ML written in Python
- Combining Python and Java in a single Hazelcast Pipeline.


## Prerequisites

Docker Desktop is required.

Before running this project, clone https://github.com/hazelcast-guides/spring-hazelcast-pipeline-dispatcher and install it locally by running 
`mvn clean install`. Verify that the version of `spring-hazelcast-pipeline-dispatcher` 
matches the one needed by this project (see `hazelcast.pipeline.dispatcher.version` in `pom.xml`).  If it does not then check out the required version (`git checkout n.n.n`) and build it locally with `mvn clean install`.

This demo needs ports 80, 8888 and 8080 on your local machine.  You can adjust the 
ports used in `compose.yaml`.

## Walk Through

First, build all of the java projects: `mvn clean install`

Build the images: `docker compose build`

Pull the stock images: `docker compose pull`.  This step will take a while 
because the combined Hazelcast + Python + Pandas/Numpy/Scikit is large.  You will 
only need to do this once.

Start everything: `docker compose up -d --scale hz=3`.  This command starts 3 
hazelcast nodes.  If you omit the `--scale hz=3` then only one node will be started.

Monitor with `docker compose ps`.  Once `submit-job` has run and exited with a 0 
status everything is fully up.

Access the UI: http://localhost/movies

Send an HTTP GET directly to the web service (e.g. with Postman or your Browser):
http://localhost:8888/recommendations?like=Aliens

Management Center: http://localhost:8080

When you are done, stop everything: `docker compose down`

## Additional Notes 
- Data from [Movinder's set](github.com/Movinder/movielens-imdb-exploration).
