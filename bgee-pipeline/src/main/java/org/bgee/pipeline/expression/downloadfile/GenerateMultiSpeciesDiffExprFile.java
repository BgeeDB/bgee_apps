package org.bgee.pipeline.expression.downloadfile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.anatdev.mapping.StageGroupingDAO;
import org.bgee.model.dao.api.anatdev.mapping.StageGroupingDAO.GroupToStageTO;
import org.bgee.model.dao.api.anatdev.mapping.StageGroupingDAO.GroupToStageTOResultSet;
import org.bgee.model.dao.api.anatdev.mapping.SummarySimilarityAnnotationDAO;
import org.bgee.model.dao.api.anatdev.mapping.SummarySimilarityAnnotationDAO.SimAnnotToAnatEntityTO;
import org.bgee.model.dao.api.anatdev.mapping.SummarySimilarityAnnotationDAO.SimAnnotToAnatEntityTOResultSet;
import org.bgee.model.dao.api.anatdev.mapping.SummarySimilarityAnnotationDAO.SummarySimilarityAnnotationTO;
import org.bgee.model.dao.api.anatdev.mapping.SummarySimilarityAnnotationDAO.SummarySimilarityAnnotationTOResultSet;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTO.ComparisonFactor;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTO.DiffExprCallType;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTOResultSet;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallParams;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTO;
import org.bgee.model.dao.api.gene.HierarchicalGroupDAO;
import org.bgee.model.dao.api.gene.HierarchicalGroupDAO.HierarchicalGroupToGeneTO;
import org.bgee.model.dao.api.gene.HierarchicalGroupDAO.HierarchicalGroupToGeneTOResultSet;
import org.bgee.model.dao.api.ontologycommon.CIOStatementDAO.CIOStatementTO;
import org.bgee.model.dao.api.species.TaxonDAO;
import org.bgee.model.dao.api.species.TaxonDAO.TaxonTOResultSet;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.pipeline.BgeeDBUtils;
import org.bgee.pipeline.CommandRunner;
import org.bgee.pipeline.Utils;
import org.supercsv.cellprocessor.Trim;
import org.supercsv.cellprocessor.constraint.DMinMax;
import org.supercsv.cellprocessor.constraint.IsElementOf;
import org.supercsv.cellprocessor.constraint.LMinMax;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.constraint.StrNotNullOrEmpty;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.dozer.CsvDozerBeanWriter;
import org.supercsv.io.dozer.ICsvDozerBeanWriter;


/**
 * Class used to generate multi-species differential expression TSV download files 
 * (simple and advanced files) from the Bgee database. 
 *
 * @author 	Valentine Rech de Laval
 * @version Bgee 13
 * @since 	Bgee 13
 */
