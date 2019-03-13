package apps.app2;

import apps.app1.Person;

import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.MutableEntry;
import java.io.Serializable;

// this entry processor will be denied access to Person class
public class IllegalPersonProcessor
        implements EntryProcessor<Integer, Person, Void>, Serializable {

    @Override
    public Void process(MutableEntry<Integer, Person> entry, Object... arguments)
            throws EntryProcessorException {
        Person person = new Person((Integer) arguments[0], (String) arguments[1]);
        entry.setValue(person);
        System.out.println(person + " loaded by " + person.getClass().getClassLoader());
        return null;
    }
}
