package org.bgee.model.expressiondata;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntitySimilarity;
import org.bgee.model.anatdev.DevStageSimilarity;

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

    /**
     * An {@code AnatEntitySimilarity} that is the group of homologous anatomical entities
     * of this {@code MultiSpeciesCall}.
     */
    private final AnatEntitySimilarity anatSimilarity;
    
    /**
     * A {@code DevStageSimilarity} that is the group of developmental stages
     * of this {@code MultiSpeciesCall}.
     */
    private final DevStageSimilarity stageSimilarity;
    
    /**
     * A {@code String} that is the ID of the taxon of this {@code MultiSpeciesCall}.
     */
    private final String taxonId;

    /**
     * A {@code String} that is the ID of the OMA node of orthologous genes
     * of this {@code MultiSpeciesCall}.
     */
    private final String omaNodeId;

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
     * A {@code ServiceFactory} to obtain {@code Service} objects.
     */
    private final ServiceFactory serviceFactory;

    /**
     * Constructor providing the anat entity similarity group, the dev. stage similarity group,
     * the taxon ID, the OMA node ID and its gene IDs, the single-species calls,
     * and the conservation score of this {@code MultiSpeciesCall}. 
     * 
     * @param anatSimilarity    An {@code AnatEntitySimilarity} that is the group 
     *                          of homologous organs of this call.
     * @param stageSimilarity   A {@code DevStageSimilarity} that is the group of stages of this call.
     * @param taxonId           A {@code String} that is the ID of the taxon of this call.
     * @param omaNodeId         A {@code String} that is the ID of the OMA node of 
     *                          orthologous genes of this call.
     * @param orthologGeneIds   A {@code Collection} of {@code String}s that are the IDs of
     *                          the orthologous genes of this {@code omaNodeId}.
     * @param calls             A {@code Collection} of {@code Call}s that are single-species calls
     *                          used to constitute this {@code MultiSpeciesCall}.
     * @param conservationScore A {@code BigDecimal} that is the conservation score
     *                          of this {@code MultiSpeciesCall}.
     */
    public MultiSpeciesCall(AnatEntitySimilarity anatSimilarity, DevStageSimilarity stageSimilarity,
            String taxonId, String omaNodeId, Collection<String> orthologousGeneIds, Collection<T> calls,
            BigDecimal conservationScore, ServiceFactory serviceFactory) {
        if (serviceFactory == null) {
            throw log.throwing(new IllegalArgumentException("A ServiceFactory must be provided."));
        }

        this.anatSimilarity = anatSimilarity;
        this.stageSimilarity = stageSimilarity;
        this.taxonId = taxonId;
        this.omaNodeId = omaNodeId;
        this.orthologousGeneIds = Collections.unmodifiableSet(
                orthologousGeneIds == null? new HashSet<>(): new HashSet<>(orthologousGeneIds));
        this.calls = Collections.unmodifiableSet(calls == null? new HashSet<>(): new HashSet<>(calls));
        this.conservationScore = conservationScore;
        this.serviceFactory = serviceFactory;
    }
    
    /**
     * @return  The {@code AnatEntitySimilarity} that is the group of homologous anatomical entities
     *          of this {@code MultiSpeciesCall}.
     */
    public AnatEntitySimilarity getAnatEntitySimilarity() {
        return anatSimilarity;
    }


    /**
     * @return  The {@code DevStageSimilarity} that is the group of developmental stages
     *          of this {@code MultiSpeciesCall}.
     */
    public DevStageSimilarity getDevStageSimilarity() {
        return stageSimilarity;
    }

    /**
     * @return  The {@code String} that is the ID of the taxon of this {@code MultiSpeciesCall}.
     */
    public String getTaxonId() {
        return taxonId;
    }

    /**
     * @return  The {@code String} that is the ID of the OMA node of orthologous genes
     *          of this {@code MultiSpeciesCall}.
     */
    public String getOMANodeId() {
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
     * @return  The {@code Set} of {@code String}s that are the IDs of the species of 
     *          orthologous genes of this {@code MultiSpeciesCall}.
     */
    public Set<Integer> getSpeciesIds() {
        return log.exit(this.serviceFactory.getGeneService()
                .loadGenesByIdsAndSpeciesIds(this.getOrthologousGeneIds(), null).stream()
                .map(g -> g.getSpeciesId()).collect(Collectors.toSet()));
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((omaNodeId == null) ? 0 : omaNodeId.hashCode());
        result = prime * result + ((anatSimilarity == null) ? 0 : anatSimilarity.hashCode());
        result = prime * result + ((stageSimilarity == null) ? 0 : stageSimilarity.hashCode());
        result = prime * result + ((calls == null) ? 0 : calls.hashCode());
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
        if (omaNodeId == null) {
            if (other.omaNodeId != null) {
                return false;
            }
        } else if (!omaNodeId.equals(other.omaNodeId)) {
            return false;
        }
        if (anatSimilarity == null) {
            if (other.anatSimilarity != null) {
                return false;
            }
        } else if (!anatSimilarity.equals(other.anatSimilarity)) {
            return false;
        }
        if (stageSimilarity == null) {
            if (other.stageSimilarity != null) {
                return false;
            }
        } else if (!stageSimilarity.equals(other.stageSimilarity)) {
            return false;
        }
        if (calls == null) {
            if (other.calls != null) {
                return false;
            }
        } else if (!calls.equals(other.calls)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Anat. entity similarity: " + getAnatEntitySimilarity() + " - Dev. stage similarity: "
                + getDevStageSimilarity() + " - OMA node ID: " + getOMANodeId() + " - Calls: " + getCalls();
    }
}
