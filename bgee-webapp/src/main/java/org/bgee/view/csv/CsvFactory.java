package org.bgee.view.csv;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.view.AboutDisplay;
import org.bgee.view.DAODisplay;
import org.bgee.view.DocumentationDisplay;
import org.bgee.view.DownloadDisplay;
import org.bgee.view.ErrorDisplay;
import org.bgee.view.GeneDisplay;
import org.bgee.view.GeneralDisplay;
import org.bgee.view.JobDisplay;
import org.bgee.view.RPackageDisplay;
import org.bgee.view.SearchDisplay;
import org.bgee.view.SourceDisplay;
import org.bgee.view.SpeciesDisplay;
import org.bgee.view.TopAnatDisplay;
import org.bgee.view.ViewFactory;

/**
 * {@code ViewFactory} that returns all displays for the CSV views.
 * 
 * @author  Valentine Rech de Laval
 * @author  Frederic Bastian
 * @version Bgee 13, Mar. 2016
 * @see Delimiter
 * @since   Bgee 13
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
        log.entry();
        return log.exit(new CsvErrorDisplay(this.response, this.requestParameters, this.prop, this, 
                this.delimiter));
    }

    @Override
    public DocumentationDisplay getDocumentationDisplay() throws IOException {
        throw log.throwing(new UnsupportedOperationException("Not available for TSV/CSV display"));
    }

    @Override
    public AboutDisplay getAboutDisplay() throws IOException {
        throw log.throwing(new UnsupportedOperationException("Not available for TSV/CSV display"));
    }

    @Override
    public TopAnatDisplay getTopAnatDisplay() throws IOException {
        throw log.throwing(new UnsupportedOperationException("Not available for TSV/CSV display"));
    }

	@Override
	public GeneDisplay getGeneDisplay() throws IOException {
        throw log.throwing(new UnsupportedOperationException("Not available for TSV/CSV display"));
	}

    @Override
    public SpeciesDisplay getSpeciesDisplay() throws IOException {
        log.entry();
        return log.exit(new CsvSpeciesDisplay(this.response, this.requestParameters, this.prop, this, 
                this.delimiter));
    }

	@Override
	public SearchDisplay getSearchDisplay() throws IOException {
        throw log.throwing(new UnsupportedOperationException("Not available for TSV/CSV display"));
	}
	
	@Override
    public SourceDisplay getSourceDisplay() throws IOException {
        throw log.throwing(new UnsupportedOperationException("Not available for TSV/CSV display"));
    }

    @Override
    public DAODisplay getDAODisplay() throws IOException {
        log.entry();
        return log.exit(new CsvDAODisplay(this.response, this.requestParameters, this.prop, this, 
                this.delimiter));
    }

    @Override
    public JobDisplay getJobDisplay() throws IOException {
        log.entry();
        return log.exit(new CsvJobDisplay(this.response, this.requestParameters, this.prop, this, 
                this.delimiter));
    }
    
    @Override
    public RPackageDisplay getRPackageDisplay() throws IOException {
        log.entry();
        return log.exit(new CsvRPackageDisplay(this.response, this.requestParameters, this.prop, this, 
                this.delimiter));
    }
}
