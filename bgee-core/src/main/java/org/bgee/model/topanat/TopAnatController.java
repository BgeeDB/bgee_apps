package org.bgee.model.topanat;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.BgeeProperties;

/**
 * @author Mathieu Seppey
 *
 */
public class TopAnatController {
    private final static Logger log = LogManager.getLogger(TopAnatController.class.getName());

    /**
     * 
     */
    private final List<TopAnatParams> topAnatParams;
    
    /**
     * 
     */
    private final TopAnatRManager rManager;

    /**
     * 
     */
    private final BgeeProperties props;

    /**
     * A {@code Function} accepting a {@code TopAnatParams} object as argument 
     * and returning a new {@code TopAnatAnalysis} instance.
     */
    private final Function<TopAnatParams, TopAnatAnalysis> topAnatAnalysisSupplier;
    
    /**
     * 
     * @param topAnatParams
     */
    public TopAnatController(List<TopAnatParams> topAnatParams) {
        this(topAnatParams, BgeeProperties.getBgeeProperties());
    }
    /**
     * 
     * @param topAnatParams
     * @param props
     */
    public TopAnatController(List<TopAnatParams> topAnatParams, BgeeProperties props) {
        this(topAnatParams, TopAnatAnalysis::new, null, props);
    }
    
    /**
     * 
     * @param params
     */
    public TopAnatController(List<TopAnatParams> topAnatParams, 
            Function<TopAnatParams, TopAnatAnalysis> topAnatAnalysisSupplier,
            TopAnatRManager rManager,
            BgeeProperties props) {
        log.entry(topAnatParams, topAnatAnalysisSupplier, props);
        
        if (topAnatParams == null || topAnatParams.isEmpty() || 
                topAnatParams.stream().anyMatch(Objects::isNull)) {
            throw log.throwing(new IllegalArgumentException("At least one TopAnatParams "
                    + "must be provided, and none should be null"));
        }
        if (topAnatAnalysisSupplier == null) {
            throw log.throwing(new IllegalArgumentException("A supplier of TopAnatAnalysis "
                    + "must be provided"));
        }
        if (props == null) {
            throw log.throwing(new IllegalArgumentException("A BgeeProperties object must be provided."));
        }
        this.topAnatParams = topAnatParams;
        this.topAnatAnalysisSupplier = topAnatAnalysisSupplier;
        this.rManager = rManager;
        this.props = props;
        
        log.exit();
    }
    
    /**
     * @throws IOException
     */
    public Stream<TopAnatResults> proceedToTopAnatAnalyses() {
        log.entry();
        
        this.validateForeground();
        
        // Create TopAnatAnalysis for each TopAnatParams
        return log.exit(this.topAnatParams.stream()
                .map(this.topAnatAnalysisSupplier::apply)
                .map(analysis -> {
                    try {
                        return analysis.proceedToAnalysis();
                    } catch (Throwable e) {
                        log.catching(e);
                        log.throwing(new RuntimeException(e));
                    }
                    return null;
                }));
    }
    
    private void validateForeground(){
        //Check whether the foreground is included into the submitted background or the species
    }
}
