import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableFactory;

public class PortableFactoryImpl implements PortableFactory {
    public final static int PERSON_CLASS_ID = 1;
    public final static int FACTORY_ID = 1;

    public Portable create(int classId) {
        switch (classId) {
            case PERSON_CLASS_ID:
                return new Person();
        }
        return null;
    }
}
