package org.bgee.model.dao.api;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.anatdev.AnatEntityDAO.AnatEntityTO;
import org.bgee.model.dao.api.anatdev.StageDAO.StageTO;
import org.bgee.model.dao.api.anatdev.TaxonConstraintDAO.TaxonConstraintTO;
import org.bgee.model.dao.api.anatdev.mapping.RawSimilarityAnnotationDAO.RawSimilarityAnnotationTO;
import org.bgee.model.dao.api.anatdev.mapping.StageGroupingDAO.GroupToStageTO;
import org.bgee.model.dao.api.anatdev.mapping.SummarySimilarityAnnotationDAO.SimAnnotToAnatEntityTO;
import org.bgee.model.dao.api.anatdev.mapping.SummarySimilarityAnnotationDAO.SummarySimilarityAnnotationTO;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.GlobalExpressionToExpressionTO;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO.GlobalNoExpressionToNoExpressionTO;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO.NoExpressionCallTO;
import org.bgee.model.dao.api.file.DownloadFileDAO.DownloadFileTO;
import org.bgee.model.dao.api.file.SpeciesDataGroupDAO.SpeciesDataGroupTO;
import org.bgee.model.dao.api.file.SpeciesDataGroupDAO.SpeciesToDataGroupTO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTO;
import org.bgee.model.dao.api.gene.GeneOntologyDAO.GOTermTO;
import org.bgee.model.dao.api.gene.HierarchicalGroupDAO.HierarchicalGroupTO;
import org.bgee.model.dao.api.gene.HierarchicalGroupDAO.HierarchicalGroupToGeneTO;
import org.bgee.model.dao.api.keyword.KeywordDAO.EntityToKeywordTO;
import org.bgee.model.dao.api.keyword.KeywordDAO.KeywordTO;
import org.bgee.model.dao.api.ontologycommon.CIOStatementDAO.CIOStatementTO;
import org.bgee.model.dao.api.ontologycommon.EvidenceOntologyDAO.ECOTermTO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO;
import org.bgee.model.dao.api.source.SourceDAO.SourceTO;
import org.bgee.model.dao.api.source.SourceToSpeciesDAO.SourceToSpeciesTO;
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
 * @author  Valentine Rech de Laval
 * @author  Frederic Bastian
 * @version Bgee 13, July 2016
 * @since   Bgee 13
 */
public class TOComparator {

    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = LogManager.getLogger(TOComparator.class.getName());

