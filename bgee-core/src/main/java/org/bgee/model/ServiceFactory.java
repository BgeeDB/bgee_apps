package org.bgee.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.anatdev.AnatEntityService;
import org.bgee.model.anatdev.DevStageService;
import org.bgee.model.anatdev.TaxonConstraintService;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.expressiondata.CallService;
import org.bgee.model.file.DownloadFileService;
import org.bgee.model.file.SpeciesDataGroupService;
import org.bgee.model.gene.GeneService;
import org.bgee.model.keyword.KeywordService;
import org.bgee.model.ontology.OntologyService;
import org.bgee.model.source.SourceService;
import org.bgee.model.species.SpeciesService;
import org.bgee.model.species.TaxonService;

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
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 13, May 2016
 * @since   Bgee 13
 */
//XXX: should we put all Services in a same package, so that the constructors are protected 
//and can only be obtained through the ServiceFactory?
//XXX: similarly, should we use protected constructors for all classes obtained through a Service, 
//so that they can be obtained only through these Services? Obviously, we can't do both...
public class ServiceFactory implements AutoCloseable {
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
     * to be used to the {@code Service}s it instantiates. 
     * 
     * @see #SpeciesService(DAOManager)
     */
    public ServiceFactory() {
        this(DAOManager.getDAOManager());
    }
    /**
     * @param daoManager    The {@code DAOManager} to be used by this {@code ServiceFactory},  
     *                      to be provided to {@code Service}s it instantiates. 
     * @throws IllegalArgumentException If {@code daoManager} is {@code null} or closed.
     */
    public ServiceFactory(DAOManager daoManager) throws IllegalArgumentException {
        log.entry(daoManager);
        if (daoManager == null || daoManager.isClosed()) {
            throw log.throwing(new IllegalArgumentException("Invalid DAOManager"));
        }
        this.daoManager = daoManager;
        log.exit();
    }
    
    /**
     * @return  A newly instantiated {@code SpeciesService}
     */
    public SpeciesService getSpeciesService() {
        log.entry();
        return log.exit(new SpeciesService(this));
    }

    /**
     * @return  A newly instantiated {@code GeneService}
     */
    public GeneService getGeneService() {
        log.entry();
        return log.exit(new GeneService(this));
    }

    /**
     * @return  A newly instantiated {@code DevStageService}
     */
    public DevStageService getDevStageService() {
        log.entry();
        return log.exit(new DevStageService(this));
    }

    /**
     * @return A newly instantiated {@code DownloadFileService}
     */
    public DownloadFileService getDownloadFileService() {
        log.entry();
        return log.exit(new DownloadFileService(this));
    }

    /**
     * @return A newly instantiated {@code SpeciesDataGroupService}
     */
    public SpeciesDataGroupService getSpeciesDataGroupService() {
        log.entry();
        return log.exit(new SpeciesDataGroupService(this));
    }
    
    /**
     * @return A newly instantiated {@code KeywordService}
     */
    public KeywordService getKeywordService() {
    	log.entry();
    	return log.exit(new KeywordService(this));
    }
    
    /**
     * @return A newly instantiated {@code CallService}
     */
    public CallService getCallService() {
        log.entry();
        return log.exit(new CallService(this));
    }
    
    /**
     * @return A newly instantiated {@code AnatEntityService}
     */
    public AnatEntityService getAnatEntityService() {
        log.entry();
        return log.exit(new AnatEntityService(this));
    }
    
    /**
     * @return A newly instantiated {@code OntologyService}
     */
    public OntologyService getOntologyService() {
        log.entry();
        return log.exit(new OntologyService(this));
    }
    
    /**
     * @return  A newly instantiated {@code SourceService}
     */
    public SourceService getSourceService() {
        log.entry();
        return log.exit(new SourceService(this));
    }
    
    /**
     * @return  A newly instantiated {@code TaxonConstraintService}
     */
    public TaxonConstraintService getTaxonConstraintService() {
        log.entry();
        return log.exit(new TaxonConstraintService(this));
    }
    
    /**
     * @return  A newly instantiated {@code TaxonService}
     */
    public TaxonService getTaxonService() {
        log.entry();
        return log.exit(new TaxonService(this));
    }

    /**
     * @return  The {@code DAOManager} used by this {@code ServiceFactory} to instantiate services.
     */
    public DAOManager getDAOManager() {
        return this.daoManager;
    }
    
    /**
     * Release all resources hold by this {@code ServiceFactory} (notably releasing 
     * the {@link DAOManager} used).
     */
    @Override
    public void close() {
        log.entry();
        this.daoManager.close();
        log.exit();
    }
}
