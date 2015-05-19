package org.bgee.view.html;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.view.AboutDisplay;
import org.bgee.view.ViewFactory;


/**
 * This class displays the page having the category "about", i.e. with the parameter
 * page=about for the {@code displayTypes} HTML.
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
     * @param factory           A {@code ViewFactory} that instantiated this object.
     * @throws IOException      If there is an issue when trying to get or to use the
     *                          {@code PrintWriter}.
     */
    public HtmlAboutDisplay(HttpServletResponse response,
            RequestParameters requestParameters, BgeeProperties prop, ViewFactory factory) throws IOException {
        super(response, requestParameters, prop, factory);
    }

    @Override
    public void displayAboutPage() {
        log.entry();
        
        RequestParameters urlDownloadGenerator = this.getNewRequestParameters();
        urlDownloadGenerator.setPage(RequestParameters.PAGE_DOWNLOAD);
        RequestParameters urlDocumentationGenerator = this.getNewRequestParameters();
        urlDocumentationGenerator.setPage(RequestParameters.PAGE_DOCUMENTATION);

        
        this.startDisplay("about", "Bgee release 13 about page");

        this.writeln("<div>");
        this.writeln("<h2>What is Bgee?</h2>");
        this.writeln("<div class='documentationsubsection'>");
        this.writeln("<p>Bgee is a database to retrieve and compare gene expression patterns " + 
                "between animal species.</p>");
        this.writeln("<p>Data can be retrieved by <a href='" + urlDownloadGenerator.getRequestURL() + 
                "'>downloading data</a>.</p>");
        this.writeln("<p>More information is provided in the <a href='" + 
                urlDocumentationGenerator.getRequestURL() + "'>documentation page</a>.</p>");
        this.writeln("</div>");
        this.writeln("</div>"); // end of 'What is Bgee?'

        this.writeln("<div>");
        this.writeln("<h2>Who are we?</h2>");
        this.writeln("<div class='documentationsubsection'>");
        this.writeln("<p>Bgee is developed by the " +
                "<a href='http://bioinfo.unil.ch' title='External link to the Robinson-Rechavi " +
                "group webpage' target='_blank'>Evolutionary Bioinformatics group</a>, part of " +
                "the <a href='http://www.isb-sib.ch/' title='External link to SIB' target='_blank'>" +
                "Swiss Institute of Bioinformatics</a>, at the <a href='http://www.unil.ch/' " +
                "title='External link to UNIL' target='_blank'>University of Lausanne</a>. " +
                "Our main interest is in the evolution of animal genomes in the context of " +
                "organismal function and development. We have special interests in the early " +
                "evolution of chordates and fishes.</p>");
        this.writeln("</div>");
        this.writeln("</div>"); // end of 'Who are we?'

        this.writeln("<div>");
        this.writeln("<h2>How to cite us?</h2>");
        this.writeln("<div class='documentationsubsection'>");
        this.writeln("<ul>");
        this.writeln("<li>For the use of Bgee: Bastian F., Parmentier G., Roux J., Moretti S., " +
                "Laudet V., Robinson-Rechavi M. (2008)<br>Bgee: Integrating and Comparing " +
                "Heterogeneous Transcriptome Data Among Species." +
                "<br><em>in</em> DILS: Data Integration in Life Sciences. " +
                "<strong>Lecture Notes in Computer Science.</strong> " +
                "5109:124-131. [<a href='http://www.springerlink.com/content/92q428161616w8r5/' " +
                "title='Bgee paper in LNCS' target='_blank'>url</a>] " +
                "<a href='ftp://lausanne.isb-sib.ch/pub/databases/Bgee/general/citation01.ris'>" +
                "RIS</a></li>");
        this.writeln("<li>For the use of the HOG or vHOG ontologies: Niknejad A., Comte A., " +
                "Parmentier G., Roux J., Bastian F.B. and Robinson-Rechavi M. (2012)" +
                "<br>vHOG, a multi-species vertebrate ontology of homologous organs groups<br>" +
                "<em>in</em> Bioinformatics (2012) 28(7): 1017-1020.[<a target='_blank' " +
                "href='http://bioinformatics.oxfordjournals.org/content/28/7/1017.full' " +
                "title='External link to: &quot;vHOG, a multi-species vertebrate ontology of " +
                "homologous organs groups&quot;'>url</a>] <a href='" +
                "ftp://lausanne.isb-sib.ch/pub/databases/Bgee/general/citation03.ris'>RIS</a></li>");
        this.writeln("<li>For the use of Homolonto: Parmentier G., Bastian F.B., Robinson-Rechavi M. " +
                "(2010)<br>Homolonto: generating homology relationships by pairwise alignment of " +
                "ontologies and application to vertebrate anatomy.<br><em>in</em> Bioinformatics " +
                "(2010) 26(14): 1766-1771.[<a target='_blank' href='" +
                "http://bioinformatics.oxfordjournals.org/content/26/14/1766.long' " +
                "title='External link to: &quot;Homolonto: generating homology relationships by " +
                "pairwise alignment of ontologies and application to vertebrate anatomy&quot;'>" +
                "url</a>] <a href='" +
                "ftp://lausanne.isb-sib.ch/pub/databases/Bgee/general/citation02.ris'>" +
                "RIS</a></li></ul>");
        this.writeln("</div>");
        this.writeln("</div>"); // end of 'How to cite us?'

        this.writeln("<div>");
        this.writeln("<h2>More</h2>");
        this.writeln("<div class='documentationsubsection'>");
        this.writeln("<p>More information about data analyses and database content is available " +
                "in the <a href='" + urlDocumentationGenerator.getRequestURL() + 
                "'>documentation page</a>.</p>");
        this.writeln("</div>");
        this.writeln("</div>"); // end of 'More'

        this.endDisplay();

        log.exit();
    }

    @Override
    protected void includeCss() {
        log.entry();
        super.includeCss();
        this.includeCss("documentation.css");
        log.exit();
    }
}
