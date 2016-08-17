package org.bgee.view;

import java.util.List;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.TransferObject;

/**
 * Interface defining the methods implemented by view rendering results of queries to DAOs, 
 * allowing to use them as a webservice. It is the only view allowed to access some classes 
 * from the DAO layer. 
 * 
 * @author  Frederic Bastian
 * @version Bgee 13 Mar 2016
 * @since   Bgee 13
 */
public interface DAODisplay {
    /**
     * Display the {@code TransferObject}s returned by the provided {@code DAOResultSet}. 
     * Only the fields corresponding to the provided {@code DAO.Attribute}s are printed. 
     * 
     * @param attributes    A {@code List} of {@code DAO.Attribute}s, specifying the fields 
     *                      of the {@code TransferObject}s to be printed, and their display order.
     * @param resultSet     A {@code DAOResultSet} to iterate to print {@code TransferObject}s.
     */
    public <T extends Enum<T> & DAO.Attribute, U extends TransferObject> void displayTOs(
            List<T> attributes, DAOResultSet<U> resultSet);
}
