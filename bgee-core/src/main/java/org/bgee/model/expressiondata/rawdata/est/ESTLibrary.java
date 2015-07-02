package org.bgee.model.expressiondata.rawdata.est;

import org.bgee.model.expressiondata.rawdata.DataAnnotated;
import org.bgee.model.expressiondata.rawdata.RawDataAnnotated;

/**
 * Class related to EST libraries. 
 * Is the container of {@code EST}s. 
 * Is mapped to anatomical and developmental ontologies 
 * (child class of {@code RawDataAnnotated}).
 * 
 * @author Frederic Bastian
 * @version Bgee 11
 * @see EST
 * @since Bgee 01
 */
public class ESTLibrary extends RawDataAnnotated implements DataAnnotated
{
	/**
     * Default constructor.
     */
	public ESTLibrary()
	{//TODO
		super(null);
	}
}
