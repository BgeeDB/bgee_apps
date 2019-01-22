package org.bgee.view.html;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.view.FaqDisplay;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This class is the HTML implementation of the {@code FaqDisplay}.
 *
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Aug. 2018
 * @since   Bgee 14, June 2018
 */
public class HtmlFaqDisplay extends HtmlParentDisplay implements FaqDisplay {

    private final static Logger log = LogManager.getLogger(HtmlFaqDisplay.class.getName());
    
    public HtmlFaqDisplay(HttpServletResponse response, RequestParameters requestParameters,
                          BgeeProperties prop, HtmlFactory factory)
            throws IllegalArgumentException, IOException {
        super(response, requestParameters, prop, factory);
    }

    @Override
    public void writeFaqPage() {
        log.entry();

        RequestParameters urlCallPage = this.getNewRequestParameters();
        urlCallPage.setPage(RequestParameters.PAGE_DOWNLOAD);
        urlCallPage.setAction(RequestParameters.ACTION_DOWLOAD_CALL_FILES);
        RequestParameters urlValuePage = this.getNewRequestParameters();
        urlValuePage.setPage(RequestParameters.PAGE_DOWNLOAD);
        urlValuePage.setAction(RequestParameters.ACTION_DOWLOAD_PROC_VALUE_FILES);
        RequestParameters urlGenePage = this.getNewRequestParameters();
        urlGenePage.setPage(RequestParameters.PAGE_GENE);
        RequestParameters urlTopAnat = this.getNewRequestParameters();
        urlTopAnat.setPage(RequestParameters.PAGE_TOP_ANAT);
        
        this.writeln("<h1>Frequently asked questions (FAQ)</h1>");

        this.writeln("<div id='bgee_introduction'>");
        this.writeln("<p>Got questions? We’ve got answers! Here, you can find Bgee team answers " +
                "in response to the most frequently asked questions. " +
                "If you don't find answers here, please do not hesitate to contact us, " +
                "using " + getObfuscateBgeeEmail() + ".</p>");
        this.writeln("</div>");

        this.writeln("<div id='faq-list' class='panel-group' role='tablist' aria-multiselectable='true'>");
        
        this.writeln(this.getQuestionDisplay(
                "Are all tissues tested in every species, e.g. in both mouse and rat?",
                "We integrate publicly available data, and different species are studied in more " +
                        "or less details. Only tissues with detected active expression are displayed " +
                        "in the gene page on our website. If you use the files available for  " +
                        "download (from here <a href='" + urlCallPage.getRequestURL() + "'>" + 
                        GENE_EXPR_CALLS_PAGE_NAME + "</a>), you can see report of tissues " +
                        "with absence of expression. This will give you a definitive " +
                        "answer about which, e.g., mouse/rat tissues were studied.",
                "TissuesTested"));

        this.writeln(this.getQuestionDisplay("Why do you use chimpanzee gene IDs for bonobo data?",
                "When generating data for Bgee release 14, the bonobo genome was not yet available in Ensembl. " +
                        "So bonobo RNA-Seq libraries are mapped to the chimpanzee genome, " +
                        "and 'ENSPTRG' chimpanzee gene IDs are reported. Since the bonobo genome " +
                        "has been made available December 2017 in Ensembl, Bgee release 15 will use " +
                        "the actual bonobo genome as reference. In the meantime, you can use Ensembl " +
                        "tools to retrieve a mapping from chimpanzee genes to bonobo orthologs. " +
                        "We are sorry for the inconvenience.",
                "ChimpAndBonobo"));

        this.writeln(this.getQuestionDisplay(
                "Why are there differences between interface results and downloadable files?",
                "This depends on the way the query is submitted. " +
                        "If you are only interested in gene expression in anatomical entities, " +
                        "ignoring developmental stages, you will see some differences between " +
                        "the gene Web page and the download files: for a given anatomical entity " +
                        "the gene Web page shows the lowest rank of all pairs of anatomical " +
                        "entity-developmental stages (you can click on the '+' button " +
                        "to see the ranks for all stages of a given anatomical entity). " +
                        "For the moment, the download files do not contain the minimum expression value. " +
                        "We are probably going to modify our download files to be coherent with the approach.<br>" +
                        "Furthermore, in the Web application some anatomical entities are filtered. " +
                        "If one gene is expressed in one anatomical entity (brain) but is more expressed " +
                        "in a subpart of this anatomical entity (cerebellum), then we remove the " +
                        "anatomical entity itself (brain) from the gene web page and only keep " +
                        "the gene expression value of the subpart (cerebellum).",
                "Differences"));

        this.writeln(this.getQuestionDisplay("Why don’t you use standard names for developmental stages?",
                "<p>We use as input one specific developmental stage ontology for each species, " +
                        "and we then merge all these species-specific developmental ontologies into " +
                        "one single multi-species ontology. To do that, we use broad developmental stages " +
                        "described in the <a href='http://uberon.org' class='external_link' target='_blank'>" +
                        "Uberon ontology</a>: either we map some of the species-specific stages " +
                        "to these Uberon broad developmental stages (i.e., equivalent classes), " +
                        "or we attach some of the species-specific stages as children of " +
                        "these Uberon developmental stages (i.e., subclasses).</p>" +
                        "<p>You can find all 'source' species-specific ontologies we develop " +
                        "   <a href='https://github.com/obophenotype/developmental-stage-ontologies/tree/master/src' " +
                        "      class='external_link' target='_blank'>here</a>. " +
                        "   For <i>C. elegans</i>, we rely on the " +
                        "   <a href='http://www.obofoundry.org/ontology/wbls.html' class='external_link' target='_blank'>" +
                        "   WBls ontology</a> developed by WormBase " +
                        "   (we also rely on external ontologies for fly, zebrafish, and xenopus).</p>" +
                        "<p>You can find the ontology merging all these species-specific ontologies with Uberon " +
                        "   <a href='https://github.com/obophenotype/developmental-stage-ontologies/blob/master/external/bgee/dev_stage_ontology.obo' class='external_link' target='_blank'>" +
                        "   here</a>. " +
                        "   You can find an overview of the resulting merge for <i>C. elegans</i> " +
                        "   <a href='https://github.com/obophenotype/developmental-stage-ontologies/blob/master/external/bgee/report.md#caenorhabditis-elegans'>" +
                        "   here</a>.</p>" +
                        "<p>In the ontology, developmental stages are ordered thanks to the use of " +
                        "   the relations 'preceded_by' and 'immediately_preceded_by'.</p>" +
                        "<p>An example of apparent non-standard nomenclature arises for <i>C. elegans</i>: " +
                        "   a specific <i>C. elegans</i> developmental stage is mapped to a broad Uberon stage. " +
                        "   For instance, several WBls stages are mapped to the same Uberon term " +
                        "   'UBERON:0000092' (post-embryonic stage): 'WBls:0000022' (postembryonic Ce), " +
                        "   'WBls:0000093' (Brugia postembryonic stage), 'WBls:0000103' (postembryonic nematode); " +
                        "   these mappings are cross-references in the ontology file.</p>" +
                        "<p>Similarly, some Uberon terms have no equivalent in WBls, as for instance " +
                        "   'UBERON:0007220' (late embryonic stage). In that case, we mapped the terms " +
                        "   'WBls:0000015' (elongating embryo Ce) and 'WBls:0000021' (fully-elongated embryo Ce) " +
                        "   as children of 'UBERON:0007220'. As a result, the non-standard term " +
                        "   (late embryonic stage) will show up in the nomenclature for <i>C. elegans</i>.</p>",
                "StageNames"));

        this.writeln(this.getQuestionDisplay("Can I find information on strain and/or sex?",
                "Bgee contains only manually curated healthy expression data (e.g., no gene knock-out, " +
                        "no treatment, no disease). Currently (Bgee release 14), information on " +
                        "strain or sex is not available in files that provides calls of baseline " +
                        "presence/absence of expression (see <a href='" + urlCallPage.getRequestURL() + "'>" + 
                        GENE_EXPR_CALLS_PAGE_NAME + "</a>). However information is available in files " +
                        "that provides annotations and experiment information or processed expression values " +
                        "(see <a href='" + urlValuePage.getRequestURL() + "'>"+PROCESSED_EXPR_VALUES_PAGE_NAME+"</a>). " +
                        "It is also possible to download these data directly into R using " +
                        "<a href='https://bioconductor.org/packages/release/bioc/html/BgeeDB.html' class='external_link' target='_blank'>" +
                        "our R package </a>, or in the <a href='" + this.prop.getFTPRootDirectory() +
                        "sql_dump.tar.gz'>full Bgee database dump file</a>.",
                "MoreInfo"));

        this.writeln(this.getQuestionDisplay("Are there multi-species comparison available in gene expression calls?",
                "These files are not currently available.",
                "MultiSpecies"));

        this.writeln(this.getQuestionDisplay("What can I do with my genes of interest?",
                "Each gene can be visualized one by one via <a href='" + urlGenePage.getRequestURL() + "'>our gene search</a>. " +
                        "You can also visualize enrichment of expression of your list relative to " +
                        "a random background using <a href='" + urlTopAnat.getRequestURL() + "'>TopAnat</a>. " +
                        "All associated data can be downloaded using our " +
                        "<a href='https://www.bioconductor.org/packages/BgeeDB/' class='external_link' target='_blank'>R package</a>. " +
                        "Note that there is at present no way to visualize a list of genes.",
                "OneGene"));

        this.writeln(this.getQuestionDisplay("Do you have protein expression?",
                "No, Bgee only includes RNA level expression data. <i>In situ</i> means <i>in situ</i> hybridization of RNA only.",
                "ProteinExpr"));
        
        this.writeln("</div>"); // close faq-list

        log.exit();
    }

