package org.bgee.model;

import java.io.IOException;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO;
import org.bgee.model.expressiondata.querytool.CallFilter;
import org.bgee.model.expressiondata.querytool.CallService;

public class TopAnatAnalysis extends QueryTool {

    private Collection<String> submittedIds;

    private boolean backgroundSubmitted;

    private Collection<String> submittedBackgroundIds;

    private final static Logger log = LogManager
            .getLogger(TopAnatAnalysis.class.getName());

    private final CallFilter filter;
    
    private final CallService service;
        
    @Override
    protected Logger getLogger() {
        return log;
    }

    public TopAnatAnalysis(TopAnatBuilder builder) {
        log.entry(builder);
        this.submittedIds = builder.getSubmittedIds();
        this.backgroundSubmitted = builder.isBackgroundSubmitted();
        this.submittedBackgroundIds = builder.getSubmittedBackgroundIds();
        this.filter = builder.getCallFilter();
        this.service = builder.getService(); 
        log.exit();
    }

    public void beginTopAnatAnalysis(String uniqueFileNameString) throws IOException{
        log.entry(uniqueFileNameString);
        // fetch parameters ( BgeeProperties ? )
        // define tsv and pdf result file
        String tmpResultFileName = new String("");
        String tmpPDFFileName = new String("");
        // check whether the foreground is included in the background
        // detect species
        try{
            // check le lock
            // perform the R anaysis
            this.performRCallerFunctions(tmpResultFileName, tmpPDFFileName);
        }
        finally{
            // delete tmp files
            // unlock lock
        }
        log.exit();
    }

    private void performRCallerFunctions(String resultFileName, String resultPDFFileName) 
            throws IOException {
        log.entry(resultFileName,resultPDFFileName);
        //RCaller caller = new RCaller();
        log.exit();

    }

}
