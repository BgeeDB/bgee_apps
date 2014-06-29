package org.bgee.model.dao.mysql.gene;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

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
        String sql = "SELECT " + this.getSelectClause(this.getAttributes()) + " FROM " + 
                MySQLDAO.GENE_TABLE_NAME;

        //we don't use a try-with-resource, because we return a pointer to the results, 
        //not the actual results, so we should not close this BgeePreparedStatement.
        BgeePreparedStatement stmt = null;
        try {
            stmt = this.getManager().getConnection().prepareStatement(sql);
            return log.exit(new MySQLGeneTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }
    
    @Override
    public int updateGenes(Collection<GeneTO> genes, 
            Collection<GeneDAO.Attribute> attributesToUpdate) {
        log.entry(genes, attributesToUpdate);
        int geneUpdatedCount = 0;
        //Construct sql query according to currents attributes
        StringBuilder sql = new StringBuilder(); 

        for (GeneDAO.Attribute attribute: attributesToUpdate) {
            if (sql.length() == 0) {
                sql.append("UPDATE ");
                sql.append(MySQLDAO.GENE_TABLE_NAME);
                sql.append(" SET ");
            } else {
                sql.append(", ");
            }
            sql.append(this.getSQLExpr(attribute) + " = ?");
        }
        sql.append(" WHERE ");
        sql.append(this.getSQLExpr(GeneDAO.Attribute.ID));
        sql.append(" = ?");

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

    @Override
    public String getLabel(GeneDAO.Attribute attribute) {
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
    
    @Override
    public String getSQLExpr(GeneDAO.Attribute attribute) {
        log.entry(attribute);
        //no complex SQL expression in this DAO, we just build table_name.label
        return log.exit(MySQLDAO.GENE_TABLE_NAME + "." + this.getLabel(attribute));
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
         * Delegates to {@link MySQLDAOResultSet#MySQLDAOResultSet(BgeePreparedStatement)
         * super constructor.
         * 
         * @param statement The first {@code BgeePreparedStatement} to execute a query on.
         */
        public MySQLGeneTOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }

        @Override
        public GeneTO getTO() {
            log.entry();
            ResultSet currentResultSet = this.getCurrentResultSet();
            Map<Integer, String> currentColumnLabels = this.getColumnLabels();
            String geneId=null, geneName=null, geneDescription=null;
            int speciesId=0, geneBioTypeId=0, OMAParentNodeId=0;
            boolean ensemblGene=false;
            // Get results
            for (String currentColumnLabel : currentColumnLabels.values()) {
                try {
                    if (currentColumnLabel.equals("geneId")) {
                        geneId = currentResultSet.getString("geneId");
                    } else if (currentColumnLabel.equals("geneName")) {
                        geneName = currentResultSet.getString("geneName");
                    } else if (currentColumnLabel.equals("geneDescription")) {
                        geneDescription = currentResultSet.getString("geneDescription");
                    } else if (currentColumnLabel.equals("speciesId")) {
                        speciesId = currentResultSet.getInt("speciesId");
                    } else if (currentColumnLabel.equals("geneBioTypeId")) {
                        geneBioTypeId = currentResultSet.getInt("geneBioTypeId");
                    } else if (currentColumnLabel.equals("OMAParentNodeId")) {
                        OMAParentNodeId = currentResultSet.getInt("OMAParentNodeId");
                    } else if (currentColumnLabel.equals("ensemblGene")) {
                        ensemblGene = currentResultSet.getBoolean("ensemblGene");
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
