# Hazelcast

## The Real-Time Patient Monitoring Application

### Overview
The Real-Time Patient Monitoring application is modeled after the NEWS2 criteria. There are eight devices monitoring the patient and these devices capture and send real-time patient data to Hazelcast.

The solution simulates an environment where these eight (8) physiological parameters are captured in real-time from devices monitoring the patient. Hazelcast processes these data streams in real-time and generates a score for each patient. This score is displayed in a dashboard which a medical professional would view to determine if a patient required further treatment.
<br>

### Table of Contents
- <a href="#prerequisites">Prerequisites</a>
- <a href="#download">Download</a>
- <a href="#installation">Installation</a>
- <a href="#getting-started">Getting Started</a>
- <a href="#code-description">Code Description</a>
- <a href="#troubleshooting">Troubleshooting</a>
- <a href="#contributing">Contributing</a>
- <a href="#license">License</a>
- <a href="#additional-resources">Additional Resources</a>
<br><br>

### Prerequisites
- Hazelcast v5.1.4
- Hazelcast Management Center v5.1.4
- Docker - Docker Desktop v4.11.1 (84025)
- Graphite v1.1.10-1 (Using Docker image)
- Grafana v9.0.6
- Kafka (with Zookeeper) v2.13-3.3.1
- Java v1.8
- Python v3.8
<br><br>

### Download
The source code and data files for the application are stored in GitHub and can be downloaded from this location: https://github.com/hazelcast-demos/patient-monitoring
<br><br>

### Installation
Hazelcast Properties File
For convenience and consistency property values are stored in the hazelcast.properties file located in the Java resources folder.
<br><br>

### Getting Started
After the code has been downloaded to your computer and all the prerequisites have been met you should be able to run the application.

Navigate to the scripts folder and review file paths in all the scripts to confirm the prerequisites match the installation locations. If the file path needs adjusting, now is the time to do it. After the file paths have been confirmed, open a terminal, go to the scripts folder and enter "sh ./main-start.sh" to run the script (you may need to run it under sudo if permissions are an issue. To stop the processes use the "sh ./main-stop.sh" script.
<br><br>

### Code Description

#### com.hazelcast.app

1. com.hazelcast.app.common
   1. Resource Class - The Resource class loads all the values into memory from the Hazelcast Properties file, these values are used throughout the application to customize the behavior of the program.
      <br><br>
2. com.hazelcast.app.sink
   1. GraphiteMetric Class - The Graphic Metric class is a helper class for building results to send to Graphite, a time-series database, which is serving as the data sink for our application. The constructor is overloaded to handle string, integer, and float data types.
   2. Utils Class - The Utils class is a helper class that connects the application to the Graphite, a time-series database, which is serving as the data sink for our application. The Utils class is called extensively from the PipelineImpl (buildGraphiteSink) and RulesEngine (buildGraphiteSinkResults) classes.
      <br><br>
3. com.hazelcast.app.source
   1. MessageBusFactory Class - The Message Bus Factory class creates each MessageBusImpl object based on TOPIC_MAP_NAME_ARR values. This class implements the Runnable interface.
   2. MessageBusImpl Class - The Message Bus Implementation class loads data from the devices data files (.CSV) into the Message Bus (Apache Kafka topics).
      <br><br>
4. com.hazelcast.app.stream
   1. ProcessEngine Class - The Process Engine class starts and runs the PipelineFactory and RulesEngine classes.
      1. com.hazelcast.app.common.connection
         1. ClientConnection Class - The Client Connection class creates and manages the connection between the application and Hazelcast.
      2. com.hazelcast.app.common.person
         1. Patient Class - The Patient class holds all the values of the patient and the associated devices.
      3. com.hazelcast.app.stream.pipeline
         1. PipelineFactory Class
         2. PipelineImpl Class
      4. com.hazelcast.app.stream.rulesengine
         1. RulesEngine Class - The Rules Engine class manages the process of taking the Patient record and scoring it. Patient record is scored using a Python module developed specifically for this purpose.
            <br><br>
5. Main Class - The Main class starts the application by starting the MessageBusFactory class, instantiating the Resource class to load the Hazelcast Properties file, connecting to Hazelcast with the ClientConnection class, and finally starting the startStreamProcessingEngine method.
   <br><br>

### Troubleshooting
Loren ipsum
<br><br>

### Contributing
> We welcome your contributions! Please read [CONTRIBUTING](CONTRIBUTING.md) for details on how to submit contributions to this project.
<br><br>

### License
> This project is licensed under the [Apache 2.0 License](LICENSE).
<br><br>

### Additional Resources
For more information, see:
- [Hazelcast Documentation](https://docs.hazelcast.com/hazelcast/5.1/)
- [Tutorials](https://docs.hazelcast.com/tutorials/?product=platform)
- [Hazelcast.com](https://www.hazelcast.com)
