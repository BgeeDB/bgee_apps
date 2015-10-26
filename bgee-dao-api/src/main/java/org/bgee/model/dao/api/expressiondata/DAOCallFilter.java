package org.bgee.model.dao.api.expressiondata;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO;

public class DAOCallFilter<T extends CallTO> {
    private final static Logger log = LogManager.getLogger(DAOCallFilter.class.getName());

    private final Set<String> geneIds;
    
    private final Set<String> speciesIds;
    
    private final Set<DAOConditionFilter> conditionFilters;
    
    private final T callTOFilter;
    
    /**
     * Constructor accepting all requested parameters. 
     * 
     * @param geneIds           A {@code Set} of {@code String}s that are IDs of genes 
     *                          to filter expression queries. Can be {@code null} or empty.
     * @param speciesIds        A {@code Set} of {@code String}s that are IDs of species 
     *                          to filter expression queries. Can be {@code null} or empty.
     * @param conditionFilters  A {@code Set} of {@code ConditionFilter}s to configure 
     *                          the filtering of conditions with expression data. If several 
     *                          {@code ConditionFilter}s are provided, they are seen as "OR" conditions.
     * @param callTOFilter      A {@code T} allowing to configure the minimum quality level 
     *                          for each data type, and the call propagation method.
     *                          Only the following methods are considered (if available 
     *                          for this {@code T}): CONTINUE.
     *                          The following methods are not considered (when available):  
     *                          {@code getGeneId()}, {@code getStageId()}, and {@code getAnatEntityId()}, 
     *                          CONTINUE. 
     */
    continue javadoc
    public DAOCallFilter(Set<String> geneIds, Set<String> speciesIds, 
            Set<DAOConditionFilter> conditionFilters, T callTOFilter) {
        log.entry(geneIds, speciesIds, conditionFilters, callTOFilter);
        
        this.geneIds = geneIds == null? null: Collections.unmodifiableSet(new HashSet<>(geneIds));
        this.speciesIds = speciesIds == null? null: Collections.unmodifiableSet(new HashSet<>(speciesIds));
        this.conditionFilters = conditionFilters == null? null: Collections.unmodifiableSet(
                new HashSet<>(conditionFilters));
        this.callTOFilter = callTOFilter;
        
        log.exit();
    }
}
