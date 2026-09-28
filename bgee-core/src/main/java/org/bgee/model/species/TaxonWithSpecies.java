package org.bgee.model.species;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A node in a taxon hierarchy tree enriched with species. Each node holds a {@link Taxon},
 * the list of {@link Species} whose {@link Species#getParentTaxonId()} matches this taxon,
 * and child nodes representing descendant taxa.
 * <p>
 * This structure is used to represent the taxonomy leading to a set of species, with species
 * attached at their respective taxonomic levels (typically genus).
 *
 * @author  Frederic Bastian
 * @version Bgee 15, Mar. 2025
 * @since   Bgee 15, Mar. 2025
 */
public class TaxonWithSpecies {

    private final Taxon taxon;
    private final List<Species> species;
    private final List<TaxonWithSpecies> children;

    /**
     * @param taxon    The {@code Taxon} at this node.
     * @param species  The {@code Species} whose parent taxon is this taxon. Can be {@code null}.
     * @param children Child nodes for descendant taxa. Can be {@code null}.
     */
    public TaxonWithSpecies(Taxon taxon, List<Species> species,
            List<TaxonWithSpecies> children) {
        this.taxon = taxon;
        this.species = species == null ? List.of()
                : Collections.unmodifiableList(new ArrayList<>(species));
        this.children = children == null ? List.of()
                : Collections.unmodifiableList(new ArrayList<>(children));
    }

    public Taxon getTaxon() {
        return taxon;
    }

    public List<Species> getSpecies() {
        return species;
    }

    public List<TaxonWithSpecies> getChildren() {
        return children;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TaxonWithSpecies that = (TaxonWithSpecies) o;
        return Objects.equals(taxon, that.taxon)
                && Objects.equals(species, that.species)
                && Objects.equals(children, that.children);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taxon, species, children);
    }
}
