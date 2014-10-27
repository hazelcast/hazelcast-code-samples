package com.hazelcast.examples.application.commands;

import com.hazelcast.examples.application.Context;

import java.util.Map;

public class HelpCommand implements Command {

    @Override
    public void execute(Context context) throws Exception {
        Map<String, Command> commands = context.getCommands();
        context.writeln("Allowed commands");
        for (Map.Entry<String, Command> commandEntry : commands.entrySet()) {
            context.write(toCommandWidth(commandEntry.getKey()));
            context.write(" : ");
            context.writeln(commandEntry.getValue().description());
        }
    }

    @Override
    public String description() {
        return "Prints this help";
    }

    private String toCommandWidth(String command) {
        if (command.length() > 13) {
            throw new IllegalArgumentException("Command '" + command + "' to long, max length 13 chars");
        }
        if (command.length() == 13) {
            return command;
        }
        StringBuilder sb = new StringBuilder(command);
        for (int i = 0; i < (13 - command.length()); i++) {
            sb.append(' ');
        }
        return sb.toString();
    }
}
