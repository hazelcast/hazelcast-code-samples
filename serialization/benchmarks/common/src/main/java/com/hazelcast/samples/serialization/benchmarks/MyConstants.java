package com.hazelcast.samples.serialization.benchmarks;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class MyConstants {

    // Factory IDs must be unique
    public static final int MY_IDENTIFIED_DATA_SERIALIZABLE_FACTORY_ID = 1000;
    public static final int MY_PORTABLE_FACTORY_ID = MY_IDENTIFIED_DATA_SERIALIZABLE_FACTORY_ID + 1;
    public static final int MY_VERSIONED_PORTABLE_FACTORY_ID = MY_PORTABLE_FACTORY_ID + 1;
    public static final int PERSON_AVRO_SERIALIZER = MY_VERSIONED_PORTABLE_FACTORY_ID + 1;
    public static final int PERSON_KRYO_SERIALIZER = PERSON_AVRO_SERIALIZER + 1;
    public static final int PERSON_PROTOBUF_SERIALIZER = PERSON_KRYO_SERIALIZER + 1;

    // Class IDs must be unique per factory
    public static final int PASSPORT_IDENTIFIED_DATA_SERIALIZABLE_ID = 1;
    public static final int PASSPORT_PORTABLE_ID = PASSPORT_IDENTIFIED_DATA_SERIALIZABLE_ID + 1;
    public static final int PASSPORT_VERSIONED_PORTABLE_ID = PASSPORT_PORTABLE_ID + 1;
    public static final int PERSON_IDENTIFIED_DATA_SERIALIZABLE_ID = PASSPORT_VERSIONED_PORTABLE_ID + 1;
    public static final int PERSON_PORTABLE_ID = PERSON_IDENTIFIED_DATA_SERIALIZABLE_ID + 1;
    public static final int PERSON_VERSIONED_PORTABLE_ID = PERSON_PORTABLE_ID + 1;

    // Not using versioning here for sizing/timing
    public static final int PASSPORT_VERSIONED_PORTABLE_CLASS_VERSION = 1;
    public static final int PERSON_VERSIONED_PORTABLE_CLASS_VERSION = 1;

    // For generating test data
    public static final List<String> FIRST_NAMES = Arrays.asList("William", "Patrick", "Jon", "Tom", "Peter", "Colin",
            "Sylvester", "Paul", "Christopher", "David", "Matt", "Peter", "Jodie", "Julie");
    public static final List<String> LAST_NAMES = Arrays.asList("Hartnell", "Troughton", "Pertwee", "Baker", "Davison",
            "Baker", "McCoy", "McGann", "Eccleston", "Tennant", "Smith", "Capaldi", "Whittaker", "Forster");

    // Serialization types, with enum names and printing names
    public enum Kind {
        JAVA_SERIALIZABLE("java.io.Serializable"),
        JAVA_EXTERNALIZABLE("java.io.Externalizable"),
        HAZELCAST_DATA_SERIALIZABLE("com.hazelcast.nio.serialization.DataSerializable"),
        HAZELCAST_IDENTIFIED_DATA_SERIALIZABLE("com.hazelcast.nio.serialization.IdentifiedDataSerializable"),
        HAZELCAST_PORTABLE("com.hazelcast.nio.serialization.Portable"),
        HAZELCAST_VERSIONED_PORTABLE("com.hazelcast.nio.serialization.VersionedPortable"),
        HAZELCAST_COMPACT("com.hazelcast.nio.serialization.Compact"),
        HAZELCAST_JSON_VALUE("com.hazelcast.core.HazelcastJsonValue"),
        AVRO("https://avro.apache.org"),
        KRYO("https://github.com/EsotericSoftware/kryo"),
        PROTOBUF("https://developers.google.com/protocol-buffers");

        private final String klass;

        Kind(String arg0) {
            this.klass = arg0;
        }

        @Override
        public String toString() {
            return this.klass;
        }

        // Can't use valueOf(s) easily
        public static Kind lookup(String s) {
            for (Kind kind : Kind.values()) {
                if (kind.toString().equals(s)) {
                    return kind;
                }
            }
            return null;
        }
    }

    // For passport, constants are fine as we only care for their length
    public static final String ISSUING_COUNTRY = "GBR";
    public static final long ISSUING_DATE = System.currentTimeMillis();
    public static final String EXPIRY_DATE = LocalDate.now().toString();

}
