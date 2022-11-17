package org.bgee.model.dao.api.expressiondata.rawdata.rnaseq;

import java.util.Collection;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataExperimentDAO.ExperimentTO;

/**
 * {@code DAO} for {@link RNASeqExperimentTO}s.
 * 
 * @author Juline Wollbrett
 * @author Frederic Bastian
 * @author Valentine Rech de Laval
 * @version Bgee 15, Nov. 2022
 * @see RNASeqExperimentTO
 * @since Bgee 12
 */
public interface RNASeqExperimentDAO extends DAO<RNASeqExperimentDAO.Attribute> {

    /**
     * {@code Enum} used to define the attributes to populate in the {@code RNASeqExperimentTO}s
     * obtained from this {@code RNASeqExperimentDAO}.
     * <ul>
     * <li>{@code ID}: corresponds to {@link RNASeqExperimentTO#getId()}.
     * <li>{@code NAME}: corresponds to {@link RNASeqExperimentTO#getName()}.
     * <li>{@code DESCRIPTION}: corresponds to {@link RNASeqExperimentTO#getDescription()}.
     * <li>{@code DATA_SOURCE_ID}: corresponds to {@link RNASeqExperimentTO#getDataSourceId()}.
     * </ul>
     */
    public enum Attribute implements DAO.Attribute {
        ID("rnaSeqExperimentId"), NAME("rnaSeqExperimentName"),
        DESCRIPTION("rnaSeqExperimentDescription"), DATA_SOURCE_ID("dataSourceId");

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
     * Allows to retrieve {@code RNASeqExperimentTO}s according to the provided filters.
     * <p>
     * The {@code RNASeqExperimentTO}s are retrieved and returned as a
     * {@code RNASeqExperimentTOResultSet}. It is the responsibility of the caller to close this
     * {@code DAOResultSet} once results are retrieved.
     *
     * @param rawDataFilters    A {@code Collection} of {@code DAORawDataFilter} allowing to specify
     *                          how to filter experiments to retrieve. The query uses AND between
     *                          elements of a same filter and uses OR between filters.
     * @param isSingleCell      A {@code Boolean} allowing to specify which RNA-Seq to retrieve.
     *                          If <strong>true</strong> only single-cell RNA-Seq are retrieved.
     *                          If <strong>false</strong> only bulk RNA-Seq are retrieved.
     *                          If <strong>null</strong> all RNA-Seq are retrieved.
     * @param offset            An {@code Integer} used to specify which row to start from retrieving data
     *                          in the result of a query. If null, retrieve data from the first row. If
     *                          not null, a limit should be also provided
     * @param limit             An {@code Integer} used to limit the number of rows returned in a query
     *                          result. If null, all results are returned.
     * @param attributes        A {@code Collection} of {@code Attribute}s to specify the information
     *                          to retrieve from the data source.
     * @return                  A {@code RNASeqExperimentTOResultSet} allowing to retrieve the
     *                          targeted {@code RNASeqExperimentTO}s.
     * @throws DAOException     If an error occurred while accessing the data source.
     */
    public RNASeqExperimentTOResultSet getExperiments(Collection<DAORawDataFilter> rawDataFilters,
            Boolean isSingleCell, Integer offset, Integer limit,
            Collection<Attribute> attributes) throws DAOException;

    /**
     * Allows to retrieve {@code RNASeqExperimentTO}s according to the provided filters.
     * <p>
     * The {@code RNASeqExperimentTO}s are retrieved and returned as a
     * {@code RNASeqExperimentTOResultSet}. It is the responsibility of the caller to close this
     * {@code DAOResultSet} once results are retrieved.
     *
     * @param rawDataFilters    A {@code Collection} of {@code DAORawDataFilter} allowing to specify
     *                          how to filter experiments to retrieve. The query uses AND between
     *                          elements of a same filter and uses OR between filters.
     * @param offset            An {@code Integer} used to specify which row to start from retrieving data
     *                          in the result of a query. If null, retrieve data from the first row. If
     *                          not null, a limit should be also provided
     * @param limit             An {@code Integer} used to limit the number of rows returned in a query
     *                          result. If null, all results are returned.
     * @param attributes        A {@code Collection} of {@code Attribute}s to specify the information
     *                          to retrieve from the data source.
     * @return                  A {@code RNASeqExperimentTOResultSet} allowing to retrieve the
     *                          targeted {@code RNASeqExperimentTO}s.
     * @throws DAOException     If an error occurred while accessing the data source.
     */
    public RNASeqExperimentTOResultSet getExperiments(Collection<DAORawDataFilter> rawDataFilters,
            Integer offset, Integer limit, Collection<Attribute> attributes) throws DAOException;
    /**
     * {@code DAOResultSet} for {@code RNASeqExperimentTO}s
     * 
     * @author  Frederic Bastian
     * @version Bgee 14, Sept. 2018
     * @since   Bgee 14, Sept. 2018
     */
    public interface RNASeqExperimentTOResultSet extends DAOResultSet<RNASeqExperimentTO> {
    }

    /**
     * {@code TransferObject} RNA-Seq experiments.
     * 
     * @author Frederic Bastian
     * @author Valentine Rech de Laval
     * @version Bgee 14
     * @since Bgee 12
     */
    public final class RNASeqExperimentTO extends ExperimentTO<String> {
        private static final long serialVersionUID = 9129478756981348941L;

        public RNASeqExperimentTO(String id, String name, String description,
                Integer dataSourceId) {
            super(id, name, description, dataSourceId);
        }
    }
}