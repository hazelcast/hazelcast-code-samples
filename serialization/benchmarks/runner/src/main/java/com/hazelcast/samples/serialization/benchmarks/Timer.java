package com.hazelcast.samples.serialization.benchmarks;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OperationsPerInvocation;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

import com.hazelcast.core.HazelcastJsonValue;
import com.hazelcast.samples.serialization.benchmarks.MyConstants.Kind;
import com.hazelcast.internal.serialization.Data;
import com.hazelcast.internal.serialization.SerializationService;

@BenchmarkMode({Mode.AverageTime})
@State(Scope.Benchmark)
@Warmup(iterations = 1, batchSize = 10, time = 6, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, batchSize = 100, time = 60, timeUnit = TimeUnit.SECONDS)
@OperationsPerInvocation(1)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Threads(value = 1)
public class Timer {

    @Param({"java.io.Serializable",
        "java.io.Externalizable",
        "com.hazelcast.nio.serialization.DataSerializable",
        "com.hazelcast.nio.serialization.IdentifiedDataSerializable",
        "com.hazelcast.nio.serialization.Portable",
        "com.hazelcast.nio.serialization.VersionedPortable",
        "com.hazelcast.core.HazelcastJsonValue",
        "https://avro.apache.org",
        "https://github.com/EsotericSoftware/kryo",
        "https://developers.google.com/protocol-buffers"
            })
    private String kindStr;

    private SerializationService serializationService;
    private Object[] deserialized;
    private Data[] serialized;

    @Setup(Level.Trial)
    public void setUp() {
        Kind kind = Kind.lookup(this.kindStr);
        this.serializationService = Util.getSerializationService();
        PersonCollection personCollection = null;
        switch (kind) {
        case JAVA_SERIALIZABLE:
            personCollection = Application.getSerializable();
            break;
        case JAVA_EXTERNALIZABLE:
            personCollection = Application.getExternalizable();
            break;
        case HAZELCAST_DATA_SERIALIZABLE:
            personCollection = Application.getDataSerializable();
            break;
        case HAZELCAST_IDENTIFIED_DATA_SERIALIZABLE:
            personCollection = Application.getIdentifiedDataSerializable();
            break;
        case HAZELCAST_PORTABLE:
            personCollection = Application.getPortable();
            break;
        case HAZELCAST_VERSIONED_PORTABLE:
            personCollection = Application.getVersionedPortable();
            break;
        case HAZELCAST_JSON_VALUE:
            personCollection = Application.getHazelcastJson();
            break;
        case AVRO:
            personCollection = Application.getAvro();
            break;
        case KRYO:
            personCollection = Application.getKryo();
            break;
        case PROTOBUF:
            personCollection = Application.getProtobuf();
            break;
        default:
            personCollection = null;
            break;
        }
        this.processPersonCollection(personCollection);
    }

    private void processPersonCollection(PersonCollection personCollection) {
        if (personCollection != null) {
            this.deserialized = new Object[personCollection.getData().size()];
            this.serialized = new Data[this.deserialized.length];
            for (int i = 0 ; i < personCollection.getData().size() ; i++) {
                this.deserialized[i] = personCollection.getData().get(i);
                this.serialized[i] = this.serializationService.toData(this.deserialized[i]);
            }
        } else {
            System.err.println("No personCollection for " + this.kindStr);
        }
    }

    @Benchmark
    public void serialize() {
        for (int i = 0 ; i < this.deserialized.length ; i++) {
            this.serializationService.toData(this.deserialized[i]);
        }
    }

