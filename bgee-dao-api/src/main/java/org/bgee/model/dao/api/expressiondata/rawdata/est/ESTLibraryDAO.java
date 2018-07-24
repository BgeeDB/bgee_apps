package org.bgee.model.dao.api.expressiondata.rawdata.est;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.NamedEntityTO;
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
        ID, NAME, DESCRIPTION, DATA_SOURCE_ID, CONDITION_ID;
    }

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

        public ESTLibraryTO(String id, String name, String description, Integer dataSourceId, Integer conditionId) {
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