package org.bgee.model.dao.api.expressiondata.rawdata.rnaseq;

import java.math.BigDecimal;
import java.util.Collection;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataCallSourceDAO.CallSourceDataTO;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataCallSourceDAO.CallSourceDataTO.ExclusionReason;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataCallSourceDAO.CallSourceTO;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataCallSourceDAO.CallSourceWithRankTO;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqLibraryAnnotatedSampleDAO.RNASeqLibraryAnnotatedSampleTO.AbundanceUnit;

/**
 * {@code DAO} related to RNA-Seq experiments, using {@link RNASeqResultTO}s 
 * to communicate with the client.
 * 
 * @author Frederic Bastian
 * @author Valentine Rech de Laval
 * @version Bgee 14
 * @since Bgee 12
 */
public interface RNASeqResultAnnotatedSampleDAO extends DAO<RNASeqResultAnnotatedSampleDAO.Attribute> {

    /**
     * {@code Enum} used to define the attributes to populate in the {@code RNASeqResultTO}s 
     * obtained from this {@code RNASeqResultDAO}.
     * <ul>
     * <li>{@code RNA_SEQ_LIBRARY_ANNOTATED_ID}: corresponds to {@link RNASeqResultTO#getAssayId()}.
     * <li>{@code BGEE_GENE_ID}: corresponds to {@link RNASeqResultTO#getBgeeGeneId()}.
     * <li>{@code ABUNDANCE_UNIT}: corresponds to {@link RNASeqResultTO#getAbundanceUnit()}.
     * <li>{@code ABUNDANCE}: corresponds to {@link RNASeqResultTO#getAbundance()}.
     * <li>{@code RANK}: corresponds to {@link RNASeqResultTO#getRawRank()}.
     * <li>{@code READ_COUNT}: corresponds to {@link RNASeqResultTO#getReadCount()}.
     * <li>{@code UMIS_COUNT}: corresponds to {@link RNASeqResultTO#getUmisCount()}.
     * <li>{@code ZSCORE}: corresponds to {@link RNASeqResultTO#getZScore()}.
     * <li>{@code PVALUE}: corresponds to {@link RNASeqResultTO#getPValue()}.
     * <li>{@code EXPRESSION_ID}: corresponds to {@link RNASeqResultTO#getExpressionId()}.
     * <li>{@code RNA_SEQ_DATA}: corresponds to {@link RNASeqResultTO#getExpressionConfidence()}.
     * <li>{@code REASON_FOR_EXCLUSION}: corresponds to {@link RNASeqResultTO#getExclusionReason()}.
     * </ul>
     */
    public enum Attribute implements DAO.Attribute {
        LIBRARY_ANNOTATED_SAMPLE_ID("rnaSeqLibraryAnnotatedSampleId"), BGEE_GENE_ID("bgeeGeneId"),
        ABUNDANCE_UNIT("abundanceUnit"), ABUNDANCE("abundance"), RANK("rawRank"),
        READS_COUNT("readsCount"), UMIS_COUNT("UMIsCount"), ZSCORE("zScore"), PVALUE("pValue"),
        EXPRESSION_ID("expressionId"), RNA_SEQ_DATA("rnaSeqData"),
        REASON_FOR_EXCLUSION("reasonForExclusion");
        /**
         * A {@code String} that is the corresponding field name in {@code AffymetrixChipTO} class.
         * @see {@link Attribute#getTOFieldName()}
         */
        private final String fieldName;

        private Attribute(String fieldName) {
            this.fieldName = fieldName;
        }

        @Override
        public String getTOFieldName() {
            return this.fieldName;
        }
    }

    /**
     * Retrieve from a data source a set of {@code RNASeqResultAnnotatedSample}s,
     * corresponding to the annotated RNA-Seq results with selected library IDs or
     * {@code null} if none could be found.
     * 
     * @param libraryIds        A {@code {@link Collection} of {@code String} representing the IDs
     *                          of the RNA-Seq library of the annotated sample results to retrieve from
     *                          the data source.
     * @param attributes        A {@code Collection} of {@code Attribute}s to specify the information
     *                          to retrieve from the annotated sample result.
     * 
     * @return  A {@code RNASeqResultAnnotatedSampleTOResultSet}, encapsulating all the data
     *          related to the annotated RNA-Seq annotated sample result retrieved from the data source,
     *          or {@code null} if none could be found. 
     * @throws DAOException     If an error occurred when accessing the data source.
     */
    public RNASeqResultAnnotatedSampleTOResultSet getRNASeqResultAnnotatedSampleFromLibraryIds(
            Collection<String> libraryIds,
            Collection<RNASeqResultAnnotatedSampleDAO.Attribute> attrs);

    /**
     * Retrieve from a data source a set of {@code RNASeqResultAnnotatedSample}s,
     * corresponding to the annotated RNA-Seq results with selected gene IDs,
     * species IDs, and raw condition parameters or {@code null} if none could be found.
     * 
     * @param rawDataFilter     A {@code DAORawDataFilter} allowing to specify which annotated
     *                          sample results to retrieve.
     * @param attributes        A {@code Collection} of {@code Attribute}s to specify the information
     *                          to retrieve from the annotated sample result.
     * 
     * @return  A {@code RNASeqResultAnnotatedSampleTOResultSet}, encapsulating all the data
     *          related to the annotated RNA-Seq annotated sample result retrieved from the data source,
     *          or {@code null} if none could be found.
     * @throws DAOException     If an error occurred when accessing the data source.
     */
    public RNASeqResultAnnotatedSampleTOResultSet getRNASeqResultAnnotatedSampleFromRawDataFilter(
            DAORawDataFilter rawDataFilter,
            Collection<RNASeqResultAnnotatedSampleDAO.Attribute> attrs);

