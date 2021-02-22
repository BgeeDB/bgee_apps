package org.bgee.model.expressiondata.multispecies;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.Call;
import org.bgee.model.gene.Gene;

/**
 * Class describing multi-species calls.
 * 
 * @author  Philippe Moret
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Mar. 2019
 * @since   Bgee 13, Apr. 2016
 */
public class MultiSpeciesCall<T extends Call<?, ?>> {

    private static final Logger log = LogManager.getLogger(MultiSpeciesCall.class.getName());
    
    public static final float CONSERVATION_SCORE_THRESHOLD = (float) 0.75;

    
    private final MultiSpeciesCondition multiSpeciesCondition;
    //FIXME: we should have a class describing groups of orthologous genes, storing the taxon ID,
    //OMA ID etc, in the same way we have a MultiSpeciesCondition. The 3 following attributes
    //should then be replaced.
    /**
     * A {@code Integer} that is the ID of the taxon of this {@code MultiSpeciesCall}.
     */
    private final Integer taxonId;

    /**
     * A {@code String} that is the ID of the OMA Group of orthologous genes
     * of this {@code MultiSpeciesCall}.
     */
    private final String omaGroupId;

    /**
     * A {@code Set} of {@code Gene}s that are the orthologous genes defined 
     * by {@code omaGroupId} and {@code taxonId} of this {@code MultiSpeciesCall}.
     */
    private final Set<Gene> orthologousGenes;    
        
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
     * Constructor providing the multi-species condition, the taxon ID, the OMA group ID 
     * and its orthologous genes, the single-species calls, and the conservation score of 
     * this {@code MultiSpeciesCall}. 
     * 
     * @param cond              A {@code MultiSpeciesCondition} that is the condition
     *                          related to this call.
     * @param taxonId           An {@code Integer} that is the ID of the taxon of this call.
     * @param omaGroupId        A {@code String} that is the ID of the OMA Group of 
     *                          orthologous genes of this call.
     * @param orthologousGenes  A {@code Collection} of {@code Gene}s that are the orthologous genes
     *                          of both this {@code omaGroupId} and this {@code taxonId}.
     * @param calls             A {@code Collection} of {@code Call}s that are single-species calls
     *                          used to constitute this {@code MultiSpeciesCall}.
     * @param conservationScore A {@code BigDecimal} that is the conservation score
     *                          of this {@code MultiSpeciesCall}.
     */
    public MultiSpeciesCall(MultiSpeciesCondition cond, Integer taxonId, String omaGroupId,
                            Collection<Gene> orthologousGenes, Collection<T> calls,
                            BigDecimal conservationScore) {

        if (calls != null && calls.stream().anyMatch(c -> c.getGene() == null)) {
            throw log.throwing(new IllegalArgumentException("No gene of single-species calls can be null"));
        }
        if (calls != null && orthologousGenes != null 
                && calls.stream().anyMatch(c -> !orthologousGenes.contains(c.getGene()))) {
            throw log.throwing(new IllegalArgumentException(
                    "All genes of single-species calls should be in the provided genes"));
        }

        this.multiSpeciesCondition = cond;
        this.taxonId = taxonId;
        this.omaGroupId = omaGroupId;
        this.orthologousGenes = Collections.unmodifiableSet(
                orthologousGenes == null? new HashSet<>(): new HashSet<>(orthologousGenes));
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
     * @return  The {@code String} that is the ID of the OMA group of orthologous genes
     *          of this {@code MultiSpeciesCall}.
     */
    public String getOMAGroupId() {
        return omaGroupId;
    }

    /**
     * @return  The {@code Set} of {@code Genes}s that are the orthologous genes 
     *          defined by {@code omaGroupId} and {@code taxonId} of this {@code MultiSpeciesCall}.
     */
    public Set<Gene> getOrthologousGenes() {
        return orthologousGenes;
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
//        return log.traceExit(this.getServiceFactory().getGeneService()
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
        result = prime * result + ((omaGroupId == null) ? 0 : omaGroupId.hashCode());
        result = prime * result + ((orthologousGenes == null) ? 0 : orthologousGenes.hashCode());
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
        if (omaGroupId == null) {
            if (other.omaGroupId != null) {
                return false;
            }
        } else if (!omaGroupId.equals(other.omaGroupId)) {
            return false;
        }
        if (orthologousGenes == null) {
            if (other.orthologousGenes != null) {
                return false;
            }
        } else if (!orthologousGenes.equals(other.orthologousGenes)) {
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
               .append(", omaGroupId=").append(omaGroupId)
               .append(", orthologousGenes=").append(orthologousGenes)
               .append(", calls=").append(calls)
               .append(", conservationScore=").append(conservationScore).append("]");
        return builder.toString();
    }

}
