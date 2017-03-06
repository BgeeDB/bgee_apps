package org.bgee.model.species;


import java.util.Collection;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Service;
import org.bgee.model.ServiceFactory;
import org.bgee.model.dao.api.species.TaxonDAO.TaxonTO;

/**
 * A {@link Service} to obtain {@link Taxon} objects. 
 * Users should use the {@link ServiceFactory} to obtain {@code TaxonService}.
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 13, Nov. 2016
 * @since   Bgee 13, Sept. 2015
 */
public class TaxonService extends Service {

    private static final Logger log = LogManager.getLogger(TaxonService.class.getName());

    /**
     * @param serviceFactory            The {@code ServiceFactory} to be used to obtain {@code Service}s 
     *                                  and {@code DAOManager}.
     * @throws IllegalArgumentException If {@code serviceFactory} is {@code null}.
     */
    public TaxonService(ServiceFactory serviceFactory) {
        super(serviceFactory);
    }
    
    /**
     * Retrieve {@code Taxon}s that are LCA of the provided species.
     * If {@code commonAncestor} is {@code true}, all ancestors of the LCA will also be retrieved.
     * 
     * @param speciesIds        A {@code Collection} of {@code Integer}s that are the IDs of species 
     *                          to filter taxa to retrieve. Can be {@code null} or empty.
     * @param commonAncestor    A {@code boolean} defining whether the entities retrieved
     *                          should be common ancestor.
     * @return                  A {@code Stream} of {@code Taxon}s retrieved for
     *                          the requested parameters.
     */
    // FIXME: I'm not sure of the interpretation of the boolean commonAncestor
    // on the shitty figure of the issue 125
    public Stream<Taxon> loadTaxa(Collection<Integer> speciesIds, boolean includeAncestors) {
        log.entry(speciesIds, includeAncestors);
        return log.exit(this.getDaoManager().getTaxonDAO()
                .getLeastCommonAncestor(speciesIds, includeAncestors).stream()
                .map(TaxonService::mapFromTO));
    }
    
    /**
     * Retrieve {@code Taxon}s that are either least common ancestor
     * or parent taxon of species in data source.
     * 
     * @return  A {@code Stream} of {@code Taxon}s retrieved for the requested parameters.
     */
    public Stream<Taxon> loadAllLeastCommonAncestorAndParentTaxa() {
        return log.exit(this.getDaoManager().getTaxonDAO()
                .getAllLeastCommonAncestorAndParentTaxa(null).stream()
                .map(TaxonService::mapFromTO));
    }

    /**
     * Maps a {@code TaxonTO} to an {@code Taxon} instance (Can be passed as a {@code Function}). 
     * 
     * @param taxonTO   The {@code TaxonTO} to be mapped
     * @return          The mapped {@code Taxon}
     */
    private static Taxon mapFromTO(TaxonTO taxonTO) {
        log.entry(taxonTO);
        return log.exit(new Taxon(taxonTO.getId(), taxonTO.getName(), taxonTO.getDescription()));
    }
}
