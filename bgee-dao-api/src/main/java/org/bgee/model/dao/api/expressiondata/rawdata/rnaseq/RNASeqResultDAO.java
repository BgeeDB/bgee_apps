package org.bgee.model.dao.api.expressiondata.rawdata.rnaseq;

import java.util.Set;

import org.bgee.model.dao.api.exception.DAOException;

/**
 * {@code DAO} related to RNA-Seq experiments, using {@link RNASeqResultTO}s 
 * to communicate with the client.
 * 
 * @author Frederic Bastian
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
}
