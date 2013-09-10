package org.bgee.model;

import org.bgee.model.anatdev.StageFactory;
import org.bgee.model.anatdev.StageStaticFactory;
import org.bgee.model.dao.api.DAOManager;

/**
 * Parent class of all factories used in Bgee. 
 * Factories are designed to instantiate objects from one specific class. 
 * For instance, the methods of a <code>StageLoader</code> 
 * return a <code>Stage</code>, or a <code>Collection</code> of <code>Stage</code>s.
 * <p>
 * This parent class is responsible for obtaining 
 * a {@link org.bgee.model.dao.api.DAOManager DAOManager}, that will then be used 
 * by each factory to obtain an appropriate <code>DAO</code>. Each factory instance 
 * will use its own DAO instance. 
 * <p> 
 * This parent class also provides the static methods to obtain factories. Some factories 
 * have two implementations, a regular one, using a <code>DAO</code> at each method call, 
 * and a "static" one, pre-loading all the data into memory, and retrieving 
 * the data from this memory at each method call (see for instance 
 * {@link org.bgee.model.anatdev.AnatEntityFactory} and 
 * {@link org.bgee.model.anatdev.AnatEntityStaticFactory}). 
 * <p>
 * This class is responsible for returning the appropriate implementation 
 * (regular or static one), depending on the Bgee parameters, and the availability of 
 * a given static factory (not all factories have a static implementation). 
 * <p>
 * In order to simplify the code, static and regular factories do not implement 
 * a common interface, using an abstract factory, etc. Static factories simply 
 * extend regular factories, and override all their methods. They pre-load their data 
 * during static class initialization.  
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 01
 */
public abstract class Factory 
{
	/**
	 * A <code>DAOManager</code>, used to obtain DAOs.
	 */
	private final DAOManager daoManager;
	
    protected Factory()
    {
    	//get the default DAO Manager
    	this.daoManager = DAOManager.getDAOManager();
    }
    
    /**
     * Return the default <code>DAOManager</code> obtained 
     * at the instantiation of this class. 
     * 
     * @return 	The default <code>DAOManager</code>, used to obtain DAOs
     */
    protected DAOManager getDAOFactory()
    {
    	return this.daoManager;
    }
    
    //*********************************************
    //   STATIC METHODS TO OBTAIN FACTORIES
    //*********************************************
    public static StageFactory getStageFactory() {
    	if (BgeeProperties.getBgeeProperties().useStaticFactories()) {
    		return new StageStaticFactory();
    	}
    	return new StageFactory();
    }
}
