package org.bgee.model.dao.api;

import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
 * It is also possible to obtain a {@code Stream} to traverse the results by calling 
 * {@link #stream()}.
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
 * A {@code DAOResultSet} is also closed when a {@code Stream} obtained from the {@link #stream()} 
 * method is closed.
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
 * @version Bgee 13 Sept. 2015
 * @since Bgee 13
 *
 * @param <T>   The type of {@code TransferObject} that can be obtained 
 *              from this {@code DAOResultSet}.
 */
public interface DAOResultSet<T extends TransferObject> extends AutoCloseable {
    /**
     * A {@code Spliterator} allowing to stream over {@code TransferObject}s 
     * obtained from a {@code DAOResultSet}. 
     * This {@code Spliterator} is finite, sequential, ordered, and unsized, and it does not override 
     * the {@code Spliterator#forEachRemaining(Consumer)} default method. 
     * It is not sized, as we have no guarantee that implementations can determine 
     * the total number of results before traversal. 
     * Service provider should override this class and the {@link DAOResultSet#stream()} 
     * default method if they want to change these behaviors .
     * 
     * @author Frederic Bastian
     * @version Bgee 13 Sept. 2015
     * @see DAOResultSet#stream()
     * @since Bgee 13 Sept 2015
     *
     * @param <T>   The type of {@code TransferObject} processed by this {@code Spliterator}.
     */
    class DAOResultSetSpliterator<T extends TransferObject> implements Spliterator<T> {
        
        private final static Logger log = LogManager.getLogger(DAOResultSetSpliterator.class.getName());
        
        /**
         * The {@code DAOResultSet} to use to traverse the results. It would have been simpler 
         * to just be able to call methods of an outer {@code DAOResultSet} instance, 
         * but interfaces cannot have non-static inner classes, so we need to store it.
         */
        private final DAOResultSet<T> daoRs;
        
        /**
         * 0-arg constructor, mainly in case a service provider needs to extend this class, 
         * and does not need to be provided with a {@code DAOResultSet} instance 
         * (see {@link #DAOResultSetSpliterator(DAOResultSet)}).
         */
        public DAOResultSetSpliterator() {
            this(null);
        }
        /**
         * Constructor providing the {@code DAOResultSet} used to traverse results. 
         * This is because members of an interface can only be static, so we cannot call 
         * methods from an outer {@code DAOResultSet} class, so we need to be provided 
         * with one. It is recommended that the provided {@code DAOResultSet} has a late-binding 
         * behavior (doesn't bind to the data source until method {@code next} is called).
         * 
         * @param rs    The {@code DAOResultSet} to use to traverse the results. 
         */
        public DAOResultSetSpliterator(DAOResultSet<T> rs) {
            this.daoRs = rs;
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> action) {
            log.entry(action);
            if (this.daoRs.next()) {
                action.accept(this.daoRs.getTO());
                return log.exit(true);
            }
            return log.exit(false);
        }

        /**
         * Return {@code null}, because by default a {@code DAOResultSet} does not have 
         * to provide the capability of being accessed in parallel. Service providers 
         * should extend this class and override this method if their implementation 
         * can be accessed in parallel.
         * 
         * @return  A {@code Spliterator} that is {@code null}.
         */
        @Override
        public Spliterator<T> trySplit() {
            return null;
        }

        /**
         * Returns {@code Long.MAX_VALUE}, meaning that we have no idea of the size 
         * of the result set to traverse. Service providers should extend this class 
         * and override this method if the number of results is known before traversal. 
         * 
         * @return  A {@code long} equal to Long.MAX_VALUE. 
         */
        @Override
        public long estimateSize() {
            return Long.MAX_VALUE;
        }

        /**
         * This {@code DAOResultSetSpliterator} has only the following characteristics: 
         * {@code NONNULL} and {@code ORDERED}. Service providers should extend this class 
         * and override this method if their implementation of {@code DAOResultSet} 
         * can be traversed differently (thus, having for instance the characteristics 
         * {@code SIZED}, or {@code CONCURRENT}).
         * 
         * @return  An {@code int} representing the ORed values from the characteristics 
         *          of this {@code DAOResultSetSpliterator}.
         */
        @Override
        public int characteristics() {
            return Spliterator.NONNULL | Spliterator.ORDERED;
        }
        
    }
    
    /**
     * Returns a {@code Stream} with this {@code DAOResultSet} as source. 
     * It allows to traverse all the {@code T}s that can be obtained from this {@code DAOResultSet}. 
     * When the {@code Stream} is consumed, this {@code DAOResultSet} is closed, and it is thus 
     * not possible to call methods such as {@code getTO} anymore (would throw an exception).
     * Also, when the {@code close} method is called on the {@code Stream}, {@code close} is also called 
     * on this {@code DAOResultSet}. 
     * <p>
     * The default implementation traverse the {@code T}s exactly as if the methods 
     * {@link #next()} and {@link #getTO()} were called sequentially on this {@code DAOResultSet}, 
     * until the {@code next} method returns {@code false}. The default implementation 
     * is based on a {@code Spliterator} that is finite, sequential, ordered, unsized 
     * (see {@link DAOResultSetSpliterator}).
     * <p>
     * Implementations should make sure that the returned {@code Stream} has a close handler 
     * that closes this {@code DAOResultSet} (see method {@code BaseStream#onClose(Runnable)}).
     * 
     * @return  A {@code Stream} over the {@code T}s obtained from this {@code DAOResultSet}. 
     *          When it is closed, this {@code DAOResultSet} is also closed.
     */
    default Stream<T> stream() {
        //no Logger used, because either we need to make a DAOResultSet Logger public, 
        //or we need to instantiate it each time this method is called.
        //We add a close handler to the Stream, to close this DAOResultSet when the Stream is closed
        return StreamSupport.stream(new DAOResultSetSpliterator<T>(this), false)
                .onClose(() -> this.close());
    }
    
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
