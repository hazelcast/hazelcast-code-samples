package com.hazelcast.samples.spring.data.chemistry;

import com.hazelcast.samples.spring.data.chemistry.service.ChemistryService;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Set;

/**
 * Define commands to manipulate the test data from the command line.
 */
@Component
public class ServerCommands implements CommandMarker {

    @Resource
    private ChemistryService chemistyService;

    /**
     * List all test data present in the cluster.
     *
     * <b><i>Usage</i></b>
     * <ul>
     * <li><b>list</b></li>
     * </ul>
     *
     * @return A string displayed in the shell
     */
    @CliCommand(value = "count", help = "Count the Chemistry test data in the cluster")
    public String count() {
        Map<String, Long> items = this.chemistyService.count();

        return items.toString();
    }

    /**
     * List all test data present in the cluster.
     *
     * <b><i>Usage</i></b>
     * <ul>
     * <li><b>list</b></li>
     * </ul>
     *
     * @return A string displayed in the shell
     */
    @CliCommand(value = "list", help = "Display the Chemistry test data in the cluster")
    public String list() {
        Map<String, Set<Integer>> elements = this.chemistyService.neutrons();

        return elements.entrySet().toString();
    }

    /**
     * Insert the predefined test data in {@link example.springdata.keyvalue.chemistry.utils.PeriodicTable} into the cluster.
     *
     * <b><i>Usage</i></b>
     * <ul>
     * <li><b>load</b></li>
     * </ul>
     *
     * @return A string displayed in the shell
     */
    @CliCommand(value = "load", help = "Insert Chemistry test data into the cluster")
    public String load() {
        int count = this.chemistyService.load();

        return String.format("[%d row%s]", count, (count != 1 ? "s" : ""));
    }

    /**
     * Remove data from the cluster.
     *
     * <b><i>Usage</i></b>
     * <ul>
     * <li><b>unload</b>
     * Unload isotopes only</li>
     * <li><b>unload --isotope</b>
     * Unload elements and isotopes</li>
     * <li><b>unload --isotope true</b>
     * Unload isotopes only</li>
     * <li><b>unload --isotope false</b>
     * Unload elements and isotopes</li>
     * </ul>
     *
     * @return A string displayed in the shell
     */
    @CliCommand(value = "unload", help = "Remove Chemistry test data from the cluster")
    public String unload(
            @CliOption(key = {"isotope"}
                    , mandatory = false
                    , help = "Optionally '--isotope true' or '--isotope false' to only unload isotopes"
                    , specifiedDefaultValue = "false"
                    , unspecifiedDefaultValue = "true"
            )
            final boolean onlyIsotopes
    ) {

        this.chemistyService.unload(onlyIsotopes);

        return "Deleted isotopes" + (onlyIsotopes ? "" : " and elements");
    }
}
