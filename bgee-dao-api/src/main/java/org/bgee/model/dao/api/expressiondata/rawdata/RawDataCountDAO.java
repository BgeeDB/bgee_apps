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
        CALLS_COUNT("callsCount"), RNA_SEQ_LIBRARY_COUNT("rnaSeqLibraryCount"),
        INSITU_ASSAY_COND_COUNT("inSituAssayCondCount");

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
     * @param callCount         A boolean defining rather calls count has to be retrieved
     * @return                  A {@code RawDataConditionTO} containing requested counts.
     */
    public RawDataCountContainerTO getAffymetrixCount(Collection<DAORawDataFilter> rawDataFilters,
            boolean experimentCount, boolean assayCount, boolean callCount);

    /**
     * Retrieve EST count of assay and calls based on a {@code Collection} of 
     * {@code DAORawDataFilter}. 
     *
     * @param rawDataFilters    A {@code Collection} of {@code DAORawDataFilter} used to filter
     *                          EST data for which count are queried.
     * @param assayCount        A boolean defining rather assay count has to be retrieved
     * @param callCount         A boolean defining rather calls count has to be retrieved
     * @return                  A {@code RawDataConditionTO} containing requested counts.
     */
    public RawDataCountContainerTO getESTCount(Collection<DAORawDataFilter> rawDataFilters,
            boolean assayCount, boolean callCount);

    /**
     * Retrieve insitu count of experiment, assay and calls based on a {@code Collection} of 
     * {@code DAORawDataFilter}. 
     *
     * @param rawDataFilters        A {@code Collection} of {@code DAORawDataFilter} used to filter
     *                              insitu data for which count are queried.
     * @param experimentCount       A boolean defining rather experiment count has to be retrieved.
     * @param assayCount            A boolean defining rather assay count has to be retrieved
     * @param assayConditionCount   A boolean defining rather count of unique combination of assay
     *                              and condition has to be retrieved.
     * @param callCount             A boolean defining rather calls count has to be retrieved
     * @return                      A {@code RawDataConditionTO} containing requested counts.
     */
    public RawDataCountContainerTO getInSituCount(Collection<DAORawDataFilter> rawDataFilters,
            boolean experimentCount, boolean assayCount, boolean assayConditionCount,
            boolean resultCount);

    /**
     * Retrieve RNA-Seq count of experiment, assay and calls based on a {@code Collection} of 
     * {@code DAORawDataFilter}. 
     *
     * @param rawDataFilters        A {@code Collection} of {@code DAORawDataFilter} used to filter
     *                              RNA-Seq data for which count are queried.
     * @param isSingleCell          A {@code Boolean} allowing to specify which RNA-Seq to retrieve.
     *                              If <strong>true</strong> only single-cell RNA-Seq are retrieved.
     *                              If <strong>false</strong> only bulk RNA-Seq are retrieved.
     *                              If <strong>null</strong> all RNA-Seq are retrieved.
     * @param isUsedToGenerateCalls A {@code Boolean} allowing to specify if the library has to be used to
     *                              to generate calls. If <strong>true</strong> only libraries used to
     *                              generate calls are retrieved. If <strong>false</strong> only libraries
     *                              not used to generate calls are retrieved. If <strong>null</strong> then
     *                              no filtering on generation of calls is applied to retrieve libraries.
     * @param experimentCount       A boolean defining rather experiment count has to be retrieved.
     * @param libraryCount          A boolean defining rather RNA-Seq library count has to be retrieved.
     *                              It corresponds to the physical library sent to the sequencer. If only
     *                              one condition has been sequenced (e.g. bulk RNA-Seq) then this count
     *                              is the same than assayCount.
     * @param assayCount            A boolean defining rather assay count has to be retrieved. Assay
     *                              correspond to annotated samples. It can be different from library
     *                              count if more than one condition has been sequenced (e.g. different
     *                              celltypes in 10x or multiplexing of different tissues)
     * @param callCount             A boolean defining rather calls count has to be retrieved
     * @return                      A {@code RawDataConditionTO} containing requested counts.
     */
    public RawDataCountContainerTO getRnaSeqCount(Collection<DAORawDataFilter> rawDataFilters,
            Boolean isSingleCell, Boolean isUsedToGenerateCalls, boolean experimentCount,
            boolean libraryCount, boolean assayCount, boolean callCount);

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
        private final Integer callCount;
        private final Integer rnaSeqLibraryCount;
        private final Integer insituAssayConditionCount;
        
        public RawDataCountContainerTO(Integer experimentCount, Integer assayCount,
                Integer callCount) {
            this(experimentCount, assayCount, callCount, null, null);
        }

        public RawDataCountContainerTO(Integer experimentCount, Integer assayCount,
                Integer callCount, Integer rnaSeqLibraryCount,
                Integer insituAssayConditionCount) {
            this.experimentCount = experimentCount;
            this.assayCount = assayCount;
            this.callCount = callCount;
            this.rnaSeqLibraryCount = rnaSeqLibraryCount;
            this.insituAssayConditionCount = insituAssayConditionCount;
        }

        public Integer getExperimentCount() {
            return experimentCount;
        }
        public Integer getAssayCount() {
            return assayCount;
        }
        public Integer getCallCount() {
            return callCount;
        }
        /**
         * An {@code Integer} corresponding to insitu assay with unique condtion count. Not null only for {link RawDataCountDAO#getInSituCount}
         * when the boolean assaConditionCount is true.
         * @return
         */
        public Integer getInsituAssayConditionCount() {
            return insituAssayConditionCount;
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
                    + ", callCount=" + callCount + ", rnaSeqLibraryCount=" + rnaSeqLibraryCount
                    + ", insituAssayConditionCount=" + insituAssayConditionCount + "]";
        }

    }
}
