package org.bgee.model.dao.api;

import org.bgee.model.dao.api.exception.DAOException;

/**
 * A {@code DAOResultSet} allows to retrieve results obtained from a {@code DAO}, 
 * one {@code TransferObject} at a time. This is useful because many queries in Bgee 
 * can return thousands of results, and can lead to overload the memory if they 
 * are all retrieved at once. By using a {@code DAOResultSet}, applicative code 
 * can obtain only a part of the results, to deal with it, before moving to 
 * the next part of the results. 
 * <p>
 * A {@code DAOResultSet} object maintains a cursor pointing to the current result 
 * to retrieve. Initially, the cursor is positioned before the first result. 
 * The {@link #next()} method moves the cursor to the next result, and because it returns 
 * {@code false} when there are no more results to retrieve, it can be used in 
 * a while loop to iterate through the result set. When the cursor is positioned 
 * on a result, the method {@link #getTO()} can be called to obtain the result 
 * as a {@code TransferObject}.
 * <p>
 * Clients must call the method {@link #close()} when they are done using 
 * a {@code DAOResultSet}.
 * <p>
 * As an example, if the {@code DAO} used is using a SQL database, then each result 
 * of a {@code DAOResultSet} would correspond to one row in the database. 
 * If the {@code DAO} is using a text file retrieved from a webservice, then each 
 * result would correspond to one line in the file. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 *
 * @param <T>   The type of {@code TransferObject} that can be obtained 
 *              from this {@code DAOResultSet}.
 */
public interface DAOResultSet<T extends TransferObject> extends AutoCloseable {
    /**
     * Moves the cursor forward from its current position to the next result. 
     * A {@code DAOResultSet} cursor is initially positioned before the first result; 
     * the first call to the method {@code next} makes the first result the current 
     * position; the second call makes the second result the current position, 
     * and so on. A call to {@link #getTO()} allows to obtain the result 
     * corresponding to the current position.
     * <p>
     * When a call to the {@code next} method returns {@code false}, the cursor 
     * is positioned after the last result. Calling {@link #getTO()} if the cursor 
     * is not positioned on a result will throw an {@code DAOException}. 
     * 
     * @return  {@code true} if the new current position allows to obtain a result; 
     *          {@code false} if there are no more results to retrieve. 
     * @throws DAOException If a {@code DAO} access occurs. 
     */
    public boolean next() throws DAOException;
    /**
     * Returns the result corresponding to the current cursor position of this 
     * {@code DAOResultSet} (see {@link #next()}) as a {@code TransferObject} {@code T}. 
     * If the cursor is not positioned on a result when this method is called, 
     * it throws an {@code DAOException}.
     * 
     * @return  The {@code TransferObject} {@code T} corresponding to the result 
     *          at the current cursor position of this {@code DAOResultSet}.
     * @throws DAOException If an error occurs while retrieving the result.
     */
    public T getTO() throws DAOException;
    /**
     * Close this {@code DAOResultSet} and release all underlying resources used 
     * to generate it.
     * @throws DAOException If a {@code DAO} access occurs. 
     */
    public void close() throws DAOException;
}
