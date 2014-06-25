package org.bgee.model.dao.mysql.species;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.species.SpeciesDAO;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;

/**
 * A {@code SpeciesDAO} for MySQL. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @see org.bgee.model.dao.api.species.SpeciesTO
 * @since Bgee 01
 */
public class MySQLSpeciesDAO extends MySQLDAO<SpeciesDAO.Attribute> 
    implements SpeciesDAO {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(MySQLSpeciesDAO.class.getName());
    
    /**
     * Constructor providing the {@code MySQLDAOManager} that this {@code MySQLDAO} 
     * will use to obtain {@code BgeeConnection}s.
     * @param manager   the {@code MySQLDAOManager} to use.
     * @throws IllegalArgumentException If {@code manager} is {@code null}.
     */
    public MySQLSpeciesDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }
    

    //***************************************************************************
    // METHODS NOT PART OF THE bgee-dao-api, USED BY THE PIPELINE AND NOT MEANT 
    //TO BE EXPOSED TO THE PUBLIC API.
    //***************************************************************************
    /**
     * Inserts the provided species into the Bgee database, represented as 
     * a {@code Collection} of {@code SpeciesTO}s.
     * 
     * @param specieTOs a {@code Collection} of {@code SpeciesTO}s to be inserted 
     *                  into the database.
     * @return          An {@code int} that is the number of species inserted 
     *                  as a result of this method call.
     * @throws DAOException     If a {@code SQLException} occurred while trying 
     *                          to insert {@code species}. The {@code SQLException} 
     *                          will be wrapped into a {@code DAOException} ({@code DAOs} 
     *                          do not expose these kind of implementation details).
     */
    public int insertSpecies(Collection<SpeciesTO> specieTOs) throws DAOException {
        log.entry(specieTOs);
        
        String sql = "Insert into species (speciesId, genus, species, " +
                "speciesCommonName, taxonId, genomeFilePath, genomeSpeciesId, " +
                "fakeGeneIdPrefix) values ";
        for (int i = 0; i < specieTOs.size(); i++) {
            if (i > 0) {
                sql += ", ";
            }
            sql += "(?, ?, ?, ?, ?, ?, ?, ?) ";
        }
        try (BgeePreparedStatement stmt = 
                this.getManager().getConnection().prepareStatement(sql)) {
            int paramIndex = 1;
            for (SpeciesTO speciesTO: specieTOs) {
                stmt.setInt(paramIndex, Integer.parseInt(speciesTO.getId()));
                paramIndex++;
                stmt.setString(paramIndex, speciesTO.getGenus());
                paramIndex++;
                stmt.setString(paramIndex, speciesTO.getSpeciesName());
                paramIndex++;
                stmt.setString(paramIndex, speciesTO.getName());
                paramIndex++;
                stmt.setInt(paramIndex, Integer.parseInt(speciesTO.getParentTaxonId()));
                paramIndex++;
                stmt.setString(paramIndex, speciesTO.getGenomeFilePath());
                paramIndex++;
                //TODO: handles default values in a better way
                if (speciesTO.getGenomeSpeciesId() != null) {
                    stmt.setInt(paramIndex, Integer.parseInt(speciesTO.getGenomeSpeciesId()));
                } else {
                    stmt.setInt(paramIndex, 0);
                }
                paramIndex++;
                //TODO: handles default values in a better way
                if (speciesTO.getFakeGeneIdPrefix() != null) {
                    stmt.setString(paramIndex, speciesTO.getFakeGeneIdPrefix());
                } else {
                    stmt.setString(paramIndex, "");
                }
                paramIndex++;
            }
            
            return log.exit(stmt.executeUpdate());
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    @Override
    public SpeciesTOResultSet getAllSpecies() {
        log.entry();
        
        //Construct sql query
        String sql = "SELECT " + this.getSelectExpr(this.getAttributes()) + " FROM " + 
                MySQLDAO.SPECIES_TABLE_NAME;

        //we don't use a try-with-resource, because we return a pointer to the results, 
        //not the actual results, so we should not close this BgeePreparedStatement.
        BgeePreparedStatement stmt = null;
        try {
            stmt = this.getManager().getConnection().prepareStatement(sql.toString());
            return log.exit(new MySQLSpeciesTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    @Override
    public String getLabel(SpeciesDAO.Attribute attribute)
            throws IllegalArgumentException {
        log.entry(attribute);
        if (attribute.equals(SpeciesDAO.Attribute.ID)) {
            return log.exit("speciesId");
        } else if (attribute.equals(SpeciesDAO.Attribute.COMMONNAME)) {
            return log.exit("speciesCommonName");
        } else if (attribute.equals(SpeciesDAO.Attribute.GENUS)) {
            return log.exit("genus");
        } else if (attribute.equals(SpeciesDAO.Attribute.SPECIESNAME)) {
            return log.exit("species");
        } else if (attribute.equals(SpeciesDAO.Attribute.PARENTTAXONID)) {
            return log.exit("taxonId");
        }
        throw log.throwing(new IllegalArgumentException("The attribute provided (" + 
                attribute.toString() + ") is unknown for " + 
                MySQLSpeciesDAO.class.getName()));
    }

    @Override
    protected String getSelectExpr(Collection<SpeciesDAO.Attribute> attributes) {
        log.entry(attributes);
        if (attributes == null || attributes.size() == 0) {
            return log.exit(MySQLDAO.SPECIES_TABLE_NAME + ".*");
        }
        StringBuilder selectExpr = new StringBuilder();
        boolean isFirstIteration = true;
        for (SpeciesDAO.Attribute attribute: attributes) {
            if (isFirstIteration) {
                isFirstIteration = false;
            } else {
                selectExpr.append(", ");
            }
            selectExpr.append(MySQLDAO.SPECIES_TABLE_NAME);
            selectExpr.append(".");
            selectExpr.append(this.getLabel(attribute));
        }
        return log.exit(selectExpr.toString());        
    }

    @Override
    protected String getTableReferences(Collection<SpeciesDAO.Attribute> attributes) {
        throw new UnsupportedOperationException("The method is not implemented yet");
    }
    
    /**
     * A {@code MySQLDAOResultSet} specific to {@code SpeciesTO}.
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public class MySQLSpeciesTOResultSet extends MySQLDAOResultSet<SpeciesTO> 
            implements SpeciesTOResultSet {

        /**
         * Delegates to {@link MySQLDAOResultSet#MySQLDAOResultSet(BgeePreparedStatement)
         * super constructor.
         * 
         * @param statement The first {@code BgeePreparedStatement} to execute a query on.
         */
        public MySQLSpeciesTOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }

        @Override
        public SpeciesTO getTO() {
            log.entry();
            ResultSet currentResultSet = this.getCurrentResultSet();
            Map<Integer, String> currentColumnLabels = this.getColumnLabels();
            String speciesId=null, genus=null, species=null, 
                    speciesCommonName=null, taxonId=null, genomeFilePath=null, 
                    genomeSpeciesId=null, fakeGeneIdPrefix=null;
            // Get results
            for (String currentColumnLabel : currentColumnLabels.values()) {
                try {
                    if (currentColumnLabel.equals("speciesId")) {
                        speciesId = currentResultSet.getString("speciesId");
                    } else if (currentColumnLabel.equals("genus")) {
                        genus = currentResultSet.getString("genus");
                    } else if (currentColumnLabel.equals("species")) {
                        species = currentResultSet.getString("species");
                    } else if (currentColumnLabel.equals("speciesCommonName")) {
                        speciesCommonName = currentResultSet.getString("speciesCommonName");
                    } else if (currentColumnLabel.equals("taxonId")) {
                        taxonId = currentResultSet.getString("taxonId");
                    } else if (currentColumnLabel.equals("genomeFilePath")) {
                        genomeFilePath = currentResultSet.getString("genomeFilePath");
                    } else if (currentColumnLabel.equals("genomeSpeciesId")) {
                        genomeSpeciesId = currentResultSet.getString("genomeSpeciesId");
                    } else if (currentColumnLabel.equals("fakeGeneIdPrefix")) {
                        fakeGeneIdPrefix = currentResultSet.getString("fakeGeneIdPrefix");
                    }
                } catch (SQLException e) {
                    throw log.throwing(new DAOException(e));
                }
            }
            //Set SpeciesTO
            return log.exit(new SpeciesTO(speciesId, genus, species, speciesCommonName,
                    taxonId, genomeFilePath, genomeSpeciesId, fakeGeneIdPrefix));
        }
    }

}
