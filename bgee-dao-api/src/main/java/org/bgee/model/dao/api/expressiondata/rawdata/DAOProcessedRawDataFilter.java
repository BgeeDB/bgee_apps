package org.bgee.model.dao.api.expressiondata.rawdata;



import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DAOProcessedRawDataFilter {

    private final static Logger log = LogManager.getLogger(DAOProcessedRawDataFilter.class.getName());
    private final Set<DAORawDataFilter> rawDataFilters;
    private final boolean needGeneId;
    private final boolean needAssayId;
    private final boolean needExperimentId;
    private final boolean needSpeciesId;
    private final boolean needConditionId;
    private final boolean alwaysGeneId;

    public DAOProcessedRawDataFilter(Collection<DAORawDataFilter> rawDataFilters) {
        this.rawDataFilters = Collections.unmodifiableSet(rawDataFilters == null ?
                new LinkedHashSet<>() : new LinkedHashSet<>(rawDataFilters));
        this.needSpeciesId = this.rawDataFilters.stream().anyMatch(e -> e.getSpeciesId() != null);
        this.needGeneId = this.rawDataFilters.stream().anyMatch(e -> !e.getGeneIds().isEmpty());
        this.needAssayId = this.rawDataFilters.stream().anyMatch(e -> !e.getAssayIds().isEmpty() ||
                !e.getExprOrAssayIds().isEmpty());
        this.needConditionId = this.rawDataFilters.stream().anyMatch(e -> !e.getRawDataCondIds().isEmpty());
        this.needExperimentId = this.rawDataFilters.stream().anyMatch(e -> !e.getExperimentIds().isEmpty() ||
                !e.getExprOrAssayIds().isEmpty());
        //check filters always used
        //XXX The idea is to not start with result table if geneIds are asked in only one filter
        // but not in others. Indeed, in this scenario forcing to start with porbeset table
        // decrease drastically the time needed to query. It is maybe overthinking as it is
        // probably also the case for other tables (especially the species table). The best
        // optimization is probably to query each DAORawDataFilter separately
        this.alwaysGeneId = !this.rawDataFilters.isEmpty() //allMatch returns true if a stream is empty
                && this.rawDataFilters.stream().allMatch(e -> !e.getGeneIds().isEmpty());
        assert !(alwaysGeneId && needSpeciesId):
            "In a RawDataDAOFilter, when bgeeGeneIds are provided, no species ID is provided";
        log.debug(this.toString());
    }

    public Set<DAORawDataFilter> getRawDataFilters() {
        return rawDataFilters;
    }

    public boolean isNeedGeneId() {
        return needGeneId;
    }

    public boolean isNeedAssayId() {
        return needAssayId;
    }

    public boolean isNeedExperimentId() {
        return needExperimentId;
    }

    public boolean isNeedSpeciesId() {
        return needSpeciesId;
    }

    public boolean isNeedConditionId() {
        return needConditionId;
    }

    public boolean isAlwaysGeneId() {
        return alwaysGeneId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(alwaysGeneId, needAssayId, needConditionId, needExperimentId, needGeneId, needSpeciesId,
                rawDataFilters);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DAOProcessedRawDataFilter other = (DAOProcessedRawDataFilter) obj;
        return alwaysGeneId == other.alwaysGeneId && needAssayId == other.needAssayId
                && needConditionId == other.needConditionId && needExperimentId == other.needExperimentId
                && needGeneId == other.needGeneId && needSpeciesId == other.needSpeciesId
                && Objects.equals(rawDataFilters, other.rawDataFilters);
    }

    @Override
    public String toString() {
        return "DAOProcessedRawDataFilter [rawDataFilters=" + rawDataFilters + ", needGeneId="
                + needGeneId + ", needAssayId=" + needAssayId + ", needExperimentId=" + needExperimentId
                + ", needSpeciesId=" + needSpeciesId + ", needConditionId=" + needConditionId + ", alwaysGeneId="
                + alwaysGeneId + "]";
    }

    
    
}
