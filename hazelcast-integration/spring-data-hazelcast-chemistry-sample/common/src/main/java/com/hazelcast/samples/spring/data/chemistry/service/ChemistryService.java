package com.hazelcast.samples.spring.data.chemistry.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import com.hazelcast.samples.spring.data.chemistry.Constants;
import com.hazelcast.samples.spring.data.chemistry.domain.Element;
import com.hazelcast.samples.spring.data.chemistry.domain.Isotope;
import com.hazelcast.samples.spring.data.chemistry.domain.IsotopeKey;
import com.hazelcast.samples.spring.data.chemistry.repository.ElementRepository;
import com.hazelcast.samples.spring.data.chemistry.repository.IsotopeRepository;

/**
 *<P>The chemistry service class hides that elements and isotopes are stored
 *in different key spaces.
 *</P>
 */
@Service
@Slf4j
public class ChemistryService {

	@Resource
	private ElementRepository elementRepository;
	@Resource
	private IsotopeRepository isotopeRepository;

	
	/**
	 * <P>Indicate how many data items are stored.
	 * </P>
	 * <P>Although the figures give a breakdown for elements and
	 * isotopes, this doesn't confirm or deny if they are stored
	 * together or separately
	 * </P>
	 * 
	 * @return Entry counts for each type
	 */
	public Map<String,Long> count() {
		Map<String,Long> result = new HashMap<>();
		
		result.put(Constants.KEYSPACE_ELEMENT, this.elementRepository.count());
		result.put(Constants.KEYSPACE_ISOTOPE, this.isotopeRepository.count());
		
		return result;
	}
	
	/**
	 * <P>Find all atomic weight values, part of the composite
	 * key for an {@link Isotope}.
	 * </P>
	 *  
	 * @return Unique atomic weights, in ascending sequence
	 */
	public Set<Integer> findAtomicWeights() {
		Set<Integer> result = new TreeSet<>();
		
		for(Isotope isotope : this.isotopeRepository.findAll()) {
			result.add(isotope.getIsotopeKey().getAtomicWeight());
		}
		
		return result;
	}
	
	/**
	 * <P>Search the {@code element} key space for those with the
	 * required value for {@code element.getGroup()}.
	 * 
	 * @param 	group, a column in the periodic table
	 * @return	Elements in that group
	 */
	public List<Element> findElementsByGroupSorted(final int group) {
		return this.elementRepository.findByGroupOrderBySymbolDesc(group);
	}

	/**
	 * <P>Find matches for an atomic weight.
	 * </P>
	 * <P>This example only considers the atomic weight to be made
	 * from protons (atomic number) plus neutrons, discounting other
	 * particles. Elements with different numbers of protons can
	 * still have the same total weight depending how many neutrons
	 * they have.
	 * </P>
	 *  
	 * @param 	Isotope weight to search on
	 * @return	A possibly empty list of elements
	 */
	public List<Element> findElementsByAtomicWeight(final int weight) {
		ArrayList<Element> result = new ArrayList<>();
		
		for(Isotope isotope : this.isotopeRepository.findByIsotopeKeyAtomicWeight(weight)) {
			result.add(this.elementRepository.findOne(isotope.getIsotopeKey().getSymbol()));
		}
		
		return result;
	}

	/**
	 * <P>Return the number of neutrons for each isotopes, the
	 * atomic weight minus the number of protons.
	 * </P>
	 * 
	 * @return For each symbol, the variants in the number of neutrons
	 */
	public Map<String,Set<Integer>> neutrons() {
		Map<String,Set<Integer>> result = new HashMap<>();
		
		for(Element element : this.elementRepository.findAll()) {
			log.trace("neutrons(): {}", element);
			String symbol = element.getSymbol();
			
			Set<Integer> neutrons = new TreeSet<>();
			// Search isotopeRepository on part of the isotope key
			for(Isotope isotope : this.isotopeRepository.findByIsotopeKeySymbol(symbol)) {
				log.trace("neutrons(): {}", isotope);
				neutrons.add(isotope.getIsotopeKey().getAtomicWeight() - element.getAtomicNumber());
			}

			result.put(symbol, neutrons);
		}
		
		return result;
	}

	/**
	 * <P>Load the test data
	 * </P>
	 * <P>The logic here uses the exactness of the atomic weight. If it is
	 * whole number assume the element exists with only one form (one isotope).
	 * If the atomic weight is not a whole number, there must at least be
	 * one isotope lighter and one heavier.
	 * </P>
	 * 
	 * @return How many inserts were done, elements plus isotopes
	 */
	public int load() {
		int count=0;
		
		// Atomic number, Symbol, Name, Group [optional], Period, Atomic Weight
		for(String[] data : Constants.PERIODIC_TABLE) {
			try {
				Element element = new Element();

				element.setAtomicNumber(Integer.parseInt(data[0]));
				element.setSymbol(data[1]);
				element.setName(data[2]);
				element.setGroup("".equals(data[3])? null : Integer.parseInt(data[3]));
				element.setPeriod(Integer.parseInt(data[4]));

				log.trace("load(): {}", element);
				this.elementRepository.save(element);
				count++;
				
				double averageWeight = Double.parseDouble(data[5]);
				
				for(Double atomicWeight=Math.floor(averageWeight); atomicWeight<=Math.ceil(averageWeight); atomicWeight++) {
					Isotope isotope = new Isotope();
					IsotopeKey isotopeKey = new IsotopeKey();

					isotopeKey.setSymbol(element.getSymbol());
					isotopeKey.setAtomicWeight(atomicWeight.intValue());
					isotope.setIsotopeKey(isotopeKey);
					
					log.trace("load(): {}", isotope);
					this.isotopeRepository.save(isotope);
					count++;
				}
				
			} catch (Exception exception) {
				log.error(data.toString(), exception);
			}
		}
		
		return count;
	}
	
	/**
	 * <P>Remove some or all of the test data
	 * </P>
	 * 
	 * @param onlyIsotopes Isotope or (isotopes and elements)
	 */
	public void unload(boolean onlyIsotopes) {
		this.isotopeRepository.deleteAll();
		
		if(!onlyIsotopes) {
			this.elementRepository.deleteAll();
		}
	}
	
}
