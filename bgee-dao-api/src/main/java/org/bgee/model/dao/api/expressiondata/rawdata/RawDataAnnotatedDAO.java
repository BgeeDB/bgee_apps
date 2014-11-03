package org.bgee.model.dao.api.expressiondata.rawdata;

import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.expressiondata.rawdata.CallSourceRawDataDAO.CallSourceRawDataTO;


/**
 * DAO defining queries using or retrieving {@link RawDataAnnotatedTO}s. 
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @see RawDataAnnotatedTO
 * @since Bgee 13
 */
//TODO extends DAO<RawDataAnnotatedDAO.Attribute>
public interface RawDataAnnotatedDAO {
    
    /**
     * {@code TransferObject} for the class 
     * {@link org.bgee.model.dao.api.expressiondata.rawdata.RawDataAnnotatedDAO}.
     * <p>
     * For information on this {@code TransferObject} and its fields, 
     * see the corresponding class.
     * 
     * @author Frederic Bastian
     * @version Bgee 13
     * @see org.bgee.model.dao.api.expressiondata.rawdata.RawDataAnnotatedDAO
     * @since Bgee 11
     */
    @Deprecated
    public class RawDataAnnotatedTO extends TransferObject
    {
        public String organId;
        public String stageId;
        
        public RawDataAnnotatedTO()
        {
            super();
            
            this.organId = "";
            this.stageId = "";
        }
    }
}
