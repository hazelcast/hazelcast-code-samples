package com.hazelcast.examples.application.cache;

import com.hazelcast.examples.application.model.User;

import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.MutableEntry;

public class UserUpdateEntryProcessor
        implements EntryProcessor<Integer, User, User> {

    @Override
    public User process(MutableEntry<Integer, User> entry, Object... arguments)
            throws EntryProcessorException {

        // Test arguments length
        if (arguments.length < 1) {
            throw new EntryProcessorException("One argument needed: username");
        }

        // Get first argument and test for String type
        Object argument = arguments[0];
        if (!(argument instanceof String)) {
            throw new EntryProcessorException("First argument has wrong type, required java.lang.String");
        }

        // Retrieve the value from the MutableEntry
        User user = entry.getValue();

        // Retrieve the new username from the first argument
        String newUsername = (String) arguments[0];

        // Set the new username
        user.setUsername(newUsername);

        // Set the changed user to mark the entry as dirty
        entry.setValue(user);

        // Return the changed user to return it to the caller
        return user;
    }
}
