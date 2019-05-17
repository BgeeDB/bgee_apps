package org.bgee.model.anatdev.multispemapping;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Service;
import org.bgee.model.ServiceFactory;
import org.bgee.model.dao.api.anatdev.mapping.StageGroupingDAO.GroupToStageTO;

public class DevStageSimilarityService extends Service {
    private final static Logger log = LogManager.getLogger(DevStageSimilarityService.class.getName());

    /**
     * @param serviceFactory            The {@code ServiceFactory} to be used to obtain {@code Service}s 
     *                                  and {@code DAOManager}.
     * @throws IllegalArgumentException If {@code serviceFactory} is {@code null}.
     */
    public DevStageSimilarityService(ServiceFactory serviceFactory) {
        super(serviceFactory);
    }

    /**
     * Load developmental stage similarities from provided {@code taxonId} and {@code speciesIds}.
     * 
     * @param taxonId       An {@code Integer} that is the NCBI ID of the taxon for which the similarity 
     *                      annotations should be valid, including all its ancestral taxa.
     * @param speciesIds    A {@code Set} of {@code Integer}s that are the IDs of the species
     *                      for which the similarity annotations should be restricted.
     *                      If empty or {@code null} all available species are used.
     * @return              The {@code Set} of {@link DevStageSimilarity} that are dev. stage 
     *                      similarities from provided {@code taxonId} and {@code speciesIds}.
     */

    public Set<DevStageSimilarity> loadDevStageSimilarities(Integer taxonId, Set<Integer> speciesIds) {
        log.entry(taxonId, speciesIds);
        return log.exit(this.getDaoManager().getStageGroupingDAO().getGroupToStage(
                taxonId, speciesIds == null? null: new HashSet<>(speciesIds)).stream()
              .collect(Collectors.groupingBy(GroupToStageTO::getGroupId)) // group by groupId
                   .entrySet().stream()
                   .map(e -> new DevStageSimilarity(e.getKey(),              // map to DevStageSimilarity
                           e.getValue().stream()
                               .map(GroupToStageTO::getStageId)
                               .collect(Collectors.toSet())))
                   .collect(Collectors.toSet()));
     }
}
