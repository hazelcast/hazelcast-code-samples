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
public class MyNounLoader implements ApplicationContextAware, MapLoader<Integer, Noun> {

	private static NounJPARepository nounJPARepository;

	/**
	 * <P>Load one record using its key.
	 * </P>
	 * 
	 * @return Null if not found
	 */
	@Override
	public Noun load(Integer key) {
		log.info("load({})", key);
		return nounJPARepository.findOne(key);
	}

	/**
	 * <P>Given this member a subset of keys to pre-load.
	 * </P>
	 * 
	 * @return Records for the given keys
	 */
	@Override
	public Map<Integer, Noun> loadAll(Collection<Integer> keys) {
		Map<Integer, Noun> result = new HashMap<>();
        for(Integer key : keys) {
                Noun noun = this.load(key);
                if (noun!=null) {
                	result.put(key, noun);
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
		return nounJPARepository.findAllId();
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		nounJPARepository = applicationContext.getBean(NounJPARepository.class);
	}

}
