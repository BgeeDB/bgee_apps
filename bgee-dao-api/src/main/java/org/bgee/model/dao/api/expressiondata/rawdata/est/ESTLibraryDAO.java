package org.bgee.model.dao.api.expressiondata.rawdata.est;

import org.bgee.model.dao.api.expressiondata.rawdata.RawDataAnnotatedTO;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataAssayDAO.AssayTO;

/**
 * DAO defining queries using or retrieving {@link ESTLibraryTO}s. 
 * 
 * @author Frederic Bastian
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @see ESTLibraryTO
 * @since Bgee 01
 */
public interface ESTLibraryDAO {

    /**
     * {@code TransferObject} for the class 
     * {@link org.bgee.model.expressiondata.rawdata.est.ESTLibrary}.
     * <p>
     * For information on this {@code TransferObject} and its fields, 
     * see the corresponding class.
     * 
     * @author Frederic Bastian
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @see org.bgee.model.expressiondata.rawdata.est.ESTLibrary
     * @since Bgee 11
     */
    public final class ESTLibraryTO extends AssayTO<Integer> implements RawDataAnnotatedTO {

        private static final long serialVersionUID = 42352L;

        public ESTLibraryTO(Integer id) {
            super(id);
            this.description = "";
            this.dataSourceId = "";
        }

        public String description;
        public String dataSourceId;

    }

}
