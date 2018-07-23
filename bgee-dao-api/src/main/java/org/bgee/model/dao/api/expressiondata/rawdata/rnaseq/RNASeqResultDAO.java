package org.bgee.model.dao.api.expressiondata.rawdata.rnaseq;

import java.math.BigDecimal;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataCallSourceDAO.CallSourceTO;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataCallSourceDAO.CallSourceWithRankTO;

/**
 * {@code DAO} related to RNA-Seq experiments, using {@link RNASeqResultTO}s 
 * to communicate with the client.
 * 
 * @author Frederic Bastian
 * @author Valentine Rech de Laval
 * @version Bgee 14
 * @since Bgee 12
 */
public interface RNASeqResultDAO extends DAO<RNASeqResultDAO.Attribute> {

    /**
     * {@code Enum} used to define the attributes to populate in the {@code RNASeqResultTO}s 
     * obtained from this {@code RNASeqResultDAO}.
     * <ul>
     * <li>{@code RNA_SEQ_LIBRARY_ID}: corresponds to {@link RNASeqResultTO#getAssayId()}.
     * <li>{@code BGEE_GENE_ID}: corresponds to {@link RNASeqResultTO#getBgeeGeneId()}.
     * <li>{@code FPKM}: corresponds to {@link RNASeqResultTO#getFPKM()}.
     * <li>{@code TPM}: corresponds to {@link RNASeqResultTO#getTPM()}.
     * <li>{@code READ_COUNT}: corresponds to {@link RNASeqResultTO#getReadCount()}.
     * <li>{@code DETECTION_FLAG}: corresponds to {@link RNASeqResultTO#getDetectionFlag()}.
     * <li>{@code RNA_SEQ_DATA}: corresponds to {@link RNASeqResultTO#getExpressionConfidence()}.
     * <li>{@code REASON_FOR_EXCLUSION}: corresponds to {@link RNASeqResultTO#getExclusionReason()}.
     * <li>{@code RANK}: corresponds to {@link RNASeqResultTO#getRank()}.
     * <li>{@code EXPRESSION_ID}: corresponds to {@link RNASeqResultTO#getExpressionId()}.
     * </ul>
     */
    public enum Attribute implements DAO.Attribute {
        RNA_SEQ_LIBRARY_ID, BGEE_GENE_ID, FPKM, TPM, READ_COUNT, DETECTION_FLAG, 
        RNA_SEQ_DATA, REASON_FOR_EXCLUSION, RANK, EXPRESSION_ID;
    }

    /**
     * {@code TransferObject} for RNA-Seq results.
     *
     * @author Frederic Bastian
     * @author Valentine Rech de Laval
     * @version Bgee 14
     * @since Bgee 12
     */
    public final class RNASeqResultTO extends CallSourceTO<String> implements CallSourceWithRankTO {
        private static final long serialVersionUID = 9192921864601490175L;

        private final BigDecimal tpm;
        private final BigDecimal rpkm;
        private final BigDecimal readCount;
        /**
         * A {@code BigDecimal} that is the rank of this call source raw data.
         */
        private final BigDecimal rank;

        /**
         * Default constructor.
         */
        public RNASeqResultTO(String rnaSeqLibraryId, Integer bgeeGeneId, BigDecimal tpm, BigDecimal rpkm,
                BigDecimal readCount, DetectionFlag detectionFlag, DataState expressionConfidence,
                ExclusionReason exclusionReason, BigDecimal rank, Integer expressionId) {
            super(rnaSeqLibraryId, bgeeGeneId, detectionFlag, expressionConfidence, exclusionReason, expressionId);
            this.tpm = tpm;
            this.rpkm = rpkm;
            this.readCount = readCount;
            this.rank = rank;
        }

        /**
         * @return  A {@code BigDecimal} that is the TPM value for this gene in this library.
         *          The value is <strong>NOT</strong> log transformed.
         */
        public BigDecimal getTpm() {
            return tpm;
        }
        /**
         * @return  A {@code BigDecimal} that is the FPKM value for this gene in this library.
         *          The value is <strong>NOT</strong> log transformed.
         */
        public BigDecimal getFpkm() {
            return rpkm;
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
        public BigDecimal getRank() {
            return this.rank;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("RNASeqResultTO [bgeeGeneId=").append(getBgeeGeneId()).append(", assayId=").append(getAssayId())
                    .append(", tpm=").append(tpm).append(", rpkm=").append(rpkm)
                    .append(", readCount=").append(readCount).append(", detectionFlag=").append(getDetectionFlag())
                    .append(", expressionConfidence=").append(getExpressionConfidence())
                    .append(", exclusionReason=").append(getExclusionReason()).append(", rank=").append(rank)
                    .append(", expressionId=").append(getExpressionId()).append("]");
            return builder.toString();
        }
    }
}