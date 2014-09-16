package org.bgee.model.dao.api;

import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.anatdev.AnatEntityDAO.AnatEntityTO;
import org.bgee.model.dao.api.anatdev.StageDAO.StageTO;
import org.bgee.model.dao.api.anatdev.TaxonConstraintDAO.TaxonConstraintTO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTO;
import org.bgee.model.dao.api.gene.GeneOntologyDAO.GOTermTO;
import org.bgee.model.dao.api.gene.HierarchicalGroupDAO.HierarchicalGroupTO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTO;
import org.bgee.model.dao.api.species.TaxonDAO.TaxonTO;


/**
 * Utility class allowing to compare {@code TransferObject}s. This is because 
 * the {@code equals} method of some {@code TransferObject}s  
 * are based on some attributes only, while for test purpose we want to compare 
 * all of them, so we cannot use the {@code equals} method. 
 * <p>
 * This class is therefore not a unit or integration test class, but is meant 
 * to be used during tests. 
 * <p>
 * Methods of this class are tested in {@link TOComparatorTest}.
 * 
 * @author Valentine Rech de Laval
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class TOComparator {

    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = LogManager.getLogger(TOComparator.class.getName());

    /**
     * Method to compare two {@code TransferObject}s, to check for complete equality of each
     * attribute. This is because the {@code equals} method of some {@code TransferObject}s  
     * are based on some attributes only, while for test purpose we want to compare 
     * all of them, so we cannot use the {@code equals} method.
     * 
     * @param to1   A {@code T} to be compared to {@code to2}.
     * @param to2   A {@code T} to be compared to {@code to1}.
     * @return      {@code true} if {@code to1} and {@code to2} have all 
     *              attributes equal.
     * @param <T>   A {@code TransferObject} type parameter.
     */
    public static <T extends TransferObject> boolean areTOsEqual(T to1, T to2) {
        log.entry(to1, to2);
        //Warning: we should have used a visitor pattern here, but this would represent 
        //too much changes to the TransferObject classes, only for test purposes.
        //So we dispatch to the appropriate areTOsEqual method "manually", 
        //this is ugly but it will do the trick. 
        if (to1 instanceof SpeciesTO) {
            return log.exit(areTOsEqual((SpeciesTO) to1, (SpeciesTO) to2));
        } else if (to1 instanceof TaxonTO) {
            return log.exit(areTOsEqual((TaxonTO) to1, (TaxonTO) to2));
        } else if (to1 instanceof GOTermTO) {
            return log.exit(areTOsEqual((GOTermTO) to1, (GOTermTO) to2));
        } else if (to1 instanceof GeneTO) {
            return log.exit(areTOsEqual((GeneTO) to1, (GeneTO) to2));
        } else if (to1 instanceof AnatEntityTO) {
            return log.exit(areTOsEqual((AnatEntityTO) to1, (AnatEntityTO) to2));
        } else if (to1 instanceof StageTO) {
            return log.exit(areTOsEqual((StageTO) to1, (StageTO) to2));
        } else if (to1 instanceof HierarchicalGroupTO) {
            return log.exit(areTOsEqual((HierarchicalGroupTO) to1, (HierarchicalGroupTO) to2));
        } else if (to1 instanceof TaxonConstraintTO) {
            return log.exit(areTOsEqual((TaxonConstraintTO) to1, (TaxonConstraintTO) to2));
        }
        throw log.throwing(new IllegalArgumentException("There is no comparison method " +
        		"implemented for TransferObject " + to1.getClass() + ", you must implement one"));
    }
    /**
     * Method to compare two {@code Collection}s of {@code T}s, to check 
     * for complete equality of each attribute of each {@code T}. This is because 
     * the {@code equals} method of some {@code TransferObject}s  
     * are based on some attributes only, while for test purpose we want to compare 
     * all of them, so we cannot use the {@code equals} method.
     * 
     * @param c1    A {@code Collection} of {@code T}s o be compared to {@code c2}.
     * @param c2    A {@code Collection} of {@code T}s o be compared to {@code c1}.
     * @return      {@code true} if {@code c1} and {@code c2} contain the same number 
     *              of {@code T}s, and each {@code T} of a {@code Collection} 
     *              has an equivalent {@code T} in the other {@code Collection}, 
     *              with all attributes equal.
     * @param <T>   A {@code TransferObject} type parameter.
     */
    public static <T extends TransferObject> boolean areTOCollectionsEqual(Collection<T> c1, 
            Collection<T> c2) {
        log.entry(c1, c2);
        
        if (c1 == null && c2 == null) {
            return log.exit(true);
        }
        if (c1 == null || c2 == null) {
            return log.exit(false);
        }
        if (c1.size() != c2.size()) {
            return log.exit(false);
        }
        for (T to1: c1) {
            boolean found = false;
            for (T to2: c2) {
                if (areTOsEqual(to1, to2)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                log.trace("No equivalent term found for {}", to1);
                return log.exit(false);
            }      
        }
        return log.exit(true);
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
        return log.exit(false);
    }
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
    private static boolean areTOsEqual(SpeciesTO spTO1, SpeciesTO spTO2) {
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
    private static boolean areTOsEqual(TaxonTO taxonTO1, TaxonTO taxonTO2) {
        log.entry(taxonTO1, taxonTO2);
        if (TOComparator.areEntityTOsEqual(taxonTO1, taxonTO2) && 
                StringUtils.equals(taxonTO1.getScientificName(), taxonTO2.getScientificName()) &&
                taxonTO1.getLeftBound() == taxonTO2.getLeftBound() && 
                taxonTO1.getRightBound() == taxonTO2.getRightBound() && 
                taxonTO1.getLevel() == taxonTO2.getLevel() && 
                taxonTO1.isLca() == taxonTO2.isLca()) {
            return log.exit(true);
        }
        return log.exit(false);
    }
    
    /**
     * Method to compare two {@code GOTermTO}s, to check for complete equality of each
     * attribute. This is because the {@code equals} method of {@code GOTermTO}s is solely
     * based on their ID, not on other attributes.
     * 
     * @param goTermTO1 A {@code GOTermTO} to be compared to {@code goTermTO2}.
     * @param goTermTO2 A {@code GOTermTO} to be compared to {@code goTermTO1}.
     * @return {@code true} if {@code goTermTO1} and {@code goTermTO2} have all attributes
     *         equal.
     */
    private static boolean areTOsEqual(GOTermTO goTermTO1, GOTermTO goTermTO2) {
        log.entry(goTermTO1, goTermTO2);
        if (TOComparator.areEntityTOsEqual(goTermTO1, goTermTO2) && 
                (goTermTO1.getDomain() == null && goTermTO2.getDomain() == null || 
                goTermTO1.getDomain() != null && goTermTO1.getDomain().equals(goTermTO2.getDomain())) && 
                goTermTO1.getAltIds() == null && goTermTO2.getAltIds() == null || 
                goTermTO1.getAltIds() != null && goTermTO1.getAltIds().equals(goTermTO2.getAltIds())) {
            return log.exit(true);
        }
        return log.exit(false);
    }
    
    /**
     * Method to compare two {@code GeneTO}s, to check for complete equality of each
     * attribute. This is because the {@code equals} method of {@code GeneTO}s is solely
     * based on their ID, not on other attributes.
     * 
     * @param geneTO1 A {@code GeneTO} to be compared to {@code geneTO2}.
     * @param geneTO2 A {@code GeneTO} to be compared to {@code geneTO1}.
     * @return {@code true} if {@code geneTO1} and {@code geneTO2} have all attributes
     *         equal.
     */
    private static boolean areTOsEqual(GeneTO geneTO1, GeneTO geneTO2) {
        log.entry(geneTO1, geneTO2);
        if (TOComparator.areEntityTOsEqual(geneTO1, geneTO2) && 
                geneTO1.getSpeciesId() == geneTO2.getSpeciesId() && 
                geneTO1.getGeneBioTypeId() == geneTO2.getGeneBioTypeId() && 
                geneTO1.getOMAParentNodeId() == geneTO2.getOMAParentNodeId() && 
                geneTO1.isEnsemblGene() == geneTO2.isEnsemblGene()) {
            return log.exit(true);
        }
        return log.exit(false);
    }
    
    /**
     * Method to compare two {@code HierarchicalGroupTO}s, to check for complete equality of each
     * attribute. This is because the {@code equals} method of 
     * {@code HierarchicalGroupTO}s is solely based on their ID, not on other attributes.
     * 
     * @param to1 A {@code HierarchicalGroupTO} to be compared to {@code to2}.
     * @param to2 A {@code HierarchicalGroupTO} to be compared to {@code to1}.
     * @return {@code true} if {@code to1} and {@code to2} have all attributes
     *         equal.
     */
    private static boolean areTOsEqual(HierarchicalGroupTO to1, HierarchicalGroupTO to2) {
        log.entry(to1, to2);
        if (TOComparator.areEntityTOsEqual(to1, to2) && 
                StringUtils.equals(to1.getOMAGroupId(), to2.getOMAGroupId()) && 
                to1.getLeftBound() == to2.getLeftBound() && 
                to1.getRightBound() == to2.getRightBound() && 
                to1.getTaxonId() == to2.getTaxonId()) {
            return log.exit(true);
        }
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
    private static boolean areTOsEqual(AnatEntityTO anatEntity1, AnatEntityTO anatEntity2) {
        log.entry(anatEntity1, anatEntity2);
        
        if (TOComparator.areEntityTOsEqual(anatEntity1, anatEntity2) && 
                StringUtils.equals(anatEntity1.getStartStageId(), anatEntity2.getStartStageId()) &&
                StringUtils.equals(anatEntity1.getEndStageId(), anatEntity2.getEndStageId()) && 
                anatEntity1.isNonInformative() == anatEntity2.isNonInformative()) {
            return log.exit(true);
        }
        
        return log.exit(false);
    }

    /**
     * Method to compare two {@code StageTO}s, to check for complete equality of each
     * attribute. This is because the {@code equals} method of {@code StageTO}s is solely
     * based on their ID, not on other attributes.
     * 
     * @param to1   An {@code StageTO} to be compared to {@code to2}.
     * @param to2   An {@code StageTO} to be compared to {@code to1}.
     * @return      {@code true} if {@code to1} and {@code to2} have all 
     *              attributes equal.
     */
    private static boolean areTOsEqual(StageTO to1, StageTO to2) {
        log.entry(to1, to2);
        
        if (TOComparator.areEntityTOsEqual(to1, to2) && 
                to1.isGroupingStage() == to2.isGroupingStage() && 
                to1.isTooGranular() == to2.isTooGranular()) {
            return log.exit(true);
        }
        
        return log.exit(false);
    }

    /**
     * Method to compare two {@code TaxonConstraintTO}s, to check for complete equality of each
     * attribute. 
     * 
     * @param to1   An {@code TaxonConstraintTO} to be compared to {@code to2}.
     * @param to2   An {@code TaxonConstraintTO} to be compared to {@code to1}.
     * @return      {@code true} if {@code to1} and {@code to2} have all 
     *              attributes equal.
     */
    private static boolean areTOsEqual(TaxonConstraintTO to1, TaxonConstraintTO to2) {
        log.entry(to1, to2);
        
        //for now, the equals method of TaxonConstraintTO takes into account 
        //all attributes, so we can use it directly. We still keep the method 
        //areTOsEqual for abstraction purpose.
        return log.exit(to1.equals(to2));
    }
    
}
