package org.bgee.view.html;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.view.*;

/**
 * {@code ViewFactory} that returns all displays for the HTML view.
 * 
 * @author  Mathieu Seppey
 * @author  Valentine Rech de Laval
 * @version Bgee 15.0, Oct. 2022
 * @since   Bgee 13, July 2014
 */
public class HtmlFactory extends ViewFactory {
    
    private final static Logger log = LogManager.getLogger(HtmlFactory.class.getName());
    
    /**
     * A {@code JsonHelper} used to read/write variables in JSON, to be provided to views 
     * returned by this {@code HtmlFactory}.
     */
    private final JsonHelper jsonHelper;
    /**
     * Delegates to {@link #HtmlFactory(HttpServletResponse, RequestParameters, BgeeProperties, JsonHelper)} 
     * by providing a {@code null} argument as {@code JsonHelper} argument.
     * 
     * @param response          A {@code HttpServletResponse} that will be used to display the page to 
     *                          the client
     * @param requestParameters The {@code RequestParameters} that handles the parameters of the 
     *                          current request.
     * @param prop              An instance of {@code BgeeProperties} to provide the all 
     *                          the properties values
     */
    public HtmlFactory(HttpServletResponse response, RequestParameters requestParameters,
            BgeeProperties prop) {
        this(response, requestParameters, prop, null);
    }
    /**
     * Constructor 
     * 
     * @param response          A {@code HttpServletResponse} that will be used to display the page to 
     *                          the client
     * @param requestParameters The {@code RequestParameters} that handles the parameters of the 
     *                          current request.
     * @param prop              An instance of {@code BgeeProperties} to provide the all 
     *                          the properties values
     * @param jsonHelper        A {@code JsonHelper} used to read/write variables in JSON, 
     *                          to be provided to views returned by this {@code HtmlFactory}. 
     *                          If {@code null}, the default {@code JsonHelper} implemenatation 
     *                          is used.
     */
	public HtmlFactory(HttpServletResponse response, RequestParameters requestParameters,
	        BgeeProperties prop, JsonHelper jsonHelper) {
    	super(response, requestParameters, prop);
    	
    	if (jsonHelper == null) {
    	    this.jsonHelper = new JsonHelper(prop, requestParameters);
    	} else {
    	    this.jsonHelper = jsonHelper;
	    }
    }
    @Override
    public ErrorDisplay getErrorDisplay() throws IOException {
        log.traceEntry();
        return log.traceExit(new HtmlErrorDisplay(this.response, this.requestParameters,
                this.prop, this));
    }
    @Override
    public DownloadDisplay getDownloadDisplay() throws IOException {
        throw log.throwing(new UnsupportedOperationException("Not available for HTML display"));
    }
    public TopAnatDisplay getTopAnatDisplay() throws IOException {
        throw log.throwing(new UnsupportedOperationException("Not available for HTML display"));
    }
    @Override
    public GeneDisplay getGeneDisplay() throws IOException {
        throw log.throwing(new UnsupportedOperationException("Not available for HTML display"));
    }
    @Override
    public ExpressionComparisonDisplay getExpressionComparisonDisplay() throws IOException {
        throw log.throwing(new UnsupportedOperationException("Not available for HTML display"));
    }
    @Override
    public DataDisplay getDataDisplay() throws IOException {
        throw log.throwing(new UnsupportedOperationException("Not available for HTML display"));
    }
    @Override
    public SpeciesDisplay getSpeciesDisplay() throws IOException {
        throw log.throwing(new UnsupportedOperationException("Not available for HTML display"));
    }
    @Override
    public SearchDisplay getSearchDisplay() throws IOException {
        throw log.throwing(new UnsupportedOperationException("Not available for HTML display"));
    }
    @Override
    public SourceDisplay getSourceDisplay() throws IOException {
        throw log.throwing(new UnsupportedOperationException("Not available for HTML display"));
    }
    @Override
    public DAODisplay getDAODisplay() throws IOException {
        throw log.throwing(new UnsupportedOperationException("Not available for HTML display"));
    }
    @Override
    public JobDisplay getJobDisplay() throws IOException {
        throw log.throwing(new UnsupportedOperationException("Not available for HTML display"));
    }
    @Override
    public RPackageDisplay getRPackageDisplay() throws IOException {
        throw log.throwing(new UnsupportedOperationException("Not available for HTML display"));
    }
    @Override
    public AnatomicalSimilarityDisplay getAnatomicalSimilarityDisplay() throws IOException {
        throw log.throwing(new UnsupportedOperationException("Not available for HTML display"));
    }
    
}
