package org.bgee.model.dao.api.expressiondata.rawdata;

import java.util.Collection;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.TransferObject;

public interface RawDataCountDAO extends DAO<RawDataCountDAO.Attribute> {

    /**
     * {@code Enum} used to define the attributes to populate in the {@code RawDataCountContainerTO}s
     * obtained from this {@code RawDataCountDAO}.
     * <ul>
     * <li>{@code EXP_COUNT}: corresponds to {@link RawDataCountContainerTO#getExperimentCount()}.
     * <li>{@code ASSAY_COUNT}: corresponds to {@link RawDataCountContainerTO#getAssayCount()}.
     * <li>{@code CALLS_COUNT}: corresponds to {@link RawDataCountContainerTO#getResultCount()}.
     * <li>{@code RNA_SEQ_LIBRARY_COUNT}: corresponds to {@link RawDataCountContainerTO#getRnaSeqLibraryCount()}.
     * </ul>
     */
    public enum Attribute implements DAO.Attribute {
        EXP_COUNT("expCount"), ASSAY_COUNT("assayCount"),
        CALLS_COUNT("callsCount"), RNA_SEQ_LIBRARY_COUNT("rnaSeqLibraryCount");

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

    /**
     * Retrieve affymetrix count of experiment, assay and calls based on a {@code Collection} of 
     * {@code DAORawDataFilter}. 
     *
     * @param rawDataFilters    A {@code Collection} of {@code DAORawDataFilter} used to filter
     *                          affymetrix data for which count are queried.
     * @param experimentCount   A boolean defining rather experiment count has to be retrieved.
     * @param assayCount        A boolean defining rather assay count has to be retrieved
     * @param callsCount        A boolean defining rather calls count has to be retrieved
     * @return                  A {@code RawDataConditionTO} containing requested counts.
     */
    public RawDataCountContainerTO getAffymetrixCount(Collection<DAORawDataFilter> rawDataFilters,
            boolean experimentCount, boolean assayCount, boolean callsCount);

    /**
     * Retrieve insitu count of experiment, assay and calls based on a {@code Collection} of 
     * {@code DAORawDataFilter}. 
     *
     * @param rawDataFilters    A {@code Collection} of {@code DAORawDataFilter} used to filter
     *                          insitu data for which count are queried.
     * @param experimentCount   A boolean defining rather experiment count has to be retrieved.
     * @param assayCount        A boolean defining rather assay count has to be retrieved
     * @param callsCount        A boolean defining rather calls count has to be retrieved
     * @return                  A {@code RawDataConditionTO} containing requested counts.
     */
    public RawDataCountContainerTO getInSituCount(Collection<DAORawDataFilter> rawDataFilters,
            boolean experimentCount, boolean assayCount, boolean resultCount);

    /**
     * Retrieve EST count of assay and calls based on a {@code Collection} of 
     * {@code DAORawDataFilter}. 
     *
     * @param rawDataFilters    A {@code Collection} of {@code DAORawDataFilter} used to filter
     *                          EST data for which count are queried.
     * @param assayCount        A boolean defining rather assay count has to be retrieved
     * @param callsCount        A boolean defining rather calls count has to be retrieved
     * @return                  A {@code RawDataConditionTO} containing requested counts.
     */
    public RawDataCountContainerTO getEstCount(Collection<DAORawDataFilter> rawDataFilters,
            boolean assayCount, boolean callsCount);

    /**
     * Retrieve RNA-Seq count of experiment, assay and calls based on a {@code Collection} of 
     * {@code DAORawDataFilter}. 
     *
     * @param rawDataFilters    A {@code Collection} of {@code DAORawDataFilter} used to filter
     *                          RNA-Seq data for which count are queried.
     * @param experimentCount   A boolean defining rather experiment count has to be retrieved.
     * @param assayCount        A boolean defining rather assay count has to be retrieved. Assay
     *                          correspond to annotated samples. It can be different from library
     *                          count if more than one condition has been sequenced (e.g. different
     *                          celltypes in 10x or multiplexing of different tissues)
     * @param libraryCount      A boolean defining rather RNA-Seq library count has to be retrieved.
     *                          It corresponds to the physical library sent to the sequencer. If only
     *                          one condition has been sequenced (e.g. bulk RNA-Seq) then this count
     *                          is the same than assayCount.
     * @param callsCount        A boolean defining rather calls count has to be retrieved
     * @return                  A {@code RawDataConditionTO} containing requested counts.
     */
    public RawDataCountContainerTO getRnaSeqCount(Collection<DAORawDataFilter> rawDataFilters,
            boolean experimentCount, boolean assayCount, boolean libraryCount, boolean callsCount);

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
        private final Integer callsCount;
        private final Integer rnaSeqLibraryCount;
        
        public RawDataCountContainerTO(Integer experimentCount, Integer assayCount,
                Integer callsCount) {
            this(experimentCount, assayCount, callsCount, null);
        }

        public RawDataCountContainerTO(Integer experimentCount, Integer assayCount,
                Integer callsCount, Integer rnaSeqLibraryCount) {
            this.experimentCount = experimentCount;
            this.assayCount = assayCount;
            this.callsCount = callsCount;
            this.rnaSeqLibraryCount = rnaSeqLibraryCount;
        }

        public Integer getExperimentCount() {
            return experimentCount;
        }
        public Integer getAssayCount() {
            return assayCount;
        }
        public Integer getCallsCount() {
            return callsCount;
        }
        /**
         * An {@code Integer} corresponding to RNA-Seq library count. Not null only for {link RawDataCountDAO#getRnaSeqCount}
         * when the boolean libraryCount is true.
         * @return
         */
        public Integer getRnaSeqLibraryCount() {
            return rnaSeqLibraryCount;
        }

        @Override
        public String toString() {
            return "RawDataCountContainerTO [experimentCount=" + experimentCount + ", assayCount=" + assayCount
                    + ", callsCount=" + callsCount + ", rnaSeqLibraryCount=" + rnaSeqLibraryCount + "]";
        }
    }

}
