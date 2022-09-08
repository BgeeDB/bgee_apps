package org.bgee.model.expressiondata.rawdata.est;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Service;
import org.bgee.model.ServiceFactory;
import org.bgee.model.expressiondata.rawdata.RawDataConditionFilter;
import org.bgee.model.gene.GeneFilter;

/**
 * A {@link Service} to obtain {@link EST} objects. 
 * Users should use the {@link org.bgee.model.ServiceFactory} to obtain {@code ESTService}s.
 * 
 * @author  Julien Wollbrett
 * @version Bgee 15
 * @since   Bgee 15, Aug. 2022
*/
public class ESTService extends Service{

    private static final Logger log = LogManager.getLogger(ESTService.class.getName());

    protected ESTService(ServiceFactory serviceFactory) {
        super(serviceFactory);
    }

    public Stream<EST> loadESTsByGeneFilter(GeneFilter geneFilter) {
        return null;
    }
    
    public Stream<EST> loadESTsByGeneFilter(Collection<GeneFilter> geneFilters) {
        return null;
    }

    public Stream<EST> loadESTsByConditionFilter(RawDataConditionFilter condFilter) {
        return null;
    }
    
    public Stream<EST> loadESTsByConditionFilters(
            Collection<RawDataConditionFilter> condFilter) {
        return null;
    }

    public Stream<EST> loadESTsByIds(Collection<String> estIds) {
        return null;
    }
    
    public Stream<EST> loadESTs(Collection<String> estIds, 
            Collection<RawDataConditionFilter> condFilters, 
            Collection<GeneFilter> geneFilters, boolean withAssayInfo) {
        log.traceEntry("{}, {}, {}, {}", estIds, condFilters, geneFilters, 
                withAssayInfo);
        Set<String> clonedEstIds = estIds == null? new HashSet<>():
            new HashSet<>(estIds);
        Set<RawDataConditionFilter> clonedCondFilters = condFilters == null?
                new HashSet<>() : new HashSet<>(condFilters);
        Set<GeneFilter> clonedGeneFilters = geneFilters == null? new HashSet<>():
            new HashSet<>(geneFilters);
        
        
        return null;
    }
}
