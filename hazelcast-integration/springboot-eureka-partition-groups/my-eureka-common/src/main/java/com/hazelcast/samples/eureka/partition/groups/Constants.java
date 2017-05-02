package com.hazelcast.samples.eureka.partition.groups;

/**
 * Useful hardcoding.
 */
public class Constants {

    /**
     * The name of the Hazelcast cluster, used in the
     * Hazelcast group configuration and as will appear in
     * the Eureka application list.
     */
    public static final String CLUSTER_NAME = "eurekast".toUpperCase();

    /**
     * Zone information is stored in Eureka as key/value
     * pairs.
     * <p>
     * If the master copy of a piece of data is held in
     * one zone, you don't want to put the backup in the
     * same zone. Somewhere else, anywhere else, is safer.
     * <p>
     * Zone just means a subset of the hardware. It might
     * map to a cloud zone, such as "{@code north dakota}"
     * and "{@code south dakota}". Or it might be something
     * more localized such as "{@code servers that use power
     * supply one}" and "{@code servers that use power supply two}".
     * Either way, it corresponds to information that is
     * ordinarily hidden from Hazelcast.
     * <p>
     * The problem we're trying to solve here is finding
     * servers that won't fail together. All the software
     * sees is hostnames and IP addresses, and that's not
     * enough on virtualized environments to be able to
     * make the best choices.
     */
    public static final String HAZELCAST_ZONE_METADATA_KEY
            = "hazelcastZone";


    /**
     * <P>To prove that zones provide data safety, we will need
     * to kill some nodes to see if data is lost or not.
     * </P>
     * <P>To show this properly we need a map protected from
     * loss and one exposed to it, and these need names.
     * </P>
     */
    public static final String MAP_NAME_SAFE = CLUSTER_NAME.toLowerCase() + "_safe";
    public static final String MAP_NAME_UNSAFE = CLUSTER_NAME.toLowerCase() + "_unsafe";
}
