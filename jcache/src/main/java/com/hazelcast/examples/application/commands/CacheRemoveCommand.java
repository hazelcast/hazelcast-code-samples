package com.hazelcast.examples.application.commands;

import com.hazelcast.examples.application.Context;

public class CacheRemoveCommand implements Command {

    @Override
    public void execute(Context context) throws Exception {
        int userId = context.readUserId();
        context.getUserCache().remove(userId);
    }

    @Override
    public String description() {
        return "Removes an account using the cache (write-through)";
    }
}
