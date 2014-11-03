package org.bgee.model.dao.api.expressiondata.rawdata.insitu;

import java.io.Serializable;
import java.util.Set;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.rawdata.CallSourceRawDataDAO.CallSourceRawDataTO;
import org.bgee.model.dao.api.expressiondata.rawdata.CallSourceRawDataDAO.CallSourceRawDataTO.DetectionFlag;
import org.bgee.model.dao.api.expressiondata.rawdata.CallSourceRawDataDAO.CallSourceRawDataTO.ExclusionReason;

/**
 * DAO defining queries using or retrieving {@link InSituSpotTO}s. 
 * 
 * @author Frederic Bastian
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @see InSituSpotTO
 * @since Bgee 01
 */
public interface InSituSpotDAO extends DAO<InSituSpotDAO.Attribute> {
    
    public enum Attribute implements DAO.Attribute {
        ID, INSITUEVIDENCEID, INSITUEXPRESSIONPATTERNID, ANATENTITYID, STAGEID, GENEID, 
        DETECTIONFLAG, EXPRESSIONID, NOEXPRESSIONID, INSITUDATA, REASONFOREXCLUSION;
    }
    
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
    /*
     * (non-javadoc)
     * This TO is not in it's final version. We need to known if CallSourceRawDataTO is necessary 
     * and consistent. Need to be thinking.
     */
    public class InSituSpotTO extends CallSourceRawDataTO implements Serializable {

        private static final long serialVersionUID = 12433455L;

        public final String inSituEvidenceId;
        public final String inSituExpressionPatternId;        
        public final String anatEntityId;
        public final String stageId;

        public InSituSpotTO(String inSituSpotId, String inSituEvidenceId, 
                String inSituExpressionPatternId, String anatEntityId, String stageId, 
                String geneId, DetectionFlag detectionFlag, String expressionId, 
                String noExpressionId, DataState inSituData, ExclusionReason reasonForExclusion) {
            super(inSituSpotId, geneId, detectionFlag, expressionId, noExpressionId, 
                    inSituData, reasonForExclusion);
            this.inSituEvidenceId = inSituEvidenceId;
            this.inSituExpressionPatternId = inSituExpressionPatternId;
            this.anatEntityId = anatEntityId;
            this.stageId = stageId;
        }
    }
}
