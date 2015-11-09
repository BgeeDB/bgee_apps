package org.bgee.model.gene;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Service;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTO;

/**
 * A {@link Service} to obtain {@link Gene} objects. 
 * Users should use the {@link ServiceFactory} to obtain {@code GeneService}s.
 * 
 * @author Philippe Moret
 * @author Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 13 Nov. 2015
 * @since Bgee 13 Sept. 2015
 */
public class GeneService extends Service {
    
    private static final Logger log = LogManager.getLogger(GeneService.class.getName());
    
    /**
     * 0-arg constructor that will cause this {@code GeneService} to use 
     * the default {@code DAOManager} returned by {@link DAOManager#getDAOManager()}. 
     * 
     * @see #GeneService(DAOManager)
     */
    public GeneService() {
        this(DAOManager.getDAOManager());
    }
    /**
     * @param daoManager    The {@code DAOManager} to be used by this {@code GeneService} 
     *                      to obtain {@code DAO}s.
     * @throws IllegalArgumentException If {@code daoManager} is {@code null}.
     */
    public GeneService(DAOManager daoManager) {
        super(daoManager);
    }

    /**
     * Retrieve {@code Gene}s for a given set of species IDs and a given set of gene IDs.
     * 
     * @param geneIds       A {@code Set} of {@code String}s that are IDs of genes 
     *                      for which to return the {@code Gene}s.
     * @param speciesIds    A {@code Set} of {@code String}s that are IDs of species 
     *                      for which to return the {@code Gene}s.
     * @return              A {@code List} of {@code Gene}s that are the {@code Gene}s 
     *                      for the given set of species IDs and the given set of gene IDs.
     */
    public List<Gene> loadGenesByIdsAndSpeciesIds(Set<String> geneIds, Set<String> speciesIds) {
        log.entry(geneIds, speciesIds);
        
        return log.exit(getDaoManager().getGeneDAO().getGenesBySpeciesIds(speciesIds).stream()
                .filter(e -> geneIds.contains(e.getId()))
                .map(GeneService::mapFromTO)
                .collect(Collectors.toList()));
    }

    /**
     * Maps {@link GeneTO} to a {@link Gene}.
     * 
     * @param geneTO    The {@link GeneTO} to map.
     * @return          The mapped {@link Gene}.
     */
    private static Gene mapFromTO(GeneTO geneTO) {
        log.entry(geneTO);
        if (geneTO == null) {
            return log.exit(null);
        }
        
        return log.exit(new Gene(geneTO.getId(), String.valueOf(geneTO.getSpeciesId())));
    }
}
