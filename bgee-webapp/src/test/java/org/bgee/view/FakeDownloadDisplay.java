package org.bgee.view;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.model.file.SpeciesDataGroup;

/**
 * This is a fake display used for tests. It should be called when the parameter 'page' provided
 * in the URL is 'download'.
 * 
 * @author Mathieu Seppey
 * @author Valentine Rech de Laval
 * @version Bgee 13 May 2015
 * @since   Bgee 13
 */
public class FakeDownloadDisplay extends FakeParentDisplay implements DownloadDisplay {
    
    public FakeDownloadDisplay(HttpServletResponse response, RequestParameters requestParameters,
            BgeeProperties prop, ViewFactory factory) throws IOException {
        super(response, requestParameters, prop, factory);
    }

    @Override
    /**
     * This method call the {@code PrintWriter}. This will be used to assess that the
     * controller handles all parameters correctly to send the correct output to the
     * {@code HttpServletResponse}
     */
    public void displayDownloadHomePage() {
        this.out.println("Test page is good !");
    }

    @Override
    public void displayProcessedExpressionValuesDownloadPage(List<SpeciesDataGroup> groups
    		, Map<String, Set<String>> keywords) {
        this.out.println("TestB");
    }

    @Override
    public void displayGeneExpressionCallDownloadPage(List<SpeciesDataGroup> groups
    		, Map<String, Set<String>> keywords) {
        this.out.println("TestC");
    }
}
