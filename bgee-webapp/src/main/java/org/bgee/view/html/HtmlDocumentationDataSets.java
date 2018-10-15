package org.bgee.view.html;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This class encapsulates the documentation for data sets. 
 * It does not implement any interface from the {@code view} package, it is meant to be used 
 * by the class {@link HtmlDocumentationDisplay}. The only reason is that the class 
 * {@code HtmlDocumentationDisplay} was getting too large and too complicated. 
 *
 * @author  Valentine Rech de Laval
 * @see 	HtmlDocumentationDisplay
 * @version Bgee 14, May 2018
 * @since   Bgee 14, May 2018
 */
public class HtmlDocumentationDataSets extends HtmlDocumentationDownloadFile {
    
    private static final Logger log = LogManager.getLogger(HtmlDocumentationDataSets.class.getName());

    /**
     * Default constructor.
     *
     * @param response          A {@code HttpServletResponse} that will be used to display the
     *                          page to the client
     * @param requestParameters The {@code RequestParameters} that handles the parameters of the
     *                          current request.
     * @param prop              A {@code BgeeProperties} instance that contains the properties
     *                          to use.
     * @param factory           The {@code HtmlFactory} that instantiated the
     *                          {@link HtmlDocumentationDisplay} object using this object.
     * @throws IllegalArgumentException If {@code factory} is {@code null}.
     * @throws IOException              If there is an issue when trying to get or to use the
     *                                  {@code PrintWriter}
     */
    protected HtmlDocumentationDataSets(HttpServletResponse response, RequestParameters requestParameters,
                                        BgeeProperties prop, HtmlFactory factory)
            throws IllegalArgumentException, IOException {
        super(response, requestParameters, prop, factory);
    }

