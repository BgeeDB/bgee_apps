package org.bgee.view.html;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.view.DocumentationDisplay;


/**
 * This class displays the page having the category "documentation", i.e. with the parameter
 * page=documentation for the {@code displayTypes} HTML.
 *
 * @author  Valentine Rech de Laval
 * @version Bgee 13
 * @since   Bgee 13
 */
public class HtmlDocumentationDisplay extends HtmlParentDisplay implements DocumentationDisplay {

    private final static Logger log = LogManager.getLogger(HtmlDocumentationDisplay.class.getName());

    /**
     * A {@code String} that is the name of the gene ID column in download files, 
     * HTML escaped if necessary.
     * @see #GENE_ID_LINK_TITLE
     */
    private final String GENE_ID_COL_NAME ="Gene ID";
    /**
     * A {@code String} to be used in {@code title} attribute of {@code a} tag linking to 
     * gene ID column description (used several times), HTML escaped if necessary.
     * @see #GENE_ID_COL_NAME
     */
    private final String GENE_ID_LINK_TITLE = "See " + GENE_ID_COL_NAME + " column description";
    /**
     * A {@code String} that is the name of the gene name column in download files, 
     * HTML escaped if necessary.
     * @see #GENE_NAME_LINK_TITLE
     */
    private final String GENE_NAME_COL_NAME ="Gene name";
    /**
     * A {@code String} to be used in {@code title} attribute of {@code a} tag linking to 
     * gene name column description (used several times), HTML escaped if necessary.
     * @see #GENE_NAME_COL_NAME
     */
    private final String GENE_NAME_LINK_TITLE = "See " + GENE_NAME_COL_NAME + " column description";
    /**
     * A {@code String} that is the name of the stage ID column in download files, 
     * HTML escaped if necessary.
     * @see #STAGE_ID_LINK_TITLE
     */
    private final String STAGE_ID_COL_NAME ="Developmental stage ID";
    /**
     * A {@code String} to be used in {@code title} attribute of {@code a} tag linking to 
     * stage ID column description (used several times), HTML escaped if necessary.
     * @see #STAGE_ID_COL_NAME
     */
    private final String STAGE_ID_LINK_TITLE = "See " + STAGE_ID_COL_NAME + " column description";
    /**
     * A {@code String} that is the name of the stage name column in download files, 
     * HTML escaped if necessary.
     * @see #STAGE_NAME_LINK_TITLE
     */
    private final String STAGE_NAME_COL_NAME ="Developmental stage name";
    /**
     * A {@code String} to be used in {@code title} attribute of {@code a} tag linking to 
     * stage name column description (used several times), HTML escaped if necessary.
     * @see #STAGE_NAME_COL_NAME
     */
    private final String STAGE_NAME_LINK_TITLE = "See " + STAGE_NAME_COL_NAME 
            + " column description";
    /**
     * A {@code String} that is the name of the anatomical entity ID column in download files, 
     * HTML escaped if necessary.
     * @see #ANAT_ENTITY_ID_LINK_TITLE
     */
    private final String ANAT_ENTITY_ID_COL_NAME ="Anatomical entity ID";
    /**
     * A {@code String} to be used in {@code title} attribute of {@code a} tag linking to 
     * anatomical entity ID column description (used several times), HTML escaped if necessary.
     * @see #ANAT_ENTITY_ID_COL_NAME
     */
    private final String ANAT_ENTITY_ID_LINK_TITLE = "See " + ANAT_ENTITY_ID_COL_NAME 
            + " column description";
    /**
     * A {@code String} that is the name of the anatomical entity name column in download files, 
     * HTML escaped if necessary.
     * @see #ANAT_ENTITY_NAME_LINK_TITLE
     */
    private final String ANAT_ENTITY_NAME_COL_NAME ="Anatomical entity name";
    /**
     * A {@code String} to be used in {@code title} attribute of {@code a} tag linking to 
     * anatomical entity name column description (used several times), HTML escaped if necessary.
     * @see #ANAT_ENTITY_NAME_COL_NAME
     */
    private final String ANAT_ENTITY_NAME_LINK_TITLE = "See " + ANAT_ENTITY_NAME_COL_NAME 
            + " column description";
    /**
     * A {@code String} that is the name of the expression state column in download files, 
     * HTML escaped if necessary.
     * @see #EXPR_STATE_LINK_TITLE
     */
    //TODO: split into two columns once we regenerate the download files
    private final String EXPR_STATE_COL_NAME = "Expression state";
    /**
     * A {@code String} to be used in {@code title} attribute of {@code a} tag linking to 
     * expression state column description (used several times), HTML escaped if necessary.
     * @see #EXPR_STATE_COL_NAME
     */
    private final String EXPR_STATE_LINK_TITLE = "See " + EXPR_STATE_COL_NAME 
            + " column description";

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
     * @throws IOException      If there is an issue when trying to get or to use the
     *                          {@code PrintWriter}.
     */
    public HtmlDocumentationDisplay(HttpServletResponse response,
            RequestParameters requestParameters, BgeeProperties prop) throws IOException {
        super(response, requestParameters, prop);
    }

