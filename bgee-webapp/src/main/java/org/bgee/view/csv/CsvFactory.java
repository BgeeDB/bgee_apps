package org.bgee.view.csv;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.view.*;

/**
 * {@code ViewFactory} that returns all displays for the CSV views.
 * 
 * @author  Valentine Rech de Laval
 * @author  Frederic Bastian
 * @version Bgee 14, May 2019
 * @see Delimiter
 * @since   Bgee 13, July 2014
 */
public class CsvFactory extends ViewFactory {	
    
    private final static Logger log = LogManager.getLogger(CsvFactory.class.getName());
    
    /**
     * A {@code Delimiter} defining the delimiter between columns in CSV views. 
     */
    private final Delimiter delimiter;
    
	public CsvFactory(HttpServletResponse response, RequestParameters requestParameters, BgeeProperties prop, 
	        Delimiter delimiter) {
		super(response, requestParameters, prop);
		this.delimiter = delimiter;
	}

	@Override
	public DownloadDisplay getDownloadDisplay() {
	    throw log.throwing(new UnsupportedOperationException("Not available for TSV/CSV display"));
	}

	@Override
	public GeneralDisplay getGeneralDisplay() {
	    throw log.throwing(new UnsupportedOperationException("Not available for TSV/CSV display"));
	}

    @Override
    public ErrorDisplay getErrorDisplay() throws IllegalArgumentException, IOException {
        log.traceEntry();
        return log.traceExit(new CsvErrorDisplay(this.response, this.requestParameters, this.prop, this, 
                this.delimiter));
    }

    @Override
    public DocumentationDisplay getDocumentationDisplay() {
        throw log.throwing(new UnsupportedOperationException("Not available for TSV/CSV display"));
    }

    @Override
    public AboutDisplay getAboutDisplay() {
        throw log.throwing(new UnsupportedOperationException("Not available for TSV/CSV display"));
    }

    @Override
    public PrivacyPolicyDisplay getPrivacyPolicyDisplay() {
        throw log.throwing(new UnsupportedOperationException("Not available for TSV/CSV display"));
    }

    @Override
    public CollaborationDisplay getCollaborationDisplay() {
        throw log.throwing(new UnsupportedOperationException("Not available for TSV/CSV display"));
    }

    @Override
    public TopAnatDisplay getTopAnatDisplay() {
        throw log.throwing(new UnsupportedOperationException("Not available for TSV/CSV display"));
    }

	@Override
	public GeneDisplay getGeneDisplay() {
        throw log.throwing(new UnsupportedOperationException("Not available for TSV/CSV display"));
	}

    @Override
    public ExpressionComparisonDisplay getExpressionComparisonDisplay() {
        throw log.throwing(new UnsupportedOperationException("Not available for TSV/CSV display"));
    }

    @Override
    public RawDataDisplay getRawCallDisplay() throws IOException {
        throw log.throwing(new UnsupportedOperationException("Not available for TSV/CSV display"));
    }

    @Override
    public SpeciesDisplay getSpeciesDisplay() throws IOException {
        log.traceEntry();
        return log.traceExit(new CsvSpeciesDisplay(this.response, this.requestParameters, this.prop, this, 
                this.delimiter));
    }

	@Override
	public SearchDisplay getSearchDisplay() {
        throw log.throwing(new UnsupportedOperationException("Not available for TSV/CSV display"));
	}
	
	@Override
    public SparqlDisplay getSparqlDisplay() {
        throw log.throwing(new UnsupportedOperationException("Not available for JSON display"));
    }
	
	@Override
    public SourceDisplay getSourceDisplay() {
        throw log.throwing(new UnsupportedOperationException("Not available for TSV/CSV display"));
    }

    @Override
    public DAODisplay getDAODisplay() throws IOException {
        log.traceEntry();
        return log.traceExit(new CsvDAODisplay(this.response, this.requestParameters, this.prop, this, 
                this.delimiter));
    }

    @Override
    public JobDisplay getJobDisplay() throws IOException {
        log.traceEntry();
        return log.traceExit(new CsvJobDisplay(this.response, this.requestParameters, this.prop, this, 
                this.delimiter));
    }
    
    @Override
    public RPackageDisplay getRPackageDisplay() throws IOException {
        log.traceEntry();
        return log.traceExit(new CsvRPackageDisplay(this.response, this.requestParameters, this.prop, this, 
                this.delimiter));
    }

    @Override
    public FaqDisplay getFaqDisplay() {
        throw log.throwing(new UnsupportedOperationException("Not available for TSV/CSV display"));
    }

    @Override
    public ResourcesDisplay getResourceDisplay() throws IOException {
        throw log.throwing(new UnsupportedOperationException("Not available for TSV/CSV display"));
    }

    @Override
    public AnatomicalSimilarityDisplay getAnatomicalSimilarityDisplay() {
        throw log.throwing(new UnsupportedOperationException("Not available for TSV/CSV display"));
    }

    @Override
    public PublicationDisplay getPublicationDisplay() throws IOException {
        throw log.throwing(new UnsupportedOperationException("Not available for TSV/CSV display"));
    }
}