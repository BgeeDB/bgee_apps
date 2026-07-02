package org.bgee.model.species;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Service;
import org.bgee.model.ServiceFactory;
import org.bgee.model.ontology.Ontology;
import org.bgee.model.ontology.OntologyService;

/**
 * A {@link Service} to build taxon hierarchy trees enriched with species.
 * <p>
 * <strong>Relation to {@link TaxonService}:</strong> {@code TaxonService} focuses on retrieving
 * individual {@link Taxon} objects and computing the least common ancestor of species. Taxonomy
 * relations (ancestor/descendant structure) are provided by {@link OntologyService}, which returns
 * {@code Ontology<Taxon, Integer>} graphs. {@code TaxonTreeService} is a separate service because
 * it combines ontology structure with species data to produce a tree-shaped hierarchy
 * ({@link TaxonWithSpecies}) suitable for display or API responses. Keeping this logic in a
 * dedicated service avoids overloading {@code TaxonService} with ontology and species concerns,
 * and keeps {@code OntologyService} focused on graph structures rather than presentation models.
 *
 * @author  Harald Detering
 * @version Bgee 16, Mar. 2026
 * @since   Bgee 16, Mar. 2026
 * @see     TaxonService
 * @see     OntologyService
 */
public class TaxonTreeService extends Service {

    private static final Logger log = LogManager.getLogger(TaxonTreeService.class.getName());

    /**
     * @param serviceFactory The {@code ServiceFactory} to be used to obtain {@code Service}s.
     * @throws IllegalArgumentException If {@code serviceFactory} is {@code null}.
     */
    public TaxonTreeService(ServiceFactory serviceFactory) {
        super(serviceFactory);
    }

    /**
     * Builds a taxon hierarchy tree enriched with species for the given species IDs.
     * Species are loaded from {@link SpeciesService}.
     *
     * @param speciesIds A {@code Collection} of species IDs to include in the tree.
     * @return The root {@code TaxonWithSpecies} of the hierarchy.
     * @see #buildTaxonTreeWithSpecies(Collection, Map)
     */
    public TaxonWithSpecies buildTaxonTreeWithSpecies(Collection<Integer> speciesIds) {
        return buildTaxonTreeWithSpecies(speciesIds, null);
    }

    /**
     * Builds a taxon hierarchy tree enriched with species for the given species IDs.
     * The tree includes only taxa that are least common ancestors of the requested species,
     * with species attached at their respective parent taxon (typically genus).
     *
     * @param speciesIds   A {@code Collection} of species IDs to include in the tree.
     * @param speciesById  A {@code Map} of {@code Integer} as key corresponding to species ID,
     *                     associated to the corresponding {@code Species}. If {@code null},
     *                     species are loaded from {@link SpeciesService}.
     * @return The root {@code TaxonWithSpecies} of the hierarchy.
     */
    public TaxonWithSpecies buildTaxonTreeWithSpecies(Collection<Integer> speciesIds,
            Map<Integer, Species> speciesById) {
        log.traceEntry("{}, {}", speciesIds, speciesById != null ? "provided" : "to load");

        if (speciesIds == null || speciesIds.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("speciesIds cannot be null or empty"));
        }

        Set<Integer> ids = speciesIds.stream().collect(Collectors.toSet());
        Map<Integer, Species> speciesMap = speciesById != null
                ? speciesById
                : this.getServiceFactory().getSpeciesService().loadSpeciesByIds(ids, false).stream()
                        .collect(Collectors.toMap(Species::getId, s -> s));

        OntologyService ontService = this.getServiceFactory().getOntologyService();

        Ontology<Taxon, Integer> taxonOnt = ontService.getTaxonOntologyLeadingToSpecies(
                ids, true, false);

        Map<Integer, List<Species>> speciesByParentTaxonId = speciesMap.values().stream()
                .filter(s -> s.getParentTaxonId() != null)
                .collect(Collectors.groupingBy(Species::getParentTaxonId));

        Map<Integer, List<Taxon>> childrenByParent = new HashMap<>();
        Taxon root = null;
        for (Taxon t : taxonOnt.getElements()) {
            Set<Taxon> ancestors = taxonOnt.getAncestors(t);
            if (ancestors.isEmpty()) {
                root = t;
            } else {
                Taxon parent = ancestors.stream()
                        .max(Comparator.comparingInt(Taxon::getLevel))
                        .get();
                childrenByParent.computeIfAbsent(parent.getId(), k -> new ArrayList<>()).add(t);
            }
        }

        if (root == null) {
            throw new IllegalStateException("No root taxon found in taxonomy ontology");
        }

        return log.traceExit(buildTaxonNode(root, childrenByParent, speciesByParentTaxonId));
    }

    private static TaxonWithSpecies buildTaxonNode(Taxon taxon,
            Map<Integer, List<Taxon>> childrenByParent,
            Map<Integer, List<Species>> speciesByParentTaxonId) {
        List<Species> directSpecies = speciesByParentTaxonId.getOrDefault(
                taxon.getId(), List.of());

        List<TaxonWithSpecies> children = childrenByParent
                .getOrDefault(taxon.getId(), List.of()).stream()
                .sorted(Comparator.comparing(Taxon::getId))
                .map(child -> buildTaxonNode(child, childrenByParent, speciesByParentTaxonId))
                .collect(Collectors.toList());

        return new TaxonWithSpecies(taxon, directSpecies, children);
    }
}
