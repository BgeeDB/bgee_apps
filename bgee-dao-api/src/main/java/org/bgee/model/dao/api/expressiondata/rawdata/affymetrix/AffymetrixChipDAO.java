package model.data.common.expressionData.affymetrixData;

/**
 * An <code>interface</code> that must be implemented by all <code>DAO</code>s 
 * related to Affymetrix chips; 
 * for instance, to retrieve Affymetrix chips from a data source, 
 * or to update Affymetrix chips into a data source.
 *  
 * The communication between the DAO and the <code>model</code> layer 
 * is achieved through the use of <code>TransferObject</code>s  
 * (in that case, <code>AffymetrixChipTO</code>s).
 * 
 * @author Frederic Bastian
 * @version Bgee 12
 * @see AffymetrixChipTO
 * @see model.data.sql.mysql.expressionData.affymetrixData.MysqlAffymetrixChipDAO
 * @since Bgee 01
 *
 */
public interface AffymetrixChipDAO 
{
	/**
	 * Retrieve from a data source a <code>AffymetrixChipTO</code>,  
	 * encapsulating the data related to an Affymetrix chip, 
	 * using a chip ID (<code>bgeeAffymetrixChipId</code>).  
	 * 
	 * @param bgeeAffymetrixChipId	 	A <code>String</code> representing the ID in the Bgee database 
	 * 									of the Affymetrix chip that needs to be retrieved 
	 * 									from the data source. 
	 * @return	A <code>AffymetrixChipTO</code>, encapsulating all the data 
	 * 			related to the Affymetrix chip retrieved from the data source. 
	 */
	public AffymetrixChipTO getAffymetrixChipById(String bgeeAffymetrixChipId);
}
