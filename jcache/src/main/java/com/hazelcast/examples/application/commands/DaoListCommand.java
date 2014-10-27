package com.hazelcast.examples.application.commands;

import com.hazelcast.examples.application.Context;
import com.hazelcast.examples.application.model.User;

import java.util.Collection;

public class DaoListCommand implements Command {

    @Override
    public void execute(Context context) throws Exception {
        Collection<Integer> userIds = context.getUserDao().allUserIds();
        for (Integer userId : userIds) {
            User user = context.getUserDao().findUserById(userId);
            context.writeln(user.toString());
        }
    }

    @Override
    public String description() {
        return "Lists all accounts from DAO";
    }
}
