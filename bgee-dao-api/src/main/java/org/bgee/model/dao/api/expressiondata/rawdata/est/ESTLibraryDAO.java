package org.bgee.model.dao.api.expressiondata.rawdata.est;

import java.util.Collection;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.NamedEntityTO;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataAnnotatedTO;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataAssayDAO.AssayTO;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataWithDataSourceTO;

/**
 * DAO for {@link ESTLibraryTO}s. 
 * 
 * @author Frederic Bastian
 * @author Valentine Rech de Laval
 * @version Bgee 14
 * @see ESTLibraryTO
 * @since Bgee 01
 */
public interface ESTLibraryDAO extends DAO<ESTLibraryDAO.Attribute> {

    /**
     * {@code Enum} used to define the attributes to populate in the {@code ESTLibraryTO}s
     * obtained from this {@code ESTLibraryDAO}.
     * <ul>
     * <li>{@code ID}: corresponds to {@link ESTLibraryTO#getId()}.
     * <li>{@code NAME}: corresponds to {@link ESTLibraryTO#getName()}.
     * <li>{@code DESCRIPTION}: corresponds to {@link ESTLibraryTO#getDescription()}.
     * <li>{@code DATA_SOURCE_ID}: corresponds to {@link ESTLibraryTO#getDataSourceId()}.
     * <li>{@code CONDITION_ID}: corresponds to {@link ESTLibraryTO#getConditionId()}.
     * </ul>
     */
    public enum Attribute implements DAO.Attribute {
        ID("estLibraryId"), NAME("estLibraryName"), DESCRIPTION("estLibraryDescription"), 
        DATA_SOURCE_ID("dataSourceid"), CONDITION_ID("conditionId");

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
     * Retrieve EST Libraries existing in any requested species IDs or corresponding
     * to any requested combination of condition parameters and potentially filtered
     * using requested EST library IDs
     * 
     * @param libraryIds        A {@code Collection} of {@code String} corresponding to
     *                          library IDs to filter on.
     * @param rawDataFilter     A {@code DAORawDataFilter} allowing to specify which probesets to
     *                          retrieve.
     * @param speciesIds        A {@code Collection} of {@code Integer} corresponding to the
     *                          species IDs used to filter EST Libraries
     * @return
     */
    public ESTLibraryTOResultSet getESTLibraries(Collection<String> libraryIds,
            DAORawDataFilter rawDaraFilter, Collection<ESTLibraryDAO.Attribute> attrs);

    /**
     * {@code DAOResultSet} specifics to {@code ESTLibraryTO}s
     * 
     * @author Julien Wollbrett
     * @version Bgee 15
     * @since Bgee 15
     */
    public interface ESTLibraryTOResultSet extends DAOResultSet<ESTLibraryTO> {}

    /**
     * {@code TransferObject} for EST libraries.
     * 
     * @author Frederic Bastian
     * @author Valentine Rech de Laval
     * @version Bgee 14
     * @since Bgee 11
     */
    public final class ESTLibraryTO extends NamedEntityTO<String>
    implements AssayTO<String>, RawDataAnnotatedTO, RawDataWithDataSourceTO {
        private static final long serialVersionUID = 6500670452213931420L;

        private final Integer dataSourceId;
        private final Integer conditionId;

        public ESTLibraryTO(String id, String name, String description, Integer dataSourceId,
                Integer conditionId) {
            super(id, name, description);
            this.dataSourceId = dataSourceId;
            this.conditionId = conditionId;
        }

        @Override
        public Integer getDataSourceId() {
            return this.dataSourceId;
        }
        @Override
        public Integer getConditionId() {
            return this.conditionId;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("ESTLibraryTO [id=").append(getId()).append(", name=").append(getName())
                    .append(", description=").append(getDescription()).append(", dataSourceId=").append(dataSourceId)
                    .append(", conditionId=").append(conditionId).append("]");
            return builder.toString();
        }
    }
}