package com.hazelcast.samples.spring.data.migration;

import java.io.Serializable;

import javax.persistence.Entity;

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
 */
@SuppressWarnings("serial")
@Data
@Entity
public class Noun implements Serializable {
	
	@javax.persistence.Id
	@org.springframework.data.annotation.Id
	private int		id;
	private String	english;
	private String	french;
	private String	spanish;

}
