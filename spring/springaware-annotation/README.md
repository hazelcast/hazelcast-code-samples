# Hazelcast SpringAware Configuration

You can inject Spring Context into Hazelcast objects. This code sample presents how to do it.

## Using Spring Annotation-based Configuration

Execute the following command.

    mvn compile exec:java -Dexec.mainClass="com.hazelcast.spring.springaware.SpringAwareAnnotationJavaConfig" -Dexec.cleanupDaemonThreads=false

## Hazelcast Spring XML Configuration

Execute the following command.

    mvn compile exec:java -Dexec.mainClass="com.hazelcast.spring.springaware.SpringAwareAnnotationXMLConfig" -Dexec.cleanupDaemonThreads=false