    /* Access data in case deserialization is on LAZY basis.
     * For HazelcastJsonValue turn back into a String, don't then parse with
     * one of the JSON framework as this has it's own cost.
     * However, some JSON parser is required so this cost should still be
     * considered.
     */
    @Benchmark
    public void deserialize() {
        Kind kind = Kind.lookup(this.kindStr);
        for (int i = 0 ; i < this.serialized.length ; i++) {
            Object o = this.serializationService.toObject(this.serialized[i]);
            String issuingCountry = "???";
            switch (kind) {
            case JAVA_SERIALIZABLE:
                this.deserializeJavaSerializable(o, issuingCountry);
                break;
            case JAVA_EXTERNALIZABLE:
                this.deserializeJavaExternalizable(o, issuingCountry);
                break;
            case HAZELCAST_DATA_SERIALIZABLE:
                this.deserializeHazelcastDataSerializable(o, issuingCountry);
                break;
            case HAZELCAST_IDENTIFIED_DATA_SERIALIZABLE:
                this.deserializeHazelcastIdentifiedDataSerializable(o, issuingCountry);
                break;
            case HAZELCAST_PORTABLE:
                this.deserializeHazelcastPortable(o, issuingCountry);
                break;
            case HAZELCAST_VERSIONED_PORTABLE:
                this.deserializeHazelcastVersionedPortable(o, issuingCountry);
                break;
            case HAZELCAST_JSON_VALUE:
                this.deserializeHazelcastJson(o, issuingCountry);
                break;
            case AVRO:
                this.deserializeAvro(o, issuingCountry);
                break;
            case KRYO:
                this.deserializeKryo(o, issuingCountry);
                break;
            case PROTOBUF:
                this.deserializeProtobuf(o, issuingCountry);
                break;
            default:
                issuingCountry = null;
                break;
            }
            this.checkIssuingCountry(issuingCountry, o, this.kindStr);
        }
    }

    private void checkIssuingCountry(String issuingCountry, Object o, String kindStr) {
        if (issuingCountry == null) {
            System.err.println("issuingCountry.length() == null "
                      + kindStr + " on: " + o);
        }
    }

    private void deserializeJavaSerializable(Object o, String issuingCountry) {
        PassportSerializable passportSerializable =
                ((PersonSerializable) o).getPassport();
           if (passportSerializable != null) {
                issuingCountry = passportSerializable.getIssuingCountry();
           }
    }

    private void deserializeJavaExternalizable(Object o, String issuingCountry) {
        PassportExternalizable passportExternalizable =
                ((PersonExternalizable) o).getPassport();
           if (passportExternalizable != null) {
                issuingCountry = passportExternalizable.getIssuingCountry();
           }
    }

    private void deserializeHazelcastDataSerializable(Object o, String issuingCountry) {
        PassportDataSerializable passportDataSerializable =
                ((PersonDataSerializable) o).getPassport();
           if (passportDataSerializable != null) {
                issuingCountry = passportDataSerializable.getIssuingCountry();
           }
    }

    private void deserializeHazelcastIdentifiedDataSerializable(Object o, String issuingCountry) {
        PassportIdentifiedDataSerializable passportIdentifiedDataSerializable =
                ((PersonIdentifiedDataSerializable) o).getPassport();
           if (passportIdentifiedDataSerializable != null) {
                issuingCountry = passportIdentifiedDataSerializable.getIssuingCountry();
           }
    }

    private void deserializeHazelcastPortable(Object o, String issuingCountry) {
        PassportPortable passportPortable =
                ((PersonPortable) o).getPassport();
           if (passportPortable != null) {
                issuingCountry = passportPortable.getIssuingCountry();
           }
    }

    private void deserializeHazelcastVersionedPortable(Object o, String issuingCountry) {
        PassportVersionedPortable passportVersionedPortable =
                ((PersonVersionedPortable) o).getPassport();
           if (passportVersionedPortable != null) {
                issuingCountry = passportVersionedPortable.getIssuingCountry();
           }
    }

    private void deserializeHazelcastJson(Object o, String issuingCountry) {
        String person =
                ((HazelcastJsonValue) o).toString();
           if (person.contains("\"issuingCountry\":\"" + MyConstants.ISSUING_COUNTRY + "\"")) {
                issuingCountry = MyConstants.ISSUING_COUNTRY;
           }
    }

    private void deserializeAvro(Object o, String issuingCountry) {
        PassportAvro passportAvro =
                ((PersonAvro) o).getPassport();
           if (passportAvro != null) {
                issuingCountry = passportAvro.getIssuingCountry();
           }
    }

    private void deserializeKryo(Object o, String issuingCountry) {
        PassportKryo passportKryo =
                ((PersonKryo) o).getPassport();
           if (passportKryo != null) {
                issuingCountry = passportKryo.getIssuingCountry();
           }
    }

    private void deserializeProtobuf(Object o, String issuingCountry) {
        PassportProtobuf passportProtobuf =
                ((PersonProtobuf) o).getPassport();
           if (passportProtobuf != null) {
                issuingCountry = passportProtobuf.getIssuingCountry();
           }
    }
}
