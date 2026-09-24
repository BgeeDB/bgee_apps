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

        //Dense view of the same graph, used by the on-the-fly propagation. The index of
        //a condition is its position in topoOrder, so the index of a parent is always greater
        //than the index of its children: walking the indexes in ascending order is
        //a topological walk, and comparing two indexes tells which one comes first without
        //any lookup. Holding the relations as int[] indexed by index, rather than as
        //Map<Integer, int[]>, is what allows the propagation to run without boxing a single
        //condition ID on its hot path.
        private final Map<Integer, Integer> condIdToIndex;
        private final int[][] directAncestorIndexes;
        private final int[][] directDescendantIndexes;

        public ConditionGraphCache(Map<Integer, int[]> globalCondToDirectAncestors,
                Map<Integer, int[]> globalCondToDirectDescendants, int[] topoOrder) {
            this.globalCondToDirectAncestors = globalCondToDirectAncestors;
            this.globalCondToDirectDescendants = globalCondToDirectDescendants;
            this.topoOrder = topoOrder;

            this.condIdToIndex = new HashMap<>(topoOrder.length * 2);
            for (int i = 0; i < topoOrder.length; i++) {
                this.condIdToIndex.put(topoOrder[i], i);
            }
            this.directAncestorIndexes = toIndexes(globalCondToDirectAncestors, this.condIdToIndex,
                    topoOrder);
            this.directDescendantIndexes = toIndexes(globalCondToDirectDescendants,
                    this.condIdToIndex, topoOrder);
        }
        /**
         * Convert a {@code Map} of relations between condition IDs into an array of relations
         * between condition indexes. A condition absent from {@code relations}, or a related
         * condition absent from {@code condIdToIndex}, yields no relation: such a condition
         * is not part of the topological order and is therefore never reached
         * by the propagation.
         */
        private static int[][] toIndexes(Map<Integer, int[]> relations,
                Map<Integer, Integer> condIdToIndex, int[] topoOrder) {
            int[][] indexes = new int[topoOrder.length][];
            for (int i = 0; i < topoOrder.length; i++) {
                int[] relatedCondIds = relations.get(topoOrder[i]);
                if (relatedCondIds == null || relatedCondIds.length == 0) {
                    indexes[i] = EMPTY_ARRAY;
                    continue;
                }
                int[] relatedIndexes = new int[relatedCondIds.length];
                int count = 0;
                for (int relatedCondId: relatedCondIds) {
                    Integer relatedIndex = condIdToIndex.get(relatedCondId);
                    if (relatedIndex != null) {
                        relatedIndexes[count++] = relatedIndex;
                    }
                }
                indexes[i] = count == relatedIndexes.length? relatedIndexes:
                    Arrays.copyOf(relatedIndexes, count);
            }
            return indexes;
        }

        /**
         * @param condId    An {@code int} that is the ID of a global condition.
         * @return          An {@code int} that is the index of that condition, or {@code -1}
         *                  if it is not part of this graph.
         */
        public int getIndex(int condId) {
            Integer index = this.condIdToIndex.get(condId);
            return index == null? -1: index.intValue();
        }
        /**
         * @param index An {@code int} that is the index of a condition.
         * @return      An {@code int} that is the ID of the global condition at that index.
         */
        public int getCondId(int index) {
            return this.topoOrder[index];
        }
        /**
         * @return  An {@code int} that is the number of conditions of this graph, which is also
         *          the exclusive upper bound of the condition indexes.
         */
        public int getConditionCount() {
            return this.topoOrder.length;
        }
        /**
         * @param index An {@code int} that is the index of a condition.
         * @return      An {@code int[]} that contains the indexes of its direct ancestors.
         *              Never {@code null}, empty when it has none.
         */
        public int[] getDirectAncestorIndexes(int index) {
            return this.directAncestorIndexes[index];
        }
        /**
         * @param index An {@code int} that is the index of a condition.
         * @return      An {@code int[]} that contains the indexes of its direct descendants.
         *              Never {@code null}, empty when it has none.
         */
        public int[] getDirectDescendantIndexes(int index) {
            return this.directDescendantIndexes[index];
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
