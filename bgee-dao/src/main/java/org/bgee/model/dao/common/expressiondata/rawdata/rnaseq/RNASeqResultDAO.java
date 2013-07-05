package org.bgee.model.dao.common.expressiondata.rawdata.rnaseq;

import java.util.Collection;

import model.data.common.TransferObject;
import model.data.common.anatomy.MultiSpeciesTO;
import model.data.common.expressionData.DataTypeTO;

/**
 * An <code>interface</code> that must be implemented by all <code>DAO</code>s 
 * related to RNA-Seq results; 
 * for instance, to retrieve RNA-Seq results from a data source, 
 * or to update RNA-Seq results into a data source.
 *  
 * The communication between the DAO and the <code>model</code> layer 
 * is achieved through the use of <code>TransferObject</code>s  
 * (in that case, <code>RNASeqResultTO</code>s).
 * 
 * @author Frederic Bastian
 * @version Bgee 12
 * @see RNASeqResultTO
 * @see model.data.sql.mysql.expressionData.rnaSeqData.MysqlRnaSeqResultDAO
 * @since Bgee 12
 *
 */
public interface RNASeqResultDAO 
{
	/**
	 * Obtain a <code>Collection</code> of <code>TransferObject</code>s, 
	 * castable to <code>RNASeqResultTO</code>s, 
	 * from an expression query to the database. 
	 * 
	 * @param expressionParam 	A <code>DataTypeTO</code> used to define the expression parameters 
	 * 							(choice of data type, data quality, etc.).
	 * @param multiSpeciesTO 	A <code>MultiSpeciesTO</code> storing the parameters of the expression query 
	 * 							related to the species, the organs, the stages. 
	 * @return 					A <code>Collection</code> of <code>TransferObject</code>s, 
	 * 							castable to <code>RNASeqResultTO</code>s, 
	 * 							retrieved thanks to an expression query parameterized using 
	 * 							<code>expressionParam</code> and <code>multiSpeciesTO</code>.
	 */
	public Collection<TransferObject> getRnaSeqResultByExpression(DataTypeTO expressionParam,
			MultiSpeciesTO multiSpeciesTO);
}
