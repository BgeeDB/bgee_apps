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
 * @version Bgee 14, July 2019
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
	public DownloadDisplay getDownloadDisplay()  throws IOException {
	    log.traceEntry();
		return log.traceExit(new HtmlDownloadDisplay(this.response, this.requestParameters,
		        this.prop, this.jsonHelper, this));
	}

	@Override
	public GeneralDisplay getGeneralDisplay() throws IOException {
	    log.traceEntry();
		return log.traceExit(new HtmlGeneralDisplay(this.response, this.requestParameters,
		        this.prop, this));
	}

    @Override
    public ErrorDisplay getErrorDisplay() throws IOException {
        log.traceEntry();
        return log.traceExit(new HtmlErrorDisplay(this.response, this.requestParameters,
                this.prop, this));
    }

    @Override
    public DocumentationDisplay getDocumentationDisplay() throws IOException {
        log.traceEntry();
        return log.traceExit(new HtmlDocumentationDisplay(
                this.response, this.requestParameters, this.prop, this));
    }

    @Override
    public AboutDisplay getAboutDisplay() throws IOException {
        log.traceEntry();
        return log.traceExit(new HtmlAboutDisplay(this.response, this.requestParameters, this.prop, this));
    }

    @Override
    public PrivacyPolicyDisplay getPrivacyPolicyDisplay() throws IOException {
        log.traceEntry();
        return log.traceExit(new HtmlPrivacyPolicyDisplay(this.response, this.requestParameters, this.prop, this));
    }

    @Override
    public CollaborationDisplay getCollaborationDisplay() throws IOException {
        log.traceEntry();
        return log.traceExit(new HtmlCollaborationDisplay(
                this.response, this.requestParameters, this.prop, this));
    }

    @Override
    public TopAnatDisplay getTopAnatDisplay() throws IOException {
        log.traceEntry();
        return log.traceExit(new HtmlTopAnatDisplay(this.response, this.requestParameters, this.prop, this));
    }
    
	@Override
	public GeneDisplay getGeneDisplay() throws IOException {
		log.traceEntry();
		return log.traceExit(new HtmlGeneDisplay(response, requestParameters, prop, jsonHelper, this));
	}

    @Override
    public ExpressionComparisonDisplay getExpressionComparisonDisplay() throws IOException {
        log.traceEntry();
        return log.traceExit(new HtmlExpressionComparisonDisplay(response, requestParameters, prop, jsonHelper, this));
    }

    @Override
    public RawDataDisplay getRawCallDisplay() throws IOException {
        log.traceEntry();
        return log.traceExit(new HtmlRawDataDisplay(response, requestParameters, prop, jsonHelper, this));
    }

    @Override
	public SourceDisplay getSourceDisplay() throws IOException {
	    log.traceEntry();
	    return log.traceExit(new HtmlSourceDisplay(this.response, this.requestParameters, this.prop, this));
	}
	
    @Override
    public SpeciesDisplay getSpeciesDisplay() throws IOException {
        return log.traceExit(new HtmlSpeciesDisplay(this.response, this.requestParameters, this.prop, this));
    }
	@Override
	public SearchDisplay getSearchDisplay() throws IOException {
        throw log.throwing(new UnsupportedOperationException("Not available for HTML display"));
	}
    @Override
    public DAODisplay getDAODisplay() throws IOException {
        log.traceEntry();
        throw log.throwing(new UnsupportedOperationException("Not available for HTML display"));
    }
    @Override
    public JobDisplay getJobDisplay() throws IOException {
        log.traceEntry();
        return log.traceExit(new HtmlJobDisplay(this.response, this.requestParameters, this.prop, this));
    }
    @Override
	public RPackageDisplay getRPackageDisplay() throws IOException {
	    log.traceEntry();
	    throw log.throwing(new UnsupportedOperationException("Not available for HTML display"));
	}
    @Override
    public FaqDisplay getFaqDisplay() throws IOException {
        log.traceEntry();
        return log.traceExit(new HtmlFaqDisplay(this.response, this.requestParameters, this.prop, this));
    }

    @Override
    public SparqlDisplay getSparqlDisplay() throws IOException {
        log.traceEntry();
        return log.traceExit(new HtmlSparqlDisplay(this.response, this.requestParameters, this.prop, this));
    }

    @Override
    public ResourcesDisplay getResourceDisplay() throws IOException {
        log.traceEntry();
        return log.traceExit(new HtmlResourcesDisplay(this.response, this.requestParameters, this.prop, this));
    }

    @Override
    public AnatomicalSimilarityDisplay getAnatomicalSimilarityDisplay() throws IOException {
        log.traceEntry();
        return log.traceExit(new HtmlAnatomicalSimilarityDisplay(
                this.response, this.requestParameters, this.prop, this));
    }
    @Override
    public PublicationDisplay getPublicationDisplay() throws IOException {
        log.traceEntry();
        return log.traceExit(new HtmlPublicationsDisplay(this.response, this.requestParameters, this.prop, this));
    }
}
