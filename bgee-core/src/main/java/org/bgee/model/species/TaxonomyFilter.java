package org.bgee.model.species;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TaxonomyFilter implements Predicate<Taxon>{
	private final static Logger log = LogManager.getLogger(TaxonomyFilter.class.getName());
    //TODO: check consistency between taxonId and speciesIds

    private final Collection<Integer> taxonIds;
    private final Collection <Integer> speciesIds;
    
    public TaxonomyFilter(Collection <Integer> taxonIds, Collection <Integer> speciesIds){
    	this.taxonIds = Collections.unmodifiableCollection(
    			taxonIds== null? new HashSet<>(): taxonIds);
    	this.speciesIds = Collections.unmodifiableCollection(
                speciesIds == null? new HashSet<>(): speciesIds);
    }
        
    public Collection <Integer> getTaxonIds() {
		return taxonIds;
	}

	public Collection <Integer> getSpeciesIds() {
		return speciesIds;
	}
	

	@Override
	public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((taxonIds == null) ? 0 : taxonIds.hashCode());
        result = prime * result + ((speciesIds == null) ? 0 : speciesIds.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(Object obj){
    	if(this == obj){
    		return true;
    	}
    	if(obj == null){
    		return false;
    	}
    	if(getClass() != obj.getClass()){
    		return false;
    	}
    	TaxonomyFilter other = (TaxonomyFilter) obj;
        if (taxonIds == null) {
            if (other.getTaxonIds()!= null) {
                return false;
            }
        } else if (!taxonIds.equals(other.getTaxonIds())) {
            return false;
        }
        if (speciesIds == null) {
            if (other.getSpeciesIds() != null) {
                return false;
            }
        } else if (!speciesIds.equals(other.getSpeciesIds())) {
            return false;
        }
    	return true;
    }
    
    @Override
    public String toString() {
        return "TaxonomyFilter [taxonId=" + taxonIds
                + ", speciesIds=" + speciesIds + "]";
    }

	@Override
	public boolean test(Taxon t) {
//		Ontology<Taxon, Integer> ontology = new Ontology<>(null,
//                elements, relations, RelationType.ISA_PARTOF, serviceFactory, Taxon.class);
//		t.getDescendants(ontology, EnumSet<RelationType>RelationType.ISA_PARTOF);
		// TODO Auto-generated method stub
		return false;
	}
}
