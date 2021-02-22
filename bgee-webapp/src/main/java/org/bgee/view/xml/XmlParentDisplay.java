package org.bgee.view.xml;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.view.ConcreteDisplayParent;
import org.bgee.view.ViewFactory;

/**
 * Parent of all display for the XML view.
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 13, Feb. 2016
 * @since   Bgee 13, Feb. 2016
 */
//TODO javadoc
public class XmlParentDisplay extends ConcreteDisplayParent {

    private final static Logger log = LogManager.getLogger(XmlParentDisplay.class.getName());

	protected XmlParentDisplay(HttpServletResponse response, RequestParameters requestParameters, BgeeProperties prop,
			ViewFactory factory) throws IllegalArgumentException, IOException {
		super(response, requestParameters, prop, factory);
	}

	protected void startDisplay() {
		log.traceEntry();
		this.sendHeaders();
		this.writeln("<?xml version='1.0' encoding='UTF-8' ?>");
		log.traceExit();
	}
	
	protected void endDisplay() {
		log.traceEntry();
		log.traceExit();
	}

	@Override
	protected String getContentType() {
        log.traceEntry();
        return log.traceExit("text/xml");
	}

	protected static String xmlEntities(String stringToWrite) {
		log.traceEntry();
		try {
    		return log.traceExit(StringEscapeUtils.escapeHtml4(stringToWrite).replaceAll("'", "&apos;"));
    	} catch (Exception e) {
    		return log.traceExit("");
    	}
    }
}
