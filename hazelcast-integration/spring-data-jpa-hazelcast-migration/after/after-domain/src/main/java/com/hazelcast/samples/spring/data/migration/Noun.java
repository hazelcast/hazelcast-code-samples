package com.hazelcast.samples.spring.data.migration;

import java.io.Serializable;

import javax.persistence.Entity;

import org.springframework.data.keyvalue.annotation.KeySpace;

import lombok.Data;

/**
 * <P>A domain object for a noun in our simplistic object model.
 * </P>
 * <P>Use {@code @Entity} to map this object directly into
 * a table, one-to-one on fields to columns.
 * </P>
 * <P>Use {@code @Id} to mark which field is the unique
 * identifier.
 * </P>
 * <P>Use {@code @Data} to get Lombok to generate the
 * getters &amp; setters.
 * </P>
 * <P><U><B>MIGRATION PATH</B></U></P>
 * <OL>
 * <LI>Add the {@code @KeySpace} annotation, this object can
 * be retrieved from a key-value store (Hazelcast!).
 * </LI>
 * </OL>
 */
@SuppressWarnings("serial")
@Data
@Entity
@KeySpace
public class Noun implements Serializable {
	
	@javax.persistence.Id
	@org.springframework.data.annotation.Id
	private int		id;
	private String	english;
	private String	french;
	private String	spanish;

}
