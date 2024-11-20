package com.hazelcast.samples.spring.data.chemistry;

import org.springframework.shell.Bootstrap;

/**
 * Run a Hazelcast server embedded in a command line interpreter.
 *
 * Use Spring Shell to provide the framework of the command line interpreter, and
 * extend this with extra commands for this example.
 *
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
public class Server {

    /**
     * Launch Spring Shell, pulling in Spring beans for Hazelcast and added
     * command line interpreter commands.
     *
     * Spring Shell expects XML style config, in
     * {@code classpath:/META-INF/spring/spring-shell-plugin.xml}.
     *
     * @param args From the O/s to pass on
     * @throws Exception Allow failure
     */
    // TODO: Convert to Spring Boot
    public static void main(String[] args) throws Exception {
        System.setProperty("hazelcast.logging.type", "slf4j");
        Bootstrap.main(args);
    }
}