    /**
     * Build one panel according to a question, an answer, and a suffix for the panel ID.  
     * 
     * @param question      A {@code String} that is the question of the panel to be displayed.
     * @param answer        A {@code String} that is the answer of the panel to be displayed.
     * @param panelIdSuffix A {@code String} that is the suffix of the panel ID to be displayed.
     * @return              The {@code String} that is panel to be displayed.
     */
    private String getQuestionDisplay(String question, String answer, String panelIdSuffix) {
        log.entry(question, answer, panelIdSuffix);

        StringBuilder sb = new StringBuilder();
        sb.append("  <div class='panel panel-default'>");
        sb.append("    <div class='panel-heading' role='tab' id='heading").append(panelIdSuffix).append("Query'>");
        sb.append("      <div class='panel-title'>");
        sb.append("        <a href='#collapse").append(panelIdSuffix).append("Query' class='collapsed' " +
                "role='button' data-toggle='collapse' data-parent='#faq-list' " +
                "aria-expanded='false' aria-controls='collapse").append(panelIdSuffix).append("Query'>");
        sb.append(question);
        sb.append("        </a>");
        sb.append("      </div>");
        sb.append("    </div>");
        sb.append("    <div id='collapse").append(panelIdSuffix)
                .append("Query' class='panel-collapse collapse' role='tabpanel' aria-labelledby='collapse")
                .append(panelIdSuffix).append("Query'>");
        sb.append("      <div class='panel-body'>");
        sb.append(answer);
        sb.append("      </div>");
        sb.append("    </div>");
        sb.append("  </div>");

        return log.exit(sb.toString());
    }
}
