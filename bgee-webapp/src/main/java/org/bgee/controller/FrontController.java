package org.bgee.controller;

import java.io.IOException;
import java.util.Properties;
import java.util.function.Supplier;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.exception.MultipleValuesNotAllowedException;
import org.bgee.controller.exception.PageNotFoundException;
import org.bgee.controller.exception.RequestParametersNotFoundException;
import org.bgee.controller.exception.RequestParametersNotStorableException;
import org.bgee.controller.exception.RequestSizeExceededException;
import org.bgee.controller.exception.ValueSizeExceededException;
import org.bgee.controller.servletutils.BgeeHttpServletRequest;
import org.bgee.controller.utils.MailSender;
import org.bgee.controller.exception.InvalidFormatException;
import org.bgee.controller.exception.InvalidRequestException;
import org.bgee.model.ServiceFactory;
import org.bgee.view.ErrorDisplay;
import org.bgee.view.ViewFactory;
import org.bgee.view.ViewFactoryProvider;
import org.bgee.view.ViewFactoryProvider.DisplayType;

/**
 * This is the entry point of bgee-webapp. It can be directly mapped as the main servlet in
 * {@code web.xml} and thus responds to a call to the root "/" of the application
 * 
 * @author Mathieu Seppey
 * @author Frederic Bastian
 *
 * @version Bgee 13, Dec. 2015
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
     * The {@code MailSender} used to send emails.
     */
    private final MailSender mailSender;
    

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
        this(BgeeProperties.getBgeeProperties(prop), null, null, null, null);
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
     * @param mailSender                A {@code MailSender} instance used to send mails to users.
     */
    public FrontController(BgeeProperties prop, URLParameters urlParameters, 
            Supplier<ServiceFactory> serviceFactoryProvider, ViewFactoryProvider viewFactoryProvider, 
            MailSender mailSender) {
        log.entry(prop, urlParameters, serviceFactoryProvider, viewFactoryProvider, mailSender);

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
        
        MailSender checkMailSender = null;
        if (mailSender != null) {
            checkMailSender = mailSender;
        } else {
            try {
                checkMailSender = new MailSender(this.prop);
                log.debug("Got parameters to send mails.");
            } catch (IllegalArgumentException e) {
                //if the properties does not allow to send mail, it's fine, swallow the exception
                log.catching(Level.DEBUG, e);
                log.debug("No parameter allowing to send mails.");
            }
        }
        MailSender.setWaitTimeInMs(this.prop.getMailWaitTime());
        this.mailSender = checkMailSender;
        
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
        
        //default display type in case of error before we can do anything.
        DisplayType displayType = DisplayType.HTML;

        try (ServiceFactory serviceFactory = this.serviceFactoryProvider.get()) {
            //First, to handle errors, we "manually" determine the display type, 
            //because loading all the parameters in RequestParameters could throw an exception.
            displayType = this.getRequestedDisplayType(request);

            //OK, now we try to get the view requested.
            request.setCharacterEncoding("UTF-8"); 
            RequestParameters requestParameters = new RequestParameters(request, this.urlParameters, 
                    this.prop, true, "&");
            log.debug("Analyzed URL: " + requestParameters.getRequestURL());
            ViewFactory factory = this.viewFactoryProvider.getFactory(response, requestParameters);
            
            //now we process the request
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
                controller = new CommandTopAnat(response, requestParameters, this.prop, factory, 
                        serviceFactory, this.getServletContext(), this.mailSender);
            } else if (requestParameters.isAJobPageCategory()) {
                controller = new CommandJob(response, requestParameters, this.prop, factory, 
                        serviceFactory);
            } else {
                throw log.throwing(new PageNotFoundException("Request not recognized."));
            }
            controller.processRequest();
            
        //=== process errors ===
        } catch (Exception e) {
            log.catching(e);
            //get an ErrorDisplay of the appropriate display type. 
            //We don't acquire the ErrorDisplay before any Exception is thrown, 
            //because we might need to use the response outputstream directly; 
            //acquiring a view calls 'getWriter', which prevents further use 
            //of 'getOutputStream'.
            //this is also why we don't use multiple try-catch clauses.
            ErrorDisplay errorDisplay = null;
            try {
                //default request parameters
                RequestParameters rp = new RequestParameters(this.urlParameters, 
                        this.prop, true, "&");
                errorDisplay = this.viewFactoryProvider.getFactory(response, displayType, rp)
                        .getErrorDisplay();
            } catch (IOException e1) {
                e1.initCause(e);
                e = e1;
            }
            if (errorDisplay == null) {
                log.error("Could not display error message to caller: {}", e);
            }
            
            if (e instanceof InvalidFormatException) {
                errorDisplay.displayControllerException((InvalidFormatException) e);
            } else if (e instanceof InvalidRequestException) {
                errorDisplay.displayControllerException((InvalidRequestException) e);
            } else if (e instanceof MultipleValuesNotAllowedException) {
                errorDisplay.displayControllerException((MultipleValuesNotAllowedException) e);
            } else if (e instanceof PageNotFoundException) {
                errorDisplay.displayControllerException((PageNotFoundException) e);
            } else if (e instanceof RequestParametersNotFoundException) {
                errorDisplay.displayControllerException((RequestParametersNotFoundException) e);
            } else if (e instanceof RequestParametersNotStorableException) {
                errorDisplay.displayControllerException((RequestParametersNotStorableException) e);
            } else if (e instanceof RequestSizeExceededException) {
                errorDisplay.displayControllerException((RequestSizeExceededException) e);
            } else if (e instanceof ValueSizeExceededException) {
                errorDisplay.displayControllerException((ValueSizeExceededException) e);
            } else if (e instanceof UnsupportedOperationException) {
                errorDisplay.displayUnsupportedOperationException();
            } else {
                errorDisplay.displayUnexpectedError();
            } 
        } 
        
        log.exit();
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        log.entry(request, response);
        log.debug("doGet");
        doRequest(request, response, false);
        log.exit();
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        log.entry(request, response);
        log.debug("doPost");
        doRequest(request, response, true);
        log.exit();
    }

    /**
     * Retrieve from {@code request} the requested display type. This methods only tries 
     * to retrieve the display type, and do not examine/validate other parameters, 
     * which could throw an exception before acquiring any view. Default display type 
     * is HTML.
     * 
     * @param request   A {@code HttpServletRequest} that is the request coming from the client
     * @return          The requested {@code DisplayType}, or {@code null} if no specific one 
     *                  was requested.
     * @throws InvalidRequestException  If several display types were requested. 
     */
    private DisplayType getRequestedDisplayType(HttpServletRequest request) throws InvalidRequestException {
        log.entry(request);
        
        String[] paramValues = request.getParameterValues(
                this.urlParameters.getParamDisplayType().getName());
        if (paramValues == null || paramValues.length == 0) {
            return log.exit(null);
        }
        if (paramValues.length > 1) {
            throw log.throwing(new InvalidRequestException("It is not possible to request "
                    + "several display types"));
        }
        
        String fakeQueryString = this.urlParameters.getParamDisplayType().getName() + 
                "=" + paramValues[0];
        HttpServletRequest fakeRequest = new BgeeHttpServletRequest(fakeQueryString, 
                RequestParameters.CHAR_ENCODING);
        try {
            RequestParameters fakeParams = new RequestParameters(fakeRequest, this.urlParameters, 
                    this.prop, true, "&");
            if (fakeParams.isXmlDisplayType()) {
                return log.exit(DisplayType.XML);
            } else if (fakeParams.isTsvDisplayType()) {
                return log.exit(DisplayType.TSV);
            } else if (fakeParams.isCsvDisplayType()) {
                return log.exit(DisplayType.CSV);
            } else if (fakeParams.isJsonDisplayType()) {
                return log.exit(DisplayType.JSON);
            } else {
                return log.exit(DisplayType.HTML);
            }
        } catch (MultipleValuesNotAllowedException | RequestParametersNotFoundException e) {
            throw log.throwing(new AssertionError("Error, code block supposed to be unreachable", e));
        }
    }
}
