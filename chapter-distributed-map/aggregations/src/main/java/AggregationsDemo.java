import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.mapreduce.aggregation.Aggregation;
import com.hazelcast.mapreduce.aggregation.Aggregations;
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
        Hazelcast.newHazelcastInstance();
        Hazelcast.newHazelcastInstance();
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();

        try {
            // Retrieve the Hazelcast IMap
            IMap<String, Employee> employees = hz.getMap("employees");

            // Fill in demo data
            fillEmployeeMap(employees);

            simpleSalaryAverage(employees);

            companyBasedSalaryAverage(employees);

            salarySum(employees);

            countEmployees(employees);

            distinctEmployeeFirstNames(employees);

        } finally {
            // Shutdown Hazelcast cluster
            Hazelcast.shutdownAll();
        }
    }

    private static void simpleSalaryAverage(IMap<String, Employee> employees) {
        Supplier<String, Employee, Integer> supplier = Supplier.all(new SalaryPropertyExtractor());
        Aggregation<String, Integer, Integer> aggregation = Aggregations.integerAvg();
        int avgSalary = employees.aggregate(supplier, aggregation);
        System.out.println("Overall average salary: " + avgSalary);

        // In Java 8:
        // int avgSalary = employees.aggregate(Supplier.all((value) -> value.getSalaryPerMonth()), Aggregations.integerAvg());
    }

    private static void companyBasedSalaryAverage(IMap<String, Employee> employees) {
        Predicate<String, Employee> companyPredicate = new CompanyPredicate("Hazelcast");
        Supplier<String, Employee, Integer> salaryExtractor = Supplier.all(new SalaryPropertyExtractor());
        Supplier<String, Employee, Integer> supplier = Supplier.fromPredicate(companyPredicate, salaryExtractor);
        Aggregation<String, Integer, Integer> aggregation = Aggregations.integerAvg();
        int avgSalary = employees.aggregate(supplier, aggregation);
        System.out.println("Hazelcast average salary: " + avgSalary);

        // In Java 8:
        // int avgSalary = employees.aggregate(
        //          Supplier.fromPredicate(
        //                   (entry) -> "Hazelcast".equals(mapEntry.getValue().getCompanyName(),
        //                   Supplier.all((value) -> value.getSalaryPerMonth())
        //          ), Aggregations.integerAvg());
    }

    private static void salarySum(IMap<String, Employee> employees) {
        Supplier<String, Employee, Integer> supplier = Supplier.all(new SalaryPropertyExtractor());
        Aggregation<String, Integer, Integer> aggregation = Aggregations.integerSum();
        int sumSalary = employees.aggregate(supplier, aggregation);
        System.out.println("Sum of all salaries: " + sumSalary);

        // In Java 8:
        // int sumSalary = employees.aggregate(Supplier.all(), Aggregations.integerSum());
    }

    private static void countEmployees(IMap<String, Employee> employees) {
        Supplier<String, Employee, Object> supplier = Supplier.all();
        Aggregation<String, Object, Long> aggregation = Aggregations.count();
        long countEmployee = employees.aggregate(supplier, aggregation);
        System.out.println("Number of employees: " + countEmployee);

        // In Java 8:
        // long countEmployees = employees.aggregate(Supplier.all(), Aggregations.count());
    }

    private static void distinctEmployeeFirstNames(IMap<String, Employee> employees) {
        Supplier<String, Employee, String> supplier = Supplier.all(new FirstNamePropertyExtractor());
        Aggregation<String, String, Set<String>> aggregation = Aggregations.distinctValues();
        Set<String> allFirstNames = employees.aggregate(supplier, aggregation);
        System.out.println("All first names: " + allFirstNames);

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
    }

}