    /**
     * Delegates to {@link #areTOsEqual(T, T, boolean)},
     * with the {@code boolean} argument set to {@code true}.
     * 
     * @param to1   See {@link #areTOsEqual(T, T, boolean)}.
     * @param to2   See {@link #areTOsEqual(T, T, boolean)}.
     * @return      See {@link #areTOsEqual(T, T, boolean)}.
     * @param <T>   A {@code TransferObject} type parameter.
     */
    public static <T extends TransferObject> boolean areTOsEqual(T to1, T to2) {
        log.entry(to1, to2);
        return log.exit(areTOsEqual(to1, to2, true));
    }
    /**
     * Method to compare two {@code TransferObject}s, to check for complete equality of each
     * attribute. This is because the {@code equals} method of some {@code TransferObject}s  
     * are based on some attributes only, while for test purpose we want to compare 
     * all of them, so we cannot use the {@code equals} method.
     * <p>
     * If {@code compareId} is {@code false}, the value returned by the method {@code getId} 
     * of {@code T} will not be used for comparison (most of the time, they are 
     * {@code EntityTO}s, but other types of {@code TransferObject}s also have a {@code getId} 
     * method).
     * 
     * @param to1       A {@code T} to be compared to {@code to2}.
     * @param to2       A {@code T} to be compared to {@code to1}.
     * @param compareId A {@code boolean} defining whether IDs of {@code EntityTO}s should be 
     *                  used for comparison. 
     * @return      {@code true} if {@code to1} and {@code to2} have all requested 
     *              attributes equal.
     * @param <T>   A {@code TransferObject} type parameter.
     */
    //TODO: when NamedEntityTO will be implemented, check for EntityTO instances 
    //to properly be sure that there is a getId() method
    public static <T extends TransferObject> boolean areTOsEqual(T to1, T to2, boolean compareId) {
        log.entry(to1, to2);
        //Warning: we should have used a visitor pattern here, but this would represent 
        //too much changes to the TransferObject classes, only for test purposes.
        //So we dispatch to the appropriate areTOsEqual method "manually", 
        //this is ugly but it will do the trick. 
        if (!to1.getClass().equals(to2.getClass())) {
            return log.exit(false);
        }
        if (to1 instanceof SpeciesTO) {
            return log.exit(areTOsEqual((SpeciesTO) to1, (SpeciesTO) to2, compareId));
        } else if (to1 instanceof TaxonTO) {
            return log.exit(areTOsEqual((TaxonTO) to1, (TaxonTO) to2, compareId));
        } else if (to1 instanceof GOTermTO) {
            return log.exit(areTOsEqual((GOTermTO) to1, (GOTermTO) to2, compareId));
        } else if (to1 instanceof GeneTO) {
            return log.exit(areTOsEqual((GeneTO) to1, (GeneTO) to2, compareId));
        } else if (to1 instanceof AnatEntityTO) {
            return log.exit(areTOsEqual((AnatEntityTO) to1, (AnatEntityTO) to2, compareId));
        } else if (to1 instanceof StageTO) {
            return log.exit(areTOsEqual((StageTO) to1, (StageTO) to2, compareId));
        } else if (to1 instanceof HierarchicalGroupTO) {
            return log.exit(areTOsEqual((HierarchicalGroupTO) to1, (HierarchicalGroupTO) to2, 
                    compareId));
        } else if (to1 instanceof HierarchicalGroupToGeneTO) {
            return log.exit(areTOsEqual(
                    (HierarchicalGroupToGeneTO) to1, (HierarchicalGroupToGeneTO) to2));
        } else if (to1 instanceof TaxonConstraintTO) {
            return log.exit(areTOsEqual((TaxonConstraintTO) to1, (TaxonConstraintTO) to2));
        } else if (to1 instanceof RelationTO) {
            return log.exit(areTOsEqual((RelationTO) to1, (RelationTO) to2, compareId));
        } else if (to1 instanceof ExpressionCallTO) {
            return log.exit(areTOsEqual((ExpressionCallTO) to1, (ExpressionCallTO) to2, 
                    compareId));
        } else if (to1 instanceof NoExpressionCallTO) {
            return log.exit(areTOsEqual((NoExpressionCallTO) to1, (NoExpressionCallTO) to2, 
                    compareId));
        } else if (to1 instanceof GlobalExpressionToExpressionTO) {
            return log.exit(areTOsEqual((
                    GlobalExpressionToExpressionTO) to1, (GlobalExpressionToExpressionTO) to2));
        } else if (to1 instanceof GlobalNoExpressionToNoExpressionTO) {
            return log.exit(areTOsEqual((
                    GlobalNoExpressionToNoExpressionTO) to1, 
                    (GlobalNoExpressionToNoExpressionTO) to2));
        } else if (to1 instanceof DiffExpressionCallTO) {
            return log.exit(areTOsEqual((DiffExpressionCallTO) to1, (DiffExpressionCallTO) to2, 
                    compareId));
        } else if (to1 instanceof CIOStatementTO) {
            return log.exit(areTOsEqual((CIOStatementTO) to1, (CIOStatementTO) to2, compareId));
        } else if (to1 instanceof ECOTermTO) {
            return log.exit(areTOsEqual((ECOTermTO) to1, (ECOTermTO) to2, compareId));
        } else if (to1 instanceof RawSimilarityAnnotationTO) {
            return log.exit(areTOsEqual(
                    (RawSimilarityAnnotationTO) to1, 
                    (RawSimilarityAnnotationTO) to2));
        } else if (to1 instanceof SummarySimilarityAnnotationTO) {
            return log.exit(areTOsEqual(
                    (SummarySimilarityAnnotationTO) to1, 
                    (SummarySimilarityAnnotationTO) to2));
        } else if (to1 instanceof SimAnnotToAnatEntityTO) {
            return log.exit(areTOsEqual((SimAnnotToAnatEntityTO) to1, (SimAnnotToAnatEntityTO) to2));
        } else if (to1 instanceof GroupToStageTO) {
            return log.exit(areTOsEqual((GroupToStageTO) to1, (GroupToStageTO) to2));
        } else if (to1 instanceof KeywordTO) {
            return log.exit(areTOsEqual((KeywordTO) to1, (KeywordTO) to2, compareId));
        } else if (to1 instanceof EntityToKeywordTO) {
            return log.exit(areTOsEqual((EntityToKeywordTO) to1, (EntityToKeywordTO) to2));
        } else if (to1 instanceof DownloadFileTO) {
            return log.exit(areTOsEqual( (DownloadFileTO)to1, (DownloadFileTO) to2, compareId));
        } else if (to2 instanceof SpeciesDataGroupTO) {
            return log.exit(areTOsEqual((SpeciesDataGroupTO) to1,(SpeciesDataGroupTO) to2, compareId));
        } else if (to2 instanceof SpeciesToDataGroupTO) {
            return log.exit(areTOsEqual((SpeciesToDataGroupTO) to1,(SpeciesToDataGroupTO) to2));
        } else if (to2 instanceof SourceTO) {
            return log.exit(areTOsEqual((SourceTO) to1, (SourceTO) to2, compareId));
        } else if (to2 instanceof SourceToSpeciesTO) {
            return log.exit(areTOsEqual((SourceToSpeciesTO) to1,(SourceToSpeciesTO) to2));
        }

        throw log.throwing(new IllegalArgumentException("There is no comparison method " +
                "implemented for TransferObject " + to1.getClass() + ", you must implement one"));
    }
    /**
     * Delegates to {@link #areTOCollectionsEqual(Collection, Collection, boolean)}, 
     * with the {@code boolean} argument set to {@code true}.
     * 
     * @param c1    See {@link #areTOCollectionsEqual(Collection, Collection, boolean)}.
     * @param c2    See {@link #areTOCollectionsEqual(Collection, Collection, boolean)}.
     * @return      See {@link #areTOCollectionsEqual(Collection, Collection, boolean)}.
     * @param <T>   A {@code TransferObject} type parameter.
     */
    public static <T extends TransferObject> boolean areTOCollectionsEqual(Collection<T> c1, 
            Collection<T> c2) {
        log.entry(c1, c2);
        return log.exit(areTOCollectionsEqual(c1, c2, true));
    }
    /**
     * Method to compare two {@code Collection}s of {@code T}s, to check 
     * for complete equality of each attribute of each {@code T}. This is because 
     * the {@code equals} method of some {@code TransferObject}s  
     * are based on some attributes only, while for test purpose we want to compare 
     * all of them, so we cannot use the {@code equals} method.
     * <p>
     * If {@code compareId} is {@code false}, the value returned by the method {@code getId} 
     * of {@code T} will not be used for comparison.
     * <p>
     * Note that this method takes into account duplicate elements, but does not take 
     * into account order of elements, in case the provided {@code Collection}s are {@code List}s.
     * 
     * @param c1        A {@code Collection} of {@code T}s o be compared to {@code c2}.
     * @param c2        A {@code Collection} of {@code T}s o be compared to {@code c1}.
     * @param compareId A {@code boolean} defining whether IDs of {@code EntityTO}s should be 
     *                  used for comparisons. 
     * @return      {@code true} if {@code c1} and {@code c2} contain the same number 
     *              of {@code T}s, and each {@code T} of a {@code Collection} 
     *              has an equivalent {@code T} in the other {@code Collection}, 
     *              with all attributes equal.
     * @param <T>   A {@code TransferObject} type parameter.
     */
    //TODO: when NamedEntityTO will be implemented, check for EntityTO instances 
    //to properly be sure that there is a getId() method
    public static <T extends TransferObject> boolean areTOCollectionsEqual(Collection<T> c1, 
            Collection<T> c2, boolean compareId) {
        log.entry(c1, c2);
        if (c1 == null && c2 == null) {
            return log.exit(true);
        }
        if (c1 == null || c2 == null) {
            return log.exit(false);
        }
        if (c1.size() != c2.size()) {
            log.trace("Collections not equal, size first collection: {} - size second collection: {}", 
                    c1.size(), c2.size());
            return log.exit(false);
        }
        //to make sure we have the same number of each element. 
        //for instance, we could have a list {Element1, Element1, Element2}, 
        //and another list {Element1, Element2, Element2}, they would be seen as equal 
        //without this check. 
        //here we assume that the equals method returns true when areTOsEqual returns true
        Map<T, Integer> countElementInFirstCollection = new HashMap<T, Integer>();
        Map<T, Integer> countElementMatchInSecondCollection = new HashMap<T, Integer>();
        for (T to1: c1) {
            for (T to1bis: c1) {
                if (areTOsEqual(to1, to1bis, compareId)) {
                    Integer count1 = countElementInFirstCollection.get(to1);
                    if (count1 == null) {
                        count1 = 0;
                    }
                    count1++;
                    countElementInFirstCollection.put(to1, count1);
                }
            }
            
            boolean found = false;
            for (T to2: c2) {
                if (areTOsEqual(to1, to2, compareId)) {
                    found = true;
                    //here we store to1 as key, it's not an error, it's to be able 
                    //to compare to countElementInFirstCollection
                    //TODO: refactor
                    Integer count2 = countElementMatchInSecondCollection.get(to1);
                    if (count2 == null) {
                        count2 = 0;
                    }
                    count2++;
                    countElementMatchInSecondCollection.put(to1, count2);
                }
            }
            if (!found) {
                log.trace("No equivalent TransferObject {} found for {}", to1.getClass(), to1);
                return log.exit(false);
            }      
        }
        log.trace("Count elements in first collection: {}", countElementInFirstCollection);
        log.trace("Count matching elements in second collection: {}", 
                countElementMatchInSecondCollection);
        return log.exit(countElementInFirstCollection.equals(countElementMatchInSecondCollection));
    }
    
