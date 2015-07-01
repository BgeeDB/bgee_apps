package org.bgee.model.dao.mysql.gene;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;

/**
 * A {@code GeneDAO} for MySQL. 
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @see org.bgee.model.dao.api.gene.GeneDAO.GeneTO
 * @since Bgee 13
 */
public class MySQLGeneDAO extends MySQLDAO<GeneDAO.Attribute> implements GeneDAO {

    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = LogManager.getLogger(MySQLGeneDAO.class.getName());

    /**
     * Constructor providing the {@code MySQLDAOManager} that this {@code MySQLDAO} 
     * will use to obtain {@code BgeeConnection}s.
     * 
     * @param manager   The {@code MySQLDAOManager} to use.
     * @throws IllegalArgumentException If {@code manager} is {@code null}.
     */
    public MySQLGeneDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    //***************************************************************************
    // METHODS NOT PART OF THE bgee-dao-api, USED BY THE PIPELINE AND NOT MEANT 
    // TO BE EXPOSED TO THE PUBLIC API.
    //***************************************************************************

    @Override
    public GeneTOResultSet getAllGenes() throws DAOException {
        log.entry();
        
        //Construct sql query
        String geneTableName = "gene";
        
        String sql = this.generateSelectClause(this.getAttributes(), geneTableName);
        
        sql += " FROM " + geneTableName;
        //we don't use a try-with-resource, because we return a pointer to the results, 
        //not the actual results, so we should not close this BgeePreparedStatement.
        try {
            return log.exit(new MySQLGeneTOResultSet(
                    this.getManager().getConnection().prepareStatement(sql)));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }
    
    @Override
    //A warning is issued because we do not close the BgeePreparedStatement we use, 
    //but if we closed the PreparedStatement, it would close the ResultSet returned. 
    //The BgeePreparedStatement will be closed when the ResultSet will be closed. 
    @SuppressWarnings("resource")
    public GeneTOResultSet getGenesBySpeciesIds(Set<String> speciesIds) throws DAOException {
        log.entry();      

        //Construct sql query
        String geneTableName = "gene";
        
        String sql = this.generateSelectClause(this.getAttributes(), geneTableName);
        
        sql += " FROM " + geneTableName;
        
        if (speciesIds != null && speciesIds.size() > 0) {
            sql += " WHERE gene.speciesId IN (" + 
                       BgeePreparedStatement.generateParameterizedQueryString(
                               speciesIds.size()) + ")";
        }

        //we don't use a try-with-resource, because we return a pointer to the results, 
        //not the actual results, so we should not close this BgeePreparedStatement.
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sql);
            if (speciesIds != null && speciesIds.size() > 0) {
                List<Integer> orderedSpeciesIds = MySQLDAO.convertToOrderedIntList(speciesIds);
                stmt.setIntegers(1, orderedSpeciesIds);
            }             
            return log.exit(new MySQLGeneTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    @Override
    public int updateGenes(Collection<GeneTO> genes, 
            Collection<GeneDAO.Attribute> attributesToUpdate) 
            throws DAOException, IllegalArgumentException {
        log.entry(genes, attributesToUpdate);
        
        if (genes == null || genes.isEmpty()) {
            throw log.throwing(new IllegalArgumentException(
                    "No gene is given, then no gene is updated"));
        }
        if (attributesToUpdate == null || attributesToUpdate.isEmpty()) {
            throw log.throwing(new IllegalArgumentException(
                    "No attribute is given, then no gene is updated"));
        }
//        if (attributesToUpdate.contains(GeneDAO.Attribute.ANCESTRAL_OMA_NODE_ID) ||
//                attributesToUpdate.contains(GeneDAO.Attribute.ANCESTRAL_OMA_TAXON_ID)) {
//            throw log.throwing(new IllegalArgumentException(
//                    "'Ancestral OMA' attributes are not store in database, then no gene is updated"));
//        }
        
        int geneUpdatedCount = 0;
        //Construct sql query according to currents attributes
        StringBuilder sql = new StringBuilder(); 

        for (GeneDAO.Attribute attribute: attributesToUpdate) {
            if (sql.length() == 0) {
                sql.append("UPDATE gene SET ");
            } else {
                sql.append(", ");
            }
            sql.append(this.attributeToString(attribute) + " = ?");
        }
        sql.append(" WHERE geneId = ?");

        try (BgeePreparedStatement stmt = 
                this.getManager().getConnection().prepareStatement(sql.toString())) {
            for (GeneTO gene: genes) {
                int i = 1;
                for (GeneDAO.Attribute attribute : attributesToUpdate) {
                    if (attribute.equals(GeneDAO.Attribute.NAME)) {
                        stmt.setString(i++, gene.getName());
                    } else if (attribute.equals(GeneDAO.Attribute.DESCRIPTION)) {
                        stmt.setString(i++, gene.getDescription());
                    } else if (attribute.equals(GeneDAO.Attribute.SPECIES_ID)) {
                        stmt.setInt(i++, gene.getSpeciesId());
                    } else if (attribute.equals(GeneDAO.Attribute.GENE_BIO_TYPE_ID)) {
                        stmt.setInt(i++, gene.getGeneBioTypeId());
                    } else if (attribute.equals(GeneDAO.Attribute.OMA_PARENT_NODE_ID)) {
                        stmt.setInt(i++, gene.getOMAParentNodeId());
                    } else if (attribute.equals(GeneDAO.Attribute.ENSEMBL_GENE)) {
                        stmt.setBoolean(i++, gene.isEnsemblGene());
                    }
                }
                stmt.setString(i, gene.getId());
                geneUpdatedCount += stmt.executeUpdate();
                stmt.clearParameters();
            }
            return log.exit(geneUpdatedCount);
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }
    
    /**
     * Generates the SELECT clause of a MySQL query used to retrieve {@code GeneTO}s.
     * 
     * @param attributes            A {@code Set} of {@code Attribute}s defining 
     *                              the columns/information the query should retrieve.
     * @param diffExprTableName     A {@code String} defining the name of the gene table used.
     * @return                      A {@code String} containing the SELECT clause 
     *                              for the requested query.
     * @throws IllegalArgumentException If one {@code Attribute} of {@code attributes} is unknown.
     */
    private String generateSelectClause(Set<GeneDAO.Attribute> attributes, String geneTableName) 
                            throws IllegalArgumentException {
        log.entry(attributes, geneTableName);
        
        Set<GeneDAO.Attribute> attributesToUse = new HashSet<GeneDAO.Attribute>(attributes);
        if (attributes == null || attributes.isEmpty()) {
//            // we exclude ANCESTRAL_OMA_NODE_ID and ANCESTRAL_OMA_TAXON_ID
//            // TODO remove when retrieving 'Ancestral OMA' data will be implemented and tested 
//            attributesToUse = EnumSet.complementOf(EnumSet.of(
//                    GeneDAO.Attribute.ANCESTRAL_OMA_NODE_ID, 
//                    GeneDAO.Attribute.ANCESTRAL_OMA_TAXON_ID));
            attributesToUse = EnumSet.allOf(GeneDAO.Attribute.class);
            //return log.exit("SELECT " + geneTableName + ".* ");
        }

        String sql = "";
        for (GeneDAO.Attribute attribute: attributesToUse) {
//            if (attribute.equals(GeneDAO.Attribute.ANCESTRAL_OMA_NODE_ID) ||
//                    attribute.equals(GeneDAO.Attribute.ANCESTRAL_OMA_TAXON_ID)) {
//                // TODO remove when retrieving 'Ancestral OMA' data will be implemented and tested
//                continue;
//            }
                
            if (sql.isEmpty()) {
                sql += "SELECT ";
                //does the attributes requested ensure that there will be no duplicated results?
                if (!attributesToUse.contains(GeneDAO.Attribute.ID)) {
                    sql += "DISTINCT ";
                }
            } else {
                sql += ", ";
            }
            sql += geneTableName + "." + this.attributeToString(attribute);
        }
        return log.exit(sql);
    }

    /** 
     * Returns a {@code String} that correspond to the given {@code GeneDAO.Attribute}.
     * 
     * @param attribute   An {code GeneDAO.Attribute} that is the attribute to
     *                    convert into a {@code String}.
     * @return            A {@code String} that corresponds to the given {@code GeneDAO.Attribute}
     * @throws IllegalArgumentException If the {@code attribute} is unknown.
     */
    /*
     * We kept this method, as opposed other DAOs, because is redundantly used in this class
     */
    private String attributeToString(GeneDAO.Attribute attribute) throws IllegalArgumentException {
        log.entry(attribute);
        
        String label = null;
        if (attribute.equals(GeneDAO.Attribute.ID)) {
            label = "geneId";
        } else if (attribute.equals(GeneDAO.Attribute.NAME)) {
            label = "geneName";
        } else if (attribute.equals(GeneDAO.Attribute.DESCRIPTION)) {
            label = "geneDescription";
        } else if (attribute.equals(GeneDAO.Attribute.SPECIES_ID)) {
            label = "speciesId";
        } else if (attribute.equals(GeneDAO.Attribute.GENE_BIO_TYPE_ID)) {
            label = "geneBioTypeId";
        } else if (attribute.equals(GeneDAO.Attribute.OMA_PARENT_NODE_ID)) {
            label = "OMAParentNodeId";
        } else if (attribute.equals(GeneDAO.Attribute.ENSEMBL_GENE)) {
            label = "ensemblGene";
//        } else if (attribute.equals(GeneDAO.Attribute.ANCESTRAL_OMA_NODE_ID)) {
//            label = "ancestralOMANodeId";
//        } else if (attribute.equals(GeneDAO.Attribute.ANCESTRAL_OMA_TAXON_ID)) {
//            label = "ancestralOMATaxonId";
        } else {
            throw log.throwing(new IllegalArgumentException("The attribute provided (" + 
                    attribute.toString() + ") is unknown for " + GeneDAO.class.getName()));
        }
        return log.exit(label);
    }

    /**
     * A {@code MySQLDAOResultSet} specific to {@code GeneTO}.
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public class MySQLGeneTOResultSet extends MySQLDAOResultSet<GeneTO> 
            implements GeneTOResultSet {

        /**
         * Delegates to {@link MySQLDAOResultSet#MySQLDAOResultSet(BgeePreparedStatement)}
         * super constructor.
         * 
         * @param statement The first {@code BgeePreparedStatement} to execute a query on.
         */
        private MySQLGeneTOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }

        @Override
        protected GeneTO getNewTO() {
            log.entry();
            String geneId = null, geneName = null, geneDescription = null;
            Integer speciesId = null, geneBioTypeId = null, OMAParentNodeId = null;
            Boolean ensemblGene = null;
            // Get results
            for (Entry<Integer, String> column: this.getColumnLabels().entrySet()) {
                try {
                    if (column.getValue().equals("geneId")) {
                        geneId = this.getCurrentResultSet().getString(column.getKey());

                    } else if (column.getValue().equals("geneName")) {
                        geneName = this.getCurrentResultSet().getString(column.getKey());

                    } else if (column.getValue().equals("geneDescription")) {
                        geneDescription = this.getCurrentResultSet().getString(column.getKey());

                    } else if (column.getValue().equals("speciesId")) {
                        speciesId = this.getCurrentResultSet().getInt(column.getKey());

                    } else if (column.getValue().equals("geneBioTypeId")) {
                        geneBioTypeId = this.getCurrentResultSet().getInt(column.getKey());

                    } else if (column.getValue().equals("OMAParentNodeId")) {
                        OMAParentNodeId = this.getCurrentResultSet().getInt(column.getKey());

                    } else if (column.getValue().equals("ensemblGene")) {
                        ensemblGene = this.getCurrentResultSet().getBoolean(column.getKey());
                        
                    } else {
                        throw log.throwing(new UnrecognizedColumnException(column.getValue()));
                    }
                } catch (SQLException e) {
                    throw log.throwing(new DAOException(e));
                }
            }
            //Set GeneTO
            return log.exit(new GeneTO(geneId, geneName, geneDescription, speciesId, geneBioTypeId, 
                    OMAParentNodeId, ensemblGene));
        }
    }
}
