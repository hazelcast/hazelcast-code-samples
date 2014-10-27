package com.hazelcast.examples.application.commands;

import com.hazelcast.examples.application.Context;
import com.hazelcast.examples.application.model.User;

public class DaoAddCommand implements Command {

    @Override
    public void execute(Context context) throws Exception {
        User user = context.readUser();
        context.getUserDao().storeUser(user.getUserId(), user);
    }

    @Override
    public String description() {
        return "Adds a new account using the dao (not yet cached, only visible after cacheclear)";
    }
}