    @Override
    //XXX: anchors should have meaning: a section1 more easily become a section2 one day (unstable links)
    public void displayDocumentationHomePage() {
        log.entry();
        
        String subsectionName1 = "Subsection 1 - H2 section";
        String subsectionName2 = "Subsection 2 - H2 section";
        String subsectionName3 = "Subsection 3 - H2 section";
        
        this.startDisplay("download", "Bgee release 13 documentation page");
        
        this.writeln("<h1 id='sectionname'>Section name</h1>");
this.writeln("<div class='documentationmenu'><ul>");
        
        this.writeln("<li><a href='#single' title='Quick jump to this section'>" + 
                            "Single-species download files</a>");
        this.writeln("<ul>");           //presence/absence
        this.writeln("<li><a href='#single_expr' title='Quick jump to this section'>" + 
                            "Presence/absence of expression</a>");
        this.writeln("<ul>");
        this.writeln("<li><a href='#single_expr_simple' title='Quick jump to this section'>" + 
                            "Simple file</a></li>");
        this.writeln("<li><a href='#single_expr_complete' title='Quick jump to this section'>" + 
                            "Complete file</a></li>");
        this.writeln("</ul></li>");     //end of presence/absence
        this.writeln("<li><a href='#single_diff' title='Quick jump to this section'>" + 
                "Over-/under-expression across anatomy or life stages</a>");
        this.writeln("<ul>");
        this.writeln("<li><a href='#single_diff_simple' title='Quick jump to this section'>" + 
                        "Simple file</a></li>");
        this.writeln("<li><a href='#single_diff_complete' title='Quick jump to this section'>" + 
                        "Complete file</a></li>");
        this.writeln("</ul></li>");     //end of diff expression 
        this.writeln("</ul></li>"); // end of single-species section
        
        this.writeln("<li><a href='#multi' title='Quick jump to this section'>" + 
                            "Multi-species download files</a>");
        this.writeln("<ul>");    
        this.writeln("<li><a href='#multi_diff' title='Quick jump to this section'>" + 
                "Over-/under-expression across anatomy or life stages</a>");
        this.writeln("<ul>");
        this.writeln("<li><a href='#multi_diff_simple' title='Quick jump to this section'>" + 
                        "Simple file</a></li>");
        this.writeln("<li><a href='#multi_diff_complete' title='Quick jump to this section'>" + 
                        "Complete file</a></li>");
        this.writeln("</ul></li>");     //end of diff expression        
        this.writeln("</ul></li>"); // end of multi-species section
        
        this.writeln("</ul></div>");// end of documentationmenu

        this.writeln("<div id='subsection1'>");
        this.writeln("<h2>" + subsectionName1 + "</h2>");
        this.writeln("<div class='documentationsection'>");
        this.writeln("<h3>H3 title</h3>");
        this.writeln("<p>The raw data in .sra format are downloaded from " +
                "the <a href='http://www.ncbi.nlm.nih.gov/sra' title='External link to SRA' " +
                "target='_blank'>Short Read Archive (SRA) database</a>. " +
                "The extracted reads, in fastq format, are mapped to regions of the reference genome, " +
                "specified in a .gtf file: i) transcribed regions; " +
                "ii) selected intergenic regions (see below); iii) exon junction regions. </p>");
        this.writeln("<p>The raw data in .sra format are downloaded from " +
                "the <a href='http://www.ncbi.nlm.nih.gov/sra' title='External link to SRA' " +
                "target='_blank'>Short Read Archive (SRA) database</a>. " +
                "The extracted reads, in fastq format, are mapped to regions of the reference genome, " +
                "specified in a .gtf file: i) transcribed regions; " +
                "ii) selected intergenic regions (see below); iii) exon junction regions. </p>");
        this.writeln("<h3>H3 title</h3>");
        this.writeln("<p>The mapping of the reads is performed using " +
                "<a href='http://tophat.cbcb.umd.edu/' title='External link to TopHat website' " +
                "target='_blank'>TopHat2</a>, " +
                "which internally uses " +
                "the <a href='http://bowtie-bio.sourceforge.net/bowtie2/index.shtml' title='External link to Bowtie website' " +
                "target='_blank'>Bowtie2</a> aligner. The maximum number of mappings allowed for a read " +
                "is set to 1. The intergenic regions are chosen in such a way that the distribution of their lengths " +
                "matches the distribution of lengths of the transcriptome. " +
                "The minimal distance of boundaries of intergenic regions to the nearest gene is 5 kb. " +
                "Reads that map to the features are summed up using the htseq-count software. " +
                "The RPK (read per kilobase) value for every feature is obtained by dividing " +
                "the number of reads that match a given feature by its length. </p>");
        this.writeln("</div>");
        this.writeln("</div>"); // end of subsection1
        
        this.writeln(this.getBackToTheTopLink());
        
        this.writeln("<div id='subsection2'>");
        this.writeln("<h2>" + subsectionName2 + "</h2>");
        this.writeln("<div class='documentationsection'>");
        this.writeln("<p> </p>");
        this.writeln("<p> </p>");
        this.writeln("</div>");
        this.writeln("</div>"); // end of subsection2
        
        this.writeln(this.getBackToTheTopLink());
        
        this.writeln("<div id='subsection3'>");
        this.writeln("<h2>" + subsectionName3 + "</h2>");
        this.writeln("<div class='documentationsection'>");
        this.writeln("<p> </p>");
        this.writeln("<p> </p>");
        this.writeln("</div>");
        this.writeln("</div>"); // end of subsection3
        
        this.writeln(this.getBackToTheTopLink());
        
        this.endDisplay();

        log.exit();
    }
    
