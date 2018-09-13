package org.bgee.model.expressiondata.rawdata;

import java.util.Collection;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.rawdata.microarray.AffymetrixProbeset;
import org.bgee.model.gene.GeneFilter;

public class RawDataLoader {
    private final static Logger log = LogManager.getLogger(RawDataLoader.class.getName());


    //XXX: Actually, it might be better for multispecies queries to precisely link some GeneFilters and ConditionFilters,
    //so that the Conditions to use are not the same for each species. But since it's currently not the case in CallFilter,
    //it's not the case here.
    RawDataLoader(Collection<GeneFilter> geneFilters, Collection<RawDataConditionFilter> condFilters, RawDataService rawDataService) {
        
    }
    /**
     * @return  A {@code Stream} of {@code AffymetrixProbeset}s.
     *          If the {@code Stream} contains no element,
     *          it means that there were no data of this type for the requested parameters.
     */
    public Stream<AffymetrixProbeset> loadAffymetrixProbesets() {
        return null;
    }

}
