package org.bgee.model.topanat;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.BgeeProperties;
import org.bgee.model.species.Species;

/**
 * @author Mathieu Seppey
 *
 */
public class TopAnatController {

    /**
     * 
     */
    private final static Logger log = LogManager
            .getLogger(TopAnatController.class.getName());

    /**
     * 
     */
    private final Set<TopAnatParams> topAnatParams;

    /**
     * 
     */
    private final BgeeProperties prop = BgeeProperties.getBgeeProperties();
    
    private final String speciesId;
    
    private List<TopAnatAnalysis> topAnatAnalyses;

    /**
     * 
     * @param params
     */
    public TopAnatController(Set<TopAnatParams> topAnatParams) {
        log.entry(topAnatParams);
        this.topAnatParams = topAnatParams;
        this.speciesId = this.detectSpecies().getId();
        log.exit();
    }
    
    /**
     * @throws IOException
     */
    public void proceedToTopAnatAnalyses() throws IOException{
        this.validateForeground();
        // Create TopAnatAnalysis for each TopAnatParams
        this.topAnatAnalyses = this.topAnatParams.stream().map(
                topAnatParam -> new TopAnatAnalysis(topAnatParam,this.speciesId))
                .collect(Collectors.toList());
        // Run the analyses
        for (TopAnatAnalysis analysis : this.topAnatAnalyses){
            analysis.beginTopAnatAnalysis();
        }
    }
    
    /**
     * @throws IOException
     */
    public List<TopAnatResults> getResults() throws IOException{
        return this.topAnatAnalyses.stream()
                .map(TopAnatAnalysis::getResults)
                .collect(Collectors.toList());
    }
    
    private void validateForeground(){
        //Check whether the foreground is included into the submitted background or the species
    }
    
    /**
     * @return
     */
    private Species detectSpecies(){
        // TODO
        return new Species("999");
    }
    
}
