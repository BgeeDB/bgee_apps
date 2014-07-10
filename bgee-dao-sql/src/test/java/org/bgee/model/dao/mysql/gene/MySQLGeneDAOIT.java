package org.bgee.model.dao.mysql.gene;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.TOComparator;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTOResultSet;
import org.bgee.model.dao.mysql.MySQLITAncestor;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.junit.Test;

/**
 * Integration tests for {@link MySQLGeneDAO}, performed on a real MySQL database. 
 * See the documentation of {@link org.bgee.model.dao.mysql.MySQLITAncestor} for 
 * important information.
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @since Bgee 13
 */

public class MySQLGeneDAOIT extends MySQLITAncestor {
    
    private final static Logger log = 
            LogManager.getLogger(MySQLGeneDAOIT.class.getName());

    public MySQLGeneDAOIT() {
        super();
    }
    
    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Test the select method {@link MySQLGeneDAO#getAllGenes()}.
     */
    @Test
    public void shouldGetAllGenes() throws SQLException {
        log.entry();
        this.getMySQLDAOManager().setDatabaseToUse(System.getProperty(POPULATEDDBKEYKEY));
        // TODO Populate database if empty in a @BeforeClass
        // in MySQLITAncestor instead here
        try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                prepareStatement("select 1 from dataSource")) {
            if (!stmt.getRealPreparedStatement().executeQuery().next()) {
                this.populateAndUseDatabase(System.getProperty(POPULATEDDBKEYKEY));
            }
        }

        // Generate result with the method
        MySQLGeneDAO dao = new MySQLGeneDAO(this.getMySQLDAOManager());
        dao.setAttributes(Arrays.asList(GeneDAO.Attribute.values()));
        GeneTOResultSet methResults = dao.getAllGenes();

        // Generate manually expected result
        List<GeneTO> expectedGenes = Arrays.asList(
                new GeneTO("ID1", "genN1", "genDesc1", 11, 12, 2, true), 
                new GeneTO("ID2", "genN2", "genDesc2", 21, 0, 0, true), 
                new GeneTO("ID3", "genN3", "genDesc3", 31, 0, 3, false)); 

        while (methResults.next()) {
            boolean found = false;
            GeneTO methGene = methResults.getTO();
            for (GeneTO expGene: expectedGenes) {
                log.trace("Comparing {} to {}", methGene, expGene);
                if (TOComparator.areGeneTOsEqual(methGene, expGene)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                log.debug("No equivalent gene found for {}", methGene);
                throw log.throwing(new AssertionError("Incorrect generated TO"));
            }
        }
        methResults.close();

        dao.setAttributes(Arrays.asList(GeneDAO.Attribute.ID));
        methResults = dao.getAllGenes();

        // Generate manually expected result
        expectedGenes = Arrays.asList(
                new GeneTO("ID1", null, null, 0, 0, 0, false), 
                new GeneTO("ID2", null, null, 0, 0, 0, false), 
                new GeneTO("ID3", null, null, 0, 0, 0, false)); 

        while (methResults.next()) {
            boolean found = false;
            GeneTO methGene = methResults.getTO();
            for (GeneTO expGene: expectedGenes) {
                log.trace("Comparing {} to {}", methGene, expGene);
                if (TOComparator.areGeneTOsEqual(methGene, expGene)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                log.debug("No equivalent gene found for {}", methGene);
                throw log.throwing(new AssertionError("Incorrect generated TO"));
            }
        }
        methResults.close();

        log.exit();
    }

    /**
     * Test the select method {@link MySQLGeneDAO#updateGenes()}.
     * @throws SQLException 
     */
    @Test
    public void shouldUpdateGenes() throws SQLException {
        log.entry();
        this.populateAndUseDatabase(System.getProperty(EMPTYDBKEY));

        Collection<GeneTO> geneTOs = Arrays.asList(
                new GeneTO("ID1", "GNMod1", "DescMod1", 31, 12, 7, true),
                new GeneTO("ID2", "GNMod2", "DescMod2", 11, 12, 6, false));
        
        Collection<GeneDAO.Attribute> attributesToUpdate1 = Arrays.asList(
                GeneDAO.Attribute.OMAPARENTNODEID);
        Collection<GeneDAO.Attribute> attributesToUpdate2 = Arrays.asList(
                GeneDAO.Attribute.NAME, GeneDAO.Attribute.DESCRIPTION,
                GeneDAO.Attribute.SPECIESID, GeneDAO.Attribute.GENEBIOTYPEID,
                GeneDAO.Attribute.OMAPARENTNODEID, GeneDAO.Attribute.ENSEMBLGENE);
        
        try {
            //Test with only one Attribute
            MySQLGeneDAO dao = new MySQLGeneDAO(this.getMySQLDAOManager());
            assertEquals("Incorrect number of rows inserted", 2, 
                    dao.updateGenes(geneTOs, attributesToUpdate1));

            try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                    prepareStatement("select 1 from gene where geneId = ? " +
                                     "and OMAParentNodeId = ?")) {
                stmt.setString(1, "ID1");
                stmt.setInt(2, 7);
                assertTrue("GeneTO incorrectly updated", 
                        stmt.getRealPreparedStatement().executeQuery().next());

                stmt.setString(1, "ID2");
                stmt.setInt(2, 6);
                assertTrue("GeneTO incorrectly updated", 
                        stmt.getRealPreparedStatement().executeQuery().next());
            }
            
            //Test with all Attributes
            assertEquals("Incorrect number of rows inserted", 2, 
                    dao.updateGenes(geneTOs, attributesToUpdate2));

            try (BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                    prepareStatement("select 1 from gene where " +  
                            "geneId = ? and geneName = ? and geneDescription = ? and " +
                            "speciesId = ? and geneBioTypeId = ? and OMAParentNodeId= ? and " +
                            "ensemblGene = ?")) {
                stmt.setString(1, "ID1");
                stmt.setString(2, "GNMod1");
                stmt.setString(3, "DescMod1");
                stmt.setInt(4, 31);
                stmt.setInt(5, 12);
                stmt.setInt(6, 7);
                stmt.setBoolean(7, true);
                assertTrue("GeneTO incorrectly updated", 
                        stmt.getRealPreparedStatement().executeQuery().next());

                stmt.setString(1, "ID2");
                stmt.setString(2, "GNMod2");
                stmt.setString(3, "DescMod2");
                stmt.setInt(4, 11);
                stmt.setInt(5, 12);
                stmt.setInt(6, 6);
                stmt.setBoolean(7, false);
                assertTrue("GeneTO incorrectly updated", 
                        stmt.getRealPreparedStatement().executeQuery().next());
            }
        } finally {
            this.emptyAndUseDefaultDB();
        }
        log.exit();
    }
}
