package org.bgee.model.dao.api.expressiondata.rawdata.insitu;

import org.bgee.model.dao.api.TransferObject;

/**
 * DAO defining queries using or retrieving {@link InSituEvidenceTO}s. 
 * 
 * @author Frederic Bastian
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @see InSituEvidenceTO
 * @since Bgee 01
 */
public interface InSituEvidenceDAO {

    /**
     * {@code TransferObject} for the class 
     * {@link org.bgee.model.expressiondata.rawdata.insitu.InSituEvidence}.
     * <p>
     * For information on this {@code TransferObject} and its fields, 
     * see the corresponding class.
     * 
     * @author Frederic Bastian
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @see org.bgee.model.expressiondata.rawdata.insitu.InSituEvidence
     * @since Bgee 11
     */
    //TODO: implements equals/hashCode
    public final class InSituEvidenceTO extends TransferObject {

        /**
         * 
         */
        private static final long serialVersionUID = 546546L;
        
        public String inSituExperimentId;
        
        public InSituEvidenceTO() {
            super();
        }
    }
}
