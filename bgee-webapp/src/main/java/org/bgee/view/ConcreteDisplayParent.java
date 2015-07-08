package org.bgee.view;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;

/**
 * This class has to be extended by all display class and define the mandatory methods such as
 * {@link #write(String)} and {@link #writeln(String) }
 * 
 * @author  Frederic Bastian
 * @author  Mathieu Seppey
 * @version Bgee 13 Aug 2014
 * @since   Bgee 1
 */
public abstract class ConcreteDisplayParent {

    private final static Logger log = LogManager.getLogger(ConcreteDisplayParent.class.getName());

    /**
     * A {@code HttpServletResponse} that will be used to display the page to 
     * the client
     */
    protected final HttpServletResponse response;
    /**
     * The {@code PrintWriter} that produces the output
     */
    protected PrintWriter out;
    /**
     * A {@code boolean} set to {@code true} when the header has been sent
     */
    protected boolean headersAlreadySent;
    /**
     * A {@code boolean} set to {@code true} when the display has been started
     * TODO check why it is actually never used in views. ( Should be ? )
     */
    protected boolean displayAlreadyStarted;
    /**
     * A {@code BgeeProperties} instance that contains the properties to use.
     */
    protected final BgeeProperties prop;
    /**
     * The {@code ViewFactory} that was responsible for instantiating this object.
     */
    private final ViewFactory factory;

    /**
     * The {@code RequestParameters} holding the parameters of the current query 
     * being treated.
     */
    private final RequestParameters requestParameters;

    /**
     * Default Constructor. 
     * @param response          A {@code HttpServletResponse} that will be used to display the 
     *                          page to the client
     * @param requestParameters The {@code RequestParameters} that handles the parameters of the 
     *                          current request.
     * @param prop              A {@code BgeeProperties} instance that contains the properties
     *                          to use.
     * @param factory           A {@code ViewFactory} that instantiated this object.
     * @throws IllegalArgumentException If {@code factory} is {@code null}.
     * @throws IOException      If there is an issue when trying to get or to use the
     *                          {@code PrintWriter} 
     */
    protected ConcreteDisplayParent(HttpServletResponse response, 
            RequestParameters requestParameters, BgeeProperties prop, 
            ViewFactory factory) throws IllegalArgumentException, IOException {
        log.entry(response, requestParameters, prop, factory);
        if (factory == null) {
            throw log.throwing(new IllegalArgumentException("The provided factory cannot be null"));
        }
        this.response = response;
        this.requestParameters = requestParameters;
        this.factory = factory;
        this.prop = prop;

        if (this.response != null) {
            this.response.setCharacterEncoding("UTF-8");
            this.out = this.response.getWriter();
        } 

        this.headersAlreadySent = false;
        this.displayAlreadyStarted = false;
        log.exit();
    }
    
    /**
     * @return  The {@code ViewFactory} that instantiated this object.
     */
    protected ViewFactory getFactory() {
        return this.factory;
    }

    /**
     * Write the provided {@code String} on the output of the {@code HttpServletResponse}
     * using the {@code PrintWriter}, with a line return at the end.
     * @param stringToWrite
     */
    protected void writeln(String stringToWrite)
    {
        log.entry(stringToWrite);
        this.out.println(stringToWrite);
        log.exit();
    }
    /**
     * Write the provided {@code String} on the output of the {@code HttpServletResponse}
     * using the {@code PrintWriter}, without a line return at the end.
     * @param stringToWrite
     */
    protected void write(String stringToWrite)
    {
        log.entry(stringToWrite);
        this.out.print(stringToWrite);
        log.exit();
    }
    /**
     * Send the headers of the response. The MIME content type is defined 
     * by calling {@link #getContentType()}. The headers sent vary 
     * depending on whether the current request is an AJAX request 
     * (specific cache control, expiration date, etc, are sent). 
     * <p>
     * To determine whether the current request is from an AJAX query, 
     * it is possible to call {@link RequestParameters#isAnAjaxRequest()} 
     * on the object returned by {@link #getRequestParameters()}. 
     * <p>
     * Note that this method sends headers only at the first call on a given object:
     * sometimes, a same container can be used as a standalone response, 
     * or embedded into another container (already sending its own headers), 
     * so, some methods of a same view can redundantly call this method.
     */
    protected void sendHeaders() {
        log.entry();
        if (this.response == null) {
            log.exit(); return;
        }
        if (!this.headersAlreadySent) {
            log.trace("Set content type to {}", this.getContentType());
            this.response.setContentType(this.getContentType());
            
            if (this.getRequestParameters().isAnAjaxRequest()) {
                this.response.setDateHeader("Expires", 1);
                this.response.setHeader("Cache-Control", 
                        "no-store, no-cache, must-revalidate, proxy-revalidate");
                this.response.addHeader("Cache-Control", "post-check=0, pre-check=0");
                this.response.setHeader("Pragma", "No-cache");
            }
            
            this.headersAlreadySent = true;
        }
        log.exit();
    }

    /**
     * Send the header in case of HTTP 503 error, with MIME content type defined 
     * by calling {@link #getContentType()}.
     */
    public void sendServiceUnavailableHeaders() {
        log.entry();
        if (this.response == null) {
            return;
        }
        if (!this.headersAlreadySent) {
            log.trace("Set content type to {}", this.getContentType());
            this.response.setContentType(this.getContentType());
            this.response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            this.headersAlreadySent = true;
        }
        log.exit();
    }
    
    /**
     * Send the header in case of HTTP 400 error, with MIME content type defined 
     * by calling {@link #getContentType()}.
     */
    protected void sendBadRequestHeaders() {
        log.entry();
        if (this.response == null) {
            return;
        }
        if (!this.headersAlreadySent) {
            log.trace("Set content type to {}", this.getContentType());
            this.response.setContentType(this.getContentType());
            this.response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            this.headersAlreadySent = true;
        }
        log.exit();
    }
    
    /**
     * Send the header in case of HTTP 404 error, with MIME content type defined 
     * by calling {@link #getContentType()}.
     */
    protected void sendPageNotFoundHeaders() {
        log.entry();
        if (this.response == null) {
            return;
        }
        if (!this.headersAlreadySent) {
            log.trace("Set content type to {}", this.getContentType());
            this.response.setContentType(this.getContentType());
            this.response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            this.headersAlreadySent = true;
        }
        log.exit();
    }
    
    /**
     * Send the header in case of HTTP 500 error, with MIME content type defined 
     * by calling {@link #getContentType()}.
     */
    protected void sendInternalErrorHeaders() {
        log.entry();
        if (this.response == null) {
            return;
        }
        if (!this.headersAlreadySent) {
            log.trace("Set content type to {}", this.getContentType());
            this.response.setContentType(this.getContentType());
            this.response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            this.headersAlreadySent = true;
        }
        log.exit();
    }
    
    /**
     * Return the MIME content type for the current view, that will be used as argument  
     * when calling {@code setContentType} on the {@code HttpServletResponse} object 
     * provided at instantiation. This content type should NOT include 
     * the character encoding (this character encoding is set at instantiation 
     * to 'UTF-8', before calling {@code getWriter} on the {@code HttpServletResponse}).
     * <p>
     * Example MIME content types returned include: {@code text/html}, 
     * {@code text/csv}, {@code text/tab-separated-values}, 
     * {@code application/xml}, {@code application/json}.
     * 
     * @return  A {@code String} that is the MIME content type for the current view.
     */
    protected abstract String getContentType();

    /**
     * @return  The {@code RequestParameters} holding the parameters of the current query 
     *          being treated.
     */
    protected RequestParameters getRequestParameters() {
        return requestParameters;
    }
}
