package org.bgee.model.dao.api.expressiondata.rawdata.rnaseq;

import java.io.Serializable;
import java.util.Set;

import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.rawdata.CallSourceRawDataTO;

/**
 * {@code DAO} related to RNA-Seq experiments, using {@link RNASeqResultTO}s 
 * to communicate with the client.
 * 
 * @author Frederic Bastian
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @see RNASeqResultTO
 * @since Bgee 12
 */
//TODO: extends DAO<RNASeqResultDAO.Attribute> 
public interface RNASeqResultDAO {
    /**
     * Remove link between some RNA_Seq results and their associated no-expression 
     * call because of no-expression conflicts. The RNA_Seq results will not be deleted, 
     * but their association to the specified no-expression calls will be. A reason 
     * for exclusion should be provided in the data source, such as 'noExpression conflict'.
     * 
     * @param noExprIds    A {@code Set} of {@code String}s that are the IDs of 
     *                     the no-expression calls in conflict, whose association to 
     *                     RNA_Seq results should be removed. 
     * @return             An {@code int} that is the number of results that were actually 
     *                     updated as a result of the call to this method. 
     * @throws IllegalArgumentException    If a no-expression call ID was not associated 
     *                                     to any RNA_Seq result. 
     * @throws DAOException                If an error occurred while updating the data. 
     */
    public int updateNoExpressionConflicts(Set<String> noExprIds) 
            throws DAOException, IllegalArgumentException;


    /**
     * {@code TransferObject} for the class 
     * {@link org.bgee.model.expressiondata.rawdata.rnaseq.RNASeqResult}.
     * <p>
     * For information on this {@code TransferObject} and its fields, 
     * see the corresponding class.
     * 
     * @author Frederic Bastian
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @see org.bgee.model.expressiondata.rawdata.rnaseq.RNASeqResult
     * @since Bgee 12
     */
    public final class RNASeqResultTO extends CallSourceRawDataTO implements Serializable {
        private static final long serialVersionUID = 9192921864601490175L;

        /**
         * A {@code String} corresponding to the ID 
         * of the RNA-Seq library this result belongs to. 
         */
        public String rnaSeqLibraryId;
        /**
         * A {@code float} representing the log2 RPK 
         * (Reads Per Kilobase) for this gene in this library.
         */
        public float log2RPK;
        /**
         * An int representing the number of reads aligned to this gene 
         * in this library. 
         */
        public int readsCount;
        /**
         * A {@code String} representing the expression call for this gene 
         * in this library ('undefined', 'present', 'absent').
         * @TODO change this for an Enum.
         */
        public String detectionFlag;

        /**
         * Default constructor.
         */
        public RNASeqResultTO() {
            super();
            this.rnaSeqLibraryId = null;
            this.log2RPK = -999999;
            this.readsCount = 0;
            this.detectionFlag = "undefined";
        }
    }
}
