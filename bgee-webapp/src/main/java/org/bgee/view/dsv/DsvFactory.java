package org.bgee.view.dsv;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.view.AboutDisplay;
import org.bgee.view.DocumentationDisplay;
import org.bgee.view.DownloadDisplay;
import org.bgee.view.ErrorDisplay;
import org.bgee.view.GeneralDisplay;
import org.bgee.view.TopAnatDisplay;
import org.bgee.view.ViewFactory;

public class DsvFactory extends ViewFactory {	
    
    private final static Logger log = LogManager.getLogger(DsvFactory.class.getName());
    
	public DsvFactory(HttpServletResponse response, String localDelimiter,
	        RequestParameters requestParameters, BgeeProperties prop) {
		super(response, requestParameters, prop);
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
    public ErrorDisplay getErrorDisplay() {
        throw log.throwing(new UnsupportedOperationException("Not available for TSV/CSV display"));
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
}
