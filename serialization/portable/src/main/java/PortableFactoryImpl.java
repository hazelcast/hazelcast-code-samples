import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableFactory;

public class PortableFactoryImpl implements PortableFactory {

    static final int PERSON_CLASS_ID = 1;
    static final int FACTORY_ID = 1;

    public Portable create(int classId) {
        switch (classId) {
            case PERSON_CLASS_ID:
                return new Person();
            default:
                return null;
        }
    }
}
