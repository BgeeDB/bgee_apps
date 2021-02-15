package org.bgee.model.dao.mysql.species;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
 * @version Bgee 14 Mar 2019
 * @see org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTO
 * @since Bgee 01
 */
public class MySQLSpeciesDAO extends MySQLDAO<SpeciesDAO.Attribute> implements SpeciesDAO {
    private final static Logger log = LogManager.getLogger(MySQLSpeciesDAO.class.getName());

    /**
     * A {@code Map} of column name to their corresponding {@code Attribute}.
     */
    private static final Map<String, SpeciesDAO.Attribute> COL_TO_ATTR_MAP;
    static {
        COL_TO_ATTR_MAP = new HashMap<>();
        COL_TO_ATTR_MAP.put("speciesId", SpeciesDAO.Attribute.ID);
        COL_TO_ATTR_MAP.put("speciesCommonName", SpeciesDAO.Attribute.COMMON_NAME);
        COL_TO_ATTR_MAP.put("genus", SpeciesDAO.Attribute.GENUS);
        COL_TO_ATTR_MAP.put("species", SpeciesDAO.Attribute.SPECIES_NAME);
        COL_TO_ATTR_MAP.put("taxonId", SpeciesDAO.Attribute.PARENT_TAXON_ID);
        COL_TO_ATTR_MAP.put("genomeFilePath", SpeciesDAO.Attribute.GENOME_FILE_PATH);
        COL_TO_ATTR_MAP.put("genomeVersion", SpeciesDAO.Attribute.GENOME_VERSION);
        COL_TO_ATTR_MAP.put("dataSourceId", SpeciesDAO.Attribute.DATA_SOURCE_ID);
        COL_TO_ATTR_MAP.put("genomeSpeciesId", SpeciesDAO.Attribute.GENOME_SPECIES_ID);
        COL_TO_ATTR_MAP.put("speciesDisplayOrder", SpeciesDAO.Attribute.DISPLAY_ORDER);
    }
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
    public SpeciesTOResultSet getAllSpecies(Collection<SpeciesDAO.Attribute> attributes) throws DAOException {
        log.entry(attributes);
        return log.traceExit(this.getSpeciesByIds(null, attributes));
    }
    
    @Override
    public SpeciesTOResultSet getSpeciesByIds(Collection<Integer> speciesIds,
            Collection<SpeciesDAO.Attribute> attributes) throws DAOException {
        log.entry(speciesIds, attributes);
        return log.traceExit(this.getSpeciesByIdsAndTaxonIds(speciesIds, null, attributes));
    }

    @Override
    public SpeciesTOResultSet getSpeciesByTaxonIds(Collection<Integer> taxonIds,
            Collection<SpeciesDAO.Attribute> attributes) throws DAOException {
        log.entry(taxonIds, attributes);
        return log.traceExit(this.getSpeciesByIdsAndTaxonIds(null, taxonIds, attributes));
    }

