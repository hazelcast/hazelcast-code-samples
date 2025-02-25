package sample.com.hz.demos.mapstore.mongo;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public interface PersonRepository {

    void save(Person person);

    default void delete(Integer id) {
        delete(Collections.singletonList(id));
    }

    void deleteAll();

    void delete(Collection<Integer> id);

    List<Person> findAll(Collection<Integer> ids);

    Collection<Integer> findAllIds();

    default Optional<Person> find(Integer id) {
        return findAll(Collections.singletonList(id)).stream().findFirst();
    }

}
