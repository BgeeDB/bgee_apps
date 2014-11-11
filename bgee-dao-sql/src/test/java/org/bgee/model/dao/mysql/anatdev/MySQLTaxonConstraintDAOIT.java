package org.bgee.model.dao.mysql.anatdev;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.anatdev.TaxonConstraintDAO.TaxonConstraintTO;
import org.bgee.model.dao.mysql.MySQLITAncestor;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Integration tests for {@link MySQLTaxonConstraintDAO}, performed on a real MySQL database. 
 * See the documentation of {@link org.bgee.model.dao.mysql.MySQLITAncestor} for 
 * important information.
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @see org.bgee.model.dao.api.anatdev.TaxonConstraintDAO
 * @since Bgee 13
 */
public class MySQLTaxonConstraintDAOIT extends MySQLITAncestor {

    private final static Logger log = 
            LogManager.getLogger(MySQLTaxonConstraintDAOIT.class.getName());

    public MySQLTaxonConstraintDAOIT() {
        super();
    }
    
    @Override
    protected Logger getLogger() {
        return log;
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Test the insert method 
     * {@link MySQLTaxonConstraintDAO#insertAnatEntityRelationTaxonConstraints()}.
     */
    @Test
    public void shouldInsertAnatEntityRelationTaxonConstraint() throws SQLException {
        
        this.useEmptyDB();

        //create a Collection of TaxonConstraintTO to be inserted
        Collection<TaxonConstraintTO> taxonConstraintTOs = Arrays.asList(
                new TaxonConstraintTO("99","11"),
                new TaxonConstraintTO("98","21"),
                new TaxonConstraintTO("97",null));

        try {
            MySQLTaxonConstraintDAO dao = new MySQLTaxonConstraintDAO(this.getMySQLDAOManager());
            assertEquals("Incorrect number of rows inserted", 3, 
                    dao.insertAnatEntityRelationTaxonConstraints(taxonConstraintTOs));
            
            //we manually verify the insertion, as we do not want to rely on other methods 
            //that are tested elsewhere.
            try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                    prepareStatement("select 1 from anatEntityRelationTaxonConstraint where " +
                      "anatEntityRelationId = ? AND speciesId = ?")) {
                
                stmt.setInt(1, 99);
                stmt.setInt(2, 11);
                assertTrue("TaxonConstraintTO (AnatEntityRelation) incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
                
                stmt.setInt(1, 98);
                stmt.setInt(2, 21);
                assertTrue("TaxonConstraintTO (AnatEntityRelation) incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
                
            }
            try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                    prepareStatement("select 1 from anatEntityRelationTaxonConstraint where " +
                      "anatEntityRelationId = ? AND speciesId is null")) {
                stmt.setInt(1, 97);
              assertTrue("TaxonConstraintTO (AnatEntityRelation) incorrectly inserted", 
                      stmt.getRealPreparedStatement().executeQuery().next());
            }

            this.thrown.expect(IllegalArgumentException.class);
            dao.insertAnatEntityRelationTaxonConstraints(new HashSet<TaxonConstraintTO>());
        } finally {
            this.emptyAndUseDefaultDB();
        }
    }

    /**
     * Test the insert method 
     * {@link MySQLTaxonConstraintDAO#insertAnatEntityTaxonConstraints()}.
     */
    @Test
    public void shouldInsertAnatEntityTaxonConstraint() throws SQLException {

        this.useEmptyDB();

        //create a Collection of TaxonConstraintTO to be inserted
        Collection<TaxonConstraintTO> taxonConstraintTOs = Arrays.asList(
                new TaxonConstraintTO("Anat_id10","11"),
                new TaxonConstraintTO("Anat_id5","21"),
                new TaxonConstraintTO("Anat_id1",null));

        try {
            MySQLTaxonConstraintDAO dao = new MySQLTaxonConstraintDAO(this.getMySQLDAOManager());
            
            assertEquals("Incorrect number of rows inserted", 3, 
                    dao.insertAnatEntityTaxonConstraints(taxonConstraintTOs));
            
            //we manually verify the insertion, as we do not want to rely on other methods 
            //that are tested elsewhere.
            try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                    prepareStatement("select 1 from anatEntityTaxonConstraint where " +
                      "anatEntityId = ? AND speciesId = ?")) {
                
                stmt.setString(1, "Anat_id10");
                stmt.setInt(2, 11);
                assertTrue("TaxonConstraintTO (AnatEntity) incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
                
                stmt.setString(1, "Anat_id5");
                stmt.setInt(2, 21);
                assertTrue("TaxonConstraintTO (AnatEntity) incorrectly inserted", 
                        stmt.getRealPreparedStatement().executeQuery().next());
                
            }
            try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                    prepareStatement("select 1 from anatEntityTaxonConstraint where " +
                      "anatEntityId = ? AND speciesId is null")) {
              stmt.setString(1, "Anat_id1");
              assertTrue("TaxonConstraintTO (AnatEntity) incorrectly inserted", 
                      stmt.getRealPreparedStatement().executeQuery().next());
            }
            
            this.thrown.expect(IllegalArgumentException.class);
            dao.insertAnatEntityTaxonConstraints(new HashSet<TaxonConstraintTO>());
        } finally {
            this.emptyAndUseDefaultDB();
        }
    }        
     /**
    * Test the insert method 
    * {@link MySQLTaxonConstraintDAO#insertAnatEntityRelationTaxonConstraints()}.
    */
   @Test
   public void shouldInsertStageTaxonConstraint() throws SQLException {

       this.useEmptyDB();

       //create a Collection of TaxonConstraintTO to be inserted
       Collection<TaxonConstraintTO> taxonConstraintTOs = Arrays.asList(
               new TaxonConstraintTO("Stage_id10","11"),
               new TaxonConstraintTO("Stage_id5","21"),
               new TaxonConstraintTO("Stage_id1",null));

       try {
           MySQLTaxonConstraintDAO dao = new MySQLTaxonConstraintDAO(this.getMySQLDAOManager());
           
           assertEquals("Incorrect number of rows inserted", 3, 
                   dao.insertStageTaxonConstraints(taxonConstraintTOs));
           
           //we manually verify the insertion, as we do not want to rely on other methods 
           //that are tested elsewhere.
           try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                   prepareStatement("select 1 from stageTaxonConstraint where " +
                     "stageId = ? AND speciesId = ?")) {
               
               stmt.setString(1, "Stage_id10");
               stmt.setInt(2, 11);
               assertTrue("TaxonConstraintTO (Stage) incorrectly inserted", 
                       stmt.getRealPreparedStatement().executeQuery().next());
               
               stmt.setString(1, "Stage_id5");
               stmt.setInt(2, 21);
               assertTrue("TaxonConstraintTO (Stage) incorrectly inserted", 
                       stmt.getRealPreparedStatement().executeQuery().next());
               
           }
           try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                   prepareStatement("select 1 from stageTaxonConstraint where " +
                     "stageId = ? AND speciesId is null")) {
             stmt.setString(1, "Stage_id1");
             assertTrue("TaxonConstraintTO (Stage) incorrectly inserted", 
                     stmt.getRealPreparedStatement().executeQuery().next());
           }
           
           this.thrown.expect(IllegalArgumentException.class);
           dao.insertStageTaxonConstraints(new HashSet<TaxonConstraintTO>());
       } finally {
           this.emptyAndUseDefaultDB();
       }
   }       
}
