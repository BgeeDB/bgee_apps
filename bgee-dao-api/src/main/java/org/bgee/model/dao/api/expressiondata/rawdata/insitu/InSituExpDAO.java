package org.bgee.model.dao.api.expressiondata.rawdata.insitu;

import org.bgee.model.dao.api.TransferObject;

/**
 * DAO defining queries using or retrieving {@link InSituExpTO}s. 
 * 
 * @author Frederic Bastian
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @see InSituExpTO
 * @since Bgee 01
 */
public interface InSituExpDAO {

    /**
     * {@code TransferObject} for the class 
     * {@link org.bgee.model.expressiondata.rawdata.insitu.InSituExp}.
     * <p>
     * For information on this {@code TransferObject} and its fields, 
     * see the corresponding class.
     * 
     * @author Frederic Bastian
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @see org.bgee.model.expressiondata.rawdata.insitu.InSituExp
     * @since Bgee 11
     */
    public final class InSituExpTO extends TransferObject {

        /**
         * 
         */
        private static final long serialVersionUID = 656756L;

        public String description;

        public String dataSourceId;

        public InSituExpTO() {
            super();
            this.description = "";
            this.dataSourceId = "";
        }
    }
}
