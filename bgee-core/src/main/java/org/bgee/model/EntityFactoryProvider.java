package org.bgee.model;

/**
 * A factory to obtain factories of <code>Entity</code> 
 * (yes, this class could have been called "EntityFactoryFactory"). 
 * For instance, this class allows to obtain a <code>GeneFactory</code>, 
 * to obtain <code>Gene</code> objects, or a <code>SpeciesFactory</code>, 
 * to obtain <code>Species</code> objects. These factories are obtained 
 * through calls to static methods. 
 * <p>
 * The reason for such a design is that 
 * some factories have two implementations: one always using a DAO 
 * to return the desired objects, another one first retrieving all objects of a type 
 * using a DAO, putting all data in memory when the class is loaded,
 * to allow to return the desired objects from memory without using a DAO. 
 * Such "static" factories are factories that can return only a limited and fixed number 
 * of different entities, for instance, 
 * a {@link org.bgee.model.species.SpeciesLoader SpeciesLoader}.
 * This is useful mostly in a webapp context.
 * <p>
 * This <code>EntityFactoryProvider</code> thus allows to hide to the caller 
 * the specific implementation of a factory that is used. The implementation to use 
 * is determined by the property returned by  
 * {@link org.bgee.model.BgeeProperties#useStaticFactories() 
 * BgeeProperties.useStaticFactories()}.
 * <p>
 * <h3>If static factories are used</h3>
 * When static factories are used (<code>BgeeProperties.useStaticFactories()</code> 
 * returns <code>true</code>), it is recommended to first call the static method 
 * {@link #loadStaticFactories()}. 
 * This allows to control when the static factories load all their data in memory; otherwise, 
 * it is done for each factory when the class is loaded, so most likely in the middle of 
 * some code execution. The <code>loadStaticFactories()</code> can for instance 
 * be called by a <code>StartupListener</code> in a webapp context, so that the data 
 * are loaded when the webapp is loaded, and not at the first time 
 * a client requests the data. 
 * <p>
 * Note that <code>loadStaticFactories()</code> would load the data only if 
 * <code>BgeeProperties.useStaticFactories()</code> returns <code>true</code>, 
 * and only once for a given class loader, so it is safe to call it anytime. 
 * <p>
 * <h3>Additional notes</h3>
 * An <code>abstract factory</code> pattern could have been used here, 
 * as for the DAO factory. But: 
 * <ul>
 * <li>Only a limited amount of factories have a "static" implementation.
 * <li>We do not plan to add other types of factory in the future besides the two existing one 
 * (classic factories using a DAO vs "static" factories); these two first points 
 * make it easy to choose a concrete implementation by a simple <code>if else</code>.
 * <li>It would have added in complexity when trying to acquire a factory: it would have been 
 * necessary for instance to call 
 * <code>EntityFactoryProvider.getEntityFactoryProvider().getSpeciesFactory()</code> 
 * (<code>getEntityFactoryProvider</code> would have been needed to first obtain  
 * the concrete factory), rather than more simply 
 * <code>EntityFactoryProvider.getSpeciesFactory()</code>.
 * </ul>
 * 
 * @author Frederic Bastian
 * @version Bgee 13, Mar 2013
 * @since Bgee 13
 */
public class EntityFactoryProvider 
{
	/**
	 * Private constructor, as all access to this class are made through static methods. 
	 */
    private EntityFactoryProvider() 
    {
    	
    }
    
    /**
     * Triggers the loading of all static factories. 
     * This allows to control when the static factories load their data in memory; 
     * otherwise, it is done for each factory when the class is loaded. 
     * This method should most likely be called by a <code>StartupListener</code>. 
     * <p>
     * It will load the data of the static factories only if 
     * {@link org.bgee.model.BgeeProperties#useStaticFactories() 
     * BgeeProperties.useStaticFactories()} returns <code>true</code>, so it is safe 
     * for a <code>StartupListener</code> to always call this method. 
     */
    public static void loadStaticFactories()
    {
    	if (BgeeProperties.getBgeeProperties().useStaticFactories()) {
    		//simply calling a static factory make it load all its data in memory
    		
    	}
    }
}
