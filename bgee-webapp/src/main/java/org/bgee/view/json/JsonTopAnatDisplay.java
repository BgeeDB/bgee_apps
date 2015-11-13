package org.bgee.view.json;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.model.TaskManager;
import org.bgee.model.anatdev.DevStage;
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
    public void sendGeneListReponse(Map<String, Long> speciesIdToGeneCount, String selectedSpeciesId,
            Set<DevStage> validStages, Set<String> submittedGeneIds, Set<String> undeterminedGeneIds,
            int statusCode, String msg) {
        log.entry(speciesIdToGeneCount, selectedSpeciesId,
                validStages, submittedGeneIds, undeterminedGeneIds, statusCode, msg);

        this.sendHeaders();
        this.write(new JsonHelper().toJson(new GeneListResponse(speciesIdToGeneCount, 
                selectedSpeciesId, validStages, submittedGeneIds, undeterminedGeneIds,
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
    
    public static class GeneListResponse {
        
        /**
         * See {@link #getGeneCount()}.
         */
        Map<String, Long> geneCount;
        
        /**
         * See {@link #getSelectedSpecies()}.
         */
        String selectedSpecies;
        
        /**
         * See {@link #getStages()}.
         */
        Set<DevStage> stages;
        
        /**
         * See {@link #getSubmittedGeneIds()}.
         */
        Set<String> submittedGeneIds;

        /**
         * See {@link #getUndeterminedGeneIds()}.
         */
        Set<String> undeterminedGeneIds;
        
        /**
         * See {@link #getStatusCode()}.
         */
        int statusCode;
        
        /**
         * See {@link #getMessage()}.
         */
        String msg;
        
        /**
         * Constructor of {@code GeneListResponse}.
         * 
         * @param geneCount             A {@code Map} where keys are {@code String}s corresponding 
         *                              species IDs, the associated value being a {@code Long}
         *                              that is the gene count on the species.
         * @param selectedSpecies       A {@code String} representing the ID of the selected species.
         * @param stages                A {@code Set} of {@code DevStage}s that are 
         *                              valid dev. stages for {@code selectedSpecies}.
         * @param submittedGeneIds      A {@code Set} of {@code String}s that are submitted gene IDs 
         *                              by the user.
         * @param undeterminedGeneIds   A {@code Set} of {@code String}s that are gene IDs 
         *                              with undetermined species.
         * @param statusCode            An {@code int} that is the status code of response.
         * @param msg                   A {@code String} that is the message of response.
         */
        public GeneListResponse(Map<String, Long> geneCount, String selectedSpecies,
                Set<DevStage> stages, Set<String> submittedGeneIds, 
                Set<String> undeterminedGeneIds, int statusCode, String msg) {
            this.geneCount= Collections.unmodifiableMap(geneCount);
            this.selectedSpecies = selectedSpecies;
            this.stages = Collections.unmodifiableSet(stages);
            this.submittedGeneIds = Collections.unmodifiableSet(submittedGeneIds);
            this.undeterminedGeneIds = Collections.unmodifiableSet(undeterminedGeneIds) ;
            this.statusCode = statusCode;
            this.msg = msg;
        }
        
        /**
         * @return  The {@code Map} where keys are {@code String}s corresponding species IDs,
         *          the associated value being a {@code Long} that is the gene count on the species.
         */
        public Map<String, Long> getGeneCount() {
            return this.geneCount;
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
        public Set<DevStage> getStages() {
            return this.stages;
        }
        
        /**
         * @return  The {@code Set} of {@code String}s that are submitted gene IDs by the user.
         */
        public Set<String> getSubmittedGeneIds() {
            return this.submittedGeneIds;
        }
        
        /**
         * @return  The {@code Set} of {@code String}s that are gene IDs with undetermined species.
         */
        public Set<String> getUndeterminedGeneIds() {
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
