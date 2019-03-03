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
 * <p>
 * To retrieve relations between taxa, or taxonomy leading to requested species, etc,
 * see methods returning an {@code Ontology<Taxon, Integer>} in
 * {@link org.bgee.model.ontology.OntologyService}.
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 14 Mar. 2019
 * @since   Bgee 13, Sept. 2015
 * @see org.bgee.model.ontology.OntologyService
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
     * Retrieve taxa from their NCBI taxon IDs.
     *
     * @param taxonIds  A {@code Collection} of {@code Integer}s that are the IDs of the requested taxa.
     *                  Can be {@code null} or empty if all taxa are requested.
     * @param lca       A {@code boolean} specifying, if {@code true}, to only retrieve
     *                  taxa that are least common ancestors of species in Bgee.
     * @return          A {@code Stream} of the requested {@code Taxon}s.
     */
    public Stream<Taxon> loadTaxa(Collection<Integer> taxonIds, boolean lca) {
        log.entry(taxonIds);
        return log.exit(this.getDaoManager().getTaxonDAO()
                .getTaxa(taxonIds, lca, null).stream()
                .map(TaxonService::mapFromTO));
    }
    /**
     * Retrieve the LCA of the provided species, considering only species in Bgee.
     * <p>
     * The {@code Taxon} returned is guaranteed to be non-{@code null} (in the most extreme case:
     * the LCA of all species in Bgee).
     * 
     * @param speciesIds        A {@code Collection} of {@code Integer}s that are the IDs of 
     *                          the species for which we want to retrieve the LCA.
     *                          If {@code null} or empty, then all species in Bgee are considered
     *                          (leading to return the LCA of all species in Bgee)
     * @return                  A {@code Taxon} that is the least common ancestor
     *                          of the requested species. Only species in Bgee are considered.
     */
    public Taxon loadLeastCommonAncestor(Collection<Integer> speciesIds) {
        log.entry(speciesIds);
        return log.exit(mapFromTO(this.getDaoManager().getTaxonDAO()
                .getLeastCommonAncestor(speciesIds, null)));
    }

    /**
     * Maps a {@code TaxonTO} to an {@code Taxon} instance (Can be passed as a {@code Function}). 
     * 
     * @param taxonTO   The {@code TaxonTO} to be mapped
     * @return          The mapped {@code Taxon}
     */
    private static Taxon mapFromTO(TaxonTO taxonTO) {
        log.entry(taxonTO);
        return log.exit(new Taxon(taxonTO.getId(), taxonTO.getName(), taxonTO.getDescription(),
                taxonTO.getScientificName(), taxonTO.getLevel(), taxonTO.isLca()));
    }
}