package com.hazelcast.samples.eureka.partition.groups;

/**
 * <P>Useful hardcoding.
 * </P>
 */
public class Constants {

	/**
	 * <P>The name of the Hazelcast cluster, used in the
	 * Hazelcast group configuration and as will appear in
	 * the Eureka application list.
	 * </P>
	 */
	public static final String	CLUSTER_NAME
									= "eurekast".toUpperCase();
	
	
	/**
	 * <P>Zone information is stored in Eureka as key/value
	 * pairs.
	 * </P>
	 * </P>If the master copy of a piece of data is held in
	 * one zone, you don't want to put the backup in the
	 * same zone. Somewhere else, anywhere else, is safer.
	 * </P>
	 * <P>Zone just means a subset of the hardware. It might
	 * map to a cloud zone, such as "{@code north dakota}"
	 * and "{@code south dakota}". Or it might be something
	 * more localized such as "{@code servers that use power
	 * supply one}" and "{@code servers that use power supply two}".
	 * Either way, it corresponds to information that is
	 * ordinarily hidden from Hazelcast.
	 * </P>
	 * <P>The problem we're trying to solve here is finding
	 * servers that won't fail together. All the software
	 * sees is hostnames and IP addresses, and that's not
	 * enough on virtualized environments to be able to
	 * make the best choices.
	 * </P>.
	 */
    public static final String 	HAZELCAST_ZONE_METADATA_KEY
    								= "hazelcastZone";

	
}
