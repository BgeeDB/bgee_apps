package org.bgee.model.gene;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A class describing gene homologs. It can be an ortholg or a paralog
 * 
 * @author Julien Wollbrett
 * @version Bgee 15 Sep. 2020
 * @since Bgee 15 Sep. 2020
 * @see Gene 
 */
public class GeneHomolog {
    
    private final static Logger log = LogManager.getLogger(GeneHomolog.class.getName());
    
    private Gene gene;
    private Integer taxonId;
    
    public GeneHomolog(Gene gene, Integer taxonId) {
        if (gene == null) {
            throw log.throwing(new IllegalArgumentException("a gene can not be null"));
        }
        this.gene = gene;
        this.taxonId = taxonId;
    }
    
    public Gene getGene() {
        return gene;
    }

    public Integer getTaxonId() {
        return taxonId;
    }

    @Override
    public String toString() {
        return "GeneHomolog [gene=" + gene + ", taxonId=" + taxonId + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((gene == null) ? 0 : gene.hashCode());
        result = prime * result + ((taxonId == null) ? 0 : taxonId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GeneHomolog other = (GeneHomolog) obj;
        if (gene == null) {
            if (other.gene != null)
                return false;
        } else if (!gene.equals(other.gene))
            return false;
        if (taxonId == null) {
            if (other.taxonId != null)
                return false;
        } else if (!taxonId.equals(other.taxonId))
            return false;
        return true;
    }
    
    

}
