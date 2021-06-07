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
            	PassportSerializable passportSerializable = 
            		((PersonSerializable)o).getPassport();
            	if (passportSerializable != null) {
            		issuingCountry = passportSerializable.getIssuingCountry();
            	}
                break;
            case JAVA_EXTERNALIZABLE:
            	PassportExternalizable passportExternalizable =
            		((PersonExternalizable)o).getPassport();
            	if (passportExternalizable != null) {
            		issuingCountry = passportExternalizable.getIssuingCountry();
            	}
                break;
            case HAZELCAST_DATA_SERIALIZABLE:
            	PassportDataSerializable passportDataSerializable =
            		((PersonDataSerializable)o).getPassport();
            	if (passportDataSerializable != null) {
            		issuingCountry = passportDataSerializable.getIssuingCountry();
            	}
                break;
            case HAZELCAST_IDENTIFIED_DATA_SERIALIZABLE:
            	PassportIdentifiedDataSerializable passportIdentifiedDataSerializable =
            		((PersonIdentifiedDataSerializable)o).getPassport();
            	if (passportIdentifiedDataSerializable != null) {
            		issuingCountry = passportIdentifiedDataSerializable.getIssuingCountry();
            	}
                break;
            case HAZELCAST_PORTABLE:
            	PassportPortable passportPortable =
            		((PersonPortable)o).getPassport();
            	if (passportPortable != null) {
            		issuingCountry = passportPortable.getIssuingCountry();
            	}
                break;
            case HAZELCAST_VERSIONED_PORTABLE:
            	PassportVersionedPortable passportVersionedPortable =
            		((PersonVersionedPortable)o).getPassport();
            	if (passportVersionedPortable != null) {
            		issuingCountry = passportVersionedPortable.getIssuingCountry();
            	}
                break;
            case HAZELCAST_JSON_VALUE:
            	String person = 
            		((HazelcastJsonValue)o).toString();
            	if (person.contains("\"issuingCountry\":\"" + MyConstants.ISSUING_COUNTRY + "\"")) {
            		issuingCountry = MyConstants.ISSUING_COUNTRY;
            	}
                break;
            case AVRO:
            	PassportAvro passportAvro =
            		((PersonAvro)o).getPassport();
            	if (passportAvro != null) {
            		issuingCountry = passportAvro.getIssuingCountry();
            	}
                break;
            case KRYO:
            	PassportKryo passportKryo =
            		((PersonKryo)o).getPassport();
            	if (passportKryo != null) {
            		issuingCountry = passportKryo.getIssuingCountry();
            	}
                break;
            case PROTOBUF:
            	PassportProtobuf passportProtobuf =
            		((PersonProtobuf)o).getPassport();
            	if (passportProtobuf != null) {
            		issuingCountry = passportProtobuf.getIssuingCountry();
            	}
                break;
            default:
            	issuingCountry = null;
                break;
            }
            if (issuingCountry == null) {
                System.err.println("issuingCountry.length() == null " + 
                		this.kindStr + " on: " + o);
            }
        }
    }

}
