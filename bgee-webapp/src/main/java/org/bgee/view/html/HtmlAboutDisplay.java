package org.bgee.view.html;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.view.AboutDisplay;


/**
 * This class displays the page having the category "about", i.e. with the parameter
 * page=about for the HTML view.
 *
 * @author 	Valentine Rech de Laval
 * @version Bgee 13
 * @since 	Bgee 13
 */
public class HtmlAboutDisplay extends HtmlParentDisplay implements AboutDisplay {

    private final static Logger log = LogManager.getLogger(HtmlAboutDisplay.class.getName());

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
    public HtmlAboutDisplay(HttpServletResponse response,
            RequestParameters requestParameters, BgeeProperties prop, HtmlFactory factory) throws IOException {
        super(response, requestParameters, prop, factory);
    }

    @Override
    public void displayAboutPage() {
        log.entry();
        
        RequestParameters urlDownloadGenerator = this.getNewRequestParameters();
        urlDownloadGenerator.setPage(RequestParameters.PAGE_DOWNLOAD);
        RequestParameters urlGeneSearchGenerator = this.getNewRequestParameters();
        urlGeneSearchGenerator.setPage(RequestParameters.PAGE_GENE);
        RequestParameters urlTopAnatGenerator = this.getNewRequestParameters();
        urlTopAnatGenerator.setPage(RequestParameters.PAGE_TOP_ANAT);
        RequestParameters urlDocumentationGenerator = this.getNewRequestParameters();
        urlDocumentationGenerator.setPage(RequestParameters.PAGE_DOCUMENTATION);
        
        String version = this.getWebAppVersion();
        String title = "Bgee ";
        if (version != null) {
            title += "release " + version + " ";
        }
        title += "about page";
        this.startDisplay(title);

        this.writeln("<h1>About</h1>");

        this.writeln("<div class='row'>");

        this.writeln("<div class='" + CENTERED_ELEMENT_CLASS + "'>");
        this.writeln("<h2>What is Bgee?</h2>");

        this.writeln("<p>Bgee is a database to retrieve and compare gene expression patterns "
                + "in multiple animal species, produced from multiple data types "
                + "(RNA-Seq, Affymetrix, <em>in situ</em> hybridization, and EST data). "
                + "Bgee is based exclusively on curated \"normal\", healthy, expression data "
                + "(e.g., no gene knock-out, no treatment, no disease), "
                + "to provide a comparable reference of normal gene expression. "
                + "Bgee produces calls of presence/absence of expression, "
                + "and of differential over-/under-expression, "
                + "integrated along with information of gene orthology, and of homology "
                + "between organs. This allows comparisons of expression patterns "
                + "between species.</p>");
        this.writeln("<p>Data can be browsed through <a href='" + urlGeneSearchGenerator.getRequestURL() + 
                "'>gene search</a>, <a href='" + urlTopAnatGenerator.getRequestURL() + 
                "'>expression enrichment analysis</a>, or <a href='" + urlDownloadGenerator.getRequestURL() + 
                "'>data download</a>.</p>");
        this.writeln("<p>More information is provided in the <a href='" + 
                urlDocumentationGenerator.getRequestURL() + "'>documentation</a>.</p>");

        this.writeln("<h2>Who are we?</h2>");

        this.writeln("<p>Bgee is developed by the " +
                "<a href='http://bioinfo.unil.ch' title='External link to the Robinson-Rechavi " +
                "group webpage' target='_blank'>Evolutionary Bioinformatics group</a>, part of " +
                "the <a href='http://www.sib.swiss/' title='External link to SIB' target='_blank'>" +
                "SIB Swiss Institute of Bioinformatics</a>, at the "
                + "<a href='http://www.unil.ch/central/en/home.html' title='External link to UNIL' "
                + "target='_blank'>University of Lausanne</a>.</p>" +
                
                "<p>Our main interest is in the evolution of animal genomes in the context of " +
                "organismal function and development. We have special interests in the early " +
                "evolution of chordates and fishes. We have the aim of producing a database "
                + "useful to disciplines such as comparative genomics, Evo-Devo, "
                + "or transcriptome studies, whilst providing an improved integration "
                + "of homology and related concepts into bioinformatics through ontologies "
                + "and ontology tools.</p>");

        this.writeln("<h2>How to cite us?</h2>");

        this.writeln("<ul>");
        this.writeln("<li>For the use of Bgee: Bastian F., Parmentier G., Roux J., Moretti S., " +
                "Laudet V., Robinson-Rechavi M. (2008)<br>Bgee: Integrating and Comparing " +
                "Heterogeneous Transcriptome Data Among Species." +
                "<br><em>in</em> DILS: Data Integration in Life Sciences. " +
                "<strong>Lecture Notes in Computer Science.</strong> " +
                "5109:124-131. [<a href='http://www.springerlink.com/content/92q428161616w8r5/' " +
                "title='Bgee paper in LNCS' target='_blank'>url</a>] " +
                "<a href='ftp://ftp.bgee.org/general/citation01.ris'>" +
                "RIS</a></li>");
        this.writeln("<li>For the use of the HOG or vHOG ontologies: Niknejad A., Comte A., " +
                "Parmentier G., Roux J., Bastian F.B. and Robinson-Rechavi M. (2012)" +
                "<br>vHOG, a multi-species vertebrate ontology of homologous organs groups<br>" +
                "<em>in</em> Bioinformatics (2012) 28(7): 1017-1020.[<a target='_blank' " +
                "href='http://bioinformatics.oxfordjournals.org/content/28/7/1017.full' " +
                "title='External link to: &quot;vHOG, a multi-species vertebrate ontology of " +
                "homologous organs groups&quot;'>url</a>] <a href='" +
                "ftp://ftp.bgee.org/general/citation03.ris'>RIS</a></li>");
        this.writeln("</ul>");

        this.writeln("<h2>More</h2>");
        
        this.writeln("<p>Our curation and ontology resources "
                + "can be browsed on <a title='External link to BgeeDB on GitHub' "
                + "href='https://github.com/BgeeDB/' target='_blank'>our GitHub page</a>.</p>");

        this.writeln("<p>More information about data analyses and database content is available " +
                "in the <a href='" + urlDocumentationGenerator.getRequestURL() + 
                "'>documentation</a>.</p>");

        this.writeln("</div>"); // close specific class
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
        this.includeCss("documentation.css");
        log.exit();
    }
}
