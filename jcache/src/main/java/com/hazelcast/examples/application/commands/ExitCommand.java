package com.hazelcast.examples.application.commands;

import com.hazelcast.examples.application.Context;
import com.hazelcast.examples.application.model.User;

import javax.cache.Cache;

public class ExitCommand implements Command {

    @Override
    public void execute(Context context) throws Exception {
        Cache<Integer, User> userCache = context.getUserCache();
        userCache.getCacheManager().getCachingProvider().close();
        System.exit(0);
    }

    @Override
    public String description() {
        return "Terminates the program";
    }
}
