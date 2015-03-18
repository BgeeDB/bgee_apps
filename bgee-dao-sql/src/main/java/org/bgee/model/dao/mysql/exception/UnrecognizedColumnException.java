package org.bgee.model.dao.mysql.exception;

import org.bgee.model.dao.api.exception.DAOException;

/**
 * A {@code DAOException} thrown when a column in a MySQL query is not recognized. 
 * This kind of {@code DAOException} should usually not be thrown in production.
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Mar. 2015
 * @since Bgee 13
 */
public class UnrecognizedColumnException extends DAOException {
    private static final long serialVersionUID = -1709652334217623271L;

    public UnrecognizedColumnException(String columnName) {
        super("Unrecognized column in query: " + columnName);
    }
}
