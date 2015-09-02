package org.bgee.model.dao.api;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.exception.QueryInterruptedException;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTO;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the class {@link DAOResultSet}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Sept. 2015
 * @since Bgee 13 Sept. 2015
 */
public class DAOResultSetTest extends TestAncestor {
    private final static Logger log = LogManager.getLogger(DAOResultSetTest.class.getName());

    @Override
    protected Logger getLogger() {
        return log;
    }
    
    /**
     * Test the default method {@link DAOResultSet#stream()}.
     */
    @Test
    public void shouldStream() {
        /** 
         * We need to create a class implementing DAOResultSet, 
         * because as of Mockito 1.10.19, it is not possible to call real implementation of default methods. 
         * We use a DAOResultSet for whatever TO we want, in this case, GeneTO.
         */
        class TestResultSet implements DAOResultSet<GeneTO> {

            /**
             * An {@code int} allowing to track how many times the method {@code next} was called, 
             * and what to return accordingly.
             */
            private int callToNext = 0;
            /**
             * A {@code boolean} allowing to track whether the {@code close} method was called.
             */
            private boolean closed = false;
            
            @Override
            public boolean next() throws DAOException,
                    QueryInterruptedException {
                //we allow 3 calls to next before returning false
                callToNext++;
                if (callToNext < 4) {
                    return true;
                }
                this.close();
                return false;
            }
            @Override
            public GeneTO getTO() throws DAOException {
                if (this.closed) {
                    throw new DAOException("Called getTO when ResultSet was closed");
                }
                //we allow 3 calls to next before returning null, 
                //and we return a different GeneTO at each call to next
                switch (callToNext) {
                    case 1: return new GeneTO("ID1", null, null);
                    case 2: return new GeneTO("ID2", null, null);
                    case 3: return new GeneTO("ID3", null, null);
                }
                return null;
            }
            
            //Here we simply delegate to the default method, to be able to use it without mockito. 
            @Override
            public Stream<GeneTO> stream() {
                return DAOResultSet.super.stream();
            }
            
            @Override
            public List<GeneTO> getAllTOs() throws DAOException {
                return null;
            }
            @Override
            public void close() throws DAOException {
                this.closed = true;
            }
        }

        //check that the ResultSet is closed when the Stream is closed
        DAOResultSet<GeneTO> rs = spy(new TestResultSet());
        rs.stream().close();
        verify(rs).close();
        
        //Simply stream results into a List.
        //We don't use the TOComparator here, because we only populate the ID of the TOs, 
        //so we don't need to compare all fields. 
        rs = spy(new TestResultSet());
        assertEquals("Incorrect TO List retrieved from Stream", 
                Arrays.asList(new GeneTO("ID1", null, null), 
                        new GeneTO("ID2", null, null), 
                        new GeneTO("ID3", null, null)), 
                rs.stream().collect(Collectors.toList()));
        //check that the ResultSet was closed, as all results are supposed to have been traversed
        verify(rs).close();
        
        //test with intermediate operations, for the fun. Here, we order geneTOs 
        //in descending order of their ID, and we generate SpeciesTOs with the same IDs as the GeneTOs
        rs = spy(new TestResultSet());
        assertEquals("Incorrect TOs retrieved from Stream with intermediate operations", 
                Arrays.asList(new SpeciesTO("ID3", null, null, null, null, null, null, null), 
                        new SpeciesTO("ID2", null, null, null, null, null, null, null), 
                        new SpeciesTO("ID1", null, null, null, null, null, null, null)), 
                rs.stream()
                .sorted((g1, g2) -> g2.getId().compareTo(g1.getId()))
                .map(g -> new SpeciesTO(g.getId(), null, null, null, null, null, null, null))
                .collect(Collectors.toList()));
        //check that the ResultSet was closed, as all results are supposed to have been traversed
        verify(rs).close();
    }
}
