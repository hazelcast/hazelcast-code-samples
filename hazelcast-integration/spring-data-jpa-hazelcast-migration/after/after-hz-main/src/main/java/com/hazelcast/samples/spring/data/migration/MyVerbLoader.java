package com.hazelcast.samples.spring.data.migration;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.hazelcast.core.MapLoader;

import lombok.extern.slf4j.Slf4j;

/**
 * <P>Connect a Hazelcast server to an RDBMS, so a Java {@code map} can be populated
 * on demand from a database table.
 * </P>
 * <P><U><B>MIGRATION PATH</B></U></P>
 * <OL>
 * <LI>Add this class.
 * </LI>
 * </OL>
 */
@Component
@Slf4j
public class MyVerbLoader implements ApplicationContextAware, MapLoader<Integer, Verb> {

	private static VerbJPARepository verbJPARepository;
	
	/**
	 * <P>Load one record using its key.
	 * </P>
	 * 
	 * @return Null if not found
	 */
	@Override
	public Verb load(Integer key) {
		log.info("load({})", key);
		return verbJPARepository.findOne(key);
	}

	/**
	 * <P>Given this member a subset of keys to pre-load.
	 * </P>
	 * 
	 * @return Records for the given keys
	 */
	@Override
	public Map<Integer, Verb> loadAll(Collection<Integer> keys) {
		Map<Integer, Verb> result = new HashMap<>();
        for(Integer key : keys) {
                Verb verb = this.load(key);
                if (verb!=null) {
                	result.put(key, verb);
                }
        }
        return result;
	}

	/**
	 * <P>Select all keys that should be pre-loaded. Run on one member in
	 * the cluster.
	 * </P>
	 * 
	 * @return All keys
	 */
	@Override
	public Iterable<Integer> loadAllKeys() {
		return verbJPARepository.findAllId();
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		verbJPARepository = applicationContext.getBean(VerbJPARepository.class);
	}

}
