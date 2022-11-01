package org.bgee.view;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.view.csv.CsvFactory;
import org.bgee.view.csv.Delimiter;
import org.bgee.view.html.HtmlFactory;
import org.bgee.view.json.JsonFactory;
import org.bgee.view.xml.XmlFactory;

/**
 * This class provide the appropriate {@code ViewFactory} depending on the {@code DisplayType}
 * 
 * @author Mathieu Seppey
 * @version Bgee 15.0, Nov. 2022
 * @since   Bgee 13
 */
public class ViewFactoryProvider {
    
    private final static Logger log = LogManager.getLogger(ViewFactoryProvider.class.getName());
    
    /**
     * An {@code enum} of the different display types: {@code JSON}, {@code HTML}, {@code XML}, 
     * {@code CSV}, {@code TSV}.
     */
    public static enum DisplayType {
        JSON, HTML, XML, CSV, TSV;
    }
    
    /**
     * The {@code DisplayType} used as default
     */
    public static final DisplayType DEFAULT = DisplayType.JSON;
    
    /**
     * An instance of {@code BgeeProperties} to provide the all 
     * the properties values
     */
    public final BgeeProperties prop;
   
    /**
     * Constructor
     * 
     * @param prop  An instance of {@code BgeeProperties} to provide the all 
     *              the properties values
     */
    public ViewFactoryProvider(BgeeProperties prop) {
        this.prop = prop;
    }

    /**
     * Return the appropriate {@code ViewFactory} (see {@link DisplayType}),
     * based on the display type requested in the provided {@code requestParameters}.
     *
     * @param response          the {@code HttpServletResponse} where the outputs of the view 
     *                          classes will be written
     * @param requestParameters the {@code RequestParameters} handling the parameters of the 
     *                          current request, 
     *                          to determine the requested displayType, and for display purposes.
     * @return                  the appropriate {@code ViewFactory}.
     * @see org.bgee.view.html.HtmlFactory
     * @see org.bgee.view.xml.XmlFactory
     * @see org.bgee.view.csv.CsvFactory
     * @see org.bgee.view.json.JsonFactory
     * @see org.bgee.controller.URLParameters#DISPLAY_TYPE
     */
    public ViewFactory getFactory(HttpServletResponse response, RequestParameters requestParameters) {        
        log.traceEntry("{}, {}", response, requestParameters);
        DisplayType displayType = DEFAULT;
        
        if (requestParameters.isXmlDisplayType()) {
            displayType = DisplayType.XML;
        } else if (requestParameters.isJsonDisplayType()) {
            displayType = DisplayType.JSON;
        } else if (requestParameters.isCsvDisplayType()) {
            displayType = DisplayType.CSV;
        } else if (requestParameters.isTsvDisplayType()) {
            displayType = DisplayType.TSV;
        }
        return log.traceExit(getFactory(response, displayType, requestParameters));
    }

    /**
     * Return the appropriate {@code ViewFactory} (see {@link DisplayType}), based on 
     * the display type requested by the {@code displayType} parameter.
     * 
     * @param response          the {@code HttpServletResponse} where the outputs of the view 
     *                          classes will be written
     * @param displayType       a {@code DisplayType} specifying the requested display type, 
     *                          corresponding to either 
     *                          {@code HTML}, {@code XML}, {@code TSV}, or {@code CSV}.
     * @param requestParameters the {@code RequestParameters} handling the parameters of the 
     *                          current request, for display purposes.
     * @return                  the appropriate {@code ViewFactory}.
     * @see org.bgee.view.html.HtmlFactory
     * @see org.bgee.view.xml.XmlFactory
     * @see org.bgee.view.csv.CsvFactory
     * @see org.bgee.view.json.JsonFactory
     */
    public ViewFactory getFactory(HttpServletResponse response, 
            DisplayType displayType, RequestParameters requestParameters) {
        log.traceEntry("{}, {}, {}", response, displayType, requestParameters);
        if (displayType == DisplayType.HTML) {
            return new HtmlFactory(response, requestParameters, this.prop);
        }
        if (displayType == DisplayType.XML) {
            return new XmlFactory(response, requestParameters, this.prop);
        }
        if (displayType == DisplayType.CSV) {
            return new CsvFactory(response, requestParameters, this.prop, Delimiter.COMMA);
        }
        if (displayType == DisplayType.TSV) {
            return new CsvFactory(response, requestParameters, this.prop, Delimiter.TAB);
        }
        return log.traceExit(new JsonFactory(response, requestParameters, this.prop));
    }
}
