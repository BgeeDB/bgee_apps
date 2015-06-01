package org.bgee.view;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;

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
     * Default Constructor. 
     * @param response          A {@code HttpServletResponse} that will be used to display the 
     *                          page to the client
     * @param prop              A {@code BgeeProperties} instance that contains the properties
     *                          to use.
     * @param factory           A {@code ViewFactory} that instantiated this object.
     * @throws IllegalArgumentException If {@code factory} is {@code null}.
     * @throws IOException      If there is an issue when trying to get or to use the
     *                          {@code PrintWriter} 
     */
    protected ConcreteDisplayParent(HttpServletResponse response, BgeeProperties prop, 
            ViewFactory factory) throws IllegalArgumentException, IOException {
        log.entry(response, prop, factory);
        if (factory == null) {
            throw log.throwing(new IllegalArgumentException("The provided factory cannot be null"));
        }
        this.response = response;
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
//    /**
//     * Send the header of the page
//     * @param ajax  A {@code boolean} to indicate whether the request is ajax
//     */
//    public abstract void sendHeaders(boolean ajax);
}
