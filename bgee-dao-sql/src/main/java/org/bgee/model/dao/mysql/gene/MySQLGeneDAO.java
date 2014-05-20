package org.bgee.model.dao.mysql.gene;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

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
		
		//Construct sql query according to currents attributes
		Collection<GeneDAO.Attribute> attributes = this.getAttributes();
		StringBuilder sql = new StringBuilder("SELECT ");
		Boolean isFirstIteration = true;
		for (GeneDAO.Attribute attribute: attributes) {
			if (isFirstIteration) {
				isFirstIteration = false;
			} else {
				sql.append(", ");
			}
			if (attribute.equals(GeneDAO.Attribute.ID)) {
				sql.append("geneId");
			} else if (attribute.equals(GeneDAO.Attribute.NAME)) {
				sql.append("geneName");
			} else if (attribute.equals(GeneDAO.Attribute.DESCRIPTION)) {
				sql.append("geneDescription");
			} else if (attribute.equals(GeneDAO.Attribute.SPECIESID)) {
				sql.append("speciesId");
			} else if (attribute.equals(GeneDAO.Attribute.GENEBIOTYPEID)) {
				sql.append("geneBioTypeId");
			} else if (attribute.equals(GeneDAO.Attribute.OMAPARENTNODEID)) {
				sql.append("OMAParentNodeId");
			} else if (attribute.equals(GeneDAO.Attribute.ENSEMBLGENE)) {
				sql.append("ensemblGene");
			}
		}
		sql.append(" FROM gene");

		try (BgeePreparedStatement stmt = 
				this.getManager().getConnection().prepareStatement(sql.toString())) {
        	return log.exit((GeneTOResultSet) new MySQLGeneTOResultSet(stmt));
        } catch (SQLException e) {
        	throw log.throwing(new DAOException(e));
        }
	}

	/**
	 * Update the provided genes with OMA parent Node ID into the Bgee database, 
     * represented as a {@code Collection} of {@code GeneTO}s
     * 
	 * @param genes	A {@code Collection} of {@code GeneTO}s to be updated into the database.
	 * @return	A {@code int} representing the number of genes updated.
     * @throws DAOException		If a {@code SQLException} occurred while trying 
     *                          to update {@code gene}. The {@code SQLException} 
     *                          will be wrapped into a {@code DAOException} ({@code DAOs} 
     *                          do not expose these kind of implementation details).
	 */
	public int updateOMAGroupIDs(Collection<GeneTO> genes) throws DAOException {
		log.entry(genes);
		int geneUpdatedCount = 0;
		String sql = "UPDATE gene SET OMAParentNodeId = ? WHERE geneId = ?";

		try (BgeePreparedStatement stmt = 
				this.getManager().getConnection().prepareStatement(sql)) {
			for (GeneTO gene: genes) {
				stmt.setInt(1, gene.getOMAParentNodeId());
				stmt.setString(2, gene.getId());
				geneUpdatedCount += stmt.executeUpdate();
				stmt.clearParameters();
			}
			return log.exit(geneUpdatedCount);
		} catch (SQLException e) {
			throw log.throwing(new DAOException(e));
		}
	}

	@Override
	public int updateGenes(Collection<GeneTO> genes, Collection<GeneDAO.Attribute> attributesToUpdate) {
		//TODO generic method of updateOMAGroupIDs
		return 0;
	}

	/**
	 * A {@code MySQLDAOResultSet} specific to {@code GeneTO}.
	 * 
	 * @author Valentine Rech de Laval
	 * @version Bgee 13
	 * @since Bgee 13
	 */
	public class MySQLGeneTOResultSet extends MySQLDAOResultSet<GeneTO> {

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
//			TODO get resultsetmetadata
			String geneId=null, geneName=null, geneDescription=null;
			int speciesId=0, geneBioTypeId=0, OMAParentNodeId=0;
			Boolean ensemblGene=null;
			// Get results
			try {
				geneId = currentResultSet.getString("geneId");
			} catch (SQLException e) {
			}
			try {
				geneName = currentResultSet.getString("geneName");
			} catch (SQLException e) {
			}
			try {
				geneDescription = currentResultSet.getString("geneDescription");
			} catch (SQLException e) {
			}
			try {
				speciesId = currentResultSet.getInt("speciesId");
			} catch (SQLException e) {
			}
			try {
				geneBioTypeId = currentResultSet.getInt("geneBioTypeId");
			} catch (SQLException e) {
			}
			try {
				OMAParentNodeId = currentResultSet.getInt("OMAParentNodeId");
			} catch (SQLException e) {
			}
			try {
				ensemblGene = currentResultSet.getBoolean("ensemblGene");
			} catch (SQLException e) {
			}
			//Set GeneTO
			return log.exit(new GeneTO(geneId,
					geneName, 
					geneDescription, 
					speciesId,
					geneBioTypeId, 
					OMAParentNodeId, 
					ensemblGene));
		}
	}
}
