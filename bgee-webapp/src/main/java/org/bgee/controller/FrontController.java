package org.bgee.controller;

import java.util.Properties;
import java.util.function.Supplier;

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
import org.bgee.model.ServiceFactory;
import org.bgee.view.ErrorDisplay;
import org.bgee.view.ViewFactory;
import org.bgee.view.ViewFactoryProvider;

/**
 * This is the entry point of bgee-webapp. It can be directly mapped as the main servlet in
 * {@code web.xml} and thus responds to a call to the root "/" of the application
 * 
 * @author Mathieu Seppey
 * @author Frederic Bastian
 *
 * @version Bgee 13, Oct. 2015
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
     * A {@code Supplier} of {@code ServiceFactory}s, allowing to obtain a new {@code ServiceFactory} 
     * instance at each call to the {@code doRequest} method.
     */
    private final Supplier<ServiceFactory> serviceFactoryProvider;
    /**
     * The {@code ViewFactoryProvider} instance that will provide the appropriate 
     * {@code ViewFactory} depending on the display type
     */
    private final ViewFactoryProvider viewFactoryProvider ;
    

    /**
     * Default constructor. It will use default implementations for all dependencies 
     * (see {@link #FrontController(BgeeProperties, URLParameters, Supplier, ViewFactoryProvider)}).
     */
    public FrontController() {
        this(null);
    }

    /**
     * Constructor that takes as parameter a {@code java.util.Properties} to create 
     * a {@code BgeeProperties} instance. It will use default implementations for all dependencies 
     * (see {@link #FrontController(BgeeProperties, URLParameters, Supplier, ViewFactoryProvider)}).
     * 
     * @param prop  A {@code java.util.Properties} that will be use to create an instance of
     *              {@code BgeeProperties}
     */
    public FrontController(Properties prop) {
        this(BgeeProperties.getBgeeProperties(prop), null, null, null);
    }

    /**
     * Constructor allowing to inject all dependencies of {@code FrontController}. 
     * Each of this parameter can be {@code null}, in which case the default implementation 
     * is used. 
     * 
     * @param prop                      A {@code BgeeProperties} instance to be used in the whole 
     *                                  application.
     * @param urlParameters             A {@code urlParameters} instance to be used in the whole 
     *                                  application, to be used by {@code RequestParameters} objects 
     *                                  to read/write URLs. 
     * @param serviceFactoryProvider    A {@code Supplier} of {@code ServiceFactory}s, allowing 
     *                                  to obtain a new {@code ServiceFactory} instance 
     *                                  at each call to the {@code doRequest} method. If {@code null}, 
     *                                  the default constructor of {@code ServiceFactory} is used. 
     * @param viewFactoryProvider       A {@code ViewFactoryProvider} instance to provide 
     *                                  the appropriate {@code ViewFactory} depending on the
     *                                  display type. 
     */
    public FrontController(BgeeProperties prop, URLParameters urlParameters, 
            Supplier<ServiceFactory> serviceFactoryProvider, ViewFactoryProvider viewFactoryProvider) {
        log.entry(prop, urlParameters, serviceFactoryProvider, viewFactoryProvider);

        // If the URLParameters object is null, just use a new instance
        this.urlParameters = urlParameters != null? urlParameters: new URLParameters();
        
        // If the bgee prop object is null, just get the default instance from BgeeProperties
        this.prop = prop != null? prop: BgeeProperties.getBgeeProperties();
        
        // If the viewFactoryProvider object is null, just use a new instance, 
        //injecting the properties obtained above. 
        //XXX: if viewFactoryProvider is not null, we currently don't check that it uses 
        //the same BgeeProperties instance. Maybe it's OK to allow to use different BgeeProperties instances? 
        this.viewFactoryProvider = viewFactoryProvider != null? viewFactoryProvider: new ViewFactoryProvider(this.prop);
        
        //If serviceFactoryProvider is null, use default constructor of ServiceFactory
        this.serviceFactoryProvider = serviceFactoryProvider != null? serviceFactoryProvider: 
            ServiceFactory::new;
        
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

        try (ServiceFactory serviceFactory = this.serviceFactoryProvider.get()) {
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
            //this can throw an Exception.
            request.setCharacterEncoding("UTF-8");
            
            CommandParent controller = null;
            if (requestParameters.isTheHomePage()) {
                controller = new CommandHome(response, requestParameters, this.prop, factory, serviceFactory);
            } else if (requestParameters.isADownloadPageCategory()) {
                controller = new CommandDownload(response, requestParameters, this.prop, factory, serviceFactory);
            } else if (requestParameters.isADocumentationPageCategory()) {
                controller = new CommandDocumentation(response, requestParameters, this.prop, factory);
            } else if (requestParameters.isAnAboutPageCategory()) {
                controller = new CommandAbout(response, requestParameters, this.prop, factory);
            } else if (requestParameters.isATopAnatPageCategory()) {
                controller = new CommandTopAnat(response, requestParameters, this.prop, factory, serviceFactory);
            } else {
                throw log.throwing(new PageNotFoundException("Request not recognized."));
            }
            controller.processRequest();
            
        //=== process errors ===
        } catch(RequestParametersNotFoundException e) {
            log.catching(e);
            errorDisplay.displayRequestParametersNotFound(requestParameters.getFirstValue(
                    this.urlParameters.getParamData()));
        } catch(PageNotFoundException e) {
            log.catching(e);
            errorDisplay.displayPageNotFound(e.getMessage());
        } catch(RequestParametersNotStorableException e) {
            log.catching(e);
            errorDisplay.displayRequestParametersNotStorable(e.getMessage());
        } catch(MultipleValuesNotAllowedException e) {
            log.catching(e);
            errorDisplay.displayMultipleParametersNotAllowed(e.getMessage());
        } catch(WrongFormatException e) {
            log.catching(e);
            errorDisplay.displayWrongFormat(e.getMessage());
        } catch(UnsupportedOperationException e) {
            log.catching(e);
            errorDisplay.displayUnsupportedOperationException(e.getMessage());
        } catch(Exception e) {
            log.catching(e);
            if (errorDisplay != null) {
                errorDisplay.displayUnexpectedError();
            } else {
                log.error("Could not display error message to caller.");
            }
        } 
        log.exit();
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        log.entry(request, response);
        doRequest(request, response, false);
        log.exit();
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        log.entry(request, response);
        doRequest(request, response, true);
        log.exit();
    }

}
