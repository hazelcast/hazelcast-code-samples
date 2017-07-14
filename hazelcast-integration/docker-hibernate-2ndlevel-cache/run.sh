mvn clean install
mvn exec:java -Dexec.mainClass="com.hazelcast.hibernate.CreateTable" -Dexec.cleanupDaemonThreads=false
mvn exec:java -Dexec.mainClass="com.hazelcast.hibernate.ManageEmployee"
