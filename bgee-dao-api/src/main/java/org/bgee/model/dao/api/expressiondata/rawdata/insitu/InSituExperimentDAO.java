package org.bgee.model.dao.api.expressiondata.rawdata.insitu;

import java.util.Collection;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataExperimentDAO.ExperimentTO;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataWithDataSourceTO;

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
        ID("inSituExperimentId"), NAME("inSituExperimentName"),
        DESCRIPTION("inSituExperimentDescription"), DATA_SOURCE_ID("dataSourceId");

        /**
         * A {@code String} that is the corresponding field name in {@code ESTTO} class.
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
     * retrieve insitu experiments filtered on experiment IDs, species IDs, gene IDs and condition
     * parameters
     * 
     * @param experimentIds     A {@code Collection} of {@code String} corresponding to the
     *                          IDs of the In Situ experiments to retrieve.
     * @param rawDataFilter     A {@code DAORawDataFilter} allowing to specify which probesets to
     *                          retrieve.
     * 
     * @return                  A {@code InSituExperimentTOResultSet} containing InSitu experiments
     */
    public InSituExperimentTOResultSet getExperiments(Collection<String> experimentIds,
            DAORawDataFilter rawDataFilter, Collection<InSituExperimentDAO.Attribute> attrs);

    /**
     * retrieve insitu experiments filtered on experiment IDs
     * 
     * @param experimentIds     A {@code Collection} of {@code String} corresponding to the
     *                          IDs of the In Situ experiments to retrieve.
     * 
     * @return                  A {@code InSituExperimentTOResultSet} containing InSitu experiments
     */
    public InSituExperimentTOResultSet getExperimentsFromIds(Collection<String> experimentIds,
            Collection<InSituExperimentDAO.Attribute> attrs);

    public interface InSituExperimentTOResultSet extends DAOResultSet<InSituExperimentTO> {}

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

        public InSituExperimentTO(String id, String name, String description,
                Integer dataSourceId) {
            super(id, name, description, dataSourceId);
        }
    }
}