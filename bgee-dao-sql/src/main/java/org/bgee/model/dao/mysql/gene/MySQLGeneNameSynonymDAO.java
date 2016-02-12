package org.bgee.model.dao.mysql.gene;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.gene.GeneNameSynonymDAO;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;

/**
 * The MySQL implementation of {@code GeneNameSynonymDAO}
 * 
 * @version Bgee 13.2
 * @author Philippe Moret
 * @since Bgee 13.2
 *
 */
public class MySQLGeneNameSynonymDAO extends MySQLDAO<GeneNameSynonymDAO.Attribute> implements GeneNameSynonymDAO {

	private static final Logger log = LogManager.getLogger(MySQLGeneDAO.class.getName());

	/**
	 * The table name
	 */
	private static final String GENE_NAME_SYNONYM_TABLE = "geneNameSynonym";

	/**
	 * Constructor providing the manager
	 * @param manager A {@code MySQLDAOManager} instance
	 * @throws IllegalArgumentException If an error occurs
	 */
	public MySQLGeneNameSynonymDAO(MySQLDAOManager manager) throws IllegalArgumentException {
		super(manager);
	}

	/**
	 * The {@code Map} of column names to {@link Attribute}
	 */
	private static final Map<String, GeneNameSynonymDAO.Attribute> COL_NAMES_TO_ATTRS;

	static {
		Map<String, GeneNameSynonymDAO.Attribute> tempMap = new HashMap<String, GeneNameSynonymDAO.Attribute>();
		tempMap.put("geneId", GeneNameSynonymDAO.Attribute.GENE_ID);
		tempMap.put("geneNameSynonym", GeneNameSynonymDAO.Attribute.GENE_NAME_SYNONYM);
		COL_NAMES_TO_ATTRS = Collections.unmodifiableMap(tempMap);
	}

	/**
	 * The MySQL implementation of {@link GeneNameSynonymTOResultSet}
     * @version Bgee 13.2
 	 * @author Philippe Moret
 	 * @since Bgee 13.2
	 */
	public class MySQLGeneNameSynonymTOResultSet extends MySQLDAOResultSet<GeneNameSynonymTO>
	        implements GeneNameSynonymTOResultSet {

		protected MySQLGeneNameSynonymTOResultSet(BgeePreparedStatement statement) {
			super(statement);
		}

		@Override
		protected GeneNameSynonymTO getNewTO() throws DAOException, UnrecognizedColumnException {
			log.entry();
			try {
				final ResultSet currentResultSet = this.getCurrentResultSet();
				String geneId = null, geneNameSynonym = null;

				for (Map.Entry<Integer, String> col : this.getColumnLabels().entrySet()) {
					String columnName = col.getValue();
					GeneNameSynonymDAO.Attribute attr = MySQLGeneNameSynonymDAO.this.getAttributeFromColName(columnName,
					        COL_NAMES_TO_ATTRS);
					switch (attr) {
					case GENE_ID:
						geneId = currentResultSet.getString(col.getKey());
						break;
					case GENE_NAME_SYNONYM:
						geneNameSynonym = currentResultSet.getString(col.getKey());
						break;
					default:
						log.throwing(new UnrecognizedColumnException(columnName));
					}
				}
				return log.exit(new GeneNameSynonymTO(geneId, geneNameSynonym));
			} catch (SQLException e) {
				throw log.throwing(new DAOException(e));
			}
		}

	}

	@Override
	public GeneNameSynonymTOResultSet getGeneNameSynonyms(Set<String> geneIds) {
		log.entry(geneIds);
		// Construct sql query
		String sql = this.generateSelectClause(GENE_NAME_SYNONYM_TABLE, COL_NAMES_TO_ATTRS, true);
		sql += " FROM " + GENE_NAME_SYNONYM_TABLE;
		sql += " WHERE geneId IN ("+BgeePreparedStatement.generateParameterizedQueryString(geneIds.size())+")";
		
		try {
			BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sql);
			
			stmt.setStrings(1, geneIds, true);
			// we don't use a try-with-resource, because we return a pointer to the
			// results,
			// not the actual results, so we should not close this
			// BgeePreparedStatement.
			return log
			        .exit(new MySQLGeneNameSynonymTOResultSet(stmt));
		} catch (SQLException e) {
			throw log.throwing(new DAOException(e));
		}
	}

}
