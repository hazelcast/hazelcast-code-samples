package com.hazelcast.examples.application.commands;

import com.hazelcast.examples.application.Context;

public class CacheClearCommand implements Command {

    @Override
    public void execute(Context context) throws Exception {
        context.getUserCache().clear();
        context.writeln("Cache cleared");
    }

    @Override
    public String description() {
        return "Clears the cache";
    }
}
