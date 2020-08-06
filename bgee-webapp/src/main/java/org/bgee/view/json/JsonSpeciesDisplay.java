package org.bgee.view.json;

import static org.bgee.model.file.DownloadFile.CategoryEnum.AFFY_ANNOT;
import static org.bgee.model.file.DownloadFile.CategoryEnum.AFFY_DATA;
import static org.bgee.model.file.DownloadFile.CategoryEnum.EXPR_CALLS_COMPLETE;
import static org.bgee.model.file.DownloadFile.CategoryEnum.EXPR_CALLS_SIMPLE;
import static org.bgee.model.file.DownloadFile.CategoryEnum.RNASEQ_ANNOT;
import static org.bgee.model.file.DownloadFile.CategoryEnum.RNASEQ_DATA;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.model.expressiondata.CallService;
import org.bgee.model.file.DownloadFile;
import org.bgee.model.file.SpeciesDataGroup;
import org.bgee.model.file.DownloadFile.CategoryEnum;
import org.bgee.model.species.Species;
import org.bgee.view.JsonHelper;
import org.bgee.view.SpeciesDisplay;

/**
 * This class is the JSON view of the {@code SpeciesDisplay}.
 *
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 14, July 2019
 * @since   Bgee 13, June 2015
 */
public class JsonSpeciesDisplay extends JsonParentDisplay implements SpeciesDisplay {

    private final static Logger log = LogManager.getLogger(JsonSpeciesDisplay.class.getName());

    /**
     * Constructor providing the necessary dependencies. 
     * 
     * @param response          A {@code HttpServletResponse} that will be used to display the 
     *                          page to the client
     * @param requestParameters The {@code RequestParameters} handling the parameters of the 
     *                          current request.
     * @param prop              A {@code BgeeProperties} instance that contains the properties
     *                          to use.
     * @param jsonHelper        A {@code JsonHelper} used to dump variables into Json.
     * @param factory           The {@code JsonFactory} that instantiated this object.
     * 
     * @throws IllegalArgumentException If {@code factory} or {@code jsonHelper} is {@code null}.
     * @throws IOException      If there is an issue when trying to get or to use the
     *                          {@code PrintWriter} 
     */
    public JsonSpeciesDisplay(HttpServletResponse response,
            RequestParameters requestParameters, BgeeProperties prop,
            JsonHelper jsonHelper, JsonFactory factory) throws IllegalArgumentException, IOException {
        super(response, requestParameters, prop, jsonHelper, factory);
    }

    @Override
    public void displaySpeciesHomePage(List<Species> speciesList) {
    	
    	log.debug("hello world");
    	ArrayList<LinkedHashMap<String, String>> speciesHomePageList = new ArrayList<LinkedHashMap<String, String>>();
        speciesList.forEach((currentSpecies) -> {
            LinkedHashMap<String, String> speciesInfo = new LinkedHashMap<String, String>();
            speciesInfo.put("id", String.valueOf(currentSpecies.getId()));
            speciesInfo.put("name", String.valueOf(currentSpecies.getScientificName()));
            speciesHomePageList.add(speciesInfo);
        });
        LinkedHashMap<String, Object> resultSpeciesHome = new LinkedHashMap<String,Object>();
        resultSpeciesHome.put("result", speciesHomePageList);
        log.debug(resultSpeciesHome);
        this.sendResponse("List of species", resultSpeciesHome);

    }
    

    @Override
    public void sendSpeciesResponse(List<Species> species) {
        log.entry(species);
        
        LinkedHashMap<String, Object> data = new LinkedHashMap<String, Object>();
        data.put(this.getRequestParameters().getUrlParametersInstance()
                .getParamSpeciesList().getName(), species);
        
        this.sendResponse("List of requested species", data);
        
        log.exit();
    }

