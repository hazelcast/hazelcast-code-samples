import com.hazelcast.nio.serialization.DataSerializableFactory;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class PersonDataSerializableFactory implements DataSerializableFactory {

    public static final int ID = 1;

    public static final int PERSON_TYPE = 1;

    @Override
    public IdentifiedDataSerializable create(int typeId) {
        if (typeId == PERSON_TYPE) {
            return new Person();
        } else {
            return null;
        }
    }
}
