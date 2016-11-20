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
     * A {@code boolean} set to {@code true} when the display has been started.
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
     * @return  The {@code PrintWriter} that produces the output.
     */
    protected PrintWriter getOut() {
        return out;
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
     * Delegates to {@link #sendHeaders(int)} with the status code set to {@code HttpServletResponse.SC_OK}.
     * @see #sendHeaders(int)
     */
    protected void sendHeaders() {
        log.entry();
        this.sendHeaders(HttpServletResponse.SC_OK);
        log.exit();
    }
    /**
     * Delegates to {@link #sendHeaders(int, boolean)} with the {@code boolean} {@code noCache} argument 
     * defined depending on whether the current response follows an AJAX request. 
     * In case an AJAX request is sent, we do not want the client to store the response in cache, 
     * the {@code noCache} argument will be defined as {@code true}.
     * <p>
     * To determine whether the current response follows an AJAX request, 
     * it is possible to call {@link RequestParameters#isAnAjaxRequest()} 
     * on the object returned by {@link #getRequestParameters()}. 
     * 
     * @see #sendHeaders(int, boolean)
     */
    protected void sendHeaders(int statusCode) {
        log.entry(statusCode);
        this.sendHeaders(statusCode, this.getRequestParameters().isAnAjaxRequest());
        log.exit();
    }
    /**
     * Send the headers of the response with the provided status code. The MIME content type is defined 
     * by calling {@link #getContentType()}. Whether specific headers should be sent to disable 
     * cache on the client side is defined with the argument {@code noCache} (cache control, proxy cache,
     * expiration date, etc). For instance, it is desirable to disable cache in response of an AJAX request, 
     * or in case of server error.
     * <p>
     * Note that this method sends headers only at the first call on a given object:
     * sometimes, a same container can be used as a standalone response, 
     * or embedded into another container (already sending its own headers), 
     * so, some methods of a same view can redundantly call this method.
     * 
     * @parameters statusCode   An {@code int} corresponding to the status code to return, as defined 
     *                          in the static variables of {@code javax.servlet.http.HttpServletResponse}.
     * @parameters noCache      A {@code boolean} defining whether specific headers disabling cache 
     *                          on the client side should be sent (if {@code true}).
     * @see #sendHeaders(int)
     */
    protected void sendHeaders(int statusCode, boolean noCache) {
        log.entry(statusCode, noCache);
        if (this.response == null) {
            log.exit(); return;
        }
        if (!this.headersAlreadySent) {
            this.response.setStatus(statusCode);
            this.defineResponseContentTypeAndEncoding();
            
            if (noCache) {
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
     * Set on {@link #response} the content type (using the method {@code getContentType} 
     * on {@link #requestParameters}) and the character encoding (using the method 
     * {@code getCharacterEncoding} on {@link #requestParameters}).
     */
    private void defineResponseContentTypeAndEncoding() {
        log.entry();
        log.trace("Set content type to {}", this.getContentType());
        this.response.setContentType(this.getContentType());
        log.trace("Set character encoding to {}", this.requestParameters.getCharacterEncoding());
        this.response.setCharacterEncoding(this.requestParameters.getCharacterEncoding());
        log.exit();
    }

    /**
     * Delegates to {@link #sendHeaders(int, boolean)} with the status code set to 
     * {@code HttpServletResponse.SC_SERVICE_UNAVAILABLE} and the {@code noCache} argument set to {@code true}.
     * @see #sendHeaders(int, boolean)
     */
    public void sendServiceUnavailableHeaders() {
        log.entry();
        this.sendHeaders(HttpServletResponse.SC_SERVICE_UNAVAILABLE, true);
        log.exit();
    }
    /**
     * Delegates to {@link #sendHeaders(int, boolean)} with the status code set to 
     * {@code HttpServletResponse.SC_BAD_REQUEST} and the {@code noCache} argument set to {@code true}.
     * @see #sendHeaders(int, boolean)
     */
    protected void sendBadRequestHeaders() {
        log.entry();
        this.sendHeaders(HttpServletResponse.SC_BAD_REQUEST, true);
        log.exit();
    }
    /**
     * Delegates to {@link #sendHeaders(int, boolean)} with the status code set to 
     * {@code 429} "Too Many Requests" and the {@code noCache} argument set to {@code true}.
     * @see #sendHeaders(int, boolean)
     */
    protected void sendTooManyRequeststHeaders() {
        log.entry();
        this.sendHeaders(429, true);
        log.exit();
    }
    /**
     * Delegates to {@link #sendHeaders(int, boolean)} with the status code set to 
     * {@code HttpServletResponse.SC_NOT_FOUND} and the {@code noCache} argument set to {@code true}.
     * @see #sendHeaders(int, boolean)
     */
    protected void sendPageNotFoundHeaders() {
        log.entry();
        this.sendHeaders(HttpServletResponse.SC_NOT_FOUND, true);
        log.exit();
    }
    /**
     * Delegates to {@link #sendHeaders(int, boolean)} with the status code set to 
     * {@code HttpServletResponse.SC_INTERNAL_SERVER_ERROR} and the {@code noCache} argument set to {@code true}.
     * @see #sendHeaders(int, boolean)
     */
    protected void sendInternalErrorHeaders() {
        log.entry();
        this.sendHeaders(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, true);
        log.exit();
    }
    
    /**
     * Delegates to {@link #sendHeaders(int, boolean)} with the status code set to 
     * {@code HttpServletResponse.SC_NO_CONTENT} and the {@code noCache} argument set to {@code true}.
     * @see #sendHeaders(int, boolean)
     */
    public void respondSuccessNoContent() {
        log.entry();
        this.sendHeaders(HttpServletResponse.SC_NO_CONTENT, true);
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
