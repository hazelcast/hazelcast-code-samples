package com.hazelcast.samples.spring.data.migration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Expose only some of the methods of the {@link NounJPARepository}and {@link VerbJPARepository} repositories.
 *
 * <u><b>MIGRATION PATH</b><u>
 * <ol>
 * <li>Add this {@code @Service}.</li>
 * </ol>
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
