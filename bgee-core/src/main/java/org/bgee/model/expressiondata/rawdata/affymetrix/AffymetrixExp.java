package org.bgee.model.expressiondata.rawdata.affymetrix;

import org.bgee.model.expressiondata.rawdata.RawData;

/**
 * Class related to microarray experiment. 
 * Is the container of {@code AffymetrixChip}s.
 * 
 * @author Frederic Bastian
 * @version Bgee 11
 * @see AffymetrixChip
 * @since Bgee 01
 */
public class AffymetrixExp extends RawData
{
	/**
	 * Default constructor. 
	 */
    public AffymetrixExp(String id)
    {
    	super(id);
    }
}
