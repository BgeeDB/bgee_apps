package org.bgee.model.dao.api;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.anatdev.AnatEntityDAO.AnatEntityTO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTO;
import org.bgee.model.dao.api.gene.GeneOntologyDAO.GOTermTO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTO;
import org.bgee.model.dao.api.species.TaxonDAO.TaxonTO;


/**
 * This class provides convenient methods when to use or analyze an {@code EntityTO}. 
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @since Bgee 13
 */
public class TOComparator {

    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = LogManager.getLogger(TOComparator.class.getName());

    /**
     * Method to compare two {@code SpeciesTO}s, to check for complete equality of each
     * attribute. This is because the {@code equals} method of {@code SpeciesTO}s is 
     * solely based on their ID, not on other attributes.
     * 
     * @param spTO1     A {@code SpeciesTO} to be compared to {@code spTO2}.
     * @param spTO2     A {@code SpeciesTO} to be compared to {@code spTO1}.
     * @return          {@code true} if {@code spTO1} and {@code spTO2} have all 
     *                  attributes equal.
     */
    public static boolean areSpeciesTOsEqual(SpeciesTO spTO1, SpeciesTO spTO2) {
            log.entry(spTO1, spTO2);
            if (TOComparator.areEntityTOsEqual(spTO1, spTO2) && 
                    StringUtils.equals(spTO1.getGenus(), spTO2.getGenus()) &&
                    StringUtils.equals(spTO1.getSpeciesName(), spTO2.getSpeciesName()) &&
                    StringUtils.equals(spTO1.getParentTaxonId(), spTO2.getParentTaxonId()) &&
                    StringUtils.equals(spTO1.getGenomeFilePath(), spTO2.getGenomeFilePath()) &&
                    StringUtils.equals(spTO1.getGenomeSpeciesId(), spTO2.getGenomeSpeciesId()) &&
                    StringUtils.equals(spTO1.getFakeGeneIdPrefix(), spTO2.getFakeGeneIdPrefix())) {
                return log.exit(true);
            }
            log.debug("Species are not equivalent {}", spTO1.getId());
            return log.exit(false);
    }
    
    /**
     * Method to compare two {@code TaxonTO}s, to check for complete equality of each
     * attribute. This is because the {@code equals} method of {@code TaxonTO}s is solely
     * based on their ID, not on other attributes.
     * 
     * @param taxonTO1 A {@code TaxonTO} to be compared to {@code taxonTO2}.
     * @param taxonTO2 A {@code TaxonTO} to be compared to {@code taxonTO1}.
     * @return {@code true} if {@code taxonTO1} and {@code taxonTO2} have all attributes
     *         equal.
     */
    public static boolean areTaxonTOsEqual(TaxonTO taxonTO1, TaxonTO taxonTO2) {
        log.entry(taxonTO1, taxonTO2);
        if (TOComparator.areEntityTOsEqual(taxonTO1, taxonTO2) && 
                StringUtils.equals(taxonTO1.getScientificName(), taxonTO2.getScientificName()) &&
                taxonTO1.getLeftBound() == taxonTO2.getLeftBound() && 
                taxonTO1.getRightBound() == taxonTO2.getRightBound() && 
                taxonTO1.getLevel() == taxonTO2.getLevel() && 
                taxonTO1.isLca() == taxonTO2.isLca()) {
            return log.exit(true);
        }
        log.debug("Taxa are not equivalent {}", taxonTO1.getId());
        return log.exit(false);
    }

    /**
     * Method to compare two {@code Collection}s of {@code GOTermTO}s, to check 
     * for complete equality of each attribute of each {@code GOTermTO}. This is 
     * because the {@code equals} method of {@code GOTermTO}s is solely based 
     * on their ID, not on other attributes. Here we check for equality of each 
     * attribute. 
     * 
     * @param c1    A {@code Collection} of {@code GOTermTO}s o be compared to {@code c2}.
     * @param c2    A {@code Collection} of {@code GOTermTO}s o be compared to {@code c1}.
     * @return      {@code true} if {@code c1} and {@code c2} contain the same number 
     *              of {@code GOTermTO}s, and each {@code GOTermTO} of a {@code Collection} 
     *              has an equivalent {@code GOTermTO} in the other {@code Collection}, 
     *              with all attributes equal.
     */
    public static boolean areGOTermTOCollectionsEqual(Collection<GOTermTO> c1, 
            Collection<GOTermTO> c2) {
        log.entry(c1, c2);
        
        if (c1.size() != c2.size()) {
            log.debug("Non matching sizes, {} - {}", c1.size(), c2.size());
            return log.exit(false);
        }
        for (GOTermTO s1: c1) {
            boolean found = false;
            for (GOTermTO s2: c2) {
                log.trace("Comparing {} to {}", s1, s2);
                if ((s1.getId() == null && s2.getId() == null || 
                        s1.getId() != null && s1.getId().equals(s2.getId())) && 
                    (s1.getName() == null && s2.getName() == null || 
                        s1.getName() != null && s1.getName().equals(s2.getName())) && 
                    (s1.getDomain() == null && s2.getDomain() == null || 
                        s1.getDomain() != null && s1.getDomain().equals(s2.getDomain())) && 
                    (s1.getAltIds().equals(s2.getAltIds())) ) {
                    found = true;    
                }
            }
            if (!found) {
                log.debug("No equivalent term found for {}", s1);
                return log.exit(false);
            }      
        }
        return log.exit(true);
    }

