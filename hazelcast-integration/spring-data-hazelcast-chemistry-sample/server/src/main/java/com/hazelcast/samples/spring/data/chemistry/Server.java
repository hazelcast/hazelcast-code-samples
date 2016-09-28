package com.hazelcast.samples.spring.data.chemistry;

import org.springframework.shell.Bootstrap;

/**
 * <P>Run a Hazelcast server embedded in a command line interpreter.
 * </P>
 * <P>Use Spring Shell to provide the framework of the command line interpreter, and 
 * extend this with extra commands for this example.
 * </P>
 * <P><B><U>Commands</U></B>
 * <OL>
 * <LI><B>help</B><P>List available commands.<I>[built-in]</I>
 * </LI>
 * <LI><B>count</B><P>Count the data in the cluster<I>
 * [defined in {@link com.hazelcast.samples.spring.data.chemistry.ServerCommands ServerCommands}]</I>
 * </LI>
 * <LI><B>list</B><P>List the data in the cluster<I>
 * [defined in {@link com.hazelcast.samples.spring.data.chemistry.ServerCommands ServerCommands}]</I>
 * </LI>
 * <LI><B>load</B><P>Load the test data.<I>
 * [defined in {@link com.hazelcast.samples.spring.data.chemistry.ServerCommands ServerCommands}]</I>
 * </LI>
 * <LI><B>quit</B><P>Stop the interpreter.<I>[built-in]</I>
 * </LI>
 * <LI><B>unload</B><P>Clear the test data.<I>
 * [defined in {@link com.hazelcast.samples.spring.data.chemistry.ServerCommands ServerCommands}]</I>
 * </LI>
 * </OL>
 * </P>
 * @see <a href="https://github.com/spring-projects/spring-shell#readme"/>
 */
public class Server {
	
    /**
     * <P>Launch Spring Shell, pulling in Spring beans for Hazelcast and added
     * command line interpreter commands.
     * </P>
     * <P>Spring Shell expects XML style config, in 
     * {@code classpath:/META-INF/spring/spring-shell-plugin.xml}.
     * </P>
	 *
     * @param args		  From the O/s to pass on          
     * @throws Exception  Allow failure 
     */
	//TODO - Convert to Spring Boot.
    public static void main(String[] args) throws Exception {
        System.setProperty("hazelcast.logging.type","slf4j");
        Bootstrap.main(args);
    }

}
