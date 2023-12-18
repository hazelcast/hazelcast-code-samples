package com.hazelcast.samples.spring.data.migration;

import lombok.Data;

import javax.persistence.Entity;
import java.io.Serializable;

/**
 * A domain object for a noun in our simplistic object model.
 *
 * Use {@code @Entity} to map this object directly into a table, one-to-one on fields to columns.
 * Use {@code @Id} to mark which field is the unique identifier.
 * Use {@code @Data} to get Lombok to generate the getters &amp; setters.
 */
@SuppressWarnings("serial")
@Data
@Entity
public class Noun implements Serializable {

    @javax.persistence.Id
    @org.springframework.data.annotation.Id
    private int id;
    private String english;
    private String french;
    private String spanish;
}
