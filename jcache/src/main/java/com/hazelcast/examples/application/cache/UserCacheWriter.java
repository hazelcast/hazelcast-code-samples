package com.hazelcast.examples.application.cache;

import com.hazelcast.examples.application.dao.UserDao;
import com.hazelcast.examples.application.model.User;

import javax.cache.Cache;
import javax.cache.integration.CacheWriter;
import javax.cache.integration.CacheWriterException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

public class UserCacheWriter
        implements CacheWriter<Integer, User>, Serializable {

    private final UserDao userDao;

    public UserCacheWriter(UserDao userDao) {
        // Store the dao instance created externally
        this.userDao = userDao;
    }

    @Override
    public void write(Cache.Entry<? extends Integer, ? extends User> entry)
            throws CacheWriterException {

        // Store the user using the dao
        userDao.storeUser(entry.getKey(), entry.getValue());
    }

    @Override
    public void writeAll(Collection<Cache.Entry<? extends Integer, ? extends User>> entries)
            throws CacheWriterException {

        // Retrieve the iterator to clean up the collection from
        // written keys in case of an exception
        Iterator<Cache.Entry<? extends Integer, ? extends User>> iterator = entries.iterator();
        while (iterator.hasNext()) {
            // Write entry using dao
            write(iterator.next());
            // Remove from collection of keys
            iterator.remove();
        }
    }

    @Override
    public void delete(Object key) throws CacheWriterException {
        // Test for key type
        if (!(key instanceof Integer)) {
            throw new CacheWriterException("Illegal key type");
        }
        // Remove user using dao
        userDao.removeUser((Integer) key);
    }

    @Override
    public void deleteAll(Collection<?> keys) throws CacheWriterException {
        // Retrieve the iterator to clean up the collection from
        // written keys in case of an exception
        Iterator<?> iterator = keys.iterator();
        while (iterator.hasNext()) {
            // Write entry using dao
            delete(iterator.next());
            // Remove from collection of keys
            iterator.remove();
        }
    }
}
