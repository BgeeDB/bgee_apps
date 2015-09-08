package org.bgee.model.species;

import java.util.Set;

//Species should be always explicitly targeted, or allSpeciesRequested should be set to true, 
//or a taxonId provided.
public class TaxonomyFilter {
    //TODO: check consistency between taxonId and speciesIds
    private final String taxonId;
    private final Set<String> speciesIds;
    
    private final boolean allSpeciesRequested;
    
    public TaxonomyFilter() {
        this.taxonId = null;
        this.speciesIds = null;
        this.allSpeciesRequested = false;
    }
}
