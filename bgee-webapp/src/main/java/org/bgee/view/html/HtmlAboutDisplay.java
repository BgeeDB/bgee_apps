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
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Aug. 2018
 * @since   Bgee 13, Mar. 2015
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
        log.traceEntry();

        RequestParameters urlDownloadCallsGenerator = this.getNewRequestParameters();
        urlDownloadCallsGenerator.setPage(RequestParameters.PAGE_DOWNLOAD);
        urlDownloadCallsGenerator.setAction(RequestParameters.ACTION_DOWLOAD_CALL_FILES);
        RequestParameters urlDownloadProcsGenerator = this.getNewRequestParameters();
        urlDownloadProcsGenerator.setPage(RequestParameters.PAGE_DOWNLOAD);
        urlDownloadProcsGenerator.setAction(RequestParameters.ACTION_DOWLOAD_PROC_VALUE_FILES);
        RequestParameters urlDataDownloadGenerator = this.getNewRequestParameters();
        urlDataDownloadGenerator.setPage(RequestParameters.PAGE_DOWNLOAD);
        urlDataDownloadGenerator.setAction(RequestParameters.ACTION_DOWNLOAD_DUMPS);
        RequestParameters urlGeneSearchGenerator = this.getNewRequestParameters();
        urlGeneSearchGenerator.setPage(RequestParameters.PAGE_GENE);
        RequestParameters urlTopAnatGenerator = this.getNewRequestParameters();
        urlTopAnatGenerator.setPage(RequestParameters.PAGE_TOP_ANAT);
        RequestParameters urlExprCompGenerator = this.getNewRequestParameters();
        urlExprCompGenerator.setPage(RequestParameters.PAGE_EXPR_COMPARISON);
        RequestParameters urlPublicationsGenerator = this.getNewRequestParameters();
        urlPublicationsGenerator.setPage(RequestParameters.PAGE_PUBLICATION);
        RequestParameters urlDocumentationGenerator = this.getNewRequestParameters();
        urlDocumentationGenerator.setPage(RequestParameters.PAGE_DOCUMENTATION);
        RequestParameters urlPrivacyPolicy = this.getNewRequestParameters();
        urlPrivacyPolicy.setPage(RequestParameters.PAGE_PRIVACY_POLICY);

        String version = this.getWebAppVersion();
        String title = "Bgee ";
        if (version != null) {
            title += "release " + version + " ";
        }
        title += "about page";
        String description = title + ".";
        this.startDisplay(title, "AboutPage", description);

        this.writeln("<h1 property='schema:name'>About</h1>");

        this.writeln("<div class='row'>");

        this.writeln("<div class='" + CENTERED_ELEMENT_CLASS + "'>");

        this.writeln("<h2 property='schema:description'>What is Bgee?</h2>");

        this.writeln("<p>Bgee is a database for retrieval and comparison of gene expression patterns "
                + "across multiple animal species. It provides an intuitive answer to the question "
                + "\"where is a gene expressed?\" and supports research in cancer and agriculture "
                + "as well as evolutionary biology.</p>"
                + "<ul>"
                + "<li>Bgee data are produced from multiple data types "
                + "(RNA-Seq, Affymetrix, <em>in situ</em> hybridization, EST data), "
                + "and multiple data sets, that are all integrated consistently to provide a single answer "
                + "to the question: \"where is this gene expressed?\"</li>"
                + "<li>Bgee is based exclusively on curated \"normal\", healthy wild-type expression data "
                + "(e.g., no gene knock-out, no treatment, no disease), "
                + "to provide a comparable reference of normal gene expression.</li>"
                + "<li>Bgee produces calls of presence/absence of expression, "
                + "and of differential over-/under-expression, "
                + "integrated along with information of gene orthology, and of homology "
                + "between organs. This allows comparisons of expression patterns "
                + "between species.</li>"
                + "</ul>");
        this.writeln("<h4>Bgee provides several tools on this website to study gene expression:</h4>"
                + "<ul>"
                + "<li>a <a href='" + urlGeneSearchGenerator.getRequestURL() +
                "'>gene search</a>, to retrieve the preferred conditions of expression of any gene in Bgee</li>"
                + "<li><a href='" + urlTopAnatGenerator.getRequestURL() +
                "'>TopAnat expression enrichment analysis</a>, to discover the conditions with expression "
                + "over-associated with a list of genes, as compared to the whole genome or a custom background</li>"
                + "<li><a href='" + urlExprCompGenerator.getRequestURL() +
                "'>Expression comparison</a>, to compare expression between genes, within a given species or between multiple species</li>"
                + "</ul>");
        this.writeln("<h4>Bgee also provides two Bioconductor R packages for your analyses: </h4>"
                + "<ul>"
                + "<li><a href='" + BGEEDB_R_PACKAGE_URL + "' title='Bioconductor BgeeDB package' "
                        + "target='_blank'>BgeeDB</a>, "
                        + "allowing to download the Affymetrix and RNA-Seq data and metadata used in Bgee, "
                        + "and to perform TopAnat analyses. </li>"
                + "<li><a href='" + BGEECALL_R_PACKAGE_URL + "' title='Bioconductor BgeeCall package' "
                        + "target='_blank'>BgeeCall</a>, "
                        + "to analyze your own RNA-Seq or scRNA-Seq data and produce calls of presence/absence of expression. </li>"
                + "</ul>");
        this.writeln("<h4>All Bgee data can also be directly downloaded from the relevant pages: </h4>"
                + "<ul>"
                + "<li><a href='" + urlDownloadCallsGenerator.getRequestURL()
                + "'>gene expression calls</a>, providing the integrated summarized "
                + "calls of presence/absence of expression produced by Bgee</li>"
                + "<li><a href='" + urlDownloadProcsGenerator.getRequestURL()
                + "'>processed expression values</a>, allowing you to download the raw data used by Bgee "
                + "along with their annotations</li>"
                + "<li><a href='" + urlDataDownloadGenerator.getRequestURL() + "'>data dumps</a>, "
                + "for more advanced users, providing SQL and RDF dumps of the data found in Bgee.</li>"
                + "</ul>");
        this.writeln("<h4>You might find these videos useful: </h4>"
                + "<ul>"
                + "<li><a href='https://www.youtube.com/watch?v=hbpEJO2IzxA' target='_blank'>"
                + "Bgee, an overview</a>, the introduction of a course on Bgee</li>"
                + "<li><a href='https://www.sib.swiss/about-sib/news/10821-from-v1-to-v14"
                + "-the-gene-expression-database-bgee-under-the-spotlight' target='_blank'>"
                + "Bgee under the spotlight</a>, an interview of the group leaders of Bgee, "
                + "retracing its evolutions</li>"
                + "</ul>");
        this.writeln("<p>More information is provided in the <a href='" +
                urlDocumentationGenerator.getRequestURL() + "'>documentation</a>.</p>");

        this.writeln("<h2>Who are we?</h2>");

        this.writeln("<p>Bgee is developed by the " +
                "<a href='https://bioinfo.unil.ch' title='External link to the Robinson-Rechavi " +
                "group webpage' target='_blank' rel='noopener'>Evolutionary Bioinformatics group</a>, part of " +
                "the <a href='https://www.sib.swiss/' title='External link to SIB' target='_blank' rel='noopener'>" +
                "SIB Swiss Institute of Bioinformatics</a>, at the "
                + "<a href='https://www.unil.ch/central/en/home.html' title='External link to UNIL' "
                + "target='_blank' rel='noopener'>University of Lausanne</a>.</p>" +

                "<p>Our main interest is in the evolution of animal genomes in the context of " +
                "organismal function and development. We have special interests in the early " +
                "evolution of chordates and fishes. We have the aim of producing a database "
                + "useful to disciplines such as comparative genomics, Evo-Devo, "
                + "or transcriptome studies, whilst providing an improved integration "
                + "of homology and related concepts into bioinformatics through ontologies "
                + "and ontology tools.</p>");

        this.writeln("<h2>How to cite us?</h2>");

        this.writeln("<p>The list of all Bgee related publications including the most recent one to use to "
                + "cite us are present in the dedicated <a href='" + urlPublicationsGenerator.getRequestURL() +
                "' title ='Bgee publications' >Bgee publications</a> page.</p>");

        this.writeln("<h2>Which license did we choose?</h2>");

        this.writeln("<p>" +
                "   To the extent possible under law, Bgee team has waived all copyright and related " +
                "   or neighboring rights to Bgee project. This work is published under the " +
                "   <a href='" + LICENCE_CC0_URL + "' target='_blank' rel='noopener'>" +
                "       Creative Commons Zero license (CC0)</a> from Switzerland. " +
                "   Although CC0 doesn’t legally require users of the data to cite the source, " +
                "   if you intend to use data from Bgee, it would be nice to cite us." +
                "</p>" +
                "<p>" +
                "    <a href='" + LICENCE_CC0_URL + "' target='_blank' rel='noopener'>" +
                "        <img src='" + this.prop.getBgeeRootDirectory() +
                            this.prop.getImagesRootDirectory() + "cc-zero-large.png' alt='CC0' />" +
                "    </a>" +
                "</p>" +
                "<p>" +
                "   Any third party material on this site is the property of its original copyright holders;" +
                "   see notably \"information about original images\" at the bottom of our homepage" +
                "   for the animal photos copyright." +
                "</p>");

        this.writeln("<h2>What is our privacy policy?</h2>");
        this.writeln("<p>You can find all details about our privacy policy in the dedicated page " +
                "<a href='" + urlPrivacyPolicy.getRequestURL() + "'>Bgee privacy notice</a></p>");

        this.writeln("<h2>More</h2>");

        this.writeln("<p>Our pipeline source code, as well as our curation and ontology resources "
                + "can be browsed on <a title='External link to BgeeDB on GitHub' "
                + "href='" + BGEE_GITHUB_URL + "' target='_blank' rel='noopener'>our GitHub page</a>.</p>");

        this.writeln("<p>More information about data analyses and database content is available " +
                "in the <a href='" + urlDocumentationGenerator.getRequestURL() +
                "'>documentation</a>.</p>");

        this.writeln("</div>"); // close specific class
        this.writeln("</div>"); // close row

        this.endDisplay();

        log.traceExit();
    }

    @Override
    protected void includeCss() {
        log.traceEntry();
        super.includeCss();
        //If you ever add new files, you need to edit bgee-webapp/pom.xml
        //to correctly merge/minify them.
        this.includeCss("documentation.css");
        log.traceExit();
    }
}

