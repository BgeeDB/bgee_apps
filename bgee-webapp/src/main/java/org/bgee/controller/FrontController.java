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
import org.bgee.view.GeneralDisplay;
import org.bgee.view.ViewFactory;

/**
 * This is the entry point of bgee-webapp. It can be directely mapped as the main servlet in
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
     * Default constructor. It will use the default {@code BgeeProperties} instance
     * and the default {@code URLParameters} instance
     * 
     * @see BgeeProperties
     * @see URLParameters
     */
    public FrontController() {
        this(BgeeProperties.getBgeeProperties(), new URLParameters());
    }

    /**
     * Constructor that takes as parameter a {@code java.util.Properties} instance that 
     * will be used to instantiate a {@code BgeeProperties} object which will be be used in the whole
     * application and injected in all classes that will need it eventually.
     * It will use the default {@code URLParameters} instance
     * 
     * @param prop  A {@code java.util.Properties} that will be use to get an instance of
     *              {@code BgeeProperties}
     *  
     * @see BgeeProperties
     * @see URLParameters
     */
    public FrontController(Properties prop) {
        this(BgeeProperties.getBgeeProperties(prop), new URLParameters());
    }

    /**
     * Constructor that takes as parameters a custom {@code BgeeProperties} instance and a custom 
     * {@code URLParameters} instance that will be injected further in all classes that use them.
     * 
     * @param prop              A {@code BgeeProperties} instance to be used in the whole 
     *                          application and injected in all classes that will need it
     *                          eventually.
     * @param urlParameters     A {@code urlParameters} instance to be used in the whole 
     *                          application and injected in all classes that will need it
     *                          eventually.
     * @see BgeeProperties
     * @see URLParameters
     */
    public FrontController(BgeeProperties prop,URLParameters urlParameters) {
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
                this.urlParameters, this.prop);
        //need the default factory here in case an exception is thrown 
        // before we get the correct display type
        ViewFactory factory = ViewFactory.getFactory(response, requestParameters);
        GeneralDisplay generalDisplay = null;

        //then let's start the real job!
        try {
            //in order to display error message in catch clauses. 
            //we do it in the try clause, because getting a view can throw an IOException.
            //so here we get the default view from the default factory before any exception 
            //can be thrown.
            generalDisplay = factory.getGeneralDisplay(prop);
            request.setCharacterEncoding("UTF-8");
            requestParameters = new RequestParameters(request, this.urlParameters, this.prop);
            log.info("Analyzed URL: " + requestParameters.getRequestURL("&"));
            //in order to display error message in catch clauses. 
            //we redo it here to get the correct display type and correct user, 
            // if no exception was thrown yet
            factory = ViewFactory.getFactory(response, requestParameters);
            CommandParent controller = null;
            // call the correct controller depending on the page type
            if (requestParameters.isADownloadPageCategory()) {
                controller = new CommandDownload(response, requestParameters, this.prop);
            }

            if (controller == null) {
                controller = new CommandHome(response, requestParameters, this.prop);
            }
            controller.processRequest();
        // Display the error pages
        } catch(RequestParametersNotFoundException e) {
            generalDisplay.displayRequestParametersNotFound(requestParameters.getFirstValue(
                    this.urlParameters.getParamData()));
            log.error("RequestParametersNotFoundException", e);
        } catch(PageNotFoundException e) {
            generalDisplay.displayPageNotFound(e.getMessage());
            log.error("PageNotFoundException", e);
        } catch(RequestParametersNotStorableException e) {
            generalDisplay.displayRequestParametersNotStorable(e.getMessage());
            log.error("RequestParametersNotStorableException", e);
        } catch(MultipleValuesNotAllowedException e) {
            generalDisplay.displayMultipleParametersNotAllowed(e.getMessage());
            log.error("MultipleValuesNotAllowedException", e);
        } catch(Exception e) {
            if (generalDisplay != null) {
                generalDisplay.displayUnexpectedError();
            }
            log.error("Other Exception", e);
        } finally {
            //Database.destructAll();
        }
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
