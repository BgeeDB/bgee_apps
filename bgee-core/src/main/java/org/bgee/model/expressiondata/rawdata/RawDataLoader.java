package org.bgee.model.expressiondata.rawdata;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataFilter;
import org.bgee.model.expressiondata.rawdata.microarray.AffymetrixProbeset;
import org.bgee.model.gene.Gene;

public class RawDataLoader {
    private final static Logger log = LogManager.getLogger(RawDataLoader.class.getName());

    private final Set<RawDataFilter> rawDataFilters;

    //attributes needed for making the necessary queries
    private final RawDataService rawDataService;
    /**
     * A {@code Set} of {@code DAORawDataFilter}s corresponding to the conversion
     * of the {@code RawDataFilter}s in {@link #rawDataFilters};
     */
    private final Set<DAORawDataFilter> daoRawDataFilters;
    /**
     * A {@code Map} where keys are {@code Integer}s corresponding to Bgee internal gene IDs,
     * the associated value being the corresponding {@code Gene}.
     */
    private final Map<Integer, Gene> geneMap;

    RawDataLoader(Set<RawDataFilter> rawDataFilters, RawDataService rawDataService) {
        if (rawDataFilters == null || rawDataFilters.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("At least one RawDataFilter must be provided"));
        }
        if (rawDataFilters.contains(null)) {
            throw log.throwing(new IllegalArgumentException("No RawDataFilter can be null"));
        }
        if (rawDataService == null) {
            throw log.throwing(new IllegalArgumentException("A RawDataService must be provided"));
        }
        this.rawDataFilters = Collections.unmodifiableSet(
                rawDataFilters == null? new HashSet<>(): new HashSet<>(rawDataFilters));
        this.rawDataService = rawDataService;
        //TODO: to continue
        this.geneMap = null;
        this.daoRawDataFilters = null;
    }

    /**
     * @return  A {@code Stream} of {@code AffymetrixProbeset}s.
     *          If the {@code Stream} contains no element,
     *          it means that there were no data of this type for the requested parameters.
     */
    public Stream<AffymetrixProbeset> loadAffymetrixProbesets() {
        log.entry();
        //TODO: to continue
        return null;
    }

    public Set<RawDataFilter> getRawDataFilters() {
        return this.rawDataFilters;
    }

    //hashCode/equals do not use the RawDataService
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((rawDataFilters == null) ? 0 : rawDataFilters.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        RawDataLoader other = (RawDataLoader) obj;
        if (rawDataFilters == null) {
            if (other.rawDataFilters != null) {
                return false;
            }
        } else if (!rawDataFilters.equals(other.rawDataFilters)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RawDataLoader [rawDataFilters=").append(rawDataFilters)
               .append("]");
        return builder.toString();
    }
}
