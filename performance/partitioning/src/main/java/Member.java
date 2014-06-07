import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.core.IMap;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

import java.io.IOException;
import java.util.UUID;

public class Member {

    public final static int CUSTOMER_COUNT = 1000;

    public final static int ORDER_PER_CUSTOMER_COUNT = 1000;

    private final HazelcastInstance hz;
    private final IMap<Object, Object> orders;
    private final IMap<Object, Object> customers;

    public Member() {
        hz = Hazelcast.newHazelcastInstance();
        customers = hz.getMap("customers");
        orders = hz.getMap("orders");
    }

    public static void main(String[] args) {
        Member member = new Member();
        member.generateTestData();
        member.run();
    }

    public void generateTestData() {
        for (int k = 0; k < CUSTOMER_COUNT; k++) {
            Customer customer = new Customer();
            customer.setCustomerId(UUID.randomUUID().toString());
            customers.put(customer.getCustomerId(), customer);

            for (int l = 0; l < ORDER_PER_CUSTOMER_COUNT; l++) {
                Order order = new Order();
                order.setOrderId(UUID.randomUUID().toString());
                order.setCustomerId(customer.getCustomerId());
                orders.put(order.getOrderId(), order);
            }
        }
    }

    public void run() {

    }

    private static class SomeTask implements DataSerializable, Runnable, HazelcastInstanceAware {
        private HazelcastInstance hz;

        @Override
        public void setHazelcastInstance(HazelcastInstance hz) {
            this.hz = hz;
        }

        @Override
        public void run() {

        }

        @Override
        public void writeData(ObjectDataOutput out) throws IOException {

        }

        @Override
        public void readData(ObjectDataInput in) throws IOException {

        }
    }

    private static class Foo implements DataSerializable, Runnable, HazelcastInstanceAware {
        private HazelcastInstance hz;

        @Override
        public void setHazelcastInstance(HazelcastInstance hz) {
            this.hz = hz;
        }

        @Override
        public void run() {

        }

        @Override
        public void writeData(ObjectDataOutput out) throws IOException {

        }

        @Override
        public void readData(ObjectDataInput in) throws IOException {

        }
    }
}