    private SpeciesTOResultSet getSpeciesByIdsAndTaxonIds(Collection<Integer> speciesIds,
            Collection<Integer> taxonIds, Collection<SpeciesDAO.Attribute> attributes)
                    throws DAOException {
        log.entry(speciesIds, taxonIds, attributes);

        Set<SpeciesDAO.Attribute> clonedAttrs = Collections.unmodifiableSet(
                attributes == null? new HashSet<>(): new HashSet<>(attributes));
        Set<Integer> clonedSpeIds = Collections.unmodifiableSet(
                speciesIds == null? new HashSet<>(): new HashSet<>(speciesIds));
        Set<Integer> clonedTaxIds = Collections.unmodifiableSet(
                taxonIds == null? new HashSet<>(): new HashSet<>(taxonIds));

        String sql = generateSelectClause("species", COL_TO_ATTR_MAP, true, clonedAttrs);
        //fix for issue#173
        if (!clonedAttrs.isEmpty() && !clonedAttrs.contains(SpeciesDAO.Attribute.DISPLAY_ORDER)) {
            sql += ", speciesDisplayOrder ";
        }
        sql += "FROM ";
        if (!clonedTaxIds.isEmpty()) {
            sql += "taxon AS t1 INNER JOIN taxon AS t2 ON t2.taxonLeftBound >= t1.taxonLeftBound "
                 + "AND t2.taxonRightBound <= t1.taxonRightBound "
                 + "INNER JOIN ";
        }
        sql += "species ";
        if (!clonedTaxIds.isEmpty()) {
            sql += " ON t2.taxonId = species.taxonId ";
        }
        
        if (!clonedSpeIds.isEmpty()) {
            sql += " WHERE speciesId IN (" + 
                       BgeePreparedStatement.generateParameterizedQueryString(
                               clonedSpeIds.size()) + ")";
        }
        if (!clonedTaxIds.isEmpty()) {
            if (!clonedSpeIds.isEmpty()) {
                sql += " AND ";
            } else {
                sql += " WHERE ";
            }
            sql += "t1.taxonId IN (" +
                    BgeePreparedStatement.generateParameterizedQueryString(
                            clonedTaxIds.size()) + ")";
        }
        
        sql += " ORDER BY speciesDisplayOrder";

        //we don't use a try-with-resource, because we return a pointer to the results, 
        //not the actual results, so we should not close this BgeePreparedStatement.
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sql);
            int index = 1;
            if (!clonedSpeIds.isEmpty()) {
                stmt.setIntegers(index, clonedSpeIds, true);
                index += clonedSpeIds.size();
            }
            if (!clonedTaxIds.isEmpty()) {
                stmt.setIntegers(index, clonedTaxIds, true);
                index += clonedTaxIds.size();
            }
            return log.traceExit(new MySQLSpeciesTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    @Override
    public SpeciesTOResultSet getSpeciesFromDataGroups(Collection<SpeciesDAO.Attribute> attributes)
            throws DAOException {
        log.entry(attributes);

        Set<SpeciesDAO.Attribute> clonedAttrs = Collections.unmodifiableSet(
                attributes == null? new HashSet<>(): new HashSet<>(attributes));

        String sql = generateSelectClause("species", COL_TO_ATTR_MAP, true, clonedAttrs);
        sql += "FROM species WHERE EXISTS (SELECT 1 FROM speciesToDataGroup WHERE speciesToDataGroup.speciesId = species.speciesId)";

        //we don't use a try-with-resource, because we return a pointer to the results,
        //not the actual results, so we should not close this BgeePreparedStatement.
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sql);
            return log.traceExit(new MySQLSpeciesTOResultSet(stmt));
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
        
        StringBuilder sql = new StringBuilder(); 
        sql.append("INSERT INTO species " +  
                   "(speciesId, genus, species, speciesCommonName, speciesDisplayOrder, taxonId, " +
                   "genomeFilePath, genomeVersion, dataSourceId, genomeSpeciesId) values ");
        for (int i = 0; i < specieTOs.size(); i++) {
            if (i > 0) {
                sql.append(", ");
            }
            sql.append("(?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ");
        }
        try (BgeePreparedStatement stmt = 
                this.getManager().getConnection().prepareStatement(sql.toString())) {
            int paramIndex = 1;
            for (SpeciesTO speciesTO: specieTOs) {
                stmt.setInt(paramIndex, speciesTO.getId());
                paramIndex++;
                stmt.setString(paramIndex, speciesTO.getGenus());
                paramIndex++;
                stmt.setString(paramIndex, speciesTO.getSpeciesName());
                paramIndex++;
                stmt.setString(paramIndex, speciesTO.getName());
                paramIndex++;
                stmt.setInt(paramIndex, speciesTO.getDisplayOrder());
                paramIndex++;
                stmt.setInt(paramIndex, speciesTO.getParentTaxonId());
                paramIndex++;
                stmt.setString(paramIndex, speciesTO.getGenomeFilePath());
                paramIndex++;
                stmt.setString(paramIndex, speciesTO.getGenomeVersion());
                paramIndex++;
                stmt.setInt(paramIndex, speciesTO.getDataSourceId());
                paramIndex++;
                //TODO: handles default values in a better way
                //We should create setter methods in BgeePreparedStatement, accepting a third argument, being the default value
                if (speciesTO.getGenomeSpeciesId() != null && 
                        !speciesTO.getGenomeSpeciesId().equals(speciesTO.getId())) {
                    stmt.setInt(paramIndex, speciesTO.getGenomeSpeciesId());
                } else {
                    stmt.setInt(paramIndex, 0);
                }
                paramIndex++;
            }
            
            return log.traceExit(stmt.executeUpdate());
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
            Integer speciesId = null, taxonId = null, genomeSpeciesId = null, displayOrder = null, 
                    dataSourceId = null;
            String genus = null, species = null, speciesCommonName = null, 
                   genomeFilePath = null, genomeVersion = null;
            // Get results
            try {
                for (Entry<Integer, String> column: this.getColumnLabels().entrySet()) {
                    String columnName = column.getValue();
                    int columnIndex = column.getKey();
                    SpeciesDAO.Attribute attr = getAttributeFromColName(columnName, COL_TO_ATTR_MAP);
                    switch (attr) {
                    case ID:
                        speciesId = this.getCurrentResultSet().getInt(columnIndex);
                        break;
                    case GENUS:
                        genus = this.getCurrentResultSet().getString(columnIndex);
                        break;
                    case SPECIES_NAME:
                        species = this.getCurrentResultSet().getString(columnIndex);
                        break;
                    case COMMON_NAME:
                        speciesCommonName = this.getCurrentResultSet().getString(columnIndex);
                        break;
                    case DISPLAY_ORDER:
                        displayOrder = this.getCurrentResultSet().getInt(columnIndex);
                        break;
                    case PARENT_TAXON_ID:
                        taxonId = this.getCurrentResultSet().getInt(columnIndex);
                        break;
                    case GENOME_FILE_PATH:
                        genomeFilePath = this.getCurrentResultSet().getString(columnIndex);
                        break;
                    case GENOME_VERSION:
                        genomeVersion = this.getCurrentResultSet().getString(columnIndex);
                        break;
                    case DATA_SOURCE_ID:
                        dataSourceId = this.getCurrentResultSet().getInt(columnIndex);
                        break;
                    case GENOME_SPECIES_ID:
                        genomeSpeciesId = this.getCurrentResultSet().getInt(columnIndex);
                        break;
                    default:
                        log.throwing(new UnrecognizedColumnException(columnName));
                    }
                }
            } catch (SQLException e) {
                throw log.throwing(new DAOException(e));
            }
            //Set SpeciesTO
            return log.traceExit(new SpeciesTO(speciesId, speciesCommonName, genus, species,
                    displayOrder, taxonId, genomeFilePath, genomeVersion, dataSourceId, 
                    genomeSpeciesId));
        }
    }
}