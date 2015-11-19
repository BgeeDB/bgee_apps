package org.bgee.model;

import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.TransferObject;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.mockito.ArgumentMatcher;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Parent class of all classes implementing unit testing. 
 * It allows to automatically log starting, succeeded and failed tests.
 * 
 * @author Frederic Bastian
 * @version Bgee 13, Mar 2013
 * @since Bgee 13
 */
public abstract class TestAncestor {
    /**
     * Get a mock {@code DAOResultSet} configured to returned the provided {@code TransferObject}s.
     * 
     * @param resultSetType A {@code Class} that is the type of {@code DAOResultSet} to return.
     * @param values        A {@code List} of {@code TransferObject}s to be returned by 
     *                      the mock {@code DAOResultSet}.
     * @return              A configured mock {@code DAOResultSet}.
     * @param T             The type of {@code TransferObject} to return.
     * @param U             The type of {@code DAOResultSet} to return.
     */
    protected static <T extends TransferObject, U extends DAOResultSet<T>> U getMockResultSet(
            Class<U> resultSetType, List<T> values) {
        /**
         * An {@code Answer} to manage calls to {@code next} method.
         */
        final class ResultSetNextAnswer implements Answer<Boolean> {
            /**
             * An {@code int} that is the number of results to be returned by this {@code Answer}.
             */
            private final int size;
            /**
             * An {@code int} defining the current iteration (starts at -1, 
             * so that the first call to next put the cursor on the first result).
             */
            private int iteration;
            
            private ResultSetNextAnswer(int size) {
                this.iteration = -1;
                this.size = size;
            }
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                this.iteration++;
                if (this.iteration < this.size) {
                    return true;
                }
                return false;
            }
        }
        /**
         * An {@code Answer} to manage calls to {@code getTO} method.
         */
        final class ResultSetGetTOAnswer implements Answer<T> {
            /**
             * A {@code List} of {@code T}s to be returned by the mock {@code DAOResultSet}.
             */
            private final List<T> values;
            /**
             * The {@code Answer} used by the same mock {@code DAOResultSet} to respond to {@code next()}.
             * Allows to know which element to return;
             */
            private final ResultSetNextAnswer answerToNext;
            
            private ResultSetGetTOAnswer(List<T> values, ResultSetNextAnswer answerToNext) {
                this.values = values;
                this.answerToNext = answerToNext;
            }
            @Override
            public T answer(InvocationOnMock invocation) throws Throwable {
                if (this.answerToNext.iteration >= 0 && 
                        this.answerToNext.iteration < this.values.size()) {
                    return this.values.get(this.answerToNext.iteration);
                }
                return null;
            }
        }
        
        List<T> clonedValues = new ArrayList<>(values);
        U rs = mock(resultSetType);
        ResultSetNextAnswer nextAnswer = new ResultSetNextAnswer(clonedValues.size());
        ResultSetGetTOAnswer getTOAnswer = new ResultSetGetTOAnswer(clonedValues, nextAnswer);
        
        when(rs.next()).thenAnswer(nextAnswer);
        when(rs.getTO()).thenAnswer(getTOAnswer);
        //XXX: note that the normal behavior of stream() and getAllTOs is to throw an exception 
        //if next was already called on this resultset. But that would need yet another 
        //custom Answer class...
        when(rs.stream()).thenReturn(clonedValues.stream());
        when(rs.getAllTOs()).thenReturn(clonedValues);
        
        return rs;
    }
    
    /**
     * An {@code ArgumentMatcher} allowing to determine whether two {@code Collection}s 
     * contains the same elements (as considered by their {@code equals} method), 
     * independently of the iteration order of the {@code Collection}s.
     */
    protected static class IsCollectionEqual<T> extends ArgumentMatcher<Collection<T>> {
        private final Collection<?> expectedCollection;
        
        IsCollectionEqual(Collection<?> expectedCollection) {
            this.expectedCollection = expectedCollection;
        }

        @Override
        public boolean matches(Object argument) {
            if (expectedCollection == argument) {
                return true;
            }
            if (expectedCollection == null) {
                if (argument == null) {
                    return true;
                } 
                return false;
            } else if (argument == null) {
                return false;
            }
            if (!(argument instanceof Collection)) {
                return false;
            }
            Collection<?> arg = (Collection<?>) argument;
            if (arg.size() != expectedCollection.size()) {
                return false;
            }
            return arg.containsAll(expectedCollection);
        }
    }
    /**
     * Helper method to obtain a {@link IsCollectionEqual} {@code ArgumentMatcher}, 
     * for readability. 
     * @param expectedCollection    The {@code Collection} that is expected, to be used 
     *                              in stub or verify methods. 
     */
    protected static <T> Collection<T> collectionEq(Collection<T> expectedCollection) {
        return argThat(new IsCollectionEqual<>(expectedCollection));
    }
    
	/**
	 * Default Constructor. 
	 */
	public TestAncestor() {
		
	}
	/**
	 * A {@code TestWatcher} to log starting, succeeded and failed tests. 
	 */
	@Rule
	public TestWatcher watchman = new TestWatcher() {
	    @Override
	    protected void starting(Description description) {
	    	getLogger().info("Starting test: {}", description);
	    }
	    @Override
	    protected void failed(Throwable e, Description description) {
	    	if (getLogger().isErrorEnabled()) {
	    		getLogger().error("Test failed: " + description, e);
	    	}
	    }
	    @Override
	    protected void succeeded(Description description) {
	    	getLogger().info("Test succeeded: {}", description);
	    }
	};
	
	/**
	 * Return the logger of the class. 
	 * @return 	A {@code Logger}
	 */
	protected Logger getLogger() {
		 return LogManager.getLogger(this.getClass().getName());
	}
	
	
}
