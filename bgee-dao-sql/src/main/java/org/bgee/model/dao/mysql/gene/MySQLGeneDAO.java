package org.bgee.model.dao.mysql.gene;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
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
        
        Collection<GeneDAO.Attribute> attributes = this.getAttributes();
        //Construct sql query
        String sql = new String(); 
        if (attributes == null || attributes.size() == 0) {
            sql += "SELECT *";
        } else {
            for (GeneDAO.Attribute attribute: attributes) {
                if (sql.length() == 0) {
                    sql += "SELECT DISTINCT ";
                } else {
                    sql += ", ";
                }
                sql += this.attributeToString(attribute);
            }
        }
        sql += " FROM gene";
        //we don't use a try-with-resource, because we return a pointer to the results, 
        //not the actual results, so we should not close this BgeePreparedStatement.
        BgeePreparedStatement stmt = null;
        try {
            stmt = this.getManager().getConnection().prepareStatement(sql.toString());
            return log.exit(new MySQLGeneTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }
    
    @Override
    public GeneTOResultSet getGenes(Set<String> speciesIds) throws DAOException {
        log.entry();      

        Collection<GeneDAO.Attribute> attributes = this.getAttributes();
        //Construct sql query
        String sql = new String(); 
        if (attributes == null || attributes.size() == 0) {
            sql += "SELECT *";
        } else {
            for (GeneDAO.Attribute attribute: attributes) {
                if (sql.length() == 0) {
                    sql += "SELECT DISTINCT ";
                } else {
                    sql += ", ";
                }
                sql += this.attributeToString(attribute);
            }
        }
        sql += " FROM gene";
        
        if (speciesIds != null && speciesIds.size() > 0) {
            sql += " WHERE gene.speciesId IN (" + 
                       BgeePreparedStatement.generateParameterizedQueryString(
                               speciesIds.size()) + ")";
        }

        //we don't use a try-with-resource, because we return a pointer to the results, 
        //not the actual results, so we should not close this BgeePreparedStatement.
        BgeePreparedStatement stmt = null;
        try {
            stmt = this.getManager().getConnection().prepareStatement(sql.toString());
            if (speciesIds != null && speciesIds.size() > 0) {
                List<Integer> orderedSpeciesIds = MySQLDAO.convertToIntList(speciesIds);
                Collections.sort(orderedSpeciesIds);
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
        // TODO add check on attributesToUpdate
        
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
                    } else if (attribute.equals(GeneDAO.Attribute.SPECIESID)) {
                        stmt.setInt(i++, gene.getSpeciesId());
                    } else if (attribute.equals(GeneDAO.Attribute.GENEBIOTYPEID)) {
                        stmt.setInt(i++, gene.getGeneBioTypeId());
                    } else if (attribute.equals(GeneDAO.Attribute.OMAPARENTNODEID)) {
                        stmt.setInt(i++, gene.getOMAParentNodeId());
                    } else if (attribute.equals(GeneDAO.Attribute.ENSEMBLGENE)) {
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

    private String attributeToString(GeneDAO.Attribute attribute) {
        log.entry(attribute);
        
        String label = null;
        if (attribute.equals(GeneDAO.Attribute.ID)) {
            label = "geneId";
        } else if (attribute.equals(GeneDAO.Attribute.NAME)) {
            label = "geneName";
        } else if (attribute.equals(GeneDAO.Attribute.DESCRIPTION)) {
            label = "geneDescription";
        } else if (attribute.equals(GeneDAO.Attribute.SPECIESID)) {
            label = "speciesId";
        } else if (attribute.equals(GeneDAO.Attribute.GENEBIOTYPEID)) {
            label = "geneBioTypeId";
        } else if (attribute.equals(GeneDAO.Attribute.OMAPARENTNODEID)) {
            label = "OMAParentNodeId";
        } else if (attribute.equals(GeneDAO.Attribute.ENSEMBLGENE)) {
            label = "ensemblGene";
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
        public GeneTO getTO() {
            log.entry();
            ResultSet currentResultSet = this.getCurrentResultSet();
            String geneId = null, geneName = null, geneDescription = null;
            Integer speciesId = null, geneBioTypeId = null, OMAParentNodeId = null;
            Boolean ensemblGene = null;
            // Get results
            for (Entry<Integer, String> column: this.getColumnLabels().entrySet()) {
                try {
                    if (column.getValue().equals("geneId")) {
                        geneId = currentResultSet.getString(column.getKey());

                    } else if (column.getValue().equals("geneName")) {
                        geneName = currentResultSet.getString(column.getKey());

                    } else if (column.getValue().equals("geneDescription")) {
                        geneDescription = currentResultSet.getString(column.getKey());

                    } else if (column.getValue().equals("speciesId")) {
                        speciesId = currentResultSet.getInt(column.getKey());

                    } else if (column.getValue().equals("geneBioTypeId")) {
                        geneBioTypeId = currentResultSet.getInt(column.getKey());

                    } else if (column.getValue().equals("OMAParentNodeId")) {
                        OMAParentNodeId = currentResultSet.getInt(column.getKey());

                    } else if (column.getValue().equals("ensemblGene")) {
                        ensemblGene = currentResultSet.getBoolean(column.getKey());
                    }
                } catch (SQLException e) {
                    throw log.throwing(new DAOException(e));
                }
            }
            //Set GeneTO
            return log.exit(new GeneTO(geneId, geneName, geneDescription, speciesId,
                    geneBioTypeId, OMAParentNodeId, ensemblGene));
        }
    }
}
