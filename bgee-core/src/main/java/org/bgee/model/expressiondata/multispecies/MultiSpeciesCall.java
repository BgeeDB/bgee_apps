package org.bgee.model.expressiondata.multispecies;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.CommonService;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntitySimilarity;
import org.bgee.model.anatdev.DevStageSimilarity;
import org.bgee.model.expressiondata.Call;

/**
 * Class describing multi-species calls.
 * 
 * @author  Philippe Moret
 * @author  Valentine Rech de Laval
 * @version Bgee 13, Aug. 2016
 * @since   Bgee 13, Apr. 2016
 */
public class MultiSpeciesCall<T extends Call<?, ?>> {

    private static final Logger log = LogManager.getLogger(MultiSpeciesCall.class.getName());

    
    private final MultiSpeciesCondition multiSpeciesCondition;
    //FIXME: we should have a class describing groups of orthologous genes, storing the taxon ID,
    //OMA ID etc, in the same way we have a MultiSpeciesCondition. The 3 following attributes
    //should then be replaced.
    /**
     * A {@code Integer} that is the ID of the taxon of this {@code MultiSpeciesCall}.
     */
    private final Integer taxonId;

    /**
     * A {@code String} that is the ID of the OMA node of orthologous genes
     * of this {@code MultiSpeciesCall}.
     */
    private final Integer omaNodeId;

    /**
     * A {@code Set} of {@code String}s that are the IDs of the orthologous genes defined 
     * by {@code omaNodeId} of this {@code MultiSpeciesCall}.
     */
    private final Set<String> orthologousGeneIds;    
        
    /**
     * A {@code Set} of {@code Call}s that are single-species calls
     * used to constitute this {@code MultiSpeciesCall}.
     */
    private final Set<T> calls;

    /**
     * A {@code BigDecimal} that is the conservation score of this {@code MultiSpeciesCall}.
     */
    private final BigDecimal conservationScore;

    /**
     * Constructor providing the anat entity similarity group, the dev. stage similarity group,
     * the taxon ID, the OMA node ID and its gene IDs, the single-species calls,
     * and the conservation score of this {@code MultiSpeciesCall}. 
     * 
     * @param anatSimilarity    An {@code AnatEntitySimilarity} that is the group 
     *                          of homologous organs of this call.
     * @param stageSimilarity   A {@code DevStageSimilarity} that is the group of stages of this call.
     * @param taxonId           An {@code Integer} that is the ID of the taxon of this call.
     * @param omaNodeId         An {@code Integer} that is the ID of the OMA node of 
     *                          orthologous genes of this call.
     * @param orthologGeneIds   A {@code Collection} of {@code String}s that are the IDs of
     *                          the orthologous genes of this {@code omaNodeId}.
     * @param calls             A {@code Collection} of {@code Call}s that are single-species calls
     *                          used to constitute this {@code MultiSpeciesCall}.
     * @param conservationScore A {@code BigDecimal} that is the conservation score
     *                          of this {@code MultiSpeciesCall}.
     */
    public MultiSpeciesCall(MultiSpeciesCondition cond,
            Integer taxonId, Integer omaNodeId, Collection<String> orthologousGeneIds, Collection<T> calls,
            BigDecimal conservationScore) {

        this.multiSpeciesCondition = cond;
        this.taxonId = taxonId;
        this.omaNodeId = omaNodeId;
        this.orthologousGeneIds = Collections.unmodifiableSet(
                orthologousGeneIds == null? new HashSet<>(): new HashSet<>(orthologousGeneIds));
        this.calls = Collections.unmodifiableSet(calls == null? new HashSet<>(): new HashSet<>(calls));
        this.conservationScore = conservationScore;
    }
    

    public MultiSpeciesCondition getMultiSpeciesCondition() {
        return multiSpeciesCondition;
    }

