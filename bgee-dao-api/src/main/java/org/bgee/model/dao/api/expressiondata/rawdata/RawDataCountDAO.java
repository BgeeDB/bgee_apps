package org.bgee.model.dao.api.expressiondata.rawdata;

import java.util.Collection;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.expressiondata.rawdata.est.ESTLibraryDAO.ESTLibraryTO;

public interface RawDataCountDAO extends DAO<RawDataCountDAO.Attribute> {

    /**
     * {@code Enum} used to define the attributes to populate in the {@code RawDataCountContainerTO}s
     * obtained from this {@code RawDataCountDAO}.
     * <ul>
     * <li>{@code AFFY_EXP_COUNT}: corresponds to {@link RawDataCountContainerTO#getAffyExperimentCount()}.
     * <li>{@code AFFY_ASSAY_COUNT}: corresponds to {@link RawDataCountContainerTO#getAffyAssayCount()}.
     * <li>{@code AFFY_RESULT_COUNT}: corresponds to {@link RawDataCountContainerTO#getAffyResultCount()}.
     * </ul>
     */
    public enum Attribute implements DAO.Attribute {
        EXP_COUNT("expCount"), ASSAY_COUNT("assayCount"),
        RESULT_COUNT("resultCount");

        /**
         * A {@code String} that is the corresponding field name in {@code ESTLibraryTO} class.
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
    
    public RawDataCountContainerTO getAffymetrixCount(Collection<DAORawDataFilter> rawDataFilters,
            boolean resultCount);

    public RawDataCountContainerTO getInSituCount(Collection<DAORawDataFilter> rawDataFilters,
            boolean resultCount);

    public RawDataCountContainerTO getEstCount(Collection<DAORawDataFilter> rawDataFilters,
            boolean resultCount);

    public RawDataCountContainerTO getRnaSeqCount(Collection<DAORawDataFilter> rawDataFilters,
            boolean resultCount);

    /**
     * {@code DAOResultSet} specifics to {@code RawDataCountContainerTO}s
     * 
     * @author Julien Wollbrett
     * @version Bgee 15
     * @since Bgee 15
     */
    public interface RawDataCountContainerTOResultSet extends DAOResultSet<RawDataCountContainerTO> {}

    /**
     * {@code TransferObject} used to retrieve raw data count from the database
     * 
     * @author Julien Wollbrett
     * @version Bgee 15
     * @since Bgee 15
     */
    public class RawDataCountContainerTO extends TransferObject{
        /**
         * 
         */
        private static final long serialVersionUID = 7933114482291306433L;
        private final Integer experimentCount;
        //For RNA-Seq, corresponds to AnnotatedSample
        private final Integer assayCount;
        private final Integer resultCount;
        private final Integer rnaSeqLibraryCount;
        
        public RawDataCountContainerTO(Integer experimentCount, Integer assayCount,
                Integer resultCount) {
            this(experimentCount, assayCount, resultCount, null);
        }

        public RawDataCountContainerTO(Integer experimentCount, Integer assayCount,
                Integer resultCount, Integer rnaSeqLibraryCount) {
            this.experimentCount = experimentCount;
            this.assayCount = assayCount;
            this.resultCount = resultCount;
            this.rnaSeqLibraryCount = rnaSeqLibraryCount;
        }

        public Integer getAffyExperimentCount() {
            return experimentCount;
        }
        public Integer getAffyAssayCount() {
            return assayCount;
        }
        public Integer getAffyResultCount() {
            return resultCount;
        }

        @Override
        public String toString() {
            return "RawDataCountContainerTO [experimentCount=" + experimentCount + ", assayCount=" + assayCount
                    + ", resultCount=" + resultCount + ", rnaSeqLibraryCount=" + rnaSeqLibraryCount + "]";
        }
    }

}
