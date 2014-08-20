package org.bgee.view;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;

public abstract class ConcreteDisplayParent 
{

    private final static Logger log = LogManager.getLogger(ConcreteDisplayParent.class.getName());

    protected HttpServletResponse response;
    protected PrintWriter out;

    protected String serverRoot;
    protected String homePage;
    protected String bgeeRoot;
    protected String downloadRootDirectory;
    protected String emailContact;

    protected boolean headersAlreadySent;
    protected boolean displayAlreadyStarted;

    protected BgeeProperties prop;


    public ConcreteDisplayParent(HttpServletResponse response,BgeeProperties prop) throws IOException
    {
        log.entry(response, prop);
        this.response = response;

        if (this.response != null) {
            this.response.setCharacterEncoding("UTF-8");
            this.out = this.response.getWriter();
        } 

        this.headersAlreadySent = false;
        this.displayAlreadyStarted = false;
        this.prop = prop;
        log.exit();
    }

    public void writeln(String stringToWrite)
    {
        log.entry(stringToWrite);
        this.out.println(stringToWrite);
        log.exit();
    }
    public void write(String stringToWrite)
    {
        log.entry(stringToWrite);
        this.out.print(stringToWrite);
        log.exit();
    }

    public abstract void sendHeaders(boolean ajax);
}
