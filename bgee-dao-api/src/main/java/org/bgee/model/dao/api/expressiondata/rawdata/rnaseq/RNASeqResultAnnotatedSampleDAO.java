package org.bgee.model.dao.api.expressiondata.rawdata.rnaseq;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashMap;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.call.CallDAO.CallTO.DataState;
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
 * @author Julien Wollbrett
 * @author Frederic Bastian
 * @author Valentine Rech de Laval
 * @version Bgee 15, Nov. 2022
 * @since Bgee 12
 */
public interface RNASeqResultAnnotatedSampleDAO extends DAO<RNASeqResultAnnotatedSampleDAO.Attribute> {

    /**
     * {@code Enum} used to define the attributes to populate in the {@code RNASeqResultTO}s 
     * obtained from this {@code RNASeqResultDAO}.
     * <ul>
     * <li>{@code LIBRARY_ANNOTATED_SAMPLE_ID}: corresponds to {@link RNASeqResultTO#getAssayId()}.
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
        //TODO DETECTION_FLAG is not used in the database anymore. It is kept here to be able not to
        // consider it when creating a new TO. Should be removed here once removed from the database.
        EXPRESSION_ID("expressionId"), DETECTION_FLAG("detectionFlag"), RNA_SEQ_DATA("rnaSeqData"),
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
     * The attributes available to order retrieved {@code RNASeqResultAnnotatedSampleTO}s
     * <ul>
     * <li>{@code LIBRARY_ANNOTATED_SAMPLE_ID}: corresponds to {@link RNASeqResultTO#getAssayId()}.
     * <li>{@code BGEE_GENE_ID}: corresponds to {@link RNASeqResultAnnotatedSampleTO#getBgeeGeneId()}.
     * <li>{@code EXPRESSION_ID}: corresponds to {@link RNASeqResultTO#getExpressionId()}.
     * </ul>
     */
    public enum OrderingAttribute implements DAO.OrderingAttribute {
        LIBRARY_ANNOTATED_SAMPLE_ID("rnaSeqLibraryAnnotatedSampleId"),
        BGEE_GENE_ID("bgeeGeneId"), EXPRESSION_ID("expressionId");

        private final String fieldName;

        private OrderingAttribute(String fieldName) {
            this.fieldName = fieldName;
        }

        public String getTOFieldName() {
            return this.fieldName;
        }
    }

    /**
     * Allows to retrieve {@code RNASeqResultAnnotatedSampleTO}s according to the provided filters.
     * <p>
     * The {@code RNASeqResultAnnotatedSampleTO}s are retrieved and returned as a
     * {@code RNASeqResultAnnotatedSampleTOResultSet}. It is the responsibility of the caller to close this
     * {@code DAOResultSet} once results are retrieved.
     *
     * @param rawDataFilters    A {@code Collection} of {@code DAORawDataFilter} allowing to specify
     *                          how to filter annotated samples results to retrieve. The query uses
     *                          AND between elements of a same filter and uses OR between filters.
     * @param isSingleCell    A {@code Boolean} allowing to specify which RNA-Seq to retrieve.
     *                          If <strong>true</strong> only single-cell RNA-Seq are retrieved.
     *                          If <strong>false</strong> only bulk RNA-Seq are retrieved.
     *                          If <strong>null</strong> all RNA-Seq are retrieved.
     * @param offset            A {@code Long} used to specify which row to start from retrieving data
     *                          in the result of a query. If null, retrieve data from the first row. If
     *                          not null, a limit should be also provided.
     *                          {@code Long} because sometimes the number of potential results
     *                          can be very large.
     * @param limit             An {@code Integer} used to limit the number of rows returned in a query
     *                          result. If null, all results are returned.
     * @param attributes        A {@code Collection} of {@code Attribute}s to specify the information
     *                          to retrieve from the data source.
     * @param orderingAttributes    A {@code LinkedHashMap} where keys are
     *                              {@code RNASeqResultAnnotatedSampleDAO.OrderingAttribute}s defining
     *                              the attributes used to order the returned {@code RNASeqResultAnnotatedSampleTO}s,
     *                              the associated value being a {@code DAO.Direction}
     *                              defining whether the ordering should be ascendant or descendant.
     *                              If {@code null} or empty, the default ordering is
     *                              {@code OrderingAttribute.LIBRARY_ANNOTATED_SAMPLE_ID} {@code ASC},
     *                              {@code OrderingAttribute.BGEE_GENE_ID} {@code ASC}.
     * @return                  A {@code RNASeqResultAnnotatedSampleTOResultSet} allowing to retrieve the
     *                          targeted {@code RNASeqResultAnnotatedSampleTO}s.
     * @throws DAOException     If an error occurred while accessing the data source.
     */
    public RNASeqResultAnnotatedSampleTOResultSet getResultAnnotatedSamples(
            Collection<DAORawDataFilter> rawDataFilters, Boolean isSingleCell,
            Long offset, Integer limit, Collection<Attribute> attributes,
            LinkedHashMap<OrderingAttribute, DAO.Direction> orderingAttributes) throws DAOException;

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
        private final BigDecimal umiCount;
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
                BigDecimal readCount, BigDecimal umiCount, BigDecimal zScore, BigDecimal pValue,
                Long expressionId, DataState expressionConfidence, ExclusionReason exclusionReason) {
            super();
            this.rnaSeqLibraryAnnotatedSampleId = rnaSeqLibraryAnnotatedSampleId;
            this.abundanceUnit = abundanceUnit;
            this.abundance = abundance;
            this.readCount = readCount;
            this.umiCount = umiCount;
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
        public BigDecimal getUmiCount() {
            return umiCount;
        }
        public BigDecimal getzScore() {
            return zScore;
        }

        @Override
        public String toString() {
            return "RNASeqResultTO [rnaSeqLibraryAnnotatedSampleId=" + rnaSeqLibraryAnnotatedSampleId
                    + ", abundanceUnit=" + abundanceUnit + ", abundance=" + abundance + ", readCount=" + readCount
                    + ", umiCount=" + umiCount + ", zscore=" + zScore + ", callSourceDataTO=" + callSourceDataTO
                    + ", rank=" + rank + "]";
        }

    }
}