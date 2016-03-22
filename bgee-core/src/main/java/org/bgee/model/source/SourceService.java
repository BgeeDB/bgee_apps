package org.bgee.model.source;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Service;
import org.bgee.model.ServiceFactory;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.source.SourceDAO.SourceTO;

/**
 * A {@link Service} to obtain {@link Source} objects. Users should use the
 * {@link ServiceFactory} to obtain {@code SourceService}s.
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 13, Mar. 2016
 * @since   Bgee 13, Mar. 2016
 */
public class SourceService extends Service {

    private static final Logger log = LogManager.getLogger(SourceService.class.getName());

    /**
     * 0-arg constructor that will cause this {@code SourceService} to use 
     * the default {@code DAOManager} returned by {@link DAOManager#getDAOManager()}. 
     * 
     * @see #SourceService(DAOManager)
     */
    public SourceService() {
        this(DAOManager.getDAOManager());
    }
    /**
     * @param daoManager    The {@code DAOManager} to be used by this {@code SourceService} 
     *                      to obtain {@code DAO}s.
     * @throws IllegalArgumentException If {@code daoManager} is {@code null}.
     */
    public SourceService(DAOManager daoManager) {
        super(daoManager);
    }
    
    
    /**
     * Retrieve all {@code Source}s.
     * 
     * @return  A {@code List} of {@code Source}s that are sources used in Bgee.
     */
    public List<Source> loadAllSources() {
        log.entry();
        return log.exit(getDaoManager().getSourceDAO().getAllDataSources(null).stream()
                    .map(SourceService::mapFromTO)
                    .collect(Collectors.toList()));
    }
    
    /**
     * Retrieve {@code Source}s to be displayed.
     * 
     * @return  A {@code List} of {@code Source}s that are sources used in Bgee to be displayed.
     */
    public List<Source> loadDisplayableSources() {
        log.entry();
        return log.exit(getDaoManager().getSourceDAO().getDisplayableDataSources(null).stream()
                    .map(SourceService::mapFromTO)
                    .collect(Collectors.toList()));
    }

    /**
     * Maps {@link SourceTO} to a {@link Source}.
     * 
     * @param sourceTO  The {@link SourceTO} to map.
     * @return          The mapped {@link Source}.
     */
    private static Source mapFromTO(SourceTO sourceTO) {
        log.entry(sourceTO);
        if (sourceTO == null) {
            return log.exit(null);
        }
        return log.exit(new Source(sourceTO.getId(), sourceTO.getName(), sourceTO.getDescription(),
                sourceTO.getXRefUrl(), sourceTO.getExperimentUrl(), sourceTO.getEvidenceUrl(),
                sourceTO.getBaseUrl(), sourceTO.getReleaseDate(), sourceTO.getReleaseVersion(),
                sourceTO.isToDisplay(), convertDataStateToDataQuality(sourceTO.getSourceCategory()),
                sourceTO.getDisplayOrder()));
    }
    
    private static SourceCategory convertDataStateToDataQuality(SourceTO.SourceCategory cat) 
            throws IllegalStateException{
        log.entry(cat);
        switch(cat) {
            case NONE: 
                return log.exit(SourceCategory.NONE);
            case GENOMICS:
                return log.exit(SourceCategory.GENOMICS);
            case PROTEOMICS: 
                return log.exit(SourceCategory.PROTEOMICS);
            case IN_SITU: 
                return log.exit(SourceCategory.IN_SITU);
            case AFFYMETRIX: 
                return log.exit(SourceCategory.AFFYMETRIX);
            case EST: 
                return log.exit(SourceCategory.EST);
            case RNA_SEQ: 
                return log.exit(SourceCategory.RNA_SEQ);
            case ONTOLOGY: 
                return log.exit(SourceCategory.ONTOLOGY);
        default: 
            throw log.throwing(new IllegalStateException("Unsupported SourceTO.SourceCategory: " + cat));
        }
    }
}
