package com.hazelcast.samples.spring.data.migration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <P>Expose only some of the methods of the {@link NounJPARepository}
 * and {@link VerbJPARepository} repositories.
 * </P>
 * <P><U><B>MIGRATION PATH</B></U></P>
 * <OL>
 * <LI>Add this {@code @Service}.
 * </LI>
 * </OL>
 */
@Service
public class DatabaseService {

	@Autowired
	private NounJPARepository nounJPARepository;
	
	@Autowired
	private VerbJPARepository verbJPARepository;
	
	public Noun findNoun(Integer id) {
		return this.nounJPARepository.findOne(id);
	}
	
	public Iterable<Integer> findNounIds() {
		return this.nounJPARepository.findAllId();
	}
	
	public Verb findVerb(Integer id) {
		return this.verbJPARepository.findOne(id);
	}

	public Iterable<Integer> findVerbIds() {
		return this.verbJPARepository.findAllId();
	}

}
