package com.hazelcast.samples.serialization.benchmarks;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("checkstyle:classdataabstractioncoupling")
public class Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    // How much data to generate
    private static final int INPUT_SIZE = 100_000;

    // Raw data, with some randomness, but same data for type of PersonCollection
    private static final Object[][] RAW_DATA = createRawData();

    public static void main(String[] args) throws Exception {
        // Use the same raw data to build in each serialization format
        PersonCollection serializable = getSerializable();
        PersonCollection externalizable = getExternalizable();
        PersonCollection dataSerializable = getDataSerializable();
        PersonCollection identifiedDataSerializable = getIdentifiedDataSerializable();
        PersonCollection portable = getPortable();
        PersonCollection versionedPortable = getVersionedPortable();
        PersonCollection compact = getCompact();
        PersonCollection hazelcastJson = getHazelcastJson();
        PersonCollection avro = getAvro();
        PersonCollection kryo = getKryo();
        PersonCollection protobuf = getProtobuf();

        List<PersonCollection> personsInDifferentFormats
            = Arrays.asList(serializable, externalizable, dataSerializable,
                    identifiedDataSerializable, portable, versionedPortable, compact, hazelcastJson,
                    avro, kryo, protobuf);

        System.out.println();
        System.out.println("~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ");
        System.out.println("START:  " + new Date());
        System.out.println("~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ");

        System.out.println();
        System.out.println("Sizes for " + Util.getNumberFormat().format(INPUT_SIZE) + " records.");
        for (PersonCollection p : personsInDifferentFormats) {
            Sizer.size(p.getKind(), p.getData());
        }

        System.out.println();
        System.out.println("~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ");
        System.out.println("MIDDLE: " + new Date());
        System.out.println("~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ");

        System.out.println();
        Options options = new OptionsBuilder()
                .include(Timer.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(options).run();

        System.out.println();
        System.out.println("~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ");
        System.out.println("END:    " + new Date());
        System.out.println("~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ");
        System.out.println();
    }

    /**
     * <p>Create the test data to be used by all serialization
     * formats, a list of first name, last name and a flag
     * for whether the person has a passport.
     * </p>
     * <p>We use the build timestamp so all runs use the same
     * random numbers, as JMH will fork processes.
     * </p>
     *
     * @return A collection of <String, String, boolean>
     */
    private static Object[][] createRawData() {

        long seed = 0L;
        ClassLoader classLoader = Application.class.getClassLoader();
        try (InputStream inputStream = classLoader.getResourceAsStream("application.properties");) {
            Properties properties = new Properties();
            properties.load(inputStream);

            String seedDateStr = properties.getProperty("maven.build.timestamp");

            LocalDateTime localDateTime = LocalDateTime.parse(seedDateStr);
            seed = localDateTime.atZone(ZoneId.systemDefault()).toEpochSecond();

            LOGGER.debug("Random seed {} => {}", seedDateStr, seed);
        } catch (Exception e) {
            LOGGER.error("Cannot determine random seed", e);
        }

        // All runs used the same seed, from the build
        Random random = new Random(seed);
        Object[][] data = new Object[INPUT_SIZE][];

        int firstNameMax = MyConstants.FIRST_NAMES.size();
        int lastNameMax = MyConstants.LAST_NAMES.size();

        for (int i = 0 ; i < INPUT_SIZE; i++) {
            int firstNameIndex = random.nextInt(firstNameMax);
            int lastNameIndex = random.nextInt(lastNameMax);

            // First object needs passport, as first object size reported == worst case
            Boolean hasPassport = i == 0 ? true : random.nextBoolean();

            Object[] datum =
                { MyConstants.FIRST_NAMES.get(firstNameIndex),
                    MyConstants.LAST_NAMES.get(lastNameIndex),
                    hasPassport,
                };

            data[i] = datum;
        }

        return data;
    }

    // Accessors for the kinds
    public static PersonCollection getSerializable() {
        return new PersonSerializableCollectionBuilder().addData(RAW_DATA).build();
    }
    public static PersonCollection getExternalizable() {
        return new PersonExternalizableCollectionBuilder().addData(RAW_DATA).build();
    }
    public static PersonCollection getDataSerializable() {
        return new PersonDataSerializableCollectionBuilder().addData(RAW_DATA).build();
    }
    public static PersonCollection getIdentifiedDataSerializable() {
        return new PersonIdentifiedDataSerializableCollectionBuilder().addData(RAW_DATA).build();
    }
    public static PersonCollection getPortable() {
        return new PersonPortableCollectionBuilder().addData(RAW_DATA).build();
    }
    public static PersonCollection getVersionedPortable() {
        return new PersonVersionedPortableCollectionBuilder().addData(RAW_DATA).build();
    }
    public static PersonCollection getCompact() {
        return new PersonCompactCollectionBuilder().addData(RAW_DATA).build();
    }
    public static PersonCollection getHazelcastJson() {
        return new PersonJsonCollectionBuilder().addData(RAW_DATA).build();
    }
    public static PersonCollection getAvro() {
        return new PersonAvroCollectionBuilder().addData(RAW_DATA).build();
    }
    public static PersonCollection getKryo() {
        return new PersonKryoCollectionBuilder().addData(RAW_DATA).build();
    }
    public static PersonCollection getProtobuf() {
        return new PersonProtobufCollectionBuilder().addData(RAW_DATA).build();
    }
}
