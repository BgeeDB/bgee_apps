package org.bgee.model.anatdev;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Service;
import org.bgee.model.ServiceFactory;
import org.bgee.model.dao.api.anatdev.StageDAO;
import org.bgee.model.dao.api.anatdev.StageDAO.StageTO;

/**
 * A {@link Service} to obtain {@link DevStage} objects. 
 * Users should use the {@link org.bgee.model.ServiceFactory} to obtain {@code DevStageService}s.
 * 
 * @author  Valentine Rech de Laval
 * @author  Frederic Bastian
 * @author  Philippe Moret
 * @version Bgee 14 Mar. 2019
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
     * @param speciesIds    A {@code Collection} of {@code Integer}s that are IDs of species 
     *                      for which to return the {@code DevStage}s.
     * @param level         An {@code Integer} that is the level of dev. stages 
     *                      for which to return the {@code DevStage}s.
     * @return              A {@code List} of {@code DevStage}s that are the grouping 
     *                      dev. stages for {@code speciesIds} and {@code level}.
     */
    public Set<DevStage> loadGroupingDevStages(Collection<Integer> speciesIds, Integer level) {
        log.entry(speciesIds, level);
        return log.traceExit(getDaoManager().getStageDAO().getStages(
                    speciesIds == null? null: new HashSet<>(speciesIds),
                    true, null, true, level, null)
                .stream()
                .map(DevStageService::mapFromTO)
                .collect(Collectors.toSet()));
    }

    /**
     * Retrieve {@code DevStage}s for the requested species developmental stage IDs,
     * with all dev. stage descriptions loaded. 
     * If a stage in {@code stageIds} does not exists according to the species filtering, 
     * it will not be returned.
     * 
     * @param speciesIds    A {@code Collection} of {@code Integer}s that are the IDs of species 
     *                      to filter developmental stages to retrieve. Can be {@code null} or empty.
     * @param anySpecies    A {@code Boolean} defining, when {@code speciesIds} contains several IDs, 
     *                      whether the stages retrieved should be valid in any 
     *                      of the requested species (if {@code true}), or in all 
     *                      of the requested species (if {@code false} or {@code null}).
     * @param stageIds      A {@code Collection} of {@code String}s that are IDs of developmental
     *                      stages to retrieve. Can be {@code null} or empty.
     * @return              A {@code Stream} of {@code DevStage}s retrieved for the requested parameters,
     *                      with all descriptions loaded.
     */
    public Stream<DevStage> loadDevStages(Collection<Integer> speciesIds, Boolean anySpecies, 
            Collection<String> stageIds) {
        log.entry(speciesIds, anySpecies, stageIds, true);
        return log.traceExit(this.loadDevStages(speciesIds, anySpecies, stageIds, true));
    }
    /**
     * Retrieve {@code DevStage}s for the requested species filtering and developmental stage IDs. 
     * If a stage in {@code stageIds} does not exists according to the species filtering, 
     * it will not be returned.
     * 
     * @param speciesIds        A {@code Collection} of {@code Integer}s that are the IDs of species 
     *                          to filter developmental stages to retrieve. Can be {@code null} or empty.
     * @param anySpecies        A {@code Boolean} defining, when {@code speciesIds} contains several IDs, 
     *                          whether the stages retrieved should be valid in any 
     *                          of the requested species (if {@code true}), or in all 
     *                          of the requested species (if {@code false} or {@code null}).
     * @param stageIds          A {@code Collection} of {@code String}s that are IDs of developmental
     *                          stages to retrieve. Can be {@code null} or empty.
     * @param withDescription   A {@code boolean} defining whether the description of the {@code DevStage}s
     *                          should be retrieved (higher memory usage).
     * @return                  A {@code Stream} of {@code DevStage}s retrieved for the requested parameters.
     */
    public Stream<DevStage> loadDevStages(Collection<Integer> speciesIds, Boolean anySpecies, 
            Collection<String> stageIds, boolean withDescription) {
        log.entry(speciesIds, anySpecies, stageIds, withDescription);
        return log.traceExit(getDaoManager().getStageDAO().getStages(
                    speciesIds, anySpecies, stageIds, null, null,
                    withDescription? null: EnumSet.complementOf(EnumSet.of(StageDAO.Attribute.DESCRIPTION)))
                .stream()
                .map(DevStageService::mapFromTO));
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
            return log.traceExit((DevStage) null);
        }

        return log.traceExit(new DevStage(stageTO.getId(), stageTO.getName(), 
                stageTO.getDescription(), stageTO.getLeftBound(), stageTO.getRightBound(), 
                stageTO.getLevel(), stageTO.isTooGranular(), stageTO.isGroupingStage()));
    }
}