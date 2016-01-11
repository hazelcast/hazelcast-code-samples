import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

import java.io.IOException;

public class CarStreamSerializer implements StreamSerializer<Car> {

    @Override
    public int getTypeId() {
        return MySerializationConstants.CAR_TYPE.getId();
    }

    @Override
    public void write(ObjectDataOutput out, Car car) throws IOException {
        out.writeObject(car.getOwner());
        out.writeUTF(car.getColor());
    }

    @Override
    public Car read(ObjectDataInput in) throws IOException {
        Person owner = in.readObject();
        String color = in.readUTF();
        return new Car(owner, color);
    }

    @Override
    public void destroy() {
    }
}
