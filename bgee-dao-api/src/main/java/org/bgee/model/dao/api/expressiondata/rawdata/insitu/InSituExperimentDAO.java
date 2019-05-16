package org.bgee.model.dao.api.expressiondata.rawdata.insitu;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataExperimentDAO.ExperimentTO;

/**
 * DAO defining queries using or retrieving {@link InSituExperimentTO}s. 
 *
 * @author Frederic Bastian
 * @author Valentine Rech de Laval
 * @version Bgee 14
 * @see InSituExperimentTO
 * @since Bgee 01
 */
public interface InSituExperimentDAO extends DAO<InSituExperimentDAO.Attribute> {

    /**
     * {@code Enum} used to define the attributes to populate in the {@code InSituExperimentTO}s
     * obtained from this {@code InSituExperimentDAO}.
     * <ul>
     * <li>{@code ID}: corresponds to {@link InSituExperimentTO#getId()}.
     * <li>{@code NAME}: corresponds to {@link InSituExperimentTO#getName()}.
     * <li>{@code DESCRIPTION}: corresponds to {@link InSituExperimentTO#getDescription()}.
     * <li>{@code DATA_SOURCE_ID}: corresponds to {@link InSituExperimentTO#getDataSourceId()}.
     * </ul>
     */
    public enum Attribute implements DAO.Attribute {
        ID, NAME, DESCRIPTION, DATA_SOURCE_ID;
    }

    /**
     * {@code TransferObject} in situ hybridization experiments.
     *
     * @author Frederic Bastian
     * @author Valentine Rech de Laval
     * @version Bgee 14
     * @since Bgee 11
     */
    public final class InSituExperimentTO extends ExperimentTO<String> {
        private static final long serialVersionUID = 8572165147198917938L;

        public InSituExperimentTO(String id, String name, String description, Integer dataSourceId) {
            super(id, name, description, dataSourceId);
        }
    }
}