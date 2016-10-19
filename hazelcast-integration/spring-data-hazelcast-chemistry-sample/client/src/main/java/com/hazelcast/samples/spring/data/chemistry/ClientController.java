package com.hazelcast.samples.spring.data.chemistry;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.hazelcast.samples.spring.data.chemistry.domain.Element;
import com.hazelcast.samples.spring.data.chemistry.service.ChemistryService;

/**
 * <P>Provide a simple REST based interface onto the Chemistry
 * example.
 * </P>
 *<UL>
 * <LI><A HREF="http://localhost:8080/">/</A> - Top level page, lists what else is available
 * </LI>
 * <LI><A HREF="http://localhost:8080/halogens">/halogens</A> - The Halogens column
 * </LI>
 * <LI><A HREF="http://localhost:8080/neutrons">/neutrons</A> - Neutrons per isotope
 * </LI>
 * <LI><A HREF="http://localhost:8080/weight/0">/weight/??</A> - Find isotopes by atomic weight
 * </LI>
 *</UL> 
 */
@RestController
public class ClientController {

	@Autowired
	private ChemistryService chemistryService;

	/** 
	 * <P>Return the other URLs supported by this controller.
	 * </P>
	 * <P>HATEOAS or Swagger would be an alternative mechanism here,
	 * although unreleated to the purpose of the example.
	 * </P>
	 * @throws MalformedURLException 
	 */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public List<URL> index() throws MalformedURLException {

    	List<URL> urls = new ArrayList<>();
    
    	urls.add(new URL("http://localhost:8080/halogens"));
    	urls.add(new URL("http://localhost:8080/neutrons"));
    	urls.add(new URL("http://localhost:8080/weight/50"));
    	
    	return urls;
    }
	
	/**
	 * <P>The Halogens are elements in column 17 of the periodic
	 * table.</P>
	 * <P>Sorting is done as part of retrieval, descending sequence on symbol.
	 * </P>
	 *
	 * @return A list of elements that form salt compounds
	 */
	@RequestMapping(value = "/halogens", method = RequestMethod.GET)
	public List<Element> halogens() {
		
		List<Element> halogens = this.chemistryService.findElementsByGroupSorted(17);
		
		return halogens;
	}

	/**
	 * <P>List all elements stored, with optional isotope information.
	 * </P>
	 * <P>Use {@link ChemistryService#unload} with {@code true} as a
	 * parameter to remove isotopes but leave elements from the data
	 * held, and see the effect on the result of this method.
	 * </P>
	 * 
	 * @return Element symbols and atomic weights for each.
	 */
	@RequestMapping(value = "/neutrons", method = RequestMethod.GET)
	public Map<String,Set<Integer>> neutrons() {

		return this.chemistryService.neutrons();
	}

	/**
	 *<P>Find elements that have isotopes with specific weight.
	 * This is quite likely for the test data, as the isotope
	 * data is generated.
	 * </P>
	 * <P>Sort post retrieval.</P>
	 *
	 * @param An atomic weight to find
	 * @return A possibly empty, non-null list
	 */
	@RequestMapping(value = "/weight/{weight}", method = RequestMethod.GET)
	public List<Element> weight(@PathVariable final int weight) {
		
		List<Element> elements = this.chemistryService.findElementsByAtomicWeight(weight);
		
		Collections.sort(elements);
		
		return elements;
	}

}
