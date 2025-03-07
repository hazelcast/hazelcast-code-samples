package hazelcast.platform.solutions.pipeline.dispatcher.internal;


import java.io.Serializable;
import java.util.List;

/**
 * Instances of this class will be used to control routing between different versions of an API.
 *
 * It is used to configure a MultiVersionRequestRouter  The two classes are separated because
 * the MultiVersionRequestRouter itself cannot be serialized but its configuration can be.
 */
public class MultiVersionRequestRouterConfig implements Serializable {

    public MultiVersionRequestRouterConfig(){
        // this is here for the case where Jackson or another serializer needs to create one of these
        // during deserialization
    }
    public MultiVersionRequestRouterConfig(List<String> versions, List<Float> percentages){
        this.versions = versions;
        this.percentages = percentages;
        check();
    }
    private List<String> versions;
    private List<Float> percentages;

    /*
     * Throws a RuntimeException if all checks are not passed;
     */
    public void check(){
        if (versions == null || percentages == null || versions.size() == 0 || percentages.size() == 0){
            throw new RuntimeException("Error while creating MultiVersionRequestRouter configuration.  " +
                    "Version list and percentage list must both be non-null and non-empty lists");
        }

        if (versions.size() != percentages.size()){
            throw new RuntimeException("Error while creating MultiVersionRequestRouter configuration.  " +
                    "Version list and percentage list must both be the same length");
        }

        if (versions.size() > 16){
            throw new RuntimeException("Error while creating MultiVersionRequestRouter configuration.  " +
                    "Only 16 versions are supported");

        }

        float prev = 0.0f;
        for(Float f: percentages){
            if (f <= prev){
                throw new RuntimeException("Error while creating MultiVersionRequestRouter configuration.  " +
                        "Each percentage must be more than the previous one in the list " +
                        "and the first percentage must be more than 0.");
            }

            if (f > 1.0){
                throw new RuntimeException("Error while creating MultiVersionRequestRouter configuration.  " +
                        "Each percentage must less than or equal to 1.0");
            }

            prev =f ;
        }
    }

    /**
     *
     * @param f must be a float in [0.0, 1.0]
     * @return
     */
    public String getVersion(float f){
        if (f < 0.0f || f > 1.0f){
            throw new RuntimeException("Value passed to getVersion must be in [0.0f,1.0f].");
        }

        int i=0;
        for(;i < percentages.size(); ++i){
            if (f <= percentages.get(i)) break;
        }
        return versions.get(i);
    }

    public List<String> getVersions() {
        return versions;
    }

    public void setVersions(List<String> versions) {
        this.versions = versions;
    }

    public List<Float> getPercentages() {
        return percentages;
    }

    public void setPercentages(List<Float> percentages) {
        this.percentages = percentages;
    }

    @Override
    public String toString() {
        return "MultiVersionRequestRouterConfig{" +
                "versions=" + versions +
                ", percentages=" + percentages +
                '}';
    }
}
