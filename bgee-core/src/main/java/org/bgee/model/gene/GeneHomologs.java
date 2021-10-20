package org.bgee.model.gene;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

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
 * @version Bgee 14.2 Feb. 2021
 * @since Bgee 14.2 Feb. 2021
 * @see Gene 
 */
public class GeneHomologs {
    private final static Logger log = LogManager.getLogger(GeneHomologs.class.getName());

    /**
     * Merge the orthologs and paralogs {@code Map}s of two {@code GeneHomologs} related to a same
     * target {@code Gene}. The {@code Map}s are re-ordered appropriatly based on the taxonomic level.
     *
     * @param gh1   First {@code GeneHomologs} to merge
     * @param gh2   Second {@code GeneHomologs} to merge
     * @return      The resulting merged {@code GeneHomologs}
     * @throws IllegalArgumentException If the two {@code GeneHomologs} do not have the same
     *                                  target {@code Gene}.
     */
    public static GeneHomologs mergeGeneHomologs(GeneHomologs gh1, GeneHomologs gh2)
            throws IllegalArgumentException {
        log.traceEntry("{}, {}", gh1, gh2);
        if (!gh1.getGene().equals(gh2.getGene())) {
            throw log.throwing(new IllegalArgumentException(
                    "Cannot merge GeneHomologs for different genes"));
        }
        LinkedHashMap<Taxon, Set<Gene>> orthologs = new LinkedHashMap<>(gh1.getOrthologsByTaxon());
        for (Entry<Taxon, Set<Gene>> taxonOrthologs2: gh2.getOrthologsByTaxon().entrySet()) {
            orthologs.merge(taxonOrthologs2.getKey(), taxonOrthologs2.getValue(),
                    (v1, v2) -> {v1.addAll(v2); return v1;});
        }
        orthologs = GeneHomologsService.sortMapByTaxon(orthologs);

        LinkedHashMap<Taxon, Set<Gene>> paralogs = new LinkedHashMap<>(gh1.getParalogsByTaxon());
        for (Entry<Taxon, Set<Gene>> taxonParalogs2: gh2.getParalogsByTaxon().entrySet()) {
            paralogs.merge(taxonParalogs2.getKey(), taxonParalogs2.getValue(),
                    (v1, v2) -> {v1.addAll(v2); return v1;});
        }
        paralogs = GeneHomologsService.sortMapByTaxon(paralogs);

        return log.traceExit(new GeneHomologs(gh1.getGene(), orthologs, paralogs));
    }

    private final Gene gene;
    private final LinkedHashMap<Taxon, Set<Gene>> orthologsByTaxon;
    private final LinkedHashMap<Taxon, Set<Gene>> paralogsByTaxon;
    
    public GeneHomologs(Gene gene, LinkedHashMap<Taxon, Set<Gene>> orthologsByTaxon,
            LinkedHashMap<Taxon, Set<Gene>> paralogsByTaxon) {
        log.traceEntry("{}, {}, {}", gene, orthologsByTaxon, paralogsByTaxon);
        if (gene == null) {
            throw log.throwing(new IllegalArgumentException("a gene can not be null"));
        }

        this.gene = gene;
        this.orthologsByTaxon = orthologsByTaxon == null? new LinkedHashMap<>():
            orthologsByTaxon.entrySet().stream().collect(Collectors.toMap(
                    e -> e.getKey(),
                    e -> new HashSet<>(e.getValue()),
                    (v1, v2) -> {throw log.throwing(new IllegalStateException("Collision impossible"));},
                    LinkedHashMap::new));
        this.paralogsByTaxon = paralogsByTaxon == null? new LinkedHashMap<>():
            paralogsByTaxon.entrySet().stream().collect(Collectors.toMap(
                    e -> e.getKey(),
                    e -> new HashSet<>(e.getValue()),
                    (v1, v2) -> {throw log.throwing(new IllegalStateException("Collision impossible"));},
                    LinkedHashMap::new));
    }

    /**
     * @return  The {@code Gene} for which orthologs and/or paralogs were requested.
     */
    public Gene getGene() {
        return gene;
    }

    /**
     * @return  A {@code LinkedHashMap} where the key is a {@code Taxon}, the associated value
     *          being a {@code Set} of {@code Gene}s that are the orthologs of the {@code Gene}
     *          returned by {@link #getGene()} for the associated {@code Taxon}. This {@code Map}
     *          is ordered from the closest taxon to the oldest taxon, and is a copy
     *          that can be safely modified.
     */
    public LinkedHashMap<Taxon, Set<Gene>> getOrthologsByTaxon() {
        //defensive copying, not possible to make an immutable LinkedHashMap in vanilla Java
        return new LinkedHashMap<>(orthologsByTaxon);
    }
    /**
     * @return  A {@code LinkedHashMap} where the key is a {@code Taxon}, the associated value
     *          being a {@code Set} of {@code Gene}s that are the paralogs of the {@code Gene}
     *          returned by {@link #getGene()} for the associated {@code Taxon}. This {@code Map}
     *          is ordered from the closest taxon to the oldest taxon, and is a copy
     *          that can be safely modified.
     */
    public LinkedHashMap<Taxon, Set<Gene>> getParalogsByTaxon() {
        //defensive copying, not possible to make an immutable LinkedHashMap in vanilla Java
        return new LinkedHashMap<>(paralogsByTaxon);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("GeneHomologs [gene=").append(gene)
               .append(", orthologsByTaxon=").append(orthologsByTaxon)
               .append(", paralogsByTaxon=").append(paralogsByTaxon)
               .append("]");
        return builder.toString();
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
