package org.bgee.controller;

import java.io.Writer;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.view.ViewFactory;

/**
 * Parent class of all controllers. It achieves operations that are common to all subclasses, 
 * and defines methods that subclasses must implement.
 * 
 * Notably, it defines the abstract method {@code processRequest}, 
 * that is the main method of all controllers, 
 * launching actions on the {@code model} layer, 
 * and display from the {@code view} layer.
 * <p>
 * This class is not instantiable, as all concrete controllers must provide at least 
 * their own implementation of {@code processRequest()}.
 * 
 * @author 	Frederic Bastian
 * @author  Mathieu Seppey
 * @version Bgee 13 Aug 2014
 * @see 	#processRequest()
 * @since 	Bgee 1
 *
 */
abstract class CommandParent {
    
    private final static Logger log = LogManager.getLogger(CommandParent.class.getName());

    /**
     * Concrete factory providing classes from the {@code view} package. 
     * This concrete factory implements the {@code ViewFactory} interface.
     */
    protected ViewFactory viewFactory;
    protected Writer out;
    protected HttpServletResponse response;
    /**
     * Stores the parameters of the current request.
     */
    protected RequestParameters requestParameters;

    protected String serverRoot;
    protected String homePage;
    protected String bgeeRoot;

    /**
     * A {@code BgeeProperties} instance that contains the properties
     * to use.
     */
    protected BgeeProperties prop;

    /**
     * Constructor
     * 
     * @param response          A {@code HttpServletResponse} that will be used to display the 
     *                          page to the client
     * @param requestParameters The {@code RequestParameters} that handles the parameters of the 
     *                          current request.
     * @param prop              A {@code BgeeProperties} instance that contains the properties
     *                          to use.
     * @param viewFactory       A {@code ViewFactory} that provides the display type to be used.
     */
    public CommandParent(HttpServletResponse response, 
            RequestParameters requestParameters, BgeeProperties prop, ViewFactory viewFactory) {
        log.entry(response, requestParameters, prop, viewFactory);
        this.response = response;
        this.requestParameters = requestParameters;
        this.prop = prop;
        this.viewFactory = viewFactory;
        this.serverRoot = prop.getBgeeRootDirectory();
        this.homePage   = prop.getBgeeRootDirectory();
        this.bgeeRoot   = prop.getBgeeRootDirectory();
        log.exit();
    }

    /**
     * This method is responsible for pre-processing a request, 
     * and returns {@code true} if the application can continue 
     * and call the {@code processRequest()} method.
     * 
     * A pre-processing step is, for instance, redirecting to another page, 
     * filtering a parameter... In the case of a redirection, the method should 
     * return {@code false} so that the application is aware that 
     * the request should not be processed further.
     * <p>
     * Concrete controllers can override this method to provide 
     * their own pre-processing, specific to their domain of action. 
     * It is recommended that they call the parent method in their own implementation, 
     * unless their pre-processing steps overlap (for instance, performing a redirection 
     * in the concrete controller, while this parent controller also perform a redirection). 
     * 
     * @return 	{@code true} if the request can be further processed, 
     * 			{@code false} otherwise ({@code processRequest()} should then not been called).
     * @throws 	Exception 	any exception not-caught during the process of the request 
     * 						is thrown to be caught by the {@code FrontController}.
     * @see 	#processRequest()
     * @see 	FrontController#doRequest(HttpServletRequest, HttpServletResponse, boolean)
     */
    public boolean preprocessRequestAndCheckIfContinue() {
        log.entry();
        return log.exit(false);
    }

    /**
     * Main method of all controllers, responsible for analyzing the query, 
     * and triggering the appropriate actions on the {@code model} layer 
     * and the {@code view} layer.
     * 
     * Each concrete controller must provide its own implementation of this method.
     * 
     * @throws  Exception 	any exception not-caught during the process of the request 
     * 						is thrown to be caught by the {@code FrontController}.
     * @see 	FrontController#doRequest(HttpServletRequest, HttpServletResponse, boolean)
     */
    public abstract void processRequest() throws Exception;

}
