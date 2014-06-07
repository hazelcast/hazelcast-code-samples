import com.hazelcast.mapreduce.aggregation.PropertyExtractor;

public class SalaryPropertyExtractor implements PropertyExtractor<Employee, Integer> {

    @Override
    public Integer extract(Employee value) {
        return value.getSalaryPerMonth();
    }
}
