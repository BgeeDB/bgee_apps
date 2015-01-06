package org.bgee.model.dao.api.expressiondata.rawdata;

import org.bgee.model.dao.api.DAO;
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
public interface RawDataAnnotatedDAO extends DAO<RawDataAnnotatedDAO.Attribute> {
    
    /**
     * {@code Enum} used to define the attributes to populate in the {@code RawDataAnnotatedTO}s 
     * obtained from this {@code RawDataAnnotatedDAO}.
     * <ul>
     * <li>{@code ORGAN_ID}: corresponds to {@link RawDataAnnotatedTO#getOrganId()}.
     * <li>{@code STAGE_ID}: corresponds to {@link RawDataAnnotatedTO#getStageId()}.
     * </ul>
     * @see org.bgee.model.dao.api.DAO#setAttributes(Collection)
     * @see org.bgee.model.dao.api.DAO#setAttributes(Enum[])
     * @see org.bgee.model.dao.api.DAO#clearAttributes()
     */
    public enum Attribute implements DAO.Attribute {
        ORGAN_ID, STAGE_ID;
    }

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
    public class RawDataAnnotatedTO extends TransferObject {
        
        public String organId;
        public String stageId;
        
        /**
         * Constructor providing the organ ID and the stage ID of this raw data annotated.
         * <p>
         * All of these parameters are optional, so they can be {@code null} when not used.
         * 
         * @param organId   A {@code String} that is the ID of the organ of this raw data annotated.
         * @param stageId   A {@code String} that is the ID of the stage of this raw data annotated.
         */
        public RawDataAnnotatedTO() {
            super();
            this.organId = "";
            this.stageId = "";
        }
        
        /**
         * @return the {@code String} representing the ID of the organ of this raw data annotated.
         */
        public String getOrganId() {
            return this.organId;
        }

        /**
         * @return the {@code String} representing the ID of the stage of this raw data annotated.
         */
        public String getStageId() {
            return this.stageId;
        }

    }
}
