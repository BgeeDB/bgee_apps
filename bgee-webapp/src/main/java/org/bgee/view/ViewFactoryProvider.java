package org.bgee.view;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.RequestParameters;
import org.bgee.view.dsv.DsvFactory;
import org.bgee.view.html.HtmlFactory;
import org.bgee.view.xml.XmlFactory;

/**
 * This class provide the appropriate {@code ViewFactory} depending on the {@code DisplayTypes}
 * 
 * @author Mathieu Seppey
 * @version Bgee 13 Aug 2014
 * @since   Bgee 13
 */
public class ViewFactoryProvider
{
    
    private final static Logger log = LogManager.getLogger(ViewFactoryProvider.class.getName());
    
    /**
     * An {@code enum} of the different display types
     */
    public static enum displayTypes {
        HTML, XML, CSV, TSV;
    }
    
    /**
     * The {@code displayTypes} used as default
     */
    public static final displayTypes DEFAULT = displayTypes.HTML;
   
    /**
     * Constructor
     */
    public ViewFactoryProvider()
    {

    }

    /**
     * Return the appropriate {@code ViewFactory} 
     * (either {@code HtmlFactory}, {@code XmlFactory}, 
     * or {@code DsvFactory}), based on the display type requested 
     * in the provided {@code requestParameters}
     *
     * @param response          the {@code HttpServletResponse} where the outputs of the view 
     *                          classes will be written
     * @param requestParameters the {@code RequestParameters} handling the parameters of the 
     *                          current request, 
     *                          to determine the requested displayType, and for display purposes.
     * @return  the appropriate {@code ViewFactory} (either {@code HtmlFactory},
     *          {@code XmlFactory}, or {@code DsvFactory})
     *          
     * @see org.bgee.view.html.HtmlFactory
     * @see org.bgee.view.xml.XmlFactory
     * @see org.bgee.view.dsv.DsvFactory
     * @see org.bgee.controller.URLParameters#DISPLAY_TYPE
     */
    public ViewFactory getFactory(HttpServletResponse response, 
            RequestParameters requestParameters)
    {        
        log.entry(response, requestParameters);
        displayTypes displayType = DEFAULT;
        if (requestParameters.isXmlDisplayType()) {
            displayType = displayTypes.XML;
        } else if (requestParameters.isCsvDisplayType()) {
            displayType = displayTypes.CSV;
        } else if (requestParameters.isTsvDisplayType()) {
            displayType = displayTypes.TSV;
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
     * @param displayType       an {@code int} specifying the requested display type, 
     *                          corresponding to either 
     *                          {@code HTML}, {@code XML}, {@code TSV}, or {@code CSV}.
     * @param requestParameters the {@code RequestParameters} handling the parameters of the 
     *                          current request, for display purposes.
     * @return     the appropriate {@code ViewFactory}(either {@code HtmlFactory},
     *             {@code XmlFactory}, or {@code DsvFactory})

     * @see org.bgee.view.html.HtmlFactory
     * @see org.bgee.view.xml.XmlFactory
     * @see org.bgee.view.dsv.DsvFactory
     */
    protected synchronized ViewFactory getFactory(HttpServletResponse response, 
            displayTypes displayType, 
            RequestParameters requestParameters)
    {
        log.entry(response, displayType, requestParameters);
        if (displayType == displayTypes.XML) {
            return new XmlFactory(response, requestParameters);
        }
        if (displayType == displayTypes.CSV) {
            return new DsvFactory(response, ",", requestParameters);
        }
        if (displayType == displayTypes.TSV) {
            return new DsvFactory(response, "\t", requestParameters);
        }
        return log.exit(new HtmlFactory(response, requestParameters));
    }

}
