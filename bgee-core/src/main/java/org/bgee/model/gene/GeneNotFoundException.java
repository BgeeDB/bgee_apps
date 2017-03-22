package org.bgee.model.gene;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A {@code RuntimeException} used when some requested gene IDs were not found in Bgee.
 * The getter {@link #getSpeciesIdToGeneIdsNotFound()} allows to retrieve the IDs
 * of the offending genes. This means that a user can recover from such an {@code Exception},
 * and thus that it should be a checked {@code Exception}. But checked {@code Exception}s
 * are completely broken since Java 8 {@code Stream}s, so we extend {@code RuntimeException} shamelessly.
 *
 * @author Frederic Bastian
 * @version Bgee 14 Mar. 2017
 * @since Bgee 14 Mar. 2017
 */
public class GeneNotFoundException extends RuntimeException {
    private static final long serialVersionUID = -8505814208967898043L;
    
    private static String buildMessage(Map<Integer, Set<String>> speciesIdToGeneIdsNotFound) {
        StringBuilder sb = new StringBuilder();
        sb.append("Some requested gene IDs were not found in Bgee: ")
          .append(System.lineSeparator());
        for (Entry<Integer, Set<String>> speIdToGeneIds: speciesIdToGeneIdsNotFound.entrySet()) {
            sb.append("For species with ID ").append(speIdToGeneIds.getKey())
              .append(" - Gene IDs: ").append(speIdToGeneIds.getValue())
              .append(System.lineSeparator());
        }
        return sb.toString();
    }
    
    private final Map<Integer, Set<String>> speciesIdToGeneIdsNotFound;
    
    public GeneNotFoundException(Map<Integer, Set<String>> speciesIdToGeneIdsNotFound) {
        super(buildMessage(speciesIdToGeneIdsNotFound));
        this.speciesIdToGeneIdsNotFound = Collections.unmodifiableMap(
                speciesIdToGeneIdsNotFound == null? new HashMap<>():
                    speciesIdToGeneIdsNotFound.entrySet().stream()
                    .collect(Collectors.toMap(
                            e -> e.getKey(),
                            e -> Collections.unmodifiableSet(new HashSet<>(e.getValue())))));
    }

    /**
     * @return  A {@code Map} where keys are {@code Integer}s that are NCBI species IDs,
     *          the associated value being a {@code Set} of {@code String}s that are
     *          the requested gene IDs with no correspondences in Bgee.
     */
    public Map<Integer, Set<String>> getSpeciesIdToGeneIdsNotFound() {
        return speciesIdToGeneIdsNotFound;
    }
}
