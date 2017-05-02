import com.hazelcast.aggregation.Aggregators;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.Predicate;

import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class FastAggregationsDemo {

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
        simpleSalaryAverage(employees);

        // here we first search Hazelcast employees using the Predicate and calculate the average only on those
        companyBasedSalaryAverage(employees);

        // now we just sum up the salaries
        salarySum(employees);

        // this time we count the employees that we calculate on (let's expect it to be 10k ;-))
        countEmployees(employees);

        // last but not least we want to get all distinct first names of the employees
        distinctEmployeeFirstNames(employees);

        Hazelcast.shutdownAll();
    }

    private static void simpleSalaryAverage(IMap<String, Employee> employees) {
        System.out.println("Calculating salary average");

        // execute the aggregation and print the result
        double avgSalary = employees.aggregate(Aggregators.<Map.Entry<String, Employee>>integerAvg("salaryPerMonth"));
        System.out.println("Overall average salary: " + avgSalary);
        System.out.println("\n");
    }

    private static void companyBasedSalaryAverage(IMap<String, Employee> employees) {
        System.out.println("Calculating average monthly salary for Hazelcast");

        // create the Predicate to select only Hazelcast employees
        Predicate<String, Employee> companyPredicate = new CompanyPredicate("Hazelcast");

        // execute the aggregation and print the result
        double avgSalary = employees
                .aggregate(Aggregators.<Map.Entry<String, Employee>>integerAvg("salaryPerMonth"), companyPredicate);
        System.out.println("Hazelcast average salary: " + avgSalary);
        System.out.println("\n");
    }

    private static void salarySum(IMap<String, Employee> employees) {
        // execute the aggregation and print the result
        long sumSalary = employees.aggregate(Aggregators.<Map.Entry<String, Employee>>integerSum("salaryPerMonth"));
        System.out.println("Sum of all salaries: " + sumSalary);
        System.out.println("\n");
    }

    private static void countEmployees(IMap<String, Employee> employees) {
        // execute the aggregation and print the result
        long countEmployee = employees.aggregate(Aggregators.<Map.Entry<String, Employee>>count());
        System.out.println("Number of employees: " + countEmployee);
        System.out.println("\n");
    }

    private static void distinctEmployeeFirstNames(IMap<String, Employee> employees) {
        // execute the aggregation and print the result
        Set<String> allFirstNames = employees.aggregate(Aggregators.<Map.Entry<String, Employee>, String>distinct("firstName"));
        System.out.println("All first names: " + allFirstNames);
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
