package com.hazelcast.samples.spring.data.migration;

import lombok.Data;
import org.springframework.data.keyvalue.annotation.KeySpace;

import javax.persistence.Entity;
import java.io.Serializable;

/**
 * A domain object for a noun in our simplistic object model.
 *
 * Use {@code @Entity} to map this object directly into a table, one-to-one on fields to columns.
 *
 * Use {@code @Id} to mark which field is the unique identifier.
 * Use {@code @Data} to get Lombok to generate the getters &amp; setters.
 *
 * <u><b>MIGRATION PATH</b></u>
 * <ol>
 * <li>Add the {@code @KeySpace} annotation, this object can be retrieved from a key-value store (Hazelcast!).</li>
 * </ol>
 */
@SuppressWarnings("serial")
@Data
@Entity
@KeySpace
public class Noun implements Serializable {

    @javax.persistence.Id
    @org.springframework.data.annotation.Id
    private int id;
    private String english;
    private String french;
    private String spanish;
}
