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
 * XXX
 */
@Component
public class TestDataLoader implements CommandMarker {
	
    @Autowired
    private HazelcastInstance hazelcastInstance;
    
    @CliCommand(value = "load",
    				help = "Loads the test data")
    public String load() {
    	
        IMap<PersonKey, PersonValue> personMap
        		= this.hazelcastInstance.getMap("person");
        
        Arrays.stream(TestData.PERSONS).forEach((Object[] datum) -> {
        		PersonKey personKey = new PersonKey();
        		personKey.setFirstName(datum[0].toString());
        		personKey.setLastName(datum[1].toString());
        		
        		PersonValue personValue = new PersonValue();
        		personValue.setDateOfBirth(LocalDate.parse(datum[2].toString()));
        		
        		personMap.put(personKey, personValue);
        });
        
		return String.format("Loaded %d into '%s'", 
				TestData.PERSONS.length,
				personMap.getName());

	}

}
