package org.bgee.model.dao.api.expressiondata;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataConditionFilter;

public class DAOObservedExpressionFilter {

    //XXX: the limitation of this approach is that we can not combine conditionIds and speciesIds as they are part
    //of rawDataConditionFilter and we force to have either conditionIds or rawDataConditionFilter. The solution would be
    //for DAOObservedExpressionFilter to extends DAOBaseConditionFilter and remove DAORawDataConditionFilter as an attribute.
    private final Set<Integer> bgeeGeneIds;
    private final EnumSet<DAODataType> datatypes;
    private final DAORawDataConditionFilter rawDataConditionFilter;
    private final Set<Integer> conditionIds;
    private final static Logger log = LogManager.getLogger(DAOObservedExpressionFilter.class.getName());


    public DAOObservedExpressionFilter(Collection<Integer> bgeeGeneIds, EnumSet<DAODataType> datatypes,
            DAORawDataConditionFilter rawDataConditionFilter) {
        this(bgeeGeneIds, datatypes, rawDataConditionFilter, null);
    }

    public DAOObservedExpressionFilter(Collection<Integer> bgeeGeneIds, EnumSet<DAODataType> datatypes,
            Collection<Integer> conditionIds) {
        this(bgeeGeneIds, datatypes, null, conditionIds);
    }

    private DAOObservedExpressionFilter(Collection<Integer> bgeeGeneIds, EnumSet<DAODataType> datatypes,
            DAORawDataConditionFilter rawDataConditionFilter, Collection<Integer> conditionIds) {
        log.traceEntry("{}, {}, {}, {}", bgeeGeneIds, datatypes, rawDataConditionFilter, conditionIds);
        this.bgeeGeneIds = Collections.unmodifiableSet(bgeeGeneIds == null ? new HashSet<>() :
            bgeeGeneIds.stream().filter(id -> {return (id != null && id >= 1);}).collect(Collectors.toSet()));
        this.datatypes = datatypes == null || datatypes.isEmpty() ? EnumSet.allOf(DAODataType.class) :
            EnumSet.copyOf(datatypes);
        this.rawDataConditionFilter = rawDataConditionFilter;
        this.conditionIds = Collections.unmodifiableSet(conditionIds == null ? new HashSet<>() : new HashSet<>(conditionIds));
        //For now we force at least one filter other than datatype to be provided. It avoids retrieving the full expression table
        if (allFiltersExceptDatatypesAreEmpty()) {
            throw log.throwing(new IllegalArgumentException("At least one expression filter other than datatypes"
                    + "should be provided"));
        }
    }

    public Set<Integer> getBgeeGeneIds() {
        return bgeeGeneIds;
    }

    public EnumSet<DAODataType> getDatatypes() {
        return datatypes;
    }

    public DAORawDataConditionFilter getRawDataConditionFilter() {
        return rawDataConditionFilter;
    }

    public Set<Integer> getConditionIds() {
        return conditionIds;
    }

    @Override
    public int hashCode() {
        return Objects.hash(bgeeGeneIds, datatypes, rawDataConditionFilter);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DAOObservedExpressionFilter other = (DAOObservedExpressionFilter) obj;
        return Objects.equals(bgeeGeneIds, other.bgeeGeneIds) && Objects.equals(datatypes, other.datatypes)
                && Objects.equals(rawDataConditionFilter, other.rawDataConditionFilter);
    }



    @Override
    public String toString() {
        return "DAOObservedExpressionFilter [bgeeGeneIds=" + bgeeGeneIds + ", datatypes=" + datatypes
                + ", rawDataConditionFilter=" + rawDataConditionFilter + ", conditionIds=" + conditionIds + "]";
    }

    private boolean allFiltersExceptDatatypesAreEmpty() {
        if (this.rawDataConditionFilter != null) {
            if (this.rawDataConditionFilter.getSpeciesIds() != null && ! this.rawDataConditionFilter.getSpeciesIds().isEmpty()) {
                return false;
            }
            if (! this.rawDataConditionFilter.areAllCondParamFiltersEmpty()) {
                return false;
            }
        }
        if (! this.bgeeGeneIds.isEmpty()) {
            return false;
        }
        if (! this.conditionIds.isEmpty()) {
            return false;
        }
        
        return true;
    }
}
