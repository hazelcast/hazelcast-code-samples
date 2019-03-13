package apps.app1;

import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.MutableEntry;
import java.io.Serializable;

public class AddPersonProcessor
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
