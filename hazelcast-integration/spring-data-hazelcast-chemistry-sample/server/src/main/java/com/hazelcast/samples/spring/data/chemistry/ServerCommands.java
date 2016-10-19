package com.hazelcast.samples.spring.data.chemistry;

import com.hazelcast.samples.spring.data.chemistry.service.ChemistryService;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

/**
 *<P>Define commands to manipulate the test data from the command line.
 *</P>
 */
@Component
public class ServerCommands implements CommandMarker {

	@Resource
	private ChemistryService chemistyService;

    /**
     *<P>List all test data present in the cluster.
     *</P>
     *<P><B><I>Usage</I></B></P>
     *<UL>
     * <LI><B>list</B>
     * </LI>
     *</UL>
     *
     * @return A string displayed in the shell
     */
    @CliCommand(value = "count",
				help = "Count the Chemistry test data in the cluster")
    public String count() {
    	Map<String,Long> items = this.chemistyService.count();
 
     	return items.toString();
    }

    /**
     *<P>List all test data present in the cluster.
     *</P>
     *<P><B><I>Usage</I></B></P>
     *<UL>
     * <LI><B>list</B>
     * </LI>
     *</UL>
     *
     * @return A string displayed in the shell
     */
    @CliCommand(value = "list",
				help = "Display the Chemistry test data in the cluster")
    public String list() {
    	Map<String,Set<Integer>> elements = this.chemistyService.neutrons();
 
    	String result = elements.entrySet().stream()
		.map(Object::toString)
        .collect(Collectors.joining(", ", "[", "]"));

     	return result;
    }
        
    /**
     *<P>Insert the predefined test data in {@link example.springdata.keyvalue.chemistry.utils.PeriodicTable} into the cluster.
     *</P>
     *<P><B><I>Usage</I></B></P>
     *<UL>
     * <LI><B>load</B>
     * </LI>
     *</UL>
     *
     * @return A string displayed in the shell
     */
    @CliCommand(value = "load",
				help = "Insert Chemistry test data into the cluster")
    public String load() {
    	int count = this.chemistyService.load();
    	
    	String result = String.format("[%d row%s]", count, (count!=1 ?"s" :""));
    	
    	return result;
    }

    /**
     *<P>Remove data from the cluster.
     *</P>
     *<P><B><I>Usage</I></B></P>
     *<UL>
     * <LI><B>unload</B>
     * <P>Unload isotopes only</P>
     * </LI>
     * <LI><B>unload --isotope</B>
     * <P>Unload elements and isotopes</P>
     * </LI>
     * <LI><B>unload --isotope true</B>
     * <P>Unload isotopes only</P>
     * </LI>
     * <LI><B>unload --isotope false</B>
     * <P>Unload elements and isotopes</P>
     * </LI>
     *</UL>
     *
     * @return A string displayed in the shell
     */
    @CliCommand(value = "unload",
    			help = "Remove Chemistry test data from the cluster")
    public String unload(
    			@CliOption(key={"isotope"}
    						,mandatory = false
    						,help="Optionally '--isotope true' or '--isotope false' to only unload isotopes"
    						,specifiedDefaultValue="false"
    						,unspecifiedDefaultValue="true"
    						) 
    			final boolean onlyIsotopes
    		) {

    	this.chemistyService.unload(onlyIsotopes);
    	
    	return "Deleted isotopes" + (onlyIsotopes ? "" : " and elements");
    }

}
