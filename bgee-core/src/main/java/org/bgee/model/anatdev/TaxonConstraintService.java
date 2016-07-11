package org.bgee.model.anatdev;

import java.util.Collection;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Service;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.anatdev.TaxonConstraintDAO.TaxonConstraintTO;

/**
 * A {@link Service} to obtain {@link TaxonConstraint} objects. 
 * Users should use the {@link org.bgee.model.ServiceFactory} to obtain {@code TaxonConstraint}s.
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 13, May 2016
 * @since   Bgee 13, May 2016
 */
public class TaxonConstraintService extends Service {
    
    private static final Logger log = LogManager.getLogger(TaxonConstraintService.class.getName());
    
    /**
     * 0-arg constructor that will cause this {@code TaxonConstraintService} to use 
     * the default {@code DAOManager} returned by {@link DAOManager#getDAOManager()}. 
     * 
     * @see #TaxonConstraintService(DAOManager)
     */
    public TaxonConstraintService() {
        this(DAOManager.getDAOManager());
    }
    /**
     * @param daoManager    The {@code DAOManager} to be used by this {@code TaxonConstraintService} 
     *                      to obtain {@code DAO}s.
     * @throws IllegalArgumentException If {@code daoManager} is {@code null}.
     */
    public TaxonConstraintService(DAOManager daoManager) {
        super(daoManager);
    }

    /**
     * Retrieve anatomical entity taxon constraints for a given set of species IDs.
     * 
     * @param speciesIds    A {@code Collection} of {@code String}s that are IDs of species 
     *                      for which to return the {@code TaxonConstraint}s.
     * @return              A {@code Stream} of {@code TaxonConstraint}s that are 
     *                      the {@code TaxonConstraint}s for the given set of species IDs.
     */
    public Stream<TaxonConstraint> loadAnatEntityTaxonConstraintBySpeciesIds(Collection<String> speciesIds) {
        log.entry(speciesIds);
        
        return log.exit(getDaoManager().getTaxonConstraintDAO()
                    .getAnatEntityTaxonConstraints(speciesIds, null).stream()
                    .map(TaxonConstraintService::mapFromTO));
    }
    
    /**
     * Retrieve anatomical entity relation taxon constraints for a given set of species IDs.
     * 
     * @param speciesIds    A {@code Collection} of {@code String}s that are IDs of species 
     *                      for which to return the {@code TaxonConstraint}s.
     * @return              A {@code Stream} of {@code TaxonConstraint}s that are 
     *                      the {@code TaxonConstraint}s for the given set of species IDs.
     */
    public Stream<TaxonConstraint> loadAnatEntityRelationTaxonConstraintBySpeciesIds(
            Collection<String> speciesIds) {
        log.entry(speciesIds);
        
        return log.exit(getDaoManager().getTaxonConstraintDAO()
                    .getAnatEntityRelationTaxonConstraints(speciesIds, null).stream()
                    .map(TaxonConstraintService::mapFromTO));
    }

    /**
     * Retrieve developmental stage taxon constraints for a given set of species IDs.
     * 
     * @param speciesIds    A {@code Collection} of {@code String}s that are IDs of species 
     *                      for which to return the {@code TaxonConstraint}s.
     * @return              A {@code Stream} of {@code TaxonConstraint}s that are 
     *                      the {@code TaxonConstraint}s for the given set of species IDs.
     */
    public Stream<TaxonConstraint> loadDevStageTaxonConstraintBySpeciesIds(Collection<String> speciesIds) {
        log.entry(speciesIds);
        
        return log.exit(getDaoManager().getTaxonConstraintDAO()
                    .getStageTaxonConstraints(speciesIds, null).stream()
                    .map(TaxonConstraintService::mapFromTO));
    }
    
    /**
     * Map {@link TaxonConstraintTO} to a {@link TaxonConstraint}.
     * 
     * @param taxonConstraintTO A {@code TaxonConstraintTO} that is the transfert object to be mapped.
     * @return                  The mapped {@link TaxonConstraint}.
     */
    private static TaxonConstraint mapFromTO(TaxonConstraintTO taxonConstraintTO) {
        log.entry(taxonConstraintTO);
        if (taxonConstraintTO == null) {
            return log.exit(null);
        }

        return log.exit(new TaxonConstraint(
                taxonConstraintTO.getEntityId(), taxonConstraintTO.getSpeciesId()));
    }
}
