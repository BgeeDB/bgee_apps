package org.bgee.model.dao.mysql.species;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.species.SpeciesDAO;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;

/**
 * A {@code SpeciesDAO} for MySQL. 
 * 
 * @author Frederic Bastian
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @see org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTO
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
    public SpeciesTOResultSet getAllSpecies() throws DAOException {
        log.entry();
        return log.exit(this.getSpeciesByIds(null));
    }
    
    @Override
    public SpeciesTOResultSet getSpeciesByIds(Set<String> speciesIds) throws DAOException {
        log.entry(speciesIds);
        
        String sql = this.generateSelectClause(this.getAttributes(), "species");
        sql += "FROM species ";
        
        if (speciesIds != null && speciesIds.size() > 0) {
            sql += " WHERE speciesId IN (" + 
                       BgeePreparedStatement.generateParameterizedQueryString(
                               speciesIds.size()) + ")";
        }

        //we don't use a try-with-resource, because we return a pointer to the results, 
        //not the actual results, so we should not close this BgeePreparedStatement.
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sql);
            if (speciesIds != null && speciesIds.size() > 0) {
                stmt.setStringsToIntegers(1, speciesIds, true);
            }  
            return log.exit(new MySQLSpeciesTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    @Override
    public SpeciesTOResultSet getSpeciesFromDataGroups() throws DAOException {
        String sql = this.generateSelectClause(this.getAttributes(), "species");
        sql += "FROM species WHERE EXISTS (SELECT 1 FROM speciesToDataGroup WHERE speciesToDataGroup.speciesId = species.speciesId)";

        //we don't use a try-with-resource, because we return a pointer to the results,
        //not the actual results, so we should not close this BgeePreparedStatement.
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sql);
            return log.exit(new MySQLSpeciesTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    /**
     * Generates the SELECT clause of a MySQL query used to retrieve {@code SpeciesTO}s.
     * 
     * @param attributes                A {@code Set} of {@code Attribute}s defining 
     *                                  the columns/information the query should retrieve.
     * @return                          A {@code String} containing the SELECT clause 
     *                                  for the requested query, ending with a whitespace.
     */
    private String generateSelectClause(Set<SpeciesDAO.Attribute> attributes, 
            String speciesTableName) {
        log.entry(attributes, speciesTableName);
        
        String sql = new String(); 
        if (attributes == null || attributes.size() == 0) {
            sql += "SELECT " + speciesTableName + ".* ";
        } else {
            for (SpeciesDAO.Attribute attribute: attributes) {
                if (sql.length() == 0) {
                    sql += "SELECT DISTINCT ";
                } else {
                    sql += ", ";
                }
                if (attribute.equals(SpeciesDAO.Attribute.ID)) {
                    sql += speciesTableName + ".speciesId";
                } else if (attribute.equals(SpeciesDAO.Attribute.COMMON_NAME)) {
                    sql += speciesTableName + ".speciesCommonName";
                } else if (attribute.equals(SpeciesDAO.Attribute.GENUS)) {
                    sql += speciesTableName + ".genus";
                } else if (attribute.equals(SpeciesDAO.Attribute.SPECIES_NAME)) {
                    sql += speciesTableName + ".species";
                } else if (attribute.equals(SpeciesDAO.Attribute.DISPLAY_ORDER)) {
                    sql += speciesTableName + ".speciesDisplayOrder";
                } else if (attribute.equals(SpeciesDAO.Attribute.PARENT_TAXON_ID)) {
                    sql += speciesTableName + ".taxonId";
                } else if (attribute.equals(SpeciesDAO.Attribute.GENOME_FILE_PATH)) {
                    sql += speciesTableName + ".genomeFilePath";
                } else if (attribute.equals(SpeciesDAO.Attribute.GENOME_VERSION)) {
                    sql += speciesTableName + ".genomeVersion";
                } else if (attribute.equals(SpeciesDAO.Attribute.DATA_SOURCE_ID)) {
                    sql += speciesTableName + ".dataSourceId";
                } else if (attribute.equals(SpeciesDAO.Attribute.GENOME_SPECIES_ID)) {
                    sql += speciesTableName + ".genomeSpeciesId";
                } else if (attribute.equals(SpeciesDAO.Attribute.FAKE_GENE_ID_PREFIX)) {
                    sql += speciesTableName + ".fakeGeneIdPrefix";
                } else {
                    throw log.throwing(new IllegalArgumentException(
                            "The attribute provided (" + attribute.toString() + 
                            ") is unknown for " + SpeciesDAO.class.getName()));
                }
            }
        }
        sql += " ";
        
        return log.exit(sql);
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
        
        StringBuilder sql = new StringBuilder(); 
        sql.append("INSERT INTO species" +  
                   "(speciesId, genus, species, speciesCommonName, speciesDisplayOrder, taxonId, " + 
                   "genomeFilePath, genomeVersion, dataSourceId, " + 
                   "genomeSpeciesId, fakeGeneIdPrefix) values ");
        for (int i = 0; i < specieTOs.size(); i++) {
            if (i > 0) {
                sql.append(", ");
            }
            sql.append("(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ");
        }
        try (BgeePreparedStatement stmt = 
                this.getManager().getConnection().prepareStatement(sql.toString())) {
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
                stmt.setInt(paramIndex, speciesTO.getDisplayOrder());
                paramIndex++;
                stmt.setInt(paramIndex, Integer.parseInt(speciesTO.getParentTaxonId()));
                paramIndex++;
                stmt.setString(paramIndex, speciesTO.getGenomeFilePath());
                paramIndex++;
                stmt.setString(paramIndex, speciesTO.getGenomeVersion());
                paramIndex++;
                stmt.setInt(paramIndex, Integer.parseInt(speciesTO.getDataSourceId()));
                paramIndex++;
                //TODO: handles default values in a better way
                //We should create setter methods in BgeePreparedStatement, accepting a third argument, being the default value
                if (speciesTO.getGenomeSpeciesId() != null && 
                        !speciesTO.getGenomeSpeciesId().equals(speciesTO.getId())) {
                    stmt.setInt(paramIndex, Integer.parseInt(speciesTO.getGenomeSpeciesId()));
                } else {
                    stmt.setInt(paramIndex, 0);
                }
                paramIndex++;
                //TODO: handles default values in a better way
                //We should create setter methods in BgeePreparedStatement, accepting a third argument, being the default value
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
         * Default constructor.
         * 
         * @param statement     The first {@code BgeePreparedStatement} to execute a query on, 
         *                      passed to the super constructor {@link 
         *                      org.bgee.model.dao.mysql.connector.MySQLDAOResultSet
         *                      #MySQLDAOResultSet(BgeePreparedStatement) 
         *                      MySQLDAOResultSet(BgeePreparedStatement)}.
         */
        private MySQLSpeciesTOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }

        @Override
        protected SpeciesTO getNewTO() {
            log.entry();
            String speciesId = null, genus = null, species = null, speciesCommonName = null, 
                   taxonId = null, genomeFilePath = null, genomeVersion = null,
                   dataSourceId = null, genomeSpeciesId = null, fakeGeneIdPrefix=null;
            Integer displayOrder = null;
            // Get results
            for (Entry<Integer, String> column: this.getColumnLabels().entrySet()) {
                try {
                    if (column.getValue().equals("speciesId")) {
                        speciesId = this.getCurrentResultSet().getString(column.getKey());
                        
                    } else if (column.getValue().equals("genus")) {
                        genus = this.getCurrentResultSet().getString(column.getKey());
                        
                    } else if (column.getValue().equals("species")) {
                        species = this.getCurrentResultSet().getString(column.getKey());
                        
                    } else if (column.getValue().equals("speciesCommonName")) {
                        speciesCommonName = this.getCurrentResultSet().getString(column.getKey());
                        
                    } else if (column.getValue().equals("speciesDisplayOrder")) {
                        displayOrder = this.getCurrentResultSet().getInt(column.getKey());
                        
                    } else if (column.getValue().equals("taxonId")) {
                        taxonId = this.getCurrentResultSet().getString(column.getKey());
                        
                    } else if (column.getValue().equals("genomeFilePath")) {
                        genomeFilePath = this.getCurrentResultSet().getString(column.getKey());

                    } else if (column.getValue().equals("genomeVersion")) {
                        genomeVersion = this.getCurrentResultSet().getString(column.getKey());

                    } else if (column.getValue().equals("dataSourceId")) {
                        dataSourceId = this.getCurrentResultSet().getString(column.getKey());

                    } else if (column.getValue().equals("genomeSpeciesId")) {
                        genomeSpeciesId = this.getCurrentResultSet().getString(column.getKey());

                    } else if (column.getValue().equals("fakeGeneIdPrefix")) {
                        fakeGeneIdPrefix = this.getCurrentResultSet().getString(column.getKey());
                    } else {
                        throw log.throwing(new UnrecognizedColumnException(column.getValue()));
                    }
                } catch (SQLException e) {
                    throw log.throwing(new DAOException(e));
                }
            }
            //Set SpeciesTO
            return log.exit(new SpeciesTO(speciesId, speciesCommonName, genus, species,
                    displayOrder, taxonId, genomeFilePath, genomeVersion, dataSourceId, 
                    genomeSpeciesId, fakeGeneIdPrefix));
        }
    }

}
