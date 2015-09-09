package org.bgee.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.species.SpeciesService;

/**
 * Factory allowing to obtain {@link Service}s. 
 * <p>
 * <strong>Implementation specifications: </strong> It was chosen to not use 
 * an abstract factory pattern to obtain {@code Service}s, to avoid multiplication 
 * of interfaces and classes, and because such a need was not foreseen at middle term. 
 * When different implementations of a {@code Service} exist, it is the responsibility 
 * of this {@code ServiceFactory} to directly return the appropriate implementation. 
 * At present, different implementations of a {@code Service} are not extending 
 * a common interface, but extending an already-existing implementation. 
 * <p>
 * An example of {@code ServiceFactory} method capable of returning different {@code Service} 
 * implementations could be: 
 * <pre><code>
 * public static GeneService getGeneService() {
 *     if (BgeeProperties.getBgeeProperties().useStaticFactories()) {
 *         return new StaticGeneService(); //class extending GeneService
 *     }
 *     return new GeneService();
 * }
 * </code></pre>
 * <p>
 * At middle term, the different implementations of {@code Service}s that are expected to be created are: 
 * <ul>
 * <li>{@code Service}s directly using {@code DAO}s to retrieve data.
 * <li>{@code Service}s retrieving data from {@code DAO}s and storing them into a cache, 
 * during their static initialization, then using these cached data rather than {@code DAO}s.
 * <ul>
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Sept. 2015
 * @since Bgee 13
 */
public class ServiceFactory {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = LogManager.getLogger(ServiceFactory.class.getName());

    /**
     * @see #getDAOManager()
     */
    private final DAOManager daoManager;
    
    /**
     * 0-arg constructor that will cause this {@code ServiceFactory} to use 
     * the default {@code DAOManager} returned by {@link DAOManager#getDAOManager()}, 
     * to be provided to the {@code Service}s it instantiates. 
     * 
     * @see #SpeciesService(DAOManager)
     */
    public ServiceFactory() {
        this(DAOManager.getDAOManager());
    }
    /**
     * @param daoManager    The {@code DAOManager} to be used by this {@code ServiceFactory},  
     *                      to be provided to the {@code Service}s it instantiates. 
     */
    public ServiceFactory(DAOManager daoManager) {
        this.daoManager = daoManager;
    }
    
    /**
     * @return  A newly instantiated {@code SpeciesService}, using the same {@code DAOManager} 
     *          as the one selected by this {@code ServiceFactory}.
     */
    public SpeciesService getSpeciesFactory() {
        log.entry();
        return log.exit(new SpeciesService(this.daoManager));
    }
}
