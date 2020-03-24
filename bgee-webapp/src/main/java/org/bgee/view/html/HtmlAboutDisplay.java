package org.bgee.view.html;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
        log.entry();

        RequestParameters urlDownloadCallsGenerator = this.getNewRequestParameters();
        urlDownloadCallsGenerator.setPage(RequestParameters.PAGE_DOWNLOAD);
        urlDownloadCallsGenerator.setAction(RequestParameters.ACTION_DOWLOAD_CALL_FILES);
        RequestParameters urlDownloadProcsGenerator = this.getNewRequestParameters();
        urlDownloadProcsGenerator.setPage(RequestParameters.PAGE_DOWNLOAD);
        urlDownloadProcsGenerator.setAction(RequestParameters.ACTION_DOWLOAD_PROC_VALUE_FILES);
        RequestParameters urlDownloadMysqlGenerator = this.getNewRequestParameters();
        urlDownloadMysqlGenerator.setPage(RequestParameters.PAGE_DOWNLOAD);
        urlDownloadMysqlGenerator.setAction(RequestParameters.ACTION_DOWNLOAD_MYSQL_DUMPS);
        RequestParameters urlGeneSearchGenerator = this.getNewRequestParameters();
        urlGeneSearchGenerator.setPage(RequestParameters.PAGE_GENE);
        RequestParameters urlTopAnatGenerator = this.getNewRequestParameters();
        urlTopAnatGenerator.setPage(RequestParameters.PAGE_TOP_ANAT);
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
        this.startDisplay(title, "AboutPage");

        this.writeln("<h1 property='schema:name'>About</h1>");

        this.writeln("<div class='row'>");

        this.writeln("<div class='" + CENTERED_ELEMENT_CLASS + "'>");

        this.writeln("<h2 property='schema:description'>What is Bgee?</h2>");

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
                "'>gene search</a> or <a href='" + urlTopAnatGenerator.getRequestURL() +
                "'>expression enrichment analysis</a>. It is also possible to download <a href='" +
                urlDownloadCallsGenerator.getRequestURL() + "'>gene expression calls</a>, <a href='" +
                urlDownloadProcsGenerator.getRequestURL() + "'>processed expression values</a>, or " +
                "<a href='" + urlDownloadMysqlGenerator.getRequestURL() + "'>MySQL dumps</a>.</p>");
        this.writeln("<p>More information is provided in the <a href='" +
                urlDocumentationGenerator.getRequestURL() + "'>documentation</a>.</p>");

        this.writeln("<h2>Who are we?</h2>");

        this.writeln("<p>Bgee is developed by the " +
                "<a href='http://bioinfo.unil.ch' title='External link to the Robinson-Rechavi " +
                "group webpage' target='_blank' rel='noopener'>Evolutionary Bioinformatics group</a>, part of " +
                "the <a href='https://www.sib.swiss/' title='External link to SIB' target='_blank' rel='noopener'>" +
                "SIB Swiss Institute of Bioinformatics</a>, at the "
                + "<a href='http://www.unil.ch/central/en/home.html' title='External link to UNIL' "
                + "target='_blank' rel='noopener'>University of Lausanne</a>.</p>" +

                "<p>Our main interest is in the evolution of animal genomes in the context of " +
                "organismal function and development. We have special interests in the early " +
                "evolution of chordates and fishes. We have the aim of producing a database "
                + "useful to disciplines such as comparative genomics, Evo-Devo, "
                + "or transcriptome studies, whilst providing an improved integration "
                + "of homology and related concepts into bioinformatics through ontologies "
                + "and ontology tools.</p>");

        this.writeln("<h2>How to cite us?</h2>");

        this.writeln("<ul>");
        this.writeln("<li typeof='schema:ScholarlyArticle'>For the use of Bgee: "
                + "<br>" + this.getAuthors(Arrays.asList("Bastian FB", "Parmentier G", "Roux J",
                    "Moretti S", "Laudet V", "Robinson-Rechavi M"))
                + "<br>" + this.getTitle("Bgee: Integrating and Comparing Heterogeneous Transcriptome Data Among Species")
                + "<br><em>in</em> " + this.getPeriodical("DILS: Data Integration in Life Sciences")
                + " <strong>Lecture Notes in Computer Science</strong>. "
                + "5109:124-131. [<a href='https://link.springer.com/chapter/10.1007/978-3-540-69828-9_12' "
                + "title='Bgee paper in LNCS' target='_blank' rel='noopener' property='schema:url'>url</a>] "
                + "<a href='ftp://ftp.bgee.org/general/citation01.ris' property='schema:sameAs'>RIS</a></li>");
        this.writeln("<li typeof='schema:ScholarlyArticle'>For UBERON: "
                + "<br>" + this.getAuthors(Arrays.asList("Haendel MA", "Balhoff JP", "Bastian FB",
                    "Blackburn DC", "Blake JA", "Bradford Y", "Comte A", "Dahdul WM", "Dececchi TA",
                    "Druzinsky RE", "Hayamizu TF", "Ibrahim N", "Lewis SE", "Mabee PM", "Niknejad A",
                    "Robinson-Rechavi M", "Sereno PC", "Mungall CJ"))
                + "<br>" + this.getTitle("Unification of multi-species vertebrate anatomy ontologies for comparative biology in Uberon")
                + "<br><em>in</em> " + this.getPeriodical("J Biomed Semantics") + " (2014): 5:21. "
                + "[<a target='_blank' rel='noopener' href='https://www.ncbi.nlm.nih.gov/pmc/articles/PMC4089931/' "
                + "title='Unification of multi-species vertebrate anatomy ontologies for comparative biology in Uberon'>url</a>] "
                + "<a href='ftp://ftp.bgee.org/general/citation04.ris'>RIS</a></li>");
        this.writeln("<li typeof='schema:ScholarlyArticle'>For the use of the BgeeDB R package: "
                + "<br>" + this.getAuthors(Arrays.asList("Komljenovic A", "Roux J", "Wollbrett J",
                    "Robinson-Rechavi M", "Bastian F"))
                + "<br>" + this.getTitle("BgeeDB, an R package for retrieval of curated expression datasets and "
                + "for gene list enrichment tests")
                + "<br><em>in</em> " + this.getPeriodical("F1000Research") + " 2018, 5:2748. "
                + "[<a target='_blank' rel='noopener' href='https://f1000research.com/articles/5-2748/v2' "
                + "title='BgeeDB, an R package for retrieval of curated expression datasets and "
                + "for gene list enrichment tests' property='schema:url'>url</a>] "
                + "<a href='ftp://ftp.bgee.org/general/citation05.ris' property='schema:sameAs'>RIS</a></li>");
        this.writeln("</ul>");

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

        log.exit();
    }

    private String getTitle(String title) {
        log.entry(title);
        return log.exit("<span property='schema:headline'>" + title + "</span>.");
    }

    private String getAuthors(List<String> names) {
        log.entry(names);
        return log.exit(names.stream().map(this::getAuthor).collect(Collectors.joining(", ", "", ".")));
    }

    private String getAuthor(String name) {
        log.entry(name);
        return log.exit(
                "<span property='schema:author' typeof='schema:Person'>" +
                "    <span property='schema:name'>" + name + "</span>" +
                "</span>");
    }

    private String getPeriodical(String journalName) {
        log.entry(journalName);
        return log.exit(
                "<span property='schema:isPartOf' typeof='schema:Periodical'>" +
                "    <span property='schema:name'>" + journalName + "</span>" +
                "</span>.");
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

