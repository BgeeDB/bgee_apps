package org.bgee.model.source;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Service;
import org.bgee.model.ServiceFactory;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.source.SourceDAO.SourceTO;
import org.bgee.model.dao.api.source.SourceToSpeciesDAO.SourceToSpeciesTO;
import org.bgee.model.dao.api.source.SourceToSpeciesDAO.SourceToSpeciesTO.InfoType;
import org.bgee.model.expressiondata.baseelements.DataType;

/**
 * A {@link Service} to obtain {@link Source} objects. Users should use the
 * {@link ServiceFactory} to obtain {@code SourceService}s.
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 13, July 2016
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
     * @param withSpeciesInfo   A {@code boolean}s defining whether species information of 
     *                          the source are retrieved or not.
     * @return                  A {@code List} of {@code Source}s that are sources used in Bgee.
     */
    public List<Source> loadAllSources(boolean withSpeciesInfo) {
        log.entry(withSpeciesInfo);
        System.err.println(withSpeciesInfo);
        List<Source> sources = getDaoManager().getSourceDAO().getAllDataSources(null).stream()
                .map(SourceService::mapFromTO)
                .collect(Collectors.toList());
        if (withSpeciesInfo) {
            sources = this.loadSpeciesInfo(sources);
        }
        return log.exit(sources);
    }
    
    /**
     * Retrieve {@code Source}s to be displayed.
     * 
     * @param withSpeciesInfo   A {@code boolean}s defining whether species information of 
     *                          the source are retrieved or not.
     * @return                  A {@code List} of {@code Source}s that are sources used in Bgee 
     *                          to be displayed.
     */
    public List<Source> loadDisplayableSources(boolean withSpeciesInfo) {
        log.entry(withSpeciesInfo);
        
        List<Source> sources = getDaoManager().getSourceDAO().getDisplayableDataSources(null).stream()
                .map(SourceService::mapFromTO)
                .collect(Collectors.toList());
        if (withSpeciesInfo) {
            sources = this.loadSpeciesInfo(sources);
        }
        
        return log.exit(sources);
    }

    /**
     * Retrieve {@code Source}s with species information.
     * 
     * @param   A {@code List} of {@code Source}s that are sources to be completed.
     * @return  A {@code List} of {@code Source}s that are sources used in Bgee to be displayed.
     */
    private List<Source> loadSpeciesInfo(List<Source> sources) {
        log.entry(sources);
        
        final List<SourceToSpeciesTO> sourceToSpeciesTOs = getDaoManager().getSourceToSpeciesDAO()
                .getAllSourceToSpecies(null).stream()
                .collect(Collectors.toList());
        
        List<Source> completedSources = new ArrayList<>();

        for (Source source : sources) {
            Map<String, Set<DataType>> forData = getDataTypesBySpecies(
                    sourceToSpeciesTOs, source.getId(), InfoType.DATA);
            Map<String, Set<DataType>> forAnnotation = getDataTypesBySpecies(
                    sourceToSpeciesTOs, source.getId(), InfoType.ANNOTATION);

            completedSources.add(new Source(source.getId(), source.getName(), source.getDescription(),
                    source.getxRefUrl(), source.getExperimentUrl(), source.getEvidenceUrl(),
                    source.getBaseUrl(), source.getReleaseDate(), source.getReleaseVersion(),
                    source.getToDisplay(), source.getCategory(), source.getDisplayOrder(),
                    forData.isEmpty() ? null : forData, forAnnotation.isEmpty() ? null : forAnnotation));
        }
        
        return log.exit(completedSources);
    }
    
    /** 
     * Retrieve data types by species from {@code SourceToSpeciesTO}.
     * 
     * @param sourceToSpeciesTOs    A {@code List} of {@code SourceToSpeciesTO}s that are sources 
     *                              to species to be grouped.
     * @param sourceId              A {@code String} that is the source ID for which to return
     *                              data types by species.
     * @param infoType              An {@code InfoType} that is the information type for which
     *                              to return data types by species.
     * @return                      A {@code Map} where keys are {@code String}s corresponding to 
     *                              species IDs, the associated values being a {@code Set} of 
     *                              {@code DataType}s corresponding to data types of {@code infoType}
     *                              data of the provided {@code sourceId}.
     */
    private Map<String, Set<DataType>> getDataTypesBySpecies(
            final List<SourceToSpeciesTO> sourceToSpeciesTOs, String sourceId, InfoType infoType) {
        log.entry(sourceToSpeciesTOs, sourceId, infoType);
        Map<String, Set<DataType>> map = sourceToSpeciesTOs.stream()
            .filter(to -> to.getDataSourceId().equals(sourceId))
            .filter(to -> to.getInfoType().equals(infoType))
            .collect(Collectors.toMap(to -> to.getSpeciesId(), 
                to -> new HashSet<DataType>(Arrays.asList(convertDaoDataTypeToDataType(to.getDataType()))), 
                (v1, v2) -> {
                    Set<DataType> newSet = new HashSet<>(v1);
                    newSet.addAll(v2);
                    return newSet;
                }));
        return log.exit(map);
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
    
    private static DataType convertDaoDataTypeToDataType(SourceToSpeciesTO.DataType dt) {
        log.entry(dt);
        switch(dt) {
            case AFFYMETRIX: 
                return log.exit(DataType.AFFYMETRIX);
            case EST:
                return log.exit(DataType.EST);
            case IN_SITU: 
                return log.exit(DataType.IN_SITU);
            case RNA_SEQ: 
                return log.exit(DataType.RNA_SEQ);
        default: 
            throw log.throwing(new IllegalStateException("Unsupported SourceToSpeciesTO.DataType: " + dt));
        }
    }
}
