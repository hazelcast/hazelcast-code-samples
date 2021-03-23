import com.hazelcast.aggregation.Aggregators;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.query.Predicate;
import com.hazelcast.config.*;
import com.hazelcast.client.*;
import com.hazelcast.partition.*;
import com.hazelcast.cluster.Cluster;

import java.util.Random;
import java.util.Set;
import java.util.UUID;


public class FastAggregationsDemo {
    // predefined companies
    private static final String[] COMPANIES = {"Hazelcast", "Hazelnut", "Hazelstorm", "Stormcast", "Nutcast"};
    private static final String[] FIRST_NAMES = {"Peter", "Greg", "Chris", "Talip", "Fuad", "Mehmet", "Miko", "Asim", "Enes"};
    private static final String[] LAST_NAMES = {"Veentjer", "Luck", "Engelbert", "Ozturk", "Malikov", "Matsumura", "Arslan", "Akar"};
    private static final String[] CATEGORIES = {"BASEBALL", "BASKETBALL", "FOOTBALL", "GOLF", "AMERICAN FOOTBALL", "HORSE RACING", "TENNIS", "CYCLING"};


	private static class MigrationCallbacks implements MigrationListener {


		@Override
		public void migrationStarted(MigrationState state) {
			System.out.println("AJ Migration Started: " + state);
		}
		@Override
		public void migrationFinished(MigrationState state) {
			System.out.println("AJ Migration Finished: " + state);
			System.out.println("AJ Remaining Migrations: " + state.getRemainingMigrations());
			/*
			HazelcastInstance hzclient = HazelcastClient.newHazelcastClient();
			hzclient.getCluster().getMembers().forEach(i -> System.out.println("AJ Finished: " + i));
			*/
			if (state.getRemainingMigrations() == 0) {
				System.out.println("Executing Queries, as remaining migrations = 0");
				executeQueries();
			}
		}

		@Override
		public void replicaMigrationCompleted(ReplicaMigrationEvent event) {
			//System.out.println("AJ Replica Migration Completed: " + event);
		}

		@Override
		public void replicaMigrationFailed(ReplicaMigrationEvent event) {
			System.out.println("AJ Replica Migration Failed: " + event);
		}
	}


    public static void main(String[] args) {

        // build Hazelcast cluster
        System.out.println("Starting instance 1");
        HazelcastInstance hz1 = Hazelcast.newHazelcastInstance();

        // retrieve the Hazelcast IMap
        IMap<String, Employee> employees = hz1.getMap("employees");


		// Build the indexes before we populate the data
		employees.addIndex(new IndexConfig(IndexType.SORTED, "salaryPerMonth"));
		employees.addIndex(new IndexConfig(IndexType.HASH, "companyName"));

        // fill in demo data
        fillEmployeeMap(employees);

		// Register ourselves for the migration callbacks
		hz1.getPartitionService().addMigrationListener(new MigrationCallbacks());

		// Start the other instances now
        System.out.println("Starting instance 2");
        HazelcastInstance hz2 = Hazelcast.newHazelcastInstance();
        System.out.println("Starting instance 3");
        HazelcastInstance hz3 = Hazelcast.newHazelcastInstance();
		hz1.getCluster().getMembers().forEach(i -> System.out.println("AJ: " + i));
	}


	public static void executeQueries() {

		HazelcastInstance hzclient = HazelcastClient.newHazelcastClient();
        IMap<String, Employee> employees = hzclient.getMap("employees");

        // we simple calculate a average over all salaries on all employees
        for (int i = 0; i < 5; i++) {
			simpleSalaryAverage(employees);
		}
        System.out.println();

        // here we first search Hazelcast employees using the Predicate and calculate the average only on those
        for (int i = 0; i < 5; i++) {
			companyBasedSalaryAverage(employees);
		}
        System.out.println();

        // now we just sum up the salaries
        for (int i = 0; i < 5; i++) {
			salarySum(employees);
		}
        System.out.println();

        // this time we count the employees that we calculate on (let's expect it to be 10k ;-))
        for (int i = 0; i < 5; i++) {
			countEmployees(employees);
		}
        System.out.println();

        // last but not least we want to get all distinct first names of the employees
        for (int i = 0; i < 5; i++) {
			distinctEmployeeFirstNames(employees);
		}
        System.out.println();

        Hazelcast.shutdownAll();
    }