    public void displayDownloadFileDocumentation() {
        log.entry();
        
        this.startDisplay("download", "Download file documentation");
        
        this.writeln("<h1 id='sectionname'>Download file documentation</h1>");
        RequestParameters urlDownloadGenerator = this.getNewRequestParameters();
        urlDownloadGenerator.setPage(RequestParameters.PAGE_DOWNLOAD);
        this.writeln("<p class='documentationintro'>Bgee provides calls of baseline "
                + "presence/absence of expression, and of over/under expression, "
                + "either for single species, or compared between species (homologous genes "
                + "in homologous organs). This documentation describes the format of these "
                + "<a href='" + urlDownloadGenerator.getRequestURL()
                + "' title='Bgee expression data page'>download files</a>.</p>");
        
        
        //Documentation menu
        this.writeln("<div class='documentationmenu'><ul>");
            //Single-species
        this.writeln("<li><a href='#single' title='Quick jump to this section'>" + 
                            "Single-species download files</a>");
                //presence/absence
        this.writeln("<ul>");           
        this.writeln("<li><a href='#single_expr' title='Quick jump to this section'>" + 
                            "Presence/absence of expression</a>");
        this.writeln("<ul>");
        this.writeln("<li><a href='#single_expr_simple' title='Quick jump to this section'>" + 
                            "Simple file</a></li>");
        this.writeln("<li><a href='#single_expr_complete' title='Quick jump to this section'>" + 
                            "Complete file</a></li>");
        this.writeln("</ul></li>");         //end of presence/absence
                //diff expression
        this.writeln("<li><a href='#single_diff' title='Quick jump to this section'>" + 
                "Over-/under-expression across anatomy or life stages</a>");
        this.writeln("<ul>");
        this.writeln("<li><a href='#single_diff_simple' title='Quick jump to this section'>" + 
                        "Simple file</a></li>");
        this.writeln("<li><a href='#single_diff_complete' title='Quick jump to this section'>" + 
                        "Complete file</a></li>");
        this.writeln("</ul></li>");         //end of diff expression 
        this.writeln("</ul></li>");     // end of single-species section
        
            //multi-species
        this.writeln("<li><a href='#multi' title='Quick jump to this section'>" + 
                            "Multi-species download files</a>");
                //diff expression
        this.writeln("<ul>");    
        this.writeln("<li><a href='#multi_diff' title='Quick jump to this section'>" + 
                "Over-/under-expression across anatomy or life stages</a>");
        this.writeln("<ul>");
        this.writeln("<li><a href='#multi_diff_simple' title='Quick jump to this section'>" + 
                        "Simple file</a></li>");
        this.writeln("<li><a href='#multi_diff_complete' title='Quick jump to this section'>" + 
                        "Complete file</a></li>");
        this.writeln("</ul></li>");         //end of diff expression        
        this.writeln("</ul></li>");     // end of multi-species section
        
        this.writeln("</ul></div>");// end of documentationmenu
        
        
        //Single species documentation
        this.writeln("<div>");
        this.writeln("<h2 id='single'>Single-species download files</h2>");
        this.writeln("<div class='doc_content'>");
        this.writeln("<p>Jump to: </p>"
                + "<ul>"
                + "<li><a href='#single_expr' title='Quick jump to presence/absence of expression'>"
                + "Presence/absence of expression</a></li>"
                + "<li><a href='#single_diff' title='Quick jump to differential expression'>"
                + "Over-/under-expression across anatomy or life stages</a></li>"
                + "</ul>");
        this.writeln("<h3 id='single_expr'>Presence/absence of expression</h3>");
        //TODO: add link to data analyses documentation
        this.writeln("<p>Bgee provides calls of presence/absence of expression. A call "
                + "corresponds to a gene, with reported presence or absence of expression, "
                + "in an anatomical structure, during a developmental stage. Only \"normal\" "
                + "expression is considered in Bgee (i.e., no treatment, no disease, no gene knock-out, etc.). "
                + "Bgee collects data from different types, from different studies, "
                + "in different organisms, and provides a summary from all these data "
                + "as unique calls <code>gene - anatomical structure - developmental stage</code>.</p>");
        this.writeln("<p>Calls of presence/absence of expression are very similar to the data "
                + "that can be reported using <i>in situ</i> hybridization methods; Bgee applies "
                + "dedicated statistical analyses to generate such calls from EST, Affymetrix, "
                + "and RNA-Seq data, with confidence information, and also collects "
                + "<i>in situ</i> hybridization calls from model organism databases. "
                + "This offers the possibility to aggregate and compare these calls of "
                + "presence/absence of expression between different experiments, "
                + "different data types, and different species, and to benefit from both "
                + "the high anatomy coverage provided by low-throughput methods, "
                + "and the high genomic coverage provided by high-throughput methods.</p>");
        this.writeln("<p>After presence/absence calls are generated from the raw data, "
                + "they are propagated using anatomical and life stage ontologies: </p>"
                + "<ul class='doc_content'>"
                + "<li><span class='list_element_title'>calls of expression</span> "
                + "are propagated to parent anatomical structures "
                + "and parent developmental stages; for instance, if gene A is expressed "
                + "in midbrain at young adult stage, it will also be considered as expressed "
                + "in brain at adult stage;</li>"
                + "<li><span class='list_element_title'>calls of absence of expression</span> "
                + "are propagated to child anatomical structures "
                + "(and not to child developmental stages); for instance, if gene A is reported "
                + "as not expressed in the brain at young adult stage, it will also be considered "
                + "as not expressed in the midbrain at young adult stage. This is only permitted "
                + "when it does not generate any contradiction with expression calls from "
                + "the same data type (for instance, no contradiction permitted of reported "
                + "absence of expression by RNA-Seq, with report of expression by RNA-Seq "
                + "for the same gene, in the same anatomical structure and developmental stage, "
                + "or any child anatomical structure and child developmental stage).</li>"
                + "</ul>");
        this.writeln("<p>Presence/absence calls are then filtered and presented differently "
                + "depending on whether a <code>simple file</code>, "
                + "or a <code>complete file</code> is used. Notably: <code>simple files</code> "
                + "aim at providing summarized information over all data types, and only "
                + "in anatomical structures and developmental stages actually used "
                + "in experimental data; <code>complete files</code> aim at reporting all information, "
                + "allowing for instance to retrieve the contribution of each data type to a call, "
                + "in all possible anatomical structures and developmental stages.</p>");
        this.writeln("<p>Jump to format description for: </p>"
                + "<ul>"
                + "<li><a href='#single_expr_simple' title='Quick jump to simple file description'>"
                + "simple file</a></li>"
                + "<li><a href='#single_expr_complete' title='Quick jump to complete file description'>"
                + "complete file</a></li>"
                + "</ul>");
        
        //simple expression file
        this.writeln("<h4 id='single_expr_simple'>Simple file</h4>");
        this.writeln("<p>In simple files, propagated presence/absence of expression calls "
                + "are provided, but only calls in conditions of anatomical structure/developmental stage "
                + "actually used in experimental data are displayed (no calls generated "
                + "from propagation only).</p>");
        this.writeln("<table class='call_download_file'>");
        this.writeln("<caption>Format description for single species simple expression file</caption>");
        this.writeln("<thead>");
        this.writeln("<tr><th>Column</th><th>Content</th><th>Cardinality</th><th>Example</th></tr>");
        this.writeln("</thead>");
        this.writeln("<tbody>");
        this.writeln("<tr><td>1</td><td><a href='#single_expr_simple_col1' title='" 
                + GENE_ID_LINK_TITLE + "'>" + GENE_ID_COL_NAME 
                + "</a></td><td>1</td><td>ENSG00000001631</td></tr>");
        this.writeln("<tr><td>2</td><td><a href='#single_expr_simple_col2' title='" 
                + GENE_NAME_LINK_TITLE + "'>" + GENE_NAME_COL_NAME 
                + "</a></td><td>1</td><td>KRIT1</td></tr>");
        this.writeln("<tr><td>3</td><td><a href='#single_expr_simple_col3' title='" 
                + STAGE_ID_LINK_TITLE + "'>" + STAGE_ID_COL_NAME 
                + "</a></td><td>1</td><td>HsapDv:0000092</td></tr>");
        this.writeln("<tr><td>4</td><td><a href='#single_expr_simple_col4' title='" 
                + STAGE_NAME_LINK_TITLE + "'>" + STAGE_NAME_COL_NAME 
                + "</a></td><td>1</td><td>human middle aged stage (human)</td></tr>");
        this.writeln("<tr><td>5</td><td><a href='#single_expr_simple_col5' title='" 
                + ANAT_ENTITY_ID_LINK_TITLE + "'>" + ANAT_ENTITY_ID_COL_NAME 
                + "</a></td><td>1</td><td>UBERON:0004720</td></tr>");
        this.writeln("<tr><td>6</td><td><a href='#single_expr_simple_col6' title='" 
                + ANAT_ENTITY_NAME_LINK_TITLE + "'>" + ANAT_ENTITY_NAME_COL_NAME 
                + "</a></td><td>1</td><td>cerebellar vermis</td></tr>");
        this.writeln("<tr><td>7</td><td><a href='#single_expr_simple_col7' title='" 
                + EXPR_STATE_LINK_TITLE + "'>" + EXPR_STATE_COL_NAME 
                + "</a></td><td>1</td><td>expression high quality</td></tr>");
        this.writeln("</tbody>");
        this.writeln("</table>");
        this.writeln("<h5 id='single_expr_simple_col1'>" + GENE_ID_COL_NAME + "</h5>");
        this.writeln(this.getGeneIdColDescription());
        this.writeln("<h5 id='single_expr_simple_col2'>" + GENE_NAME_COL_NAME + "</h5>");
        this.writeln(this.getGeneNameColDescription(1));
        this.writeln("<h5 id='single_expr_simple_col3'>" + STAGE_ID_COL_NAME + "</h5>");
        this.writeln(this.getStageIdColDescription());
        this.writeln("<h5 id='single_expr_simple_col4'>" + STAGE_NAME_COL_NAME + "</h5>");
        this.writeln(this.getStageNameColDescription(3));
        this.writeln("<h5 id='single_expr_simple_col5'>" + ANAT_ENTITY_ID_COL_NAME + "</h5>");
        this.writeln(this.getAnatEntityIdColDescription());
        this.writeln("<h5 id='single_expr_simple_col6'>" + ANAT_ENTITY_NAME_COL_NAME + "</h5>");
        this.writeln(this.getAnatEntityNameColDescription(5));
        this.writeln("<h5 id='single_expr_simple_col7'>" + EXPR_STATE_COL_NAME + "</h5>");
        this.writeln(this.getExprStateColDescription(1, 3, 5));
        
        this.writeln("<h3>H3 title</h3>");
        this.writeln("<p>The mapping of the reads is performed using " +
                "<a href='http://tophat.cbcb.umd.edu/' title='External link to TopHat website' " +
                "target='_blank'>TopHat2</a>, " +
                "which internally uses " +
                "the <a href='http://bowtie-bio.sourceforge.net/bowtie2/index.shtml' title='External link to Bowtie website' " +
                "target='_blank'>Bowtie2</a> aligner. The maximum number of mappings allowed for a read " +
                "is set to 1. The intergenic regions are chosen in such a way that the distribution of their lengths " +
                "matches the distribution of lengths of the transcriptome. " +
                "The minimal distance of boundaries of intergenic regions to the nearest gene is 5 kb. " +
                "Reads that map to the features are summed up using the htseq-count software. " +
                "The RPK (read per kilobase) value for every feature is obtained by dividing " +
                "the number of reads that match a given feature by its length. </p>");
        this.writeln("</div>"); // end of single-species
        
        this.writeln(this.getBackToTheTopLink());
        this.writeln("</div>");
        
        this.writeln("<div id='multi'>");
        this.writeln("<h2>Multi-species download files</h2>");
        this.writeln("<div class='documentationsection'>");
        this.writeln("<p> </p>");
        this.writeln("<p> </p>");
        this.writeln("</div>");
        this.writeln("</div>"); // end of subsection2
        
        this.writeln(this.getBackToTheTopLink());
        
        this.writeln(this.getBackToTheTopLink());
        
        this.endDisplay();

        log.exit();
    }
    
