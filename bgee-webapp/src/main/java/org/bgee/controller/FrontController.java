package org.bgee.controller;

import java.util.Properties;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.exception.MultipleValuesNotAllowedException;
import org.bgee.controller.exception.PageNotFoundException;
import org.bgee.controller.exception.RequestParametersNotFoundException;
import org.bgee.controller.exception.RequestParametersNotStorableException;
import org.bgee.controller.exception.WrongFormatException;
import org.bgee.view.ErrorDisplay;
import org.bgee.view.ViewFactory;
import org.bgee.view.ViewFactoryProvider;

/**
 * This is the entry point of bgee-webapp. It can be directly mapped as the main servlet in
 * {@code web.xml}
 * and thus responds to a call to the root "/" of the application
 * 
 * @author Mathieu Seppey
 * @author Frederic Bastian
 *
 * @version Bgee 13, Aug 2014
 * @since Bgee 13
 */
public class FrontController extends HttpServlet {

    private final static Logger log = LogManager.getLogger(FrontController.class.getName());

    /**
     * The serialVersionUID is needed, as this class extends the HttpServlet, 
     * which is serializable.
     */
    private static final long serialVersionUID = 2022651427006588913L;

    /**
     * The {@code BgeeProperties} instance that will be used in the whole application
     * and re injected in all classes that will need it eventually.
     */
    private final BgeeProperties prop ;

    /**
     * The {@code URLParameters} instance that will provide the parameters list available 
     * within the application
     */
    private final URLParameters urlParameters ;

    /**
     * The {@code ViewFactoryProvider} instance that will provide the appropriate 
     * {@code ViewFactory} depending on the display type
     */
    private final ViewFactoryProvider viewFactoryProvider ;

    /**
     * Default constructor. It will use the default {@code BgeeProperties} class,
     * the default {@code URLParameters} class and the default {@code ViewFactoryProvider} class.
     * 
     * @see BgeeProperties
     * @see URLParameters
     * @see ViewFactory
     */
    public FrontController() {
        this(null, null, null);
    }

    /**
     * Constructor that takes as parameter a {@code java.util.Properties} instance that
     * will be used to create a custom {@code BgeeProperties} instance.
     * It will use the default {@code URLParameters} class and
     * the default {@code ViewFactoryProvider} class.
     * 
     * @param prop  A {@code java.util.Properties} that will be use to get an instance of
     *              {@code BgeeProperties}
     *  
     * @see BgeeProperties
     * @see URLParameters
     */
    public FrontController(Properties prop) {
        this(BgeeProperties.getBgeeProperties(prop), null, null);
    }

    /**
     * Constructor that takes as parameters a custom {@code BgeeProperties} instance, a custom 
     * {@code URLParameters} instance and a custom {@code viewFactoryProvider} that will be 
     * injected further in all classes that use them.
     * 
     * @param prop                  A {@code BgeeProperties} instance to be used in the whole 
     *                              application and injected in all classes that will need it
     *                              eventually.
     *                              
     * @param urlParameters         A {@code urlParameters} instance to be used in the whole 
     *                              application and injec
     * 
     * @param viewFactoryProvider   A {@code ViewFactoryProvider} instance to provide 
     *                              the appropriate {@code ViewFactory} depending on the
     *                              display type
     *
     *
     * @see BgeeProperties
     * @see URLParameters
     */
    public FrontController(BgeeProperties prop, URLParameters urlParameters, 
            ViewFactoryProvider viewFactoryProvider) {
        log.entry(prop, urlParameters, viewFactoryProvider);
        if(prop == null){
            // If the bgee prop object is null, just get the default instance from BgeeProperties
            this.prop = BgeeProperties.getBgeeProperties();
        }
        else{
            this.prop = prop;

        }
        if(urlParameters == null){
            // If the URLParamters object is null, just use a new instance
            this.urlParameters = new URLParameters();
        }
        else{
            this.urlParameters = urlParameters;
        }
        if(viewFactoryProvider == null){
            // If the viewFactoryProvider object is null, just use a new instance
            this.viewFactoryProvider = new ViewFactoryProvider(this.prop);
        }
        else{
            this.viewFactoryProvider = viewFactoryProvider;
        }
        log.exit();
    }

