import com.hazelcast.query.Predicate;

import java.util.Map;

public class CompanyPredicate implements Predicate<String, Employee> {

    private String companyName;

    public CompanyPredicate() {
    }

    public CompanyPredicate(String companyName) {
        this.companyName = companyName;
    }

    @Override
    public boolean apply(Map.Entry<String, Employee> mapEntry) {
        return companyName.equals(mapEntry.getValue().getCompanyName());
    }
}
