package org.bgee.model.dao.api;

import java.util.List;

import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.exception.QueryInterruptedException;

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
 * When a call to the {@code next} method returns {@code false}, this 
 * {@code DAOResultSet} is closed, and all underlying resources used 
 * to generate it are released. Calling {@link #getTO()} would throw 
 * a {@code DAOException}. A {@code DAOResultSet} is not backward iterable.
 * {@code close} should still be called if the end of the result set was not reached 
 * (for instance, an exception was thrown before getting to the point where 
 * {@code next} returns {@code false}). But the could be accomplished at end 
 * of the applicative code, by calling {@link DAOManager#close()}, that would 
 * close all resources, including {@code DAOResultSet}.
 * <p>
 * As an example, if the {@code DAO} used is using a SQL database, then each result 
 * of a {@code DAOResultSet} would correspond to one row in the database. 
 * If the {@code DAO} is using a text file retrieved from a webservice, then each 
 * result would correspond to one line in the file. 
 * <p>
 * Of note, the implementation acting under the hood may need to perform several 
 * queries to respond to the {@code DAO} API call that returned this {@code DAOResultSet}. 
 * This could result in a "freeze" on a call to {@code next}. But this way, 
 * the implementation will not need to perform all the queries at once, and to store 
 * all results in memory. In any case, this is none of the business of the caller.
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
     * corresponding to the current position. A call to {@link #getTO()} allows to 
     * obtain all results.
     * <p>
     * When a call to the {@code next} method returns {@code false}, this 
     * {@code DAOResultSet} is closed, and all underlying resources used 
     * to generate it are released. Calling {@link #getTO()} would throw 
     * a {@code DAOException}. A {@code DAOResultSet} is not backward iterable.
     * 
     * @return  {@code true} if the new current position allows to obtain a result; 
     *          {@code false} if there are no more results to retrieve. 
     * @throws DAOException If an error occurred while iterating this {@code DAOResultSet}
     * @throws QueryInterruptionException   If the query was requested to be interrupted 
     *                                      following a call to {@link DAOManager#kill()} 
     *                                      or {@link DAOManager#kill(long)}.
     */
    public boolean next() throws DAOException, QueryInterruptedException;
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
     * Convenient method to retrieve all {@code TransferObject}s {@code T} returned by of this 
     * {@code DAOResultSet}. This method is useful because, usually, DAO methods returned a 
     * {@code DAOResultSet}, allowing to iterate the results without putting all of them in memory; 
     * this method can thus be used to retrieve all results at once, putting all of them in memory. 
     * <p>
     * This method will call {@link DAOResultSet#next()} as long as it returns {@code true}, 
     * and will store, in a {@code List}, the {@code TransferObject}s {@code T} returned by 
     * {@link DAOResultSet#getTO()}. It will then call {@link DAOResultSet#close()}, before 
     * returning the {@code List} of {@code TransferObject}s {@code T}.
     * 
     * @return          A {@code List} of {@code TransferObject}s {@code T}, retrieved by iterating 
     *                  the results of {@code resultSet}, and stored in the order 
     *                  they were retrieved. 
     * @throws DAOException if an error occurred while calling {@code next}, or {@code getTO}, 
     *                      or {@code close}, on {@code resultSet}. 
     */
    public List<T> getAllTOs() throws DAOException;
    /**
     * Close this {@code DAOResultSet} and release all underlying resources used 
     * to generate it.
     * @throws DAOException If a {@code DAO} access occurs. 
     */
    public void close() throws DAOException;
}
