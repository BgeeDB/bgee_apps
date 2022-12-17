package org.bgee.model.expressiondata.call.multispecies;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.expressiondata.call.Call;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.species.TaxonomyFilter;

/**
* A filter to parameterize queries to {@link MultiSpeciesCallService}. 
* 
* @author  Julien Wollbrett
* @version Bgee 14, Mar. 2017
* @since   Bgee 14, Mar. 2017
* @param T The type of {@code CallData} to be used by this {@code MultiSpeciesExpresionCallFilter}. 
*          Can be declared as {@code CallData}, to include a mixture of {@code CallData} subtypes, 
*          or as a specific subtype, for instance, {@code ExpressionCallData}.
* @param U The type of {@code SummaryCallType} to be used by this {@code MultiSpeciesExpresionCallFilter}.
*/

public class MultiSpeciesExpressionCallFilter implements Predicate<MultiSpeciesCall<Call<?,?>>>{
	
	private final MultiSpeciesConditionFilter multiSpeciesCondFilter;
	
	private final TaxonomyFilter taxonFilter;
	
	private final Map<ExpressionSummary, SummaryQuality> summaryCallTypeQualityFilter;
	
	private final Set<DataType> dataTypeFilters;
	
	public MultiSpeciesExpressionCallFilter(MultiSpeciesConditionFilter multiSpeciesCondFilter,
			TaxonomyFilter taxonFilter, Map<ExpressionSummary,
			SummaryQuality> minCallQualityLevel, Set<DataType> dataTypeFilters) {
		this.multiSpeciesCondFilter = multiSpeciesCondFilter;
		this.taxonFilter = taxonFilter;
		this.summaryCallTypeQualityFilter = Collections.unmodifiableMap(minCallQualityLevel);
		this.dataTypeFilters = Collections.unmodifiableSet(dataTypeFilters == null? new HashSet<>(): dataTypeFilters);
	}

	public MultiSpeciesConditionFilter getMultiSpeciesCondFilter() {
		return multiSpeciesCondFilter;
	}

	public TaxonomyFilter getTaxonFilter() {
		return taxonFilter;
	}

	public Map<ExpressionSummary, SummaryQuality> getSummaryCallTypeQualityFilter() {
		return summaryCallTypeQualityFilter;
	}
	
	public Set<DataType> getDataTypeFilters() {
		return dataTypeFilters;
	}

	@Override
	public boolean test(MultiSpeciesCall<Call<?, ?>> t) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
        builder.append("MultiSpeciesExpressionCallFilter [multiSpeciesCondFilter=").append(multiSpeciesCondFilter)
               .append(", taxonFilter=").append(taxonFilter)
               .append(", summaryCallTypeQualityFilter=").append(summaryCallTypeQualityFilter)
               .append(", dataTypeFilters=").append(dataTypeFilters).append("]");
        return builder.toString();
	}
	
	@Override
	public int hashCode(){
		final int prime = 31;
        int result = 1;
        result = prime * result + ((multiSpeciesCondFilter == null) ? 0 : multiSpeciesCondFilter.hashCode());
        result = prime * result + ((taxonFilter == null) ? 0 : taxonFilter.hashCode());
        result = prime * result + ((summaryCallTypeQualityFilter == null) ? 0 : summaryCallTypeQualityFilter.hashCode());
        result = prime * result + ((dataTypeFilters == null) ? 0 : dataTypeFilters.hashCode());
        return result;
	}
	
	@Override
	public boolean equals(Object obj){
		if (this == obj) {
            return true;
        }
		if(obj == null){
			return false;
		}
		if (obj.getClass() != this.getClass()){
			return false;
		}
		MultiSpeciesExpressionCallFilter other = (MultiSpeciesExpressionCallFilter) obj;
		if (multiSpeciesCondFilter == null) {
            if (other.getMultiSpeciesCondFilter() != null) {
                return false;
            }
        } else if (!multiSpeciesCondFilter.equals(other.getMultiSpeciesCondFilter())) {
            return false;
        }
		if (taxonFilter == null) {
            if (other.getTaxonFilter() != null) {
                return false;
            }
        } else if (!taxonFilter.equals(other.getTaxonFilter())) {
            return false;
        }
		if (summaryCallTypeQualityFilter == null) {
            if (other.getSummaryCallTypeQualityFilter() != null) {
                return false;
            }
        } else if (!summaryCallTypeQualityFilter.equals(other.getSummaryCallTypeQualityFilter())) {
            return false;
        }
		if (dataTypeFilters == null) {
            if (other.getDataTypeFilters() != null) {
                return false;
            }
        } else if (!dataTypeFilters.equals(other.getDataTypeFilters())) {
            return false;
        }
		
		return true;
	}

}
