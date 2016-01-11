package com.hazelcast.examples.application.commands;

import com.hazelcast.examples.application.Context;

/**
 * Implementations of the Command interface are used to execute different
 * operations using command line commands.
 */
public interface Command {

    /**
     * The execute method is called by giving it the current
     * {@link com.hazelcast.examples.application.Context} and is meant to execute
     * operations against other commands, the cache or dao.
     *
     * @param context current execution context
     * @throws Exception might throw an exception at any kind of problem
     */
    void execute(Context context) throws Exception;

    /**
     * Returns the description of this command to be shown in the help command list
     *
     * @return the description of the command
     */
    String description();
}
