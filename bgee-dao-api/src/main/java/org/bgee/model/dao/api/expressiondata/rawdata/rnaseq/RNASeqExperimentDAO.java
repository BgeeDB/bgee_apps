package org.bgee.model.dao.api.expressiondata.rawdata.rnaseq;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.exception.DAOException;
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
        ID, NAME, DESCRIPTION, DATA_SOURCE_ID;
    }

    /**
     * Retrieve from the data source a {@code RNASeqExpTO},  
     * corresponding to the RNA-Seq experiment with the ID {@code expId}, 
     * {@code null} if none could be found.  
     * 
     * @param expId 		A {@code String} representing the ID 
     * 						of the RNA-Seq experiment to retrieved 
     * 						from the data source. 
     * @return	A {@code RNASeqExpTO}, encapsulating all the data 
     * 			related to the RNA-Seq experiment retrieved from the data source, 
     * 			or {@code null} if none could be found. 
     * @throws DAOException 	If an error occurred when accessing the data source.
     */
    public RNASeqExperimentTO getExperimentById(String expId) throws DAOException;

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

        public RNASeqExperimentTO(String id, String name, String description, Integer dataSourceId) {
            super(id, name, description, dataSourceId);
        }
    }
}