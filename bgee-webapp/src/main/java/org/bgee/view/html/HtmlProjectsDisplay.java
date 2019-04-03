package org.bgee.view.html;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.view.ProjectsDisplay;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * This class displays the page having the category "project", i.e. with the parameter
 * page=about for the HTML view.
 *
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Apr. 2019
 * @since   Bgee 13, Apr. 2019
 */
public class HtmlProjectsDisplay extends HtmlParentDisplay implements ProjectsDisplay {

    private final static Logger log = LogManager.getLogger(HtmlProjectsDisplay.class.getName());

    /**
     * Default constructor.
     *
     * @param response          A {@code HttpServletResponse} that will be used to display the 
     *                          page to the client.
     * @param requestParameters A {@code RequestParameters} handling the parameters of the 
     *                          current request, to determine the requested displayType, 
     *                          and for display purposes.
     * @param prop              A {@code BgeeProperties} instance that contains the properties
     *                          to use.
     * @param factory           The {@code HtmlFactory} that instantiated this object.
     * @throws IOException      If there is an issue when trying to get or to use the
     *                          {@code PrintWriter}.
     */
    public HtmlProjectsDisplay(HttpServletResponse response, RequestParameters requestParameters,
                               BgeeProperties prop, HtmlFactory factory) throws IOException {
        super(response, requestParameters, prop, factory);
    }

    @Override
    public void displayProjectsPage() {
        log.entry();

        this.startDisplay("Bgee collaborations");

        this.writeln("<h1>Bgee collaborations</h1>");

        this.writeln("<div class='row'>");

        this.writeln("<div class='" + CENTERED_ELEMENT_CLASS + "'>");
        this.writeln("<p>This page shows the projects in which bgee is involved in.</p>");

        this.writeln("<h2>OncoMX</h2>");

        this.writeln("<p><a target='_blank' href='https://www.oncomx.org/'>OncoMX</a> is " +
                "a knowledgebase of unified cancer genomics data from integrated mutation, " +
                "expression, literature, and biomarker databases, accessible through web portal.<p>");
        
        this.writeln("<p>During this collaboration, we generated files available on " +
                "<a href='" + this.prop.getFTPRootDirectory() + "/collaboration/oncoMX/'>our FTP</a>. " +
                "You can see the description of these files in our " +
                "<a href='" + BGEE_GITHUB_URL + "/bgee_pipeline/tree/develop/pipeline/collaboration/oncoMX" +
                "#information-about-the-files-generated-for-oncomx'>pipeline documentation</a><p>");

        this.writeln("<h2>OMA</h2>");
        this.writeln("<p>The <a target='_blank' href='https://omabrowser.org/oma/home/'>OMA " +
                "(&quot;Orthologous MAtrix&quot;) project</a> is a method and database " +
                "for the inference of orthologs among complete genomes.<p>");

        this.writeln("<p>During this collaboration, we generated files available on " +
                "<a href='" + this.prop.getFTPRootDirectory() + "/collaboration/branch_length_expression_divergence/'>our FTP</a>.<p>");

        this.writeln("</div>"); // close CENTERED_ELEMENT_CLASS

        this.writeln("</div>"); // close row

        this.endDisplay();

        log.exit();
    }

    @Override
    protected void includeCss() {
        log.entry();
        super.includeCss();
        //If you ever add new files, you need to edit bgee-webapp/pom.xml 
        //to correctly merge/minify them.
        log.exit();
    }
}