    /**
     * @return  The {@code Integer} that is the ID of the taxon of this {@code MultiSpeciesCall}.
     */
    public Integer getTaxonId() {
        return taxonId;
    }

    /**
     * @return  The {@code Integer} that is the ID of the OMA node of orthologous genes
     *          of this {@code MultiSpeciesCall}.
     */
    public Integer getOMANodeId() {
        return omaNodeId;
    }

    /**
     * @return  The {@code Set} of {@code String}s that are the IDs of the orthologous genes 
     *          defined by {@code omaNodeId} of this {@code MultiSpeciesCall}.
     */
    public Set<String> getOrthologousGeneIds() {
        return orthologousGeneIds;
    }

    /**
     * @return  The {@code Set} of {@code Call}s that are single-species calls
     *          used to constitute this {@code MultiSpeciesCall}.
     */
    public Set<T> getCalls() {
        return calls;
    }

    /**
     * @return  The {@code BigDecimal} that is the conservation score of this {@code MultiSpeciesCall}.
     */
    public BigDecimal getConservationScore() {
        return conservationScore;
    }
    
    /** 
     * Helper method to obtain IDs of species from orthologous gene IDs of this {@code MultiSpeciesCall}.
     * 
     * @return  The {@code Set} of {@code Integer}s that are the IDs of the species of 
     *          orthologous genes of this {@code MultiSpeciesCall}.
     */
    public Set<Integer> getSpeciesIds() {
        //FIXME: to reimplement
        throw new UnsupportedOperationException("To implement");
//        return log.exit(this.getServiceFactory().getGeneService()
//                .loadGenesByIdsAndSpeciesIds(this.getOrthologousGeneIds(), null).stream()
//                .map(g -> g.getSpeciesId()).collect(Collectors.toSet()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((calls == null) ? 0 : calls.hashCode());
        result = prime * result + ((conservationScore == null) ? 0 : conservationScore.hashCode());
        result = prime * result + ((multiSpeciesCondition == null) ? 0 : multiSpeciesCondition.hashCode());
        result = prime * result + ((omaNodeId == null) ? 0 : omaNodeId.hashCode());
        result = prime * result + ((orthologousGeneIds == null) ? 0 : orthologousGeneIds.hashCode());
        result = prime * result + ((taxonId == null) ? 0 : taxonId.hashCode());
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
        MultiSpeciesCall<?> other = (MultiSpeciesCall<?>) obj;
        if (calls == null) {
            if (other.calls != null) {
                return false;
            }
        } else if (!calls.equals(other.calls)) {
            return false;
        }
        if (conservationScore == null) {
            if (other.conservationScore != null) {
                return false;
            }
        } else if (!conservationScore.equals(other.conservationScore)) {
            return false;
        }
        if (multiSpeciesCondition == null) {
            if (other.multiSpeciesCondition != null) {
                return false;
            }
        } else if (!multiSpeciesCondition.equals(other.multiSpeciesCondition)) {
            return false;
        }
        if (omaNodeId == null) {
            if (other.omaNodeId != null) {
                return false;
            }
        } else if (!omaNodeId.equals(other.omaNodeId)) {
            return false;
        }
        if (orthologousGeneIds == null) {
            if (other.orthologousGeneIds != null) {
                return false;
            }
        } else if (!orthologousGeneIds.equals(other.orthologousGeneIds)) {
            return false;
        }
        if (taxonId == null) {
            if (other.taxonId != null) {
                return false;
            }
        } else if (!taxonId.equals(other.taxonId)) {
            return false;
        }
        return true;
    }


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MultiSpeciesCall [multiSpeciesCondition=").append(multiSpeciesCondition)
               .append(", taxonId=").append(taxonId)
               .append(", omaNodeId=").append(omaNodeId)
               .append(", orthologousGeneIds=").append(orthologousGeneIds)
               .append(", calls=").append(calls)
               .append(", conservationScore=").append(conservationScore).append("]");
        return builder.toString();
    }

}
