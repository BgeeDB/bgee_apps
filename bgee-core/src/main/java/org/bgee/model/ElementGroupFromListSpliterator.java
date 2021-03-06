package org.bgee.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A {@code Spliterator} allowing to obtain elements of a {@code Stream}
 * grouped according to a criterion, when the source {@code Stream} is sorted
 * according to this same criterion. For instance, to retrieve expression calls
 * grouped by genes, when the source stream of expression calls is sorted by gene.
 *
 * @author Frederic Bastian
 * @since Bgee 14 Feb. 2019
 * @version Bgee 14 Feb. 2019
 *
 * @param <T>   The type of elements in the source {@code Stream}.
 * @param <U>   The type of the criterion that will be used for grouping the elements
 *              of the source {@code Stream} (for instance, an Integer that would be the type
 *              of the Bgee gene IDs extracted from expression calls in the source stream).
 *              The source {@code Stream} must be sorted according to the same criterion.
 */
public class ElementGroupFromListSpliterator<T, U>
extends Spliterators.AbstractSpliterator<List<T>> {
    private final static Logger log = LogManager.getLogger(ElementGroupFromListSpliterator.class.getName());

    /**
     * A {@code Function} allowing to retrieve entities {@code U} from elements {@code T}.
     */
    private final Function<T, U> extractEntityFunction;
    /**
     * A {@code Comparator} only to verify that {@code T} {@code Stream} elements are properly ordered,
     * based on the entities {@code U} retrieved from {@code T} elements.
     */
    private final Comparator<T> elementComparator;
    private final Stream<T> elementStream;

    private Iterator<T> elementIterator;
    private T lastElementIterated;
    private boolean isInitiated;
    private boolean isClosed;
    
    public ElementGroupFromListSpliterator(Stream<T> elementsOrderedByEntity, Function<T, U> extractEntityFunction,
            Comparator<U> entityComparator) {
        super(Long.MAX_VALUE, Spliterator.ORDERED | Spliterator.IMMUTABLE 
                | Spliterator.DISTINCT | Spliterator.NONNULL | Spliterator.SORTED);

        this.extractEntityFunction = extractEntityFunction;
        this.elementComparator = Comparator.comparing(extractEntityFunction,
                Comparator.nullsLast(entityComparator));
        this.elementStream = elementsOrderedByEntity;

        this.elementIterator = null;
        this.lastElementIterated = null;
        this.isInitiated = false;
        this.isClosed = false;
    }

    @Override
    public boolean tryAdvance(Consumer<? super List<T>> action) {
        log.entry(action);

        if (this.isClosed) {
            throw log.throwing(new IllegalStateException("Already close"));
        }

        // Lazy loading: we do not get stream iterators (terminal operation)
        // before tryAdvance() is called.
        if (!this.isInitiated) {
            //set it first because method can return false and exist the block
            this.isInitiated = true;
            
            this.elementIterator = this.elementStream.iterator();
            try {
                this.lastElementIterated = this.elementIterator.next();
            } catch (NoSuchElementException e) {
                log.catching(Level.DEBUG, e);
                return log.traceExit(false);
            }
        }
        //if already initialized, no calls retrieved, but method called again (should never happen, 
        //as the method would have returned false during initialization above)
        if (this.lastElementIterated == null) {
            log.warn("Stream used again despite having no elements.");
            return log.traceExit(false);
        }

        //This List is the output generated by this Stream, on which the Consumer is applied.
        //It retrieves all elements having the same entity, from the list of elements ordered by this entity
        final List<T> result =  new ArrayList<T>();

        //we iterate the elements and stop when we reach the next entity, or when 
        //there is no more element, then we do a last iteration after the last element is 
        //retrieved, to properly group all the elements. This is why we use the boolean currentIteration.
        //This loop always work on this.lastElementIterated, which has already been populated at this point.
        boolean currentIteration = true;
        while (currentIteration) {
            U lastEntity = this.extractEntityFunction.apply(this.lastElementIterated);
            if (lastEntity == null) {
                throw log.throwing(new IllegalStateException("Missing required attributes in element: "
                    + this.lastElementIterated));
            }
            // We add the previous element to the resulting List
            result.add(this.lastElementIterated);
            
            T currentElement = null;
            //try-catch to avoid calling both hasNext and next
            try {
                currentElement = this.elementIterator.next();
                currentIteration = true;
            } catch (NoSuchElementException e) {
                currentIteration = false;
            }
            //the elements are supposed to be ordered according to the comparator provided at instantiation,
            //base on the entities U extracted from the elements T
            if (currentElement != null && this.elementComparator.compare(this.lastElementIterated, currentElement) > 0) {
                throw log.throwing(new IllegalStateException("The elements "
                    + "were not retrieved in correct order, which is mandatory "
                    + "for proper grouping. Previous element: "
                    + this.lastElementIterated + ", current element: " + currentElement));
            }
            log.trace("Previous element={} - Current element={}", this.lastElementIterated, currentElement);

            //if the entity changes, or if it is the latest iteration (one iteration after
            //the last element was retrieved, this.elementIterator.next() threw an exception),
            //we generate the List grouping elements for the same entity,
            //as all elements were iterated for that entity.
            U currentEntity = null;
            if (currentElement != null) {
                currentEntity = this.extractEntityFunction.apply(currentElement);
            }
            if (!currentIteration || !currentEntity.equals(lastEntity)) {
                assert (currentIteration && currentElement != null) || (!currentIteration && currentElement == null);
                currentIteration = false;
                action.accept(result); //method will exit after accepting the action
                log.trace("Done accumulating data for {}", lastEntity);
            }
            
            //Important that this line is executed at every iteration, 
            //so that it is set to null when there is no more data
            this.lastElementIterated = currentElement;
        }
        
        if (this.lastElementIterated != null) {
            return log.traceExit(true);
        }
        return log.traceExit(false);
    }

    /**
     * Return {@code null}, because this {@code Spliterator} does not have 
     * the capability of being accessed in parallel. 
     * 
     * @return  The {@code Spliterator} that is {@code null}.
     */
    @Override
    public Spliterator<List<T>> trySplit() {
        log.traceEntry();
        return log.traceExit((Spliterator<List<T>>) null);
    }
    
    @Override
    public Comparator<? super List<T>> getComparator() {
        log.traceEntry();
        //An element of the Stream is a List<T>, where all the Ts have a same entity U,
        //so retrieving the first T of the List is enough to extract the entity U and order the Lists
        //(Done by the Comparator this.elementComparator)
        return log.traceExit(Comparator.comparing(l -> ((List<T>) l).get(0), 
            Comparator.nullsLast(this.elementComparator)));
    }

    /** 
     * Close the {@code Stream} provided at instantiation, if not already done.
     */
    public void close() {
        log.traceEntry();
        if (!isClosed){
            try {
                this.elementStream.close();
            } finally {
                this.isClosed = true;
            }
        }
        log.traceExit();
    }
}