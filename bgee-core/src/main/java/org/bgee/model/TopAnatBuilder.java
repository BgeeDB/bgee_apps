package org.bgee.model;

import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.querytool.CallFilter;
import org.bgee.model.expressiondata.querytool.CallService;

public class TopAnatBuilder {

    private Collection<String> submittedIds;

    private boolean backgroundSubmitted;

    private Collection<String> submittedBackgroundIds;
    
    private final CallFilter filter;
    
    private final static Logger log = LogManager
            .getLogger(TopAnatBuilder.class.getName());
    
    private CallService service;

    public TopAnatBuilder(Collection<String> submittedIds,boolean backgroundSubmitted,
            CallFilter filter,CallService service) {
        log.entry(submittedIds,backgroundSubmitted);
        // Mandatory attributes
        this.submittedIds = submittedIds;
        this.backgroundSubmitted = backgroundSubmitted;
        this.filter = filter;
        // Optional attributes default values
        this.submittedBackgroundIds = null;
        this.service = service;
        log.exit();
    }
    
    public TopAnatBuilder submittedBackgroundIds(Collection<String> submittedBackgroundIds){
        log.entry(submittedBackgroundIds);
        this.submittedBackgroundIds = submittedBackgroundIds;
        return log.exit(this);
    }

    public Collection<String> getSubmittedIds() {
        return submittedIds;
    }

    public boolean isBackgroundSubmitted() {
        return backgroundSubmitted;
    }

    public Collection<String> getSubmittedBackgroundIds() {
        return submittedBackgroundIds;
    }

    public CallFilter getCallFilter() {
        return filter;
    }

    public CallService getService() {
        return service;
    }
    
    public TopAnatAnalysis build(){
        return new TopAnatAnalysis(this);
    }
    
}
