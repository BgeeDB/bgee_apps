package org.bgee.model.dao.api.expressiondata.rawdata.est;

import java.io.Serializable;

import org.bgee.model.dao.api.expressiondata.rawdata.CallSourceRawDataTO;

/**
 * DAO defining queries using or retrieving {@link ESTTO}s. 
 * 
 * @author Frederic Bastian
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @see ESTTO
 * @since Bgee 01
 */
public interface ESTDAO {
    
    /**
     * {@code TransferObject} for the class 
     * {@link org.bgee.model.expressiondata.rawdata.est.EST}.
     * <p>
     * For information on this {@code TransferObject} and its fields, 
     * see the corresponding class.
     * 
     * @author Frederic Bastian
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @see org.bgee.model.expressiondata.rawdata.est.EST
     * @since Bgee 11
     */
    public final class ESTTO extends CallSourceRawDataTO implements Serializable {
        /**
         * 
         */
        private static final long serialVersionUID = 12343L;

        public ESTTO() {
            super();
            this.estLibraryId = null;
        }
        
        public String estLibraryId;
    }
}
