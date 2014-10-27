package com.hazelcast.examples.application.commands;

import com.hazelcast.examples.application.Context;

public class DaoRemoveCommand implements Command {

    @Override
    public void execute(Context context) throws Exception {
        int userId = context.readUserId();
        context.getUserDao().removeUser(userId);
    }

    @Override
    public String description() {
        return "Removes an account using the dao (not yet cached, only visible after cacheclear)";
    }
}
