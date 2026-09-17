package org.bgee.model.expressiondata.call;



import org.bgee.model.CommonService;
import org.bgee.model.ServiceFactory;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO.GlobalConditionToDirectAncestorTO;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO.GlobalConditionToDirectAncestorTOResultSet;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;


public final class ConditionGraphCacheService extends CommonService{
    private static final Logger log = Logger.getLogger(ConditionGraphCacheService.class.getName());

    // Main cache: speciesId → (graph data)
    private static final Map<Integer, ConditionGraphCache> speciesGraphs = new ConcurrentHashMap<>();

    
    private static final int[] EMPTY_ARRAY = new int[0];

    public ConditionGraphCacheService(ServiceFactory serviceFactory) {
        super(serviceFactory);
    }

    /**
     * Load all species graphs at startup.
     */
    public void loadAllSpeciesGraphs(List<Integer> speciesIds) {
        for (Integer speciesId : speciesIds) {
            log.info("Loading condition graph for species " + speciesId);
            ConditionGraphCache graph = buildConditionGraph(speciesId);
            speciesGraphs.put(speciesId, graph);
            log.info("Species " + speciesId + " graph loaded.");
        }
    }

    /**
     * Retrieve cached graph for a species.
     */
    public static ConditionGraphCache getGraph(int speciesId) {
        return speciesGraphs.get(speciesId);
    }

    /**
     * Retrieve cached graph for a species, building and caching it on-demand if not yet loaded.
     */
    public ConditionGraphCache getOrLoadGraph(int speciesId) {
        return speciesGraphs.computeIfAbsent(speciesId, id -> {
            log.warning("ConditionGraphCache not pre-loaded for species " + id
                    + " — building on-demand.");
            return buildConditionGraph(id);
        });
    }

    /**
     * Build and cache the graph for one species.
     */
    private ConditionGraphCache buildConditionGraph(Integer speciesId) {
        DAOManager manager = this.getDaoManager();
        ConditionDAO conditionDAO = manager.getConditionDAO();

        // Load all parent-child relations
        log.info("start to retrieve relations");
        GlobalConditionToDirectAncestorTOResultSet relations = 
                conditionDAO.getGlobalConditionToDirectAncestor(speciesId);

        // Build adjacency maps (child → parents) and (parent -> children)
        Map<Integer, Set<Integer>> directAncestorsSet = new HashMap<>();
        Map<Integer, Set<Integer>> directDescendantsSet = new HashMap<>();
        for (GlobalConditionToDirectAncestorTO rel : relations.getAllTOs()) {
            directAncestorsSet.computeIfAbsent(rel.getSourceConditionId(), k -> new HashSet<>())
                       .add(rel.getTargetConditionId());
            directDescendantsSet.computeIfAbsent(rel.getTargetConditionId(), k -> new HashSet<>())
            .add(rel.getSourceConditionId());
            directAncestorsSet.computeIfAbsent(rel.getTargetConditionId(), k -> new HashSet<>());
            directDescendantsSet.computeIfAbsent(rel.getSourceConditionId(), k -> new HashSet<>());
        }

        // Convert to primitive arrays for compact storage
        Map<Integer, int[]> directAncestorsMap = new HashMap<>(directAncestorsSet.size());
        for (Entry<Integer, Set<Integer>> e : directAncestorsSet.entrySet()) {
            int[] arr = e.getValue().stream()
                    .mapToInt(Integer::intValue)
                    .toArray();
            directAncestorsMap.put(e.getKey(), arr);
        }
        Map<Integer, int[]> directDescendantsMap = new HashMap<>(directDescendantsSet.size());
        for (Entry<Integer, Set<Integer>> e : directDescendantsSet.entrySet()) {
            int[] arr = e.getValue().stream()
                    .mapToInt(Integer::intValue)
                    .toArray();
            directDescendantsMap.put(e.getKey(), arr);
        }

        // Compute topological order (children before parents)
        int[] topoOrder = computeTopologicalOrder(directAncestorsMap);

        return new ConditionGraphCache(
            Collections.unmodifiableMap(directAncestorsMap),
            Collections.unmodifiableMap(directDescendantsMap),
            topoOrder
        );
    }

    /**
     * Compute topological order (children first).
     */
    private static int[] computeTopologicalOrder(Map<Integer, int[]> parentMap) {
        Map<Integer, Integer> indegree = new HashMap<>();
        for (Map.Entry<Integer, int[]> e : parentMap.entrySet()) {
            indegree.putIfAbsent(e.getKey(), 0);
            for (int parent : e.getValue()) {
                indegree.merge(parent, 1, Integer::sum);
            }
        }

        Deque<Integer> queue = new ArrayDeque<>();
        for (Map.Entry<Integer, Integer> e : indegree.entrySet()) {
            if (e.getValue() == 0) queue.add(e.getKey());
        }

        int[] order = new int[parentMap.size()];
        int orderPos = 0;
        while (!queue.isEmpty()) {
            int node = queue.removeFirst();
            order[orderPos++] = node;
            for (int parent : parentMap.getOrDefault(node, EMPTY_ARRAY)) {
                int deg = indegree.get(parent) - 1;
                indegree.put(parent, deg);
                if (deg == 0) queue.add(parent);
            }
        }

        if (orderPos != parentMap.size()) {
            throw new IllegalStateException("Cycle detected in condition graph!");
        }

        return order;
    }

    /**
     * Simple immutable holder for graph data.
     */
    public static final class ConditionGraphCache {

        private final Map<Integer, int[]> globalCondToDirectAncestors;
        //XXX: Not useful for on-the-fly propagation but allows to easily filter calls
        // as in the gene page. May be removed if the memory footprint is too high as it could
        // be generated from globalCondToParents
        private final Map<Integer, int[]> globalCondToDirectDescendants;
        private final int[] topoOrder;

        public ConditionGraphCache(Map<Integer, int[]> globalCondToDirectAncestors,
                Map<Integer, int[]> globalCondToDirectDescendants, int[] topoOrder) {
            this.globalCondToDirectAncestors = globalCondToDirectAncestors;
            this.globalCondToDirectDescendants = globalCondToDirectDescendants;
            this.topoOrder = topoOrder;
        }

        public Map<Integer, int[]> getGlobalCondToDirectAncestors() {
            return globalCondToDirectAncestors;
        }

        public Map<Integer, int[]> getGlobalCondToDirectDescendants() {
            return globalCondToDirectDescendants;
        }

        public int[] getTopoOrder() {
            return topoOrder;
        }

    }
}
