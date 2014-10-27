package com.hazelcast.examples.application.commands;

import com.hazelcast.examples.application.Context;
import com.hazelcast.examples.application.model.User;

import java.util.Collection;

public class CacheListCommand implements Command {

    @Override
    public void execute(Context context) throws Exception {
        Collection<Integer> userIds = context.getUserDao().allUserIds();
        for (Integer userId : userIds) {
            User user = context.getUserCache().get(userId);
            context.writeln(user.toString());
        }
    }

    @Override
    public String description() {
        return "Lists all accounts stored in the cache";
    }
}
