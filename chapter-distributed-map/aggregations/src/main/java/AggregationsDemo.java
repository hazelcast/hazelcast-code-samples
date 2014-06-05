import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.mapreduce.aggregation.Aggregation;
import com.hazelcast.mapreduce.aggregation.Aggregations;
import com.hazelcast.mapreduce.aggregation.PropertyExtractor;
import com.hazelcast.mapreduce.aggregation.Supplier;
import com.hazelcast.query.Predicate;

import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class AggregationsDemo {

    // Predefined companies
    private static final String[] COMPANIES = {"Hazelcast", "Hazelnut", "Hazelstorm", "Stormcast", "Nutcast"};
    private static final String[] FIRSTNAMES = {"Peter", "Greg", "Chris", "Talip", "Fuad", "Mehmet", "Miko", "Asim", "Enes"};
    private static final String[] LASTNAMES = {"Veentjer", "Luck", "Engelbert", "Ozturk", "Malikov", "Matsumura", "Arslan", "Akar"};

    public static void main(String[] args) {
        // Build Hazelcast cluster
        System.out.println("Starting instance 1");
        Hazelcast.newHazelcastInstance();
        System.out.println("Starting instance 2");
        Hazelcast.newHazelcastInstance();
        System.out.println("Starting instance 3");
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();

        // Retrieve the Hazelcast IMap
        IMap<String, Employee> employees = hz.getMap("employees");

        // Fill in demo data
        fillEmployeeMap(employees);

        // We simple calculate a average over all salaries on all employees
        simpleSalaryAverage(employees);

        // Here we first search Hazelcast employees using the Predicate and calculate the average only on those
        companyBasedSalaryAverage(employees);

        // Now we just sum up the salaries
        salarySum(employees);

        // This time we count the employees that we calculate on (let's expect it to be 10k ;-))
        countEmployees(employees);

        // Last but not least we want to get all distinct first names of the employees
        distinctEmployeeFirstNames(employees);

    }

    private static void simpleSalaryAverage(IMap<String, Employee> employees) {

        System.out.println("Calculating salary average");

        // Create the PropertyExtractor to extract salary value from the employee
        PropertyExtractor<Employee, Integer> propertyExtractor = new SalaryPropertyExtractor();

        // Select all employees
        Supplier<String, Employee, Integer> supplier = Supplier.all(propertyExtractor);

        // Choose the aggregation to perform
        Aggregation<String, Integer, Integer> aggregation = Aggregations.integerAvg();

        // Execute the aggregation and print the result
        int avgSalary = employees.aggregate(supplier, aggregation);
        System.out.println("Overall average salary: " + avgSalary);
        System.out.println("\n");

        // In Java 8:
        // int avgSalary = employees.aggregate(Supplier.all((value) -> value.getSalaryPerMonth()), Aggregations.integerAvg());
    }

    private static void companyBasedSalaryAverage(IMap<String, Employee> employees) {

        System.out.println("Calculating average monthly salary for Hazelcast");

        // Create the Predicate to select only Hazelcast employees
        Predicate<String, Employee> companyPredicate = new CompanyPredicate("Hazelcast");

        // Create the PropertyExtractor to extract salary value from the employee
        PropertyExtractor<Employee, Integer> propertyExtractor = new SalaryPropertyExtractor();

        // Create the supplier to handle extracted salaries from selected employees
        Supplier<String, Employee, Integer> salaryExtractor = Supplier.all(propertyExtractor);

        // Merge Predicate and PropertyExtractor (through the previous Supplier) together
        Supplier<String, Employee, Integer> supplier = Supplier.fromPredicate(companyPredicate, salaryExtractor);

        // Choose the aggregation to perform
        Aggregation<String, Integer, Integer> aggregation = Aggregations.integerAvg();

        // Execute the aggregation and print the result
        int avgSalary = employees.aggregate(supplier, aggregation);
        System.out.println("Hazelcast average salary: " + avgSalary);
        System.out.println("\n");

        // In Java 8:
        // int avgSalary = employees.aggregate(
        //          Supplier.fromPredicate(
        //                   (entry) -> "Hazelcast".equals(mapEntry.getValue().getCompanyName(),
        //                   Supplier.all((value) -> value.getSalaryPerMonth())
        //          ), Aggregations.integerAvg());
    }

    private static void salarySum(IMap<String, Employee> employees) {
        // Create the PropertyExtractor to extract salary value from the employee
        PropertyExtractor<Employee, Integer> propertyExtractor = new SalaryPropertyExtractor();

        // Select all employees
        Supplier<String, Employee, Integer> supplier = Supplier.all(propertyExtractor);

        // Choose the aggregation to perform
        Aggregation<String, Integer, Integer> aggregation = Aggregations.integerSum();

        // Execute the aggregation and print the result
        int sumSalary = employees.aggregate(supplier, aggregation);
        System.out.println("Sum of all salaries: " + sumSalary);
        System.out.println("\n");

        // In Java 8:
        // int sumSalary = employees.aggregate(Supplier.all((value) -> value.getSalaryPerMonth()), Aggregations.integerSum());
    }

    private static void countEmployees(IMap<String, Employee> employees) {
        // Select all employees
        Supplier<String, Employee, Object> supplier = Supplier.all();

        // Choose the aggregation to perform
        Aggregation<String, Object, Long> aggregation = Aggregations.count();

        // Execute the aggregation and print the result
        long countEmployee = employees.aggregate(supplier, aggregation);
        System.out.println("Number of employees: " + countEmployee);
        System.out.println("\n");

        // In Java 8:
        // long countEmployees = employees.aggregate(Supplier.all(), Aggregations.count());
    }

    private static void distinctEmployeeFirstNames(IMap<String, Employee> employees) {
        // Create PropertyExtractor to extract firstName value from the employee
        PropertyExtractor<Employee, String> propertyExtractor = new FirstNamePropertyExtractor();

        // Select all employees
        Supplier<String, Employee, String> supplier = Supplier.all(propertyExtractor);

        // Choose the aggregation to perform
        Aggregation<String, String, Set<String>> aggregation = Aggregations.distinctValues();

        // Execute the aggregation and print the result
        Set<String> allFirstNames = employees.aggregate(supplier, aggregation);
        System.out.println("All first names: " + allFirstNames);
        System.out.println("\n");

        // In Java 8:
        // Set<String> allFirstNames = employees.aggregate(Supplier.all((value) -> value.getFirstName()),
        //                                                 Aggregations.distinctValues());
    }

    private static void fillEmployeeMap(IMap<String, Employee> employees) {
        Random random = new Random();
        for (int i = 0; i < 10000; i++) {
            String companyName = COMPANIES[random.nextInt(COMPANIES.length)];
            String firstName = FIRSTNAMES[random.nextInt(FIRSTNAMES.length)];
            String lastName = LASTNAMES[random.nextInt(LASTNAMES.length)];
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
