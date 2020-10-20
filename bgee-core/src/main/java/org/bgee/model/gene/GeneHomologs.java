package org.bgee.model.gene;

import java.util.LinkedHashMap;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.species.Taxon;

/**
 * A class describing a gene and its homology relations (orthologs and/or paralogs). 
 * Homology are stored as {@code LinkedHashMap} where the key correspond to {@code Taxon} and
 * Keys correspond to {@code Set} of {@code Gene} in order to keep proper Taxon order (e.g more
 * recent to oldest).
 * 
 * 
 * @author Julien Wollbrett
 * @version Bgee 15 Oct. 2020
 * @since Bgee 15 Sep. 2020
 * @see Gene 
 */
public class GeneHomologs {
    
    private final static Logger log = LogManager.getLogger(GeneHomologs.class.getName());
    
    private final Gene gene;
    private final LinkedHashMap<Taxon, Set<Gene>> orthologsByTaxon;
    private final LinkedHashMap<Taxon, Set<Gene>> paralogsByTaxon;
    
    public GeneHomologs(Gene gene, LinkedHashMap<Taxon, Set<Gene>> orthologsByTaxon,
            LinkedHashMap<Taxon, Set<Gene>> paralogsByTaxon) {
        log.entry(orthologsByTaxon, paralogsByTaxon);
        if (gene == null) {
            throw log.throwing(new IllegalArgumentException("a gene can not be null"));
        }

        this.gene = gene;
        this.orthologsByTaxon = orthologsByTaxon;
        this.paralogsByTaxon = paralogsByTaxon;
    }
    
    public Gene getGene() {
        return gene;
    }

    public LinkedHashMap<Taxon, Set<Gene>> getOrthologsByTaxon() {
        return orthologsByTaxon;
    }

    public LinkedHashMap<Taxon, Set<Gene>> getParalogsByTaxon() {
        return paralogsByTaxon;
    }

    @Override
    public String toString() {
        return "GeneHomolog [gene=" + gene + ", orthologsByTaxon=" + orthologsByTaxon + ", paralogsByTaxon="
                + paralogsByTaxon + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((gene == null) ? 0 : gene.hashCode());
        result = prime * result + ((orthologsByTaxon == null) ? 0 : orthologsByTaxon.hashCode());
        result = prime * result + ((paralogsByTaxon == null) ? 0 : paralogsByTaxon.hashCode());
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
        GeneHomologs other = (GeneHomologs) obj;
        if (gene == null) {
            if (other.gene != null)
                return false;
        } else if (!gene.equals(other.gene))
            return false;
        if (orthologsByTaxon == null) {
            if (other.orthologsByTaxon != null)
                return false;
        } else if (!orthologsByTaxon.equals(other.orthologsByTaxon))
            return false;
        if (paralogsByTaxon == null) {
            if (other.paralogsByTaxon != null)
                return false;
        } else if (!paralogsByTaxon.equals(other.paralogsByTaxon))
            return false;
        return true;
    }


    
    

}
