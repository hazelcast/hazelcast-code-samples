import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.projection.Projection;
import com.hazelcast.query.Predicate;

import java.util.Collection;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class ProjectionsDemo {

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

        // return single field value
        returnSingleFieldValueWithPredicate(employees);

        // combine a couple of field values
        returnTransformedFieldsValue(employees);

        Hazelcast.shutdownAll();
    }

    private static void returnSingleFieldValueWithPredicate(IMap<String, Employee> employees) {
        System.out.println("Doing a projection of a single field value");

        // create the Predicate to select only Hazelcast employees
        Predicate<String, Employee> companyPredicate = new CompanyPredicate("Hazelcast");

        // execute the aggregation and print the result
        Collection<String> names = employees.project(new Projection<Map.Entry<String, Employee>, String>() {
            @Override
            public String transform(Map.Entry<String, Employee> entry) {
                return entry.getValue().getFirstName();
            }
        }, companyPredicate);
        System.out.println("Projected names: " + names);
        System.out.println("\n");
    }

    private static void returnTransformedFieldsValue(IMap<String, Employee> employees) {
        System.out.println("Doing a projection of a couple of field values");

        // create the Predicate to select only Hazelcast employees
        Predicate<String, Employee> companyPredicate = new CompanyPredicate("Hazelcast");

        // execute the aggregation and print the result
        Collection<String> names = employees.project(new Projection<Map.Entry<String, Employee>, String>() {
            @Override
            public String transform(Map.Entry<String, Employee> entry) {
                return entry.getValue().getFirstName() + ":" + entry.getValue().getSalaryPerMonth();
            }
        }, companyPredicate);
        System.out.println("Projected names and salaries: " + names);
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