    /**
     * Retrieve from a data source a set of {@code RNASeqResultAnnotatedSample}s,
     * corresponding to the annotated RNA-Seq results with selected library IDs, gene IDs,
     * species IDs, and raw condition parameters or {@code null} if none could be found.
     * 
     * @param libraryIds        A {@code {@link Collection} of {@code String} representing the IDs
     *                          of the RNA-Seq library of the annotated sample results to retrieve from
     *                          the data source.
     * @param rawDataFilter     A {@code DAORawDataFilter} allowing to specify which annotated
     *                          sample results to retrieve.
     * @param attributes        A {@code Collection} of {@code Attribute}s to specify the information
     *                          to retrieve from the annotated sample result.
     * 
     * @return  A {@code RNASeqResultAnnotatedSampleTOResultSet}, encapsulating all the data
     *          related to the annotated RNA-Seq annotated sample result retrieved from the data source,
     *          or {@code null} if none could be found.
     * @throws DAOException     If an error occurred when accessing the data source.
     */
    public RNASeqResultAnnotatedSampleTOResultSet getRNASeqResultAnnotatedSamples(
            Collection<String> libraryIds, DAORawDataFilter rawDataFilter,
            Collection<RNASeqResultAnnotatedSampleDAO.Attribute> attrs);
    /**
     * {@code DAOResultSet} for {@code RNASeqExperimentTO}s
     * 
     * @author  Julien Wollbrett
     * @version Bgee 15, Aug. 2022
     * @since   Bgee 15
     */
    public interface RNASeqResultAnnotatedSampleTOResultSet extends DAOResultSet<RNASeqResultAnnotatedSampleTO> {
    }

    /**
     * {@code TransferObject} for RNA-Seq results.
     *
     * @author Frederic Bastian
     * @author Valentine Rech de Laval
     * @version Bgee 14
     * @since Bgee 12
     */
    public final class RNASeqResultAnnotatedSampleTO extends TransferObject
    implements CallSourceTO<Integer>, CallSourceWithRankTO {
        private static final long serialVersionUID = 9192921864601490175L;

        private final Integer rnaSeqLibraryAnnotatedSampleId;
        private final AbundanceUnit abundanceUnit;
        private final BigDecimal abundance;
        private final BigDecimal readCount;
        private final BigDecimal umisCount;
        private final BigDecimal zScore;
        /**
         * The {@code CallSourceDataTO} carrying the information about
         * the produced call of presence/absence of expression.
         */
        private final CallSourceDataTO callSourceDataTO;
        /**
         * A {@code BigDecimal} that is the rank of this call source raw data.
         */
        private final BigDecimal rank;

        /**
         * Default constructor.
         */
        public RNASeqResultAnnotatedSampleTO(Integer rnaSeqLibraryAnnotatedSampleId, Integer bgeeGeneId,
                AbundanceUnit abundanceUnit, BigDecimal abundance, BigDecimal rank,
                BigDecimal readCount, BigDecimal umisCount, BigDecimal zScore, BigDecimal pValue,
                Long expressionId, DataState expressionConfidence, ExclusionReason exclusionReason) {
            super();
            this.rnaSeqLibraryAnnotatedSampleId = rnaSeqLibraryAnnotatedSampleId;
            this.abundanceUnit = abundanceUnit;
            this.abundance = abundance;
            this.readCount = readCount;
            this.umisCount = umisCount;
            this.zScore = zScore;
            this.rank = rank;
            this.callSourceDataTO = new CallSourceDataTO(bgeeGeneId, pValue,
                    expressionConfidence, exclusionReason, expressionId);

        }

        @Override
        public Integer getAssayId() {
            return this.rnaSeqLibraryAnnotatedSampleId;
        }
        @Override
        public CallSourceDataTO getCallSourceDataTO() {
            return this.callSourceDataTO;
        }
        /**
         * @return  A {@code BigDecimal} that is the count of reads mapped to this gene in this library.
         *          As of Bgee 14, the counts are "estimated counts", produced using the Kallisto software.
         *          They are not normalized for read or gene lengths.
         */
        public BigDecimal getReadCount() {
            return readCount;
        }
        /**
         * @return  A {@code BigDecimal} that is the rank of this call source raw data.
         */
        @Override
        public BigDecimal getRank() {
            return this.rank;
        }
        public AbundanceUnit getAbundanceUnit() {
            return abundanceUnit;
        }
        public BigDecimal getAbundance() {
            return abundance;
        }
        public BigDecimal getUmisCount() {
            return umisCount;
        }
        public BigDecimal getzScore() {
            return zScore;
        }

        @Override
        public String toString() {
            return "RNASeqResultTO [rnaSeqLibraryAnnotatedSampleId=" + rnaSeqLibraryAnnotatedSampleId
                    + ", abundanceUnit=" + abundanceUnit + ", abundance=" + abundance + ", readCount=" + readCount
                    + ", umisCount=" + umisCount + ", zscore=" + zScore + ", callSourceDataTO=" + callSourceDataTO
                    + ", rank=" + rank + "]";
        }

    }
}