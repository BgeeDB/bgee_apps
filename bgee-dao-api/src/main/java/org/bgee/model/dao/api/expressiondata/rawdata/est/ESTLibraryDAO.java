package org.bgee.model.dao.api.expressiondata.rawdata.est;

import org.bgee.model.dao.api.expressiondata.rawdata.RawDataAnnotatedTO;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataAssayDAO.NamedEntityAssayTO;
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
public interface ESTLibraryDAO {

    /**
     * {@code TransferObject} for EST libraries.
     * 
     * @author Frederic Bastian
     * @author Valentine Rech de Laval
     * @version Bgee 14
     * @since Bgee 11
     */
    public final class ESTLibraryTO extends NamedEntityAssayTO<String> implements RawDataAnnotatedTO, RawDataWithDataSourceTO {
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