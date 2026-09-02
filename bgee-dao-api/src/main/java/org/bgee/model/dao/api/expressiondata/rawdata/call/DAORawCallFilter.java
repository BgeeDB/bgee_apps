package org.bgee.model.dao.api.expressiondata.rawdata.call;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.expressiondata.DAODataFilter;
import org.bgee.model.dao.api.expressiondata.DAODataType;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataConditionFilter;

public class DAORawCallFilter extends  DAODataFilter<DAORawDataConditionFilter>{

    private final static Logger log = LogManager.getLogger(DAORawCallFilter.class.getName());
    private final EnumSet<DAODataType> dataTypes;
    public DAORawCallFilter(Collection<Integer> geneIds, Collection<Integer> speciesIds,
            Collection<DAORawDataConditionFilter> conditionFilters, EnumSet<DAODataType> dataTypes) {
        super(geneIds, speciesIds, conditionFilters);
        if ((geneIds == null || geneIds.isEmpty()) && (speciesIds == null  || speciesIds.isEmpty()) &&
            (conditionFilters == null || conditionFilters.isEmpty())) { 
            throw log.throwing(new IllegalArgumentException("at least one geneId, speciesId or condition filter"
                    + " should be provided"));
        }
        this.dataTypes = dataTypes == null ? EnumSet.allOf(DAODataType.class) : dataTypes;
    }

    public EnumSet<DAODataType> getDataTypes() {
        return dataTypes;
    }

    @Override
    public String toString() {
        return "DAORawCallFilter [dataTypes=" + dataTypes + ", getGeneIds()=" + getGeneIds() + ", getSpeciesIds()="
                + getSpeciesIds() + ", getConditionFilters()=" + getConditionFilters() + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(dataTypes);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        DAORawCallFilter other = (DAORawCallFilter) obj;
        return Objects.equals(dataTypes, other.dataTypes);
    }

    
}