    /**
     * Method that handles a http request and call the specific controller and view.
     * It also call the correct view when an {@code Exception} is thrown to display
     * an error message
     * 
     * @param request   A {@code HttpServletRequest} that is the request coming from the client
     * @param response  A {@code HttpServletResponse} that will be used to display the page to 
     *                  the client
     * @param postData  A {@code boolean} that indicates whether the request method is POST
     */
    public void doRequest(HttpServletRequest request, HttpServletResponse response, 
            boolean postData) { 
        log.entry(request, response, postData);
        //in order to display error message in catch clauses
        //we get  "fake" RequestParameters so that no exception is thrown already.
        RequestParameters requestParameters = new RequestParameters(
                this.urlParameters, this.prop, true, "&");
        //in this variable, we will first store the default HTML ErrorDisplay, 
        //in case we cannot acquire an ErrorDisplay for the requested view, 
        //then we will try to acquire the appropriate ErrorDisplay. 
        ErrorDisplay errorDisplay = null;

        try {
            //in order to display error message in catch clauses. 
            //we do it in the try clause, because getting a view can throw an IOException.
            //so here we get the default view from the default factory before any exception 
            //can be thrown.
            ViewFactory factory = this.viewFactoryProvider.getFactory(response, requestParameters);
            errorDisplay = factory.getErrorDisplay();
            
            //OK, now we try to get the view requested. If an error occurred, 
            //the HTML view will allow to display an error message anyway
            requestParameters = new RequestParameters(request, this.urlParameters, this.prop,
                    true, "&");
            log.info("Analyzed URL: " + requestParameters.getRequestURL());
            factory = this.viewFactoryProvider.getFactory(response, requestParameters);
            errorDisplay = factory.getErrorDisplay();
            
            //Set character encoding after acquiring an ErrorDisplay, 
            //this can thrown an Exception.
            request.setCharacterEncoding("UTF-8");
            
            CommandParent controller = null;
            if (requestParameters.isTheHomePage()) {
                controller = new CommandHome(response, requestParameters, this.prop, factory);
            } else if (requestParameters.isADownloadPageCategory()) {
                controller = new CommandDownload(response, requestParameters, this.prop, factory);
            } else if (requestParameters.isADocumentationPageCategory()) {
                controller = new CommandDocumentation(response, requestParameters, this.prop, factory);
            } else if (requestParameters.isAnAboutPageCategory()) {
                controller = new CommandAbout(response, requestParameters, this.prop, factory);
            } else if (requestParameters.isATopAnatPageCategory()) {
                controller = new CommandTopAnat(response, requestParameters, this.prop, factory);
            } else {
                throw log.throwing(new PageNotFoundException("Request not recognized."));
            }
            controller.processRequest();
            
        //=== process errors ===
        } catch(RequestParametersNotFoundException e) {
            errorDisplay.displayRequestParametersNotFound(requestParameters.getFirstValue(
                    this.urlParameters.getParamData()));
            log.error("RequestParametersNotFoundException", e);
        } catch(PageNotFoundException e) {
            errorDisplay.displayPageNotFound(e.getMessage());
            log.error("PageNotFoundException", e);
        } catch(RequestParametersNotStorableException e) {
            errorDisplay.displayRequestParametersNotStorable(e.getMessage());
            log.error("RequestParametersNotStorableException", e);
        } catch(MultipleValuesNotAllowedException e) {
            errorDisplay.displayMultipleParametersNotAllowed(e.getMessage());
            log.error("MultipleValuesNotAllowedException", e);
        } catch(WrongFormatException e) {
            errorDisplay.displayWrongFormat(e.getMessage());
            log.error("WrongFormatException", e);
        } catch(UnsupportedOperationException e) {
            errorDisplay.displayUnsupportedOperationException(e.getMessage());
            log.error("UnsupportedOperationException", e);
        } catch(Exception e) {
            if (errorDisplay != null) {
                errorDisplay.displayUnexpectedError();
            }
            log.error("Other Exception", e);
        } finally {
            // Remove the bgee properties instance from the pool
            this.prop.removeFromBgeePropertiesPool();
        }
        log.exit();
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        doRequest(request, response, false);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        doRequest(request, response, true);
    }

}
