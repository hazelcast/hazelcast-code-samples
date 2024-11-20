package com.hazelcast.samples.spring.data.chemistry;

import com.hazelcast.samples.spring.data.chemistry.domain.Element;
import com.hazelcast.samples.spring.data.chemistry.service.ChemistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Provide a simple REST based interface onto the Chemistry
 * example.
 * <ul>
 * <li><a href="http://localhost:8080/">/</a> - Top level page, lists what else is available
 * </li>
 * <li><a href="http://localhost:8080/halogens">/halogens</a> - The Halogens column
 * </li>
 * <li><a href="http://localhost:8080/neutrons">/neutrons</a> - Neutrons per isotope
 * </li>
 * <li><a href="http://localhost:8080/weight/0">/weight/??</a> - Find isotopes by atomic weight
 * </li>
 * </ul>
 */
@RestController
public class ClientController {

    @Autowired
    private ChemistryService chemistryService;

    /**
     * Return the other URLs supported by this controller.
     *
     * HATEOAS or Swagger would be an alternative mechanism here,
     * although unrelated to the purpose of the example.
     *
     * @throws MalformedURLException
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public List<URL> index() throws MalformedURLException {

        List<URL> urls = new ArrayList<URL>();

        urls.add(new URL("http://localhost:8080/halogens"));
        urls.add(new URL("http://localhost:8080/neutrons"));
        urls.add(new URL("http://localhost:8080/weight/50"));

        return urls;
    }

    /**
     * The Halogens are elements in column 17 of the periodic
     * table.
     *
     * Sorting is done as part of retrieval, descending sequence on symbol.
     *
     * @return A list of elements that form salt compounds
     */
    @RequestMapping(value = "/halogens", method = RequestMethod.GET)
    public List<Element> halogens() {
        return this.chemistryService.findElementsByGroupSorted(17);
    }

    /**
     * List all elements stored, with optional isotope information.
     *
     * Use {@link ChemistryService#unload} with {@code true} as a
     * parameter to remove isotopes but leave elements from the data
     * held, and see the effect on the result of this method.
     *
     * @return Element symbols and atomic weights for each.
     */
    @RequestMapping(value = "/neutrons", method = RequestMethod.GET)
    public Map<String, Set<Integer>> neutrons() {

        return this.chemistryService.neutrons();
    }

    /**
     * Find elements that have isotopes with specific weight.
     * This is quite likely for the test data, as the isotope
     * data is generated.
     *
     * Sort post retrieval.
     *
     * @param weight atomic weight to find
     * @return A possibly empty, non-null list
     */
    @RequestMapping(value = "/weight/{weight}", method = RequestMethod.GET)
    public List<Element> weight(@PathVariable final int weight) {

        List<Element> elements = this.chemistryService.findElementsByAtomicWeight(weight);

        Collections.sort(elements);

        return elements;
    }
}
