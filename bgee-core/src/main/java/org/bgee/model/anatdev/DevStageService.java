package org.bgee.model.anatdev;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Service;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.anatdev.StageDAO.StageTO;

/**
 * A {@link Service} to obtain {@link DevStage} objects. 
 * Users should use the {@link org.bgee.model.ServiceFactory} to obtain {@code DevStageService}s.
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 13, Nov. 2013
 * @since   Bgee 13, Nov. 2013
 */
public class DevStageService extends Service {

    private final static Logger log = LogManager.getLogger(DevStageService.class.getName());

    /**
     * 0-arg constructor private, because it might be difficult to determine 
     * the {@code Service}s and {@code DAOManager} to use by default, see 
     * {@link #DevStageService(DAOManager)}.
     */
    @SuppressWarnings("unused")
    private DevStageService() {
        this(DAOManager.getDAOManager());
    }
    
    /**
     *
     * @param daoManager                The {@code DAOManager} to be used by this 
     *                                  {@code DevStageService} to obtain {@code DAO}s.
     * @throws IllegalArgumentException If {@code daoManager} is {@code null}.
     */
    public DevStageService(DAOManager daoManager) throws IllegalArgumentException {
        super(daoManager);
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
    public List<DevStage> loadGroupingDevStages(Collection<String> speciesIds, Integer level) {
        log.entry(speciesIds, level);
        return log.exit(getDaoManager().getStageDAO().getStagesBySpeciesIds(
                        Optional.ofNullable(speciesIds).map(e -> new HashSet<>(e)).orElse(new HashSet<>()),
                        true, level)
                .stream()
                .map(DevStageService::mapFromTO)
                .collect(Collectors.toList()));
    }

    /**
     * Retrieve {@code DevStage}s for given dev. stage IDs.
     * 
     * @param stageIds  A {@code Collection} of {@code String}s that are IDs of dev. stages 
     *                  for which to return the {@code DevStage}s.
     * @return          The {@code List} of {@code DevStage}s that are the 
     *                  dev. stages for the given set of stage IDs.
     */
    public List<DevStage> loadDevStagesByIds(Collection<String> stageIds) {
        log.entry(stageIds);
        return log.exit(getDaoManager().getStageDAO().getStagesByIds(
                Optional.ofNullable(stageIds).map(e -> new HashSet<>(e)).orElse(new HashSet<>()))
                .stream()
                .map(DevStageService::mapFromTO)
                .collect(Collectors.toList()));
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