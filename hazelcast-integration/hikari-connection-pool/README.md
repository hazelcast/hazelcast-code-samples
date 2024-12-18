<h1>Hazelcast Integration with Hikari Connection Pool</h1>
To create Hikari connection pool, first create database table mapping with Hazelcast in-memory map.
In this demo, I have used EMPLOYEE table created on H2 DB, which is mapped with employee-map.

- Create Hikari data source from HikariConfig, with database credentials and connection pool configuration e.g. pool size, connection timeout etc.
- Get the connection from Hikari Data Source.
- Implement the Map Store Factory from MapLoader<K, V>, MapStore<K, V> and override all the necessary methods.
- Register the MapStore into hazelcast.xml config.
   
  
    <hazelcast>
     ...
        <map name="empaloyee_map">
           <map-store enabled="true">
             <class-name>com.demo.maps.EmployeeMapStore</class-name>
           </map-store>
        </map>
     ...
    </hazelcast>

Database connection information can be externalized from hazelcast.xml. Build the above classes in a jar and copy them inside hazelcast user-lib directory under the Hazelcast installation.