    /**
     * @return  A {@code String} that is the description of the gene ID column 
     *          in download files (because we use it several times), formated in HTML 
     *          and HTML escaped if necessary.
     * @see #GENE_ID_COL_NAME
     */
    private String getGeneIdColDescription() {
        log.entry();
        return log.exit("<p>Unique identifier of gene from Ensembl. </p><p>" 
                + this.getGenomeMappingExplanation() + "</p>");
    }
    /**
     * @param colNumber An {@code int} that is the index of the column containing 
     *                  the gene ID (see {@link #GENE_ID_COL_NAME}). 
     *                  Index starting from 1.
     * @return  A {@code String} that is the description of the gene name column 
     *          in download files (because we use it several times), formated in HTML 
     *          and HTML escaped if necessary.
     * @see #GENE_NAME_COL_NAME
     */
    private String getGeneNameColDescription(int colNumber) {
        log.entry();
        return log.exit("<p>Name of the gene defined by <code>" + GENE_ID_COL_NAME 
                + "</code> (column " + colNumber + ")</p>");
    }
    /**
     * @return  A {@code String} that is the description of the stage ID column 
     *          in download files (because we use it several times), formated in HTML 
     *          and HTML escaped if necessary.
     * @see #STAGE_ID_COL_NAME
     */
    private String getStageIdColDescription() {
        log.entry();
        return log.exit("<p>Unique identifier of the developmental stage, from the Uberon ontology.</p>");
    }
    /**
     * @param colNumber An {@code int} that is the index of the column containing 
     *                  the stage ID (see {@link #STAGE_ID_COL_NAME}). 
     *                  Index starting from 1.
     * @return  A {@code String} that is the description of the stage name column 
     *          in download files (because we use it several times), formated in HTML 
     *          and HTML escaped if necessary.
     * @see #STAGE_NAME_COL_NAME
     */
    private String getStageNameColDescription(int colNumber) {
        log.entry();
        return log.exit("<p>Name of the developmental stage defined by <code>" 
        + STAGE_ID_COL_NAME + "</code> (column " + colNumber + ")</p>");
    }
    /**
     * @return  A {@code String} that is the description of the anatomical entity ID column 
     *          in download files (because we use it several times), formated in HTML 
     *          and HTML escaped if necessary.
     * @see #ANAT_ENTITY_ID_COL_NAME
     */
    private String getAnatEntityIdColDescription() {
        log.entry();
        return log.exit("<p>Unique identifier of the anatomical entity, from the Uberon ontology.</p>");
    }
    /**
     * @param colNumber An {@code int} that is the index of the column containing 
     *                  the anatomical entity ID (see {@link #ANAT_ENTITY_ID_COL_NAME}). 
     *                  Index starting from 1.
     * @return  A {@code String} that is the description of the anatomical entity name column 
     *          in download files (because we use it several times), formated in HTML 
     *          and HTML escaped if necessary.
     * @see #ANAT_ENTITY_NAME_COL_NAME
     */
    private String getAnatEntityNameColDescription(int colNumber) {
        log.entry();
        return log.exit("<p>Name of the anatomical entity defined by <code>" 
        + ANAT_ENTITY_ID_COL_NAME + "</code> (column " + colNumber + ")</p>");
    }
    /**
     * Generates description of the expression state column. 
     * 
     * @param geneIdColNumber           An {@code int} that is the index of the column 
     *                                  containing the gene ID (see {@link #GENE_ID_COL_NAME}). 
     *                                  Index starting from 1.
     * @param stageIdColNumber          An {@code int} that is the index of the column 
     *                                  containing the stage ID (see {@link #STAGE_ID_COL_NAME}). 
     *                                  Index starting from 1.
     * @param anatEntityIdColNumber     An {@code int} that is the index of the column 
     *                                  containing the anatomical entity ID (see 
     *                                  {@link #ANAT_ENTITY_ID_COL_NAME}). 
     *                                  Index starting from 1.
     * @return  A {@code String} that is the description of the expression state column 
     *          in download files (because we use it several times), formated in HTML 
     *          and HTML escaped if necessary.
     * @see #EXPR_STATE_COL_NAME
     */
    private String getExprStateColDescription(int geneIdColNumber, int stageIdColNumber, 
            int anatEntityIdColNumber) {
        log.entry(geneIdColNumber, stageIdColNumber, anatEntityIdColNumber);
        return log.exit("<p>Reported call for " + GENE_ID_COL_NAME + " (column " 
                + geneIdColNumber + "), in " + ANAT_ENTITY_ID_COL_NAME + " (column " 
                + anatEntityIdColNumber + "), at " + STAGE_ID_COL_NAME + " (column " 
                + stageIdColNumber + "). One of: </p>"
                + "<ul class='doc_content'>"
                + "<li><span class='list_element_title'>expression high quality</span>: "
                + "expression reported as high quality, from Bgee statistical tests and/or from "
                + "<i>in situ</i> data sources, with no contradicting call of absence "
                + "of expression (call generated either from multiple congruent data, "
                + "or from single data);</li>"
                + "<li><span class='list_element_title'>expression low quality</span>: "
                + "expression reported as low quality, either from Bgee statistical tests and/or "
                + "from <i>in situ</i> data sources, or because there exists a conflict of "
                + "presence/absence of expression for the same gene, anatomical structure "
                + "and developmental stage, from different samples of a same data type "
                + "(conflicts between different data types are treated differently, see below);</li>"
                + "<li><span class='list_element_title'>absent high quality</span>: "
                + "report of absence of expression, either from Bgee statistical tests and/or "
                + "from <i>in situ</i> data sources; in Bgee, calls of absence of expression "
                + "are always discarded if there exists a contradicting call of expression, "
                + "from the same data type and for the same gene, in the same anatomical structure "
                + "and developmental stage, or in a child structure or child developmental stage; "
                + "this is why they are always considered of high quality;</li>"
                + "<li><span class='list_element_title'>low ambiguity</span>: "
                + "there exists a call of expression generated from a data type, but "
                + "there exists a call of absence of expression generated from another data type "
                + "for the same gene in a parent anatomical structure; for instance, gene A "
                + "is reported to be expressed in the midbrain at young adult stage "
                + "from Affymetrix data, but is reported to be not expressed in the brain "
                + "at young adult stage from RNA-Seq data;</li>"
                + "<li><span class='list_element_title'>high ambiguity</span>: "
                + "there exists a call of expression generated from a data type, but "
                + "there exists a call of absence of expression generated from another data type "
                + "for the same gene, anatomical structure and developmental stage; for instance, "
                + "gene A is reported to be expressed in the midbrain at young adult stage "
                + "from Affymetrix data, but is reported to be not expressed in the midbrain "
                + "at young adult stage from RNA-Seq data.</li>"
                + "</ul>");
    }
    
