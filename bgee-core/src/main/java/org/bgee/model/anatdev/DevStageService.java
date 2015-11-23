package org.bgee.model.anatdev;

import java.util.List;
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
     * Retrieve grouping {@code DevStage}s for a given set of species IDs.
     * 
     * @param speciesIds    A {@code Set} of {@code String}s that are IDs of species 
     *                      for which to return the {@code DevStage}s.
     * @return              A {@code List} of {@code DevStage}s that are the grouping {@code DevStage}s 
     *                      for the given set of species IDs and the given level.
     */
    public List<DevStage> loadGroupingDevStages(Set<String> speciesIds) {
        log.entry(speciesIds);

        return log.exit(getDaoManager().getStageDAO().getStagesBySpeciesIds(speciesIds, true).stream()
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