    /**
     * Method to compare two {@code EntityTO}s, to check for complete equality of each attribute.
     * This is because the {@code equals} method of {@code EntityTO}s is solely based 
     * on their ID, not on other attributes.
     * <p>
     * If {@code compareId} is {@code false}, the value returned by the method {@code getId} 
     * will not be used for comparison.
     * 
     * @param entity1   An {@code EntityTO} to be compared to {@code entity2}.
     * @param entity2   An {@code EntityTO} to be compared to {@code entity1}.
     * @param compareId A {@code boolean} defining whether IDs of {@code EntityTO}s should be 
     *                  used for comparisons. 
     * @return          {@code true} if {@code entity1} and {@code entity2} have 
     *                  all attributes equal.
     */
    private static boolean areEntityTOsEqual(EntityTO entity1, EntityTO entity2, 
            boolean compareId) {
        log.entry(entity1, entity2, compareId);
        if ((!compareId || StringUtils.equals(entity1.getId(), entity2.getId())) &&
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
     * <p>
     * If {@code compareId} is {@code false}, the value returned by the method {@code getId} 
     * will not be used for comparison.
     * 
     * @param spTO1     A {@code SpeciesTO} to be compared to {@code spTO2}.
     * @param spTO2     A {@code SpeciesTO} to be compared to {@code spTO1}.
     * @param compareId A {@code boolean} defining whether IDs of {@code EntityTO}s should be 
     *                  used for comparisons. 
     * @return          {@code true} if {@code spTO1} and {@code spTO2} have all 
     *                  attributes equal.
     */
    private static boolean areTOsEqual(SpeciesTO spTO1, SpeciesTO spTO2, 
            boolean compareId) {
        log.entry(spTO1, spTO2, compareId);
        if (TOComparator.areEntityTOsEqual(spTO1, spTO2, compareId) && 
                StringUtils.equals(spTO1.getGenus(), spTO2.getGenus()) &&
                StringUtils.equals(spTO1.getSpeciesName(), spTO2.getSpeciesName()) &&
                StringUtils.equals(spTO1.getParentTaxonId(), spTO2.getParentTaxonId()) &&
                StringUtils.equals(spTO1.getGenomeFilePath(), spTO2.getGenomeFilePath()) &&
                StringUtils.equals(spTO1.getGenomeVersion(), spTO2.getGenomeVersion()) &&
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
     * <p>
     * If {@code compareId} is {@code false}, the value returned by the method {@code getId} 
     * will not be used for comparison.
     * 
     * @param taxonTO1 A {@code TaxonTO} to be compared to {@code taxonTO2}.
     * @param taxonTO2 A {@code TaxonTO} to be compared to {@code taxonTO1}.
     * @param compareId A {@code boolean} defining whether IDs of {@code EntityTO}s should be 
     *                  used for comparisons. 
     * @return {@code true} if {@code taxonTO1} and {@code taxonTO2} have all attributes
     *         equal.
     */
    private static boolean areTOsEqual(TaxonTO taxonTO1, TaxonTO taxonTO2, 
            boolean compareId) {
        log.entry(taxonTO1, taxonTO2, compareId);
        if (TOComparator.areEntityTOsEqual(taxonTO1, taxonTO2, compareId) && 
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
     * <p>
     * If {@code compareId} is {@code false}, the value returned by the method {@code getId} 
     * will not be used for comparison.
     * 
     * @param goTermTO1 A {@code GOTermTO} to be compared to {@code goTermTO2}.
     * @param goTermTO2 A {@code GOTermTO} to be compared to {@code goTermTO1}.
     * @param compareId A {@code boolean} defining whether IDs of {@code EntityTO}s should be 
     *                  used for comparisons. 
     * @return {@code true} if {@code goTermTO1} and {@code goTermTO2} have all attributes
     *         equal.
     */
    private static boolean areTOsEqual(GOTermTO goTermTO1, GOTermTO goTermTO2, 
            boolean compareId) {
        log.entry(goTermTO1, goTermTO2, compareId);
        if (TOComparator.areEntityTOsEqual(goTermTO1, goTermTO2, compareId) && 
                (goTermTO1.getDomain() == null && goTermTO2.getDomain() == null || 
                goTermTO1.getDomain() != null && goTermTO1.getDomain().equals(goTermTO2.getDomain())) && 
                (goTermTO1.getAltIds() == null && goTermTO2.getAltIds() == null || 
                goTermTO1.getAltIds() != null && goTermTO1.getAltIds().equals(goTermTO2.getAltIds()))) {
            return log.exit(true);
        }
        return log.exit(false);
    }

    /**
     * Method to compare two {@code GeneTO}s, to check for complete equality of each
     * attribute. This is because the {@code equals} method of {@code GeneTO}s is solely
     * based on their ID, not on other attributes.
     * <p>
     * If {@code compareId} is {@code false}, the value returned by the method {@code getId} 
     * will not be used for comparison.
     * 
     * @param geneTO1 A {@code GeneTO} to be compared to {@code geneTO2}.
     * @param geneTO2 A {@code GeneTO} to be compared to {@code geneTO1}.
     * @param compareId A {@code boolean} defining whether IDs of {@code EntityTO}s should be 
     *                  used for comparisons. 
     * @return {@code true} if {@code geneTO1} and {@code geneTO2} have all attributes
     *         equal.
     */
    private static boolean areTOsEqual(GeneTO geneTO1, GeneTO geneTO2, 
            boolean compareId) {
        log.entry(geneTO1, geneTO2, compareId);
        if (TOComparator.areEntityTOsEqual(geneTO1, geneTO2, compareId) && 
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
     * <p>
     * If {@code compareId} is {@code false}, the value returned by the method {@code getId} 
     * will not be used for comparison.
     * 
     * @param to1 A {@code HierarchicalGroupTO} to be compared to {@code to2}.
     * @param to2 A {@code HierarchicalGroupTO} to be compared to {@code to1}.
     * @param compareId A {@code boolean} defining whether IDs of {@code EntityTO}s should be 
     *                  used for comparisons. 
     * @return {@code true} if {@code to1} and {@code to2} have all attributes
     *         equal.
     */
    private static boolean areTOsEqual(HierarchicalGroupTO to1, HierarchicalGroupTO to2, 
            boolean compareId) {
        log.entry(to1, to2, compareId);
        if (TOComparator.areEntityTOsEqual(to1, to2, compareId) && 
                StringUtils.equals(to1.getOMAGroupId(), to2.getOMAGroupId()) && 
                to1.getLeftBound() == to2.getLeftBound() && 
                to1.getRightBound() == to2.getRightBound() && 
                to1.getTaxonId() == to2.getTaxonId()) {
            return log.exit(true);
        }
        return log.exit(false);
    }
    
    /**
     * Method to compare two {@code HierarchicalGroupToGeneTO}s, to check for complete equality 
     * of each attribute. This is because the {@code equals} method of 
     * {@code HierarchicalGroupToGeneTO}s is solely based on their ID, not on other attributes.
     * 
     * @param to1   A {@code HierarchicalGroupToGeneTO} to be compared to {@code to2}.
     * @param to2   A {@code HierarchicalGroupToGeneTO} to be compared to {@code to1}.
     * @return      {@code true} if {@code to1} and {@code to2} have all attributes equal.
     */
    private static boolean areTOsEqual(HierarchicalGroupToGeneTO to1, HierarchicalGroupToGeneTO to2) {
        log.entry(to1, to2);

        if (StringUtils.equals(to1.getGeneId(), to2.getGeneId()) && 
                StringUtils.equals(to1.getGroupId(), to2.getGroupId())) {
            return log.exit(true);
        }
        return log.exit(false);
    }

    /**
     * Method to safely compare two {@code BigDecimal} instances
     * @param b0 A {@code BigDecimal}
     * @param b1 A {@code BigDecimal}
     * @return true if both BigDecimal are null, or their value are equals using {@link BigDecimal#compareTo(Object)}
     */
    protected static boolean areBigDecimalEquals(BigDecimal b0, BigDecimal b1) {
        return b0 == b1 || b0 != null && b1 != null && b0.compareTo(b1) == 0;
    }

    /**
     * Method to compare two {@code AnatEntityTO}s, to check for complete equality of each
     * attribute. This is because the {@code equals} method of {@code AnatEntityTO}s is solely
     * based on their ID, not on other attributes.
     * <p>
     * If {@code compareId} is {@code false}, the value returned by the method {@code getId} 
     * will not be used for comparison.
     * 
     * @param anatEntity1   An {@code AnatEntityTO} to be compared to {@code entity2}.
     * @param anatEntity2   An {@code AnatEntityTO} to be compared to {@code entity1}.
     * @param compareId A {@code boolean} defining whether IDs of {@code EntityTO}s should be 
     *                  used for comparisons. 
     * @return          {@code true} if {@code entity1} and {@code entity2} have all 
     *                  attributes equal.
     */
    private static boolean areTOsEqual(AnatEntityTO anatEntity1, AnatEntityTO anatEntity2, 
            boolean compareId) {
        log.entry(anatEntity1, anatEntity2, compareId);
        if (TOComparator.areEntityTOsEqual(anatEntity1, anatEntity2, compareId) && 
                StringUtils.equals(anatEntity1.getStartStageId(), anatEntity2.getStartStageId()) &&
                StringUtils.equals(anatEntity1.getEndStageId(), anatEntity2.getEndStageId()) && 
                anatEntity1.isNonInformative() == anatEntity2.isNonInformative()) {
            return log.exit(true);
        }
        return log.exit(false);
    }

    private static boolean areTOsEqual(DownloadFileTO to1, DownloadFileTO to2, boolean compareId){
        log.entry(to1, to2, compareId);
        return log.exit(TOComparator.areEntityTOsEqual(to1, to2, compareId) && 
                to1.getCategory() == to2.getCategory()
                && StringUtils.equals(to1.getPath(), to2.getPath())
                //not possible to simply use to1.getSize() == to2.getSize() for Long value > 127, 
                //see http://stackoverflow.com/a/20542511/1768736
                && (to1.getSize() == null && to2.getSize() == null || 
                    to1.getSize().equals(to2.getSize()))
                && StringUtils.equals(to1.getSpeciesDataGroupId(), to2.getSpeciesDataGroupId())
        );
    }

    /**
     * Method to compare two {@code SpeciesDataGroupTO}s, to check for complete equality of each
     * attribute. This is because the {@code equals} method of {@code SpeciesDataGroupTO}s is solely
     * based on their ID, not on other attributes.
     * <p>
     * If {@code compareId} is {@code false}, the value returned by the method {@code getId} 
     * will not be used for comparison.
     * 
     * @param to1       An {@code SpeciesDataGroupTO} to be compared to {@code to2}.
     * @param to2       An {@code SpeciesDataGroupTO} to be compared to {@code to1}.
     * @param compareId A {@code boolean} defining whether IDs of {@code EntityTO}s should be 
     *                  used for comparisons. 
     * @return          {@code true} if {@code to1} and {@code to2} have all attributes equal.
     */
    private static boolean areTOsEqual(SpeciesDataGroupTO to1, SpeciesDataGroupTO to2, 
            boolean compareId){
        log.entry(to1, to2, compareId);
        return log.exit(TOComparator.areEntityTOsEqual(to1, to2, compareId) && 
                (to1.getPreferredOrder() == null && to2.getPreferredOrder() == null || 
                 to1.getPreferredOrder() != null && to1.getPreferredOrder().equals(to2.getPreferredOrder())));
    }
    
    /**
     * Method to compare two {@code SpeciesToDataGroupTO}s, to check for complete equality of each
     * attribute.
     * 
     * @param to1   An {@code SpeciesToDataGroupTO} to be compared to {@code to2}.
     * @param to2   An {@code SpeciesToDataGroupTO} to be compared to {@code to1}.
     * @return      {@code true} if {@code to1} and {@code to2} have all attributes equal.
     */
    private static boolean areTOsEqual(SpeciesToDataGroupTO to1, SpeciesToDataGroupTO to2){
        log.entry(to1, to2);
        
        if (StringUtils.equals(to1.getGroupId(), to2.getGroupId()) && 
                StringUtils.equals(to1.getSpeciesId(), to2.getSpeciesId())) {
            return log.exit(true);
        }
        return log.exit(false);
    }
    
    /**
     * Method to compare two {@code StageTO}s, to check for complete equality of each
     * attribute. This is because the {@code equals} method of {@code StageTO}s is solely
     * based on their ID, not on other attributes.
     * <p>
     * If {@code compareId} is {@code false}, the value returned by the method {@code getId} 
     * will not be used for comparison.
     * 
     * @param to1   An {@code StageTO} to be compared to {@code to2}.
     * @param to2   An {@code StageTO} to be compared to {@code to1}.
     * @param compareId A {@code boolean} defining whether IDs of {@code EntityTO}s should be 
     *                  used for comparisons. 
     * @return      {@code true} if {@code to1} and {@code to2} have all 
     *              attributes equal.
     */
    private static boolean areTOsEqual(StageTO to1, StageTO to2, 
            boolean compareId) {
        log.entry(to1, to2, compareId);
        if (TOComparator.areEntityTOsEqual(to1, to2, compareId) && 
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

    /**
     * Method to compare two {@code RelationTO}s, to check for complete equality of each
     * attribute. 
     * <p>
     * If {@code compareId} is {@code false}, the value returned by the method {@code getId} 
     * will not be used for comparison.
     * 
     * @param to1   An {@code RelationTO} to be compared to {@code to2}.
     * @param to2   An {@code RelationTO} to be compared to {@code to1}.
     * @param compareId A {@code boolean} defining whether IDs of {@code EntityTO}s should be 
     *                  used for comparisons. 
     * @return      {@code true} if {@code to1} and {@code to2} have all 
     *              attributes equal.
     */
    private static boolean areTOsEqual(RelationTO to1, RelationTO to2, 
            boolean compareId) {
        log.entry(to1, to2, compareId);

        if ((!compareId || StringUtils.equals(to1.getId(), to2.getId())) && 
                StringUtils.equals(to1.getSourceId(), to2.getSourceId()) && 
                StringUtils.equals(to1.getTargetId(), to2.getTargetId()) && 
                to1.getRelationStatus() == to2.getRelationStatus() && 
                to1.getRelationType() == to2.getRelationType()) {
            return log.exit(true);
        }
        return log.exit(false);
    }

    /**
     * Method to compare two {@code CallTO}s, to check for complete equality of each
     * attribute. 
     * <p>
     * If {@code compareId} is {@code false}, the value returned by the method {@code getId} 
     * will not be used for comparison.
     * 
     * @param to1   An {@code CallTO} to be compared to {@code to2}.
     * @param to2   An {@code CallTO} to be compared to {@code to1}.
     * @param compareId A {@code boolean} defining whether IDs of {@code EntityTO}s should be 
     *                  used for comparisons. 
     * @return      {@code true} if {@code to1} and {@code to2} have all 
     *              attributes equal.
     */
    private static boolean areCallTOsEqual(CallTO<?> to1, CallTO<?> to2, 
            boolean compareId) {
        log.entry(to1, to2, compareId);
        if ((!compareId || StringUtils.equals(to1.getId(), to2.getId())) &&
                StringUtils.equals(to1.getGeneId(), to2.getGeneId()) &&
                StringUtils.equals(to1.getStageId(), to2.getStageId()) &&
                StringUtils.equals(to1.getAnatEntityId(), to2.getAnatEntityId()) &&
                to1.getAffymetrixData() == to2.getAffymetrixData() &&
                to1.getESTData() == to2.getESTData() &&
                to1.getInSituData() == to2.getInSituData() &&
                to1.getRelaxedInSituData() == to2.getRelaxedInSituData() &&
                to1.getRNASeqData() == to2.getRNASeqData()) {
            return log.exit(true);
        }
        return log.exit(false);
    }

    /**
     * Method to compare two {@code ExpressionCallTO}s, to check for complete equality of each
     * attribute. 
     * <p>
     * If {@code compareId} is {@code false}, the value returned by the method {@code getId} 
     * will not be used for comparison.
     * 
     * @param to1   An {@code ExpressionCallTO} to be compared to {@code to2}.
     * @param to2   An {@code ExpressionCallTO} to be compared to {@code to1}.
     * @param compareId A {@code boolean} defining whether IDs of {@code EntityTO}s should be 
     *                  used for comparisons. 
     * @return      {@code true} if {@code to1} and {@code to2} have all 
     *              attributes equal.
     */
    private static boolean areTOsEqual(ExpressionCallTO to1, ExpressionCallTO to2, 
            boolean compareId) {
        log.entry(to1, to2, compareId);
        if (TOComparator.areCallTOsEqual(to1, to2, compareId) && 
                to1.isIncludeSubstructures() == to2.isIncludeSubstructures() && 
                to1.isIncludeSubStages() == to2.isIncludeSubStages() &&
                to1.getAnatOriginOfLine() == to2.getAnatOriginOfLine() &&
                to1.getStageOriginOfLine() == to2.getStageOriginOfLine() &&
                to1.isObservedData() == to2.isObservedData() &&
                areBigDecimalEquals(to1.getGlobalMeanRank(), to2.getGlobalMeanRank()) &&
                areBigDecimalEquals(to1.getAffymetrixMeanRank(), to2.getAffymetrixMeanRank()) &&
                areBigDecimalEquals(to1.getESTMeanRank(), to2.getESTMeanRank()) &&
                areBigDecimalEquals(to1.getInSituMeanRank(), to2.getInSituMeanRank()) &&
                areBigDecimalEquals(to1.getRNASeqMeanRank(), to2.getRNASeqMeanRank())) {
            return log.exit(true);
        }
        return log.exit(false);
    }
    
    /**
     * Method to compare two {@code NoExpressionCallTO}s, to check for complete equality of each
     * attribute. 
     * <p>
     * If {@code compareId} is {@code false}, the value returned by the method {@code getId} 
     * will not be used for comparison.
     * 
     * @param to1   An {@code NoExpressionCallTO} to be compared to {@code to2}.
     * @param to2   An {@code NoExpressionCallTO} to be compared to {@code to1}.
     * @param compareId A {@code boolean} defining whether IDs of {@code EntityTO}s should be 
     *                  used for comparisons. 
     * @return      {@code true} if {@code to1} and {@code to2} have all 
     *              attributes equal.
     */
    private static boolean areTOsEqual(NoExpressionCallTO to1, NoExpressionCallTO to2, 
            boolean compareId) {
        log.entry(to1, to2);
        if (TOComparator.areCallTOsEqual(to1, to2, compareId) && 
                to1.isIncludeParentStructures() == to2.isIncludeParentStructures() && 
                to1.getOriginOfLine() == to2.getOriginOfLine()) {
            return log.exit(true);
        }
        return log.exit(false);
    }

    /**
     * Method to compare two {@code GlobalExpressionToExpressionTO}s, to check for complete 
     * equality of each attribute. 
     * 
     * @param to1   A {@code GlobalExpressionToExpressionTO} to be compared to {@code to2}.
     * @param to2   A {@code GlobalExpressionToExpressionTO} to be compared to {@code to1}.
     * @return      {@code true} if {@code to1} and {@code to2} have all 
     *              attributes equal.
     */
    private static boolean areTOsEqual(GlobalExpressionToExpressionTO to1, 
            GlobalExpressionToExpressionTO to2) {
        log.entry(to1, to2);
        if (to1.getExpressionId() == to2.getExpressionId() && 
                to1.getGlobalExpressionId() == to2.getGlobalExpressionId()) {
            return log.exit(true);
        }
        return log.exit(false);
    }

    /**
     * Method to compare two {@code GlobalNoExpressionToNoExpressionTO}s, to check for complete 
     * equality of each attribute. 
     * 
     * @param to1   A {@code GlobalNoExpressionToNoExpressionTO} to be compared to {@code to2}.
     * @param to2   A {@code GlobalNoExpressionToNoExpressionTO} to be compared to {@code to1}.
     * @return      {@code true} if {@code to1} and {@code to2} have all 
     *              attributes equal.
     */
    private static boolean areTOsEqual(GlobalNoExpressionToNoExpressionTO to1, 
            GlobalNoExpressionToNoExpressionTO to2) {
        log.entry(to1, to2);
        if (to1.getNoExpressionId() == to2.getNoExpressionId() && 
                to1.getGlobalNoExpressionId() == to2.getGlobalNoExpressionId()) {
            return log.exit(true);
        }
        return log.exit(false);
    }
    
    /**
     * Method to compare two {@code DiffExpressionCallTO}s, to check for complete equality of each
     * attribute. 
     * <p>
     * If {@code compareId} is {@code false}, the value returned by the method {@code getId} 
     * will not be used for comparison.
     * 
     * @param to1       A {@code DiffExpressionCallTO} to be compared to {@code to2}.
     * @param to2       A {@code DiffExpressionCallTO} to be compared to {@code to1}.
     * @param compareId A {@code boolean} defining whether IDs of {@code EntityTO}s should be 
     *                  used for comparisons. 
     * @return          {@code true} if {@code to1} and {@code to2} have all attributes equal.
     */
    private static boolean areTOsEqual(DiffExpressionCallTO to1, DiffExpressionCallTO to2, 
            boolean compareId) {
        log.entry(to1, to2);
        if (TOComparator.areCallTOsEqual(to1, to2, compareId) && 
                to1.getComparisonFactor() == to2.getComparisonFactor() &&
                to1.getDiffExprCallTypeAffymetrix() == to2.getDiffExprCallTypeAffymetrix() &&
                TOComparator.areNearlyEqualFloat(
                        to1.getBestPValueAffymetrix(), to2.getBestPValueAffymetrix()) &&
                to1.getConsistentDEACountAffymetrix() == to2.getConsistentDEACountAffymetrix() &&
                to1.getInconsistentDEACountAffymetrix() == to2.getInconsistentDEACountAffymetrix() &&
                to1.getDiffExprCallTypeRNASeq() == to2.getDiffExprCallTypeRNASeq() &&
                TOComparator.areNearlyEqualFloat(
                        to1.getBestPValueRNASeq(), to2.getBestPValueRNASeq()) &&
                to1.getConsistentDEACountRNASeq() == to2.getConsistentDEACountRNASeq() &&
                to1.getInconsistentDEACountRNASeq() == to2.getInconsistentDEACountRNASeq()) {
            return log.exit(true);
        }
        return log.exit(false);
    }
    
    /**
     * Method to compare two {@code CIOStatementTO}s, to check for complete equality of each
     * attribute. 
     * <p>
     * If {@code compareId} is {@code false}, the value returned by the method {@code getId} 
     * will not be used for comparison.
     * 
     * @param to1       A {@code CIOStatementTO} to be compared to {@code to2}.
     * @param to2       A {@code CIOStatementTO} to be compared to {@code to1}.
     * @param compareId A {@code boolean} defining whether IDs of {@code EntityTO}s should be 
     *                  used for comparisons. 
     * @return          {@code true} if {@code to1} and {@code to2} have all attributes equal.
     */
    private static boolean areTOsEqual(CIOStatementTO to1, CIOStatementTO to2, boolean compareId) {
        log.entry(to1, to2);
        if (TOComparator.areEntityTOsEqual(to1, to2, compareId) && 
                to1.getConfidenceLevel() == to2.getConfidenceLevel() &&
                to1.getEvidenceConcordance() == to2.getEvidenceConcordance() &&
                to1.getEvidenceTypeConcordance() == to2.getEvidenceTypeConcordance()) {
            return log.exit(true);
        }
        return log.exit(false);
    }

    /**
     * Method to compare two {@code ECOTermTO}s, to check for complete equality of each
     * attribute. 
     * <p>
     * If {@code compareId} is {@code false}, the value returned by the method {@code getId} 
     * will not be used for comparison.
     * 
     * @param to1       A {@code ECOTermTO} to be compared to {@code to2}.
     * @param to2       A {@code ECOTermTO} to be compared to {@code to1}.
     * @param compareId A {@code boolean} defining whether IDs of {@code EntityTO}s should be 
     *                  used for comparisons. 
     * @return          {@code true} if {@code to1} and {@code to2} have all attributes equal.
     */
    private static boolean areTOsEqual(ECOTermTO to1, ECOTermTO to2, 
            boolean compareId) {
        log.entry(to1, to2, compareId);
        return log.exit(TOComparator.areEntityTOsEqual(to1, to2, compareId));
    }

    /**
     * Method to compare two {@code RawSimilarityAnnotationTO}s, to check for complete 
     * equality of each attribute. 
     * 
     * @param to1   A {@code RawSimilarityAnnotationTO} to be compared to {@code to2}.
     * @param to2   A {@code RawSimilarityAnnotationTO} to be compared to {@code to1}.
     * @return      {@code true} if {@code to1} and {@code to2} have all attributes equal.
     */
    private static boolean areTOsEqual(RawSimilarityAnnotationTO to1, RawSimilarityAnnotationTO to2) {
        log.entry(to1, to2);
        
        if (StringUtils.equals(to1.getSummarySimilarityAnnotationId(), to2.getSummarySimilarityAnnotationId()) &&
            to1.isNegated() == to2.isNegated() &&
            StringUtils.equals(to1.getECOId(), to2.getECOId()) &&
            StringUtils.equals(to1.getCIOId(), to2.getCIOId()) &&
            StringUtils.equals(to1.getReferenceId(), to2.getReferenceId()) &&
            StringUtils.equals(to1.getReferenceTitle(), to2.getReferenceTitle()) &&
            StringUtils.equals(to1.getSupportingText(), to2.getSupportingText()) &&
            StringUtils.equals(to1.getAssignedBy(), to2.getAssignedBy()) &&
            StringUtils.equals(to1.getCurator(), to2.getCurator()) &&
            (to1.getAnnotationDate() == to2.getAnnotationDate() ||
                (to1.getAnnotationDate() != null && 
                to1.getAnnotationDate().equals(to2.getAnnotationDate())))) {
            return log.exit(true);
        }
        return log.exit(false);
    }

    /**
     * Method to compare two {@code SummarySimilarityAnnotationTO}s, to check for complete 
     * equality of each attribute. 
     * 
     * @param to1   A {@code SummarySimilarityAnnotationTO} to be compared to {@code to2}.
     * @param to2   A {@code SummarySimilarityAnnotationTO} to be compared to {@code to1}.
     * @return      {@code true} if {@code to1} and {@code to2} have all attributes equal.
     */
    private static boolean areTOsEqual(SummarySimilarityAnnotationTO to1, 
            SummarySimilarityAnnotationTO to2) {
        log.entry(to1, to2);
        if (StringUtils.equals(to1.getId(), to2.getId()) &&
            StringUtils.equals(to1.getTaxonId(), to2.getTaxonId()) &&
            to1.isNegated() == to2.isNegated() &&
            StringUtils.equals(to1.getCIOId(), to2.getCIOId())) {
            return log.exit(true);
        }
        return log.exit(false);
    }
    
    /**
     * Method to compare two {@code SimAnnotToAnatEntityTO}s, to check for complete 
     * equality of each attribute. 
     * 
     * @param to1   A {@code SimAnnotToAnatEntityTO} to be compared to {@code to2}.
     * @param to2   A {@code SimAnnotToAnatEntityTO} to be compared to {@code to1}.
     * @return      {@code true} if {@code to1} and {@code to2} have all attributes equal.
     */
    private static boolean areTOsEqual(SimAnnotToAnatEntityTO to1, SimAnnotToAnatEntityTO to2) {
        log.entry(to1, to2);
        if (StringUtils.equals(
                to1.getSummarySimilarityAnnotationId(), to2.getSummarySimilarityAnnotationId()) &&
            StringUtils.equals(to1.getAnatEntityId(), to2.getAnatEntityId())) {
            return log.exit(true);
        }
        return log.exit(false);
    }
    
    /**
     * Method to compare two {@code GroupToStageTO}s, to check for complete equality of each
     * attribute. This is because the {@code equals} method of {@code GroupToStageTO}s is solely
     * based on their ID, not on other attributes.
     * 
     * @param to1   An {@code GroupToStageTO} to be compared to {@code to2}.
     * @param to2   An {@code GroupToStageTO} to be compared to {@code to1}.
     * @param compareId A {@code boolean} defining whether IDs of {@code EntityTO}s should be 
     *                  used for comparisons. 
     * @return      {@code true} if {@code to1} and {@code to2} have all 
     *              attributes equal.
     */
    private static boolean areTOsEqual(GroupToStageTO to1, GroupToStageTO to2) {
        log.entry(to1, to2);
        if (StringUtils.equals(to1.getGroupId(), to2.getGroupId()) &&
                StringUtils.equals(to1.getStageId(), to2.getStageId())) {
            return log.exit(true);
        }
        return log.exit(false);
    }

    /**
     * Method to compare two {@code KeywordTO}s, to check for complete 
     * equality of each attribute. 
     * 
     * @param to1       A {@code KeywordTO} to be compared to {@code to2}.
     * @param to2       A {@code KeywordTO} to be compared to {@code to1}.
     * @param compareId A {@code boolean} defining whether IDs of {@code EntityTO}s should be 
     *                  used for comparisons. 
     * @return      {@code true} if {@code to1} and {@code to2} have all attributes equal.
     */
    private static boolean areTOsEqual(KeywordTO to1, KeywordTO to2, boolean compareId) {
        log.entry(to1, to2, compareId);
        return log.exit(TOComparator.areEntityTOsEqual(to1, to2, compareId));
    }
    /**
     * Method to compare two {@code EntityToKeywordTO}s, to check for complete 
     * equality of each attribute. 
     * 
     * @param to1       A {@code EntityToKeywordTO} to be compared to {@code to2}.
     * @param to2       A {@code EntityToKeywordTO} to be compared to {@code to1}.
     * @return      {@code true} if {@code to1} and {@code to2} have all attributes equal.
     */
    private static boolean areTOsEqual(EntityToKeywordTO to1, EntityToKeywordTO to2) {
        log.entry(to1, to2);
        if (StringUtils.equals(to1.getEntityId(), to2.getEntityId()) &&
                StringUtils.equals(to1.getKeywordId(), to2.getKeywordId())) {
            return log.exit(true);
        }
        return log.exit(false);
    }

    /**
     * Method to compare two {@code SourceTO}s, to check for complete equality of each attribute. 
     * 
     * @param to1       A {@code SourceTO} to be compared to {@code to2}.
     * @param to2       A {@code SourceTO} to be compared to {@code to1}.
     * @param compareId A {@code boolean} defining whether IDs of {@code EntityTO}s should be 
     *                  used for comparisons. 
     * @return          {@code true} if {@code to1} and {@code to2} have all attributes equal.
     */
    private static boolean areTOsEqual(SourceTO to1, SourceTO to2, boolean compareId) {
        log.entry(to1, to2);
                
        if (TOComparator.areEntityTOsEqual(to1, to2, compareId) && 
                StringUtils.equals(to1.getXRefUrl(), to2.getXRefUrl()) &&
                StringUtils.equals(to1.getExperimentUrl(), to2.getExperimentUrl()) &&
                StringUtils.equals(to1.getEvidenceUrl(), to2.getEvidenceUrl()) &&
                StringUtils.equals(to1.getBaseUrl(), to2.getBaseUrl()) &&
                (to1.getReleaseDate() == to2.getReleaseDate() ||
                    (to1.getReleaseDate() != null && to1.getReleaseDate().equals(to2.getReleaseDate()))) &&
                StringUtils.equals(to1.getReleaseVersion(), to2.getReleaseVersion()) &&
                to1.isToDisplay() == to2.isToDisplay() &&
                to1.getSourceCategory() == to2.getSourceCategory() &&
                (to1.getDisplayOrder() == to2.getDisplayOrder() ||
                    (to1.getDisplayOrder() != null && to1.getDisplayOrder().equals(to2.getDisplayOrder())))) {
            return log.exit(true);
        }
        return log.exit(false);
    }

    /**
     * Method to compare two {@code SourceToSpeciesTO}s, to check for complete 
     * equality of each attribute. 
     * 
     * @param to1       A {@code SourceToSpeciesTO} to be compared to {@code to2}.
     * @param to2       A {@code SourceToSpeciesTO} to be compared to {@code to1}.
     * @return      {@code true} if {@code to1} and {@code to2} have all attributes equal.
     */
    private static boolean areTOsEqual(SourceToSpeciesTO to1, SourceToSpeciesTO to2) {
        log.entry(to1, to2);
        if (StringUtils.equals(to1.getDataSourceId(), to2.getDataSourceId()) &&
                StringUtils.equals(to1.getSpeciesId(), to2.getSpeciesId()) &&
                to1.getDataType() == to2.getDataType() &&
                to1.getInfoType() == to2.getInfoType()) {
            return log.exit(true);
        }
        return log.exit(false);
    }

    /**
     * Method to compare floating-point values using an epsilon. 
     *
     * @param f1    A {@code Float} to be compared to {@code f2}.
     * @param f2    A {@code Float} to be compared to {@code f1}.
     * @return      {@code true} if {@code f1} and {@code f2} are nearly equals.
     */
    private static boolean areNearlyEqualFloat(Float f1, Float f2) {
        log.entry(f1, f2); 
        double epsilon = 1e-11;

        if ((f1 == null && f2 == null) || (f1 != null && Math.abs(f1 - f2) < epsilon)) {
            return log.exit(true);            
        }
        return log.exit(false);            
    }
}
