package com.hazelcast.samples.serialization.benchmarks;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.hazelcast.config.SerializationConfig;
import com.hazelcast.config.SerializerConfig;
import com.hazelcast.internal.serialization.SerializationService;
import com.hazelcast.internal.serialization.impl.DefaultSerializationServiceBuilder;
import com.hazelcast.internal.serialization.impl.compact.Schema;
import com.hazelcast.internal.serialization.impl.compact.SchemaService;

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

        // Taken from CompactTestUtil.createInMemorySchemaService
        SchemaService simpleSchemaService = new SchemaService() {
            private final Map<Long, Schema> schemas = new ConcurrentHashMap<>();

            @Override
            public Schema get(long schemaId) {
                return schemas.get(schemaId);
            }

            @Override
            public void put(Schema schema) {
                long schemaId = schema.getSchemaId();
                Schema existingSchema = schemas.putIfAbsent(schemaId, schema);
                if (existingSchema != null && !schema.equals(existingSchema)) {
                    throw new IllegalStateException("Schema with schemaId " + schemaId + " already exists. "
                            + "existing schema " + existingSchema
                            + "new schema " + schema);
                }
            }

            @Override
            public void putLocal(Schema schema) {
                put(schema);
            }
        };

        SerializationService serializationService =
                new DefaultSerializationServiceBuilder()
                .addDataSerializableFactory(MyConstants.MY_IDENTIFIED_DATA_SERIALIZABLE_FACTORY_ID,
                        new MyIdentifiedDataSerializableFactory())
                .addPortableFactory(MyConstants.MY_PORTABLE_FACTORY_ID,
                        new MyPortableFactory())
                .addPortableFactory(MyConstants.MY_VERSIONED_PORTABLE_FACTORY_ID,
                        new MyVersionedPortableFactory())
                .setConfig(serializationConfig)
                .setSchemaService(simpleSchemaService)
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
