package org.bgee.view.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.model.TaskManager;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.species.Species;
import org.bgee.model.topanat.TopAnatResults;
import org.bgee.view.JsonHelper;
import org.bgee.view.TopAnatDisplay;

/**
 * This class generates the JSON views relative to topAnat.
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 13, Nov 2015
 * @since   Bgee 13
 */
public class JsonTopAnatDisplay extends JsonParentDisplay implements TopAnatDisplay {
    
    private final static Logger log = LogManager.getLogger(JsonTopAnatDisplay.class.getName());
    
    /**
     * A {@code String} that is the label of the count of genes whose the species is undetermined. 
     */
    private final static String UNDETERMINED_SPECIES_LABEL = "UNDETERMINED";

    /**
     * Constructor providing the necessary dependencies. 
     * 
     * @param response          A {@code HttpServletResponse} that will be used to display the 
     *                          page to the client
     * @param requestParameters The {@code RequestParameters} handling the parameters of the 
     *                          current request.
     * @param prop              A {@code BgeeProperties} instance that contains the properties
     *                          to use.
     * @param factory           The {@code JsonFactory} that instantiated this object.
     * @throws IOException      If there is an issue when trying to get or to use the
     *                          {@code PrintWriter} 
     */
    public JsonTopAnatDisplay(HttpServletResponse response,
            RequestParameters requestParameters, BgeeProperties prop,
            JsonFactory factory) throws IllegalArgumentException, IOException {
        super(response, requestParameters, prop, factory);
    }

    @Override
    public void displayTopAnatHomePage() {
        throw log.throwing(new UnsupportedOperationException("Not available for JSON display"));
    }

    @Override
    public void sendGeneListReponse(Map<Species, Long> speciesToGeneCount, String selectedSpeciesId,
            Set<DevStage> validStages, Set<String> submittedGeneIds, Set<String> undeterminedGeneIds,
            int statusCode, String msg) {
        log.entry(speciesToGeneCount, selectedSpeciesId,
                validStages, submittedGeneIds, undeterminedGeneIds, statusCode, msg);
        
        //sanity checks
        if (speciesToGeneCount.isEmpty() && undeterminedGeneIds.isEmpty()) {
            throw log.throwing(new IllegalArgumentException(
                    "Some gene information to display must be provided."));
        }

        //Transform speciesToGeneCount into a Map species ID -> gene count, and add 
        //the invalid gene count, associated to a specific key, and make it a LinkedHashMap, 
        //for sorted and predictable responses
        LinkedHashMap<String, Long> responseSpeciesIdToGeneCount = Optional.of(speciesToGeneCount)
                .map(map -> {
                    //create a map species ID -> gene count
                    Map<String, Long> newMap = map.entrySet().stream()
                            .collect(Collectors.toMap(e -> e.getKey().getId(), e -> e.getValue()));
                    //add an entry for undetermined genes
                    newMap.put(UNDETERMINED_SPECIES_LABEL, Long.valueOf(undeterminedGeneIds.size()));
                    return newMap;
                })
                .get().entrySet().stream()
                //sort in descending order of gene count (and in case of equality, 
                //by ascending order of key, for predictable message generation)
                .sorted((e1, e2) -> {
                    if (e1.getValue().equals(e2.getValue())) {
                        return e1.getKey().compareTo(e2.getKey()); 
                    } 
                    return e2.getValue().compareTo(e1.getValue());
                }).collect(Collectors.toMap(Entry::getKey, Entry::getValue, 
                    (v1, v2) -> {throw log.throwing(new IllegalStateException("no key collision possible"));}, 
                    LinkedHashMap::new));

        this.sendHeaders();
        this.write(new JsonHelper().toJson(new GeneListResponse(
                responseSpeciesIdToGeneCount, 
                //provide a TreeMap species ID -> species
                speciesToGeneCount.keySet().stream().collect(Collectors.toMap(
                        spe -> spe.getId(), spe -> spe, 
                        (v1, v2) -> {throw log.throwing(new IllegalStateException("No key collision possible"));}, 
                        TreeMap::new)), 
                selectedSpeciesId, 
                //provide a sorted List of DevStages
                Optional.ofNullable(validStages)
                    .map(stages -> stages.stream()
                        .sorted((st1, st2) -> st1.getId().compareTo(st2.getId()))
                        .collect(Collectors.toList()))
                    .orElse(new ArrayList<>()), 
                //SortedSet of submitted gene IDs
                Optional.ofNullable(submittedGeneIds)
                    .map(TreeSet<String>::new)
                    .orElse(new TreeSet<>()), 
                //SortedSet of undetermined gene IDs
                Optional.ofNullable(undeterminedGeneIds)
                    .map(TreeSet<String>::new)
                    .orElse(new TreeSet<>()),
                statusCode, msg)));
        
        log.exit();
    }
    


    @Override
    public void sendTopAnatParameters(String hash) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void sendNewJobResponse(int jobTrackingId) {
        // TODO Auto-generated method stub
    }

