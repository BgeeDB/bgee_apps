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
		StringBuilder sql = new StringBuilder("SELECT ");
		sql.append(getSelectExpr());
		sql.append(" FROM gene");

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

    public String getSelectExpr() {
		log.entry();
		StringBuilder selectExpr = new StringBuilder();
		Collection<GeneDAO.Attribute> attributes = this.getAttributes();
		Boolean isFirstIteration = true;
		for (GeneDAO.Attribute attribute: attributes) {
			if (isFirstIteration) {
				isFirstIteration = false;
			} else {
				selectExpr.append(", ");
			}
			if (attribute.equals(GeneDAO.Attribute.ID)) {
				selectExpr.append("geneId");
			} else if (attribute.equals(GeneDAO.Attribute.NAME)) {
				selectExpr.append("geneName");
			} else if (attribute.equals(GeneDAO.Attribute.DESCRIPTION)) {
				selectExpr.append("geneDescription");
			} else if (attribute.equals(GeneDAO.Attribute.SPECIESID)) {
				selectExpr.append("speciesId");
			} else if (attribute.equals(GeneDAO.Attribute.GENEBIOTYPEID)) {
				selectExpr.append("geneBioTypeId");
			} else if (attribute.equals(GeneDAO.Attribute.OMAPARENTNODEID)) {
				selectExpr.append("OMAParentNodeId");
			} else if (attribute.equals(GeneDAO.Attribute.ENSEMBLGENE)) {
				selectExpr.append("ensemblGene");
			}
		}
    	return log.exit(selectExpr.toString());
    }
	@Override
	public int updateGenes(Collection<GeneTO> genes, 
			Collection<GeneDAO.Attribute> attributesToUpdate) {
		log.entry(genes);
		int geneUpdatedCount = 0;
		//Construct sql query according to currents attributes
		StringBuilder sql = new StringBuilder("UPDATE gene SET ");  
		Boolean isFirstIteration = true;
		for (GeneDAO.Attribute attribute: attributesToUpdate) {
			if (isFirstIteration) {
				isFirstIteration = false;
			} else {
				sql.append(", ");
			}
			if (attribute.equals(GeneDAO.Attribute.NAME)) {
				sql.append("geneName = ?");
			} else if (attribute.equals(GeneDAO.Attribute.DESCRIPTION)) {
				sql.append("geneDescription = ?");
			} else if (attribute.equals(GeneDAO.Attribute.SPECIESID)) {
				sql.append("speciesId = ?");
			} else if (attribute.equals(GeneDAO.Attribute.GENEBIOTYPEID)) {
				sql.append( "geneBioTypeId = ?");
			} else if (attribute.equals(GeneDAO.Attribute.OMAPARENTNODEID)) {
				sql.append("OMAParentNodeId = ?");
			} else if (attribute.equals(GeneDAO.Attribute.ENSEMBLGENE)) {
				sql.append("ensemblGene = ?");
			}
		}
		sql.append(" WHERE geneId = ?");
		try (BgeePreparedStatement stmt = 
				this.getManager().getConnection().prepareStatement(sql.toString())) {
			for (GeneTO gene: genes) {
				int i = 1;
				for (GeneDAO.Attribute attribute: attributesToUpdate) {
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

	/**
	 * A {@code MySQLDAOResultSet} specific to {@code GeneTO}.
	 * 
	 * @author Valentine Rech de Laval
	 * @version Bgee 13
	 * @since Bgee 13
	 */
	public class MySQLGeneTOResultSet extends MySQLDAOResultSet<GeneTO> implements GeneTOResultSet {

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
			Boolean ensemblGene=null;
			// Get results
			for (String currentColumnLabel : currentColumnLabels.values()) {
				try {
					if (currentColumnLabel.equals(GeneDAO.Attribute.ID)) {
						geneId = currentResultSet.getString("geneId");
					} else if (currentColumnLabel.equals(GeneDAO.Attribute.NAME)) {
						geneName = currentResultSet.getString("geneName");
					} else if (currentColumnLabel.equals(GeneDAO.Attribute.DESCRIPTION)) {
						geneDescription = currentResultSet.getString("geneDescription");
					} else if (currentColumnLabel.equals(GeneDAO.Attribute.SPECIESID)) {
						speciesId = currentResultSet.getInt("speciesId");
					} else if (currentColumnLabel.equals(GeneDAO.Attribute.GENEBIOTYPEID)) {
						geneBioTypeId = currentResultSet.getInt("geneBioTypeId");
					} else if (currentColumnLabel.equals(GeneDAO.Attribute.OMAPARENTNODEID)) {
						OMAParentNodeId = currentResultSet.getInt("OMAParentNodeId");
					} else if (currentColumnLabel.equals(GeneDAO.Attribute.ENSEMBLGENE)) {
						ensemblGene = currentResultSet.getBoolean("ensemblGene");
					}
				} catch (SQLException e) {
					log.throwing(new DAOException(e));				
				}
			}
			//Set GeneTO
			return log.exit(new GeneTO(geneId, geneName, geneDescription, speciesId,
					geneBioTypeId, OMAParentNodeId, ensemblGene));
		}
	}
}
