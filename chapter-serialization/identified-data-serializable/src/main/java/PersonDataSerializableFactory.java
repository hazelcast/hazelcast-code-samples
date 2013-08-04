import com.hazelcast.nio.serialization.DataSerializableFactory;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class PersonDataSerializableFactory implements DataSerializableFactory{

    public static final int ID = 1;

    private static int ID_GENERATOR = 1;
    public static final int PERSON_TYPE = ID_GENERATOR++;

    @Override
    public IdentifiedDataSerializable create(int typeId) {
        return typeId == 1? new Person():null;
    }
}
