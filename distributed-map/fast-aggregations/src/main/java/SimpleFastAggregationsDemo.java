import com.hazelcast.aggregation.Aggregator;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

import java.util.Map;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

public class SimpleFastAggregationsDemo {

    // predefined companies
    private static final String[] COMPANIES = {"Hazelcast", "Hazelnut", "Hazelstorm", "Stormcast", "Nutcast"};
    private static final String[] FIRST_NAMES = {"Peter", "Greg", "Chris", "Talip", "Fuad", "Mehmet", "Miko", "Asim", "Enes"};
    private static final String[] LAST_NAMES
            = {"Veentjer", "Luck", "Engelbert", "Ozturk", "Malikov", "Matsumura", "Arslan", "Akar"};

	static HazelcastInstance hz;

    public static void main(String[] args) {
        // build Hazelcast cluster
        System.out.println("Starting instance 1");
        hz = Hazelcast.newHazelcastInstance();
        System.out.println("Starting instance 2");
        Hazelcast.newHazelcastInstance();
		/*
        System.out.println("Starting instance 3");
        Hazelcast.newHazelcastInstance();
		*/

        // retrieve the Hazelcast IMap
        IMap<String, Employee> employees = hz.getMap("employees");

        // fill in demo data
        fillEmployeeMap(employees);


        // we simple calculate a average over all salaries on all employees
        simpleCustomSumAggregation(employees);

        Hazelcast.shutdownAll();
    }

    private static void simpleCustomSumAggregation(IMap<String, Employee> employees) {
        System.out.println("Calculating summed salary by lastname");
		hz.getCluster().getMembers().forEach(i -> System.out.println("AJ: " + i));


		long startTime = System.nanoTime();
        Map<String, Integer> sumSalaries = employees.aggregate(new Aggregator<Map.Entry<String, Employee>, Map<String, Integer>>() {

			// Stores the sum of salary by lastname
            protected HashMap<String, Integer> sumLastname = new HashMap<String, Integer>();

            @Override
            public void accumulate(Map.Entry<String, Employee> entry) {
				String lastname = entry.getValue().getLastName();
				Integer sum = sumLastname.getOrDefault(lastname, 0) + entry.getValue().getSalaryPerMonth();
				sumLastname.put(lastname, sum);
            }

            @Override
            public void combine(Aggregator aggregator) {

				//long startTime = System.nanoTime();
				for(Map.Entry<String, Integer> entry: this.getClass().cast(aggregator).sumLastname.entrySet()) {
					String lastname = entry.getKey();
					Integer sum = entry.getValue() + this.sumLastname.getOrDefault(lastname, 0);
					this.sumLastname.put(lastname, sum);
				}
				//double totalTime = (System.nanoTime() - startTime)/1E9;
				//System.out.println("combined in : " + totalTime + "secs");
            }

            @Override
            public Map<String, Integer> aggregate() {
				// TODO can add ORDER BY and LIMIT in this step if needed
                return sumLastname;
            }

        });
		double totalTime = (System.nanoTime() - startTime)/1E9;
        System.out.println("Overall summed salaries: " + sumSalaries + " in " + totalTime + "secs");
        System.out.println("\n");
    }

    private static void fillEmployeeMap(IMap<String, Employee> employees) {
        Random random = new Random();
        for (int i = 0; i < 2E6; i++) {
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