    @Override
    public void displaySpecies(Species species, SpeciesDataGroup speciesDataGroup) {
    	
        // create LinkedHashMap that we will pass to Gson in order to generate the JSON 
        LinkedHashMap<String, Object> JSONHashMap = new LinkedHashMap<String, Object>();
        
    	//LinkedHashMap containing General information:
        LinkedHashMap<String, String> generalInformation = new LinkedHashMap<String, String>();
    	generalInformation.put("scientificName", species.getScientificName());
    	generalInformation.put("commonName", species.getName());
    	generalInformation.put("speciesID", String.valueOf(species.getId()));
    	generalInformation.put("taxonomyURL", getSpeciesIdURL(species));
    	generalInformation.put("genomeSource", species.getGenomeSource().getName());
    	generalInformation.put("genomeSourceUrl", getSpeciesGenomeSourceURL(species));
    	generalInformation.put("Genome version", species.getGenomeVersion());
        
    	//LinkedHashMap containing Gene expression call files
        LinkedHashMap<String, String> geneExpressionCallFiles = new LinkedHashMap<String, String>();
        Set<CallService.Attribute> condParam = Collections.singleton(CallService.Attribute.ANAT_ENTITY_ID);
        Optional<DownloadFile> fileExpr = this.getCallFile(speciesDataGroup, EXPR_CALLS_SIMPLE, condParam);
        geneExpressionCallFiles.put("Anatomical entities only without advanced columns", getFileLi(fileExpr));
        fileExpr = this.getCallFile(speciesDataGroup, EXPR_CALLS_COMPLETE, condParam);
        geneExpressionCallFiles.put("Anatomical entities only with advanced columns", getFileLi(fileExpr));
        condParam = new HashSet<>(Arrays.asList(
                CallService.Attribute.ANAT_ENTITY_ID, CallService.Attribute.DEV_STAGE_ID));
        fileExpr = this.getCallFile(speciesDataGroup, EXPR_CALLS_SIMPLE, condParam);
        geneExpressionCallFiles.put("Anatomical entities and developmental stages without advanced columns", getFileLi(fileExpr));
        fileExpr = this.getCallFile(speciesDataGroup, EXPR_CALLS_COMPLETE, condParam);
        geneExpressionCallFiles.put("Anatomical entities and developmental stages with advanced columns", getFileLi(fileExpr));
        


    	//LinkedHashMap containing Processed expression value files
        LinkedHashMap<String, String> processedExpressionValueFiles = new LinkedHashMap<String, String>();
        Optional<DownloadFile> filePro = speciesDataGroup.getDownloadFiles().stream()
                .filter(f -> AFFY_ANNOT.equals(f.getCategory())).findFirst();
        processedExpressionValueFiles.put("Anatomical entities only without advanced columns", getFileLi(filePro));
        filePro = speciesDataGroup.getDownloadFiles().stream()
                .filter(f -> AFFY_DATA.equals(f.getCategory())).findFirst();
        processedExpressionValueFiles.put("Anatomical entities only with advanced columns", getFileLi(filePro));
        filePro = speciesDataGroup.getDownloadFiles().stream()
                .filter(f -> RNASEQ_ANNOT.equals(f.getCategory())).findFirst();
        processedExpressionValueFiles.put("Anatomical entities and developmental stages without advanced columns", getFileLi(filePro));
        filePro = speciesDataGroup.getDownloadFiles().stream()
                .filter(f -> RNASEQ_DATA.equals(f.getCategory())).findFirst();
        processedExpressionValueFiles.put("Anatomical entities and developmental stages with advanced columns", getFileLi(filePro));
        
        JSONHashMap.put("generalInformation", generalInformation);
        JSONHashMap.put("geneExpressionCallFiles", geneExpressionCallFiles);
        JSONHashMap.put("processedExpressionValueFiles", processedExpressionValueFiles);

        this.sendResponse("Explanation text",
                JSONHashMap);
        
        //test to see how to obtain the different information
        
          
    }
    
    private String getSpeciesIdURL(Species species){
    	final String speciesIdUrl = "https://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?lvl=0&amp;id=" + String.valueOf(species.getId());
    	return speciesIdUrl;
    	
    }
    
    private String getSpeciesGenomeSourceURL(Species species){
    	final String speciesSourceURL = species.getGenomeSource().getBaseUrl() + species.getScientificName().replace(" ", "_");
    	return speciesSourceURL;

    }
    
    private Optional<DownloadFile> getCallFile(SpeciesDataGroup speciesDataGroup, CategoryEnum category,
            Set<CallService.Attribute> attrs) {
	log.entry(speciesDataGroup, category, attrs);
	
	return log.exit(speciesDataGroup.getDownloadFiles().stream()
	.filter(f -> category.equals(f.getCategory()))
	.filter(f -> attrs.equals(f.getConditionParameters()))
	.findFirst());
	}
    
    private String getFileLi(Optional<DownloadFile> file) {
        log.entry(file);
        
        if (file.isPresent()) {
            DownloadFile downloadFile = file.get();
            return log.exit(this.prop.getDownloadRootDirectory() + downloadFile.getPath());
        }
        return log.exit("");
    }
    
    
}
