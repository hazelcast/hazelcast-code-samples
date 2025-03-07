package hazelcast.platform.labs.machineshop;

/*
 * This is a copy of the Names class in common.  It would be better not to do this but
 * the domain objects are also in common and those should not be available in this project.
 * Making them available will cause domain objects to be deserialized as their Java PPJOs
 * rather than GenericRecords, which will break some pipelines.
 *
 * Ideally, there are 2 shared artifacts, one with domain objects and one with everythign else
 */
public class Names {

    public static final String PROFILE_MAP_NAME = "machine_profiles";

    public static final String STATUS_SUMMARY_MAP_NAME = "machine_status_summary";

}
