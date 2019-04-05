package your.company.name;

import com.hazelcast.core.IMap;

/**
 * <p>Load the test data into given
 * {@link com.hazelcast.core.IMap IMap}.
 * </p>
 * <p>Five customers with satisfaction ratings
 * recorded from 1 to 5. "{@code Bill}" is happiest
 * with a rating of 5. "{@code Mick}" doesn't
 * have much satisfaction with a rating of 1.
 * </p>
 */
public class TestDataLoader {

    public static void loadTestData(IMap<Integer, Customer> iMap) {
        System.out.printf("%s.loadTestData('%s')%n",
                TestDataLoader.class.getSimpleName(), iMap.getName());

        Customer one = new Customer();
        one.setFirstName("Brian");
        one.setSatisfaction(4);

        Customer two = new Customer();
        two.setFirstName("Mick");
        two.setSatisfaction(1);

        Customer three = new Customer();
        three.setFirstName("Keith");
        three.setSatisfaction(2);

        Customer four = new Customer();
        four.setFirstName("Bill");
        four.setSatisfaction(5);

        Customer five = new Customer();
        five.setFirstName("Charlie");
        five.setSatisfaction(3);

        Customer[] customers = new Customer[] { one, two, three, four, five };

        for (int i = 0 ; i < customers.length; i++) {
            iMap.set(i, customers[i]);
            System.out.printf(" -> %d %s%n", i, customers[i]);
        }

        System.out.println("");
    }

}
