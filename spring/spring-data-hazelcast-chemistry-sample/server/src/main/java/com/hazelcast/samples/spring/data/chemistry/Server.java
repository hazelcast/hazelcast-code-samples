package com.hazelcast.samples.spring.data.chemistry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.shell.command.annotation.EnableCommand;

/**
 * Run a Hazelcast server embedded in a command line interpreter.
 * <p>
 * extend this with extra commands for this example.
 * <p>
 * <b><u>Commands</u></b>
 * <ol>
 * <li><b>help</b>List available commands.<i>[built-in]</i>
 * </li>
 * <li><b>count</b>Count the data in the cluster<i>
 * [defined in {@link com.hazelcast.samples.spring.data.chemistry.ServerCommands ServerCommands}]</i>
 * </li>
 * <li><b>list</b>List the data in the cluster<i>
 * [defined in {@link com.hazelcast.samples.spring.data.chemistry.ServerCommands ServerCommands}]</i>
 * </li>
 * <li><b>load</b>Load the test data.<i>
 * [defined in {@link com.hazelcast.samples.spring.data.chemistry.ServerCommands ServerCommands}]</i>
 * </li>
 * <li><b>quit</b>Stop the interpreter.<i>[built-in]</i>
 * </li>
 * <li><b>unload</b>Clear the test data.<i>
 * [defined in {@link com.hazelcast.samples.spring.data.chemistry.ServerCommands ServerCommands}]</i>
 * </li>
 * </ol>
 *
 * @see <a href="https://github.com/spring-projects/spring-shell#readme"/>
 */
@EnableCommand(ServerCommands.class)
@SpringBootApplication
public class Server {

    /**
     * Launch Spring Shell, pulling in Spring beans for Hazelcast and added
     * command line interpreter commands.
     *
     * @param args From the O/s to pass on
     */
    public static void main(String[] args) {
        System.setProperty("hazelcast.logging.type", "log4j2");
        SpringApplication.run(Server.class, args);
    }
}
