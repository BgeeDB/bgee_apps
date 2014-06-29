package org.bgee.model.dao.mysql.species;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map.Entry;

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
    
    @Override
    public SpeciesTOResultSet getAllSpecies() {
        log.entry();
        
        //Construct sql query
        String sql = "SELECT " + this.getSelectClause(this.getAttributes()) + " FROM " + 
                MySQLDAO.SPECIES_TABLE_NAME;

        //we don't use a try-with-resource, because we return a pointer to the results, 
        //not the actual results, so we should not close this BgeePreparedStatement.
        BgeePreparedStatement stmt = null;
        try {
            stmt = this.getManager().getConnection().prepareStatement(sql);
            return log.exit(new MySQLSpeciesTOResultSet(stmt, this));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
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
        
        String sql = "INSERT INTO " + MySQLDAO.SPECIES_TABLE_NAME + 
                "(" + this.getSQLExpr(SpeciesDAO.Attribute.ID) + ", " + 
                this.getSQLExpr(SpeciesDAO.Attribute.GENUS) + ", " + 
                this.getSQLExpr(SpeciesDAO.Attribute.SPECIES_NAME) + ", " + 
                this.getSQLExpr(SpeciesDAO.Attribute.COMMON_NAME) + ", " + 
                this.getSQLExpr(SpeciesDAO.Attribute.PARENT_TAXON_ID) + ", " + 
                this.getSQLExpr(SpeciesDAO.Attribute.GENOME_FILE_PATH) + ", " + 
                this.getSQLExpr(SpeciesDAO.Attribute.GENOME_SPECIES_ID) + ", " + 
                this.getSQLExpr(SpeciesDAO.Attribute.FAKE_GENE_ID_PREFIX) + ") values ";
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
    public String getLabel(SpeciesDAO.Attribute attribute) {
        log.entry(attribute);
        
        String label = null;
        if (attribute.equals(SpeciesDAO.Attribute.ID)) {
            label = "speciesId";
        } else if (attribute.equals(SpeciesDAO.Attribute.COMMON_NAME)) {
            label = "speciesCommonName";
        } else if (attribute.equals(SpeciesDAO.Attribute.GENUS)) {
            label = "genus";
        } else if (attribute.equals(SpeciesDAO.Attribute.SPECIES_NAME)) {
            label = "species";
        } else if (attribute.equals(SpeciesDAO.Attribute.PARENT_TAXON_ID)) {
            label = "taxonId";
        } else if (attribute.equals(SpeciesDAO.Attribute.GENOME_FILE_PATH)) {
            label = "genomeFilePath";
        } else if (attribute.equals(SpeciesDAO.Attribute.GENOME_SPECIES_ID)) {
            label = "genomeSpeciesId";
        } else if (attribute.equals(SpeciesDAO.Attribute.FAKE_GENE_ID_PREFIX)) {
            label = "fakeGeneIdPrefix";
        } 
        
        return log.exit(label);
    }
    
    @Override
    public String getSQLExpr(SpeciesDAO.Attribute attribute) {
        log.entry(attribute);
        //no complex SQL expression in this DAO, we just build table_name.label
        return log.exit(MySQLDAO.SPECIES_TABLE_NAME + "." + this.getLabel(attribute));
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
         * The {@code MySQLSpeciesDAO} that produced this {@code MySQLSpeciesTOResultSet}. 
         * Needed to obtain labels associated to {@code SpeciesDAO.Attributes} (see remarks 
         * in {@link org.bgee.model.dao.mysql.MySQLDAO#getLabel(Enum)} javadoc).
         */
        private final MySQLSpeciesDAO speciesDAO;
        /**
         * Default constructor.
         * 
         * @param statement     The first {@code BgeePreparedStatement} to execute a query on, 
         *                      passed to the super constructor {@link 
         *                      org.bgee.model.dao.mysql.connector.MySQLDAOResultSet
         *                      #MySQLDAOResultSet(BgeePreparedStatement) 
         *                      MySQLDAOResultSet(BgeePreparedStatement)}.
         * @param speciesDAO    The {@code MySQLSpeciesDAO} that produced this 
         *                      {@code MySQLSpeciesTOResultSet}. Needed to obtain labels 
         *                      associated to {@code SpeciesDAO.Attributes}.
         */
        public MySQLSpeciesTOResultSet(BgeePreparedStatement statement, 
                MySQLSpeciesDAO speciesDAO) {
            super(statement);
            this.speciesDAO = speciesDAO;
        }

        @Override
        public SpeciesTO getTO() {
            log.entry();
            ResultSet currentResultSet = this.getCurrentResultSet();
            String speciesId = null, genus = null, species = null, 
                   speciesCommonName = null, taxonId = null, genomeFilePath = null, 
                   genomeSpeciesId = null, fakeGeneIdPrefix=null;
            // Get results
            for (Entry<Integer, String> column: this.getColumnLabels().entrySet()) {
                try {
                    if (this.speciesDAO.getLabel(SpeciesDAO.Attribute.ID).equals(
                            column.getValue())) {
                        speciesId = currentResultSet.getString(column.getKey());
                        
                    } else if (this.speciesDAO.getLabel(SpeciesDAO.Attribute.GENUS).equals(
                            column.getValue())) {
                        genus = currentResultSet.getString(column.getKey());
                        
                    } else if (this.speciesDAO.getLabel(SpeciesDAO.Attribute.SPECIES_NAME).equals(
                            column.getValue())) {
                        species = currentResultSet.getString(column.getKey());
                        
                    } else if (this.speciesDAO.getLabel(SpeciesDAO.Attribute.COMMON_NAME).equals(
                            column.getValue())) {
                        speciesCommonName = currentResultSet.getString(column.getKey());
                        
                    } else if (this.speciesDAO.getLabel(SpeciesDAO.Attribute.PARENT_TAXON_ID).equals(
                            column.getValue())) {
                        taxonId = currentResultSet.getString(column.getKey());
                        
                    } else if (this.speciesDAO.getLabel(SpeciesDAO.Attribute.GENOME_FILE_PATH).equals(
                            column.getValue())) {
                        genomeFilePath = currentResultSet.getString(column.getKey());
                    } else if (this.speciesDAO.getLabel(SpeciesDAO.Attribute.GENOME_SPECIES_ID).equals(
                            column.getValue())) {
                        genomeSpeciesId = currentResultSet.getString(column.getKey());
                    } else if (this.speciesDAO.getLabel(SpeciesDAO.Attribute.FAKE_GENE_ID_PREFIX).equals(
                            column.getValue())) {
                        fakeGeneIdPrefix = currentResultSet.getString(column.getKey());
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