    @Override
    public void sendResultResponse(TopAnatResults results) {
        // TODO Auto-generated method stub
    }

    @Override
    public void sendJobStatusResponse(TaskManager taskManager) {
        // TODO Auto-generated method stub
    }

    @Override
    public void sendJobErrorResponse(TaskManager taskManager) {
        // TODO Auto-generated method stub
    }
    
    /**
     * A convenient class to be passed to {@link JsonHelper} for gene list upload responses. 
     */
    public static class GeneListResponse {
        /**
         * See {@link #getGeneCount()}.
         */
        private final LinkedHashMap<String, Long> geneCount;
        /**
         * See {@link #getDetectedSpecies()}.
         */
        private final TreeMap<String, Species> detectedSpecies;
        /**
         * See {@link #getSelectedSpecies()}.
         */
        private final String selectedSpecies;
        /**
         * See {@link #getStages()}.
         */
        private final List<DevStage> stages;
        /**
         * See {@link #getSubmittedGeneIds()}.
         */
        private final TreeSet<String> submittedGeneIds;
        /**
         * See {@link #getUndeterminedGeneIds()}.
         */
        private final TreeSet<String> undeterminedGeneIds;
        /**
         * See {@link #getStatusCode()}.
         */
        private final int statusCode;
        /**
         * See {@link #getMessage()}.
         */
        private final String msg;
        
        /**
         * Constructor of {@code GeneListResponse}. All {@code Collection}s or {@code Map}s 
         * have a predictable iteration order, for predictable and consistent responses.
         * 
         * @param geneCount             A {@code LinkedHashMap} where keys are {@code String}s 
         *                              corresponding to species IDs, the associated value being 
         *                              a {@code Long} that is the gene count on the species.
         * @param detectedSpecies       A {@code List} of {@code Species} detected in the gene list uploaded.
         * @param selectedSpecies       A {@code String} representing the ID of the selected species.
         * @param stages                A {@code List} of {@code DevStage}s that are 
         *                              valid dev. stages for {@code selectedSpecies}.
         * @param submittedGeneIds      A {@code TreeSet} of {@code String}s that are submitted gene IDs 
         *                              by the user.
         * @param undeterminedGeneIds   A {@code TreeSet} of {@code String}s that are gene IDs 
         *                              with undetermined species.
         * @param statusCode            An {@code int} that is the status code of response.
         * @param msg                   A {@code String} that is the message of response.
         */
        private GeneListResponse(LinkedHashMap<String, Long> geneCount, 
                TreeMap<String, Species> detectedSpecies, 
                String selectedSpecies, List<DevStage> stages, TreeSet<String> submittedGeneIds, 
                TreeSet<String> undeterminedGeneIds, int statusCode, String msg) {
            log.entry(geneCount, detectedSpecies, selectedSpecies, stages, 
                    submittedGeneIds, undeterminedGeneIds, statusCode, msg);
            this.geneCount= geneCount;
            this.detectedSpecies = detectedSpecies;
            this.selectedSpecies = selectedSpecies;
            this.stages = stages;
            this.submittedGeneIds = submittedGeneIds;
            this.undeterminedGeneIds = undeterminedGeneIds;
            this.statusCode = statusCode;
            this.msg = msg;
            log.exit();
        }
        
        /**
         * @return  The {@code Map} where keys are {@code String}s corresponding species IDs,
         *          the associated value being a {@code Long} that is the gene count on the species.
         */
        public LinkedHashMap<String, Long> getGeneCount() {
            return this.geneCount;
        }
        /**
         * @return  The {@code TreeMap} where keys are {@code String}s corresponding 
         *          to IDs of detected species, the associated value being the corresponding 
         *          {@code Species} object.
         */
        public TreeMap<String, Species> getDetectedSpecies() {
            return this.detectedSpecies;
        }
        /**
         * @return  The {@code String} representing the ID of the selected species.
         */
        public String getSelectedSpecies() {
            return this.selectedSpecies;
        }
        /**
         * @return The {@code Set} of {@code DevStage}s that are 
         *          valid dev. stages for {@code selectedSpecies}.
         */
        public List<DevStage> getStages() {
            return this.stages;
        }
        /**
         * @return  The {@code Set} of {@code String}s that are submitted gene IDs by the user.
         */
        public TreeSet<String> getSubmittedGeneIds() {
            return this.submittedGeneIds;
        }
        /**
         * @return  The {@code Set} of {@code String}s that are gene IDs with undetermined species.
         */
        public TreeSet<String> getUndeterminedGeneIds() {
            return this.undeterminedGeneIds;
        }
        /**
         * @return  The {@code int} that is the status code of response.
         */
        public int getStatusCode() {
            return this.statusCode;
        }
        /**
         * @return  The {@code String} that is the message of response.
         */
        public String getMessage() {
            return this.msg;
        }
    }    
}
