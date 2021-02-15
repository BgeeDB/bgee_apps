package org.bgee.view.json;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.view.*;

/**
 * {@code ViewFactory} returning objects generating JSON views.
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 14, May 2019
 * @since   Bgee 13, July 2015
 */
public class JsonFactory extends ViewFactory { 
    
    private final static Logger log = LogManager.getLogger(JsonFactory.class.getName());
    
    /**
     * A {@code JsonHelper} to be passed to Json views, to dump variables into Json.
     */
    private final JsonHelper jsonHelper;
    
    /**
     * @param response          A {@code HttpServletResponse} that will be used to display the page to 
     *                          the client
     * @param requestParameters The {@code RequestParameters} that handles the parameters of the 
     *                          current request.
     * @param prop              An instance of {@code BgeeProperties} to provide the all 
     *                          the properties values
     */
    public JsonFactory(HttpServletResponse response, RequestParameters requestParameters, 
            BgeeProperties prop) {
        this(response, requestParameters, prop, new JsonHelper(prop, requestParameters));
    }
    /**
     * Constructor providing all dependencies. 
     * 
     * @param response          A {@code HttpServletResponse} that will be used to display the page to 
     *                          the client
     * @param requestParameters The {@code RequestParameters} that handles the parameters of the 
     *                          current request.
     * @param prop              An instance of {@code BgeeProperties} to provide the all 
     *                          the properties values
     */
    public JsonFactory(HttpServletResponse response, RequestParameters requestParameters, 
            BgeeProperties prop, JsonHelper jsonHelper) {
        super(response, requestParameters, prop);
        this.jsonHelper = jsonHelper;
    }

    @Override
    public ErrorDisplay getErrorDisplay() throws IOException {
        log.entry();
        return log.traceExit(new JsonErrorDisplay(this.response, this.requestParameters,
            this.prop, this.jsonHelper, this));
    }

    @Override
    public TopAnatDisplay getTopAnatDisplay() throws IOException {
        log.entry();
        return log.traceExit(new JsonTopAnatDisplay(this.response, this.requestParameters,
            this.prop, this.jsonHelper, this));
    }

    @Override
    public GeneralDisplay getGeneralDisplay() {
        throw log.throwing(new UnsupportedOperationException("Not available for JSON display"));
    }

    @Override
    public DownloadDisplay getDownloadDisplay() {
        throw log.throwing(new UnsupportedOperationException("Not available for JSON display"));
    }

    @Override
    public DocumentationDisplay getDocumentationDisplay() {
        throw log.throwing(new UnsupportedOperationException("Not available for JSON display"));
    }

    @Override
    public AboutDisplay getAboutDisplay() {
        throw log.throwing(new UnsupportedOperationException("Not available for JSON display"));
    }

	@Override
    public PrivacyPolicyDisplay getPrivacyPolicyDisplay() {
        throw log.throwing(new UnsupportedOperationException("Not available for JSON display"));
    }

    @Override
    public CollaborationDisplay getCollaborationDisplay() {
        throw log.throwing(new UnsupportedOperationException("Not available for JSON display"));
    }

    @Override
	public GeneDisplay getGeneDisplay() {
        throw log.throwing(new UnsupportedOperationException("Not available for JSON display"));
	}

    @Override
    public ExpressionComparisonDisplay getExpressionComparisonDisplay() {
        throw log.throwing(new UnsupportedOperationException("Not available for JSON display"));
    }

    @Override
    public RawDataDisplay getRawCallDisplay() throws IOException {
        throw log.throwing(new UnsupportedOperationException("Not available for JSON display"));
    }

    @Override
    public SpeciesDisplay getSpeciesDisplay() throws IOException {
        log.entry();
        return log.traceExit(new JsonSpeciesDisplay(this.response, this.requestParameters,
            this.prop, this.jsonHelper, this));
    }
	@Override
	public SearchDisplay getSearchDisplay() {
        throw log.throwing(new UnsupportedOperationException("Not available for JSON display"));
	}
	@Override
    public SparqlDisplay getSparqlDisplay() {
        throw log.throwing(new UnsupportedOperationException("Not available for JSON display"));
    }
    @Override
    public SourceDisplay getSourceDisplay() {
        throw log.throwing(new UnsupportedOperationException("Not available for JSON display"));
    }
    @Override
    public DAODisplay getDAODisplay() {
        log.entry();
        throw log.throwing(new UnsupportedOperationException("Not available for JSON display"));
    }

    @Override
    public JobDisplay getJobDisplay() throws IOException {
        log.entry();
        return log.traceExit(new JsonJobDisplay(this.response, this.requestParameters,
            this.prop, this.jsonHelper, this));
    }
    @Override
	public RPackageDisplay getRPackageDisplay() {
	    log.entry();
	    throw log.throwing(new UnsupportedOperationException("Not available for JSON display"));
	}

    @Override
    public FaqDisplay getFaqDisplay() {
        throw log.throwing(new UnsupportedOperationException("Not available for JSON display"));
    }

    @Override
    public ResourcesDisplay getResourceDisplay() throws IOException {
        throw log.throwing(new UnsupportedOperationException("Not available for JSON display"));
    }

    @Override
    public AnatomicalSimilarityDisplay getAnatomicalSimilarityDisplay() {
        throw log.throwing(new UnsupportedOperationException("Not available for JSON display"));
    }
    @Override
    public PublicationDisplay getPublicationDisplay() throws IOException {
        throw log.throwing(new UnsupportedOperationException("Not available for JSON display"));
    }
}
