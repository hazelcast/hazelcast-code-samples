package hibernate;

import org.hibernate.engine.HibernateIterator;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

/**
 * Iterator implementing Closeable and delegating to a Hibernate iterator
 **/
public class CloseableIterator<T> implements Iterator<T>, Closeable {

    private HibernateIterator iterator;

    public CloseableIterator(HibernateIterator iterator) {
        this.iterator = iterator;
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @SuppressWarnings("unchecked")
    @Override
    public T next() {
        return (T) iterator.next();
    }

    @Override
    public void close() throws IOException {
        iterator.close();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
