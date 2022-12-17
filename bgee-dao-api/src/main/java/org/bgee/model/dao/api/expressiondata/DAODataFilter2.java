package org.bgee.model.dao.api.expressiondata;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class DAODataFilter2 {
    private final static Logger log = LogManager.getLogger(DAODataFilter2.class.getName());

    private final Set<Integer> speciesIds;
    private final Set<Integer> geneIds;
    private final Set<Integer> conditionIds;

    /**
     * @param speciesIds    A {@code Collection} of {@code Integer}s that are the IDs
     *                      of the species for which raw data should be retrieved.
     *                      The species present in this {@code Collection} should not have any related
     *                      genes nor conditions in {@code geneIds} and {@code conditionIds}.
     * @param geneIds       A {@code Collection} of {@code Integer}s that are the Bgee internal IDs
     *                      of the genes for which raw data should be retrieved.
     *                      Genes present in this {@code Collection} should not have
     *                      their related species in {@code speciesIds}.
     * @param conditionIds  A {@code Collection} of {@code Integer}s that are the IDs
     *                      of the conditions for which raw data should be retrieved.
     *                      They can be global condition IDs, or raw data condition IDs,
     *                      depending on the query used. Conditions present in this {@code Collection}
     *                      should not have their related species in {@code speciesIds}.
     * @throws IllegalArgumentException
     */
    protected DAODataFilter2(Collection<Integer> speciesIds, Collection<Integer> geneIds,
            Collection<Integer> conditionIds) throws IllegalArgumentException {
        log.traceEntry("{}, {}, {}", speciesIds, geneIds, conditionIds);

        this.speciesIds = Collections.unmodifiableSet(
                speciesIds == null? new HashSet<>(): new HashSet<>(speciesIds));
        this.geneIds = Collections.unmodifiableSet(
                geneIds == null? new HashSet<>(): new HashSet<>(geneIds));
        this.conditionIds = Collections.unmodifiableSet(
                conditionIds == null? new HashSet<>(): new HashSet<>(conditionIds));

        if (this.speciesIds.stream().anyMatch(id -> id == null || id < 1)) {
            throw log.throwing(new IllegalArgumentException("No species ID can be null or less than 1"));
        }
        if (this.geneIds.stream().anyMatch(id -> id == null || id < 1)) {
            throw log.throwing(new IllegalArgumentException("No gene ID can be null or less than 1"));
        }
        if (this.conditionIds.stream().anyMatch(id -> id == null || id < 1)) {
            throw log.throwing(new IllegalArgumentException("No condition ID can be null or less than 1"));
        }

        log.traceExit();
    }

    /**
     * @return  An unmodifiable {@code Set} of {@code Integer}s containing the IDs of species used
     *          to filter expression queries. Can be empty. The species present
     *          in this {@code Set} should not have any related genes nor conditions
     *          in {@link #getGeneIds()} and {@link #getConditionIds()}.
     */
    public Set<Integer> getSpeciesIds() {
        return speciesIds;
    }
    /**
     * @return  An unmodifiable {@code Set} of {@code Integer}s containing the IDs of genes used
     *          to filter expression queries. Can be empty. Genes present in this {@code Set}
     *          should not have their related species in {@link #getSpeciesIds()}.
     */
    public Set<Integer> getGeneIds() {
        return geneIds;
    }
    /**
     * @return  An unmodifiable {@code Set} of {@code Integer}s containing the IDs of conditions used
     *          to filter expression queries. They can be global condition IDs, or raw data condition IDs,
     *          depending on the query used. Can be empty. Conditions present in this {@code Set}
     *          should not have their related species in {@link #getSpeciesIds()}.
     */
    public Set<Integer> getConditionIds() {
        return conditionIds;
    }

    @Override
    public int hashCode() {
        return Objects.hash(conditionIds, geneIds, speciesIds);
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DAODataFilter2 other = (DAODataFilter2) obj;
        return Objects.equals(conditionIds, other.conditionIds)
                && Objects.equals(geneIds, other.geneIds)
                && Objects.equals(speciesIds, other.speciesIds);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DAODataFilter2 [")
               .append("speciesIds=").append(speciesIds)
               .append(", geneIds=").append(geneIds)
               .append(", conditionIds=").append(conditionIds)
               .append("]");
        return builder.toString();
    }
}
