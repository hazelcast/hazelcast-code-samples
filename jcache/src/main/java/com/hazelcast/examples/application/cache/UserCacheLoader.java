package com.hazelcast.examples.application.cache;

import com.hazelcast.examples.application.dao.UserDao;
import com.hazelcast.examples.application.model.User;

import javax.cache.integration.CacheLoader;
import javax.cache.integration.CacheLoaderException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class UserCacheLoader
        implements CacheLoader<Integer, User>, Serializable {

    private final UserDao userDao;

    public UserCacheLoader(UserDao userDao) {
        // Store the dao instance created externally
        this.userDao = userDao;
    }

    @Override
    public User load(Integer key) throws CacheLoaderException {
        // Just call through into the dao
        return userDao.findUserById(key);
    }

    @Override
    public Map<Integer, User> loadAll(Iterable<? extends Integer> keys)
            throws CacheLoaderException {

        // Create the resulting map
        Map<Integer, User> loaded = new HashMap<Integer, User>();
        // For every key in the given set of keys
        for (Integer key : keys) {
            // Try to retrieve the user
            User user = userDao.findUserById(key);
            // If user is not found do not add the key to the result set
            if (user != null) {
                loaded.put(key, user);
            }
        }
        return loaded;
    }
}
