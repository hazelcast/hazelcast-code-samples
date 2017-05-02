import com.hazelcast.aggregation.Aggregator;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class SimpleFastAggregationsDemo {

    // predefined companies
    private static final String[] COMPANIES = {"Hazelcast", "Hazelnut", "Hazelstorm", "Stormcast", "Nutcast"};
    private static final String[] FIRST_NAMES = {"Peter", "Greg", "Chris", "Talip", "Fuad", "Mehmet", "Miko", "Asim", "Enes"};
    private static final String[] LAST_NAMES
            = {"Veentjer", "Luck", "Engelbert", "Ozturk", "Malikov", "Matsumura", "Arslan", "Akar"};

    public static void main(String[] args) {
        // build Hazelcast cluster
        System.out.println("Starting instance 1");
        Hazelcast.newHazelcastInstance();
        System.out.println("Starting instance 2");
        Hazelcast.newHazelcastInstance();
        System.out.println("Starting instance 3");
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();

        // retrieve the Hazelcast IMap
        IMap<String, Employee> employees = hz.getMap("employees");

        // fill in demo data
        fillEmployeeMap(employees);

        // we simple calculate a average over all salaries on all employees
        simpleCustomAverageAggregation(employees);

        Hazelcast.shutdownAll();
    }

    private static void simpleCustomAverageAggregation(IMap<String, Employee> employees) {
        System.out.println("Calculating salary average");

        double avgSalary = employees.aggregate(new Aggregator<Map.Entry<String, Employee>, Double>() {

            protected long sum;
            protected long count;

            @Override
            public void accumulate(Map.Entry<String, Employee> entry) {
                count++;
                sum += entry.getValue().getSalaryPerMonth();
            }

            @Override
            public void combine(Aggregator aggregator) {

                this.sum += this.getClass().cast(aggregator).sum;
                this.count += this.getClass().cast(aggregator).count;
            }

            @Override
            public Double aggregate() {
                if (count == 0) {
                    return null;
                }
                return ((double) sum / (double) count);
            }

        });

        System.out.println("Overall average salary: " + avgSalary);
        System.out.println("\n");
    }

    private static void fillEmployeeMap(IMap<String, Employee> employees) {
        Random random = new Random();
        for (int i = 0; i < 10000; i++) {
            String companyName = COMPANIES[random.nextInt(COMPANIES.length)];
            String firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
            String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
            int salaryPerMonth = 2800 + random.nextInt(2000);

            Employee employee = new Employee();
            employee.setCompanyName(companyName);
            employee.setFirstName(firstName);
            employee.setLastName(lastName);
            employee.setSalaryPerMonth(salaryPerMonth);

            String key = UUID.randomUUID().toString();
            employees.put(key, employee);

        }
        System.out.println("Employee map filled.");
    }


}