    /**
     * Provide explanations about how to retrieve correct genes in Ensembl, when we use 
     * the genome of another species for a given species. For instance, for bonobo we use 
     * the chimpanzee genome, and replace the 'ENSPTRG' prefix of chimp genes by 
     * the prefix 'PPAG'.
     * 
     * @return  A {@code String} formatted in HTML, providing the explanation.
     */
    //TODO: this needs to be generated automatically from the species table in database.
    public String getGenomeMappingExplanation() {
        log.entry();
        return log.exit("Please note that "
        + "for <i>P. paniscus</i> (bonobo) we use <i>P. troglodytes</i> genome (chimpanzee), "
        + "and that for <i>P. pygmaeus</i> (Bornean orangutan) we use <i>P. abelii</i> genome "
        + "(Sumatran orangutan). Only for those species (bonobo and Bornean orangutan), "
        + "we modify the Ensembl gene IDs, to ensure that we provide unique gene identifiers "
        + "over all species. It is therefore necessary, to obtain correct Ensembl gene IDs "
        + "for those species, to replace gene ID prefix 'PPAG' with 'ENSPTRG', "
        + "and 'PPYG' prefix with 'ENSPPYG'.");
    }
    
    /**
     * @return  a {@code String} containing the HTML to create a table containing the header 
     *          of a single species simple expression file (can be used in "help" links).
     */
    public String getSingleSpeciesSimpleExprFileHeaderDesc() {
        log.entry();
        return log.exit("<table class='call_download_file'>"
                + "<tbody>"
                + "<tr>"
                + "<td>" + GENE_ID_COL_NAME + "</td>"
                + "<td>" + GENE_NAME_COL_NAME + "</td>"
                + "<td>"  + STAGE_ID_COL_NAME + "</td>"
                + "<td>" + STAGE_NAME_COL_NAME + "</td>"
                + "<td>"  + ANAT_ENTITY_ID_COL_NAME + "</td>"
                + "<td>" + ANAT_ENTITY_NAME_COL_NAME + "</td>"
                + "<td>" + EXPR_STATE_COL_NAME + "</td>"
                + "</tr>"
                + "</tbody>"
                + "</table>");
    }

    /**
     * @return  the {@code String} that is the link of the back to the top.
     */
    private String getBackToTheTopLink() {
        log.entry();
        return log.exit("<a class='backlink' href='#sectionname'>Back to the top</a>");
    }

    @Override
    protected void includeCss() {
        log.entry();
        super.includeCss();
        this.includeCss("documentation.css");
        log.exit();
    }
}