    private static void simpleSalaryAverage(IMap<String, Employee> employees) {
        // execute the aggregation and print the result
		long startTime = System.nanoTime();
        double avgSalary = employees.aggregate(Aggregators.integerAvg("salaryPerMonth"));
		double totalTime = (System.nanoTime() - startTime)/1E9;
        System.out.println("Overall average salary: " + avgSalary + " in " + totalTime + "secs");
        System.out.println("\n");
    }

    private static void companyBasedSalaryAverage(IMap<String, Employee> employees) {
		long startTime = System.nanoTime();
        // create the Predicate to select only Hazelcast employees
        Predicate<String, Employee> companyPredicate = new CompanyPredicate("Hazelcast");

        // execute the aggregation and print the result
        double avgSalary = employees.aggregate(Aggregators.integerAvg("salaryPerMonth"), companyPredicate);
		double totalTime = (System.nanoTime() - startTime)/1E9;
        System.out.println("Hazelcast average salary: " + avgSalary + " in " + totalTime + "secs");
    }

    private static void salarySum(IMap<String, Employee> employees) {
		long startTime = System.nanoTime();
        // execute the aggregation and print the result
        long sumSalary = employees.aggregate(Aggregators.integerSum("salaryPerMonth"));
		double totalTime = (System.nanoTime() - startTime)/1E9;
        System.out.println("Sum of all salaries: " + sumSalary + " in " + totalTime + "secs");
    }

    private static void countEmployees(IMap<String, Employee> employees) {
		long startTime = System.nanoTime();
        // execute the aggregation and print the result
        long countEmployee = employees.aggregate(Aggregators.count());
		double totalTime = (System.nanoTime() - startTime)/1E9;
        System.out.println("Number of employees: " + countEmployee + " in " + totalTime + "secs");
    }

    private static void distinctEmployeeFirstNames(IMap<String, Employee> employees) {
		long startTime = System.nanoTime();
        // execute the aggregation and print the result
        Set<String> allFirstNames = employees.aggregate(Aggregators.distinct("firstName"));
		double totalTime = (System.nanoTime() - startTime)/1E9;
        System.out.println("All first names: " + allFirstNames + " in " + totalTime + "secs");
    }

    private static void fillEmployeeMap(IMap<String, Employee> employees) {
        System.out.println("Employee map generation started.");
		long startTime = System.nanoTime();
        Random random = new Random();
        for (int i = 0; i < 2E6; i++) {
            String companyName = COMPANIES[random.nextInt(COMPANIES.length)];
            String firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
            String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
			int eventId = i % 300; // This will split the data over 300 events
            String categoryName = CATEGORIES[eventId % CATEGORIES.length];
            int salaryPerMonth = 2800 + random.nextInt(2000);

            Employee employee = new Employee();
            employee.setCompanyName(companyName);
            employee.setFirstName(firstName);
            employee.setLastName(lastName);
            employee.setSalaryPerMonth(salaryPerMonth);
			employee.setEventId(eventId);
            employee.setCategoryName(categoryName);

            //String key = UUID.randomUUID().toString();
            //employees.put(key, employee);
            employees.put(String.format("%d", i), employee);

			if (i % 1E5 == 0) {
				double totalTime = (System.nanoTime() - startTime)/1E9;
				System.out.println("Number created: " + i + " in " + totalTime + "secs + - " + employee);
			}

        }
		double totalTime = (System.nanoTime() - startTime)/1E9;
        System.out.println("Employee map filled in " + totalTime + "secs");
    }
}
