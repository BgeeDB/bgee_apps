package model.data.common.expressionData.affymetrixData;

import java.util.Collection;

import model.data.common.TransferObject;
import model.data.common.anatomy.MultiSpeciesTO;
import model.data.common.expressionData.DataTypeTO;

/**
 * An <code>interface</code> that must be implemented by all <code>DAO</code>s 
 * related to Affymetrix probesets; 
 * for instance, to retrieve Affymetrix probesets from a data source, 
 * or to update Affymetrix probesets into a data source.
 *  
 * The communication between the DAO and the <code>model</code> layer 
 * is achieved through the use of <code>TransferObject</code>s  
 * (in that case, <code>AffymetrixProbesetTO</code>s).
 * 
 * @author Frederic Bastian
 * @version Bgee 12
 * @see AffymetrixProbesetTO
 * @see model.data.sql.mysql.expressionData.affymetrixData.MysqlAffymetrixProbesetDAO
 * @since Bgee 01
 *
 */
public interface AffymetrixProbesetDAO 
{
	/**
	 * Obtain a <code>Collection</code> of <code>TransferObject</code>s, 
	 * castable to <code>AffymetrixProbesetTO</code>s, 
	 * from an expression query to the data source. 
	 * 
	 * @param expressionParam 	A <code>DataTypeTO</code> used to define the expression parameters 
	 * 							(choice of data type, data quality, etc.).
	 * @param multiSpeciesTO 	A <code>MultiSpeciesTO</code> storing the parameters of the expression query 
	 * 							related to the species, the organs, the stages. 
	 * @return 					A <code>Collection</code> of <code>TransferObject</code>s, 
	 * 							castable to <code>AffymetrixProbesetTO</code>s, 
	 * 							retrieved thanks to an expression query parameterized using 
	 * 							<code>expressionParam</code> and <code>multiSpeciesTO</code>.
	 */
	public Collection<TransferObject> getProbesetByExpression(DataTypeTO expressionParam,
			MultiSpeciesTO multiSpeciesTO);

	/**
	 * Return a <code>Collection</code> of <code>String</code>s  
	 * corresponding to Affymetrix probeset Ids, 
	 * subset of those passed as a parameter (<code>probesetIds</code>), 
	 * that were not found in the database.
	 * @param probesetIds	a <code>Collection</code> of <code>String</code>s 
	 * 						to be checked for presence in the database
	 * @return 				a <code>Collection</code> of <code>String</code>s that could not be found 
	 * 						in the list of probeset IDs in the database. 
	 * 						An empty <code>Collection</code> 
	 * 						if all IDs were found in the database, 
	 * 						or if <code>probesetIds</code> was empty.
	 */
	public Collection<String> getNonMatchingProbesetIds(Collection<String> probesetIds);
}
