import com.hazelcast.mapreduce.aggregation.PropertyExtractor;

public class FirstNamePropertyExtractor implements PropertyExtractor<Employee, String> {

    @Override
    public String extract(Employee value) {
        return value.getFirstName();
    }
}
