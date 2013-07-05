package org.bgee.model;

import org.bgee.model.data.common.DAOFactory;

/**
 * Parent class of all loaders used in Bgee. 
 * A loader is a factory used to instantiate objects from one specific class. 
 * For instance, the public methods of an <code>OrganLoader</code> 
 * return an <code>Organ</code>, or a <code>Collection</code> of <code>Organ</code>s.
 * <p>
 * This class is responsible for obtaining a <code>DAOFactory</code>, 
 * that will be used by subclasses to obtain DAOs, to query data sources. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 01
 */
public abstract class EntityFactory 
{
	/**
	 * A <code>DAOFactory</code>, used to obtain DAOs.
	 */
	private final DAOFactory daoFactory;
	
    public EntityFactory()
    {
    	//get the default DAO Factory
    	this.daoFactory = DAOFactory.getDAOFactory();
    }
    
    /**
     * Return the default <code>DAOFactory</code> obtained at the instantiation of this class, 
     * stored in the <code>#daoFactory</code> attribute. 
     * 
     * @return 	The default <code>DAOFactory</code>, used to obtain DAOs
     * @see #daoFactory
     */
    protected DAOFactory getDAOFactory()
    {
    	return this.daoFactory;
    }
}
