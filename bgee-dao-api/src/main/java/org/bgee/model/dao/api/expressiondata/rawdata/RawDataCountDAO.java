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
//     * <li>{@code DATA_SOURCE_ID}: corresponds to {@link ESTLibraryTO#getDataSourceId()}.
//     * <li>{@code CONDITION_ID}: corresponds to {@link ESTLibraryTO#getConditionId()}.
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
        
//        private final Integer inSituExperimentCount;
//        private final Integer inSituAssayCount;
//        private final Integer inSituResultCount;
//        private final Integer estAssayCount;
//        private final Integer estResultCount;
//        private final Integer rnaSeqExperimentCount;
//        private final Integer rnaSeqAssayCount;
//        private final Integer rnaSeqResultCount;

        public RawDataCountContainerTO(Integer affyExperimentCount, Integer affyAssayCount,
                Integer affyResultCount) {
            this.affyExperimentCount = affyExperimentCount;
            this.affyAssayCount = affyAssayCount;
            this.affyResultCount = affyResultCount;
        }

        public Integer getAffyExperimentCount() {
            return affyExperimentCount;
        }
        public Integer getAffyAssayCount() {
            return affyAssayCount;
        }
        public Integer getAffyResultCount() {
            return affyResultCount;
        }
//        public Integer getInSituExperimentCount() {
//            return inSituExperimentCount;
//        }
//        public Integer getInSituAssayCount() {
//            return inSituAssayCount;
//        }
//        public Integer getEstAssayCount() {
//            return estAssayCount;
//        }
//        public Integer getRnaSeqExperimentCount() {
//            return rnaSeqExperimentCount;
//        }
//        public Integer getRnaSeqAssayCount() {
//            return rnaSeqAssayCount;
//        }

        @Override
        public String toString() {
            return "RawDataCountContainerTO [affyExperimentCount=" + affyExperimentCount + ", affyAssayCount="
                    + affyAssayCount + ", affyResultCount=" + affyResultCount + "]";
        }
        
    }

}
