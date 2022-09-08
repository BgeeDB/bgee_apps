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
 * @author Frederic Bastian
 * @author Valentine Rech de Laval
 * @version Bgee 14
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
     * Retrieve from the data source a {@code RNASeqExpTO}, corresponding to the RNA-Seq
     * experiment with the ID {@code expId}, {@code null} if none could be found.
     * 
     * @param expIds            A {@code Collection} of {code String} representing the IDs of
     *                          the RNA-Seq experiments to retrieve from the data source.
     * @param attributes        A {@code Collection} of {@code Attribute}s to specify the information
     *                          to retrieve from the data source.
     * @return                  A {@code RNASeqExperimentTOResultSet}, encapsulating all the data
     *                          related to the RNA-Seq experiments, {@code null} if none could be found.
     * @throws DAOException     If an error occurred when accessing the data source.
     */
    public RNASeqExperimentTOResultSet getExperimentsFromIds(Collection<String> expIds,
            Collection<Attribute> attributes) throws DAOException;

    /**
     * Retrieve from the data source a {@code RNASeqExpTO}, corresponding to the RNA-Seq
     * experiment with the ID {@code expId}, {@code null} if none could be found.
     * 
     * @param rawDataFilter     A {@code DAORawDataFilter} allowing to specify which probesets to
     *                          retrieve.
     * @param attributes        A {@code Collection} of {@code Attribute}s to specify the information
     *                          to retrieve from the data source.
     * @return                  A {@code RNASeqExperimentTOResultSet}, encapsulating all the data
     *                          related to the RNA-Seq experiments, {@code null} if none could be
     *                          found.
     * @throws DAOException     If an error occurred when accessing the data source.
     */
    public RNASeqExperimentTOResultSet getExperimentsFromRawDataFilter(
            DAORawDataFilter rawDataFilter,
            Collection<Attribute> attributes) throws DAOException;
    
    /**
     * Retrieve from the data source a {@code RNASeqExpTO}, corresponding to the RNA-Seq
     * experiment with the ID {@code expId}, {@code null} if none could be found.
     * 
     * @param expIds            A {@code Collection} of {code String} representing the IDs of
     *                          the RNA-Seq experiments to retrieve from the data source.
     * @param libraryIds        A {@code Collection} of {code String} representing the IDs of
     *                          the RNA-Seq libraries to retrieve from the data source.
     * @param rawDataFilter     a {@code DAORawDataFilter} allowing to specify which probesets to
     *                          retrieve.
     * @param attributes        A {@code Collection} of {@code Attribute}s to specify the information
     *                          to retrieve from the data source.
     * @return                  A {@code RNASeqExperimentTOResultSet}, encapsulating all the data
     *                          related to the RNA-Seq experiments, {@code null} if none could be found.
     * @throws DAOException     If an error occurred when accessing the data source.
     */
    public RNASeqExperimentTOResultSet getExperiments(Collection<String> expIds, 
            Collection<String> explibraryIds, DAORawDataFilter filter,
            Collection<Attribute> attributes) throws DAOException;

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