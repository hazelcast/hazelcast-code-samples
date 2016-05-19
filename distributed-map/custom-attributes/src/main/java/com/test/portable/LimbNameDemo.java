package com.test.portable;

import com.hazelcast.config.Config;
import com.hazelcast.config.MapAttributeConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.Predicates;

import java.text.ParseException;
import java.util.Set;

import static com.test.portable.Limb.limb;

public class LimbNameDemo {

    public static void main(String[] args) throws ParseException {
        MapAttributeConfig mapAttributeConfig = new MapAttributeConfig();
        mapAttributeConfig.setName("limbName");
        mapAttributeConfig.setExtractor("com.test.portable.LimbNameExtractor");

        MapConfig mapConfig = new MapConfig();
        mapConfig.setName("people");
        mapConfig.addMapAttributeConfig(mapAttributeConfig);

        Config config = new Config();
        config.getSerializationConfig().addPortableFactory(PersonFactory.FACTORY_ID, new PersonFactory());
        config.addMapConfig(mapConfig);

        HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);
        IMap<Integer, Person> map = hz.getMap("people");

        map.put(1, new Person("Georg", limb("left-leg"), limb("right-leg")));
        map.put(2, new Person("Peter", limb("left-hand"), limb("right-hand")));
        map.put(3, new Person("Hans", limb("left-finger"), limb("right-finger")));
        map.put(4, new Person("Stefanie", limb("left-arm"), limb("right-arm")));

        // we're using a custom attribute 'limbName' which is provided by the 'LimbNameExtractor'
        Set<Person> people = (Set<Person>) map.values(Predicates.equal("limbName", "left-arm"));
        System.out.println("People: " + people);

        Hazelcast.shutdownAll();
    }

}