    /**
     * Write the documentation for data sets imported in Bgee. This method is not responsible 
     * for calling the methods {@code startDisplay} and {@code endDisplay}. Otherwise, 
     * all other elements of the page are written by this method.
     *
     * @see HtmlDocumentationDisplay#displayDataSets() 
     */
    protected void writeDocumentation() {
        log.entry();

        RequestParameters urlExprCalls = this.getNewRequestParameters();
        urlExprCalls.setPage(RequestParameters.PAGE_DOWNLOAD);
        urlExprCalls.setAction(RequestParameters.ACTION_DOWLOAD_CALL_FILES);

        RequestParameters urlSearch = this.getNewRequestParameters();
        urlSearch.setPage(RequestParameters.PAGE_GENE);

        RequestParameters urlTopAnat = this.getNewRequestParameters();
        urlTopAnat.setPage(RequestParameters.PAGE_TOP_ANAT);
        
        this.writeln("<h1>GTEx data into Bgee</h1>");

        this.writeln("<div id='bgee_introduction'><p>In addition to the continuous growth of transcriptomics datasets, "
                + "some specific projects produce large amounts of data, generated and accessible "
                + "in a consistent manner, as, notably, "
                + "the <a href='https://www.gtexportal.org/home/' title='GTEx portal' target='_blank'>" +
                "GTEx project</a>. The GTEx project aims at building a comprehensive resource for tissue-specific "
                + "gene expression in human. Here we describe how this dataset was integrated into Bgee.</p></div>");

        this.writeln("<h2>Annotation process</h2>");
        this.writeln("<div class='doc_content'>");
        this.writeln("    <p>We applied a stringent re-annotation process to the GTEx data to retain " +
                "only healthy tissues and non-contaminated samples, using the information available " +
                "under restricted-access. For instance, we rejected all samples for 31&#37; of subjects, " +
                "deemed globally unhealthy from the pathology report (e.g., drug abuse, diabetes, BMI &#62; 35), " +
                "as well as specific samples from another 28&#37; of subjects who had local pathologies " +
                "(e.g., brain from Alzheimer patients). We also rejected samples with contamination " +
                "from other tissues.</p>" +
                "        <p>In total, only 50&#37; of samples were kept; these represent a high quality " +
                "subset of GTEx. All these samples were re-annotated manually to specific Uberon " +
                "anatomy and aging terms.<p>");
        this.writeln("</div>");

        this.writeln("<h2>GTEx data into Bgee</h2>");
        this.writeln("<div class='doc_content'>");
        this.writeln("<p>All corresponding RNA-seq were reanalyzed in the Bgee pipeline, " +
                "consistently with all other healthy RNA-seq from human and other species. " +
                "These data are being made available both through the website, " +
                "and through <a href='https://bioconductor.org/packages/release/bioc/html/BgeeDB.html' " +
                "class='external_link' target='_blank'>BgeeDB R package</a> " +
                "(with sensitive information hidden).</p>");
        this.writeln("</div>");

        this.writeln("<h3>GTEx data into our website</h3>");
        this.writeln("<div class='doc_content'>");
        this.writeln("<ul>");
        this.writeln("  <li>Annotations can be retrieved from <a href='"+ this.prop.getFTPRootDirectory() +
                "/download/processed_expr_values/rna_seq/Homo_sapiens/Homo_sapiens_RNA-Seq_experiments_libraries.zip' " +
                "title='Retrieve human RNA-Seq data per experiment'>RNA-Seq human experiments/libraries info</a>. " +
                "Experiment ID of GTEx is 'SRP012682'.</li>");
        this.writeln("  <li>Processed expression values, from GTEx only, are available on our FTP " +
                "(<a href='" + this.prop.getFTPRootDirectory() + "/download/processed_expr_values/" +
                "rna_seq/Homo_sapiens/Homo_sapiens_RNA-Seq_read_counts_TPM_FPKM_SRP012682.tsv.zip'>" +
                "download file</a>).</li>");
        this.writeln("  <li>Gene expression calls are included into <a href='" + urlExprCalls.getRequestURL() +
                "#id1'>human files</a></li>");
        this.writeln("  <li>Each human gene page includes GTEx data if there is any " +
                "(search a gene <a href='" + urlSearch.getRequestURL() + "'>here</a>).</li>");
        this.writeln("  <li>TopAnat analyses can be performs <a href='" + urlTopAnat.getRequestURL() +
                "'>here</a>, which leverage the power of the abundant GTEx data integrated " +
                "with many smaller datasets to provide biological insight into gene lists.</li>");
        this.writeln("</ul>");
        this.writeln("</div>");

        this.writeln("<h3>GTEx data using R package</h3>");
        this.writeln("<div class='doc_content'>");
        this.writeln("<ul>");
        this.writeln("  <li>Annotations can be retrieved from RNA-Seq human experiments/libraries " +
                "information. Experiment ID of GTEx is 'SRP012682'.");
        this.writeln("<pre><code>{");
        this.writeln("    bgee <- Bgee$new(species = \"Homo_sapiens\", dataType = \"rna_seq\")");
        this.writeln("    myAnnotation <- getAnnotation(bgee)");
        this.writeln("}");
        this.writeln("</code></pre></li>");

        this.writeln("  <li>Quantitative expression data and presence calls for GTEx can be loaded.");
        this.writeln("<pre><code>{");
        this.writeln("    bgee <- Bgee$new(species = \"Homo_sapiens\", dataType = \"rna_seq\")");
        this.writeln("    dataGTEx <- getData(bgee, experimentId = \"SRP012682\")");
        this.writeln("}");
        this.writeln("</code></pre></li>");

        this.writeln("  <li>TopAnat analyses can be performs, which leverage the power of the " +
                "abundant GTEx data integrated with many smaller datasets to provide biological " +
                "insight into gene lists.");
        this.writeln("<pre><code>{");
        this.writeln("    bgee <- Bgee$new(species = \"Homo_sapiens\", dataType = \"rna_seq\")");
        this.writeln("    myTopAnatData <- loadTopAnatData(bgee)");
        this.writeln("    geneList <- as.factor(c(rep(0, times=90), rep(1, times=10)))");
        this.writeln("    names(geneList) <- c(\"ENSMUSG00000064370\", \"ENSMUSG00000064368\", \"ENSMUSG00000064367\")");
        this.writeln("    myTopAnatObject <- topAnat(myTopAnatData, geneList, nodeSize=1)");
        this.writeln("}");
        this.writeln("</code></pre></li>");
        this.writeln("</ul>");
        this.writeln("</div>");

        log.exit();
    }
}