public class GenerateMultiSpeciesDiffExprFile   extends GenerateDownloadFile 
                                                implements GenerateMultiSpeciesDownloadFile {

    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = LogManager.getLogger(
            GenerateMultiSpeciesDiffExprFile.class.getName());
    
    /**
     * A {@code String} that is the name of the column containing best p-value using Affymetrix, 
     * in the download file.
     */
    public final static String AFFYMETRIX_P_VALUE_COLUMN_NAME = "Affymetrix best supporting p-value";
    /**
     * A {@code String} that is the name of the column containing the number of analysis using 
     * Affymetrix data where the same call is found, in the download file.
     */
    //XXX: maybe we should also provide number of probesets, not only number of analysis
    public final static String AFFYMETRIX_CONSISTENT_DEA_COUNT_COLUMN_NAME = 
            "Affymetrix analysis count supporting Affymetrix call";
    /**
     * A {@code String} that is the name of the column containing the number of analysis using 
     * Affymetrix data where a different call is found, in the download file.
     */
    //XXX: maybe we should also provide number of probesets, not only number of analysis
    public final static String AFFYMETRIX_INCONSISTENT_DEA_COUNT_COLUMN_NAME = 
            "Affymetrix analysis count in conflict with Affymetrix call";
    /**
     * A {@code String} that is the name of the column containing best p-value using RNA-Seq, 
     * in the download file.
     */
    public final static String RNASEQ_P_VALUE_COLUMN_NAME = "RNA-Seq best supporting p-value";
    /**
     * A {@code String} that is the name of the column containing the number of analysis using 
     * RNA-Seq data where the same call is found, in the download file.
     */
    public final static String RNASEQ_CONSISTENT_DEA_COUNT_COLUMN_NAME = 
            "RNA-Seq analysis count supporting RNA-Seq call";
    /**
     * A {@code String} that is the name of the column containing the number of analysis using 
     * RNA-Seq data where a different call is found, in the download file.
     */
    public final static String RNASEQ_INCONSISTENT_DEA_COUNT_COLUMN_NAME = 
            "RNA-Seq analysis count in conflict with RNA-Seq call";
    /**
     * A {@code String} that is the name of the column containing merged differential expressions 
     * from different data types, in the download file.
     */
    public final static String DIFFEXPRESSION_COLUMN_NAME = "Differential expression";

    /**
     * Class used to store multi-species differential expression counts: over-expressed gene count, 
     * under-expressed gene count, not diff. expressed gene count, not expressed gene count,
     * and count of genes without data in the family.
     *
     * @author  Valentine Rech de Laval
     * @version Bgee 13
     * @since   Bgee 13
     */
    public static class SpeciesDiffExprCounts {
        
        /**
         * See {@link #getSpeciesId()}.
         */
        private String speciesId;
        /**
         * See {@link #getOverExprGeneCount()}.
         */
        private Long overExprGeneCount;
        /**
         * See {@link #getUnderExprGeneCount()}.
         */
        private Long underExprGeneCount;
        /**
         * See {@link #getNotDiffExprGeneCount()}.
         */
        private Long notDiffExprGeneCount;
        /**
         * See {@link #getNotExprGeneCount()}.
         */
        private Long notExprGeneCount;
        /**
         * See {@link #getNAGeneCount()}.
         */
        private Long naGeneCount;
    
        /**
         * Constructor providing all arguments of the class.
         *
         * @param speciesId             See {@link #getSpeciesId()}.
         * @param overExprGeneCount     See {@link #getOverExprGeneCount()}.
         * @param underExprGeneCount    See {@link #getUnderExprGeneCount()}.
         * @param notDiffExprGeneCount  See {@link #getNotDiffExprGeneCount()}.
         * @param notExprGeneCount      See {@link #getNotExprGeneCount()}.
         * @param naGeneCount           See {@link #getNAGeneCount()}.
         */
        public SpeciesDiffExprCounts(String speciesId, Long overExprGeneCount, Long underExprGeneCount, 
                Long notDiffExprGeneCount, Long notExprGeneCount, Long naGeneCount) {
            this.speciesId = speciesId;
            this.overExprGeneCount = overExprGeneCount;
            this.underExprGeneCount = underExprGeneCount;
            this.notDiffExprGeneCount = notDiffExprGeneCount;
            this.notExprGeneCount = notExprGeneCount;
            this.naGeneCount = naGeneCount;
        }
    
        /**
         * @return  the {@code String} that is the ID of the species.
         */
        public String getSpeciesId() {
            return speciesId;
        }
        /**
         * @param speciesId A {@code String} that is the ID of the species.
         */
        public void setSpeciesId(String speciesId) {
            this.speciesId = speciesId;
        }
    
        /**
         * @return  the {@code Long} that is the number of over-expressed genes in the family.
         */
        public Long getOverExprGeneCount() {
            return overExprGeneCount;
        }
        /**
         * @param overExprGeneCount A {@code Long} that is the number of over-expressed 
         *                          genes in the family.
         */
        public void setOverExprGeneCount(Long overExprGeneCount) {
            this.overExprGeneCount = overExprGeneCount;
        }
        
        /**
         * @return  the {@code Long} that is the number of under-expressed genes in the family.
         */
        public Long getUnderExprGeneCount() {
            return underExprGeneCount;
        }
        /**
         * @param underExprGeneCount    A {@code Long} that is the number of under-expressed 
         *                              genes in the family.
         */
        public void setUnderExprGeneCount(Long underExprGeneCount) {
            this.underExprGeneCount = underExprGeneCount;
        }
    
        /**
         * @return  the {@code Long} that is the number of not diff. expressed genes in the family.
         */
        public Long getNotDiffExprGeneCount() {
            return notDiffExprGeneCount;
        }
        /**
         * @param notDiffExprGeneCount  A {@code Long} that is the number of not diff. expressed 
         *                              genes in the family.
         */
        public void setNotDiffExprGeneCount(Long notDiffExprGeneCount) {
            this.notDiffExprGeneCount = notDiffExprGeneCount;
        }
    
        /**
         * @return  the {@code Long} that is the number of not expressed genes in the family.
         */
        public Long getNotExprGeneCount() {
            return notExprGeneCount;
        }
        /**
         * @param notExprGeneCount  A {@code Long} that is the number of not expressed genes 
         *                          in the family. 
         */
        public void setNotExprGeneCount(Long notExprGeneCount) {
            this.notExprGeneCount = notExprGeneCount;
        }
        
        /**
         * @return  the {@code Long} that is the number of genes without data in the family.
         */
        public Long getNAGeneCount() {
            return naGeneCount;
        }
        /**
         * @param naGeneCount   A {@code Long} that is the number of genes without data in the family.
         */
        public void setNAGeneCount(Long naGeneCount) {
            this.naGeneCount = naGeneCount;
        }
    
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((speciesId == null) ? 0 : speciesId.hashCode());
            result = prime * result + 
                    ((overExprGeneCount == null) ? 0 : overExprGeneCount.hashCode());
            result = prime * result + 
                    ((underExprGeneCount == null) ? 0 : underExprGeneCount.hashCode());
            result = prime * result +
                    ((notDiffExprGeneCount == null) ? 0 : notDiffExprGeneCount.hashCode());
            result = prime * result + ((notExprGeneCount == null) ? 0 : notExprGeneCount.hashCode());
            result = prime * result + ((naGeneCount == null) ? 0 : naGeneCount.hashCode());
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
            SpeciesDiffExprCounts other = (SpeciesDiffExprCounts) obj;
            if (speciesId == null) {
                if (other.speciesId != null)
                    return false;
            } else if (!speciesId.equals(other.speciesId))
                return false;
            if (overExprGeneCount == null) {
                if (other.overExprGeneCount != null)
                    return false;
            } else if (!overExprGeneCount.equals(other.overExprGeneCount))
                return false;
            if (underExprGeneCount == null) {
                if (other.underExprGeneCount != null)
                    return false;
            } else if (!underExprGeneCount.equals(other.underExprGeneCount))
                return false;
            if (notDiffExprGeneCount == null) {
                if (other.notDiffExprGeneCount != null)
                    return false;
            } else if (!notDiffExprGeneCount.equals(other.notDiffExprGeneCount))
                return false;
            if (notExprGeneCount == null) {
                if (other.notExprGeneCount != null)
                    return false;
            } else if (!notExprGeneCount.equals(other.notExprGeneCount))
                return false;
            if (naGeneCount == null) {
                if (other.naGeneCount != null)
                    return false;
            } else if (!naGeneCount.equals(other.naGeneCount))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "Species ID: " + getSpeciesId() +
                    " - Over-expressed gene count: " + getOverExprGeneCount() +
                    " - Under-expressed gene count: " + getUnderExprGeneCount() +
                    " - Not diff. expressed gene count: " + getNotDiffExprGeneCount() +
                    " - No-expressed gene count: " + getNotExprGeneCount() + 
                    " - N/A gene count: " + getNAGeneCount();
        }
    }

    /**
     * A bean representing a row of a multi-species simple differential expression file. 
     * Getter and setter names must follow standard bean definitions.
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13 Apr. 2015
     * @since Bgee 13
     */
    public static class MultiSpeciesSimpleDiffExprFileBean extends MultiSpeciesFileBean {
    
        /**
         * See {@link #getSpeciesDiffExprCounts()}.
         */
        private List<SpeciesDiffExprCounts> speciesDiffExprCounts;
    
        /**
         * 0-argument constructor of the bean.
         */
        public MultiSpeciesSimpleDiffExprFileBean() {
        }
    
        /**
         * Constructor providing all arguments of the class.
         *
         * @param omaId             See {@link #getOmaId()}.
         * @param entityIds         See {@link #getEntityIds()}.
         * @param entityNames       See {@link #getEntityNames()}.
         * @param stageIds          See {@link #getStageIds()}.
         * @param stageNames        See {@link #getStageNames()}.
         * @param speciesCounts     See {@link #getSpeciesDiffExprCounts()}.
         */
        public MultiSpeciesSimpleDiffExprFileBean(String omaId,
                List<String> entityIds, List<String> entityNames, List<String> stageIds, 
                List<String> stageNames, List<SpeciesDiffExprCounts> speciesDiffExprCounts) {
            super(omaId, entityIds, entityNames, stageIds, stageNames);
            this.speciesDiffExprCounts = speciesDiffExprCounts;
        }
    
        /**
         * @return  the {@code List} of {@code SpeciesDiffExprCounts}s that are the counts of the 
         *          genes for each species of a multi-species differential expression file.
         */
        public List<SpeciesDiffExprCounts> getSpeciesDiffExprCounts() {
            return speciesDiffExprCounts;
        }
        /**
         * @param speciesDiffExprCounts A {@code List} of {@code SpeciesDiffExprCounts}s that are 
         *                              the counts of the genes for each species of a multi-species 
         *                              differential expression file.
         */
        public void setSpeciesDiffExprCounts(List<SpeciesDiffExprCounts> speciesDiffExprCounts) {
            this.speciesDiffExprCounts = speciesDiffExprCounts;
        }
    
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + 
                    ((speciesDiffExprCounts == null) ? 0 : speciesDiffExprCounts.hashCode());
            return result;
        }
    
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!super.equals(obj))
                return false;
            if (getClass() != obj.getClass())
                return false;
            MultiSpeciesSimpleDiffExprFileBean other = (MultiSpeciesSimpleDiffExprFileBean) obj;
            if (speciesDiffExprCounts == null) {
                if (other.speciesDiffExprCounts != null)
                    return false;
            } else if (!speciesDiffExprCounts.equals(other.speciesDiffExprCounts))
                return false;
            return true;
        }
        
        @Override
        public String toString() {
            return super.toString() + " - Species counts: " + getSpeciesDiffExprCounts();
        }
    }

    /**
     * A bean representing a row of a complete differential expression multi-species file. 
     * Getter and setter names must follow standard bean definitions.
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13 Apr. 2015
     * @since Bgee 13
     */
    public static class MultiSpeciesCompleteDiffExprFileBean extends MultiSpeciesCompleteFileBean {
    
        /** 
         * @see getAffymetrixData()
         */
        private String affymetrixData;
        /**
         * @see getAffymetrixQuality()
         */
        private String affymetrixQuality;
        /**
         * See {@link #getAffymetrixPValue()}.
         */
        private Double affymetrixPValue;
        /**
         * See {@link #getAffymetrixConsistentDEA()}.
         */
        private Long affymetrixConsistentDEA;
        /**
         * See {@link #getAffymetrixInconsistentDEA()}.
         */
        private Long affymetrixInconsistentDEA;
        /**
         * @see getRNASeqData()
         */
        private String rnaSeqData;
        /**
         * @see getRNASeqQuality()
         */
        private String rnaSeqQuality;
        /**
         * See {@link #getRnaSeqPValue()}.
         */
        private Double rnaSeqPValue;
        /**
         * See {@link #getRnaSeqConsistentDEA()}.
         */
        private Long rnaSeqConsistentDEA;        
        /**
         * See {@link #getRnaSeqInconsistentDEA()}.
         */
        private Long rnaSeqInconsistentDEA;
        /**
         * See {@link #getDifferentialExpression()}.
         */
        private String differentialExpression;
        /**
         * See {@link #getCallQuality()}.
         */
        private String callQuality;
    
        /**
         * 0-argument constructor of the bean.
         */
        public MultiSpeciesCompleteDiffExprFileBean() {
        }
    
        /**
         * Constructor providing all arguments of the class.
         * 
         * @param omaId                     See {@link #getOmaId()}.
         * @param entityIds                 See {@link #getEntityIds()}.
         * @param entityNames               See {@link #getEntityNames()}.
         * @param stageIds                  See {@link #getStageIds()}.
         * @param stageNames                See {@link #getStageNames()}.
         * @param geneId                    See {@link #getGeneId()}.
         * @param geneName                  See {@link #getGeneName()}.
         * @param cioId                     See {@link #getCioId()}.
         * @param cioName                   See {@link #getCioName()}.
         * @param speciesId                 See {@link #getSpeciesId()}.
         * @param speciesName               See {@link #getSpeciesName()}.
         * @param affymetrixData            See {@link #getAffymetrixData()}.
         * @param affymetrixQuality         See {@link #getAffymetrixQuality()}.
         * @param affymetrixPValue          See {@link #getAffymetrixPValue()}.
         * @param affymetrixConsistentDEA   See {@link #getAffymetrixConsistentDEA()}.
         * @param affymetrixInconsistentDEA See {@link #getAffymetrixInconsistentDEA()}.
         * @param rnaSeqData                See {@link #getRNASeqData()}.
         * @param rnaSeqQuality             See {@link #getRNASeqQuality()}.
         * @param rnaSeqPValue              See {@link #getAffymetrixPValue()}.
         * @param rnaSeqConsistentDEA       See {@link #getRnaSeqConsistentDEA()}.
         * @param rnaSeqInconsistentDEA     See {@link #getRnaSeqInconsistentDEA()}.
         * @param differentialExpression    See {@link #getDifferentialExpression()}.
         * @param callQuality               See {@link #getCallQuality()}.
         */
        public MultiSpeciesCompleteDiffExprFileBean(String omaId, 
                List<String> entityIds, List<String> entityNames, List<String> stageIds, 
                List<String> stageNames, String geneId, String geneName, 
                String cioId, String cioName, String speciesId, String speciesName,
                String affymetrixData, String affymetrixQuality, Double affymetrixPValue, 
                Long affymetrixConsistentDEA, Long affymetrixInconsistentDEA, 
                String rnaSeqData, String rnaSeqQuality, Double rnaSeqPValue, 
                Long rnaSeqConsistentDEA, Long rnaSeqInconsistentDEA,
                String differentialExpression, String callQuality) {
            super(omaId, entityIds, entityNames, stageIds, stageNames, 
                    geneId, geneName, cioId, cioName, speciesId, speciesName);
            this.affymetrixData = affymetrixData;
            this.affymetrixQuality = affymetrixQuality;
            this.affymetrixPValue = affymetrixPValue; 
            this.affymetrixConsistentDEA = affymetrixConsistentDEA;
            this.affymetrixInconsistentDEA = affymetrixInconsistentDEA; 
            this.rnaSeqData = rnaSeqData ;
            this.rnaSeqQuality = rnaSeqQuality;
            this.rnaSeqPValue = rnaSeqPValue; 
            this.rnaSeqConsistentDEA = rnaSeqConsistentDEA;
            this.rnaSeqInconsistentDEA = rnaSeqInconsistentDEA; 
            this.differentialExpression = differentialExpression;
            this.callQuality = callQuality;
        }
    
        /**
         * @return  the {@code String} defining the contribution of Affymetrix data 
         *          to the generation of this call.
         */
        public String getAffymetrixData() {
            return affymetrixData;
        }
        /**
         * @param affymetrixData    A {@code String} defining the contribution 
         *                          of Affymetrix data to the generation of this call.
         * @see #getAffymetrixData()
         */
        public void setAffymetrixData(String affymetrixData) {
            this.affymetrixData = affymetrixData;
        }
    
        /**
         * @return  the {@code String} defining the call quality found with Affymetrix experiment.
         */
        public String getAffymetrixQuality() {
            return affymetrixQuality;
        }
        /** 
         * @param affymetrixQuality A {@code String} defining the call quality found with 
         *                          Affymetrix experiment.
         * @see #getAffymetrixQuality()
         */
        public void setAffymetrixQuality(String affymetrixQuality) {
            this.affymetrixQuality = affymetrixQuality;
        }
    
        /**
         * @return  the {@code Double} that is the best p-value using Affymetrix.
         */
        public Double getAffymetrixPValue() {
            return affymetrixPValue;
        }
        /**
         * @param affymetrixPValue  A {@code Double} that is the best p-value using Affymetrix.
         */
        public void setAffymetrixPValue(Double affymetrixPValue) {
            this.affymetrixPValue = affymetrixPValue;
        }
    
        /**
         * @return  the {@code Long} that is the number of analysis using 
         *          Affymetrix data where the same call is found.
         */
        public Long getAffymetrixConsistentDEA() {
            return affymetrixConsistentDEA;
        }
        /**
         * @param affymetrixConsistentDEA   A {@code Long} that is the number of analysis using 
         *                                  Affymetrix data where the same call is found.
         */
        public void setAffymetrixConsistentDEA(Long affymetrixConsistentDEA) {
            this.affymetrixConsistentDEA = affymetrixConsistentDEA;
        }
    
        /**
         * @return  the {@code Long} that is the number of analysis using 
         *          Affymetrix data where a different call is found.
         */
        public Long getAffymetrixInconsistentDEA() {
            return affymetrixInconsistentDEA;
        }
        /**
         * @param affymetrixInconsistentDEA A {@code Long} that is the number of analysis using 
         *                                  Affymetrix data where a different call is found.
         */
        public void setAffymetrixInconsistentDEA(Long affymetrixInconsistentDEA) {
            this.affymetrixInconsistentDEA = affymetrixInconsistentDEA;
        }
    
        /**
         * @return  the {@code String} defining the contribution of RNA-Seq data 
         *          to the generation of this call.
         */
        public String getRNASeqData() {
            return rnaSeqData;
        }
        /**
         * @param rnaSeqData    A {@code String} defining the contribution 
         *                      of RNA-Seq data to the generation of this call.
         * @see #getRNASeqData()
         */
        public void setRNASeqData(String rnaSeqData) {
            this.rnaSeqData = rnaSeqData;
        }
    
        /**
         * @return  the {@code String} defining the call quality found with RNA-Seq experiment.
         */
        public String getRNASeqQuality() {
            return rnaSeqQuality;
        }
        /** 
         * @param rnaSeqQuality A {@code String} defining the call quality found with 
         *                      RNA-Seq experiment.
         * @see #getRNASeqQuality()
         */
        public void setRNASeqQuality(String rnaSeqQuality) {
            this.rnaSeqQuality = rnaSeqQuality;
        }
    
        /**
         * @return  the {@code Double} that is the best p-value using RNA-Seq.
         */
        public Double getRnaSeqPValue() {
            return rnaSeqPValue;
        }
        /**
         * @param rnaSeqPValue  A {@code Double} that is the best p-value using RNA-Seq.
         */
        public void setRnaSeqPValue(Double rnaSeqPValue) {
            this.rnaSeqPValue = rnaSeqPValue;
        }
    
        /**
         * @return  the {@code Long} that is the number of analysis using 
         *          RNA-Seq data where the same call is found.
         */
        public Long getRnaSeqConsistentDEA() {
            return rnaSeqConsistentDEA;
        }
        /**
         * @param rnaSeqConsistentDEA   A {@code Long} that is the number of analysis using 
         *                              RNA-Seq data where the same call is found.
         */
        public void setRnaSeqConsistentDEA(Long rnaSeqConsistentDEA) {
            this.rnaSeqConsistentDEA = rnaSeqConsistentDEA;
        }
    
        /**
         * @return  the {@code Long} that is the number of analysis using 
         *          RNA-Seq data where a different call is found.
         */
        public Long getRnaSeqInconsistentDEA() {
            return rnaSeqInconsistentDEA;
        }
        /**
         * @param rnaSeqInconsistentDEA A {@code Long} that is the number of analysis using 
         *                              RNA-Seq data where a different call is found.
         */
        public void setRnaSeqInconsistentDEA(Long rnaSeqInconsistentDEA) {
            this.rnaSeqInconsistentDEA = rnaSeqInconsistentDEA;
        }
    
        /**
         * @return  the {@code String} that is merged differential expressions 
         *          from different data types.
         */
        public String getDifferentialExpression() {
            return differentialExpression;
        }
        /**
         * @param differentialExpression    A {@code String} that is merged differential expressions 
         *                                  from different data types.
         */
        public void setDifferentialExpression(String differentialExpression) {
            this.differentialExpression = differentialExpression;
        }
    
        /** 
         * @return  the {@code String} that is call quality.
         */
        public String getCallQuality() {
            return callQuality;
        }
        /**
         * @param callQuality   A {@code String} that is call quality.
         */
        public void setCallQuality(String callQuality) {
            this.callQuality = callQuality;
        }
    
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + ((affymetrixData == null) ? 0 : affymetrixData.hashCode());
            result = prime * result + ((affymetrixQuality == null) ? 0 : affymetrixQuality.hashCode());
            result = prime * result + ((affymetrixPValue == null) ? 0 : affymetrixPValue.hashCode());
            result = prime * result +
                    ((affymetrixConsistentDEA == null) ? 0 : affymetrixConsistentDEA.hashCode());
            result = prime * result +
                    ((affymetrixInconsistentDEA == null) ? 0 : affymetrixInconsistentDEA.hashCode());
            result = prime * result + ((rnaSeqData == null) ? 0 : rnaSeqData.hashCode());
            result = prime * result + ((rnaSeqQuality == null) ? 0 : rnaSeqQuality.hashCode());
            result = prime * result + ((rnaSeqPValue == null) ? 0 : rnaSeqPValue.hashCode());
            result = prime * result + 
                    ((rnaSeqConsistentDEA == null) ? 0 : rnaSeqConsistentDEA.hashCode());
            result = prime * result + 
                    ((rnaSeqInconsistentDEA == null) ? 0 : rnaSeqInconsistentDEA.hashCode());
            result = prime * result +
                    ((differentialExpression == null) ? 0 : differentialExpression.hashCode());
            result = prime * result + ((callQuality == null) ? 0 : callQuality.hashCode());
            return result;
        }
    
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!super.equals(obj))
                return false;
            if (getClass() != obj.getClass())
                return false;
            MultiSpeciesCompleteDiffExprFileBean other = (MultiSpeciesCompleteDiffExprFileBean) obj;
            if (affymetrixData == null) {
                if (other.affymetrixData != null)
                    return false;
            } else if (!affymetrixData.equals(other.affymetrixData))
                return false;
            if (affymetrixQuality == null) {
                if (other.affymetrixQuality != null)
                    return false;
            } else if (!affymetrixQuality.equals(other.affymetrixQuality))
                return false;
            if (affymetrixPValue == null) {
                if (other.affymetrixPValue != null)
                    return false;
            } else if (!affymetrixPValue.equals(other.affymetrixPValue))
                return false;
            if (affymetrixConsistentDEA == null) {
                if (other.affymetrixConsistentDEA != null)
                    return false;
            } else if (!affymetrixConsistentDEA.equals(other.affymetrixConsistentDEA))
                return false;
            if (affymetrixInconsistentDEA == null) {
                if (other.affymetrixInconsistentDEA != null)
                    return false;
            } else if (!affymetrixInconsistentDEA.equals(other.affymetrixInconsistentDEA))
                return false;
            if (rnaSeqData == null) {
                if (other.rnaSeqData != null)
                    return false;
            } else if (!rnaSeqData.equals(other.rnaSeqData))
                return false;
            if (rnaSeqQuality == null) {
                if (other.rnaSeqQuality != null)
                    return false;
            } else if (!rnaSeqQuality.equals(other.rnaSeqQuality))
                return false;
            if (rnaSeqPValue == null) {
                if (other.rnaSeqPValue != null)
                    return false;
            } else if (!rnaSeqPValue.equals(other.rnaSeqPValue))
                return false;
            if (rnaSeqConsistentDEA == null) {
                if (other.rnaSeqConsistentDEA != null)
                    return false;
            } else if (!rnaSeqConsistentDEA.equals(other.rnaSeqConsistentDEA))
                return false;
            if (rnaSeqInconsistentDEA == null) {
                if (other.rnaSeqInconsistentDEA != null)
                    return false;
            } else if (!rnaSeqInconsistentDEA.equals(other.rnaSeqInconsistentDEA))
                return false;
            if (differentialExpression == null) {
                if (other.differentialExpression != null)
                    return false;
            } else if (!differentialExpression.equals(other.differentialExpression))
                return false;
            if (callQuality == null) {
                if (other.callQuality != null)
                    return false;
            } else if (!callQuality.equals(other.callQuality))
                return false;
            return true;
        }
    
        @Override
        public String toString() {
            return super.toString() + " - Affymetrix data: " + getAffymetrixData() + 
                    " - Affymetrix quality: " + getAffymetrixQuality() + 
                    " - Affymetrix p-value: " + getAffymetrixPValue() + 
                    " - Affymetrix consistent DEA: " + getAffymetrixConsistentDEA() + 
                    " - Affymetrix inconsistent DEA: " + getAffymetrixInconsistentDEA() + 
                    " - RNA-Seq data: " + getRNASeqData() + 
                    " - RNA-Seq quality: " + getRNASeqQuality() + 
                    " - RNA-Seq p-value: " + getRnaSeqPValue() + 
                    " - RNA-Seq consistent DEA: " + getRnaSeqConsistentDEA() + 
                    " - RNA-Seq inconsistent DEA: " + getRnaSeqInconsistentDEA() + 
                    " - Differential expression: " + getDifferentialExpression() + 
                    " - Call quality: " + getCallQuality();
        }
    }

    /**
     * An {@code Enum} used to define, for each data type (Affymetrix and RNA-Seq), 
     * as well as for the summary column, the data state of the call.
     * <ul>
     * <li>{@code NO_DATA}:              means that the call has never been observed 
     *                                   for the related data type.
     * <li>{@code NOT_EXPRESSED}:        means that the related gene was never seen 
     *                                   as 'expressed' in any of the samples used 
     *                                   in the analysis for the related data type, 
     *                                   it was then not tested for differential expression.
     * <li>{@code OVER_EXPRESSION}:      over-expressed calls.
     * <li>{@code UNDER_EXPRESSION}:     under-expressed calls.
     * <li>{@code NOT_DIFF_EXPRESSION}:  means that the gene was tested for differential 
     *                                   expression, but no significant fold change observed.
     * <li>{@code WEAK_AMBIGUITY}:       different data types are not completely coherent: a data 
     *                                   type says over or under-expressed, while the other says 
     *                                   'not differentially expressed'; or a data type says 
     *                                   over-expressed, while the other data type says 'not 
     *                                   expressed'.
     * <li>{@code STRONG_AMBIGUITY}:     different data types are not coherent: a data type says over 
     *                                   or under-expressed, while the other data says the opposite.
     * </ul>
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    //XXX: why doesn't it reuse code from single species download files?
    public enum DiffExpressionData {
        NO_DATA("no data"), NOT_EXPRESSED("not expressed"), OVER_EXPRESSION("over-expression"), 
        UNDER_EXPRESSION("under-expression"), NOT_DIFF_EXPRESSION("no diff expression"), 
        WEAK_AMBIGUITY("weak ambiguity"), STRONG_AMBIGUITY("strong ambiguity");
    
        private final String stringRepresentation;
    
        /**
         * Convert the {@code String} representation of a differential expression data 
         * into a {@code DiffExpressionData}. Operation performed by calling 
         * {@link TransferObject#convert(Class, String)} with {@code DiffExpressionData} as the 
         * {@code Class} argument, and {@code representation} as the {@code String} argument.
         * 
         * @param representation    A {@code String} representing a differential expression data.
         * @return                  A {@code DiffExpressionData} corresponding to 
         *                          {@code representation}.
         * @throw IllegalArgumentException  If {@code representation} does not correspond 
         *                                  to any {@code DiffExprCallType}.
         * @see #convert(Class, String)
         */
        //TODO: DRY from TransfertObject
        public static final DiffExpressionData convertToDiffExpressionData(String representation) {
            log.entry(representation);
            
            if (representation == null) {
                return log.exit(null);
            }
            for (DiffExpressionData element: DiffExpressionData.class.getEnumConstants()) {
                if (element.getStringRepresentation().equals(representation) || 
                        element.name().equals(representation)) {
                    return log.exit(element);
                }
            }
            throw log.throwing(new IllegalArgumentException("\"" + representation + 
                    "\" does not correspond to any element of " + 
                    DiffExpressionData.class.getName()));
        }
        
    
        /**
         * Constructor providing the {@code String} representation 
         * of this {@code DiffExpressionData}.
         * 
         * @param stringRepresentation   A {@code String} corresponding to this 
         *                               {@code DiffExpressionData}.
         */
        private DiffExpressionData(String stringRepresentation) {
            this.stringRepresentation = stringRepresentation;
        }
    
        public String getStringRepresentation() {
            return this.stringRepresentation;
        }
    
        @Override
        public String toString() {
            return this.getStringRepresentation();
        }
    }

    /**
     * An {@code Enum} used to define the possible differential expression in multi-species file
     * types to be generated.
     * <ul>
     * <li>{@code MULTI_DIFF_EXPR_ANATOMY_SIMPLE}:
     *          differential expression in multi-species based on comparison of several anatomical 
     *          entities at a same (broad) developmental stage, in a simple download file.
     * <li>{@code MULTI_DIFF_EXPR_ANATOMY_COMPLETE}:
     *          differential expression in multi-species based on comparison of several anatomical 
     *          entities at a same (broad) developmental stage, in a complete download file.
     * <li>{@code MULTI_DIFF_EXPR_DEVELOPMENT_SIMPLE}:
     *          differential expression in multi-species based on comparison of a same anatomical 
     *          entity at different developmental stages, in a simple download file.
     * <li>{@code MULTI_DIFF_EXPR_DEVELOPMENT_COMPLETE}:    
     *          differential expression in multi-species based on comparison of a same anatomical  
     *          entity at different developmental stages, in a complete download file
     * </ul>
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    //XXX: alternatively, if you use the Bean principle, you could simply use different bean types, 
    //so that you don't need this Enum
    public enum MultiSpeciesDiffExprFileType implements DiffExprFileType {
        MULTI_DIFF_EXPR_ANATOMY_SIMPLE(
                "multi-diffexpr-anatomy-simple", true, ComparisonFactor.ANATOMY), 
        MULTI_DIFF_EXPR_ANATOMY_COMPLETE(
                "multi-diffexpr-anatomy-complete", false, ComparisonFactor.ANATOMY),
        MULTI_DIFF_EXPR_DEVELOPMENT_SIMPLE(
                "multi-diffexpr-development-simple", true, ComparisonFactor.DEVELOPMENT), 
        MULTI_DIFF_EXPR_DEVELOPMENT_COMPLETE(
                "multi-diffexpr-development-complete", false, ComparisonFactor.DEVELOPMENT);
    
        /**
         * A {@code String} that can be used to generate names of files of this type.
         */
        private final String stringRepresentation;
        
        /**
         * A {@code boolean} defining whether this {@code MultiSpDiffExprFileType} is a simple 
         * file type
         */
        private final boolean simpleFileType;
    
        /**
         * A {@code ComparisonFactor} defining what is the compared experimental factor that 
         * generated the differential expression calls.
         */
        //XXX: I find it a bit weird to use the ComparisonFactor of DiffExpressionCallTO at this point, 
        //because it is not a class related to a DAO...
        private final ComparisonFactor comparisonFactor;
    
        /**
         * Constructor providing the {@code String} representation of this 
         * {@code MultiSpDiffExprFileType}, a {@code boolean} defining whether this 
         * {@code MultiSpDiffExprFileType} is a simple file type, and a 
         * {@code ComparisonFactor} defining what is the experimental factor compared 
         * that generated the differential expression calls.
         */
        private MultiSpeciesDiffExprFileType(String stringRepresentation, boolean simpleFileType,
                ComparisonFactor comparisonFactor) {
            this.stringRepresentation = stringRepresentation;
            this.simpleFileType = simpleFileType;
            this.comparisonFactor = comparisonFactor;
        }
    
        @Override
        public String getStringRepresentation() {
            return this.stringRepresentation;
        }
    
        @Override
        public boolean isSimpleFileType() {
            return this.simpleFileType;
        }
        
        @Override
        public ComparisonFactor getComparisonFactor() {
            return this.comparisonFactor;
        }
    
        @Override
        public String toString() {
            return this.getStringRepresentation();
        }
    }

    /**
     * Main method to trigger the generate multi-species differential expression TSV download files 
     * (simple and advanced) from Bgee database. Parameters that must be provided in order in 
     * {@code args} are: 
     * <ol>
     * <li>a {@code Map} where keys are {@code String}s that are names given 
     *     to groups of species, the associated value being a {@code Set} of {@code String}s 
     *     that are the IDs of the species composing the group. Entries of the {@code Map} 
     *     must be separated by {@link CommandRunner#LIST_SEPARATOR}, keys must be  
     *     separated from their associated value by {@link CommandRunner#KEY_VALUE_SEPARATOR}, 
     *     values must be separated using {@link CommandRunner#VALUE_SEPARATOR}, see 
     *     {@link org.bgee.pipeline.CommandRunner#parseMapArgument(String)}
     * <li>a list of files types that will be generated ('multi-diffexpr-anatomy-simple' for 
     *     {@link MultiSpeciesDiffExprFileType MULTI_DIFF_EXPR_ANATOMY_SIMPLE}, 
     *     'multi-diffexpr-anatomy-complete' for 
     *     {@link MultiSpeciesDiffExprFileType MULTI_DIFF_EXPR_ANATOMY_COMPLETE}, 
     *     'multi-diffexpr-development-simple' for 
     *     {@link MultiSpeciesDiffExprFileType MULTI_DIFF_EXPR_DEVELOPMENT_SIMPLE}, and 
     *     'multi-diffexpr-development-complete' for 
     *     {@link MultiSpeciesDiffExprFileType MULTI_DIFF_EXPR_DEVELOPMENT_COMPLETE}), separated by the 
     *     {@code String} {@link CommandRunner#LIST_SEPARATOR}. If an empty list is provided 
     *     (see {@link CommandRunner#EMPTY_LIST}), all possible file types will be generated.
     * <li>the directory path that will be used to generate download files. 
     * </ol>
     * 
     * @param args  An {@code Array} of {@code String}s containing the requested parameters.
     * @throws IllegalArgumentException If incorrect parameters were provided.
     * @throws IOException              If an error occurred while trying to write generated files.
     */
    //TODO: in all other classes, the first argument is a command to trigger the proper action, 
    //we should keep the same pattern
    public static void main(String[] args) throws IllegalArgumentException, IOException {
        log.entry((Object[]) args);
    
        int expectedArgLength = 3;
        if (args.length != expectedArgLength) {
            throw log.throwing(new IllegalArgumentException(
                    "Incorrect number of arguments provided, expected " + 
                    expectedArgLength + " arguments, " + args.length + " provided."));
        }
        GenerateMultiSpeciesDiffExprFile generator = new GenerateMultiSpeciesDiffExprFile(
                CommandRunner.parseMapArgument(args[0]),
                GenerateDownloadFile.convertToFileTypes(
                    CommandRunner.parseListArgument(args[1]), MultiSpeciesDiffExprFileType.class),
                args[2]);
        generator.generateMultiSpeciesDiffExprFiles();
        log.exit();
    }

    /**
     * A {@code Map} where keys are {@code String}s that are names given to a group of species, 
     * the associated values being {@code Set}s of {@code String}s corresponding to 
     * species IDs belonging to the group.
     */
    private Map<String, Set<String>> providedGroups;
    
    /**
     * Default constructor. 
     */
    //suppress warning as this default constructor should not be used.
    @SuppressWarnings("unused")
    private GenerateMultiSpeciesDiffExprFile() {
        this(null, null, null);
    }
    /**
     * Constructor providing parameters to generate files, using the default {@code DAOManager}.
     * 
     * @param providedGroups    A {@code Map} where keys are {@code String}s that are names 
     *                          given to groups of species, the associated value being 
     *                          a {@code Set} of {@code String}s that are the IDs 
     *                          of the species composing the group.
     * @param fileTypes         A {@code Set} of {@code MultiSpDiffExprFileType}s that are the types
     *                          of files we want to generate. If {@code null} or empty, 
     *                          all {@code MultiSpDiffExprFileType}s are generated.
     * @param directory         A {@code String} that is the directory where to store files.
     * @throws IllegalArgumentException If {@code directory} is {@code null} or blank.
     */
    public GenerateMultiSpeciesDiffExprFile(Map<String, Set<String>> providedGroups, 
            Set<MultiSpeciesDiffExprFileType> fileTypes, String directory) 
                    throws IllegalArgumentException {
        this(null, providedGroups, fileTypes, directory);
    }

    /**
     * Constructor providing parameters to generate files, and the {@code MySQLDAOManager} that will  
     * be used by this object to perform queries to the database. This is useful for unit testing.
     * 
     * @param manager           The {@code MySQLDAOManager} to use.
     * @param providedGroups    A {@code Map} where keys are {@code String}s that are names 
     *                          given to groups of species, the associated value being 
     *                          a {@code Set} of {@code String}s that are the IDs 
     *                          of the species composing the group.
     * @param fileTypes         A {@code Set} of {@code MultiSpDiffExprFileType}s that are the types
     *                          of files we want to generate. If {@code null} or empty, 
     *                          all {@code MultiSpDiffExprFileType}s are generated.
     * @param directory         A {@code String} that is the directory where to store files.
     * @throws IllegalArgumentException If {@code directory} is {@code null} or blank.
     */
    public GenerateMultiSpeciesDiffExprFile(MySQLDAOManager manager, 
            Map<String, Set<String>> providedGroups, Set<MultiSpeciesDiffExprFileType> fileTypes, 
            String directory) throws IllegalArgumentException {
        // We do not use species IDs global variable as for single species files.
        // TODO: Correct class schema of all download files to avoid to set null 
        // when calling super constructor for species IDs
        super(manager, null, fileTypes, directory);

        if (providedGroups == null || providedGroups.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("No group is provided"));
        }
        this.providedGroups = providedGroups;
    }
    
    /**
     * Generate multi-species differential expression files, for the types defined by 
     * {@code fileTypes}, for groups defined by {@code providedGroups}, in the directory 
     * {@code directory}.
     * 
     * @throws IllegalArgumentException If no species ID or taxon ID is provided.
     * @throws IOException              If an error occurred while trying to write generated files.
     */
    //TODO: re-write javadoc, either by specifying that thee parameters are provided at instantiation, 
    //or by pointing to public getters
    public void generateMultiSpeciesDiffExprFiles() throws IOException {
        log.entry();
        log.info("Start generating multi-species diff. expression files {} with parameters {} in the directory {}",
                this.fileTypes, this.providedGroups, this.directory);

        // If no file types are given by user, we set all file types
        if (this.fileTypes == null || this.fileTypes.isEmpty()) {
            this.fileTypes = EnumSet.allOf(MultiSpeciesDiffExprFileType.class);
        }
        
        // We retrieve all CIO so it's common to all groups
        Map<String, CIOStatementTO> cioNamesByIds = 
                BgeeDBUtils.getCIOStatementsByIds(this.getCIOStatementDAO());
        
        for (Entry<String, Set<String>> currentGroup : this.providedGroups.entrySet()) {
            Set<String> setSpecies = currentGroup.getValue();
            if (setSpecies == null || setSpecies.isEmpty()) {
                throw log.throwing(new IllegalArgumentException("No species ID is provided"));
            }
            //Validate provided species, and retrieve species names
            Map<String, String> speciesNamesByIds = 
                    this.checkAndGetLatinNamesBySpeciesIds(setSpecies);
            
            String currentPrefix = currentGroup.getKey();
            String taxonId = this.getLeastCommonAncestor(setSpecies);
            
            // Retrieve gene TOs, stage names, anat. entity names, and cio names 
            // for all species
            this.getGeneDAO().setAttributes(GeneDAO.Attribute.ID, GeneDAO.Attribute.NAME, 
                    GeneDAO.Attribute.OMA_PARENT_NODE_ID, GeneDAO.Attribute.SPECIES_ID);
            Map<String, GeneTO> geneTOByIds = 
                    BgeeDBUtils.getGeneTOsByIds(setSpecies, this.getGeneDAO());
            Map<String, String> stageNamesByIds = 
                    BgeeDBUtils.getStageNamesByIds(setSpecies, this.getStageDAO());
            Map<String, String> anatEntityNamesByIds = 
                    BgeeDBUtils.getAnatEntityNamesByIds(setSpecies, this.getAnatEntityDAO());

            // Generate multi-species differential expression files
            log.info("Start generating of multi-species diff. expression files for the group {} " +
                    "with the species {} and the ancestral taxon ID {}...", 
                    currentPrefix, speciesNamesByIds.values(), taxonId);

            try {
                //XXX: maybe all the xxxByIds could be stored in class attributes, 
                //to simplify this method signature
                this.generateMultiSpeciesDiffExprFilesForOneGroup(currentPrefix, taxonId, 
                        speciesNamesByIds, geneTOByIds, stageNamesByIds, anatEntityNamesByIds, 
                        cioNamesByIds);
            } finally {
                // Release resources after each group. 
                this.getManager().releaseResources();
            }
            
            log.info("Done generating of multi-species diff. expression files for the group {}.", 
                    currentGroup);
        }
        log.exit();
    }

    /**
     * Generate multi-species differential expression files, for the types defined by 
     * {@code fileTypes}, for species defined by {@code speciesNamesByIds} with ancestral taxon 
     * defined by {@code taxonId}, in the directory {@code directory} provided at instantiation.
     *
     * @param groupName             A {@code String} that is the group name.
     * @param taxonId               A {@code String} that is the ancestral taxon.
     * @param speciesNamesByIds     A {@code Map} where keys are {@code String}s corresponding 
     *                              to species IDs, the associated values being {@code String}s 
     *                              corresponding to species names. 
     * @param geneTOsByIds          A {@code Map} where keys are {@code String}s corresponding  
     *                              to gene IDs, the associated values being {@code GeneTO}s 
     *                              corresponding to gene TOs. 
     * @param stageNamesByIds       A {@code Map} where keys are {@code String}s corresponding  
     *                              to stage IDs, the associated values being {@code String}s 
     *                              corresponding to stage names. 
     * @param anatEntityNamesByIds  A {@code Map} where keys are {@code String}s corresponding  
     *                              to anatomical entity IDs, the associated values being 
     *                              {@code String}s corresponding to anatomical entity names.
     * @param cioStatementsByIds    A {@code Map} where keys are {@code String}s corresponding  
     *                              to CIO IDs, the associated values being {@code CIOStatementTO}s 
     *                              corresponding to CIO TOs. 
     * @throws IOException              If an error occurred while trying to write generated files.
     * @throws IllegalArgumentException If all file types do not have the same comparison factor
     *                                  or file types defined with development comparison factor.
     */
    private void generateMultiSpeciesDiffExprFilesForOneGroup(String groupName, String taxonId, 
            Map<String, String> speciesNamesByIds, Map<String, GeneTO> geneTOsByIds, 
            Map<String, String> stageNamesByIds, Map<String, String> anatEntityNamesByIds, 
            Map<String, CIOStatementTO> cioStatementsByIds) 
                    throws IOException, IllegalArgumentException {
        log.entry(groupName, taxonId, speciesNamesByIds, 
                geneTOsByIds, stageNamesByIds, anatEntityNamesByIds, cioStatementsByIds);

        Set<String> speciesFilter = speciesNamesByIds.keySet();

        log.debug("Start generating multi-species differential expression files for: " + 
                "groupName={}, taxon ID={}, species IDs={} and file types {}...", 
                groupName, taxonId, speciesFilter, this.fileTypes);

        // We check that all file types have the same comparison factor and we retrieve it 
        //TODO: accept more than one comparison factor over all possible file types, 
        //if we generate multi-species diff expression files over DEVELOPMENT.
        //the expression query should be performed once per comparison factor
        ComparisonFactor factor = null;
        for (FileType fileType: this.fileTypes) {
            if (factor == null) {
                factor = ((MultiSpeciesDiffExprFileType)fileType).getComparisonFactor(); 
            } else if (!((MultiSpeciesDiffExprFileType)fileType).getComparisonFactor().equals(factor)) {
                throw log.throwing(new IllegalArgumentException(
                        "All file types do not have the same comparison factor: " + this.fileTypes));
            }
        }
        assert factor != null && factor.equals(ComparisonFactor.ANATOMY);

        //****************************
        // PRODUCE AND WRITE DATA
        //****************************
        log.trace("Start generating and writing file content");

        // Now, we write all requested files at once. This way, we will generate the data only once, 
        // and we will not have to store them in memory.
        
        // First we allow to store file names, writers, etc, associated to a FileType, 
        // for the catch and finally clauses.
        Map<MultiSpeciesDiffExprFileType, String> generatedFileNames = 
                new HashMap<MultiSpeciesDiffExprFileType, String>();

        // We will write results in temporary files that we will rename at the end
        // if everything is correct
        String tmpExtension = ".tmp";

        // In order to close all writers in a finally clause.
        Map<MultiSpeciesDiffExprFileType, ICsvDozerBeanWriter> writersUsed = 
                new HashMap<MultiSpeciesDiffExprFileType, ICsvDozerBeanWriter>();
        try {
            //**************************
            // OPEN FILES, CREATE WRITERS, WRITE HEADERS
            //**************************
            Map<MultiSpeciesDiffExprFileType, CellProcessor[]> processors = 
                    new HashMap<MultiSpeciesDiffExprFileType, CellProcessor[]>();
            Map<MultiSpeciesDiffExprFileType, String[]> headers = 
                    new HashMap<MultiSpeciesDiffExprFileType, String[]>();

            // Get ordered species names
            List<String> orderedSpeciesNames = this.getSpeciesNamesOrderBySpeciesId(
                    speciesFilter, speciesNamesByIds);
            
            for (FileType fileType : this.fileTypes) {
                MultiSpeciesDiffExprFileType currentFileType = (MultiSpeciesDiffExprFileType) fileType;
                if (currentFileType.getComparisonFactor().equals(ComparisonFactor.DEVELOPMENT)) {
                    throw log.throwing(new IllegalArgumentException(
                            "Development comparison factor is currently not supported."));
                }

                String[] fileTypeHeaders = this.generateHeader(currentFileType, orderedSpeciesNames);
                headers.put(currentFileType, fileTypeHeaders);
                
                CellProcessor[] fileTypeProcessors = this.generateCellProcessors(
                        currentFileType, fileTypeHeaders);
                processors.put(currentFileType, fileTypeProcessors);
                
                // Create file name
                String fileName = groupName + "_" +
                        currentFileType.getStringRepresentation() + EXTENSION;
                generatedFileNames.put(currentFileType, fileName);

                // write in temp file
                File file = new File(this.directory, fileName + tmpExtension);
                // override any existing file
                if (file.exists()) {
                    file.delete();
                }
                
                // create and configure writer
                ICsvDozerBeanWriter beanWriter = new CsvDozerBeanWriter(new FileWriter(file),
                        Utils.TSVCOMMENTED);
                // configure the mapping from the fields to the CSV columns
                if (currentFileType.isSimpleFileType()) {
                    beanWriter.configureBeanMapping(MultiSpeciesSimpleDiffExprFileBean.class, 
                            this.generateFieldMapping(currentFileType, fileTypeHeaders));
                } else {
                    beanWriter.configureBeanMapping(MultiSpeciesCompleteDiffExprFileBean.class, 
                            this.generateFieldMapping(currentFileType, fileTypeHeaders));
                }
                //  Write header
                beanWriter.writeHeader(fileTypeHeaders);
                log.debug("Write header: {}", Arrays.toString(fileTypeHeaders));
                
                writersUsed.put(currentFileType, beanWriter);
            }
            
            //********************************
            // RETRIEVE DATA FROM DATA SOURCE
            //********************************
            log.trace("Start retrieving secondary data...");
            
            // Get homologous genes: Gene ID -> OMA node Id and OMA node ID -> Gene IDs
            Map<String, String> mapGeneOMANode = 
                    this.getMappingGeneIdOMANodeId(taxonId, speciesFilter);
            Map<String, Set<String>> mapOMANodeGene = 
                    this.getMappingOMANodeIdGeneIDs(mapGeneOMANode);

            // Get comparable stages: Stage ID -> Stage group and Stage group -> Stage IDs
            List<Map<String, List<String>>> mapStageGroup = 
                    this.getComparableStages(taxonId, speciesFilter);
            Map<String, List<String>> mapStageIdToStageGroup = mapStageGroup.get(0);
            Map<String, List<String>> mapStageGroupToStageId = mapStageGroup.get(1);

            // Get summary similarity annotations with CIO IDs: Summary annotation ID -> CIO ID
            Map<String, String> mapSumSimCIO = this.getSummarySimilarityAnnotations(taxonId);

            // Get relations between similarity annotations and anatomical entities:
            // Summary annotation ID -> Anat. entities and Anat. entity -> Summary annotation ID 
            List<Map<String, List<String>>> simAnnotToAnatEntity = 
                    this.getSimAnnotToAnatEntities(taxonId, speciesFilter);
            Map<String, List<String>> mapSimAnnotToAnatEntities = simAnnotToAnatEntity.get(0);
            Map<String, List<String>> mapAnatEntityToSimAnnot = simAnnotToAnatEntity.get(1);
                    
            log.trace("Done retrieving secondary data.");
            
            log.trace("Start retrieving and writing differential expression data...");

            // Load differential expression calls order by OMA node ID
            try (DiffExpressionCallTOResultSet diffExprRs = 
                    this.getDiffExpressionCallsOrderByOMANodeId(taxonId, speciesFilter, factor)) {

                // ****************************
                // WRITE ROWS
                // ****************************
                //we will consider all together the calls mapped to a same OMA group. 
                //This Set will store all calls from the iterated OMA node.
                Set<DiffExpressionCallTO> omaGroupCalls = new HashSet<DiffExpressionCallTO>();
                String previousOMANodeId = null;
                //we iterate the ResultSet, then we do a last iteration after the last TO is 
                //retrieved, to properly group all the calls.
                boolean doIteration = true;
                while (doIteration) {
                    doIteration = diffExprRs.next();
                    DiffExpressionCallTO currentTO = null;
                    String currentOMANodeId = null;
                    if (doIteration) {
                        currentTO = diffExprRs.getTO();
                        currentOMANodeId = mapGeneOMANode.get(currentTO.getGeneId());
                    }
                    //if the OMA group changes, or if it is the latest iteration
                    if (!doIteration || //doIteration is false for the latest iteration, 
                                        // AFTER retrieving the last TO
                                        //(ResultSet.isAfterLast would return true)
                          (previousOMANodeId != null && !previousOMANodeId.equals(currentOMANodeId))) {
                        
                        assert (doIteration && currentOMANodeId != null && currentTO != null) || 
                        (!doIteration && currentOMANodeId == null && currentTO == null);
                        
                        //the calls are supposed to be ordered by ascending OMA group ID
                        if (currentOMANodeId != null && 
                                currentOMANodeId.compareTo(previousOMANodeId) <= 0) {
                            throw log.throwing(new IllegalStateException("The expression calls "
                                    + "were not retrieved by OMA group ID ascending order, which "
                                    + "is mandatory for proper generation of multi-species data."));
                        }

                        if (previousOMANodeId == null) {
                            //if we reach this code block, it means there were no results at all 
                            //retrieved from the ResultSet. This is not formally an error, 
                            //maybe there is no homologous genes with expression 
                            //for the selected species...
                            log.warn("No Expression data retrieved for group {}, taxon LCA {}, composed of species {}", 
                                    groupName, taxonId, speciesFilter);
                            break;
                        }

                        //We filter and write rows for one OMA node ID.
                        this.filterAndWriteOMANodeRows(geneTOsByIds, stageNamesByIds, 
                                anatEntityNamesByIds, cioStatementsByIds, speciesNamesByIds, 
                                writersUsed, processors, previousOMANodeId, 
                                mapOMANodeGene.get(previousOMANodeId), mapGeneOMANode, 
                                omaGroupCalls, mapSumSimCIO, mapSimAnnotToAnatEntities, 
                                mapStageGroupToStageId, mapAnatEntityToSimAnnot, 
                                mapStageIdToStageGroup);

                        // We clear the set containing TOs with the previous OMA Node ID
                        omaGroupCalls.clear();
                    }
                    if (doIteration) {
                        // We add the current TOs to the group
                        omaGroupCalls.add(currentTO);
                        // We store the current OMA Node ID to be compare with the next one
                        previousOMANodeId = currentOMANodeId;
                    }
                }
            }
            log.trace("Done retrieving and writing differential expression data.");

        } catch (Exception e) {
            this.deleteTempFiles(generatedFileNames, tmpExtension);
            throw e;
        } finally {
            for (ICsvDozerBeanWriter writer : writersUsed.values()) {
                writer.close();
            }
        }

        // Now, if everything went fine, we rename the temporary files
        this.renameTempFiles(generatedFileNames, tmpExtension);

        log.exit();
    }

    /**
     * Retrieve from the data source the least common ancestor of the provided species. 
     *
     * @param speciesIds    A {@code Set} of {@code String}s that are the IDs of species
     *                      allowing to retrieve the LCA.
     * @return              A {@code String} that is the ID of least common ancestor of 
     *                      the provided species.
     * @throws DAOException If an error occurred while getting the data from the Bgee data source.
     */
    private String getLeastCommonAncestor(Set<String> speciesIds) throws DAOException {
        log.entry(speciesIds);
        
        log.debug("Start retrieving least common ancestor for the species IDs {}...", speciesIds);
    
        TaxonDAO dao = this.getTaxonDAO();
        dao.setAttributes(TaxonDAO.Attribute.ID);
    
        String lca = null;
        // includeAncestors is false to retrieved only the LCA and not all ancestors of the LCA.
        try (TaxonTOResultSet rs = dao.getLeastCommonAncestor(speciesIds, false)) {
            boolean isFirst = true;
            while (rs.next()) {
                if (isFirst) {
                    lca = rs.getTO().getId();
                    isFirst = false;
                } else {
                    throw log.throwing(new IllegalStateException("Severals LCA returns"));
                }
            }
        }
    
        log.debug("Done retrieving least common ancestor, the taxon found is {}", lca);
    
        return log.exit(lca);
    }

    /**
     * Retrieves mapping from gene IDs to OMA node IDs for the requested species,
     * present into the Bgee database.
     * 
     * @param taxonId       A {@code String} that is the ID of the common ancestor taxon
     *                      we want to into account.
     * @param speciesIds    A {@code Set} of {@code String}s that are the IDs of species
     *                      allowing to retrieve the mapping.
     * @return              A {@code Map} where keys are {@code String}s that are gene IDs, the  
     *                      associated values being {@code String}s corresponding to OMA group ID 
     *                      of the given taxon ID.
     * @throws DAOException If an error occurred while getting the data from the Bgee data source.
     * @throws IllegalStateException If we retrieve several LCA.
     */
    private Map<String, String> getMappingGeneIdOMANodeId(String taxonId, Set<String> speciesIds)
            throws DAOException, IllegalArgumentException {
        log.entry(taxonId, speciesIds);
    
        log.debug("Start retrieving homologous genes for the taxon ID {}...", taxonId);
    
        HierarchicalGroupDAO dao = this.getHierarchicalGroupDAO();
        // setAttributes methods has no effect on attributes retrieved  
        
        Map<String, String> mapping = new HashMap<String, String>();
        try (HierarchicalGroupToGeneTOResultSet rs = dao.getGroupToGene(taxonId, speciesIds)) {
            boolean hasResult = false;
            while (rs.next()) {
                HierarchicalGroupToGeneTO to = rs.getTO();
                mapping.put(to.getGeneId(), to.getGroupId());
                hasResult = true;
            }
            if (!hasResult) {
                throw log.throwing(new IllegalStateException("No retrieved homologous genes"));
            }
        }
    
        log.debug("Done retrieving homologous genes, {} genes found", mapping.size());
    
        return log.exit(mapping);
    }

    /**
     * Retrieves mapping from OMA node IDs to gene IDs for the provided {@code mapGeneOMANode}.
     * 
     * @param mapGeneOMANode    A {@code Map} where keys are {@code String}s that are gene IDs, the  
     *                          associated values being {@code String}s corresponding to OMA node
     *                          ID.
     * @return                  A {@code Map} where keys are {@code String}s that are OMA node IDs, 
     *                          the  associated values being {@code List} of {@code String}s 
     *                          corresponding to gene IDs.
     */
    private Map<String, Set<String>> getMappingOMANodeIdGeneIDs(Map<String, String> mapGeneOMANode) {
        log.entry(mapGeneOMANode);
        
        Map<String, Set<String>> mapOMANodeGene = new HashMap<String, Set<String>>(); 
        for (Entry<String, String> entry : mapGeneOMANode.entrySet()) {
            String OMANodeId = entry.getValue();
            Set<String> geneIds = mapOMANodeGene.get(OMANodeId);
            if (geneIds == null) {
                geneIds = new HashSet<String>();
                mapOMANodeGene.put(OMANodeId, geneIds);
            }
            geneIds.add(entry.getKey());
        }
        
        return log.exit(mapOMANodeGene);
    }
    
    /**
     * Retrieves comparable stages for the requested species in the requested taxon,
     * present into the Bgee database.
     *
     * @param taxonId       A {@code String} that is the ID of the common ancestor taxon 
     *                      we want to into account. 
     * @param speciesIds    A {@code Set} of {@code String}s that are the IDs of species
     *                      allowing to filter the comparable stages to use.
     * @return              the {@code List} of {@code Map}s. The first {@code Map} is the 
     *                      {@code Map} where keys are {@code String}s that are stage IDs, the 
     *                      associated values being {@code Set} of {@code String}s corresponding to 
     *                      stage group IDs. And the second {@code Map} is the {@code Map} where 
     *                      keys are {@code String}s that are stage group IDs, the associated values
     *                      being {@code Set} of {@code String}s corresponding to stage IDs.
     * @throws DAOException If an error occurred while getting the data from the Bgee data source.
     * @throws IllegalStateException IF an error is detected in data source.
     */
    //TODO: this is really an ugly design :p 
    //Implements a first method retrieving information from database, then two other methods 
    //to generate the proper mappings you need. 
    private List<Map<String, List<String>>> getComparableStages(
            String taxonId, Set<String> speciesIds) throws DAOException, IllegalStateException {
        log.entry(taxonId, speciesIds);
        
        log.debug("Start retrieving comparable stages for the taxon ID {} and the species IDs {}...", 
                taxonId, speciesIds);
        
       StageGroupingDAO dao = this.getStageGroupingDAO();
       // setAttributes methods has no effect on attributes retrieved  
        
       Map<String, List<String>> mappingStageIdToStageGroup = new HashMap<String, List<String>>();
       Map<String, List<String>> mappingStageGroupToStageId = new HashMap<String, List<String>>();
        try (GroupToStageTOResultSet rs = dao.getGroupToStage(taxonId, speciesIds)) {
            while (rs.next()) {
                GroupToStageTO to = rs.getTO();
    
                if (mappingStageIdToStageGroup.containsKey(to.getStageId())) {
                    throw log.throwing(new IllegalStateException(
                            "One stage ID souldn't be reported to severals stage groups"));
                }
                mappingStageIdToStageGroup.put(to.getStageId(), Arrays.asList(to.getGroupId()));
    
                List<String> stageIds = mappingStageGroupToStageId.get(to.getGroupId());
                if (stageIds == null) {
                    stageIds = new ArrayList<String>();
                    mappingStageGroupToStageId.put(to.getGroupId(), stageIds);
                }
                stageIds.add(to.getStageId());
            }
        }
    
        log.debug("Done retrieving relation from stage ID to " + 
                "stage group, {} found", mappingStageIdToStageGroup.size());
        log.debug("Done retrieving relation from stage group to " + 
                "stage IDs, {} found", mappingStageGroupToStageId.size());
    
        return log.exit(Arrays.asList(mappingStageIdToStageGroup, mappingStageGroupToStageId));
    }

    /**
     * Retrieves summary similarity annotations with the CIO ID for the requested taxon,
     * present into the Bgee database.
     *
     * @param taxonId       A {@code String} that is the ID of the common ancestor taxon 
     *                      we want to take into account. 
     * @return              A {@code Map} where keys are {@code String}s corresponding to summary 
     *                      similarity annotation IDs, the associated values being {@code String}s 
     *                      corresponding to CIO IDs.
     * @throws DAOException If an error occurred while getting the data from the Bgee data source.
     */
    private Map<String, String> getSummarySimilarityAnnotations(String taxonId) throws DAOException {
        log.entry(taxonId);
        
        log.debug("Start retrieving summary similarity annotations for the taxon ID {}...", taxonId);
    
        SummarySimilarityAnnotationDAO dao = this.getSummarySimilarityAnnotationDAO();
        dao.setAttributes(SummarySimilarityAnnotationDAO.Attribute.ID, 
                SummarySimilarityAnnotationDAO.Attribute.CIO_ID);
    
        Map<String, String> mapping = new HashMap<String, String>();
        try (SummarySimilarityAnnotationTOResultSet rs = dao.getSummarySimilarityAnnotations(taxonId)) {
            while (rs.next()) {
                SummarySimilarityAnnotationTO to = rs.getTO();
                mapping.put(to.getId(), to.getCIOId());
            }
        }
    
        log.debug("Done retrieving summary similarity annotations, {} found", mapping.size());
    
        return log.exit(mapping);
    }

    /**
     * Retrieves relation between summary similarity annotation and anatomical entity 
     * for the provided taxon ID and species IDs. 
     *
     * @param taxonId       A {@code String} that is the ID of the common ancestor taxon 
     *                      we want to into account. 
     * @param speciesIds    A {@code Set} of {@code String}s that are the IDs of species
     *                      allowing to filter the comparable stages to use.
     * @return              the {@code List} of {@code Map}s. The first {@code Map} is the 
     *                      {@code Map} where keys are {@code String}s that are summary similarity 
     *                      annotation IDs, the associated values being {@code Set} of 
     *                      {@code String}s corresponding to anat. entity IDs. And the second 
     *                      {@code Map} is the {@code Map} where keys are {@code String}s that are 
     *                      anat. entity IDs, the associated values being {@code Set} of 
     *                      {@code String}s corresponding to summary similarity annotation IDs.  
     * @throws DAOException If an error occurred while getting the data from the Bgee data source.
     */
    //TODO: same ugliness than for getComparableStages, to fix.
    private List<Map<String, List<String>>> getSimAnnotToAnatEntities(
            String taxonId, Set<String> speciesIds) throws DAOException {
        log.entry(taxonId, speciesIds);
    
        log.debug("Start retrieving relation between summary similarity annotation and " + 
                "anatomical entity for the taxon ID {} and species IDs {}...", 
                taxonId, speciesIds);
    
        SummarySimilarityAnnotationDAO dao = this.getSummarySimilarityAnnotationDAO();
        // setAttributes methods has no effect on attributes retrieved  
    
        Map<String, List<String>> mappingSimAnnotToAnatEntity = new HashMap<String, List<String>>();
        Map<String, List<String>> mappingAnatEntityToSimAnnot = new HashMap<String, List<String>>();
        //note that we retrieve all organs, even those not existing in all species
        try (SimAnnotToAnatEntityTOResultSet rs = dao.getSimAnnotToAnatEntity(taxonId, null)) {
            while (rs.next()) {
                SimAnnotToAnatEntityTO to = rs.getTO();
                
                List<String> sumAnatEntIds = mappingSimAnnotToAnatEntity.get(
                        to.getSummarySimilarityAnnotationId());
                if (sumAnatEntIds == null) {
                    sumAnatEntIds = new ArrayList<String>();
                    mappingSimAnnotToAnatEntity.put(to.getSummarySimilarityAnnotationId(), 
                            sumAnatEntIds);
                }
                sumAnatEntIds.add(to.getAnatEntityId());
                
                if (mappingAnatEntityToSimAnnot.containsKey(to.getAnatEntityId())) {
                    throw log.throwing(new IllegalStateException(
                    "An anatomical entity sould not be reported to severals similarity groups"));
                }
                mappingAnatEntityToSimAnnot.put(to.getAnatEntityId(), 
                        Arrays.asList(to.getSummarySimilarityAnnotationId()));
    
            }
        }
    
        log.debug("Done retrieving relation from summary similarity annotation to " + 
                "anatomical entities, {} found", mappingSimAnnotToAnatEntity.size());
        log.debug("Done retrieving relation from anatomical entity to " + 
                "summary similarity annotations, {} found", mappingAnatEntityToSimAnnot.size());
    
        return log.exit(Arrays.asList(mappingSimAnnotToAnatEntity, mappingAnatEntityToSimAnnot));
    }

    /**
     * Retrieve differential expression calls for genes homologous in the provided taxon ID, 
     * order by groups of homologous genes.
     * 
     * @param taxonId       A {@code String} that is the ID of the common ancestor taxon
     *                      allowing to filter the calls to retrieve.
     * @param speciesIds    A {@code Set} of {@code String}s that are the IDs of species 
     *                      allowing to filter the calls to retrieve.
     * @param factor        A {@code ComparisonFactor}s that is the comparison factor 
     *                      allowing to filter the calls to retrieve.
     * @return              A {@code List} of {@code DiffExpressionCallTO}s that are 
     *                      all differential expression calls for the requested species, in
     *                      in requested taxon. 
     * @throws DAOException If an error occurred while getting the data from the Bgee data source.
     */
    private DiffExpressionCallTOResultSet getDiffExpressionCallsOrderByOMANodeId(
            String taxonId, Set<String> speciesIds, ComparisonFactor factor) throws DAOException {
        log.entry(taxonId, speciesIds, factor);
    
        log.debug("Start retrieving differential expression calls (factor {}) for the taxon ID {} and species IDs {}...", 
                factor, taxonId, speciesIds);
        
        DiffExpressionCallDAO dao = this.getDiffExpressionCallDAO();
        // do not retrieve the internal diff. expression IDs
        dao.setAttributes(EnumSet.complementOf(EnumSet.of(DiffExpressionCallDAO.Attribute.ID)));
    
        DiffExpressionCallParams params = new DiffExpressionCallParams();
        params.addAllSpeciesIds(speciesIds);
        params.setComparisonFactor(factor);
    
        DiffExpressionCallTOResultSet rs = 
                dao.getOrderedHomologousGenesDiffExpressionCalls(taxonId, params);
    
        log.debug("Done retrieving differential expression calls");
    
        return log.exit(rs);
    }

    /**
     * Return species names in the alphabetical order of provided species IDs. 
     *
     * @param speciesIds        A {@code Set} of {@code String}s that are the IDs of species.
     * @param speciesNamesByIds A {@code Map} where keys are {@code String}s corresponding to 
     *                          species IDs, the associated values being {@code String}s 
     *                          corresponding to species names. 
     * @return                  the {@code List} of {@code String}s that are species names in the 
     *                          alphabetical order from the provided species IDs.
     */
    private List<String> getSpeciesNamesOrderBySpeciesId(
            Set<String> speciesIds, Map<String, String> speciesNamesByIds) {
        log.entry(speciesIds, speciesNamesByIds);
        
        List<String> ids = new ArrayList<String>(speciesIds);
        Collections.sort(ids);
        List<String> names = new ArrayList<String>();
        for (String id : ids) {
            names.add(speciesNamesByIds.get(id));
        }
        assert names.size() == speciesIds.size();
        
        return log.exit(names);
    }

    /**
     * Groups provided differential expression calls by condition (summary similarity annotation ID
     * and stage group ID).
     *
     * @param callTOs                   A {@code Set} of {@code DiffExpressionCallTO}s that are 
     *                                  the calls to be grouped.
     * @param mapAnatEntityToSimAnnot   A {@code Map} where keys are {@code String}s that are 
     *                                  anat. entity IDs, the associated values being {@code Set} of 
     *                                  {@code String}s corresponding to summary similarity 
     *                                  annotation IDs. 
     * @param mapStageIdToStageGroup    A {@code Map} where keys are {@code String}s that are stage 
     *                                  IDs, the associated values being {@code Set} of 
     *                                  {@code String}s corresponding to stage group IDs. 
     * @return                          the Map of where keys are {@code MultiSpeciesCondition}s 
     *                                  that are condition, the associated values being 
     *                                  {@code Collection} of {@code DiffExpressionCallTO}s 
     *                                  corresponding to grouped differential expression calls.
     */
    //XXX: maybe we should also provide the mapping gene ID -> OMA group ID, 
    //to check that all calls are from a same OMA group ID?
    private Map<MultiSpeciesCondition, Collection<DiffExpressionCallTO>>
            groupByMultiSpeciesCondition(Collection<DiffExpressionCallTO> groupedCallTOs, 
                    Map<String, List<String>> mapAnatEntityToSimAnnot, 
                    Map<String, List<String>> mapStageIdToStageGroup) {
        log.entry(groupedCallTOs, mapAnatEntityToSimAnnot, mapStageIdToStageGroup);
        
        Map<MultiSpeciesCondition, Collection<DiffExpressionCallTO>> groupedCalls = 
                new HashMap<MultiSpeciesCondition, Collection<DiffExpressionCallTO>>();
        //for sanity check: currently, this class only supports having one call per gene 
        //per multi-species condition. This might change in the future.
        //this Map will store association from a multi-species condition to the genes considered
        Map<MultiSpeciesCondition, Set<String>> condToGeneIds = 
                new HashMap<MultiSpeciesCondition, Set<String>>();
        
        for (DiffExpressionCallTO diffExpressionCallTO : groupedCallTOs) {
            //each anat entity is supposed to be mapped to one and only one group.
            //same for stages.
            assert mapAnatEntityToSimAnnot.get(diffExpressionCallTO.getAnatEntityId()).size() <= 1;
            assert mapStageIdToStageGroup.get(diffExpressionCallTO.getStageId()).size() <= 1;
            String sumSimAnnotId = null;
            if (!mapAnatEntityToSimAnnot.get(diffExpressionCallTO.getAnatEntityId()).isEmpty()) {
                sumSimAnnotId = 
                        mapAnatEntityToSimAnnot.get(diffExpressionCallTO.getAnatEntityId()).get(0);
            }
            String stageGroupId = null;
            if (!mapStageIdToStageGroup.get(diffExpressionCallTO.getStageId()).isEmpty()) {
                stageGroupId = 
                        mapStageIdToStageGroup.get(diffExpressionCallTO.getStageId()).get(0);
            }
            
            //OK, both the anat entity and the stage have a multi-species mapping
            if (sumSimAnnotId != null && stageGroupId != null) {
                MultiSpeciesCondition condition = 
                        new MultiSpeciesCondition(sumSimAnnotId, stageGroupId);
                
                //sanity check, only one call per gene per condition. 
                //this might change in the future.
                Set<String> associatedGeneIds = condToGeneIds.get(condition);
                if (associatedGeneIds == null) {
                    associatedGeneIds = new HashSet<String>();
                    condToGeneIds.put(condition, associatedGeneIds);
                } else if (associatedGeneIds.contains(diffExpressionCallTO.getGeneId())) {
                    throw log.throwing(new IllegalStateException("Several calls were retrieved "
                            + "for a same gene in a same multi-species condition. "
                            + "Condition: " + condition 
                            + " - Gene: " + diffExpressionCallTO.getGeneId() 
                            + " - Iterated call: " + diffExpressionCallTO));
                }
                associatedGeneIds.add(diffExpressionCallTO.getGeneId());
                
                
                Collection<DiffExpressionCallTO> calls = groupedCalls.get(condition);
                if (calls == null) {
                    log.trace("Create new map key: {}", condition);
                    calls = new HashSet<DiffExpressionCallTO>();
                    groupedCalls.put(condition, calls);
                }
                calls.add(diffExpressionCallTO);
            }
        }
        return log.exit(groupedCalls);
    }

    //NOTE: we should filter and write in the same time because filter could be according to file type
    /**
     * Filter provided call group to be written and write them in a file. 
     *
     * @param geneTOsByIds              A {@code Map} where keys are {@code String}s corresponding  
     *                                  to gene IDs, the associated values being {@code GeneTO}s 
     *                                  corresponding to gene TOs. 
     * @param stageNamesByIds           A {@code Map} where keys are {@code String}s corresponding  
     *                                  to stage IDs, the associated values being {@code String}s 
     *                                  corresponding to stage names. 
     * @param anatEntityNamesByIds      A {@code Map} where keys are {@code String}s corresponding  
     *                                  to anatomical entity IDs, the associated values being 
     *                                  {@code String}s corresponding to anatomical entity names.
     * @param cioStatementByIds         A {@code Map} where keys are {@code String}s corresponding  
     *                                  to CIO IDs, the associated values being 
     *                                  {@code CIOStatementTO}s corresponding to CIO TOs.
     * @param speciesNamesByIds         A {@code Map} where keys are {@code String}s corresponding 
     *                                  to species IDs, the associated values being {@code String}s 
     *                                  corresponding to species names. 
     * @param writersUsed               A {@code Map} where keys are {@code MultiSpDiffExprFileType}s 
     *                                  corresponding to which type of file should be generated, the 
     *                                  associated values being {@code ICsvDozerBeanWriter}s 
     *                                  corresponding to the writers.
     * @param processors                A {@code Map} where keys are {@code MultiSpDiffExprFileType}s 
     *                                  corresponding to which type of file should be generated, the 
     *                                  associated values being an {@code Array} of 
     *                                  {@code CellProcessor}s used to process a file.
     * @param omaNodeId                 A {@code String} that is the OMA node ID.
     * @param omaGeneIds                A {@code Set} of {@code String}s that are the gene IDs
     *                                  contains in the provided {@code omaNodeId}.
     * @param mapGeneOMANode            A {@code Map} where keys are {@code String}s corresponding 
     *                                  to gene IDs, the associated values being {@code String}s 
     *                                  corresponding to OMA node IDs.
     * @param calls                     A {@code Collection} of {@code DiffExpressionCallTO}s that 
     *                                  are the differential expression calls to be filtered and 
     *                                  written.
     * @param mapSumSimCIO              A {@code Map} where keys are {@code String}s corresponding 
     *                                  to summary similarity annotation IDs, the associated values 
     *                                  being {@code String}s corresponding to CIO IDs.
     * @param mapSimAnnotToAnatEntities A {@code Map} is the {@code Map} where keys are 
     *                                  {@code String}s that are summary similarity annotation IDs, 
     *                                  the associated values being {@code Set} of {@code String}s 
     *                                  corresponding to anat. entity IDs.
     * @param mapStageGroupToStageId    A {@code Map} is the {@code Map} where keys are 
     *                                  {@code String}s that are stage group IDs, the associated 
     *                                  values being {@code Set} of {@code String}s corresponding 
     *                                  to stage IDs.
     * @param mapAnatEntityToSimAnnot   A {@code Map} where keys are {@code String}s that are 
     *                                  anat. entity IDs, the associated values being {@code Set} of 
     *                                  {@code String}s corresponding to summary similarity 
     *                                  annotation IDs. 
     * @param mapStageIdToStageGroup    A {@code Map} where keys are {@code String}s that are stage 
     *                                  IDs, the associated values being {@code Set} of 
     *                                  {@code String}s corresponding to stage group IDs. 
     * @throws IllegalArgumentException If call data are inconsistent (for instance, without any data).
     * @throws IOException              If an error occurred while trying to write generated files.
     */
    //XXX: I'm not a fan of methods with so many arguments, maybe the mappings 
    //should be stored in class attributes. A good design would be to have *another* class 
    //to hold these params...
    //TODO: do we really need the mapSumSimCIO mapping? It is easy to retrieve it from cioStatementByIds...
    private void filterAndWriteOMANodeRows(Map<String, GeneTO> geneTOsByIds, 
            Map<String, String> stageNamesByIds, Map<String, String> anatEntityNamesByIds, 
            Map<String, CIOStatementTO> cioStatementByIds, Map<String, String> speciesNamesByIds,
            Map<MultiSpeciesDiffExprFileType, ICsvDozerBeanWriter> writersUsed,
            Map<MultiSpeciesDiffExprFileType, CellProcessor[]> processors,
            String omaNodeId, Set<String> omaGeneIds, Map<String, String> mapGeneOMANode,
            Collection<DiffExpressionCallTO> calls,
            Map<String, String> mapSumSimCIO, Map<String, List<String>> mapSimAnnotToAnatEntities,
            Map<String, List<String>> mapStageGroupToStageId,
            Map<String, List<String>> mapAnatEntityToSimAnnot, 
            Map<String, List<String>> mapStageIdToStageGroup)
                    throws IllegalArgumentException, IOException {
        log.entry(geneTOsByIds, stageNamesByIds, anatEntityNamesByIds, cioStatementByIds, 
                speciesNamesByIds, writersUsed, processors, omaNodeId, omaGeneIds, mapGeneOMANode,  
                calls, mapSumSimCIO, mapSimAnnotToAnatEntities, mapStageGroupToStageId, 
                mapAnatEntityToSimAnnot, mapStageIdToStageGroup);
        
        // We group calls (without propagation) by condition 
        // (summary similarity annotation/stage group) 
        Map<MultiSpeciesCondition, Collection<DiffExpressionCallTO>> 
            callsGroupByCondition = this.groupByMultiSpeciesCondition(
                calls, mapAnatEntityToSimAnnot, mapStageIdToStageGroup);
        
        //TODO refactor in a method that takes a set of IDs and mapping between IDs and names 
        List<String> geneIds = new ArrayList<String>(omaGeneIds),
                     geneNames = new ArrayList<String>();
        assert geneIds != null && !geneIds.isEmpty();
        // We sort gene IDs.
        Collections.sort(geneIds);
        // Then, we build gene name list according to ordered gene ID list.
        for (String geneId : geneIds) {
            geneNames.add(geneTOsByIds.get(geneId).getName());
        }

        // We store complete and simple multi-species diff. expression file beans to be able to  
        // write them after the comment line containing gene IDs and names. 
        List<MultiSpeciesCompleteDiffExprFileBean> allCompleteBeans = 
                new ArrayList<MultiSpeciesCompleteDiffExprFileBean>();
        List<MultiSpeciesSimpleDiffExprFileBean> allSimpleBeans = 
                new ArrayList<MultiSpeciesSimpleDiffExprFileBean>();
        
        //TODO: here, it should be possible to have several calls for a same gene
        //in a same multi-species condition; in that case, calls for a same gene 
        //should be "merged".
        for (Entry<MultiSpeciesCondition, Collection<DiffExpressionCallTO>> entry : 
            callsGroupByCondition.entrySet()) {
            
            MultiSpeciesCondition condition = entry.getKey();

            // First, we get some data to be able to build beans 
            String cioId = mapSumSimCIO.get(condition.getSummarySimilarityAnnotationId());
            
            //TODO see todo for gene IDs and names 
            List<String> stageIds = mapStageGroupToStageId.get(condition.getStageGroupId());
            assert stageIds != null && !stageIds.isEmpty();
            //sort IDs for consistent diff between releases
            Collections.sort(stageIds);
            List<String> stageNames = new ArrayList<String>();
            for (String stageId: stageIds) {
                stageNames.add(stageNamesByIds.get(stageId));
            }
            assert stageIds.size() == stageNames.size();
            
            //TODO see todo for gene IDs and names 
            List<String> organIds = 
                    mapSimAnnotToAnatEntities.get(condition.getSummarySimilarityAnnotationId());
            assert organIds != null && !organIds.isEmpty();
            //sort IDs for consistent diff between releases
            Collections.sort(organIds);
            List<String> organNames = new ArrayList<String>();
            for (String entityId: organIds) {
                organNames.add(anatEntityNamesByIds.get(entityId));
            }
            assert organIds.size() == organNames.size();
            
            // Then, we compute data for each species
            
            // We store complete beans for one condition to be able to check if there is any problem 
            List<MultiSpeciesCompleteDiffExprFileBean> currentCompleteBeans = 
                    new ArrayList<MultiSpeciesCompleteDiffExprFileBean>();

            int totalOver = 0, totalUnder = 0, totalNotDiffExpr = 0;
            //to count number of species with data (we validate only conditions 
            //with a least two species); the data we accept are not the same for simple 
            //and complete files, so we use two Sets.
            Set<String> speciesIdsWithDataForSimple   = new HashSet<String>();
            Set<String> speciesIdsWithDataForComplete = new HashSet<String>();
            
            // A map which is filled during the iteration calls where keys corresponding to  
            // species IDs, the associated values being species counts.  
            Map<String, SpeciesDiffExprCounts> allSpeciesCounts = 
                    new HashMap<String, SpeciesDiffExprCounts>();
            
            for (DiffExpressionCallTO to : entry.getValue()) {
                
                //We ensure that only one OMA group ID is seen over all the calls iterated.
                assert omaNodeId.equals(mapGeneOMANode.get(to.getGeneId()));
                
                String speciesId = String.valueOf(geneTOsByIds.get(to.getGeneId()).getSpeciesId());
                
                // We create a complete bean with null differential expression and call quality
                MultiSpeciesCompleteDiffExprFileBean currentBean = 
                        new MultiSpeciesCompleteDiffExprFileBean(
                                omaNodeId, organIds, organNames,
                                // TODO use stageIds and stage names? even if only one stage IDs for the moment
                                Arrays.asList(to.getStageId()),   
                                Arrays.asList(stageNamesByIds.get(to.getStageId())), 
                                to.getGeneId(), geneTOsByIds.get(to.getGeneId()).getName(), 
                                cioId, cioStatementByIds.get(cioId).getName(),
                                speciesId, speciesNamesByIds.get(speciesId), 
                                to.getDiffExprCallTypeAffymetrix().getStringRepresentation(), 
                                to.getAffymetrixData().getStringRepresentation(), 
                                Double.valueOf(to.getBestPValueAffymetrix()), 
                                Long.valueOf(to.getConsistentDEACountAffymetrix()), 
                                Long.valueOf(to.getInconsistentDEACountAffymetrix()), 
                                to.getDiffExprCallTypeRNASeq().getStringRepresentation(), 
                                to.getRNASeqData().getStringRepresentation(), 
                                Double.valueOf(to.getBestPValueRNASeq()), 
                                Long.valueOf(to.getConsistentDEACountRNASeq()), 
                                Long.valueOf(to.getInconsistentDEACountRNASeq()),
                                null, null); // Differential expression and call quality

                // We add differential expression and call quality to the complete bean
                this.addDiffExprCallMergedDataToRow(currentBean);
                // And add it to the set of bean to be written
                currentCompleteBeans.add(currentBean);
                
                // We finish by counting gene types
                SpeciesDiffExprCounts currentCounts = allSpeciesCounts.get(currentBean.getSpeciesId());
                if (currentCounts == null) {
                    currentCounts = new SpeciesDiffExprCounts(currentBean.getSpeciesId(), 
                            Long.valueOf(0), Long.valueOf(0), Long.valueOf(0), Long.valueOf(0), 
                            Long.valueOf(0));
                    allSpeciesCounts.put(currentBean.getSpeciesId(), currentCounts);                
                }

                boolean hasDataForSimple = false;
                boolean hasDataForComplete = false;
                switch (DiffExpressionData.convertToDiffExpressionData(
                        currentBean.getDifferentialExpression())) {
                    case NO_DATA:
                        currentCounts.setNAGeneCount(currentCounts.getNAGeneCount() + 1);
                        break;
                    case NOT_EXPRESSED:
                        currentCounts.setNotExprGeneCount(currentCounts.getNotExprGeneCount() + 1);
                        break;
                    case OVER_EXPRESSION:
                        currentCounts.setOverExprGeneCount(currentCounts.getOverExprGeneCount() + 1);
                        totalOver++;
                        hasDataForSimple   = true;
                        hasDataForComplete = true;
                        break;
                    case UNDER_EXPRESSION:
                        currentCounts.setUnderExprGeneCount(currentCounts.getUnderExprGeneCount() + 1);
                        totalUnder++;
                        hasDataForSimple   = true;
                        hasDataForComplete = true;
                        break;
                    case NOT_DIFF_EXPRESSION:
                    case WEAK_AMBIGUITY:
                    case STRONG_AMBIGUITY:
                        currentCounts.setNotDiffExprGeneCount(currentCounts.getNotDiffExprGeneCount() + 1);
                        totalNotDiffExpr++;
                        hasDataForComplete = true;
                        break;
                    default:
                        throw log.throwing(new AssertionError(
                                "All logical conditions should have been checked."));
                } 

                if (hasDataForSimple) {
                    speciesIdsWithDataForSimple.add(speciesId);
                }
                if (hasDataForComplete) {
                    speciesIdsWithDataForComplete.add(speciesId);
                }
            }
            
            // We filter when there is no data in at least 2 species 
            if (speciesIdsWithDataForComplete.size() < 2) {
                log.trace("This OMA group doesn't have data in at least 2 species");
                return;
            }
            assert totalOver + totalUnder + totalNotDiffExpr >= 2;
            assert currentCompleteBeans != null && !currentCompleteBeans.isEmpty();

            // We filter, in simple file, poor quality homologous annotations (CIO), 
            // and conditions with 'no diff expressed' only,
            // and we do not have the same criteria for counting species with data 
            // as for the complete file.
            if (cioStatementByIds.get(cioId).isTrusted() &&
                    speciesIdsWithDataForSimple.size() >= 2) {
                MultiSpeciesSimpleDiffExprFileBean simpleBean = new MultiSpeciesSimpleDiffExprFileBean(
                        omaNodeId, organIds, organNames, stageIds, stageNames, 
                        new ArrayList<SpeciesDiffExprCounts>());
                        
                // We order species IDs to keep the same order when we regenerate files.
                List<String> speciesIds = new ArrayList<String>(speciesNamesByIds.keySet());
                Collections.sort(speciesIds);
                for (String speciesId: speciesIds) {
                    // A species could have no data even if at least two other species have some data
                    SpeciesDiffExprCounts counts = allSpeciesCounts.get(speciesId);
                    if (counts == null) {
                        counts = new SpeciesDiffExprCounts(
                                speciesId, Long.valueOf(0), Long.valueOf(0), Long.valueOf(0), 
                                Long.valueOf(0), Long.valueOf(0));
                    }
                    simpleBean.getSpeciesDiffExprCounts().add(counts);
                }
                // We store the simple bean.
                allSimpleBeans.add(simpleBean);
            }
            // We store complete beans.
            allCompleteBeans.addAll(currentCompleteBeans);
        }
        
        // Then we write all beans for one OMA node ID.
        for (Entry<MultiSpeciesDiffExprFileType, ICsvDozerBeanWriter> writerFileType: 
                                writersUsed.entrySet()) {
            MultiSpeciesDiffExprFileType fileType = writerFileType.getKey();
            ICsvDozerBeanWriter writer = writerFileType.getValue();
            
            if (writerFileType.getKey().isSimpleFileType() && allSimpleBeans != null 
                    && !allSimpleBeans.isEmpty()) {
                this.orderAndWriteRows(fileType, writer, processors, omaNodeId, 
                        geneIds, geneNames, allSimpleBeans);

            } else if (!writerFileType.getKey().isSimpleFileType()) {
                this.orderAndWriteRows(fileType, writer, processors, omaNodeId, 
                        geneIds, geneNames, allCompleteBeans);
            }
        }

        log.debug("Done writing calls of OMA node ID", omaNodeId);
        
        log.exit();
    }
    
    /**
     * Order and write rows from provided beans, that should be beans of one OMA node ID.
     *
     * @param fileType      A {@code MultiSpDiffExprFileType} defining the type of file 
     *                      that will be written.
     * @param writer        A {@code ICsvDozerBeanWriter} that is the writer to use.
     * @param omaNodeId     A {@code String} that is the OMA node ID.
     * @param geneIds       A {@code List} of {@code String}s that are gene IDs of the OMA node ID.
     * @param geneNames     A {@code List} of {@code String}s that are gene names of the OMA node ID.
     * @param beans         A {@code List} of {@code T} that are beans to be written.   
     * @throws IOException  If an error occurred while trying to write generated files.
     * @param <T>           A {@code MultiSpeciesFileBean} type parameter.
     */
    //XXX: maybe we should check that all calls are from a same OMA group ID?
    private <T extends MultiSpeciesFileBean> void orderAndWriteRows(
            MultiSpeciesDiffExprFileType fileType, ICsvDozerBeanWriter writer, 
            Map<MultiSpeciesDiffExprFileType, CellProcessor[]> processors, String omaNodeId, 
            List<String> geneIds, List<String> geneNames, List<T> beans) throws IOException {
        log.entry(fileType, writer, processors, omaNodeId, geneIds, geneNames, beans);
        
        // We write gene IDs and names of the OMA group in a comment row
        String comment = "//OMA node ID " + omaNodeId + 
                " contains gene IDs " + geneIds +" with gene names " + geneNames;
        writer.writeComment(comment);
        
        log.trace("Write comment: {} - using writer: {}", comment, writer);

        // We order calls according to OMA ID, entity IDs, stage IDs for simple and complete files
        // then according to species ID, gene ID for complete files. 
        this.sortMultiSpeciesDiffExprFileBeanCollection(beans);

        // We write rows
        for (MultiSpeciesFileBean bean: beans) {
            writer.write(bean, processors.get(fileType));
            log.trace("Write row: {} - using writer: {}", bean, writer);
        }

        // We finish by a comment to separate OMA groups
        writer.writeComment("//");
        log.trace("Write comment: // - using writer: {}", writer);

        log.exit();
    }

    /**
     * Add to the provided {@code CompleteMultiSpeciesDiffExprFileBean} merged 
     * {@code DataState}s and qualities.
     * <p>
     * The provided {@code CompleteMultiSpeciesDiffExprFileBean} will be modified.
     *
     * @param bean  A {@code CompleteMultiSpeciesDiffExprFileBean} that is the bean to be modified.
     * @throws IllegalArgumentException If call data are inconsistent (for instance, without any data).
     */
    //TODO: DRY
    private void addDiffExprCallMergedDataToRow(MultiSpeciesCompleteDiffExprFileBean bean) 
                    throws IllegalArgumentException {
        log.entry(bean);
        
        DiffExpressionData summary = DiffExpressionData.NO_DATA;
        String quality = GenerateDiffExprFile.NA_VALUE;
    
        DiffExprCallType affymetrixType = 
                DiffExprCallType.convertToDiffExprCallType(bean.getAffymetrixData()); 
        DiffExprCallType rnaSeqType = 
                DiffExprCallType.convertToDiffExprCallType(bean.getRNASeqData()); 
        
        Set<DiffExprCallType> allType = EnumSet.of(affymetrixType, rnaSeqType);
    
        // Sanity check on data: one call should't be only no data and/or not_expressed data.
        if ((affymetrixType.equals(DiffExprCallType.NOT_EXPRESSED) ||
                affymetrixType.equals(DiffExprCallType.NO_DATA)) &&
            (rnaSeqType.equals(DiffExprCallType.NOT_EXPRESSED) ||
                    rnaSeqType.equals(DiffExprCallType.NO_DATA))) {
            throw log.throwing(new IllegalArgumentException("One call should not be only "+
                    DiffExprCallType.NOT_EXPRESSED.getStringRepresentation() + " and/or " + 
                    DiffExprCallType.NO_DATA.getStringRepresentation()));
        }
    
        // One call containing over- AND under- expression returns STRONG_AMBIGUITY.
        if ((allType.contains(DiffExprCallType.UNDER_EXPRESSED) &&
                allType.contains(DiffExprCallType.OVER_EXPRESSED))) {
            summary = DiffExpressionData.STRONG_AMBIGUITY;
            quality = GenerateDiffExprFile.NA_VALUE;
    
        // Both data types are equals or only one is set to 'no data': 
        // we choose the data which is not 'no data'.
        } else if (affymetrixType.equals(rnaSeqType) || allType.contains(DiffExprCallType.NO_DATA)) {
            DiffExprCallType type = affymetrixType;
            if (affymetrixType.equals(DiffExprCallType.NO_DATA)) {
                type = rnaSeqType;
            }
            assert !type.equals(DiffExprCallType.NO_DATA);
            
            //store only quality of data different from NO_DATA
            Set<DataState> allDataQuality = EnumSet.noneOf(DataState.class);
            if (!affymetrixType.equals(DiffExprCallType.NO_DATA)) {
                allDataQuality.add(DataState.convertToDataState(bean.getAffymetrixQuality()));
            }
            if (!rnaSeqType.equals(DiffExprCallType.NO_DATA)) {
                allDataQuality.add(DataState.convertToDataState(bean.getRNASeqQuality()));
            }
            assert allDataQuality.size() >=1 && allDataQuality.size() <= 2;
            
            switch (type) {
                case OVER_EXPRESSED: 
                    summary = DiffExpressionData.OVER_EXPRESSION;
                    break;
                case UNDER_EXPRESSED: 
                    summary = DiffExpressionData.UNDER_EXPRESSION;
                    break;
                case NOT_DIFF_EXPRESSED: 
                    summary = DiffExpressionData.NOT_DIFF_EXPRESSION;
                    break;
                default:
                    throw log.throwing(new AssertionError(
                            "Both DiffExprCallType are set to 'no data' or 'not expressed'"));
            }
            if (allDataQuality.contains(DataState.HIGHQUALITY)) {
                quality = DataState.HIGHQUALITY.getStringRepresentation();
            } else {
                quality = DataState.LOWQUALITY.getStringRepresentation();
            }
    
        // All possible cases where the summary is WEAK_AMBIGUITY:
        // - NOT_DIFF_EXPRESSED and (OVER_EXPRESSED or UNDER_EXPRESSED)
        // - NOT_EXPRESSED and OVER_EXPRESSED
        // - NOT_EXPRESSED and NOT_DIFF_EXPRESSED
        //XXX: actually, I think that there are no NOT_EXPRESSED case inserted, 
        //but it doesn't hurt to keep this code
        } else if ((allType.contains(DiffExprCallType.NOT_DIFF_EXPRESSED) && 
                        (allType.contains(DiffExprCallType.OVER_EXPRESSED) ||
                        allType.contains(DiffExprCallType.UNDER_EXPRESSED))) || 
                   (allType.contains(DiffExprCallType.NOT_EXPRESSED) && 
                        (allType.contains(DiffExprCallType.OVER_EXPRESSED)) || 
                        allType.contains(DiffExprCallType.NOT_DIFF_EXPRESSED))) {
            summary = DiffExpressionData.WEAK_AMBIGUITY;
            quality = GenerateDiffExprFile.NA_VALUE;
    
        // One call containing NOT_EXPRESSED and UNDER_EXPRESSED returns 
        // UNDER_EXPRESSION with LOWQUALITY 
        //XXX: actually, I think that there are no NOT_EXPRESSED case inserted, 
        //but it doesn't hurt to keep this code
        } else if (allType.contains(DiffExprCallType.NOT_EXPRESSED) && 
                allType.contains(DiffExprCallType.UNDER_EXPRESSED)) {
            summary = DiffExpressionData.UNDER_EXPRESSION;
            quality = DataState.LOWQUALITY.getStringRepresentation();
            
        } else {
            throw log.throwing(new AssertionError("All logical conditions should have been checked."));
        }
        assert !summary.equals(DiffExpressionData.NO_DATA);
    
        // Add diff. expression and quality to the bean
        bean.setDifferentialExpression(summary.getStringRepresentation());
        bean.setCallQuality(quality);
    
        log.exit();
    }

    /**
     * Sort multi-species diff. expression file beans according to OMA ID, entity IDs, stage IDs 
     * for simple and complete files then according to species ID, gene ID for complete files.
     * <p>
     * The provided {@code beans} will be modified.
     *
     * @param beans A {@code List} of {@code T} that are beans to be sorted.
     * @param <T>   A {@code MultiSpeciesFileBean} type parameter.
     */ 
    private <T extends MultiSpeciesFileBean> void sortMultiSpeciesDiffExprFileBeanCollection(
            List<T> beans) {
        log.entry(beans);
    
        Collections.sort(beans, new Comparator<MultiSpeciesFileBean>(){
            @Override
            public int compare(MultiSpeciesFileBean bean1, MultiSpeciesFileBean bean2) {
                log.entry(bean1, bean2);
                
                int omaIdComp = bean1.getOmaId().compareToIgnoreCase(bean2.getOmaId());
                if (omaIdComp != 0)
                    return omaIdComp;
    
                int uberonIdComp = compareTwoStringLists(bean1.getEntityIds(), bean2.getEntityIds());
                if (uberonIdComp != 0)
                    return uberonIdComp;
    
                int stageIdComp = compareTwoStringLists(bean1.getStageIds(), bean2.getStageIds());
                if (stageIdComp != 0)
                    return stageIdComp;
                
                if (bean1 instanceof MultiSpeciesCompleteDiffExprFileBean) {
                    int speciesIdComp = ((MultiSpeciesCompleteDiffExprFileBean)bean1).getSpeciesId().
                            compareToIgnoreCase(
                                    ((MultiSpeciesCompleteDiffExprFileBean)bean2).getSpeciesId());
                    if (speciesIdComp != 0)
                        return speciesIdComp;
                    
                    int geneIdComp = ((MultiSpeciesCompleteDiffExprFileBean)bean1).getGeneId().
                            compareToIgnoreCase(
                                    ((MultiSpeciesCompleteDiffExprFileBean)bean2).getGeneId());
                    if (geneIdComp != 0)
                        return geneIdComp;
                }
                return log.exit(0);
            }
        });
        
        log.exit();
    }

    /**
     * Compare two {@code List}s of {@code String}s, elements by elements. When two elements (with 
     * the same index), are different, the comparison returns an {@code int} whose sign is that of 
     * calling {@link java.lang.String.compareToIgnoreCase(String)}.
     *
     * @param list1 A {@code List} to be compared to {@code list2}.
     * @param list2 A {@code List} to be compared to {@code list1}.
     * @return      A negative integer, zero, or a positive integer as {@code list1} is greater than, 
     *              equal to, or less than this {@code list2}, ignoring case considerations.
     */
    //TODO: transform this method into a Comparator
    private int compareTwoStringLists(List<String> list1, List<String> list2) {
        log.entry(list1, list2);
        int minLength = Math.min(list1.size(), list2.size());

        for (int i = 0; i < minLength; i++) {
            final int compareValue = list1.get(i).compareToIgnoreCase(list2.get(i));
            if (compareValue != 0) {
                return compareValue; // They are already not equal
            }
        }
        if (list1.size() == list2.size()) {
            return 0; // They are equal
        } else if (list1.size() < list2.size()) {
            return -1; // list 1 is smaller
        } else {
            return 1;
        }
    }

    /**
     * Generates an {@code Array} of {@code CellProcessor}s used to process a multi-species 
     * differential expression TSV file of type {@code fileType}.
     * 
     * @param fileType  The {@code MultiSpDiffExprFileType} of the file to be generated.
     * @param header    An {@code Array} of {@code String}s representing the names 
     *                  of the columns of a multi-species differential expression file.
     * @return          An {@code Array} of {@code CellProcessor}s used to process 
     *                  a multi-species differential expression file.
     * @throw IllegalArgumentException If {@code fileType} is not managed by this method.
     */
    private CellProcessor[] generateCellProcessors(
            MultiSpeciesDiffExprFileType fileType, String[] header) throws IllegalArgumentException {
        log.entry(fileType, header);
        
        //First, we define all set of possible values
        List<Object> data = new ArrayList<Object>();
        for (DiffExpressionData diffExprData: DiffExpressionData.values()) {
            data.add(diffExprData.getStringRepresentation());
        }
        
        List<Object> specificTypeQualities = new ArrayList<Object>();
        specificTypeQualities.add(DataState.HIGHQUALITY.getStringRepresentation());
        specificTypeQualities.add(DataState.LOWQUALITY.getStringRepresentation());
        specificTypeQualities.add(DataState.NODATA.getStringRepresentation());
        
        List<Object> resumeQualities = new ArrayList<Object>();
        resumeQualities.add(DataState.HIGHQUALITY.getStringRepresentation());
        resumeQualities.add(DataState.LOWQUALITY.getStringRepresentation());
        resumeQualities.add(GenerateDiffExprFile.NA_VALUE);
        
        //Then, we build the CellProcessor
        CellProcessor[] processors = new CellProcessor[header.length];
        for (int i = 0; i < header.length; i++) {
            switch (header[i]) {
            // *** CellProcessors common to all file types ***
                case OMA_ID_COLUMN_NAME: 
                    processors[i] = new StrNotNullOrEmpty(); 
                    break;
                //XXX change STAGE_XX_COLUMN_NAME to STAGE_XX_LIST_COLUMN_NAME 
                // when we will have several stages for one StageGroup.
                case STAGE_ID_COLUMN_NAME: 
                case STAGE_NAME_COLUMN_NAME: 
                case ANAT_ENTITY_ID_LIST_ID_COLUMN_NAME: 
                case ANAT_ENTITY_NAME_LIST_ID_COLUMN_NAME: 
                    processors[i] = new Utils.FmtMultipleStringValues(new Trim()); 
                    break;
            }
            
            //if it was one of the column common to all file types, 
            //iterate next column name
            if (processors[i] != null) {
                continue;
            }

            if (fileType.isSimpleFileType()) {
                // *** Attributes specific to simple file ***
                // TODO: for the moment, we use LMinMax() constraint but when we will add dealing 
                // with lost homologous organs, we should use StrNotNullOrEmpty() because it may
                // have N/A when an organ is lost.
                if (header[i].startsWith(OVER_EXPR_GENE_COUNT_COLUMN_NAME) ||
                        header[i].startsWith(UNDER_EXPR_GENE_COUNT_COLUMN_NAME) ||
                        header[i].startsWith(NO_DIFF_EXPR_GENE_COUNT_COLUMN_NAME) ||
                        header[i].startsWith(NOT_EXPR_GENE_COUNT_COLUMN_NAME) ||
                        header[i].startsWith(NA_GENES_COUNT_COLUMN_NAME)) {
                    processors[i] = new LMinMax(0, Long.MAX_VALUE);
                }
                    
            } else {
                // *** Attributes specific to complete file ***
                switch (header[i]) {
                    case SPECIES_LATIN_NAME_COLUMN_NAME: 
                    case GENE_ID_COLUMN_NAME:
                    case CIO_ID_COLUMN_NAME:
                    case CIO_NAME_ID_COLUMN_NAME:
                        processors[i] = new StrNotNullOrEmpty();
                        break;
                    case GENE_NAME_COLUMN_NAME:
                        processors[i] = new NotNull();
                        break;
                    case DIFFEXPRESSION_COLUMN_NAME:
                    case AFFYMETRIX_DATA_COLUMN_NAME:
                    case RNASEQ_DATA_COLUMN_NAME:
                        processors[i] = new IsElementOf(data);
                        break;
                    case QUALITY_COLUMN_NAME:
                        processors[i] = new IsElementOf(resumeQualities);
                        break;
                    case AFFYMETRIX_CALL_QUALITY_COLUMN_NAME:
                    case RNASEQ_CALL_QUALITY_COLUMN_NAME:
                        processors[i] = new IsElementOf(specificTypeQualities);
                        break;
                    case AFFYMETRIX_P_VALUE_COLUMN_NAME:
                    case RNASEQ_P_VALUE_COLUMN_NAME:
                        processors[i] = new DMinMax(0, 1);
                        break;
                    case AFFYMETRIX_CONSISTENT_DEA_COUNT_COLUMN_NAME:
                    case AFFYMETRIX_INCONSISTENT_DEA_COUNT_COLUMN_NAME:
                    case RNASEQ_CONSISTENT_DEA_COUNT_COLUMN_NAME:
                    case RNASEQ_INCONSISTENT_DEA_COUNT_COLUMN_NAME:
                        processors[i] = new LMinMax(0, Long.MAX_VALUE);
                }
            } 
            
            if (processors[i] == null) {
                throw log.throwing(new IllegalArgumentException("Unrecognized header: " 
                        + header[i] + " for file type: " + fileType.getStringRepresentation()));
            }
        }
        
        return log.exit(processors);
    }
    
    /**
     * Generates an {@code Array} of {@code String}s used to generate the header of a multi-species
     * differential expression TSV file of type {@code fileType}.
     * 
     * @param fileType      The {@code MultiSpDiffExprFileType} of the file to be generated.
     * @param speciesNames  A {@code List} of {@code String}s that are the names of species 
     *                      we want to generate data for.
     * @return              An {@code Array} of {@code String}s used to produce the header.
     * @throw IllegalArgumentException If {@code fileType} is not managed by this method.
     */
    private String[] generateHeader(MultiSpeciesDiffExprFileType fileType, List<String> speciesNames)
        throws IllegalArgumentException {
        log.entry(fileType, speciesNames);

        String[] headers = null; 
        if (fileType.isSimpleFileType()) {
            // For simple file, we always have 5 columns and 5 columns for each species
            int nbColumns = 5 + 5 * speciesNames.size();
            headers = new String[nbColumns];
        } else {
            // For complete file, the number of columns is independent of the number of species.
            headers = new String[22];
        }
        
        // *** Headers common to all file types ***
        headers[0] = OMA_ID_COLUMN_NAME;
        headers[1] = ANAT_ENTITY_ID_LIST_ID_COLUMN_NAME;
        headers[2] = ANAT_ENTITY_NAME_LIST_ID_COLUMN_NAME;
        headers[3] = STAGE_ID_COLUMN_NAME;
        headers[4] = STAGE_NAME_COLUMN_NAME;

        if (fileType.isSimpleFileType()) {
            // *** Headers specific to simple file ***
            for (int i = 0; i < speciesNames.size(); i++) {
                // the number of columns depends on the number of species
                int columnIndex = 5 + 5 * i;
                String endHeader = " for " + speciesNames.get(i);
                headers[columnIndex] = OVER_EXPR_GENE_COUNT_COLUMN_NAME + endHeader;
                headers[columnIndex+1] = UNDER_EXPR_GENE_COUNT_COLUMN_NAME + endHeader;
                headers[columnIndex+2] = NO_DIFF_EXPR_GENE_COUNT_COLUMN_NAME + endHeader;
                headers[columnIndex+3] = NOT_EXPR_GENE_COUNT_COLUMN_NAME + endHeader;
                headers[columnIndex+4] = NA_GENES_COUNT_COLUMN_NAME + endHeader;
            }
        } else {
            // *** Headers specific to complete file ***
            headers[5] = SPECIES_LATIN_NAME_COLUMN_NAME;                
            headers[6] = GENE_ID_COLUMN_NAME;
            headers[7] = GENE_NAME_COLUMN_NAME; 
            headers[8] = DIFFEXPRESSION_COLUMN_NAME;
            headers[9] = QUALITY_COLUMN_NAME;
            headers[10] = CIO_ID_COLUMN_NAME; 
            headers[11] = CIO_NAME_ID_COLUMN_NAME;
            headers[12] = AFFYMETRIX_DATA_COLUMN_NAME; 
            headers[13] = AFFYMETRIX_CALL_QUALITY_COLUMN_NAME;
            headers[14] = AFFYMETRIX_P_VALUE_COLUMN_NAME; 
            headers[15] = AFFYMETRIX_CONSISTENT_DEA_COUNT_COLUMN_NAME; 
            headers[16] = AFFYMETRIX_INCONSISTENT_DEA_COUNT_COLUMN_NAME;
            headers[17] = RNASEQ_DATA_COLUMN_NAME; 
            headers[18] = RNASEQ_CALL_QUALITY_COLUMN_NAME;
            headers[19] = RNASEQ_P_VALUE_COLUMN_NAME; 
            headers[20] = RNASEQ_CONSISTENT_DEA_COUNT_COLUMN_NAME; 
            headers[21] = RNASEQ_INCONSISTENT_DEA_COUNT_COLUMN_NAME;
        }
        return log.exit(headers);
    }
    
    /**
     * Generate the field mapping for each column of the header of a multi-species
     * differential expression TSV file of type {@code fileType}.
     *
     * @param fileType  A {@code MultiSpDiffExprFileType} defining the type of file 
     *                  that will be written.
     * @param header    An {@code Array} of {@code String}s representing the names 
     *                  of the columns of a multi-species differential expression file.
     * @return          The {@code Array} of {@code String}s that is the field mapping, put in 
     *                  the {@code Array} at the same index as the column they are supposed 
     *                  to process.
     */
    private String[] generateFieldMapping(MultiSpeciesDiffExprFileType fileType, String[] header) {
        log.entry(fileType, header);

        String[] fieldMapping = new String[header.length];

        int speciesIndex = -1;
        for (int i = 0; i < header.length; i++) {
            switch (header[i]) {
            // *** attributes common to all file types ***
                case OMA_ID_COLUMN_NAME:
                    fieldMapping[i] = "omaId";
                    break;
                case STAGE_ID_COLUMN_NAME:
                    fieldMapping[i] = "stageIds";
                    break;
                case STAGE_NAME_COLUMN_NAME:
                    fieldMapping[i] = "stageNames";
                    break;
                case ANAT_ENTITY_ID_LIST_ID_COLUMN_NAME:
                    fieldMapping[i] = "entityIds";
                    break;
                case ANAT_ENTITY_NAME_LIST_ID_COLUMN_NAME:
                    fieldMapping[i] = "entityNames";
                    break;
            }
            
            //if it was one of the column common to all beans, 
            //iterate next column name
            if (fieldMapping[i] != null) {
                continue;
            }

            if (fileType.isSimpleFileType()) {
                // *** Attributes specific to simple file ***
                
                //FIXME: this correction assumes that OVER_EXPR_GENE_COUNT_COLUMN_NAME is always 
                //the first column in the group of columns of a given species. A real correction 
                //would be to extract the species name from the column names, store the species names 
                //in a Set, and detect when we change species.
                //See for instance org.bgee.pipeline.annotations.SimilarityAnnotation.REF_COL_PATTERN
                //to see a way of coding this. The generation of the names of the columns 
                //should be modified to ensure that they are in sync with the Pattern used to retrieve 
                //species names.
                if (header[i].startsWith(OVER_EXPR_GENE_COUNT_COLUMN_NAME)) {
                    speciesIndex++;
                }
                
                if (header[i].startsWith(OVER_EXPR_GENE_COUNT_COLUMN_NAME)) {
                    fieldMapping[i] = "speciesDiffExprCounts[" + speciesIndex + "].overExprGeneCount";
                    
                } else if (header[i].startsWith(UNDER_EXPR_GENE_COUNT_COLUMN_NAME)) {
                    fieldMapping[i] = "speciesDiffExprCounts[" + speciesIndex + "].underExprGeneCount";
                    
                } else if (header[i].startsWith(NO_DIFF_EXPR_GENE_COUNT_COLUMN_NAME)) {
                    fieldMapping[i] = "speciesDiffExprCounts[" + speciesIndex + "].notDiffExprGeneCount";
                    
                } else if (header[i].startsWith(NOT_EXPR_GENE_COUNT_COLUMN_NAME)) {
                    fieldMapping[i] = "speciesDiffExprCounts[" + speciesIndex + "].notExprGeneCount";
                    
                } else if (header[i].startsWith(NA_GENES_COUNT_COLUMN_NAME)) {
                    fieldMapping[i] = "speciesDiffExprCounts[" + speciesIndex + "].naGeneCount";
                } 

            } else {
                // *** Attributes specific to complete file ***
                switch (header[i]) {
                    case SPECIES_LATIN_NAME_COLUMN_NAME:
                        fieldMapping[i] = "speciesName";
                        break;
                    case GENE_ID_COLUMN_NAME:
                        fieldMapping[i] = "geneId";
                        break;
                    case GENE_NAME_COLUMN_NAME:
                        fieldMapping[i] = "geneName";
                        break;
                    case CIO_ID_COLUMN_NAME:
                        fieldMapping[i] = "cioId";
                        break;
                    case CIO_NAME_ID_COLUMN_NAME:
                        fieldMapping[i] = "cioName";
                        break;
                    case DIFFEXPRESSION_COLUMN_NAME:
                        fieldMapping[i] = "differentialExpression";
                        break;
                    case QUALITY_COLUMN_NAME:
                        fieldMapping[i] = "callQuality";
                        break;
                    case AFFYMETRIX_DATA_COLUMN_NAME:
                        fieldMapping[i] = "affymetrixData";
                        break;
                    case AFFYMETRIX_CALL_QUALITY_COLUMN_NAME:
                        fieldMapping[i] = "affymetrixQuality"; 
                        break;
                    case AFFYMETRIX_P_VALUE_COLUMN_NAME:
                        fieldMapping[i] = "affymetrixPValue";
                        break;
                    case AFFYMETRIX_CONSISTENT_DEA_COUNT_COLUMN_NAME:
                        fieldMapping[i] = "affymetrixConsistentDEA";
                        break;
                    case AFFYMETRIX_INCONSISTENT_DEA_COUNT_COLUMN_NAME:
                        fieldMapping[i] = "affymetrixInconsistentDEA"; 
                        break;
                    case RNASEQ_DATA_COLUMN_NAME:
                        fieldMapping[i] = "rnaSeqData";
                        break;
                    case RNASEQ_CALL_QUALITY_COLUMN_NAME:
                        fieldMapping[i] = "rnaSeqQuality";
                        break;
                    case RNASEQ_P_VALUE_COLUMN_NAME:
                        fieldMapping[i] = "rnaSeqPValue";
                        break;
                    case RNASEQ_CONSISTENT_DEA_COUNT_COLUMN_NAME:
                        fieldMapping[i] = "rnaSeqConsistentDEA";
                        break;
                    case RNASEQ_INCONSISTENT_DEA_COUNT_COLUMN_NAME:
                        fieldMapping[i] = "rnaSeqInconsistentDEA";
                        break;
                }
            } 
            
            if (fieldMapping[i] == null) {
                throw log.throwing(new IllegalArgumentException("Unrecognized header: " 
                        + header[i] + " for file type: " + fileType.getStringRepresentation()));
            }
        }
        
        return log.exit(fieldMapping);
    }
}
