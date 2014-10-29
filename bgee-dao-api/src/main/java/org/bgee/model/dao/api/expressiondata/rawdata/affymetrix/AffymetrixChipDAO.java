package org.bgee.model.dao.api.expressiondata.rawdata.affymetrix;

import java.io.Serializable;

import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataAnnotatedTO;

/**
 * DAO defining queries using or retrieving {@link AffymetrixChipTO}s. 
 * 
 * @author Frederic Bastian
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @since Bgee 01
 */
public interface AffymetrixChipDAO {
    /**
     * Retrieve from a data source a {@code AffymetrixChipTO}, corresponding to 
     * the Affymetrix chip, with the Bgee chip ID {@code bgeeAffymetrixChipId}, 
     * {@code null} if no corresponding chip was found.  
     * 
     * @param bgeeAffymetrixChipId	 	A {@code String} representing the ID 
     * 									in the Bgee database of the Affymetrix chip 
     * 									that needs to be retrieved from the data source. 
     * @return	An {@code AffymetrixChipTO}, encapsulating all the data 
     * 			related to the Affymetrix chip, {@code null} if none could be found. 
     * @throws DAOException 	If an error occurred when accessing the data source.
     */
    public AffymetrixChipTO getAffymetrixChipById(String bgeeAffymetrixChipId) 
            throws DAOException;

    /**
     * {@code TransferObject} for the class 
     * {@link org.bgee.model.expressiondata.rawdata.affymetrix.AffymetrixChip}.
     * <p>
     * For information on this {@code TransferObject} and its fields, 
     * see the corresponding class.
     * 
     * @author Frederic Bastian
     * @version Bgee 13
     * @see org.bgee.model.expressiondata.rawdata.affymetrix.AffymetrixChip
     * @since Bgee 11
     */
    public final class AffymetrixChipTO extends RawDataAnnotatedTO implements Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = 1434334L;

        public String microarrayExperimentId;
        public String affymetrixChipId;

        public String chipType;
        public String normalizationType;
        public String detectionType;

        /**
         * Default constructor. 
         */
        public AffymetrixChipTO() {
            super();
            this.microarrayExperimentId = null;
            this.affymetrixChipId = null;
            this.chipType = null;
            this.normalizationType = null;
            this.detectionType = null;
        }
    }
}
