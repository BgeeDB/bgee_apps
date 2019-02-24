package org.bgee.model.dao.api;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
import org.bgee.model.dao.api.expressiondata.BaseConditionTO;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO;
import org.bgee.model.dao.api.expressiondata.ConditionDAO.ConditionTO;
import org.bgee.model.dao.api.expressiondata.ConditionDAO.GlobalConditionMaxRankTO;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.ExperimentExpressionDAO.ExperimentExpressionTO;
import org.bgee.model.dao.api.expressiondata.GlobalExpressionCallDAO.GlobalExpressionCallDataTO;
import org.bgee.model.dao.api.expressiondata.GlobalExpressionCallDAO.GlobalExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.RawExpressionCallDAO.RawExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataCallSourceDAO.CallSourceDataTO;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataCallSourceDAO.CallSourceTO;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataConditionDAO.RawDataConditionTO;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataExperimentDAO.ExperimentTO;
import org.bgee.model.dao.api.expressiondata.rawdata.est.ESTDAO.ESTTO;
import org.bgee.model.dao.api.expressiondata.rawdata.est.ESTLibraryDAO.ESTLibraryTO;
import org.bgee.model.dao.api.expressiondata.rawdata.insitu.InSituEvidenceDAO.InSituEvidenceTO;
import org.bgee.model.dao.api.expressiondata.rawdata.insitu.InSituExperimentDAO.InSituExperimentTO;
import org.bgee.model.dao.api.expressiondata.rawdata.insitu.InSituSpotDAO.InSituSpotTO;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixChipDAO.AffymetrixChipTO;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixProbesetDAO.AffymetrixProbesetTO;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.MicroarrayExperimentDAO.MicroarrayExperimentTO;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqExperimentDAO.RNASeqExperimentTO;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqLibraryDAO.RNASeqLibraryTO;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqResultDAO.RNASeqResultTO;
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
 * @version Bgee 14, Feb. 2018
 * @since   Bgee 13, July 2014
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
    //to properly be sure that there is a getId() method
    public static <T extends TransferObject> boolean areTOsEqual(T to1, T to2, boolean compareId) {
        log.entry(to1, to2);

        if (to1 == to2) {
            return log.exit(true);
        }
        if(to1== null || to2 == null) {
            return log.exit(false);
        }

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
            return log.exit(areTOsEqual((TaxonConstraintTO<?>) to1, (TaxonConstraintTO<?>) to2));
        } else if (to1 instanceof RelationTO) {
            return log.exit(areTOsEqual((RelationTO<?>) to1, (RelationTO<?>) to2, compareId));
        } else if (to1 instanceof ConditionTO) {
            return log.exit(areTOsEqual((ConditionTO) to1, (ConditionTO) to2, compareId));
        } else if (to1 instanceof RawDataConditionTO) {
            return log.exit(areTOsEqual((RawDataConditionTO) to1, (RawDataConditionTO) to2, compareId));
        } else if (to1 instanceof GlobalConditionMaxRankTO) {
            return log.exit(areTOsEqual((GlobalConditionMaxRankTO) to1, (GlobalConditionMaxRankTO) to2));
        } else if (to1 instanceof RawExpressionCallTO) {
            return log.exit(areTOsEqual((RawExpressionCallTO) to1, (RawExpressionCallTO) to2, 
                    compareId));
        } else if (to1 instanceof GlobalExpressionCallTO) {
            return log.exit(areTOsEqual((GlobalExpressionCallTO) to1, (GlobalExpressionCallTO) to2, 
                    compareId));
        } else if (to1 instanceof GlobalExpressionCallDataTO) {
            return log.exit(areTOsEqual((GlobalExpressionCallDataTO) to1, (GlobalExpressionCallDataTO) to2));
        } else if (to1 instanceof ExperimentExpressionTO) {
            return log.exit(areTOsEqual((ExperimentExpressionTO) to1, (ExperimentExpressionTO) to2));
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
            return log.exit(areTOsEqual((EntityToKeywordTO<?>) to1, (EntityToKeywordTO<?>) to2));
        } else if (to1 instanceof DownloadFileTO) {
            return log.exit(areTOsEqual( (DownloadFileTO)to1, (DownloadFileTO) to2, compareId));
        } else if (to2 instanceof SpeciesDataGroupTO) {
            return log.exit(areTOsEqual((SpeciesDataGroupTO) to1, (SpeciesDataGroupTO) to2, compareId));
        } else if (to2 instanceof SpeciesToDataGroupTO) {
            return log.exit(areTOsEqual((SpeciesToDataGroupTO) to1, (SpeciesToDataGroupTO) to2));
        } else if (to2 instanceof SourceTO) {
            return log.exit(areTOsEqual((SourceTO) to1, (SourceTO) to2, compareId));
        } else if (to2 instanceof SourceToSpeciesTO) {
            return log.exit(areTOsEqual((SourceToSpeciesTO) to1, (SourceToSpeciesTO) to2));
        } else if (to2 instanceof AffymetrixProbesetTO) {
            return log.exit(areTOsEqual((AffymetrixProbesetTO) to1, (AffymetrixProbesetTO) to2, compareId));
        } else if (to2 instanceof AffymetrixChipTO) {
            return log.exit(areTOsEqual((AffymetrixChipTO) to1, (AffymetrixChipTO) to2, compareId));
        } else if (to2 instanceof MicroarrayExperimentTO) {
            return log.exit(areTOsEqual((MicroarrayExperimentTO) to1, (MicroarrayExperimentTO) to2, compareId));
        } else if (to2 instanceof RNASeqResultTO) {
            return log.exit(areTOsEqual((RNASeqResultTO) to1, (RNASeqResultTO) to2));
        } else if (to2 instanceof RNASeqLibraryTO) {
            return log.exit(areTOsEqual((RNASeqLibraryTO) to1, (RNASeqLibraryTO) to2, compareId));
        } else if (to2 instanceof RNASeqExperimentTO) {
            return log.exit(areTOsEqual((RNASeqExperimentTO) to1, (RNASeqExperimentTO) to2, compareId));
        } else if (to2 instanceof ESTLibraryTO) {
            return log.exit(areTOsEqual((ESTLibraryTO) to1, (ESTLibraryTO) to2, compareId));
        } else if (to2 instanceof ESTTO) {
            return log.exit(areTOsEqual((ESTTO) to1, (ESTTO) to2, compareId));
        } else if (to2 instanceof InSituExperimentTO) {
            return log.exit(areTOsEqual((InSituExperimentTO) to1, (InSituExperimentTO) to2, compareId));
        } else if (to2 instanceof InSituEvidenceTO) {
            return log.exit(areTOsEqual((InSituEvidenceTO) to1, (InSituEvidenceTO) to2, compareId));
        } else if (to2 instanceof InSituSpotTO) {
            return log.exit(areTOsEqual((InSituSpotTO) to1, (InSituSpotTO) to2, compareId));
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
    private static boolean areEntityTOsEqual(EntityTO<?> entity1, EntityTO<?> entity2, 
            boolean compareId) {
        log.entry(entity1, entity2, compareId);
        return log.exit(!compareId || Objects.equals(entity1.getId(), entity2.getId()));
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
    private static boolean areEntityTOsEqual(NamedEntityTO<?> entity1, NamedEntityTO<?> entity2, 
            boolean compareId) {
        log.entry(entity1, entity2, compareId);
        if (areEntityTOsEqual((EntityTO<?>) entity1, (EntityTO<?>) entity2, compareId) &&
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
                (spTO1.getDisplayOrder() == null && spTO2.getDisplayOrder() == null || 
                    spTO1.getDisplayOrder() != null && 
                    spTO1.getDisplayOrder().equals(spTO2.getDisplayOrder())) && 
                Objects.equals(spTO1.getDisplayOrder(), spTO2.getDisplayOrder()) &&
                Objects.equals(spTO1.getParentTaxonId(), spTO2.getParentTaxonId()) &&
                StringUtils.equals(spTO1.getGenomeFilePath(), spTO2.getGenomeFilePath()) &&
                StringUtils.equals(spTO1.getGenomeVersion(), spTO2.getGenomeVersion()) &&
                Objects.equals(spTO1.getDataSourceId(), spTO2.getDataSourceId()) &&
                Objects.equals(spTO1.getGenomeSpeciesId(), spTO2.getGenomeSpeciesId())) {
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
                Objects.equals(taxonTO1.getLeftBound(), taxonTO2.getLeftBound()) && 
                Objects.equals(taxonTO1.getRightBound(), taxonTO2.getRightBound()) && 
                Objects.equals(taxonTO1.getLevel(), taxonTO2.getLevel()) && 
                Objects.equals(taxonTO1.isLca(), taxonTO2.isLca())) {
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
                Objects.equals(goTermTO1.getDomain(), goTermTO2.getDomain()) && 
                Objects.equals(goTermTO1.getAltIds(), goTermTO2.getAltIds())) {
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
                Objects.equals(geneTO1.getGeneId(), geneTO2.getGeneId()) && 
                Objects.equals(geneTO1.getSpeciesId(), geneTO2.getSpeciesId()) && 
                Objects.equals(geneTO1.getGeneBioTypeId(), geneTO2.getGeneBioTypeId()) && 
                Objects.equals(geneTO1.getOMAParentNodeId(), geneTO2.getOMAParentNodeId()) && 
                Objects.equals(geneTO1.isEnsemblGene(), geneTO2.isEnsemblGene()) &&
                Objects.equals(geneTO1.getGeneMappedToGeneIdCount(), geneTO2.getGeneMappedToGeneIdCount())) {
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
                Objects.equals(to1.getLeftBound(), to2.getLeftBound()) && 
                Objects.equals(to1.getRightBound(), to2.getRightBound()) && 
                Objects.equals(to1.getTaxonId(), to2.getTaxonId())) {
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

        if (Objects.equals(to1.getBgeeGeneId(), to2.getBgeeGeneId()) && 
            Objects.equals(to1.getNodeId(), to2.getNodeId())) {
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
                Objects.equals(anatEntity1.isNonInformative(), anatEntity2.isNonInformative())) {
            return log.exit(true);
        }
        return log.exit(false);
    }

    private static boolean areTOsEqual(DownloadFileTO to1, DownloadFileTO to2, boolean compareId){
        log.entry(to1, to2, compareId);
        return log.exit(TOComparator.areEntityTOsEqual(to1, to2, compareId) && 
                Objects.equals(to1.getCategory(), to2.getCategory())
                && StringUtils.equals(to1.getPath(), to2.getPath())
                //not possible to simply use to1.getSize() == to2.getSize() for Long value > 127, 
                //see http://stackoverflow.com/a/20542511/1768736
                && Objects.equals(to1.getSize(), to2.getSize())
                && Objects.equals(to1.getSpeciesDataGroupId(), to2.getSpeciesDataGroupId())
                && Objects.equals(to1.getConditionParameters(), to2.getConditionParameters())
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
                Objects.equals(to1.getPreferredOrder(), to2.getPreferredOrder()));
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
        
        if (Objects.equals(to1.getGroupId(), to2.getGroupId()) && 
                Objects.equals(to1.getSpeciesId(), to2.getSpeciesId())) {
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
                Objects.equals(to1.isGroupingStage(), to2.isGroupingStage()) && 
                Objects.equals(to1.isTooGranular(), to2.isTooGranular())) {
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
    private static boolean areTOsEqual(TaxonConstraintTO<?> to1, TaxonConstraintTO<?> to2) {
        log.entry(to1, to2);

        return log.exit(Objects.equals(to1.getEntityId(), to2.getEntityId()) && 
                Objects.equals(to1.getSpeciesId(), to2.getSpeciesId()));
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
    private static boolean areTOsEqual(RelationTO<?> to1, RelationTO<?> to2, 
            boolean compareId) {
        log.entry(to1, to2, compareId);

        if ((!compareId || Objects.equals(to1.getId(), to2.getId())) && 
                Objects.equals(to1.getSourceId(), to2.getSourceId()) && 
                Objects.equals(to1.getTargetId(), to2.getTargetId()) && 
                Objects.equals(to1.getRelationStatus(), to2.getRelationStatus()) && 
                Objects.equals(to1.getRelationType(), to2.getRelationType())) {
            return log.exit(true);
        }
        return log.exit(false);
    }
    /**
     * Method to compare two {@code BaseConditionTO}s, to check for complete equality of each
     * attribute.
     * <p>
     * If {@code compareId} is {@code false}, the value returned by the method {@code getId}
     * will not be used for comparison.
     *
     * @param to1       A {@code BaseConditionTO} to be compared to {@code to2}.
     * @param to2       A {@code BaseConditionTO} to be compared to {@code to1}.
     * @param compareId A {@code boolean} defining whether IDs of {@code EntityTO}s should be
     *                  used for comparisons.
     * @return          {@code true} if {@code to1} and {@code to2} have all attributes equal.
     */
    private static boolean areTOsEqual(BaseConditionTO to1, BaseConditionTO to2, boolean compareId) {
        log.entry(to1, to2, compareId);

        if (TOComparator.areEntityTOsEqual(to1, to2, compareId) &&
                StringUtils.equals(to1.getAnatEntityId(), to2.getAnatEntityId()) &&
                StringUtils.equals(to1.getStageId(), to2.getStageId()) &&
                Objects.equals(to1.getSex(), to2.getSex()) &&
                StringUtils.equals(to1.getStrain(), to2.getStrain()) &&
                Objects.equals(to1.getSpeciesId(), to2.getSpeciesId())) {
            return log.exit(true);
        }
        return log.exit(false);
    }
    /**
     * Method to compare two {@code ConditionTO}s, to check for complete equality of each
     * attribute. 
     * <p>
     * If {@code compareId} is {@code false}, the value returned by the method {@code getId} 
     * will not be used for comparison.
     * 
     * @param to1       A {@code ConditionTO} to be compared to {@code to2}.
     * @param to2       A {@code ConditionTO} to be compared to {@code to1}.
     * @param compareId A {@code boolean} defining whether IDs of {@code EntityTO}s should be 
     *                  used for comparisons. 
     * @return          {@code true} if {@code to1} and {@code to2} have all attributes equal.
     */
    private static boolean areTOsEqual(ConditionTO to1, ConditionTO to2, boolean compareId) {
        log.entry(to1, to2, compareId);

        if (areTOsEqual((BaseConditionTO) to1, (BaseConditionTO) to2, compareId)) {
            return log.exit(true);
        }
        return log.exit(false);
    }
    /**
     * Method to compare two {@code RawDataConditionTO}s, to check for complete equality of each
     * attribute.
     * <p>
     * If {@code compareId} is {@code false}, the value returned by the method {@code getId}
     * will not be used for comparison.
     *
     * @param to1       A {@code RawDataConditionTO} to be compared to {@code to2}.
     * @param to2       A {@code RawDataConditionTO} to be compared to {@code to1}.
     * @param compareId A {@code boolean} defining whether IDs of {@code EntityTO}s should be 
     *                  used for comparisons.
     * @return          {@code true} if {@code to1} and {@code to2} have all attributes equal.
     */
    private static boolean areTOsEqual(RawDataConditionTO to1, RawDataConditionTO to2, boolean compareId) {
        log.entry(to1, to2, compareId);

        if (areTOsEqual((BaseConditionTO) to1, (BaseConditionTO) to2, compareId) &&
                Objects.equals(to1.getSexInferred(), to2.getSexInferred()) &&
                Objects.equals(to1.getExprMappedConditionId(), to2.getExprMappedConditionId())) {
            return log.exit(true);
        }
        return log.exit(false);
    }
    /**
     * Method to compare two {@code GlobalConditionMaxRankTO}s, to check for complete equality of each
     * attribute.
     * 
     * @param to1       An {@code ConditionTO} to be compared to {@code to2}.
     * @param to2       An {@code ConditionTO} to be compared to {@code to1}.
     * @return          {@code true} if {@code to1} and {@code to2} have all attributes equal.
     */
    //TODO: unit test
    private static boolean areTOsEqual(GlobalConditionMaxRankTO to1, GlobalConditionMaxRankTO to2) {
        log.entry(to1, to2);

        if (Objects.equals(to1.getConditionId(), to2.getConditionId()) && 
                Objects.equals(to1.getDataType(), to2.getDataType()) &&
                areBigDecimalEquals(to1.getMaxRank(), to2.getMaxRank()) && 
                areBigDecimalEquals(to1.getGlobalMaxRank(), to2.getGlobalMaxRank())) {
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
        if (areEntityTOsEqual(to1, to2, compareId) &&
                Objects.equals(to1.getBgeeGeneId(), to2.getBgeeGeneId()) &&
                Objects.equals(to1.getConditionId(), to2.getConditionId()) &&
                Objects.equals(to1.getAffymetrixData(), to2.getAffymetrixData()) &&
                Objects.equals(to1.getESTData(), to2.getESTData()) &&
                Objects.equals(to1.getInSituData(), to2.getInSituData()) &&
                Objects.equals(to1.getRelaxedInSituData(), to2.getRelaxedInSituData()) &&
                Objects.equals(to1.getRNASeqData(), to2.getRNASeqData())) {
            return log.exit(true);
        }
        return log.exit(false);
    }

    /**
     * Method to compare two {@code RawExpressionCallTO}s, to check for complete equality of each
     * attribute. 
     * <p>
     * If {@code compareId} is {@code false}, the value returned by the method {@code getId} 
     * will not be used for comparison.
     * 
     * @param to1   An {@code RawExpressionCallTO} to be compared to {@code to2}.
     * @param to2   An {@code RawExpressionCallTO} to be compared to {@code to1}.
     * @param compareId A {@code boolean} defining whether IDs of {@code EntityTO}s should be 
     *                  used for comparisons. 
     * @return      {@code true} if {@code to1} and {@code to2} have all 
     *              attributes equal.
     */
    //TODO: unit test
    private static boolean areTOsEqual(RawExpressionCallTO to1, RawExpressionCallTO to2, 
            boolean compareId) {
        log.entry(to1, to2, compareId);
        if (areEntityTOsEqual(to1, to2, compareId) && 
                Objects.equals(to1.getBgeeGeneId(), to2.getBgeeGeneId()) && 
                Objects.equals(to1.getConditionId(), to2.getConditionId())) {
            return log.exit(true);
        }
        return log.exit(false);
    }

    /**
     * Method to compare two {@code GlobalExpressionCallTO}s, to check for complete equality of each
     * attribute. 
     * <p>
     * If {@code compareId} is {@code false}, the value returned by the method {@code getId} 
     * will not be used for comparison.
     * 
     * @param to1   An {@code GlobalExpressionCallTO} to be compared to {@code to2}.
     * @param to2   An {@code GlobalExpressionCallTO} to be compared to {@code to1}.
     * @param compareId A {@code boolean} defining whether IDs of {@code EntityTO}s should be 
     *                  used for comparisons. 
     * @return      {@code true} if {@code to1} and {@code to2} have all 
     *              attributes equal.
     */
    //TODO: unit test
    private static boolean areTOsEqual(GlobalExpressionCallTO to1, GlobalExpressionCallTO to2, 
            boolean compareId) {
        log.entry(to1, to2, compareId);
        if (areTOsEqual((RawExpressionCallTO) to1, (RawExpressionCallTO) to2, compareId) &&
                areBigDecimalEquals(to1.getMeanRank(), to2.getMeanRank()) &&

                //GlobalExpressionCallDataTOs do not implement hashCode/equals
                (to1.getCallDataTOs() == null && to2.getCallDataTOs() == null || 
                to1.getCallDataTOs() != null && to2.getCallDataTOs() != null &&
                to1.getCallDataTOs().stream()
                    .allMatch(c1 -> to2.getCallDataTOs().stream()
                            .anyMatch(c2 -> areTOsEqual(c1, c2))))) {
            return log.exit(true);
        }
        return log.exit(false);
    }

    /**
     * Method to compare two {@code GlobalExpressionCallDataTO}s, to check for complete 
     * equality of each attribute. 
     * 
     * @param to1   A {@code GlobalExpressionCallDataTO} to be compared to {@code to2}.
     * @param to2   A {@code GlobalExpressionCallDataTO} to be compared to {@code to1}.
     * @return      {@code true} if {@code to1} and {@code to2} have all 
     *              attributes equal.
     */
    //TODO: unit test
    private static boolean areTOsEqual(GlobalExpressionCallDataTO to1, GlobalExpressionCallDataTO to2) {
        log.entry(to1, to2);
        if (Objects.equals(to1.getDataType(), to2.getDataType()) &&
                Objects.equals(to1.getDataPropagation(), to2.getDataPropagation()) &&
                Objects.equals(to1.isConditionObservedData(), to2.isConditionObservedData()) &&
                Objects.equals(to1.getExperimentCounts(), to2.getExperimentCounts()) &&
                Objects.equals(to1.getPropagatedCount(), to2.getPropagatedCount()) &&
                areBigDecimalEquals(to1.getRank(), to2.getRank()) &&
                areBigDecimalEquals(to1.getRankNorm(), to2.getRankNorm()) &&
                areBigDecimalEquals(to1.getWeightForMeanRank(), to2.getWeightForMeanRank())) {
            return log.exit(true);
        }
        return log.exit(false);
    }

    /**
     * Method to compare two {@code ExperimentExpressionTO}s, to check for complete equality of each
     * attribute. 
     * <p>
     * If {@code compareId} is {@code false}, the value returned by the method {@code getId} 
     * will not be used for comparison.
     * 
     * @param to1       An {@code ExperimentExpressionTO} to be compared to {@code to2}.
     * @param to2       An {@code ExperimentExpressionTO} to be compared to {@code to1}.
     * @param compareId A {@code boolean} defining whether IDs of {@code EntityTO}s should be 
     *                  used for comparisons. 
     * @return      {@code true} if {@code to1} and {@code to2} have all attributes equal.
     */
    private static boolean areTOsEqual(ExperimentExpressionTO to1, ExperimentExpressionTO to2) {
        log.entry(to1, to2);
        if (Objects.equals(to1.getExpressionId(), to2.getExpressionId()) && 
                Objects.equals(to1.getExperimentId(), to2.getExperimentId()) && 
                Objects.equals(to1.getPresentHighCount(), to2.getPresentHighCount()) && 
                Objects.equals(to1.getPresentLowCount(), to2.getPresentLowCount()) && 
                Objects.equals(to1.getAbsentHighCount(), to2.getAbsentHighCount()) &&
                Objects.equals(to1.getAbsentLowCount(), to2.getAbsentLowCount()) &&
                Objects.equals(to1.getCallDirection(), to2.getCallDirection()) &&
                Objects.equals(to1.getCallQuality(), to2.getCallQuality())) {
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
                Objects.equals(to1.getComparisonFactor(), to2.getComparisonFactor()) &&
                Objects.equals(to1.getDiffExprCallTypeAffymetrix(), to2.getDiffExprCallTypeAffymetrix()) &&
                TOComparator.areNearlyEqualFloat(
                        to1.getBestPValueAffymetrix(), to2.getBestPValueAffymetrix()) &&
                Objects.equals(to1.getConsistentDEACountAffymetrix(), to2.getConsistentDEACountAffymetrix()) &&
                Objects.equals(to1.getInconsistentDEACountAffymetrix(), to2.getInconsistentDEACountAffymetrix()) &&
                Objects.equals(to1.getDiffExprCallTypeRNASeq(), to2.getDiffExprCallTypeRNASeq()) &&
                TOComparator.areNearlyEqualFloat(
                        to1.getBestPValueRNASeq(), to2.getBestPValueRNASeq()) &&
                Objects.equals(to1.getConsistentDEACountRNASeq(), to2.getConsistentDEACountRNASeq()) &&
                Objects.equals(to1.getInconsistentDEACountRNASeq(), to2.getInconsistentDEACountRNASeq())) {
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
                Objects.equals(to1.getConfidenceLevel(), to2.getConfidenceLevel()) &&
                Objects.equals(to1.getEvidenceConcordance(), to2.getEvidenceConcordance()) &&
                Objects.equals(to1.getEvidenceTypeConcordance(), to2.getEvidenceTypeConcordance())) {
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
            Objects.equals(to1.isNegated(), to2.isNegated()) &&
            StringUtils.equals(to1.getECOId(), to2.getECOId()) &&
            StringUtils.equals(to1.getCIOId(), to2.getCIOId()) &&
            StringUtils.equals(to1.getReferenceId(), to2.getReferenceId()) &&
            StringUtils.equals(to1.getReferenceTitle(), to2.getReferenceTitle()) &&
            StringUtils.equals(to1.getSupportingText(), to2.getSupportingText()) &&
            StringUtils.equals(to1.getAssignedBy(), to2.getAssignedBy()) &&
            StringUtils.equals(to1.getCurator(), to2.getCurator()) &&
            Objects.equals(to1.getAnnotationDate(), to2.getAnnotationDate())) {
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
            Objects.equals(to1.getTaxonId(), to2.getTaxonId()) &&
            Objects.equals(to1.isNegated(), to2.isNegated()) &&
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
    private static boolean areTOsEqual(EntityToKeywordTO<?> to1, EntityToKeywordTO<?> to2) {
        log.entry(to1, to2);
        if (Objects.equals(to1.getEntityId(), to2.getEntityId()) &&
                Objects.equals(to1.getKeywordId(), to2.getKeywordId())) {
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
                Objects.equals(to1.getReleaseDate(), to2.getReleaseDate()) &&
                StringUtils.equals(to1.getReleaseVersion(), to2.getReleaseVersion()) &&
                Objects.equals(to1.isToDisplay(), to2.isToDisplay()) &&
                Objects.equals(to1.getSourceCategory(), to2.getSourceCategory()) &&
                Objects.equals(to1.getDisplayOrder(), to2.getDisplayOrder())) {
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
        if (Objects.equals(to1.getDataSourceId(), to2.getDataSourceId()) &&
                Objects.equals(to1.getSpeciesId(), to2.getSpeciesId()) &&
                Objects.equals(to1.getDataType(), to2.getDataType()) &&
                Objects.equals(to1.getInfoType(), to2.getInfoType())) {
            return log.exit(true);
        }
        return log.exit(false);
    }

    /**
     * Method to compare two {@code AffymetrixProbesetTO}s, to check for complete
     * equality of each attribute.
     *
     * @param to1       A {@code AffymetrixProbesetTO} to be compared to {@code to2}.
     * @param to2       A {@code AffymetrixProbesetTO} to be compared to {@code to1}.
     * @return          {@code true} if {@code to1} and {@code to2} have all attributes equal.
     */
    private static boolean areTOsEqual(AffymetrixProbesetTO to1, AffymetrixProbesetTO to2, boolean compareId) {
        log.entry(to1, to2, compareId);
        if (areEntityTOsEqual(to1, to2, compareId) &&
                areBigDecimalEquals(to1.getNormalizedSignalIntensity(), to2.getNormalizedSignalIntensity()) &&
                areBigDecimalEquals(to1.getRank(), to2.getRank()) &&
                areCallSourceTOsEqual(to1, to2)) {
            return log.exit(true);
        }
        return log.exit(false);
    }
    /**
     * Method to compare two {@code RNASeqResultTO}s, to check for complete
     * equality of each attribute.
     *
     * @param to1       A {@code RNASeqResultTO} to be compared to {@code to2}.
     * @param to2       A {@code RNASeqResultTO} to be compared to {@code to1}.
     * @return          {@code true} if {@code to1} and {@code to2} have all attributes equal.
     */
    private static boolean areTOsEqual(RNASeqResultTO to1, RNASeqResultTO to2) {
        log.entry(to1, to2);
        if (areCallSourceTOsEqual(to1, to2) &&
                areBigDecimalEquals(to1.getTpm(), to2.getTpm()) &&
                areBigDecimalEquals(to1.getFpkm(), to2.getFpkm()) &&
                areBigDecimalEquals(to1.getReadCount(), to2.getReadCount()) &&
                areBigDecimalEquals(to1.getRank(), to2.getRank())) {
            return log.exit(true);
        }
        return log.exit(false);
    }
    /**
     * Method to compare two {@code InSituSpotTO}s, to check for complete
     * equality of each attribute.
     *
     * @param to1       A {@code InSituSpotTO} to be compared to {@code to2}.
     * @param to2       A {@code InSituSpotTO} to be compared to {@code to1}.
     * @return          {@code true} if {@code to1} and {@code to2} have all attributes equal.
     */
    private static boolean areTOsEqual(InSituSpotTO to1, InSituSpotTO to2, boolean compareId) {
        log.entry(to1, to2, compareId);
        if (areEntityTOsEqual(to1, to2, compareId) &&
                Objects.equals(to1.getInSituExpressionPatternId(), to2.getInSituExpressionPatternId()) &&
                Objects.equals(to1.getConditionId(), to2.getConditionId()) &&
                areCallSourceTOsEqual(to1, to2)) {
            return log.exit(true);
        }
        return log.exit(false);
    }
    /**
     * Method to compare two {@code ESTTO}s, to check for complete
     * equality of each attribute.
     *
     * @param to1       A {@code ESTTO} to be compared to {@code to2}.
     * @param to2       A {@code ESTTO} to be compared to {@code to1}.
     * @return          {@code true} if {@code to1} and {@code to2} have all attributes equal.
     */
    private static boolean areTOsEqual(ESTTO to1, ESTTO to2, boolean compareId) {
        log.entry(to1, to2);
        if (areEntityTOsEqual(to1, to2, compareId) &&
                areCallSourceTOsEqual(to1, to2) &&
                Objects.equals(to1.getEstId2(), to2.getEstId2()) &&
                Objects.equals(to1.getUniGeneClusterId(), to2.getUniGeneClusterId())) {
            return log.exit(true);
        }
        return log.exit(false);
    }

    /**
     * Method to compare two {@code CallSourceTO}s, to check for complete
     * equality of each attribute.
     *
     * @param to1       A {@code CallSourceTO} to be compared to {@code to2}.
     * @param to2       A {@code CallSourceTO} to be compared to {@code to1}.
     * @return          {@code true} if {@code to1} and {@code to2} have all attributes equal.
     */
    private static boolean areCallSourceTOsEqual(CallSourceTO<?> to1, CallSourceTO<?> to2) {
        log.entry(to1, to2);
        if (Objects.equals(to1.getAssayId(), to2.getAssayId()) &&
                areTOsEqual(to1.getCallSourceDataTO(), to2.getCallSourceDataTO())) {
            return log.exit(true);
        }
        return log.exit(false);
    }
    /**
     * Method to compare two {@code CallSourceDataTO}s, to check for complete
     * equality of each attribute.
     *
     * @param to1       A {@code CallSourceDataTO} to be compared to {@code to2}.
     * @param to2       A {@code CallSourceDataTO} to be compared to {@code to1}.
     * @return          {@code true} if {@code to1} and {@code to2} have all attributes equal.
     */
    private static boolean areTOsEqual(CallSourceDataTO to1, CallSourceDataTO to2) {
        log.entry(to1, to2);
        if (Objects.equals(to1.getBgeeGeneId(), to2.getBgeeGeneId()) &&
                Objects.equals(to1.getDetectionFlag(), to2.getDetectionFlag()) &&
                Objects.equals(to1.getExpressionConfidence(), to2.getExpressionConfidence()) &&
                Objects.equals(to1.getExclusionReason(), to2.getExclusionReason()) &&
                Objects.equals(to1.getExpressionId(), to2.getExpressionId())) {
            return log.exit(true);
        }
        return log.exit(false);
    }

    /**
     * Method to compare two {@code AffymetrixChipTO}s, to check for complete
     * equality of each attribute.
     *
     * @param to1       An {@code AffymetrixChipTO} to be compared to {@code to2}.
     * @param to2       An {@code AffymetrixChipTO} to be compared to {@code to1}.
     * @param compareId A {@code boolean} defining whether IDs of {@code EntityTO}s should be
     *                  used for comparisons.
     * @return          {@code true} if {@code to1} and {@code to2} have all attributes equal.
     */
    private static boolean areTOsEqual(AffymetrixChipTO to1, AffymetrixChipTO to2, boolean compareId) {
        log.entry(to1, to2);
        if (TOComparator.areEntityTOsEqual(to1, to2, compareId) &&
                Objects.equals(to1.getExperimentId(), to2.getExperimentId()) &&
                Objects.equals(to1.getConditionId(), to2.getConditionId()) &&
                Objects.equals(to1.getAffymetrixChipId(), to2.getAffymetrixChipId()) &&
                Objects.equals(to1.getScanDate(), to2.getScanDate()) &&
                Objects.equals(to1.getChipTypeId(), to2.getChipTypeId()) &&
                Objects.equals(to1.getNormalizationType(), to2.getNormalizationType()) &&
                Objects.equals(to1.getDetectionType(), to2.getDetectionType()) &&
                Objects.equals(to1.getDistinctRankCount(), to2.getDistinctRankCount()) &&
                areBigDecimalEquals(to1.getQualityScore(), to2.getQualityScore()) &&
                areBigDecimalEquals(to1.getPercentPresent(), to2.getPercentPresent()) &&
                areBigDecimalEquals(to1.getMaxRank(), to2.getMaxRank())) {
            return log.exit(true);
        }
        return log.exit(false);
    }
    /**
     * Method to compare two {@code RNASeqLibraryTO}s, to check for complete
     * equality of each attribute.
     *
     * @param to1       A {@code RNASeqLibraryTO} to be compared to {@code to2}.
     * @param to2       A {@code RNASeqLibraryTO} to be compared to {@code to1}.
     * @param compareId A {@code boolean} defining whether IDs of {@code EntityTO}s should be
     *                  used for comparisons.
     * @return          {@code true} if {@code to1} and {@code to2} have all attributes equal.
     */
    private static boolean areTOsEqual(RNASeqLibraryTO to1, RNASeqLibraryTO to2, boolean compareId) {
        log.entry(to1, to2);
        if (TOComparator.areEntityTOsEqual(to1, to2, compareId) &&
                Objects.equals(to1.getExperimentId(), to2.getExperimentId()) &&
                Objects.equals(to1.getConditionId(), to2.getConditionId()) &&
                Objects.equals(to1.getPlatformId(), to2.getPlatformId()) &&
                areBigDecimalEquals(to1.getTmmFactor(), to2.getTmmFactor()) &&
                areBigDecimalEquals(to1.getTpmThreshold(), to2.getTpmThreshold()) &&
                areBigDecimalEquals(to1.getFpkmThreshold(), to2.getFpkmThreshold()) &&
                areBigDecimalEquals(to1.getAllGenesPercentPresent(), to2.getAllGenesPercentPresent()) &&
                areBigDecimalEquals(to1.getProteinCodingGenesPercentPresent(), to2.getProteinCodingGenesPercentPresent()) &&
                areBigDecimalEquals(to1.getIntergenicRegionsPercentPresent(), to2.getIntergenicRegionsPercentPresent()) &&
                areBigDecimalEquals(to1.getThresholdRatioIntergenicCodingPercent(), to2.getThresholdRatioIntergenicCodingPercent()) &&
                Objects.equals(to1.getAllReadCount(), to2.getAllReadCount()) &&
                Objects.equals(to1.getMappedReadCount(), to2.getMappedReadCount()) &&
                Objects.equals(to1.getMinReadLength(), to2.getMinReadLength()) &&
                Objects.equals(to1.getMaxReadLength(), to2.getMaxReadLength()) &&
                Objects.equals(to1.getLibraryType(), to2.getLibraryType()) &&
                Objects.equals(to1.getLibraryOrientation(), to2.getLibraryOrientation()) &&
                Objects.equals(to1.getDistinctRankCount(), to2.getDistinctRankCount()) &&
                areBigDecimalEquals(to1.getMaxRank(), to2.getMaxRank())) {
            return log.exit(true);
        }
        return log.exit(false);
    }
    /**
     * Method to compare two {@code InSituEvidenceTO}s, to check for complete
     * equality of each attribute.
     *
     * @param to1       An {@code InSituEvidenceTO} to be compared to {@code to2}.
     * @param to2       An {@code InSituEvidenceTO} to be compared to {@code to1}.
     * @param compareId A {@code boolean} defining whether IDs of {@code EntityTO}s should be
     *                  used for comparisons.
     * @return          {@code true} if {@code to1} and {@code to2} have all attributes equal.
     */
    private static boolean areTOsEqual(InSituEvidenceTO to1, InSituEvidenceTO to2, boolean compareId) {
        log.entry(to1, to2);
        if (TOComparator.areEntityTOsEqual(to1, to2, compareId) &&
                Objects.equals(to1.getExperimentId(), to2.getExperimentId()) &&
                Objects.equals(to1.getEvidenceDistinguishable(), to2.getEvidenceDistinguishable()) &&
                Objects.equals(to1.getInSituEvidenceUrlPart(), to2.getInSituEvidenceUrlPart())) {
            return log.exit(true);
        }
        return log.exit(false);
    }
    /**
     * Method to compare two {@code ESTLibraryTO}s, to check for complete
     * equality of each attribute.
     *
     * @param to1       A {@code ESTLibraryTO} to be compared to {@code to2}.
     * @param to2       A {@code ESTLibraryTO} to be compared to {@code to1}.
     * @param compareId A {@code boolean} defining whether IDs of {@code EntityTO}s should be
     *                  used for comparisons.
     * @return          {@code true} if {@code to1} and {@code to2} have all attributes equal.
     */
    private static boolean areTOsEqual(ESTLibraryTO to1, ESTLibraryTO to2, boolean compareId) {
        log.entry(to1, to2);
        if (TOComparator.areEntityTOsEqual(to1, to2, compareId) &&
                Objects.equals(to1.getConditionId(), to2.getConditionId()) &&
                Objects.equals(to1.getDataSourceId(), to2.getDataSourceId())) {
            return log.exit(true);
        }
        return log.exit(false);
    }

    /**
     * Method to compare two {@code MicroarrayExperimentTO}s, to check for complete
     * equality of each attribute.
     *
     * @param to1       A {@code MicroarrayExperimentTO} to be compared to {@code to2}.
     * @param to2       A {@code MicroarrayExperimentTO} to be compared to {@code to1}.
     * @param compareId A {@code boolean} defining whether IDs of {@code EntityTO}s should be
     *                  used for comparisons. 
     * @return          {@code true} if {@code to1} and {@code to2} have all attributes equal.
     */
    private static boolean areTOsEqual(MicroarrayExperimentTO to1, MicroarrayExperimentTO to2, boolean compareId) {
        log.entry(to1, to2);
        if (areTOsEqual((ExperimentTO<?>) to1, (ExperimentTO<?>) to2, compareId)) {
            return log.exit(true);
        }
        return log.exit(false);
    }
    /**
     * Method to compare two {@code RNASeqExperimentTO}s, to check for complete
     * equality of each attribute.
     *
     * @param to1       A {@code RNASeqExperimentTO} to be compared to {@code to2}.
     * @param to2       A {@code RNASeqExperimentTO} to be compared to {@code to1}.
     * @param compareId A {@code boolean} defining whether IDs of {@code EntityTO}s should be
     *                  used for comparisons. 
     * @return          {@code true} if {@code to1} and {@code to2} have all attributes equal.
     */
    private static boolean areTOsEqual(RNASeqExperimentTO to1, RNASeqExperimentTO to2, boolean compareId) {
        log.entry(to1, to2);
        if (areTOsEqual((ExperimentTO<?>) to1, (ExperimentTO<?>) to2, compareId)) {
            return log.exit(true);
        }
        return log.exit(false);
    }
    /**
     * Method to compare two {@code InSituExperimentTO}s, to check for complete
     * equality of each attribute.
     *
     * @param to1       A {@code InSituExperimentTO} to be compared to {@code to2}.
     * @param to2       A {@code InSituExperimentTO} to be compared to {@code to1}.
     * @param compareId A {@code boolean} defining whether IDs of {@code EntityTO}s should be
     *                  used for comparisons. 
     * @return          {@code true} if {@code to1} and {@code to2} have all attributes equal.
     */
    private static boolean areTOsEqual(InSituExperimentTO to1, InSituExperimentTO to2, boolean compareId) {
        log.entry(to1, to2);
        if (areTOsEqual((ExperimentTO<?>) to1, (ExperimentTO<?>) to2, compareId)) {
            return log.exit(true);
        }
        return log.exit(false);
    }
    /**
     * Method to compare two {@code ExperimentTO}s, to check for complete
     * equality of each attribute.
     *
     * @param to1       A {@code AffymetrixProbesetTO} to be compared to {@code to2}.
     * @param to2       A {@code AffymetrixProbesetTO} to be compared to {@code to1}.
     * @param compareId A {@code boolean} defining whether IDs of {@code EntityTO}s should be
     *                  used for comparisons. 
     * @return          {@code true} if {@code to1} and {@code to2} have all attributes equal.
     */
    private static boolean areTOsEqual(ExperimentTO<?> to1, ExperimentTO<?> to2, boolean compareId) {
        log.entry(to1, to2);
        if (areEntityTOsEqual(to1, to2, compareId) &&
                Objects.equals(to1.getDataSourceId(), to2.getDataSourceId())) {
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
