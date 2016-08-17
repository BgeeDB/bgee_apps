package org.bgee.model.anatdev;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Service;
import org.bgee.model.ServiceFactory;
import org.bgee.model.dao.api.anatdev.StageDAO.StageTO;
import org.bgee.model.dao.api.anatdev.mapping.StageGroupingDAO.GroupToStageTO;

/**
 * A {@link Service} to obtain {@link DevStage} objects. 
 * Users should use the {@link org.bgee.model.ServiceFactory} to obtain {@code DevStageService}s.
 * 
 * @author  Valentine Rech de Laval
 * @author  Frederic Bastian
 * @author  Philippe Moret
 * @version Bgee 13, Aug. 2016
 * @since   Bgee 13, Nov. 2015
 */
public class DevStageService extends Service {

    private final static Logger log = LogManager.getLogger(DevStageService.class.getName());

    /**
     * @param serviceFactory            The {@code ServiceFactory} to be used to obtain {@code Service}s 
     *                                  and {@code DAOManager}.
     * @throws IllegalArgumentException If {@code serviceFactory} is {@code null}.
     */
    public DevStageService(ServiceFactory serviceFactory) {
        super(serviceFactory);
    }

    /**
     * Retrieve grouping {@code DevStage}s for the given species IDs and level.
     * 
     * @param speciesIds    A {@code Collection} of {@code String}s that are IDs of species 
     *                      for which to return the {@code DevStage}s.
     * @param level         An {@code Integer} that is the level of dev. stages 
     *                      for which to return the {@code DevStage}s.
     * @return              A {@code List} of {@code DevStage}s that are the grouping 
     *                      dev. stages for {@code speciesIds} and {@code level}.
     */
    public Set<DevStage> loadGroupingDevStages(Collection<String> speciesIds, Integer level) {
        log.entry(speciesIds, level);
        return log.exit(getDaoManager().getStageDAO().getStages(
                    speciesIds == null? null: new HashSet<>(speciesIds),
                    true, null, true, level, null)
                .stream()
                .map(DevStageService::mapFromTO)
                .collect(Collectors.toSet()));
    }

    /**
     * Retrieve {@code DevStage}s for given dev. stage IDs.
     * 
     * @param stageIds  A {@code Collection} of {@code String}s that are IDs of dev. stages 
     *                  for which to return the {@code DevStage}s.
     * @return          A {@code Stream} of {@code DevStage}s that are the 
     *                  dev. stages for the given set of stage IDs.
     */
    //TODO: javadoc/method name consistency (see AnatEntityService)/parameter order
    public Stream<DevStage> loadDevStages(Collection<String> speciesIds, Boolean anySpecies, 
            Collection<String> stageIds) {
        log.entry(speciesIds, anySpecies, stageIds);
        return log.exit(getDaoManager().getStageDAO().getStages(
                    speciesIds == null? null: new HashSet<>(speciesIds), 
                    anySpecies, 
                    stageIds == null? null: new HashSet<>(stageIds), 
                    null, null, null)
                .stream()
                .map(DevStageService::mapFromTO));
    }

    /**
     * Load developmental stage similarities from provided {@code taxonId} and {@code speciesIds}.
     * 
     * @param taxonId       A {@code String} that is the NCBI ID of the taxon for which the similarity 
     *                      annotations should be valid, including all its ancestral taxa.
     * @param speciesIds    A {@code Set} of IDs of the species for which the similarity 
     *                      annotations should be restricted.
     *                      If empty or {@code null} all available species are used.
     * @return              The {@code Set} of {@link DevStageSimilarity} that are dev. stage 
     *                      similarities from provided {@code taxonId} and {@code speciesIds}.
     */
    public Set<DevStageSimilarity> loadDevStageSimilarities(String taxonId, Set<String> speciesIds) {
       log.entry(taxonId, speciesIds);
       return log.exit(this.getDaoManager().getStageGroupingDAO().getGroupToStage(taxonId, speciesIds).stream()
             .collect(Collectors.groupingBy(GroupToStageTO::getGroupId)) // group by groupId
                  .entrySet().stream()
                  .map(e -> new DevStageSimilarity(e.getKey(),              // map to DevStageSimilarity
                          e.getValue().stream()
                              .map(GroupToStageTO::getStageId)
                              .collect(Collectors.toSet())))
                  .collect(Collectors.toSet()));
    }

    /**
     * Maps {@link StageTO} to a {@link DevStage}.
     * 
     * @param stageTO   The {@link StageTO} to map.
     * @return          The mapped {@link DevStage}.
     */
    private static DevStage mapFromTO(StageTO stageTO) {
        log.entry(stageTO);
        if (stageTO == null) {
            return log.exit(null);
        }

        return log.exit(new DevStage(stageTO.getId(), stageTO.getName(), 
                stageTO.getDescription(), stageTO.getLeftBound(), stageTO.getRightBound(), 
                stageTO.getLevel(), stageTO.isTooGranular(), stageTO.isGroupingStage()));
    }


}
