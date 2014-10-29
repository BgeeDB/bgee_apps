package org.bgee.model.dao.api.expressiondata.rawdata.rnaseq;

import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.exception.DAOException;

/**
 * {@code DAO} related to RNA-Seq experiments, using {@link RNASeqExpTO}s 
 * to communicate with the client.
 * 
 * @author Frederic Bastian
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @see RNASeqExpTO
 * @since Bgee 12
 */
public interface RNASeqExpDAO {
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
    public RNASeqExpTO getExperimentById(String expId) throws DAOException;

    /**
     * {@code TransferObject} for the class 
     * {@link org.bgee.model.expressiondata.rawdata.rnaseq.RNASeqExp}.
     * <p>
     * For information on this {@code TransferObject} and its fields, 
     * see the corresponding class.
     * 
     * @author Frederic Bastian
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @see org.bgee.model.expressiondata.rawdata.rnaseq.RNASeqExp
     * @since Bgee 12
     */
    public final class RNASeqExpTO extends TransferObject {

        /**
         * 
         */
        private static final long serialVersionUID = 17567458L;

        /**
         * A {@code String} containing the description of the RNA-Seq experiment. 
         */
        public String description;
        /**
         * A {@code String} representing the ID of the data source 
         * where this RNA-Seq experiment comes from. 
         */
        public String dataSourceId;

        /**
         * Default constructor. 
         */
        public RNASeqExpTO() {
            super();
            this.description = "";
            this.dataSourceId = "";
        }

    }
}
