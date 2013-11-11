package org.bgee.model.dao.mysql.species;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.species.SpeciesTO;
import org.bgee.model.dao.mysql.MySQLITAncestor;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.junit.Test;

/**
 * Integration tests for {@link MySQLSpeciesDAO}, performed on a real MySQL database. 
 * See the documentation of {@link org.bgee.model.dao.mysql.MySQLITAncestor} for 
 * important information.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class MySQLSpeciesDAOIT extends MySQLITAncestor {
    private final static Logger log = LogManager.getLogger(MySQLSpeciesDAOIT.class.getName());
    
    public MySQLSpeciesDAOIT() {
        super();
    }
    @Override
    protected Logger getLogger() {
        return log;
    }
    
    /**
     * Test the insertion method {@link MySQLSpeciesDAO#insertSpecies(Collection)}.
     */
    @Test
    public void shouldInsertAndGetSpecies() throws SQLException {
        this.useEmptyDB();
        //create a Collection of SpeciesTOs to be inserted
        Collection<SpeciesTO> speciesTOs = new ArrayList<SpeciesTO>();
        speciesTOs.add(new SpeciesTO("ID1", "commonName1", "genus1", "speciesName1", 
                "parentTaxonID1"));
        speciesTOs.add(new SpeciesTO("ID2", "commonName2", "genus2", "speciesName2", 
                "parentTaxonID2"));
        speciesTOs.add(new SpeciesTO("ID3", "commonName3", "genus3", "speciesName3", 
                "parentTaxonID3"));
        MySQLSpeciesDAO dao = new MySQLSpeciesDAO(this.getMySQLDAOManager());
        assertEquals("Incorrect number of rows inserted", 3, dao.insertSpecies(speciesTOs));
        
        //we manually verify the insertion, as we do not want to rely on other methods 
        //that are tested elsewhere
        BgeePreparedStatement stmt = this.getMySQLDAOManager().getConnection().
                prepareStatement("select * from species order by speciesId");
        
        
        
        this.useDefaultDB();
    }
}
