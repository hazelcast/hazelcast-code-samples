package com.hazelcast.examples.application.dao;

import com.hazelcast.examples.application.model.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserDaoImpl implements UserDao {

    private final Map<Integer, User> users = new ConcurrentHashMap<Integer, User>();

    public UserDaoImpl() {
        users.put(1, new User(1, "user1"));
        users.put(2, new User(2, "user2"));
        users.put(3, new User(3, "user3"));
        users.put(4, new User(4, "user4"));
    }

    @Override
    public User findUserById(int userId) {
        User user = users.get(userId);
        if (user != null) {
            // add latency to show the caching effect
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return user;
        }
        throw new IllegalArgumentException("User not found");
    }

    @Override
    public boolean storeUser(int userId, User user) {
        users.put(userId, user);
        return true;
    }

    @Override
    public boolean removeUser(int userId) {
        return users.remove(userId) != null;
    }

    @Override
    public Collection<Integer> allUserIds() {
        return new ArrayList<Integer>(users.keySet());
    }
}
