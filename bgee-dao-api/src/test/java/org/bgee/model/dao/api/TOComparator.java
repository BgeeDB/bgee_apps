package org.bgee.model.dao.api;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTO;
import org.bgee.model.dao.api.gene.GeneOntologyDAO.GOTermTO;


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
        if ((geneTO1.getId() == null && geneTO2.getId() == null || 
                geneTO1.getId() != null && geneTO1.getId().equals(geneTO2.getId())) && 
            (geneTO1.getName() == null && geneTO2.getName() == null || 
                    geneTO1.getName() != null && geneTO1.getName().equals(geneTO2.getName())) && 
             geneTO1.getSpeciesId() == geneTO2.getSpeciesId() && 
             geneTO1.getGeneBioTypeId() == geneTO2.getGeneBioTypeId() && 
             geneTO1.getOMAParentNodeId() == geneTO2.getOMAParentNodeId() && 
             geneTO1.isEnsemblGene() == geneTO2.isEnsemblGene()) {
            return log.exit(true);
        }
        log.debug("Genes {} and {} are not equivalent", geneTO1, geneTO2);
        return log.exit(false);
    }


}
