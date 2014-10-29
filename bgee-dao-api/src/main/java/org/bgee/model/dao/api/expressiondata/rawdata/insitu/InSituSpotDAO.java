package org.bgee.model.dao.api.expressiondata.rawdata.insitu;

import java.io.Serializable;
import java.util.Set;

import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.rawdata.CallSourceRawDataTO;

/**
 * DAO defining queries using or retrieving {@link InSituSpotTO}s. 
 * 
 * @author Frederic Bastian
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @see InSituSpotTO
 * @since Bgee 01
 */
//TODO: extends DAO<InSituSpotDAO.Attribute> 
public interface InSituSpotDAO {
    
    /**
     * Remove link between some <em>in situ</em> spots and their associated no-expression 
     * call because of no-expression conflicts. The <em>in situ</em> spots will not be deleted, 
     * but their association to the specified no-expression calls will be. A reason 
     * for exclusion should be provided in the data source, such as 'noExpression conflict'.
     * 
     * @param noExprIds    A {@code Set} of {@code String}s that are the IDs of 
     *                     the no-expression calls in conflict, whose association to 
     *                     <em>in situ</em> spots should be removed. 
     * @return             An {@code int} that is the number of spots that were actually 
     *                     updated as a result of the call to this method. 
     * @throws IllegalArgumentException    If a no-expression call ID was not associated 
     *                                     to any spot. 
     * @throws DAOException                If an error occurred while updating the data. 
     */
    public int updateNoExpressionConflicts(Set<String> noExprIds) 
            throws DAOException, IllegalArgumentException;

    /**
     * {@code TransferObject} for the class 
     * {@link org.bgee.model.expressiondata.rawdata.insitu.InSituSpot}.
     * <p>
     * For information on this {@code TransferObject} and its fields, 
     * see the corresponding class.
     * 
     * @author Frederic Bastian
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @see org.bgee.model.expressiondata.rawdata.insitu.InSituSpot
     * @since Bgee 11
     */
    public class InSituSpotTO extends CallSourceRawDataTO implements Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = 12433455L;

        public String inSituEvidenceId;
        public String organId;
        public String stageId;

        public String detectionFlag;

        public InSituSpotTO() {
            super();
            this.detectionFlag = "undefined";
        }
    }
}
