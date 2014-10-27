package com.hazelcast.examples.application.commands;

import com.hazelcast.examples.application.Context;
import com.hazelcast.examples.application.cache.UserUpdateEntryProcessor;
import com.hazelcast.examples.application.model.User;

import javax.cache.Cache;

public class CacheEntryProcessorCommand implements Command {

    @Override
    public void execute(Context context) throws Exception {
        int userId = context.readUserId();
        context.write("New username: ");
        String username = context.readLine();
        Cache<Integer, User> userCache = context.getUserCache();
        User result = userCache.invoke(userId, new UserUpdateEntryProcessor(), username);
        context.writeln("User updated: " + result);
    }

    @Override
    public String description() {
        return "Updates an account using JCache EntryProcessor";
    }
}
