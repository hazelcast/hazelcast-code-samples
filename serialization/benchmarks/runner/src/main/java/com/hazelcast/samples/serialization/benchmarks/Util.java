package com.hazelcast.samples.serialization.benchmarks;

import java.text.NumberFormat;
import java.util.Locale;

import com.hazelcast.config.SerializationConfig;
import com.hazelcast.config.SerializerConfig;
import com.hazelcast.internal.serialization.SerializationService;
import com.hazelcast.internal.serialization.impl.DefaultSerializationServiceBuilder;

public class Util {
    private static NumberFormat numberFormat = NumberFormat.getInstance(Locale.getDefault());
    private static SerializationService serializationService;

    /**
     * <p>A serialization service to serialize and deserialize
     * Hazelcast format objects.
     * </p>
     *
     * @return
     */
    private static SerializationService createSerializationService() {

        SerializerConfig personAvroSerializerConfig = new SerializerConfig();
        personAvroSerializerConfig.setTypeClass(PersonAvro.class);
        personAvroSerializerConfig.setClass(PersonAvroSerializer.class);

        SerializerConfig personKryoSerializerConfig = new SerializerConfig();
        personKryoSerializerConfig.setTypeClass(PersonKryo.class);
        personKryoSerializerConfig.setClass(PersonKryoSerializer.class);

        SerializerConfig personProtobufSerializerConfig = new SerializerConfig();
        personProtobufSerializerConfig.setTypeClass(PersonProtobuf.class);
        personProtobufSerializerConfig.setClass(PersonProtobufSerializer.class);

        SerializationConfig serializationConfig = new SerializationConfig();
        serializationConfig.addSerializerConfig(personAvroSerializerConfig);
        serializationConfig.addSerializerConfig(personKryoSerializerConfig);
        serializationConfig.addSerializerConfig(personProtobufSerializerConfig);

        SerializationService serializationService =
                new DefaultSerializationServiceBuilder()
                .addDataSerializableFactory(MyConstants.MY_IDENTIFIED_DATA_SERIALIZABLE_FACTORY_ID,
                        new MyIdentifiedDataSerializableFactory())
                .addPortableFactory(MyConstants.MY_PORTABLE_FACTORY_ID,
                        new MyPortableFactory())
                .addPortableFactory(MyConstants.MY_VERSIONED_PORTABLE_FACTORY_ID,
                        new MyVersionedPortableFactory())
                .setConfig(serializationConfig)
                .build();

        preRegisterNullTypes(serializationService);

        return serializationService;
    }

    public static SerializationService getSerializationService() {
        if (serializationService == null) {
            serializationService = createSerializationService();
        }
        return serializationService;
    }

    @SuppressWarnings("checkstyle:linelength")
    /**
     * <p>See
     * <a href="https://docs.hazelcast.com/imdg/4.1.2/serialization/implementing-portable-serialization.html#null-portable-serialization">
     * here</a>, to correctly handle a null for a type it cannot be unknown to Hazelcast.
     * <p>
     */
    private static void preRegisterNullTypes(SerializationService arg0) {
        PassportPortable passportPortable = new PassportPortable();
        PassportVersionedPortable passportVersionedPortable = new PassportVersionedPortable();

        arg0.toData(passportPortable);
        arg0.toData(passportVersionedPortable);
    }

    public static NumberFormat getNumberFormat() {
        return numberFormat;
    }

}
