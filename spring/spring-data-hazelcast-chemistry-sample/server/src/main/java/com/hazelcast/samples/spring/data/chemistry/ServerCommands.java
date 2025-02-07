package com.hazelcast.samples.spring.data.chemistry;

import com.hazelcast.samples.spring.data.chemistry.service.ChemistryService;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Set;

/**
 * Define commands to manipulate the test data from the command line.
 */
@Command(group = "Server Commands")
public class ServerCommands {

    @Resource
    private ChemistryService chemistyService;

    /**
     * List all test data present in the cluster.
     * <p>
     * <b><i>Usage</i></b>
     * <ul>
     * <li><b>list</b></li>
     * </ul>
     *
     * @return A string displayed in the shell
     */
    @Command(command = "count", description = "Count the Chemistry test data in the cluster")
    public String count() {
        Map<String, Long> items = this.chemistyService.count();

        return items.toString();
    }

    /**
     * List all test data present in the cluster.
     * <p>
     * <b><i>Usage</i></b>
     * <ul>
     * <li><b>list</b></li>
     * </ul>
     *
     * @return A string displayed in the shell
     */
    @Command(description = "Display the Chemistry test data in the cluster")
    public String list() {
        Map<String, Set<Integer>> elements = this.chemistyService.neutrons();

        return elements.entrySet().toString();
    }

    /**
     * Insert the predefined test data in {@link Constants#PERIODIC_TABLE} into the cluster.
     * <p>
     * <b><i>Usage</i></b>
     * <ul>
     * <li><b>load</b></li>
     * </ul>
     *
     * @return A string displayed in the shell
     */
    @Command(description = "Insert Chemistry test data into the cluster")
    public String load() {
        int count = this.chemistyService.load();

        return String.format("[%d row%s]", count, (count != 1 ? "s" : ""));
    }

    /**
     * Remove data from the cluster.
     * <p>
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
    @Command(description = "Remove Chemistry test data from the cluster")
    public String unload(
            @Option(
                    label = "isotope",
                    description = "Optionally '--isotope true' or '--isotope false' to only unload isotopes"
            )
            final boolean onlyIsotopes
    ) {

        this.chemistyService.unload(onlyIsotopes);

        return "Deleted isotopes" + (onlyIsotopes ? "" : " and elements");
    }
}
