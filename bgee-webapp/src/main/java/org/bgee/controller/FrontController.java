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
import org.bgee.controller.user.User;
import org.bgee.controller.user.UserService;
import org.bgee.controller.utils.MailSender;
import org.bgee.controller.exception.InvalidFormatException;
import org.bgee.controller.exception.InvalidRequestException;
import org.bgee.controller.exception.JobResultNotFoundException;
import org.bgee.model.ServiceFactory;
import org.bgee.model.dao.api.exception.QueryInterruptedException;
import org.bgee.model.gene.GeneNotFoundException;
import org.bgee.model.job.JobService;
import org.bgee.model.job.exception.TooManyJobsException;
import org.bgee.view.ErrorDisplay;
import org.bgee.view.ViewFactory;
import org.bgee.view.ViewFactoryProvider;
import org.bgee.view.ViewFactoryProvider.DisplayType;

/**
 * This is the entry point of bgee-webapp. It can be directly mapped as the main servlet in
 * {@code web.xml} and thus responds to a call to the root "/" of the application
 *
 * @author  Mathieu Seppey
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 14, May 2019
 * @since   Bgee 13, June 2014
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
    private final BgeeProperties prop;
    /**
     * The {@code URLParameters} instance that will provide the parameters list available 
     * within the application
     */
    private final URLParameters urlParameters;
    /**
     * The {@code JobService} instance that will allow to manage jobs between threads 
     * across the entire webapp. 
     */
    private final JobService jobService;
    /**
     * The {@link UserService} instance allowing to create {@code User} objects to identify 
     * and track users in the webapp.
     */
    private final UserService userService;
    /**
     * A {@code Supplier} of {@code ServiceFactory}s, allowing to obtain a new {@code ServiceFactory} 
     * instance at each call to the {@code doRequest} method.
     */
    private final Supplier<ServiceFactory> serviceFactoryProvider;
    /**
     * The {@code ViewFactoryProvider} instance that will provide the appropriate 
     * {@code ViewFactory} depending on the display type
     */
    private final ViewFactoryProvider viewFactoryProvider;
    /**
     * The {@code MailSender} used to send emails.
     */
    private final MailSender mailSender;
    

    /**
     * Default constructor. It will use default implementations for all dependencies 
     * (see {@link FrontController(BgeeProperties, URLParameters, JobService, UserService,
     * GeneMatchResultService, Supplier, ViewFactoryProvider, MailSender)}).
     */
    public FrontController() {
        this(null);
    }

    /**
     * Constructor that takes as parameter a {@code java.util.Properties} to create 
     * a {@code BgeeProperties} instance. It will use default implementations for all dependencies 
     * (see {@link FrontController(BgeeProperties, URLParameters, JobService, UserService,
     * GeneMatchResultService, Supplier, ViewFactoryProvider, MailSender)}).
     * 
     * @param prop  A {@code java.util.Properties} that will be use to create an instance of
     *              {@code BgeeProperties}
     */
    public FrontController(Properties prop) {
        this(BgeeProperties.getBgeeProperties(prop), null, null, null, null, null, null);
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
     * @param jobService                A {@code JobService} instance allowing to manage jobs 
     *                                  between threads across the entire webapp.
     * @param userService               A {@link UserService} instance, allowing to create
     *                                  {@code User} objects to identify and track users in the webapp. 
     *                                  If {@code null}, the default constructor of {@code UserService} is used.  
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
            JobService jobService, UserService userService,
            Supplier<ServiceFactory> serviceFactoryProvider, ViewFactoryProvider viewFactoryProvider, 
            MailSender mailSender) {
        log.traceEntry("{}, {}, {}, {}, {}, {}, {}",prop, urlParameters, jobService, userService, 
                serviceFactoryProvider, viewFactoryProvider, mailSender);

        // If the URLParameters object is null, just use a new instance
        this.urlParameters = urlParameters != null? urlParameters: new URLParameters();
        
        // If the bgee prop object is null, just get the default instance from BgeeProperties
        this.prop = prop != null? prop: BgeeProperties.getBgeeProperties();

        //If serviceFactoryProvider is null, use default constructor of ServiceFactory
        this.serviceFactoryProvider = serviceFactoryProvider != null? serviceFactoryProvider: 
            ServiceFactory::new;
        
        this.jobService  = jobService != null? jobService: new JobService(this.prop);
        this.userService = userService != null? userService: new UserService();
        
        // If the viewFactoryProvider object is null, just use a new instance, 
        //injecting the properties obtained above. 
        //XXX: if viewFactoryProvider is not null, we currently don't check that it uses 
        //the same BgeeProperties instance. Maybe it's OK to allow to use different BgeeProperties instances? 
        this.viewFactoryProvider = viewFactoryProvider != null? viewFactoryProvider: new ViewFactoryProvider(this.prop);

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
        
        log.traceExit();
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
    public void doRequest(final HttpServletRequest request, final HttpServletResponse response,
            final boolean postData) {
        log.traceEntry("{}, {}, {}", request, response, postData);
        
        //default display type in case of error before we can do anything.
        DisplayType displayType = DisplayType.JSON;
        //default RequestParameters in case of errors
        RequestParameters requestParameters = new RequestParameters(this.urlParameters,
                this.prop, true, "&");

        try (ServiceFactory serviceFactory = this.serviceFactoryProvider.get()) {
            //First, to handle errors, we "manually" determine the display type, 
            //because loading all the parameters in RequestParameters could throw an exception.
            //This displayType will thus have a chance of being used in the catch clause.
            displayType = this.getRequestedDisplayType(request);

            //Now, try to load and analyze the request parameters
            request.setCharacterEncoding(RequestParameters.CHAR_ENCODING); 
            requestParameters = new RequestParameters(request, this.urlParameters,
                    this.prop, true, "&");
            log.debug("Analyzed URL: {} - POST data? {}", requestParameters.getRequestURL(), postData);
            
            //Load a User instance to track users between requests
            User user = this.userService.createNewUser(request, requestParameters);
            //If needed we'll set a tracking cookie, unless it is inappropriate for the requested page 
            //(e.g., response success no content)
            boolean setCookie = true;
            
            //Now we try to get the view factory requested.
            ViewFactory factory = this.viewFactoryProvider.getFactory(response, requestParameters);
            
            //now we process the request
            CommandParent controller = null;

            //if this is not an AJAX request, and the data are submitted by POST method, 
            //then we need to redirect the user (to avoid an annoying message when pressing 'back', 
            //and also to try to put all requested parameters into the URL if they are not too long, 
            //otherwise, and ID allowing to retrieve the parameters will be added to the URL 
            //(see RequestParameters#getRequestURI()))

            //get the requested URI, trying to put all parameters in the URL
            //get the URI without URLencoding it, because it will be done by the method 
            //<code>encodeRedirectURL</code> of the <code>HttpServletResponse</code>

            //encodeRedirectURL is supposed to be the way of properly redirecting users, 
            //but it actually does not encode \n, so, forget it... we provide an url already URL encoded
            //and we do not care about sessionid passed by URL anyway.
            //so finally, we do not use this.requestParameters.getRequestURL(false) anymore
            if (requestParameters.isPostFormSubmit()) {
                controller = new CommandRedirect(response, requestParameters, this.prop, factory, serviceFactory);

            } else if (requestParameters.isADownloadPageCategory()) {
                controller = new CommandDownload(response, requestParameters, this.prop, factory, serviceFactory);
                
            } else if (requestParameters.isATopAnatPageCategory()) {
                controller = new CommandTopAnat(response, requestParameters, this.prop, factory, 
                        serviceFactory, this.jobService, user, this.getServletContext(), this.mailSender);
                
            } else if (requestParameters.isAJobPageCategory()) {
                controller = new CommandJob(response, requestParameters, this.prop, factory, 
                        serviceFactory, this.jobService, user);
                
            } else if (requestParameters.isAGenePageCategory()){
                controller = new CommandGene(response, requestParameters, this.prop, factory,
                        serviceFactory);

            } else if (requestParameters.isAExprComparisonPageCategory()){
                controller = new CommandExpressionComparison(response, requestParameters, this.prop, factory, serviceFactory);

            } else if (requestParameters.isADataPageCategory()){
                controller = new CommandData(response, requestParameters, this.prop, factory, serviceFactory,
                        this.jobService, user);

            } else if (requestParameters.isASourcePageCategory()){
                controller = new CommandSource(response, requestParameters, this.prop, factory, serviceFactory);
                
            } else if (requestParameters.isASpeciesPageCategory()){
                controller = new CommandSpecies(response, requestParameters, this.prop, factory, serviceFactory);
                
            } else if (requestParameters.isASearchPageCategory()) {
            		controller = new CommandSearch(response, requestParameters, this.prop, factory,
                            serviceFactory);
            } else if (requestParameters.isARPackagePageCategory()) {
                controller = new CommandRPackage(response, requestParameters, this.prop, factory, 
                        serviceFactory, this.jobService, user);
                
            } else if (requestParameters.isAAnatSimilarityPageCategory()) {
                controller = new CommandAnatomicalSimilarity(
                        response, requestParameters, this.prop, factory, serviceFactory);
                
            } else {
                throw log.throwing(new PageNotFoundException("Request not recognized."));
            }
            if (controller != null) {
                controller.processRequest();
            }
            
            //only after the processing we set the tracking cookie: some responses (e.g., redirection) 
            //might need to set some headers of the response first. And also, we don't want 
            //to set the cookie in case of error
            if (setCookie) {
                try {
                    user.manageTrackingCookie(request, this.prop.getBgeeRootDomain())
                    .ifPresent(c -> response.addCookie(c));
                } catch (IllegalArgumentException e) {
                    //we are not going to make the query fail for a cookie issue
                    log.catching(Level.DEBUG, e);
                }
            }
            
        //=== process errors ===
        } catch (Exception e) {
            Throwable realException = e.getCause() != null && 
                    (e.getCause() instanceof TooManyJobsException || 
                    e.getCause() instanceof QueryInterruptedException || 
                    e.getCause() instanceof JobResultNotFoundException)? e.getCause(): e;
            Level logLevel = Level.ERROR;
            if (realException instanceof QueryInterruptedException || 
                    realException instanceof JobResultNotFoundException || 
                    realException instanceof TooManyJobsException) {
                logLevel = Level.DEBUG;
            }
            log.catching(logLevel, realException);
            //to know the URL of the error
            //Retrieve the URL in a try-catch to make sure it cannot create any more problems.
            //We still use the method 'catching' on the previous line, so that the exception is caught
            //with the proper log4j2 Marker "CATCHING".
            try {
                String url = requestParameters.getRequestURL();
                log.log(logLevel, "URL requested {} for Exception {}", url, e.getStackTrace());
            } catch (Exception eUrl) {
                //We'll just do nothing in that case
                log.catching(eUrl);
            }
            
            //get an ErrorDisplay of the appropriate display type. 
            //We don't acquire the ErrorDisplay before any Exception is thrown, 
            //because we might need to use the response outputstream directly; 
            //acquiring a view calls 'getWriter', which prevents further use 
            //of 'getOutputStream'.
            //this is also why we don't use multiple try-catch clauses.
            ErrorDisplay errorDisplay = null;
            try {
                errorDisplay = this.viewFactoryProvider.getFactory(response, displayType, requestParameters)
                        .getErrorDisplay();
            } catch (IOException e1) {
                e1.initCause(realException);
                realException = e1;
            }
            if (errorDisplay == null) {
                log.error("Could not display error message to caller: {}", realException);
                log.traceExit();
                return;
            }
            
            if (realException instanceof InvalidFormatException) {
                errorDisplay.displayControllerException((InvalidFormatException) realException);
            } else if (realException instanceof InvalidRequestException) {
                errorDisplay.displayControllerException((InvalidRequestException) realException);
            } else if (realException instanceof MultipleValuesNotAllowedException) {
                errorDisplay.displayControllerException((MultipleValuesNotAllowedException) realException);
            } else if (realException instanceof PageNotFoundException) {
                errorDisplay.displayControllerException((PageNotFoundException) realException);
            } else if (realException instanceof RequestParametersNotFoundException) {
                errorDisplay.displayControllerException((RequestParametersNotFoundException) realException);
            } else if (realException instanceof RequestParametersNotStorableException) {
                errorDisplay.displayControllerException((RequestParametersNotStorableException) realException);
            } else if (realException instanceof RequestSizeExceededException) {
                errorDisplay.displayControllerException((RequestSizeExceededException) realException);
            } else if (realException instanceof ValueSizeExceededException) {
                errorDisplay.displayControllerException((ValueSizeExceededException) realException);
            } else if (realException instanceof UnsupportedOperationException) {
                errorDisplay.displayUnsupportedOperationException();
            } else if (realException instanceof TooManyJobsException) {
                errorDisplay.displayControllerException((TooManyJobsException) realException);
            } else if (realException instanceof GeneNotFoundException) {
                errorDisplay.displayControllerException(new InvalidRequestException(e.getMessage()));
            } else {
                errorDisplay.displayUnexpectedError();
            } 
        } 
        
        log.traceExit();
    }

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) {
        log.traceEntry("{}, {}", request, response);
        doRequest(request, response, false);
        log.traceExit();
    }

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) {
        log.traceEntry("{}, {}", request, response);
        doRequest(request, response, true);
        log.traceExit();
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
        log.traceEntry("{}", request);
        
        String[] paramValues = request.getParameterValues(
                this.urlParameters.getParamDisplayType().getName());
        if (paramValues == null || paramValues.length == 0) {
            return log.traceExit((DisplayType) null);
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
                return log.traceExit(DisplayType.XML);
            } else if (fakeParams.isTsvDisplayType()) {
                return log.traceExit(DisplayType.TSV);
            } else if (fakeParams.isCsvDisplayType()) {
                return log.traceExit(DisplayType.CSV);
            } else if (fakeParams.isHtmlDisplayType()) {
                return log.traceExit(DisplayType.HTML);
            } else {
                return log.traceExit(DisplayType.JSON);
            }
        } catch (MultipleValuesNotAllowedException | RequestParametersNotFoundException e) {
            throw log.throwing(new AssertionError("Error, code block supposed to be unreachable", e));
        }
    }
}