    /**
     * Method to compare two {@code Collection}s of {@code GeneTO}s, to check for complete
     * equality of each attribute of each {@code GeneTO} calling {@link #areGeneTOsEqual()}.
     * This is because the {@code equals} method of {@code GeneTO}s is solely based on
     * their ID, not on other attributes.
     * 
     * @param cGeneTO1  A {@code Collection} of {@code GeneTO}s to be compared to
     *                  {@code cGeneTO2}.
     * @param cGeneTO2  A {@code Collection} of {@code GeneTO}s to be compared to
     *                  {@code cGeneTO1}.
     * @return          {@code true} if {@code cGeneTO1} and {@code cGeneTO2} contain the
     *                  same number of {@code GeneTO}s, and each {@code GeneTO} of a 
     *                  {@code Collection} has an equivalent {@code GeneTO} in the other
     *                  {@code Collection}, with all attributes equal.
     * @see #areGeneTOsEqual()
     */
    public static boolean areGeneTOCollectionsEqual(
            List<GeneTO> cGeneTO1, Set<GeneTO> cGeneTO2) {
        log.entry(cGeneTO1, cGeneTO2);

        if (cGeneTO1.size() != cGeneTO2.size()) {
            log.debug("Non matching sizes for collection of GeneTO, {} - {}",
                    cGeneTO1.size(), cGeneTO2.size());
            return log.exit(false);
        }
        for (GeneTO g1: cGeneTO1) {
            boolean found = false;
            for (GeneTO g2: cGeneTO2) {
                if (areGeneTOsEqual(g1, g2)) {
                    found = true;    
                    break;
                }
            }
            if (!found) {
                log.debug("No equivalent gene found for {} of {}", g1.getId(), cGeneTO1);
                return log.exit(false);
            }      
        }
        return log.exit(true);
    }
    
    /**
     * Method to compare two {@code GeneTO}s, to check for complete equality of each
     * attribute. This is because the {@code equals} method of {@code GeneTO}s is solely
     * based on their ID, not on other attributes.
     * 
     * @param geneTO1   A {@code GeneTO} to be compared to {@code geneTO2}.
     * @param geneTO2   A {@code GeneTO} to be compared to {@code geneTO1}.
     * @return          {@code true} if {@code geneTO1} and {@code geneTO2} have all 
     *                  attributes equal.
     */
    public static boolean areGeneTOsEqual(GeneTO geneTO1, GeneTO geneTO2) {
        log.entry(geneTO1, geneTO2);
        if (TOComparator.areEntityTOsEqual(geneTO1, geneTO2) && 
             geneTO1.getSpeciesId() == geneTO2.getSpeciesId() && 
             geneTO1.getGeneBioTypeId() == geneTO2.getGeneBioTypeId() && 
             geneTO1.getOMAParentNodeId() == geneTO2.getOMAParentNodeId() && 
             geneTO1.isEnsemblGene() == geneTO2.isEnsemblGene()) {
            return log.exit(true);
        }
        log.debug("Genes {} and {} are not equivalent", geneTO1, geneTO2);
        return log.exit(false);
    }
    
    /**
     * Method to compare two {@code AnatEntityTO}s, to check for complete equality of each
     * attribute. This is because the {@code equals} method of {@code AnatEntityTO}s is solely
     * based on their ID, not on other attributes.
     * 
     * @param anatEntity1   An {@code AnatEntityTO} to be compared to {@code entity2}.
     * @param anatEntity2   An {@code AnatEntityTO} to be compared to {@code entity1}.
     * @return          {@code true} if {@code entity1} and {@code entity2} have all 
     *                  attributes equal.
     */
    public static boolean areAnatEntityTOsEqual(AnatEntityTO anatEntity1, AnatEntityTO anatEntity2) {
        log.entry(anatEntity1, anatEntity2);
        
        if (TOComparator.areEntityTOsEqual(anatEntity1, anatEntity2) && 
                StringUtils.equals(anatEntity1.getStartStageId(), anatEntity2.getStartStageId()) &&
                StringUtils.equals(anatEntity1.getEndStageId(), anatEntity2.getEndStageId())) {
            return log.exit(true);
        }
        log.debug("Anatomical entities {} and {} are not equivalent", anatEntity1, anatEntity2);
        
        return log.exit(false);
    }
    
    /**
     * Method to compare two {@code EntityTO}s, to check for complete equality of each attribute.
     * This is because the {@code equals} method of {@code EntityTO}s is solely based 
     * on their ID, not on other attributes.
     * 
     * @param entity1   An {@code EntityTO} to be compared to {@code entity2}.
     * @param entity2   An {@code EntityTO} to be compared to {@code entity1}.
     * @return          {@code true} if {@code entity1} and {@code entity2} have 
     *                  all attributes equal.
     */
    private static boolean areEntityTOsEqual(EntityTO entity1, EntityTO entity2) {
        log.entry(entity1, entity2);
        if (StringUtils.equals(entity1.getId(), entity2.getId()) &&
                StringUtils.equals(entity1.getName(), entity2.getName()) &&
                StringUtils.equals(entity1.getDescription(), entity2.getDescription())) {
            return log.exit(true);
        }
        log.debug("Entities {} and {} are not equivalent", entity1, entity2);
        return log.exit(false);
    }
}
