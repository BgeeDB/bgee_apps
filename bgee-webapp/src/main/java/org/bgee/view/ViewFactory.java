package org.bgee.view;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.bgee.controller.RequestParameters;
import org.bgee.view.dsv.DsvFactory;
import org.bgee.view.html.HtmlFactory;
import org.bgee.view.xml.XmlFactory;


public abstract class ViewFactory 
{
    public static final int HTML = 1;
    public static final int XML  = 2;
    public static final int CSV  = 3;
    public static final int TSV  = 4;


    public static final int DEFAULT = HTML;

    protected HttpServletResponse response;
    protected RequestParameters requestParameters;

    public ViewFactory(HttpServletResponse response, RequestParameters requestParameters)
    {
        this.response = response;
        this.requestParameters = requestParameters;
    }

    public ViewFactory(HttpServletResponse response)
    {
        this.response = response;
        this.requestParameters = null;
    }

    /**
     * Return the appropriate <code>ViewFactory</code> 
     * (either <code>HtmlFactory</code>, <code>XmlFactory</code>, 
     * or <code>DsvFactory</code>), based on the display type requested 
     * in the provided <code>requestParameters</code>

     * @param response 	the <code>HttpServletResponse</code> where the outputs of the view classes will be written
     * @param requestParameters 	the <code>RequestParameters</code> handling the parameters of the current request, 
     * 								to determine the requested displayType, and for display purposes.
     * @return 	the appropriate <code>ViewFactory</code> 
     * 			(either <code>HtmlFactory</code>, <code>XmlFactory</code>, 
     * 			or <code>DsvFactory</code>)
     * @see org.bgee.view.html.HtmlFactory
     * @see org.bgee.view.xml.XmlFactory
     * @see org.bgee.view.dsv.DsvFactory
     * @see org.bgee.controller.URLParameters#DISPLAY_TYPE
     */
    public static synchronized ViewFactory getFactory(HttpServletResponse response, 
            RequestParameters requestParameters)
    {
        int displayType = DEFAULT;
        if (requestParameters.isXmlDisplayType()) {
            displayType = XML;
        } else if (requestParameters.isCsvDisplayType()) {
            displayType = CSV;
        } else if (requestParameters.isTsvDisplayType()) {
            displayType = TSV;
        }
        return getFactory(response, displayType, requestParameters);
    }

    /**
     * Return the appropriate <code>ViewFactory</code> 
     * (either <code>HtmlFactory</code>, <code>XmlFactory</code>, 
     * or <code>DsvFactory</code>), based on the display type requested 
     * by the <code>displayType</code> parameter.
     * 
     * @param response 	the <code>HttpServletResponse</code> where the outputs of the view classes will be written
     * @param displayType 	an <code>int</code> specifying the requested display type, corresponding to either 
     * 						<code>HTML</code>, <code>XML</code>, <code>TSV</code>, or <code>CSV</code>.
     * @param requestParameters 	the <code>RequestParameters</code> handling the parameters of the current request, 
     * 								for display purposes.
     * @return 	the appropriate <code>ViewFactory</code> 
     * 			(either <code>HtmlFactory</code>, <code>XmlFactory</code>, 
     * 			or <code>DsvFactory</code>)
     * @see #HTML
     * @see #XML
     * @see #TSV
     * @see #CSV
     * @see org.bgee.view.html.HtmlFactory
     * @see org.bgee.view.xml.XmlFactory
     * @see org.bgee.view.dsv.DsvFactory
     */
    public static synchronized ViewFactory getFactory(HttpServletResponse response, 
            int displayType, 
            RequestParameters requestParameters)
    {
        if (displayType == XML) {
            return new XmlFactory(response);
        }
        if (displayType == CSV) {
            return new DsvFactory(response, ",");
        }
        if (displayType == TSV) {
            return new DsvFactory(response, "\t");
        }
        return new HtmlFactory(response, requestParameters);
    }

    public abstract GeneralDisplay getGeneralDisplay() throws IOException;

    public abstract DownloadDisplay getDownloadDisplay() throws IOException;

}
