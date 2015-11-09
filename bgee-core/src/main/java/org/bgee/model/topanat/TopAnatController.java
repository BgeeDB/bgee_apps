package org.bgee.model.topanat;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.BgeeProperties;
import org.bgee.model.QueryTool;
import org.bgee.model.ServiceFactory;
import org.bgee.model.function.TriFunction;

/**
 * @author Mathieu Seppey
 */
public class TopAnatController extends QueryTool {
    private final static Logger log = LogManager.getLogger(TopAnatController.class.getName()); 

    /**
     * 
     */
    private final List<TopAnatParams> topAnatParams;

    /**
     * 
     */
    private final BgeeProperties props;
    
    /**
     * 
     */
    private final ServiceFactory serviceFactory;

    /**
     * A {@code TriFunction} allowing to obtain new {@code TopAnatAnalysis} instances.
     */
    private final TriFunction<TopAnatParams, BgeeProperties, ServiceFactory, TopAnatAnalysis> 
        topAnatAnalysisSupplier;
    
    /**
     * 
     * @param topAnatParams
     */
    public TopAnatController(List<TopAnatParams> topAnatParams) {
        this(topAnatParams, BgeeProperties.getBgeeProperties(), new ServiceFactory());
    }
    /**
     * 
     * @param topAnatParams
     * @param props
     */
    public TopAnatController(List<TopAnatParams> topAnatParams, BgeeProperties props, 
            ServiceFactory serviceFactory) {
        this(topAnatParams, props, serviceFactory, TopAnatAnalysis::new);
    }
    
    /**
     * 
     * @param params
     */
    public TopAnatController(List<TopAnatParams> topAnatParams, BgeeProperties props, 
            ServiceFactory serviceFactory, 
            TriFunction<TopAnatParams, BgeeProperties, ServiceFactory, TopAnatAnalysis> topAnatAnalysisSupplier) {
        log.entry(topAnatParams, props, serviceFactory, topAnatAnalysisSupplier);
        
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
        if (serviceFactory == null) {
            throw log.throwing(new IllegalArgumentException("A ServiceFactory must be provided."));
        }
        this.topAnatParams = Collections.unmodifiableList(topAnatParams);
        this.topAnatAnalysisSupplier = topAnatAnalysisSupplier;
        this.props = props;
        this.serviceFactory = serviceFactory;
        
        log.exit();
    }
    
    /**
     * @throws IOException
     */
    public Stream<TopAnatResults> proceedToTopAnatAnalyses() {
        log.entry();
                
        // Create TopAnatAnalysis for each TopAnatParams
                
        return log.exit(this.topAnatParams.stream()
                .map(params -> this.topAnatAnalysisSupplier.apply(params, this.props, 
                        this.serviceFactory))
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
        
    @Override
    protected Logger getLogger() {
        return log;
    }
}
