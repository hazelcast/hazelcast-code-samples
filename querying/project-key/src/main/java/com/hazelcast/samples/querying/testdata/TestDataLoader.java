package com.hazelcast.samples.querying.testdata;

import java.time.LocalDate;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.stereotype.Component;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.samples.querying.domain.PersonKey;
import com.hazelcast.samples.querying.domain.PersonValue;

/**
 * <P>
 * Test data. Recording births and deaths separately, since not everyone will
 * have died!
 * <P>
 */
@Component
public class TestDataLoader implements CommandMarker {

    @Autowired
    private HazelcastInstance hazelcastInstance;

    @CliCommand(value = "load", help = "Loads the test data")
    public String load() {

        IMap<PersonKey, PersonValue> personMap = this.hazelcastInstance.getMap("person");
        IMap<String, LocalDate> deathsMap = this.hazelcastInstance.getMap("deaths");

        Arrays.stream(TestData.BIRTHS).forEach((Object[] datum) -> {
            PersonKey personKey = new PersonKey();
            personKey.setFirstName(datum[0].toString());
            personKey.setLastName(datum[1].toString());

            PersonValue personValue = new PersonValue();
            personValue.setDateOfBirth(LocalDate.parse(datum[2].toString()));

            personMap.put(personKey, personValue);
        });

        Arrays.stream(TestData.DEATHS).forEach((Object[] datum) -> {
            String firstName = datum[0].toString();
            LocalDate dateOfDeath = LocalDate.parse(datum[1].toString());

            deathsMap.put(firstName, dateOfDeath);
        });

        return String.format("Loaded %d into '%s'", TestData.BIRTHS.length, personMap.getName())
                + String.format(", Loaded %d into '%s'", TestData.DEATHS.length, deathsMap.getName());
    }

}
