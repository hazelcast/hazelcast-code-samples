package com.hazelcast.examples.application.dao;

import com.hazelcast.examples.application.model.User;

import java.util.Collection;

/**
 * This interface is used to implement a simulation of database access,
 * it could also be used to implement a facade around the cache to make
 * access using the JCache cache transparent to the user's application
 */
public interface UserDao {

    /**
     * Tries to find a user instance for a given userId
     *
     * @param userId the user's id to search for
     * @return the user if found otherwise null
     */
    User findUserById(int userId);

    /**
     * Stores the given {@link com.hazelcast.examples.application.model.User}
     * instance to the given userId. Previously stored data are lost, no
     * merging process takes place.
     *
     * @param userId the user's id to store to
     * @param user   the user instance to store
     * @return true if store was successful otherwise false
     */
    boolean storeUser(int userId, User user);

    /**
     * Removes all data for the given userId
     *
     * @param userId the user's id to remove
     * @return true is store was successful otherwise false
     */
    boolean removeUser(int userId);

    /**
     * Returns a collection of all userIds currently stored in the store
     *
     * @return a collection of all userIds, never returns null
     */
    Collection<Integer> allUserIds();
}
