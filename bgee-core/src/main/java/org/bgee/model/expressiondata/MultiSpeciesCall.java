package org.bgee.model.expressiondata;

import java.util.Set;

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

    private final AnatEntitySimilarity anatSimilarity;
    
    private final DevStageSimilarity stageSimilarity;
    
    private final String omaNodeId;
    
    private final Set<T> calls;

    /**
     * Constructor providing the anat entity similarity group, the dev. stage similarity group,
     * the OMA node ID and the single-species calls of this {@code MultiSpeciesCall}. 
     * 
     * @param anatSimilarity        An {@code AnatEntitySimilarity} that is the group 
     *                              of homologous organs of this call.
     * @param devStageSimilarity    A {@code DevStageSimilarity} that is the group of stages of this call.
     * @param omaNodeId             A {@code String} that is the ID of the OMA node of 
     *                              orthologous genes of this call.
     * @param calls                 A {@code Set} of {@code Call}s that are single-species calls
     *                              used to constitute this {@code MultiSpeciesCall}.
     */
    public MultiSpeciesCall(AnatEntitySimilarity anatSimilarity, DevStageSimilarity stageSimilarity,
            String omaNodeId, Set<T> calls) {
        this.anatSimilarity = anatSimilarity;
        this.stageSimilarity = stageSimilarity;
        this.omaNodeId = omaNodeId;
        this.calls = calls;
    }
    
    /**
     * @return The {@code AnatEntitySimilarity} that is the group of homologous organs of this call.
     */
    public AnatEntitySimilarity getAnatEntitySimilarity() {
        return anatSimilarity;
    }


    /**
     * @return The {@code DevStageSimilarity} that is the group of stages of this call.
     */
    public DevStageSimilarity getDevStageSimilarity() {
        return stageSimilarity;
    }


    /**
     * @return The {@code String} that is ID of the OMA node of orthologous genes of this call.
     */
    public String getOMANodeId() {
        return omaNodeId;
    }

    /**
     * @return  The {@code Set} of {@code Call}s that are single-species calls
     *          used to constitute this {@code MultiSpeciesCall}.
     */
    public Set<T> getCalls() {
        return calls;
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
        MultiSpeciesCall other = (MultiSpeciesCall) obj;
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
