package org.bgee.view.html;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class displays the resources for the HTML view.
 *
 * @author  Julien Wollbrett
 * @version Bgee 14, May 2019
 * @since   Bgee 14, May 2019
 */

import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.view.ResourceDisplay;

public class HtmlResourceDisplay extends HtmlParentDisplay implements ResourceDisplay{

    private final static Logger log = LogManager.getLogger(HtmlResourceDisplay.class.getName());
    
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
    public HtmlResourceDisplay(HttpServletResponse response,
            RequestParameters requestParameters, BgeeProperties prop, HtmlFactory factory) throws IOException {
        super(response, requestParameters, prop, factory);
    }

    @Override
    public void displayRPackages() {
        log.entry();
        
        RequestParameters urlTopAnat = this.getNewRequestParameters();
        urlTopAnat.setPage(RequestParameters.PAGE_TOP_ANAT);
        
        this.startDisplay("R Packages overview");

        this.writeln("<h1>R Packages overview</h1>");

        this.writeln("<div class='feature_list'>");

        this.writeln(HtmlParentDisplay.getSingleFeatureLogo(BGEEDB_R_PACKAGE_URL,
                true, "BgeeDB R package", "BgeeDB R package",
                this.prop.getLogoImagesRootDirectory() + "r_logo_color.png",
                "Retrieve the annotation of RNA-seq or Affymetrix experiments integrated "
                + "into the Bgee database, and download into R the quantitative data and "
                + "expression calls produced by the Bgee pipeline. The package also allows "
                + "to run GO-like enrichment analyses based on anatomical terms, where genes "
                + "are mapped to anatomical terms by expression patterns, based on the topGO "
                + "package. This is the same as the <a href='" + urlTopAnat.getRequestURL() + "' "
                + "title='TopAnat: Enrichment analyses of expression localization'>TopAnat "
                + "webservice</a> but with more flexibility in the choice of parameters and "
                + "developmental stages.."));
        
        

        this.writeln(HtmlParentDisplay.getSingleFeatureLogo(BGEECALL_R_PACKAGE_URL,
                true, "BgeeCall R package", "BgeeCall R package",
                this.prop.getLogoImagesRootDirectory() + "r_logo_color.png",
                "Generate present/absent calls for your own RNA-Seq libraries as long as the species"
                + "are present in Bgee. BgeeCall reuse intergenic regions generated with the expertise"
                + " of Bgee by taking into account expression of all RNA-Seq libraries integrated in the "
                + "Bgee database. This threshold is no longer arbitrary defined but is specieficto your "
                + "RNA-Seq library."));
        
        this.writeln("</div>"); // close feature_list

        this.endDisplay();

        log.exit();
        
    }

    @Override
    public void displayAnnotations() {
        log.entry();
        
        this.startDisplay("Annotation resources overview");

        this.writeln("<h1>Annotation resources overview</h1>");

        this.writeln("<div class='feature_list'>");
        
        this.writeln("</div>"); // close feature_list

        this.endDisplay();

        log.exit();
        
    }

    @Override
    public void displayOntologies() {
        log.entry();
        
        this.startDisplay("Ontology resources overview");

        this.writeln("<h1>Ontology resources overview</h1>");

        this.writeln("<div class='feature_list'>");
        
        this.writeln("</div>"); // close feature_list

        this.endDisplay();

        log.exit();        
    }

    @Override
    public void displaySourceCode() {
        log.entry();
        
        this.startDisplay("Source code resources overview");

        this.writeln("<h1>Source code resources overview</h1>");

        this.writeln("<div class='feature_list'>");
        
        this.writeln("</div>"); // close feature_list

        this.endDisplay();

        log.exit();         
    }

}
