package org.bgee.model.gene;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

//Basically corresponds to a OMA node
public class OrthologousGeneGroup {
    private final Integer taxonId;
    private final Set<Gene> genes;
    private final String omaGroupId;
    private final Integer omaNodeId;
    
    public OrthologousGeneGroup(Integer taxonId, String omaGroupId, Integer omaNodeId, Set<Gene> genes) {
    	this.taxonId = taxonId;
    	this.omaGroupId = omaGroupId;
    	this.omaNodeId = omaNodeId;
    	this.genes = Collections.unmodifiableSet(genes == null ? new HashSet<Gene>(): genes);
    }

	public Integer getTaxonId() {
		return taxonId;
	}

	public Set<Gene> getGenes() {
		return genes;
	}

	public String getOmaGroupId() {
		return omaGroupId;
	}
    
	public Integer getOMANodeId() {
		return omaNodeId;
	}
	@Override
    public String toString(){
    	return "OrthologousGeneGroup [taxonId=" + taxonId + ", omagroupId=" + omaGroupId + ", omaNodeId=" + omaNodeId + ", genes=" + genes + "]";
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
        OrthologousGeneGroup other = (OrthologousGeneGroup) obj;
        if (taxonId == null) {
            if (other.taxonId != null) {
                return false;
            }
        } else if (!taxonId.equals(other.taxonId)) {
            return false;
        }
        if (omaGroupId == null) {
            if (other.omaGroupId != null) {
                return false;
            }
        } else if (!omaGroupId.equals(other.omaGroupId)) {
            return false;
        }
        if (omaNodeId == null) {
            if (other.omaNodeId != null) {
                return false;
            }
        } else if (!omaNodeId.equals(other.omaNodeId)) {
            return false;
        }
        if (genes == null) {
            if (other.genes != null) {
                return false;
            }
        } else if (!genes.equals(other.genes)) {
            return false;
        }
        return true;
	}

	@Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((taxonId == null) ? 0 : taxonId.hashCode());
        result = prime * result + ((genes == null) ? 0 : genes.hashCode());
        result = prime * result + ((omaNodeId == null) ? 0 : omaNodeId.hashCode());
        result = prime * result + ((omaGroupId == null) ? 0 : omaGroupId.hashCode());
        return result;
    }
}
