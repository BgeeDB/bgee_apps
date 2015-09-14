package org.bgee.view;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.view.dsv.DsvFactory;
import org.bgee.view.html.HtmlFactory;
import org.bgee.view.json.JsonFactory;
import org.bgee.view.xml.XmlFactory;

/**
 * This class provide the appropriate {@code ViewFactory} depending on the {@code DisplayType}
 * 
 * @author Mathieu Seppey
 * @version Bgee 13 Aug 2014
 * @since   Bgee 13
 */
public class ViewFactoryProvider {
    
    private final static Logger log = LogManager.getLogger(ViewFactoryProvider.class.getName());
    
    /**
     * An {@code enum} of the different display types
     */
    public static enum DisplayType {
        HTML, XML, JSON, CSV, TSV;
    }
    
    /**
     * The {@code DisplayType} used as default
     */
    public static final DisplayType DEFAULT = DisplayType.HTML;
    
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
     * Return the appropriate {@code ViewFactory} (either {@code HtmlFactory}, {@code XmlFactory}, 
     * or {@code DsvFactory}), based on the display type requested 
     * in the provided {@code requestParameters}
     *
     * @param response          the {@code HttpServletResponse} where the outputs of the view 
     *                          classes will be written
     * @param requestParameters the {@code RequestParameters} handling the parameters of the 
     *                          current request, 
     *                          to determine the requested displayType, and for display purposes.
     * @return                  the appropriate {@code ViewFactory} (either {@code HtmlFactory},
     *                          {@code XmlFactory}, or {@code DsvFactory})
     * @see org.bgee.view.html.HtmlFactory
     * @see org.bgee.view.xml.XmlFactory
     * @see org.bgee.view.dsv.DsvFactory
     * @see org.bgee.controller.URLParameters#DISPLAY_TYPE
     */
    public ViewFactory getFactory(HttpServletResponse response, RequestParameters requestParameters) {        
        log.entry(response, requestParameters);
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
        return log.exit(getFactory(response, displayType, requestParameters));
    }

    /**
     * Return the appropriate {@code ViewFactory} (either {@code HtmlFactory}, 
     * {@code XmlFactory}, or {@code DsvFactory}), based on the display type requested 
     * by the {@code displayType} parameter.
     * 
     * @param response          the {@code HttpServletResponse} where the outputs of the view 
     *                          classes will be written
     * @param displayType       a {@code DisplayType} specifying the requested display type, 
     *                          corresponding to either 
     *                          {@code HTML}, {@code XML}, {@code TSV}, or {@code CSV}.
     * @param requestParameters the {@code RequestParameters} handling the parameters of the 
     *                          current request, for display purposes.
     * @return                  the appropriate {@code ViewFactory}(either {@code HtmlFactory},
     *                          {@code XmlFactory}, or {@code DsvFactory})
     * @see org.bgee.view.html.HtmlFactory
     * @see org.bgee.view.xml.XmlFactory
     * @see org.bgee.view.dsv.DsvFactory
     */
    protected synchronized ViewFactory getFactory(HttpServletResponse response, 
            DisplayType displayType, RequestParameters requestParameters) {
        log.entry(response, displayType, requestParameters);
        if (displayType == DisplayType.JSON) {
            return new JsonFactory(response, requestParameters, this.prop);
        }
        if (displayType == DisplayType.XML) {
            return new XmlFactory(response, requestParameters, this.prop);
        }
        if (displayType == DisplayType.CSV) {
            return new DsvFactory(response, ",", requestParameters, this.prop);
        }
        if (displayType == DisplayType.TSV) {
            return new DsvFactory(response, "\t", requestParameters, this.prop);
        }
        return log.exit(new HtmlFactory(response, requestParameters, this.prop));
    }
}
