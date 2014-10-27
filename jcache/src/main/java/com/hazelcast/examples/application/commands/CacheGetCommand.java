package com.hazelcast.examples.application.commands;

import com.hazelcast.examples.application.Context;
import com.hazelcast.examples.application.model.User;

public class CacheGetCommand implements Command {

    @Override
    public void execute(Context context) throws Exception {
        int userId = context.readUserId();
        User user = context.getUserCache().get(userId);
        context.writeln(user.toString());
    }

    @Override
    public String description() {
        return "Retrieves a single account from the cache";
    }
}
