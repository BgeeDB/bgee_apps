package org.bgee.model.dao.mysql.gene;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.gene.GeneOntologyDAO.GOTermTO;
import org.bgee.model.dao.api.gene.GeneOntologyDAO.GOTermTO.Domain;
import org.bgee.model.dao.api.ontologycommon.RelationTO;
import org.bgee.model.dao.mysql.MySQLITAncestor;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.junit.Test;

/**
 * Integration tests for {@link MySQLGeneOntologyDAO}, performed on a real MySQL database. 
 * See the documentation of {@link org.bgee.model.dao.mysql.MySQLITAncestor} for 
 * important information.
 * 
 * @author Frederic Bastian
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @since Bgee 13
 */
public class MySQLGeneOntologyDAOIT extends MySQLITAncestor {
    private final static Logger log = LogManager.getLogger(MySQLGeneOntologyDAO.class.getName());
        
    public MySQLGeneOntologyDAOIT() {
        super();
    }
    @Override
    protected Logger getLogger() {
        return log;
    }
    
    /**
     * Test the insertion method {@link MySQLGeneOntologyDAO#insertTerms(Collection)}.
     */
    @Test
    public void shouldInsertTerms() throws SQLException {
        this.useEmptyDB();
        //create a Collection of GOTermTOs to be inserted
        Collection<GOTermTO> goTermTOs = new ArrayList<GOTermTO>();
        goTermTOs.add(new GOTermTO("GO:10", "term1", Domain.BP));
        goTermTOs.add(new GOTermTO("GO:50", "term2", Domain.MF, Arrays.asList("alt1_GO:50", "alt2_GO:50", "alt3_GO:50")));
        goTermTOs.add(new GOTermTO("GO:60", "term3", Domain.CC, Arrays.asList("alt1_GO:60")));
        try {
            MySQLGeneOntologyDAO dao = new MySQLGeneOntologyDAO(this.getMySQLDAOManager());
            assertEquals("Incorrect number of rows inserted", 3, 
                    dao.insertTerms(goTermTOs));
            
            //we manually verify the insertion, as we do not want to rely on other methods 
            //that are tested elsewhere.
            //This test method could be better written (DRY, ...)
            try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                    prepareStatement("select 1 from geneOntologyTerm where GOId = ? and " +
                            "GOTerm = ? and GODomain = ?")) {
                
                stmt.setString(1, "GO:10");
                stmt.setString(2, "term1");
                stmt.setString(3, dao.domainToString(Domain.BP));
                assertTrue("GOTermTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
                
                stmt.setString(1, "GO:50");
                stmt.setString(2, "term2");
                stmt.setString(3, dao.domainToString(Domain.MF));
                assertTrue("GOTermTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
                
                stmt.setString(1, "GO:60");
                stmt.setString(2, "term3");
                stmt.setString(3, dao.domainToString(Domain.CC));
                assertTrue("GOTermTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
            }
            
            //check insertion of alternative IDs
            try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                    prepareStatement("select 1 from geneOntologyTermAltId where GOId = ? " +
                    		"and goAltId = ?")) {
                
                stmt.setString(1, "GO:50");
                stmt.setString(2, "alt1_GO:50");
                assertTrue("GOTerm AltIds incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
                
                stmt.setString(1, "GO:50");
                stmt.setString(2, "alt2_GO:50");
                assertTrue("GOTerm AltIds incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
                
                stmt.setString(1, "GO:50");
                stmt.setString(2, "alt3_GO:50");
                assertTrue("GOTerm AltIds incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
                
                stmt.setString(1, "GO:60");
                stmt.setString(2, "alt1_GO:60");
                assertTrue("GOTerm AltIds incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
            }
            //check that only the desire alternative IDs were inserted
            try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                    prepareStatement("select count(*) from geneOntologyTermAltId")) {
                
                ResultSet rs = stmt.getRealPreparedStatement().executeQuery();
                rs.next();
                assertEquals("GOTerm AltIds incorrectly inserted", 4, rs.getInt(1));
            }
            
        } finally {
            this.emptyAndUseDefaultDB();
        }
    }
    
    /**
     * Test the insertion method {@link MySQLGeneOntologyDAO#insertRelations(Collection)}.
     */
    @Test
    public void shouldInsertRelations() throws SQLException {
        this.useEmptyDB();
        //create a Collection of RelationTOs to be inserted
        Collection<RelationTO> relTOs = new ArrayList<RelationTO>();
        relTOs.add(new RelationTO("GO:1", "GO:2"));
        relTOs.add(new RelationTO("GO:1", "GO:3"));
        relTOs.add(new RelationTO("GO:2", "GO:3"));
        try {
            MySQLGeneOntologyDAO dao = new MySQLGeneOntologyDAO(this.getMySQLDAOManager());
            assertEquals("Incorrect number of rows inserted", 3, 
                    dao.insertRelations(relTOs));
            
            //we manually verify the insertion, as we do not want to rely on other methods 
            //that are tested elsewhere.
            //This test method could be better written (DRY, ...)
            try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                    prepareStatement("select 1 from geneOntologyRelation where " +
                    		"goAllSourceId = ? and goAllTargetId= ?")) {
                
                stmt.setString(1, "GO:1");
                stmt.setString(2, "GO:2");
                assertTrue("RelationTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
                
                stmt.setString(1, "GO:1");
                stmt.setString(2, "GO:3");
                assertTrue("RelationTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
                
                stmt.setString(1, "GO:2");
                stmt.setString(2, "GO:3");
                assertTrue("RelationTO incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
            }
            
        } finally {
            this.emptyAndUseDefaultDB();
        }
    }
}
