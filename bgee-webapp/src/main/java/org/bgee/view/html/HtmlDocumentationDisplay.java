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
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 13 May 2015
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
     * A {@code String} that is the name of the expression state column for affymetrix data 
     * in download files, HTML escaped if necessary.
     * @see #AFFY_EXPR_STATE_LINK_TITLE
     */
    private final String AFFY_EXPR_STATE_COL_NAME = "Affymetrix data";
    /**
     * A {@code String} to be used in {@code title} attribute of {@code a} tag linking to 
     * affymetrix expression state column description (used several times), HTML escaped if necessary.
     * @see #AFFY_EXPR_STATE_COL_NAME
     */
    private final String AFFY_EXPR_STATE_LINK_TITLE = "See " + AFFY_EXPR_STATE_COL_NAME 
            + " column description";
    /**
     * A {@code String} that is the name of the expression state column for EST data 
     * in download files, HTML escaped if necessary.
     * @see #EST_EXPR_STATE_LINK_TITLE
     */
    private final String EST_EXPR_STATE_COL_NAME = "EST data";
    /**
     * A {@code String} to be used in {@code title} attribute of {@code a} tag linking to 
     * EST expression state column description (used several times), HTML escaped if necessary.
     * @see #EST_EXPR_STATE_COL_NAME
     */
    private final String EST_EXPR_STATE_LINK_TITLE = "See " + EST_EXPR_STATE_COL_NAME 
            + " column description";
    /**
     * A {@code String} that is the name of the expression state column for in situ data 
     * in download files, HTML escaped if necessary.
     * @see #IN_SITU_EXPR_STATE_LINK_TITLE
     */
    private final String IN_SITU_EXPR_STATE_COL_NAME = "In situ data";
    /**
     * A {@code String} to be used in {@code title} attribute of {@code a} tag linking to 
     * in situ expression state column description (used several times), HTML escaped if necessary.
     * @see #IN_SITU_EXPR_STATE_COL_NAME
     */
    private final String IN_SITU_EXPR_STATE_LINK_TITLE = "See " + IN_SITU_EXPR_STATE_COL_NAME 
            + " column description";
    /**
     * A {@code String} that is the name of the expression state column for RNA-Seq data  
     * in download files, HTML escaped if necessary.
     * @see #RNA_SEQ_EXPR_STATE_LINK_TITLE
     */
    private final String RNA_SEQ_EXPR_STATE_COL_NAME = "RNA-Seq data";
    /**
     * A {@code String} to be used in {@code title} attribute of {@code a} tag linking to 
     * RNA-Seq expression state column description (used several times), HTML escaped if necessary.
     * @see #RNA_SEQ_EXPR_STATE_COL_NAME
     */
    private final String RNA_SEQ_EXPR_STATE_LINK_TITLE = "See " + RNA_SEQ_EXPR_STATE_COL_NAME 
            + " column description";
    /**
     * A {@code String} that is the name of the column describing whether data were "observed",
     * in download files, HTML escaped if necessary.
     * @see #OBSERVED_DATA_LINK_TITLE
     */
    private final String OBSERVED_DATA_COL_NAME = "Including observed data";
    /**
     * A {@code String} to be used in {@code title} attribute of {@code a} tag linking to 
     * the observed data column description (used several times), HTML escaped if necessary.
     * @see #OBSERVED_DATA_EXPR_STATE_COL_NAME
     */
    private final String OBSERVED_DATA_LINK_TITLE = "See " + OBSERVED_DATA_COL_NAME 
            + " column description";
    /**
     * A {@code String} that is the name of the differential expression state column in download files, 
     * HTML escaped if necessary.
     * @see #DIFF_EXPR_STATE_LINK_TITLE
     */
    private final String DIFF_EXPR_STATE_COL_NAME = "Differential expression";
    /**
     * A {@code String} to be used in {@code title} attribute of {@code a} tag linking to 
     * differential expression state column description (used several times), HTML escaped if necessary.
     * @see #DIFF_EXPR_STATE_COL_NAME
     */
    private final String DIFF_EXPR_STATE_LINK_TITLE = "See " + DIFF_EXPR_STATE_COL_NAME 
            + " column description";
    /**
     * A {@code String} that is the name of the differential expression call quality column 
     * in download files, HTML escaped if necessary.
     * @see #DIFF_EXPR_QUAL_LINK_TITLE
     */
    private final String DIFF_EXPR_QUAL_COL_NAME = "Call quality";
    /**
     * A {@code String} to be used in {@code title} attribute of {@code a} tag linking to 
     * differential expression call quality column description (used several times), 
     * HTML escaped if necessary.
     * @see #DIFF_EXPR_QUAL_COL_NAME
     */
    private final String DIFF_EXPR_QUAL_LINK_TITLE = "See " + DIFF_EXPR_QUAL_COL_NAME 
            + " column description";
    /**
     * A {@code String} that is the name of the differential expression state column 
     * for Affymetrix data in download files, HTML escaped if necessary.
     */
    private final String AFFY_DIFF_EXPR_STATE_COL_NAME = "Affymetrix data";
    /**
     * A {@code String} that is the name of the differential expression quality column 
     * for Affymetrix data in download files, HTML escaped if necessary.
     */
    private final String AFFY_DIFF_EXPR_QUAL_COL_NAME = "Affymetrix call quality";
    /**
     * A {@code String} that is the name of the column storing the best p-value supporting 
     * an Affymetrix differential expression call in download files, HTML escaped if necessary.
     */
    private final String AFFY_DIFF_EXPR_P_VAL_COL_NAME = "Affymetrix best supporting p-value";
    /**
     * A {@code String} that is the name of the column storing the count of analyses supporting 
     * an Affymetrix differential expression call in download files, HTML escaped if necessary.
     */
    private final String AFFY_DIFF_EXPR_SUPPORT_COUNT_COL_NAME = 
            "Affymetrix analysis count supporting Affymetrix call";
    /**
     * A {@code String} that is the name of the column storing the count of analyses in conflict of  
     * an Affymetrix differential expression call in download files, HTML escaped if necessary.
     */
    private final String AFFY_DIFF_EXPR_CONFLICT_COUNT_COL_NAME = 
            "Affymetrix analysis count in conflict with Affymetrix call";
    /**
     * A {@code String} that is the name of the differential expression state column 
     * for RNA-Seq data in download files, HTML escaped if necessary.
     */
    private final String RNA_SEQ_DIFF_EXPR_STATE_COL_NAME = "RNA-Seq data";
    /**
     * A {@code String} that is the name of the differential expression quality column 
     * for RNA-Seq data in download files, HTML escaped if necessary.
     */
    private final String RNA_SEQ_DIFF_EXPR_QUAL_COL_NAME = "RNA-Seq call quality";
    /**
     * A {@code String} that is the name of the column storing the best p-value supporting 
     * an RNA-Seq differential expression call in download files, HTML escaped if necessary.
     */
    private final String RNA_SEQ_DIFF_EXPR_P_VAL_COL_NAME = "RNA-Seq best supporting p-value";
    /**
     * A {@code String} that is the name of the column storing the count of analyses supporting 
     * an RNA-Seq differential expression call in download files, HTML escaped if necessary.
     */
    private final String RNA_SEQ_DIFF_EXPR_SUPPORT_COUNT_COL_NAME = 
            "RNA-Seq analysis count supporting RNA-Seq call";
    /**
     * A {@code String} that is the name of the column storing the count of analyses in conflict of  
     * an RNA-Seq differential expression call in download files, HTML escaped if necessary.
     */
    private final String RNA_SEQ_DIFF_EXPR_CONFLICT_COUNT_COL_NAME = 
            "RNA-Seq analysis count in conflict with RNA-Seq call";

    /**
     * A {@code String} that is the name of the column storing homologous anatomical 
     * entity IDs in multi-species download files, HTML escaped if necessary.
     */
    private final String MULTI_ANAT_ENTITY_IDS_COL_NAME ="Anatomical entity IDs";
    /**
     * A {@code String} that is the name of the column storing homologous anatomical 
     * entity names in multi-species download files, HTML escaped if necessary.
     */
    private final String MULTI_ANAT_ENTITY_NAMES_COL_NAME ="Anatomical entity names";
    /**
     * A {@code String} that is the name of the column storing the OMA HOG ID 
     * in multi-species download files, HTML escaped if necessary.
     */
    private final String OMA_ID_COL_NAME = "OMA ID";
    /**
     * A {@code String} that is the prefix of the name of the columns storing the number 
     * of over-expressed genes in a condition for a gene homology group, for a given species 
     * (the suffix of the column name is the latin name of the species) in multi-species 
     * download file, HTML escaped if necessary.
     */
    private final String OVER_EXPRESSED_FOR_SPECIES_COL_NAME = "Over-expressed gene count for";
    /**
     * A {@code String} that is the prefix of the name of the columns storing the number 
     * of under-expressed genes in a condition for a gene homology group, for a given species 
     * (the suffix of the column name is the latin name of the species) in multi-species 
     * download file, HTML escaped if necessary.
     */
    private final String UNDER_EXPRESSED_FOR_SPECIES_COL_NAME = "Under-expressed gene count for";
    /**
     * A {@code String} that is the prefix of the name of the columns storing the number 
     * of genes with no diff. expression in a condition for a gene homology group, for a given species 
     * (the suffix of the column name is the latin name of the species) in multi-species 
     * download file, HTML escaped if necessary.
     */
    private final String NOT_DIFF_EXPRESSED_FOR_SPECIES_COL_NAME = "Not diff. expressed gene count for";
    /**
     * A {@code String} that is the prefix of the name of the columns storing the number 
     * of genes with no data or inconclusive results in a condition for a gene homology group, 
     * for a given species (the suffix of the column name is the latin name of the species) 
     * in multi-species download file, HTML escaped if necessary.
     */
    private final String NA_FOR_SPECIES_COL_NAME = "N/A gene count for";
    /**
     * A {@code String} that is the name of the column storing the latin name of species 
     * in multi-species complete download files, HTML escaped if necessary.
     */
    private final String SPECIES_LATIN_NAME_COL_NAME = "Latin species name";
    /**
     * A {@code String} that is the name of the column storing the CIO ID associated to  
     * anatomical homology mapping, in multi-species complete download files, 
     * HTML escaped if necessary.
     */
    private final String ANAT_HOMOLOGY_CIO_ID_COL_NAME = "Anatomy homology CIO ID";
    /**
     * A {@code String} that is the name of the column storing the CIO name associated to  
     * anatomical homology mapping, in multi-species complete download files, 
     * HTML escaped if necessary.
     */
    private final String ANAT_HOMOLOGY_CIO_NAME_COL_NAME = "Anatomy homology CIO name";

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
    //(just for the note: I did the exact opposite for anchors linking to column descriptions 
    //(#col1, and not #gene_id), this is on purpose :p (so that a change of column is immediately seen))
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

    //*******************************************************
    // DOCUMENTATION FOR CALL DOWNLOAD FILES 
    //*******************************************************
    
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
        this.writeDocMenuForCallDownloadFiles();
        
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
        //presence/absence
        this.writeSingleSpeciesExprCallFileDoc();
        //over/under
        this.writeSingleSpeciesDiffExprCallFileDoc();
        this.writeln("</div>"); // end of single-species
        
        this.writeln("</div>");
        
        //multi-species documentation
        this.writeln("<div>");
        this.writeln("<h2 id='multi'>Multi-species download files</h2>");
        this.writeln("<div class='doc_content'>");
        this.writeln("<p>Bgee provides the ability to compare expression data between species, "
                + "with great anatomical details, using formal concepts of homology: "
                + "homology of genes, homology of anatomical entities. This allows to perform "
                + "accurate comparisons between species, even for distant species "
                + "for which the anatomy mapping might not be obvious.</p>");
        this.writeln("<ul class='doc_content'>"
                + "<li><span class='list_element_title'>homology of anatomical entities</span>: "
                + "When comparing multiple species, only anatomical entities homologous "
                + "between all species compared are considered, meaning, only anatomical entities "
                + "derived from an organ existing before the divergence of the species compared. "
                + "This requires careful annotations of the homology history of animal anatomy. "
                + "These annotations are described in a separate project maintained "
                + "by the Bgee team, see <a target='_blank' "
                + "href='https://github.com/BgeeDB/anatomical-similarity-annotations/' "
                + "title='See anatomical-similarity-annotations project on GitHub'>"
                + "homology annotation project on GitHub</a>. <br />"
                + "In practice, when comparing expression data between several species, "
                + "the anatomical entities used are those with a homology relation valid "
                + "for their Least Common Ancestor (LCA), and any of its ancestral taxa. "
                + "For instance, if comparing data between human and zebrafish, "
                + "the LCA would be the taxon <i>Euteleostomi</i>; as a result, "
                + "annotations to this taxon would be used, such as the relation of homology "
                + "between \"tetrapod parietal bone\" (UBERON:0000210) and "
                + "\"actinopterygian frontal bone\" (UBERON:0004866); but also, annotations "
                + "to ancestral taxa, such as the annotation stating that \"ophthalmic nerve\" "
                + "appeared in the <i>Vertebrata</i> common ancestor; annotations to more recent taxa "
                + "than the LCA would be discarded, such as the annotation to the \"forelimb\" "
                + "structure (UBERON:0002102), homologous in the <i>Tetrapoda</i> lineage.</li> "
                + "<li><span class='list_element_title'>homology of genes</span>: relations of homology "
                + "between genes are retrieved using the <a target='_blank' "
                + "href='http://omabrowser.org/oma/hogs/' title='External link to OMA browser'>"
                + "OMA Hierarchical orthologous groups</a>; when comparing several species, "
                + "Bgee identifies their Least Common Ancestor (LCA), and retrieve genes "
                + "that have descended from a single common ancestral gene in that LCA.</li>"
                + "</ul>");
        this.writeln("<p>Jump to: </p>"
                + "<ul>"
//                + "<li><a href='#multi_expr' title='Quick jump to presence/absence of expression'>"
//                + "Presence/absence of expression</a></li>"
                + "<li><a href='#multi_diff' title='Quick jump to differential expression'>"
                + "Over-/under-expression across anatomy or life stages</a></li>"
                + "</ul>");
        //over/under
        this.writeMultiSpeciesDiffExprCallFileDoc();
        this.writeln("</div>");
        this.writeln("</div>"); // end of multi-species download file
        
        this.endDisplay();

        log.exit();
    }
    
    /**
     * Write the documentation menu related to presence/absence and over/under expression call 
     * download files. Anchors used in this method for quick jump links 
     * have to stayed in sync with id attributes of h2, h3 and h4 tags defined in 
     * {@link #writeSingleSpeciesExprCallFileDoc()}, 
     * {@link #writeSingleSpeciesSimpleExprCallFileDoc()} and 
     * {@link #writeSingleSpeciesCompleteExprCallFileDoc()}.
     * 
     * @see #writeSingleSpeciesExprCallFileDoc()
     * @see #writeSingleSpeciesSimpleExprCallFileDoc()
     * @see #writeSingleSpeciesCompleteExprCallFileDoc()
     */
    private void writeDocMenuForCallDownloadFiles() {
        log.entry();
        
        this.writeln("<div class='documentationmenu'><ul>");
        //Single-species
        this.writeln("<li><a href='#single' title='Quick jump to this section'>" + 
                "Single-species download files</a>");
        //presence/absence
        this.writeln("<ul>");           
        this.writeln("<li><a href='#single_expr' title='Quick jump to this section'>" + 
                "Presence/absence of expression</a>");
        //Actually there explanations common to simple and complete files, so we don't provide
        //direct links to simple/common files, that would skip the common explanations.
//        this.writeln("<ul>");
//        this.writeln("<li><a href='#single_expr_simple' title='Quick jump to this section'>" + 
//                "Simple file</a></li>");
//        this.writeln("<li><a href='#single_expr_complete' title='Quick jump to this section'>" + 
//                "Complete file</a></li>");
//        this.writeln("</ul>");   
        this.writeln("</li>");              //end of presence/absence
        //diff expression
        this.writeln("<li><a href='#single_diff' title='Quick jump to this section'>" + 
                "Over-/under-expression across anatomy or life stages</a>");
        //Actually there explanations common to simple and complete files, so we don't provide
        //direct links to simple/common files, that would skip the common explanations.
//        this.writeln("<ul>");
//        this.writeln("<li><a href='#single_diff_simple' title='Quick jump to this section'>" + 
//                "Simple file</a></li>");
//        this.writeln("<li><a href='#single_diff_complete' title='Quick jump to this section'>" + 
//                "Complete file</a></li>");
//        this.writeln("</ul>"); 
        this.writeln("</li>");              //end of diff expression 
        this.writeln("</ul></li>");     // end of single-species section
        
        //multi-species
        this.writeln("<li><a href='#multi' title='Quick jump to this section'>" + 
                "Multi-species download files</a>");
        //diff expression
        this.writeln("<ul>");    
        this.writeln("<li><a href='#multi_diff' title='Quick jump to this section'>" + 
                "Over-/under-expression across anatomy or life stages</a>");
        //Actually there explanations common to simple and complete files, so we don't provide
        //direct links to simple/common files, that would skip the common explanations.
//        this.writeln("<ul>");
//        this.writeln("<li><a href='#multi_diff_simple' title='Quick jump to this section'>" + 
//                "Simple file</a></li>");
//        this.writeln("<li><a href='#multi_diff_complete' title='Quick jump to this section'>" + 
//                "Complete file</a></li>");
//        this.writeln("</ul>");         
        this.writeln("</li>");              //end of diff expression        
        this.writeln("</ul></li>");     // end of multi-species section
        
        this.writeln("</ul></div>");// end of documentationmenu
        
        log.exit();
    }
    
    /**
     * Write the documentation related to single species presence/absence of expression 
     * simple and complete download files. Anchors used in this method for quick jump links 
     * have to stayed in sync with id attributes of h4 tags defined in 
     * {@link #writeSingleSpeciesSimpleExprCallFileDoc()} and 
     * {@link #writeSingleSpeciesCompleteExprCallFileDoc()}.
     * 
     * @see #writeSingleSpeciesSimpleExprCallFileDoc()
     * @see #writeSingleSpeciesCompleteExprCallFileDoc()
     */
    private void writeSingleSpeciesExprCallFileDoc() {
        log.entry();
        
        //presence/absence of expression
        this.writeln("<h3 id='single_expr'>Presence/absence of expression</h3>");
        //TODO: add link to data analyses documentation
        this.writeln(this.getExprCallExplanation());
        this.writeln("<p>After presence/absence calls are generated from the raw data, "
                + "they are propagated using anatomical and life stage ontologies: </p>"
                + "<ul class='doc_content'>"
                + "<li><span class='list_element_title'>calls of expression</span> "
                + "are propagated to parent anatomical entities "
                + "and parent developmental stages; for instance, if gene A is expressed "
                + "in midbrain at young adult stage, it will also be considered as expressed "
                + "in brain at adult stage;</li>"
                + "<li><span class='list_element_title'>calls of absence of expression</span> "
                + "are propagated to child anatomical entities "
                + "(and not to child developmental stages); for instance, if gene A is reported "
                + "as not expressed in the brain at young adult stage, it will also be considered "
                + "as not expressed in the midbrain at young adult stage. This is only permitted "
                + "when it does not generate any contradiction with expression calls from "
                + "the same data type (for instance, no contradiction permitted of reported "
                + "absence of expression by RNA-Seq, with report of expression by RNA-Seq "
                + "for the same gene, in the same anatomical entity and developmental stage, "
                + "or any child anatomical entity and child developmental stage).</li>"
                + "</ul>"
                + "<p>Call propagation allows a complete integration of the data, "
                + "even if provided at different anatomical or developmental levels. "
                + "For instance: if gene A is reported to be expressed in the midbrain dura mater "
                + "at yound adult stage; gene B is reported to be expressed "
                + "in the midbrain pia mater at late adult stage; and gene C has an absence "
                + "of expression reported in the brain at adult stage; it is then possible to retrieve "
                + "that, in the midbrain at adult stage, gene A and B are both expressed, "
                + "while gene C is not, thanks to call propagation.</p>");
        this.writeln("<p>Presence/absence calls are then filtered and presented differently "
                + "depending on whether a <code>simple file</code>, "
                + "or a <code>complete file</code> is used. Notably: <code>simple files</code> "
                + "aim at providing summarized information over all data types, and only "
                + "in anatomical entities and developmental stages actually used "
                + "in experimental data; <code>complete files</code> aim at reporting all information, "
                + "allowing for instance to retrieve the contribution of each data type to a call, "
                + "in all possible anatomical entities and developmental stages.</p>");
        this.writeln("<p>Jump to format description for: </p>"
                + "<ul>"
                + "<li><a href='#single_expr_simple' title='Quick jump to simple file description'>"
                + "simple file</a></li>"
                + "<li><a href='#single_expr_complete' title='Quick jump to complete file description'>"
                + "complete file</a></li>"
                + "</ul>");
        
        //simple expression file
        this.writeSingleSpeciesSimpleExprCallFileDoc();
        
        //complete expression file
        this.writeSingleSpeciesCompleteExprCallFileDoc(); //end of presence/absence of expression
        
        
        log.exit();
    }
    /**
     * Write the documentation related to single species simple presence/absence of expression 
     * download files. The id attribute used in h4 tag must stay in sync with anchors used 
     * in quick jump links defined in method {@link #writeSingleSpeciesExprCallFileDoc()}.
     * If the header of this file changes, {@link #getSingleSpeciesSimpleExprFileHeaderDesc()} 
     * must be updated.
     * 
     * @see #writeSingleSpeciesExprCallFileDoc()
     * @see #getSingleSpeciesSimpleExprFileHeaderDesc()
     */
    private void writeSingleSpeciesSimpleExprCallFileDoc() {
        log.entry();
        
        this.writeln("<h4 id='single_expr_simple'>Simple file</h4>");
        this.writeln("<p>In simple files, propagated presence/absence of expression calls "
                + "are provided, but only calls in conditions of anatomical entity/developmental stage "
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
                + "</a></td><td>1</td><td>FBgn0005427</td></tr>");
        this.writeln("<tr><td>2</td><td><a href='#single_expr_simple_col2' title='" 
                + GENE_NAME_LINK_TITLE + "'>" + GENE_NAME_COL_NAME 
                + "</a></td><td>1</td><td>ewg</td></tr>");
        this.writeln("<tr><td>3</td><td><a href='#single_expr_simple_col3' title='" 
                + STAGE_ID_LINK_TITLE + "'>" + STAGE_ID_COL_NAME 
                + "</a></td><td>1</td><td>FBdv:00005348</td></tr>");
        this.writeln("<tr><td>4</td><td><a href='#single_expr_simple_col4' title='" 
                + STAGE_NAME_LINK_TITLE + "'>" + STAGE_NAME_COL_NAME 
                + "</a></td><td>1</td><td>prepupal stage P4(ii) (Drosophila)</td></tr>");
        this.writeln("<tr><td>5</td><td><a href='#single_expr_simple_col5' title='" 
                + ANAT_ENTITY_ID_LINK_TITLE + "'>" + ANAT_ENTITY_ID_COL_NAME 
                + "</a></td><td>1</td><td>FBbt:00003404</td></tr>");
        this.writeln("<tr><td>6</td><td><a href='#single_expr_simple_col6' title='" 
                + ANAT_ENTITY_NAME_LINK_TITLE + "'>" + ANAT_ENTITY_NAME_COL_NAME 
                + "</a></td><td>1</td><td>mesothoracic extracoxal depressor muscle 66 (Drosophila)</td></tr>");
        this.writeln("<tr><td>7</td><td><a href='#single_expr_simple_col7' title='" 
                + EXPR_STATE_LINK_TITLE + "'>" + EXPR_STATE_COL_NAME 
                + "</a></td><td>1</td><td>expression high quality</td></tr>");
        this.writeln("</tbody>");
        this.writeln("</table>");
        this.writeln("<h5 id='single_expr_simple_col1'>" + GENE_ID_COL_NAME + " (column 1)</h5>");
        this.writeln(this.getGeneIdColDescription());
        this.writeln("<h5 id='single_expr_simple_col2'>" + GENE_NAME_COL_NAME + " (column 2)</h5>");
        this.writeln(this.getGeneNameColDescription(1));
        this.writeln("<h5 id='single_expr_simple_col3'>" + STAGE_ID_COL_NAME + " (column 3)</h5>");
        this.writeln(this.getStageIdColDescription());
        this.writeln("<h5 id='single_expr_simple_col4'>" + STAGE_NAME_COL_NAME + " (column 4)</h5>");
        this.writeln(this.getStageNameColDescription(3));
        this.writeln("<h5 id='single_expr_simple_col5'>" + ANAT_ENTITY_ID_COL_NAME + " (column 5)</h5>");
        this.writeln(this.getAnatEntityIdColDescription());
        this.writeln("<h5 id='single_expr_simple_col6'>" + ANAT_ENTITY_NAME_COL_NAME + " (column 6)</h5>");
        this.writeln(this.getAnatEntityNameColDescription(5));
        this.writeln("<h5 id='single_expr_simple_col7'>" + EXPR_STATE_COL_NAME + " (column 7)</h5>");
        this.writeln(this.getExprStateColDescription(1, 3, 5, true)); 
        this.writeln("<p><a href='#single_expr'>Back to presence/absence of expression menu</a></p>");
        
        log.exit();
    }
    
    /**
     * Write the documentation related to single species complete presence/absence of expression 
     * download files. The id attribute used in h4 tag must stay in sync with anchors used 
     * in quick jump links defined in method {@link #writeSingleSpeciesExprCallFileDoc()}.
     * If the header of this file changes, {@link #getSingleSpeciesCompleteExprFileHeaderDesc()} 
     * must be updated.
     * 
     * @see #writeSingleSpeciesExprCallFileDoc()
     * @see #getSingleSpeciesCompleteExprFileHeaderDesc()
     */
    private void writeSingleSpeciesCompleteExprCallFileDoc() {
        log.entry();
        
        this.writeln("<h4 id='single_expr_complete'>Complete file</h4>");
        this.writeln("<p>The differences between simple and complete files are that, "
                + "in complete files: </p>"
                + "<ul class='doc_content'>"
                + "<li>details of expression status generated from each data type are provided;</li>"
                + "<li>all calls are provided, propagated to all possible anatomical entities "
                + "and developmental stages, including in conditions not annotated in experimental data "
                + "(calls generated from propagation only);</li>"
                + "<li>a column allows to determine whether a call was generated from propagation "
                + "only, or whether the anatomical entity/developmental stage was actually "
                + "seen in experimental data (such a call would then also be present "
                + "in simple file).</li>"
                + "</ul>");
        this.writeln("<table class='call_download_file'>");
        this.writeln("<caption>Format description for single species complete expression file</caption>");
        this.writeln("<thead>");
        this.writeln("<tr><th>Column</th><th>Content</th><th>Cardinality</th><th>Example</th></tr>");
        this.writeln("</thead>");
        this.writeln("<tbody>");
        this.writeln("<tr><td>1</td><td><a href='#single_expr_complete_col1' title='" 
                + GENE_ID_LINK_TITLE + "'>" + GENE_ID_COL_NAME 
                + "</a></td><td>1</td><td>ENSDARG00000070769</td></tr>");
        this.writeln("<tr><td>2</td><td><a href='#single_expr_complete_col2' title='" 
                + GENE_NAME_LINK_TITLE + "'>" + GENE_NAME_COL_NAME 
                + "</a></td><td>1</td><td>foxg1a</td></tr>");
        this.writeln("<tr><td>3</td><td><a href='#single_expr_complete_col3' title='" 
                + STAGE_ID_LINK_TITLE + "'>" + STAGE_ID_COL_NAME 
                + "</a></td><td>1</td><td>UBERON:0000113</td></tr>");
        this.writeln("<tr><td>4</td><td><a href='#single_expr_complete_col4' title='" 
                + STAGE_NAME_LINK_TITLE + "'>" + STAGE_NAME_COL_NAME 
                + "</a></td><td>1</td><td>post-juvenile adult stage</td></tr>");
        this.writeln("<tr><td>5</td><td><a href='#single_expr_complete_col5' title='" 
                + ANAT_ENTITY_ID_LINK_TITLE + "'>" + ANAT_ENTITY_ID_COL_NAME 
                + "</a></td><td>1</td><td>UBERON:0000955</td></tr>");
        this.writeln("<tr><td>6</td><td><a href='#single_expr_complete_col6' title='" 
                + ANAT_ENTITY_NAME_LINK_TITLE + "'>" + ANAT_ENTITY_NAME_COL_NAME 
                + "</a></td><td>1</td><td>brain</td></tr>");
        this.writeln("<tr><td>7</td><td><a href='#single_expr_complete_col7' title='" 
                + AFFY_EXPR_STATE_LINK_TITLE + "'>" + AFFY_EXPR_STATE_COL_NAME 
                + "</a></td><td>1</td><td>expression high quality</td></tr>");
        this.writeln("<tr><td>8</td><td><a href='#single_expr_complete_col8' title='" 
                + EST_EXPR_STATE_LINK_TITLE + "'>" + EST_EXPR_STATE_COL_NAME 
                + "</a></td><td>1</td><td>expression low quality</td></tr>");
        this.writeln("<tr><td>9</td><td><a href='#single_expr_complete_col9' title='" 
                + IN_SITU_EXPR_STATE_LINK_TITLE + "'>" + IN_SITU_EXPR_STATE_COL_NAME 
                + "</a></td><td>1</td><td>expression high quality</td></tr>");
        this.writeln("<tr><td>10</td><td><a href='#single_expr_complete_col10' title='" 
                + RNA_SEQ_EXPR_STATE_LINK_TITLE + "'>" + RNA_SEQ_EXPR_STATE_COL_NAME 
                + "</a></td><td>1</td><td>no data</td></tr>");
        this.writeln("<tr><td>11</td><td><a href='#single_expr_complete_col11' title='" 
                + OBSERVED_DATA_LINK_TITLE + "'>" + OBSERVED_DATA_COL_NAME 
                + "</a></td><td>1</td><td>yes</td></tr>");
        this.writeln("<tr><td>12</td><td><a href='#single_expr_complete_col12' title='" 
                + EXPR_STATE_LINK_TITLE + "'>" + EXPR_STATE_COL_NAME 
                + "</a></td><td>1</td><td>expression high quality</td></tr>");
        this.writeln("</tbody>");
        this.writeln("</table>");
        this.writeln("<h5 id='single_expr_complete_col1'>" + GENE_ID_COL_NAME + " (column 1)</h5>");
        this.writeln(this.getGeneIdColDescription());
        this.writeln("<h5 id='single_expr_complete_col2'>" + GENE_NAME_COL_NAME + " (column 2)</h5>");
        this.writeln(this.getGeneNameColDescription(1));
        this.writeln("<h5 id='single_expr_complete_col3'>" + STAGE_ID_COL_NAME + " (column 3)</h5>");
        this.writeln(this.getStageIdColDescription());
        this.writeln("<h5 id='single_expr_complete_col4'>" + STAGE_NAME_COL_NAME + " (column 4)</h5>");
        this.writeln(this.getStageNameColDescription(3));
        this.writeln("<h5 id='single_expr_complete_col5'>" + ANAT_ENTITY_ID_COL_NAME + " (column 5)</h5>");
        this.writeln(this.getAnatEntityIdColDescription());
        this.writeln("<h5 id='single_expr_complete_col6'>" + ANAT_ENTITY_NAME_COL_NAME + " (column 6)</h5>");
        this.writeln(this.getAnatEntityNameColDescription(5));
        this.writeln("<h5 id='single_expr_complete_col7'>" + AFFY_EXPR_STATE_COL_NAME + " (column 7)</h5>");
        //TODO: add links to data analyses documentation
        this.writeln("<p>Call generated by Affymetrix data for " 
                + this.getColumnListForCall(1, 3, 5, true) + ". One of: </p>"
                + "<ul class='doc_content'>"
                + "<li><span class='list_element_title'>expression high quality</span>: "
                + "expression reported as high quality from Bgee statistical tests, "
                + "with no contradicting call of absence of expression for same gene, "
                + "in same anatomical entity and developmental stage, that would have been "
                + "generated by other Affymetrix probesets or chips "
                + "(meaning that the call was either generated from multiple congruent data, "
                + "or from a single probeset/chip);</li>"
                + "<li><span class='list_element_title'>expression low quality</span>: "
                + "expression reported as low quality, either from Bgee statistical tests, "
                + "or because there exists a conflict of presence/absence of expression "
                + "for the same gene, anatomical entity and developmental stage, generated from "
                + "other Affymetrix probesets/chips;</li>"
                + "<li><span class='list_element_title'>absent high quality</span>: "
                + "report of absence of expression from Bgee statistical tests, "
                + "with no contradicting call of presence of expression generated by other "
                + "Affymetrix probesets or chips for the same gene, in the same anatomical entity "
                + "and developmental stage, or in a child entity or child developmental stage;</li>"
                + "<li><span class='list_element_title'>no data</span>: no Affymetrix data "
                + "available for this gene/anatomical entity/developmental stage (data either "
                + "not available, or discarded by Bgee quality controls).</li>"
                + "</ul>");
        this.writeln("<h5 id='single_expr_complete_col8'>" + EST_EXPR_STATE_COL_NAME + " (column 8)</h5>");
        //TODO: add links to data analyses documentation
        this.writeln("<p>Call generated by EST data for " 
                + this.getColumnListForCall(1, 3, 5, true) + ". Note that EST data are not used "
                + "to produce calls of absence of expression. One of: </p>"
                + "<ul class='doc_content'>"
                + "<li><span class='list_element_title'>expression high quality</span>: "
                + "expression reported as high quality from Bgee statistical tests;</li>"
                + "<li><span class='list_element_title'>expression low quality</span>: "
                + "expression reported as low quality from Bgee statistical tests;</li>"
                + "<li><span class='list_element_title'>no data</span>: no EST data "
                + "available for this gene/anatomical entity/developmental stage (data either "
                + "not available, or discarded by Bgee quality controls).</li>"
                + "</ul>");
        this.writeln("<h5 id='single_expr_complete_col9'>" + IN_SITU_EXPR_STATE_COL_NAME + " (column 9)</h5>");
        //TODO: add links to data analyses documentation
        this.writeln("<p>Call generated by <i>in situ</i> data for " 
                + this.getColumnListForCall(1, 3, 5, true) + ". One of: </p>"
                + "<ul class='doc_content'>"
                + "<li><span class='list_element_title'>expression high quality</span>: "
                + "expression reported as high quality from <i>in situ</i> data sources, "
                + "with no contradicting call of absence of expression for same gene, "
                + "in same anatomical entity and developmental stage "
                + "(meaning that the call was either generated from multiple congruent "
                + "<i>in situ</i> hybridization evidence lines, or from a single hybridization);</li>"
                + "<li><span class='list_element_title'>expression low quality</span>: "
                + "expression reported as low quality, either from <i>in situ</i> data sources, "
                + "or because there exists a conflict of presence/absence of expression "
                + "for the same gene, anatomical entity and developmental stage, generated from "
                + "different <i>in situ</i> hybridization evidence lines;</li>"
                + "<li><span class='list_element_title'>absent high quality</span>: "
                + "report of absence of expression from <i>in situ</i> data sources, "
                + "with no contradicting call of presence of expression generated by other "
                + "<i>in situ</i> hybridization evidence lines "
                + "for the same gene, in the same anatomical entity "
                + "and developmental stage, or in a child entity or child developmental stage;</li>"
                + "<li><span class='list_element_title'>no data</span>: no <i>in situ</i> data "
                + "available for this gene/anatomical entity/developmental stage (data either "
                + "not available, or discarded by Bgee quality controls).</li>"
                + "</ul>");
        this.writeln("<h5 id='single_expr_complete_col10'>" + RNA_SEQ_EXPR_STATE_COL_NAME + " (column 10)</h5>");
        //TODO: add links to data analyses documentation
        this.writeln("<p>Call generated by RNA-Seq data for " 
                + this.getColumnListForCall(1, 3, 5, true) + ". One of: </p>"
                + "<ul class='doc_content'>"
                + "<li><span class='list_element_title'>expression high quality</span>: "
                + "expression reported as high quality from Bgee statistical tests, "
                + "with no contradicting call of absence of expression for same gene, "
                + "in same anatomical entity and developmental stage, that would have been "
                + "generated from other RNA-Seq libraries (meaning that the call was either "
                + "generated from several libraries providing congruent results, "
                + "or from a single library);</li>"
                + "<li><span class='list_element_title'>expression low quality</span>: "
                + "expression reported as low quality, either from Bgee statistical tests, "
                + "or because there exists a conflict of presence/absence of expression "
                + "for the same gene, anatomical entity and developmental stage, generated from "
                + "different RNA-Seq libraries;</li>"
                + "<li><span class='list_element_title'>absent high quality</span>: "
                + "report of absence of expression from Bgee statistical tests, "
                + "with no contradicting call of presence of expression generated by other RNA-Seq libraries "
                + "for the same gene, in the same anatomical entity "
                + "and developmental stage, or in a child entity or child developmental stage;</li>"
                + "<li><span class='list_element_title'>no data</span>: no RNA-Seq data "
                + "available for this gene/anatomical entity/developmental stage (data either "
                + "not available, or discarded by Bgee quality controls).</li>"
                + "</ul>");
        this.writeln("<h5 id='single_expr_complete_col11'>" + OBSERVED_DATA_COL_NAME + " (column 11)</h5>");
        this.writeln("<p>Values permitted: <code>yes</code> and <code>no</code>.</p>"
                + "<p>Defines whether a call was generated from propagation only, "
                + "or whether the anatomical entity/developmental stage was actually seen "
                + "in experimental data (in which case, the call will also be present "
                + "in the expression simple file).</p>");
        this.writeln("<h5 id='single_expr_complete_col12'>" + EXPR_STATE_COL_NAME + " (column 12)</h5>");
        this.writeln(this.getExprStateColDescription(1, 3, 5, true)); 
        this.writeln("<p><a href='#single_expr'>Back to presence/absence of expression menu</a></p>");
        
        log.exit();
    }
    
    /**
     * Write the documentation related to single species over/under expression 
     * simple and complete download files. Anchors used in this method for quick jump links 
     * have to stayed in sync with id attributes of h4 tags defined in 
     * {@link #writeSingleSpeciesSimpleDiffExprFileDoc()} and 
     * {@link #writeSingleSpeciesCompleteDiffExprFileDoc()}.
     * 
     * @see #writeSingleSpeciesSimpleDiffExprFileDoc()
     * @see #writeSingleSpeciesCompleteDiffExprFileDoc()
     */
    private void writeSingleSpeciesDiffExprCallFileDoc() {
        log.entry();
        
        //presence/absence of expression
        this.writeln("<h3 id='single_diff'>Over-/under-expression across anatomy or life stages</h3>");
        //TODO: add link to data analyses documentation
        this.writeln(this.getDiffExprCallExplanation());
        this.writeln("<p>Note that, as opposed to calls of presence/absence of expression, "
                + "no propagation of differential expression calls is performed "
                + "using anatomical and life stage ontologies.</p>");
        this.writeln("<p>Over-/under-expression calls are then filtered and presented differently "
                + "depending on whether a <code>simple file</code>, "
                + "or a <code>complete file</code> is used. Notably: <code>simple files</code> "
                + "aim at providing summarized information over all data types; "
                + "<code>complete files</code> aim at reporting all information, "
                + "allowing for instance to retrieve the contribution of each data type to a call, "
                + "or to retrieve all genes and conditions tested, including genes "
                + "having no differential expression in these conditions.</p>");
        this.writeln("<p>Jump to format description for: </p>"
                + "<ul>"
                + "<li><a href='#single_diff_simple' title='Quick jump to simple file description'>"
                + "simple file</a></li>"
                + "<li><a href='#single_diff_complete' title='Quick jump to complete file description'>"
                + "complete file</a></li>"
                + "</ul>");
        
        //simple diff expression file
        this.writeSingleSpeciesSimpleDiffExprCallFileDoc();
        
        //complete diff expression file
        this.writeSingleSpeciesCompleteDiffExprCallFileDoc(); //end of presence/absence of expression
        
        
        log.exit();
    }
    
    /**
     * Write the documentation related to single species simple over-/under-expression 
     * download files. The id attributes used in h4 tag must stay in sync with anchors used 
     * in quick jump links defined in method {@link #writeSingleSpeciesDiffExprFileDoc()}. 
     * If the header of this file changes, {@link #getSingleSpeciesSimpleDiffExprFileHeaderDesc()} 
     * must be updated.
     * 
     * @see #writeSingleSpeciesDiffExprFileDoc()
     * @see #getSingleSpeciesSimpleDiffExprFileHeaderDesc()
     */
    private void writeSingleSpeciesSimpleDiffExprCallFileDoc() {
        log.entry();
        
        this.writeln("<h4 id='single_diff_simple'>Simple file</h4>");
        this.writeln("<p>In simple files, only calls of over-expression and under-expression "
                + "are provided, summarizing the contribution "
                + "of each data type to the call.</p>");
        this.writeln("<table class='call_download_file'>");
        this.writeln("<caption>Format description for single species simple differential expression file</caption>");
        this.writeln("<thead>");
        this.writeln("<tr><th>Column</th><th>Content</th><th>Cardinality</th><th>Example</th></tr>");
        this.writeln("</thead>");
        this.writeln("<tbody>");
        this.writeln("<tr><td>1</td><td><a href='#single_diff_simple_col1' title='" 
                + GENE_ID_LINK_TITLE + "'>" + GENE_ID_COL_NAME 
                + "</a></td><td>1</td><td>ENSG00000000419</td></tr>");
        this.writeln("<tr><td>2</td><td><a href='#single_diff_simple_col2' title='" 
                + GENE_NAME_LINK_TITLE + "'>" + GENE_NAME_COL_NAME 
                + "</a></td><td>1</td><td>DPM1</td></tr>");
        this.writeln("<tr><td>3</td><td><a href='#single_diff_simple_col3' title='" 
                + STAGE_ID_LINK_TITLE + "'>" + STAGE_ID_COL_NAME 
                + "</a></td><td>1</td><td>HsapDv:0000083</td></tr>");
        this.writeln("<tr><td>4</td><td><a href='#single_diff_simple_col4' title='" 
                + STAGE_NAME_LINK_TITLE + "'>" + STAGE_NAME_COL_NAME 
                + "</a></td><td>1</td><td>infant stage (human)</td></tr>");
        this.writeln("<tr><td>5</td><td><a href='#single_diff_simple_col5' title='" 
                + ANAT_ENTITY_ID_LINK_TITLE + "'>" + ANAT_ENTITY_ID_COL_NAME 
                + "</a></td><td>1</td><td>UBERON:0009834</td></tr>");
        this.writeln("<tr><td>6</td><td><a href='#single_diff_simple_col6' title='" 
                + ANAT_ENTITY_NAME_LINK_TITLE + "'>" + ANAT_ENTITY_NAME_COL_NAME 
                + "</a></td><td>1</td><td>dorsolateral prefrontal cortex</td></tr>");
        this.writeln("<tr><td>7</td><td><a href='#single_diff_simple_col7' title='" 
                + DIFF_EXPR_STATE_LINK_TITLE + "'>" + DIFF_EXPR_STATE_COL_NAME 
                + "</a></td><td>1</td><td>under-expression</td></tr>");
        this.writeln("<tr><td>8</td><td><a href='#single_diff_simple_col8' title='" 
                + DIFF_EXPR_QUAL_LINK_TITLE + "'>" + DIFF_EXPR_QUAL_COL_NAME 
                + "</a></td><td>1</td><td>high quality</td></tr>");
        this.writeln("</tbody>");
        this.writeln("</table>");
        this.writeln("<h5 id='single_diff_simple_col1'>" + GENE_ID_COL_NAME + " (column 1)</h5>");
        this.writeln(this.getGeneIdColDescription());
        this.writeln("<h5 id='single_diff_simple_col2'>" + GENE_NAME_COL_NAME + " (column 2)</h5>");
        this.writeln(this.getGeneNameColDescription(1));
        this.writeln("<h5 id='single_diff_simple_col3'>" + STAGE_ID_COL_NAME + " (column 3)</h5>");
        this.writeln(this.getStageIdColDescription());
        this.writeln("<h5 id='single_diff_simple_col4'>" + STAGE_NAME_COL_NAME + " (column 4)</h5>");
        this.writeln(this.getStageNameColDescription(3));
        this.writeln("<h5 id='single_diff_simple_col5'>" + ANAT_ENTITY_ID_COL_NAME + " (column 5)</h5>");
        this.writeln(this.getAnatEntityIdColDescription());
        this.writeln("<h5 id='single_diff_simple_col6'>" + ANAT_ENTITY_NAME_COL_NAME + " (column 6)</h5>");
        this.writeln(this.getAnatEntityNameColDescription(5));
        this.writeln("<h5 id='single_diff_simple_col7'>" + DIFF_EXPR_STATE_COL_NAME + " (column 7)</h5>");
        this.writeln(this.getDiffExprStateColDescription(1, 3, 5, true, false, true, false, "all data types")); 
        this.writeln("<h5 id='single_diff_simple_col8'>" + DIFF_EXPR_QUAL_COL_NAME + " (column 8)</h5>");
        this.writeln(this.getDiffExprQualColDescription(DIFF_EXPR_STATE_COL_NAME, 7, true, false)); 
        this.writeln("<p><a href='#single_diff'>Back to over-/under-expression menu</a></p>");
        
        log.exit();
    }

    /**
     * Write the documentation related to single species complete over-/under-expression 
     * download files. The id attribute used in h4 tag must stay in sync with anchors used 
     * in quick jump links defined in method {@link #writeSingleSpeciesDiffExprFileDoc()}.
     * If the header of this file changes, {@link #getSingleSpeciesCompleteDiffExprFileHeaderDesc()} 
     * must be updated.
     * 
     * @see #writeSingleSpeciesDiffExprFileDoc()
     * @see #getSingleSpeciesCompleteDiffExprFileHeaderDesc()
     */
    private void writeSingleSpeciesCompleteDiffExprCallFileDoc() {
        log.entry();
        
        this.writeln("<h4 id='single_diff_complete'>Complete file</h4>");
        this.writeln("<p>The differences between simple and complete files are that, "
                + "in complete files: </p>"
                + "<ul class='doc_content'>"
                + "<li>details of the contribution of each data type to the final calls "
                + "are provided, notably with information about best p-values, or number "
                + "of supporting/conflicting analyses;</li>"
                + "<li>calls representing absence of differential expression are provided, "
                + "allowing to determine all genes and conditions tested for differential "
                + "expression.</li>"
                + "</ul>");
        this.writeln("<table class='call_download_file'>");
        this.writeln("<caption>Format description for single species complete differential expression file</caption>");
        this.writeln("<thead>");
        this.writeln("<tr><th>Column</th><th>Content</th><th>Cardinality</th><th>Example</th></tr>");
        this.writeln("</thead>");
        this.writeln("<tbody>");
        this.writeln("<tr><td>1</td><td><a href='#single_diff_complete_col1' title='" 
                + GENE_ID_LINK_TITLE + "'>" + GENE_ID_COL_NAME 
                + "</a></td><td>1</td><td>ENSMUSG00000093930</td></tr>");
        this.writeln("<tr><td>2</td><td><a href='#single_diff_complete_col2' title='" 
                + GENE_NAME_LINK_TITLE + "'>" + GENE_NAME_COL_NAME 
                + "</a></td><td>1</td><td>Hmgcs1</td></tr>");
        this.writeln("<tr><td>3</td><td><a href='#single_diff_complete_col3' title='" 
                + STAGE_ID_LINK_TITLE + "'>" + STAGE_ID_COL_NAME 
                + "</a></td><td>1</td><td>UBERON:0000113</td></tr>");
        this.writeln("<tr><td>4</td><td><a href='#single_diff_complete_col4' title='" 
                + STAGE_NAME_LINK_TITLE + "'>" + STAGE_NAME_COL_NAME 
                + "</a></td><td>1</td><td>post-juvenile adult stage</td></tr>");
        this.writeln("<tr><td>5</td><td><a href='#single_diff_complete_col5' title='" 
                + ANAT_ENTITY_ID_LINK_TITLE + "'>" + ANAT_ENTITY_ID_COL_NAME 
                + "</a></td><td>1</td><td>UBERON:0002107</td></tr>");
        this.writeln("<tr><td>6</td><td><a href='#single_diff_complete_col6' title='" 
                + ANAT_ENTITY_NAME_LINK_TITLE + "'>" + ANAT_ENTITY_NAME_COL_NAME 
                + "</a></td><td>1</td><td>liver</td></tr>");
        this.writeln("<tr><td>7</td><td><a href='#single_diff_complete_col7' title='" 
                + DIFF_EXPR_STATE_LINK_TITLE + "'>" + DIFF_EXPR_STATE_COL_NAME 
                + "</a></td><td>1</td><td>over-expression</td></tr>");
        this.writeln("<tr><td>8</td><td><a href='#single_diff_complete_col8' title='" 
                + DIFF_EXPR_QUAL_LINK_TITLE + "'>" + DIFF_EXPR_QUAL_COL_NAME 
                + "</a></td><td>1</td><td>high quality</td></tr>");
        this.writeln("<tr><td>9</td><td><a href='#single_diff_complete_col9' title='" 
                + "See " + AFFY_DIFF_EXPR_STATE_COL_NAME + " column description'>" 
                + AFFY_DIFF_EXPR_STATE_COL_NAME 
                + "</a></td><td>1</td><td>over-expression</td></tr>");
        this.writeln("<tr><td>10</td><td><a href='#single_diff_complete_col10' title='" 
                + "See " + AFFY_DIFF_EXPR_QUAL_COL_NAME + " column description'>" 
                + AFFY_DIFF_EXPR_QUAL_COL_NAME
        //TODO: change 'poor' to 'low'
                + "</a></td><td>1</td><td>poor quality</td></tr>");
        this.writeln("<tr><td>11</td><td><a href='#single_diff_complete_col11' title='" 
                + "See " + AFFY_DIFF_EXPR_P_VAL_COL_NAME + " column description'>" 
                + AFFY_DIFF_EXPR_P_VAL_COL_NAME 
                + "</a></td><td>1</td><td>0.0035659347</td></tr>");
        this.writeln("<tr><td>12</td><td><a href='#single_diff_complete_col12' title='" 
                + "See " + AFFY_DIFF_EXPR_SUPPORT_COUNT_COL_NAME + " column description'>" 
                + AFFY_DIFF_EXPR_SUPPORT_COUNT_COL_NAME 
                + "</a></td><td>1</td><td>1</td></tr>");
        this.writeln("<tr><td>13</td><td><a href='#single_diff_complete_col13' title='" 
                + "See " + AFFY_DIFF_EXPR_CONFLICT_COUNT_COL_NAME + " column description'>" 
                + AFFY_DIFF_EXPR_CONFLICT_COUNT_COL_NAME 
                + "</a></td><td>1</td><td>1</td></tr>");
        this.writeln("<tr><td>14</td><td><a href='#single_diff_complete_col14' title='" 
                + "See " + RNA_SEQ_DIFF_EXPR_STATE_COL_NAME + " column description'>" 
                + RNA_SEQ_DIFF_EXPR_STATE_COL_NAME 
                + "</a></td><td>1</td><td>over-expression</td></tr>");
        this.writeln("<tr><td>15</td><td><a href='#single_diff_complete_col15' title='" 
                + "See " + RNA_SEQ_DIFF_EXPR_QUAL_COL_NAME + " column description'>" 
                + RNA_SEQ_DIFF_EXPR_QUAL_COL_NAME 
                + "</a></td><td>1</td><td>high quality</td></tr>");
        this.writeln("<tr><td>16</td><td><a href='#single_diff_complete_col16' title='" 
                + "See " + RNA_SEQ_DIFF_EXPR_P_VAL_COL_NAME + " column description'>" 
                + RNA_SEQ_DIFF_EXPR_P_VAL_COL_NAME 
                + "</a></td><td>1</td><td>2.96E-8</td></tr>");
        this.writeln("<tr><td>17</td><td><a href='#single_diff_complete_col17' title='" 
                + "See " + RNA_SEQ_DIFF_EXPR_SUPPORT_COUNT_COL_NAME + " column description'>" 
                + RNA_SEQ_DIFF_EXPR_SUPPORT_COUNT_COL_NAME 
                + "</a></td><td>1</td><td>2</td></tr>");
        this.writeln("<tr><td>18</td><td><a href='#single_diff_complete_col18' title='" 
                + "See " + RNA_SEQ_DIFF_EXPR_CONFLICT_COUNT_COL_NAME + " column description'>" 
                + RNA_SEQ_DIFF_EXPR_CONFLICT_COUNT_COL_NAME 
                + "</a></td><td>1</td><td>0</td></tr>");
        this.writeln("</tbody>");
        this.writeln("</table>");
        this.writeln("<h5 id='single_diff_complete_col1'>" + GENE_ID_COL_NAME + " (column 1)</h5>");
        this.writeln(this.getGeneIdColDescription());
        this.writeln("<h5 id='single_diff_complete_col2'>" + GENE_NAME_COL_NAME + " (column 2)</h5>");
        this.writeln(this.getGeneNameColDescription(1));
        this.writeln("<h5 id='single_diff_complete_col3'>" + STAGE_ID_COL_NAME + " (column 3)</h5>");
        this.writeln(this.getStageIdColDescription());
        this.writeln("<h5 id='single_diff_complete_col4'>" + STAGE_NAME_COL_NAME + " (column 4)</h5>");
        this.writeln(this.getStageNameColDescription(3));
        this.writeln("<h5 id='single_diff_complete_col5'>" + ANAT_ENTITY_ID_COL_NAME + " (column 5)</h5>");
        this.writeln(this.getAnatEntityIdColDescription());
        this.writeln("<h5 id='single_diff_complete_col6'>" + ANAT_ENTITY_NAME_COL_NAME + " (column 6)</h5>");
        this.writeln(this.getAnatEntityNameColDescription(5));
        this.writeln("<h5 id='single_diff_complete_col7'>" + DIFF_EXPR_STATE_COL_NAME + " (column 7)</h5>");
        this.writeln(this.getDiffExprStateColDescription(1, 3, 5, true, true, true, false, "all data types")); 
        this.writeln("<h5 id='single_diff_complete_col8'>" + DIFF_EXPR_QUAL_COL_NAME + " (column 8)</h5>");
        this.writeln(this.getDiffExprQualColDescription(DIFF_EXPR_STATE_COL_NAME, 7, true, false)); 
        this.writeln("<h5 id='single_diff_complete_col9'>" + AFFY_DIFF_EXPR_STATE_COL_NAME + " (column 9)</h5>");
        this.writeln(this.getDiffExprStateColDescription(1, 3, 5, true, true, false, true, "Affymetrix data")); 
        this.writeln("<h5 id='single_diff_complete_col10'>" + AFFY_DIFF_EXPR_QUAL_COL_NAME + " (column 10)</h5>");
        this.writeln(this.getDiffExprQualColDescription(AFFY_DIFF_EXPR_STATE_COL_NAME, 9, false, true)); 
        this.writeln("<h5 id='single_diff_complete_col11'>" + AFFY_DIFF_EXPR_P_VAL_COL_NAME 
                + " (column 11)</h5>");
        this.writeln(this.getDiffExprPvalColDescription(
                "Affymetrix", AFFY_DIFF_EXPR_STATE_COL_NAME, 9));
        this.writeln("<h5 id='single_diff_complete_col12'>" + AFFY_DIFF_EXPR_SUPPORT_COUNT_COL_NAME 
                + " (column 12)</h5>");
        this.writeln(this.getDiffSupportCountColDescription(
                "Affymetrix", AFFY_DIFF_EXPR_STATE_COL_NAME, 9));
        this.writeln("<h5 id='single_diff_complete_col13'>" + AFFY_DIFF_EXPR_CONFLICT_COUNT_COL_NAME 
                + " (column 13)</h5>");
        this.writeln(this.getDiffConflictCountColDescription(
                "Affymetrix", AFFY_DIFF_EXPR_STATE_COL_NAME, 9));
        this.writeln("<h5 id='single_diff_complete_col14'>" + RNA_SEQ_DIFF_EXPR_STATE_COL_NAME 
                + " (column 14)</h5>");
        this.writeln(this.getDiffExprStateColDescription(1, 3, 5, true, true, false, true, "RNA-Seq data")); 
        this.writeln("<h5 id='single_diff_complete_col15'>" + RNA_SEQ_DIFF_EXPR_QUAL_COL_NAME 
                + " (column 15)</h5>");
        this.writeln(this.getDiffExprQualColDescription(RNA_SEQ_DIFF_EXPR_STATE_COL_NAME, 14, false, true)); 
        this.writeln("<h5 id='single_diff_complete_col16'>" + RNA_SEQ_DIFF_EXPR_P_VAL_COL_NAME 
                + " (column 16)</h5>");
        this.writeln(this.getDiffExprPvalColDescription(
                "RNA-Seq", RNA_SEQ_DIFF_EXPR_STATE_COL_NAME, 14));
        this.writeln("<h5 id='single_diff_complete_col17'>" + RNA_SEQ_DIFF_EXPR_SUPPORT_COUNT_COL_NAME 
                + " (column 17)</h5>");
        this.writeln(this.getDiffSupportCountColDescription(
                "RNA-Seq", RNA_SEQ_DIFF_EXPR_STATE_COL_NAME, 14));
        this.writeln("<h5 id='single_diff_complete_col18'>" + RNA_SEQ_DIFF_EXPR_CONFLICT_COUNT_COL_NAME 
                + " (column 18)</h5>");
        this.writeln(this.getDiffConflictCountColDescription(
                "RNA-Seq", RNA_SEQ_DIFF_EXPR_STATE_COL_NAME, 14));
        
        this.writeln("<p><a href='#single_diff'>Back to over-/under-expression menu</a></p>");
        
        log.exit();
    }
    
    /**
     * Write the documentation related to multiple-species over/under expression 
     * simple and complete download files. Anchors used in this method for quick jump links 
     * have to stayed in sync with id attributes of h4 tags defined in 
     * {@link #writeMultiSpeciesSimpleDiffExprFileDoc()} and 
     * {@link #writeMultiSpeciesCompleteDiffExprFileDoc()}.
     * 
     * @see #writeMultiSpeciesSimpleDiffExprFileDoc()
     * @see #writeMultiSpeciesCompleteDiffExprFileDoc()
     */
    private void writeMultiSpeciesDiffExprCallFileDoc() {
        log.entry();
        
        //presence/absence of expression
        this.writeln("<h3 id='multi_diff'>Over-/under-expression across anatomy or life stages in multiple species</h3>");
        //TODO: add link to data analyses documentation
        this.writeln(this.getDiffExprCallExplanation());
        this.writeln("<p>In multi-species files, results are made comparable "
                + "between homologous genes, in homologous anatomical entities and comparable "
                + "developmental stages: only genes sharing a common ancestral gene "
                + "in the least common ancestor of the species compared are studied, "
                + "and only in anatomical entities sharing a homology relation between "
                + "all species compared, with data mapped to broad developmental stages "
                + "shared across animal kingdom (see <a href='#multi' "
                + "title='Quick jump to multi-species file description'>"
                + "use of homology in multi-species files</a>).</p>");
        this.writeln("<p>Note that, as opposed to calls of presence/absence of expression, "
                + "no propagation of differential expression calls is performed "
                + "using anatomical and life stage ontologies.</p>");
        this.writeln("<p>Over-/under-expression calls are then filtered and presented differently "
                + "depending on whether a <code>simple file</code>, "
                + "or a <code>complete file</code> is used. Notably: <code>simple files</code> "
                + "aim at providing one line per homology gene group and homologous "
                + "anatomical entities/developmental stage, and only for anatomical entities "
                + "with a homology relation defined with good level of confidence; "
                + "<code>complete files</code> aim at reporting all information, for each gene "
                + "of the homology groups, using all available homology relations between "
                + "anatomical entities, and allowing for instance to retrieve the contribution "
                + "of each data type to a call, or to retrieve all genes and conditions tested, "
                + "including genes having no differential expression in these conditions.</p>");
        this.writeln("<p>Jump to format description for: </p>"
                + "<ul>"
                + "<li><a href='#multi_diff_simple' title='Quick jump to simple file description'>"
                + "simple file</a></li>"
                + "<li><a href='#multi_diff_complete' title='Quick jump to complete file description'>"
                + "complete file</a></li>"
                + "</ul>");
        
        //simple diff expression file
        this.writeMultiSpeciesSimpleDiffExprCallFileDoc();
        
        //complete diff expression file
        this.writeMultiSpeciesCompleteDiffExprCallFileDoc(); 
        
        
        log.exit();
    }
    /**
     * Write the documentation related to multi-species simple over-/under-expression 
     * download files. The id attributes used in h4 tag must stay in sync with anchors used 
     * in quick jump links defined in method {@link #writeMultiSpeciesDiffExprFileDoc()}. 
     * If the header of this file changes, {@link #getMultiSpeciesSimpleDiffExprFileHeaderDesc()} 
     * must be updated.
     * 
     * @see #writeMultiSpeciesDiffExprCallFileDoc()
     * @see #getMultiSpeciesSimpleDiffExprCallFileHeaderDesc()
     */
    private void writeMultiSpeciesSimpleDiffExprCallFileDoc() {
        log.entry();
        
        this.writeln("<h4 id='multi_diff_simple'>Simple file</h4>");
        this.writeln("<p>In simple files, each line provides information for a gene homology group, "
                + "in a condition (homologous anatomical entity/comparable developmental stage); "
                + "columns then provide, for each species, the number of genes over-expressed, "
                + "under-expressed, not differentially expressed, and with no data, or "
                + "inconclusive results; this means that the number of columns is variable "
                + "depending on the number of species compared.</p>");
        this.writeln("<p>In simple files, only lines with data in at least two species, and at least "
                + "one over-expression or under-expression call in a species, are provided, "
                + "and only for anatomical entities with a homology relation defined "
                + "with a good level of confidence.</p>");
        this.writeln("<p>Each gene homology group is separated with a \"header\" line, "
                + "starting with <code>//</code>, providing the IDs and names of the genes "
                //TODO: change when format has been updated
                + "member of the group. Please note that this format is likely to change "
                + "in the next release of Bgee.</p>");
        this.writeln("<table class='call_download_file'>");
        this.writeln("<caption>Format description for multi-species simple differential expression file</caption>");
        this.writeln("<thead>");
        this.writeln("<tr><th>Column</th><th>Content</th><th>Cardinality</th><th>Example</th></tr>");
        this.writeln("</thead>");
        this.writeln("<tbody>");
        this.writeln("<tr><td>1</td><td><a href='#multi_diff_simple_col1' title='" 
                + "See " + OMA_ID_COL_NAME + " column description'>" 
                + OMA_ID_COL_NAME 
                + "</a></td><td>1</td><td>80</td></tr>");
        //TODO: change order of columns anat entity/stage once we re-generate the files.
        this.writeln("<tr><td>2</td><td><a href='#multi_diff_simple_col2' title='" 
                + "See " + MULTI_ANAT_ENTITY_IDS_COL_NAME + " column description'>" 
                + MULTI_ANAT_ENTITY_IDS_COL_NAME 
                + "</a></td><td>1 or greater</td><td>UBERON:0001898</td></tr>");
        this.writeln("<tr><td>3</td><td><a href='#multi_diff_simple_col3' title='" 
                + "See " + MULTI_ANAT_ENTITY_NAMES_COL_NAME + " column description'>" 
                + MULTI_ANAT_ENTITY_NAMES_COL_NAME 
                + "</a></td><td>1 or greater</td><td>hypothalamus</td></tr>");
        this.writeln("<tr><td>4</td><td><a href='#multi_diff_simple_col4' title='" 
                + STAGE_ID_LINK_TITLE + "'>" + STAGE_ID_COL_NAME 
                + "</a></td><td>1</td><td>UBERON:0000113</td></tr>");
        this.writeln("<tr><td>5</td><td><a href='#multi_diff_simple_col5' title='" 
                + STAGE_NAME_LINK_TITLE + "'>" + STAGE_NAME_COL_NAME 
                + "</a></td><td>1</td><td>post-juvenile adult stage</td></tr>");
        this.writeln("<tr><td>6</td><td><a href='#multi_diff_simple_col6' title='" 
                + "See " + OVER_EXPRESSED_FOR_SPECIES_COL_NAME + " species1 column description'>" 
                + OVER_EXPRESSED_FOR_SPECIES_COL_NAME + " species1</a> (e.g., <code>"
                + OVER_EXPRESSED_FOR_SPECIES_COL_NAME + " Mus musculus</code>)"
                + "</td><td>1</td><td>1</td></tr>");
        this.writeln("<tr><td>7</td><td><a href='#multi_diff_simple_col7' title='" 
                + "See " + UNDER_EXPRESSED_FOR_SPECIES_COL_NAME + " species1 column description'>" 
                + UNDER_EXPRESSED_FOR_SPECIES_COL_NAME + " species1</a> (e.g., <code>"
                + UNDER_EXPRESSED_FOR_SPECIES_COL_NAME + " Mus musculus</code>)"
                + "</td><td>1</td><td>0</td></tr>");
        this.writeln("<tr><td>8</td><td><a href='#multi_diff_simple_col8' title='" 
                + "See " + NOT_DIFF_EXPRESSED_FOR_SPECIES_COL_NAME + " species1 column description'>" 
                + NOT_DIFF_EXPRESSED_FOR_SPECIES_COL_NAME + " species1</a> (e.g., <code>"
                + NOT_DIFF_EXPRESSED_FOR_SPECIES_COL_NAME + " Mus musculus</code>)"
                + "</td><td>1</td><td>0</td></tr>");
        this.writeln("<tr><td>9</td><td><a href='#multi_diff_simple_col9' title='" 
                + "See " + NA_FOR_SPECIES_COL_NAME + " species1 column description'>" 
                + NA_FOR_SPECIES_COL_NAME + " species1</a> (e.g., <code>"
                + NA_FOR_SPECIES_COL_NAME + " Mus musculus</code>)"
                + "</td><td>1</td><td>0</td></tr>");
        this.writeln("<tr><td>10</td><td><a href='#multi_diff_simple_col6' title='" 
                + "See " + OVER_EXPRESSED_FOR_SPECIES_COL_NAME + " species2 column description'>" 
                + OVER_EXPRESSED_FOR_SPECIES_COL_NAME + " species2</a> (e.g., <code>"
                + OVER_EXPRESSED_FOR_SPECIES_COL_NAME + " Homo sapiens</code>)"
                + "</td><td>1</td><td>1</td></tr>");
        this.writeln("<tr><td>11</td><td><a href='#multi_diff_simple_col7' title='" 
                + "See " + UNDER_EXPRESSED_FOR_SPECIES_COL_NAME + " species2 column description'>" 
                + UNDER_EXPRESSED_FOR_SPECIES_COL_NAME + " species2</a> (e.g., <code>"
                + UNDER_EXPRESSED_FOR_SPECIES_COL_NAME + " Homo sapiens</code>)"
                + "</td><td>1</td><td>0</td></tr>");
        this.writeln("<tr><td>12</td><td><a href='#multi_diff_simple_col8' title='" 
                + "See " + NOT_DIFF_EXPRESSED_FOR_SPECIES_COL_NAME + " species2 column description'>" 
                + NOT_DIFF_EXPRESSED_FOR_SPECIES_COL_NAME + " species2</a> (e.g., <code>"
                + NOT_DIFF_EXPRESSED_FOR_SPECIES_COL_NAME + " Homo sapiens</code>)"
                + "</td><td>1</td><td>0</td></tr>");
        this.writeln("<tr><td>13</td><td><a href='#multi_diff_simple_col9' title='" 
                + "See " + NA_FOR_SPECIES_COL_NAME + " species2 column description'>" 
                + NA_FOR_SPECIES_COL_NAME + " species2</a> (e.g., <code>"
                + NA_FOR_SPECIES_COL_NAME + " Homo sapiens</code>)"
                + "</td><td>1</td><td>0</td></tr>");
        this.writeln("<tr><td>...</td><td>" 
                + OVER_EXPRESSED_FOR_SPECIES_COL_NAME + " speciesXX "
                + "</td><td>1</td><td>...</td></tr>");
        this.writeln("<tr><td>...</td><td>...</td><td></td><td></td></tr>");
        this.writeln("</tbody>");
        this.writeln("</table>");
        this.writeln("<p>An example OMA header line could be: <code>//OMA node ID 80 "
                + "contains gene IDs [ENSG00000169057, ENSMUSG00000031393] with gene names "
                + "[MECP2, Mecp2]</code></p>");
        
        this.writeln("<h5 id='multi_diff_simple_col1'>" + OMA_ID_COL_NAME + " (column 1)</h5>");
        this.writeln(this.getOMAIdColDescription());
        this.writeln("<h5 id='multi_diff_simple_col2'>" + MULTI_ANAT_ENTITY_IDS_COL_NAME 
                + " (column 2)</h5>");
        this.writeln(this.getMultiAnatEntityIdsColDescription());
        this.writeln("<h5 id='multi_diff_simple_col3'>" + MULTI_ANAT_ENTITY_NAMES_COL_NAME 
                + " (column 3)</h5>");
        this.writeln(this.getMultiAnatEntityNamesColDescription(2));
        this.writeln("<h5 id='multi_diff_simple_col4'>" + STAGE_ID_COL_NAME + " (column 4)</h5>");
        this.writeln(this.getMultiSpeciesStageIdColDescription());
        this.writeln("<h5 id='multi_diff_simple_col5'>" + STAGE_NAME_COL_NAME + " (column 5)</h5>");
        this.writeln(this.getStageNameColDescription(4));
        
        String totalNumberOfGenes = "<p>Please note that the list of all genes member of "
                + "the OMA homologous gene group with ID provided in <code>" 
                + OMA_ID_COL_NAME + "</code> (column 1) is provided through the OMA header line.</p>";
        this.writeln("<h5 id='multi_diff_simple_col6'>" + OVER_EXPRESSED_FOR_SPECIES_COL_NAME 
                + " speciesXX</h5>");
        this.writeln(this.getOverUnderExprForSpeciesColDescription(1, 4, 2, true));
        this.writeln(totalNumberOfGenes);
        this.writeln("<h5 id='multi_diff_simple_col7'>" + UNDER_EXPRESSED_FOR_SPECIES_COL_NAME 
                + " speciesXX</h5>");
        this.writeln(this.getOverUnderExprForSpeciesColDescription(1, 4, 2, false));
        this.writeln(totalNumberOfGenes);
        this.writeln("<h5 id='multi_diff_simple_col8'>" + NOT_DIFF_EXPRESSED_FOR_SPECIES_COL_NAME 
                + " speciesXX</h5>");
        this.writeln("<p>Number of genes, members of the OMA homologous gene group "
                + "with ID provided in <code>" + OMA_ID_COL_NAME + "</code> (column 1), "
                + "that were tested for differential expression in this condition (<code>" 
                + MULTI_ANAT_ENTITY_IDS_COL_NAME + "</code> (column 2), at <code>" 
                + STAGE_ID_COL_NAME + "</code> (column 4)), "
                + "but that were never shown to have a significant variation of "
                + "their level of expression as compared to the other conditions "
                + "of the analyses.</p>");
        this.writeln(totalNumberOfGenes);
        this.writeln("<h5 id='multi_diff_simple_col9'>" + NA_FOR_SPECIES_COL_NAME 
                + " speciesXX</h5>");
        this.writeln("<p>Number of genes, members of the OMA homologous gene group "
                + "with ID provided in <code>" + OMA_ID_COL_NAME + "</code> (column 1), "
                + "that were either not tested for differential expression in this condition (<code>" 
                + MULTI_ANAT_ENTITY_IDS_COL_NAME + "</code> (column 2), at <code>" 
                + STAGE_ID_COL_NAME + "</code> (column 4)), or for which coflicting "
                + "results were generated from different data types.</p>");
        this.writeln(totalNumberOfGenes);
        
        log.exit();
    }
    
    /**
     * Write the documentation related to multi-species complete over-/under-expression 
     * download files. The id attributes used in h4 tag must stay in sync with anchors used 
     * in quick jump links defined in method {@link #writeMultiSpeciesDiffExprFileDoc()}. 
     * If the header of this file changes, {@link #getMultiSpeciesCompleteDiffExprFileHeaderDesc()} 
     * must be updated.
     * 
     * @see #writeMultiSpeciesDiffExprCallFileDoc()
     * @see #getMultiSpeciesCompleteDiffExprCallFileHeaderDesc()
     */
    private void writeMultiSpeciesCompleteDiffExprCallFileDoc() {
        log.entry();
        
        this.writeln("<h4 id='multi_diff_complete'>Complete file</h4>");
        this.writeln("<p>In complete files, information for all genes are provided, "
                + "in all conditions tested, for anatomical entities homologous between "
                + "all species compared, and comparable broad developmental stages. "
                + "As opposed to simple multi-species files, all homology relations available "
                + "for the anatomical entities are considered, even from homology "
                + "hypotheses with low support; a column allows to retrieve the level of confidence "
                + "in the homology hypothesis used. Also, the number of columns in complete files "
                + "is not variable, whatever the number of species compared is.</p>");
        this.writeln("<p>Each gene homology group is separated with a \"header\" line, "
                + "starting with <code>//</code>, providing the IDs and names of the genes "
                //TODO: change when format has been updated
                + "member of the group. This allows notably to detect genes with no data "
                + "for a condition: if a gene is listed as a member of a homology group, "
                + "but there is no call for this gene in a given condition, it means "
                + "that there is no data available for this gene in this condition. "
                + "Please note that this format is likely to change "
                + "in the next release of Bgee.</p>");
        this.writeln("<table class='call_download_file'>");
        this.writeln("<caption>Format description for multi-species complete differential "
                + "expression file</caption>");
        this.writeln("<thead>");
        this.writeln("<tr><th>Column</th><th>Content</th><th>Cardinality</th><th>Example</th></tr>");
        this.writeln("</thead>");
        this.writeln("<tbody>");
        this.writeln("<tr><td>1</td><td><a href='#multi_diff_complete_col1' title='" 
                + "See " + OMA_ID_COL_NAME + " column description'>" + OMA_ID_COL_NAME 
                + "</a></td><td>1</td><td>80</td></tr>");
        //TODO: change order of columns anat entity/stage once we re-generate the files.
        this.writeln("<tr><td>2</td><td><a href='#multi_diff_complete_col2' title='" 
                + "See " + MULTI_ANAT_ENTITY_IDS_COL_NAME + " column description'>" 
                + MULTI_ANAT_ENTITY_IDS_COL_NAME 
                + "</a></td><td>1 or greater</td><td>UBERON:0001898</td></tr>");
        this.writeln("<tr><td>3</td><td><a href='#multi_diff_complete_col3' title='" 
                + "See " + MULTI_ANAT_ENTITY_NAMES_COL_NAME + " column description'>" 
                + MULTI_ANAT_ENTITY_NAMES_COL_NAME 
                + "</a></td><td>1 or greater</td><td>hypothalamus</td></tr>");
        this.writeln("<tr><td>4</td><td><a href='#multi_diff_complete_col4' title='" 
                + STAGE_ID_LINK_TITLE + "'>" + STAGE_ID_COL_NAME 
                + "</a></td><td>1</td><td>UBERON:0000113</td></tr>");
        this.writeln("<tr><td>5</td><td><a href='#multi_diff_complete_col5' title='" 
                + STAGE_NAME_LINK_TITLE + "'>" + STAGE_NAME_COL_NAME 
                + "</a></td><td>1</td><td>post-juvenile adult stage</td></tr>");
        this.writeln("<tr><td>6</td><td><a href='#multi_diff_complete_col6' title='" 
                + "See " + SPECIES_LATIN_NAME_COL_NAME + " column description'>" 
                + SPECIES_LATIN_NAME_COL_NAME 
                + "</a></td><td>1</td><td>1</td></tr>");
        this.writeln("<tr><td>7</td><td><a href='#multi_diff_complete_col7' title='" 
                + GENE_ID_LINK_TITLE + "'>" + GENE_ID_COL_NAME 
                + "</a></td><td>1</td><td>1</td></tr>");
        this.writeln("<tr><td>8</td><td><a href='#multi_diff_complete_col8' title='" 
                + GENE_NAME_LINK_TITLE + "'>" + GENE_NAME_COL_NAME 
                + "</a></td><td>1</td><td>1</td></tr>");
        this.writeln("<tr><td>9</td><td><a href='#multi_diff_complete_col9' title='" 
                + DIFF_EXPR_STATE_LINK_TITLE + "'>" + DIFF_EXPR_STATE_COL_NAME 
                + "</a></td><td>1</td><td>over-expression</td></tr>");
        this.writeln("<tr><td>10</td><td><a href='#multi_diff_complete_col10' title='" 
                + DIFF_EXPR_QUAL_LINK_TITLE + "'>" + DIFF_EXPR_QUAL_COL_NAME 
                + "</a></td><td>1</td><td>high quality</td></tr>");
        this.writeln("<tr><td>11</td><td><a href='#multi_diff_complete_col11' title='" 
                + "See " + AFFY_DIFF_EXPR_STATE_COL_NAME + " column description'>" 
                + AFFY_DIFF_EXPR_STATE_COL_NAME 
                + "</a></td><td>1</td><td>over-expression</td></tr>");
        this.writeln("<tr><td>12</td><td><a href='#multi_diff_complete_col12' title='" 
                + "See " + AFFY_DIFF_EXPR_QUAL_COL_NAME + " column description'>" 
                + AFFY_DIFF_EXPR_QUAL_COL_NAME
        //TODO: change 'poor' to 'low'
                + "</a></td><td>1</td><td>poor quality</td></tr>");
        this.writeln("<tr><td>13</td><td><a href='#multi_diff_complete_col13' title='" 
                + "See " + AFFY_DIFF_EXPR_P_VAL_COL_NAME + " column description'>" 
                + AFFY_DIFF_EXPR_P_VAL_COL_NAME 
                + "</a></td><td>1</td><td>0.0035659347</td></tr>");
        this.writeln("<tr><td>14</td><td><a href='#multi_diff_complete_col14' title='" 
                + "See " + AFFY_DIFF_EXPR_SUPPORT_COUNT_COL_NAME + " column description'>" 
                + AFFY_DIFF_EXPR_SUPPORT_COUNT_COL_NAME 
                + "</a></td><td>1</td><td>1</td></tr>");
        this.writeln("<tr><td>15</td><td><a href='#multi_diff_complete_col15' title='" 
                + "See " + AFFY_DIFF_EXPR_CONFLICT_COUNT_COL_NAME + " column description'>" 
                + AFFY_DIFF_EXPR_CONFLICT_COUNT_COL_NAME 
                + "</a></td><td>1</td><td>1</td></tr>");
        this.writeln("<tr><td>16</td><td><a href='#multi_diff_complete_col16' title='" 
                + "See " + RNA_SEQ_DIFF_EXPR_STATE_COL_NAME + " column description'>" 
                + RNA_SEQ_DIFF_EXPR_STATE_COL_NAME 
                + "</a></td><td>1</td><td>over-expression</td></tr>");
        this.writeln("<tr><td>17</td><td><a href='#multi_diff_complete_col17' title='" 
                + "See " + RNA_SEQ_DIFF_EXPR_QUAL_COL_NAME + " column description'>" 
                + RNA_SEQ_DIFF_EXPR_QUAL_COL_NAME 
                + "</a></td><td>1</td><td>high quality</td></tr>");
        this.writeln("<tr><td>18</td><td><a href='#multi_diff_complete_col18' title='" 
                + "See " + RNA_SEQ_DIFF_EXPR_P_VAL_COL_NAME + " column description'>" 
                + RNA_SEQ_DIFF_EXPR_P_VAL_COL_NAME 
                + "</a></td><td>1</td><td>2.96E-8</td></tr>");
        this.writeln("<tr><td>19</td><td><a href='#multi_diff_complete_col19' title='" 
                + "See " + RNA_SEQ_DIFF_EXPR_SUPPORT_COUNT_COL_NAME + " column description'>" 
                + RNA_SEQ_DIFF_EXPR_SUPPORT_COUNT_COL_NAME 
                + "</a></td><td>1</td><td>2</td></tr>");
        this.writeln("<tr><td>20</td><td><a href='#multi_diff_complete_col20' title='" 
                + "See " + RNA_SEQ_DIFF_EXPR_CONFLICT_COUNT_COL_NAME + " column description'>" 
                + RNA_SEQ_DIFF_EXPR_CONFLICT_COUNT_COL_NAME 
                + "</a></td><td>1</td><td>0</td></tr>");
        this.writeln("<tr><td>21</td><td><a href='#multi_diff_complete_col21' title='" 
                + "See " + ANAT_HOMOLOGY_CIO_ID_COL_NAME + " column description'>" 
                + ANAT_HOMOLOGY_CIO_ID_COL_NAME 
                + "</a></td><td>1</td><td>0</td></tr>");
        this.writeln("<tr><td>22</td><td><a href='#multi_diff_complete_col22' title='" 
                + "See " + ANAT_HOMOLOGY_CIO_NAME_COL_NAME + " column description'>" 
                + ANAT_HOMOLOGY_CIO_NAME_COL_NAME 
                + "</a></td><td>1</td><td>0</td></tr>");
        this.writeln("</tbody>");
        this.writeln("</table>");
        this.writeln("<p>An example OMA header line could be: <code>//OMA node ID 80 "
                + "contains gene IDs [ENSG00000169057, ENSMUSG00000031393] with gene names "
                + "[MECP2, Mecp2]</code></p>");
        
        this.writeln("<h5 id='multi_diff_complete_col1'>" + OMA_ID_COL_NAME + " (column 1)</h5>");
        this.writeln(this.getOMAIdColDescription());
        this.writeln("<h5 id='multi_diff_complete_col2'>" + MULTI_ANAT_ENTITY_IDS_COL_NAME 
                + " (column 2)</h5>");
        this.writeln(this.getMultiAnatEntityIdsColDescription());
        this.writeln("<h5 id='multi_diff_complete_col3'>" + MULTI_ANAT_ENTITY_NAMES_COL_NAME 
                + " (column 3)</h5>");
        this.writeln(this.getMultiAnatEntityNamesColDescription(2));
        //invert anat entities and stages columns after files are re-generated
        this.writeln("<h5 id='multi_diff_complete_col4'>" + STAGE_ID_COL_NAME + " (column 4)</h5>");
        this.writeln(this.getMultiSpeciesStageIdColDescription());
        this.writeln("<h5 id='multi_diff_complete_col5'>" + STAGE_NAME_COL_NAME + " (column 5)</h5>");
        this.writeln(this.getStageNameColDescription(4));
        this.writeln("<h5 id='multi_diff_complete_col6'>" + SPECIES_LATIN_NAME_COL_NAME 
                + " (column 6)</h5>");
        this.writeln("<p>The latin name of the species which the gene in " + GENE_ID_COL_NAME 
                + " (column 7) belongs to.</p>");
        this.writeln("<h5 id='multi_diff_complete_col7'>" + GENE_ID_COL_NAME + " (column 7)</h5>");
        this.writeln(this.getGeneIdColDescription());
        this.writeln("<p>Please note that the list of all genes member of "
                + "the OMA homologous gene group with ID provided in <code>" 
                + OMA_ID_COL_NAME + "</code> (column 1) is provided through the OMA header line. "
                + "If a gene listed in this header line has no call for the condition "
                + "<code>" + MULTI_ANAT_ENTITY_IDS_COL_NAME + "</code> (column 2), at <code>" 
                + STAGE_ID_COL_NAME + "</code> (column 4), it means "
                + "that there is no data available for this gene in this condition.</p>");
        this.writeln("<h5 id='multi_diff_complete_col8'>" + GENE_NAME_COL_NAME + " (column 8)</h5>");
        this.writeln(this.getGeneNameColDescription(7));
        this.writeln("<h5 id='multi_diff_complete_col9'>" + DIFF_EXPR_STATE_COL_NAME 
                + " (column 9)</h5>");
        this.writeln(this.getDiffExprStateColDescription(7, 4, 2, false, true, true, false, 
                "all data types")); 
        this.writeln("<h5 id='multi_diff_complete_col10'>" + DIFF_EXPR_QUAL_COL_NAME 
                + " (column 10)</h5>");
        this.writeln(this.getDiffExprQualColDescription(DIFF_EXPR_STATE_COL_NAME, 7, true, false)); 
        this.writeln("<h5 id='multi_diff_complete_col11'>" + AFFY_DIFF_EXPR_STATE_COL_NAME 
                + " (column 11)</h5>");
        this.writeln(this.getDiffExprStateColDescription(7, 4, 2, false, true, false, true, 
                "Affymetrix data")); 
        this.writeln("<h5 id='multi_diff_complete_col12'>" + AFFY_DIFF_EXPR_QUAL_COL_NAME 
                + " (column 12)</h5>");
        this.writeln(this.getDiffExprQualColDescription(AFFY_DIFF_EXPR_STATE_COL_NAME, 9, false, true)); 
        this.writeln("<h5 id='multi_diff_complete_col13'>" + AFFY_DIFF_EXPR_P_VAL_COL_NAME 
                + " (column 13)</h5>");
        this.writeln(this.getDiffExprPvalColDescription(
                "Affymetrix", AFFY_DIFF_EXPR_STATE_COL_NAME, 11));
        this.writeln("<h5 id='multi_diff_complete_col14'>" + AFFY_DIFF_EXPR_SUPPORT_COUNT_COL_NAME 
                + " (column 14)</h5>");
        this.writeln(this.getDiffSupportCountColDescription(
                "Affymetrix", AFFY_DIFF_EXPR_STATE_COL_NAME, 11));
        this.writeln("<h5 id='multi_diff_complete_col15'>" + AFFY_DIFF_EXPR_CONFLICT_COUNT_COL_NAME 
                + " (column 15)</h5>");
        this.writeln(this.getDiffConflictCountColDescription(
                "Affymetrix", AFFY_DIFF_EXPR_STATE_COL_NAME, 11));
        this.writeln("<h5 id='multi_diff_complete_col16'>" + RNA_SEQ_DIFF_EXPR_STATE_COL_NAME 
                + " (column 16)</h5>");
        this.writeln(this.getDiffExprStateColDescription(7, 4, 2, false, true, false, true, "RNA-Seq data")); 
        this.writeln("<h5 id='multi_diff_complete_col17'>" + RNA_SEQ_DIFF_EXPR_QUAL_COL_NAME 
                + " (column 17)</h5>");
        this.writeln(this.getDiffExprQualColDescription(RNA_SEQ_DIFF_EXPR_STATE_COL_NAME, 14, false, true)); 
        this.writeln("<h5 id='multi_diff_complete_col18'>" + RNA_SEQ_DIFF_EXPR_P_VAL_COL_NAME 
                + " (column 18)</h5>");
        this.writeln(this.getDiffExprPvalColDescription(
                "RNA-Seq", RNA_SEQ_DIFF_EXPR_STATE_COL_NAME, 16));
        this.writeln("<h5 id='multi_diff_complete_col19'>" + RNA_SEQ_DIFF_EXPR_SUPPORT_COUNT_COL_NAME 
                + " (column 19)</h5>");
        this.writeln(this.getDiffSupportCountColDescription(
                "RNA-Seq", RNA_SEQ_DIFF_EXPR_STATE_COL_NAME, 16));
        this.writeln("<h5 id='multi_diff_complete_col20'>" + RNA_SEQ_DIFF_EXPR_CONFLICT_COUNT_COL_NAME 
                + " (column 20)</h5>");
        this.writeln(this.getDiffConflictCountColDescription(
                "RNA-Seq", RNA_SEQ_DIFF_EXPR_STATE_COL_NAME, 16));
        this.writeln("<h5 id='multi_diff_complete_col21'>" + ANAT_HOMOLOGY_CIO_ID_COL_NAME 
                + " (column 21)</h5>");
        this.writeln("<p>Unique identifier from the "
                + "<a target='_blank' title='External link to CIO in OBO' "
                + "href='https://github.com/BgeeDB/confidence-information-ontology/blob/master/src/ontology/cio-simple.obo'>"
                + "Confidence Information Ontology</a>, providing the confidence "
                + "in the annotation of homology of anatomical entities defined in <code>"
                + MULTI_ANAT_ENTITY_IDS_COL_NAME + "</code> (column 2). This ontology is an attempt "
                + "to provide a mean to capture the confidence in annotations. "
                + "See <a target='_blank' title='External link to CIO project' "
                + "href='https://github.com/BgeeDB/confidence-information-ontology'>"
                + "project home</a> for more details.</p>");
        this.writeln("<h5 id='multi_diff_complete_col22'>" + ANAT_HOMOLOGY_CIO_NAME_COL_NAME 
                + " (column 22)</h5>");
        this.writeln("<p>Name of the CIO term defined by <code>" 
                + ANAT_HOMOLOGY_CIO_ID_COL_NAME + "</code> (column 21)</p>");
        
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
     * @return  A {@code String} that is the description of the OMA ID column 
     *          in multi-species download files (because we use it several times), formated in HTML 
     *          and HTML escaped if necessary.
     * @see #OMA_ID_COL_NAME
     */
    private String getOMAIdColDescription() {
        log.entry();
        return log.exit("<p>Unique identifier of the OMA gene homology group. Note that "
                + "these identifiers are not stable between releases, and cannot be used "
                + "to retrieve data from <a target='_blank' "
                + "href='http://omabrowser.org/oma/hogs/' title='External link to OMA browser'>"
                + "the OMA browser</a>. They are provided solely to group data "
                + "from homologous genes belonging to a same homology group.</p>");
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
     * @return  A {@code String} that is the description of the stage ID column 
     *          in multi-species download files (because we use it several times), formated in HTML 
     *          and HTML escaped if necessary.
     * @see #STAGE_ID_COL_NAME
     */
    private String getMultiSpeciesStageIdColDescription() {
        log.entry();
        return log.exit("<p>Unique identifier of the developmental stage, from the Uberon ontology. "
                + "For multi-species analyses, only broad developmental stages are used, "
                + "common to the species being compared.</p>");
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
     * @return  A {@code String} that is the description of the column for multiple 
     *          anatomical IDs for multi-species download files (because we use it several times), 
     *          formated in HTML and HTML escaped if necessary.
     * @see #MULTI_ANAT_ENTITY_IDS_COL_NAME
     */
    private String getMultiAnatEntityIdsColDescription() {
        log.entry();
        return log.exit("<p>Unique identifiers of the homologous anatomical entities, "
                + "from the Uberon ontology. Cardinality 1 or greater. When more than "
                + "one anatomical entity is used, they are separated with the character "
                + "<code>|</code>.</p>"
                + "<p>In most cases, the cardinality is 1, as most of "
                + "the homologous anatomical entities compared in different species are not derived "
                + "enough so that they are described by different anatomical concepts. "
                + "But the cardinality can sometimes be greater, when homologous "
                + "anatomical entities are highly derived in the species compared, "
                + "and represented by distinct anatomical concepts.</p>"
                + "<p>For instance, if comparing expression data in human and zebrafish, "
                + "the anatomical entity \"bulbus arteriosus\" (UBERON:0004152) would be considered, "
                + "as it is believed to be homologous in the <i>Euteleostomi</i> lineage; "
                + "as it is represented by the same anatomical term in both species, "
                + "the cardinality of the value for this column would be 1. But "
                + "homology relations between distinct anatomical concepts would also "
                + "be considered, such as the homology between lung (UBERON:0002048) and "
                + "swim bladder (UBERON:0006860): these organs "
                + "are believed to descend from a same common ancestral organ, existing "
                + "in the ancestor of <i>Gnathostomata</i>, but are now sufficiently derived "
                + "that they are represented by different anatomical concepts in these species; "
                + "the cardinality of the value of this column would be 2, and the IDs "
                + "of these anatomical entities would be separated by the character "
                + "<code>|</code>, e.g., <code>UBERON:0002048|UBERON:0006860</code>.</p>");
    }
    /**
     * @param colNumber An {@code int} that is the index of the column containing 
     *                  the anatomical entity IDs (see {@link #MULTI_ANAT_ENTITY_IDS_COL_NAME}). 
     *                  Index starting from 1.
     * @return  A {@code String} that is the description of the column for multiple 
     *          anatomical entity names in multi-species download files (because we use it 
     *          several times), formated in HTML and HTML escaped if necessary.
     * @see #MULTI_ANAT_ENTITY_NAMES_COL_NAME
     */
    private String getMultiAnatEntityNamesColDescription(int colNumber) {
        log.entry();
        return log.exit("<p>Names of the anatomical entities defined by <code>" 
            + MULTI_ANAT_ENTITY_IDS_COL_NAME + "</code> (column " + colNumber + "). "
            + "Cardinality 1 or greater. When more than "
            + "one anatomical entity is used, they are separated with the character "
            + "<code>|</code>. See "+ MULTI_ANAT_ENTITY_IDS_COL_NAME + " column description "
            + "for more details.</p>");
    }
    
    /**
     * Generates a sentence listing the columns defining a call, in HTML, with HTML escaped 
     * if necessary.
     * 
     * @param geneIdColNumber           An {@code int} that is the index of the column 
     *                                  containing the gene ID (see {@link #GENE_ID_COL_NAME}). 
     *                                  Index starting from 1.
     * @param stageIdColNumber          An {@code int} that is the index of the column 
     *                                  containing the stage ID (see {@link #STAGE_ID_COL_NAME}). 
     *                                  Index starting from 1.
     * @param anatEntityIdColNumber     An {@code int} that is the index of the column 
     *                                  containing either the anatomical entity ID (see 
     *                                  {@link #ANAT_ENTITY_ID_COL_NAME}) for single-species files, 
     *                                  or the anatomical entity IDs (see 
     *                                  {@link #MULTI_ANAT_ENTITY_IDS_COL_NAME}) 
     *                                  for multi-species files. 
     *                                  Index starting from 1.
     * @param singleSpecies             A {@code boolean} defining whether the column names 
     *                                  used should be defined for a single-species file, 
     *                                  or a multi-species file. 
     *                                  Index starting from 1.
     * @return  A {@code String} that is the sentence "Gene ID (column xx), 
     *          in Anatomical entity ID (column xx), at Developmental stage ID (column xx)" 
     *          with proper column name and provided column indexes.
     */
    private String getColumnListForCall(int geneIdColNumber, int stageIdColNumber, 
            int anatEntityIdColNumber, boolean singleSpecies) {
        log.entry(geneIdColNumber, stageIdColNumber, anatEntityIdColNumber, singleSpecies);
        return log.exit("<code>" + GENE_ID_COL_NAME + "</code> (column " 
                + geneIdColNumber + "), in <code>" 
                + (singleSpecies ? ANAT_ENTITY_ID_COL_NAME : MULTI_ANAT_ENTITY_IDS_COL_NAME) 
                + "</code> (column " 
                + anatEntityIdColNumber + "), at <code>" + STAGE_ID_COL_NAME + "</code> (column " 
                + stageIdColNumber + ")");
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
     *                                  containing either the anatomical entity ID (see 
     *                                  {@link #ANAT_ENTITY_ID_COL_NAME}) for single-species files, 
     *                                  or the anatomical entity IDs (see 
     *                                  {@link #MULTI_ANAT_ENTITY_IDS_COL_NAME}) 
     *                                  for multi-species files. 
     *                                  Index starting from 1.
     * @param singleSpecies             A {@code boolean} defining whether the column names 
     *                                  used should be defined for a single-species file, 
     *                                  or a multi-species file. 
     * @return  A {@code String} that is the description of the expression state column 
     *          in download files (because we use it several times), formated in HTML 
     *          and HTML escaped if necessary.
     * @see #EXPR_STATE_COL_NAME
     */
    private String getExprStateColDescription(int geneIdColNumber, int stageIdColNumber, 
            int anatEntityIdColNumber, boolean singleSpecies) {
        log.entry(geneIdColNumber, stageIdColNumber, anatEntityIdColNumber);
        return log.exit("<p>Call generated from all data types for " 
                + this.getColumnListForCall(geneIdColNumber, stageIdColNumber, 
                        anatEntityIdColNumber, singleSpecies) + ". One of: </p>"
                + "<ul class='doc_content'>"
                + "<li><span class='list_element_title'>expression high quality</span>: "
                + "expression reported as high quality, from Bgee statistical tests and/or from "
                + "<i>in situ</i> data sources, with no contradicting call of absence "
                + "of expression for same gene, in same anatomical entity and developmental stage "
                + "(call generated either from multiple congruent data, "
                + "or from single data);</li>"
                + "<li><span class='list_element_title'>expression low quality</span>: "
                + "expression reported as low quality, either from Bgee statistical tests and/or "
                + "from <i>in situ</i> data sources, or because there exists a conflict of "
                + "presence/absence of expression for the same gene, anatomical entity "
                + "and developmental stage, from different data of a same type "
                + "(conflicts between different data types are treated differently, see below);</li>"
                + "<li><span class='list_element_title'>absent high quality</span>: "
                + "report of absence of expression, either from Bgee statistical tests and/or "
                + "from <i>in situ</i> data sources; in Bgee, calls of absence of expression "
                + "are always discarded if there exists a contradicting call of expression, "
                + "from the same data type and for the same gene, in the same anatomical entity "
                + "and developmental stage, or in a child entity or child developmental stage; "
                + "this is why they are always considered of high quality;</li>"
                + "<li><span class='list_element_title'>low ambiguity</span>: "
                + "there exists a call of expression generated from a data type, but "
                + "there exists a call of absence of expression generated from another data type "
                + "for the same gene in a parent anatomical entity at the same developmental "
                + "stage; for instance, gene A is reported to be expressed in the midbrain "
                + "at young adult stage from Affymetrix data, but is reported to be not expressed "
                + "in the brain at young adult stage from RNA-Seq data;</li>"
                + "<li><span class='list_element_title'>high ambiguity</span>: "
                + "there exists a call of expression generated from a data type, but "
                + "there exists a call of absence of expression generated from another data type "
                + "for the same gene, anatomical entity and developmental stage; for instance, "
                + "gene A is reported to be expressed in the midbrain at young adult stage "
                + "from Affymetrix data, but is reported to be not expressed in the midbrain "
                + "at young adult stage from RNA-Seq data.</li>"
                + "</ul>");
    } 
    /**
     * Generates description of the differential expression state column. 
     * 
     * @param geneIdColNumber           An {@code int} that is the index of the column 
     *                                  containing the gene ID (see {@link #GENE_ID_COL_NAME}). 
     *                                  Index starting from 1.
     * @param stageIdColNumber          An {@code int} that is the index of the column 
     *                                  containing the stage ID (see {@link #STAGE_ID_COL_NAME}). 
     *                                  Index starting from 1.
     * @param anatEntityIdColNumber     An {@code int} that is the index of the column 
     *                                  containing either the anatomical entity ID (see 
     *                                  {@link #ANAT_ENTITY_ID_COL_NAME}) for single-species files, 
     *                                  or the anatomical entity IDs (see 
     *                                  {@link #MULTI_ANAT_ENTITY_IDS_COL_NAME}) 
     *                                  for multi-species files. 
     *                                  Index starting from 1.
     * @param singleSpecies             A {@code boolean} defining whether the column names 
     *                                  used should be defined for a single-species file, 
     *                                  or a multi-species file. 
     * @param noDiffExpr                A {@code boolean} defining whether explanation 
     *                                  about the call 'no diff expression' should be provided. 
     * @param ambiguity                 A {@code boolean} defining whether explanation 
     *                                  about the ambiguity status should be provided. 
     * @param noData                    A {@code boolean} defining whether explanation 
     *                                  about the "no data" status should be provided. 
     * @param dataType                  A {@code String} allowing to provide information 
     *                                  about the data types used to produce the state.
     * @return  A {@code String} that is the description of the differential expression state column 
     *          in download files (because we use it several times), formated in HTML 
     *          and HTML escaped if necessary.
     * @see #DIFF_EXPR_STATE_COL_NAME
     * @see #getDiffExprQualColDescription(int)
     */
    private String getDiffExprStateColDescription(int geneIdColNumber, int stageIdColNumber, 
            int anatEntityIdColNumber, boolean singleSpecies, boolean noDiffExpr, 
            boolean ambiguity, boolean noData, String dataType) {
        log.entry(geneIdColNumber, stageIdColNumber, anatEntityIdColNumber, singleSpecies, 
                noDiffExpr, ambiguity, noData, dataType);
        
        String desc = "<p>Call generated from " + dataType + " for " 
                + this.getColumnListForCall(geneIdColNumber, stageIdColNumber, 
                        anatEntityIdColNumber, singleSpecies) + ". One of: </p>"
                + "<ul class='doc_content'>"
                + "<li><span class='list_element_title'>over-expression</span>: "
                + "the gene was shown in one or more analyses to have a significant over-expression "
                + "in this condition, as compared to the expression levels in other conditions "
                + "of the analyses;</li>"
                + "<li><span class='list_element_title'>under-expression</span>: "
                + "the gene was shown in one or more analyses to have a significant under-expression "
                + "in this condition, as compared to the expression levels in other conditions "
                + "of the analyses;</li>";
        if (noDiffExpr) {
            desc += "<li><span class='list_element_title'>no diff expression</span>: "
                    + "the gene was tested for differential expression in this condition, "
                    + "but was never shown to have a significant variation of expression "
                    + "as compared to the other conditions of the analyses;</li>";
        }
        if (ambiguity) {
                //TODO: change 'weak' to 'low' when files will be re-generated
            desc += "<li><span class='list_element_title'>weak ambiguity</span>: "
                + "there exists a call of over-expression or under-expression generated "
                + "from a data type, but another data type showed no significant variation "
                + "of the level of expression of this gene in the same condition; or, a call "
                + "of over-expression was generated from a data type, but the gene was "
                + "shown to be never expressed in another analysis including the same condition, "
                + "using a different data type;</li>"
                //TODO: change 'weak' to 'low' when files will be re-generated
                + "<li><span class='list_element_title'>strong ambiguity</span>: "
                + "there exists a call of over-expression or under-expression generated "
                + "from a data type, but there exists a call in the opposite direction "
                + "generated from another data type for the same gene, anatomical entity "
                + "and developmental stage; for instance, gene A is reported to be over-expressed "
                + "in the midbrain at young adult stage from Affymetrix data, but is reported "
                + "to be under-expressed in the midbrain at young adult stage from RNA-Seq data.</li>";
        }
        if (noData) {
            desc += "<li><span class='list_element_title'>no data</span>: "
                    + "no analyses of this data type compared expression level of this gene "
                    + "in this condition;</li>";
        }
        desc += "</ul>";
        
        return log.exit(desc);
    }
    /**
     * Generates the description of the column storing best p-values for each data type 
     * in complete differential expression files.
     * 
     * @param dataType              A {@code String} that is the name of the data type used.
     * @param diffExprStateColName  A {@code String} that is the name of the column storing 
     *                              the differential expression state generated from this data type.
     * @param diffExprStateColIndex An {@code int} that is the index of the column storing 
     *                              the differential expression state generated from this data type.
     *                              Index starting from 1.
     * @return  A {@code String} that is the description of the column storing best p-values 
     *          in complete differential expression download files, formated in HTML 
     *          and HTML escaped if necessary.
     */
    private String getDiffExprPvalColDescription(String dataType, String diffExprStateColName, 
            int diffExprStateColIndex) {
        log.entry(dataType, diffExprStateColName, diffExprStateColIndex);
        return log.exit("<p>Best p-value from the " + dataType + " analyses supporting the "
                + dataType + " call provided in <code>" + diffExprStateColName 
                + "</code> (column " + diffExprStateColIndex + "). Set to 1.0 if no data available "
                + "by " + dataType + ".</p>");
    }
    /**
     * Generates the description of the column storing for each data type the number of 
     * analyses supporting a differential expressionc call, 
     * in complete differential expression files.
     * 
     * @param dataType              A {@code String} that is the name of the data type used.
     * @param diffExprStateColName  A {@code String} that is the name of the column storing 
     *                              the differential expression state generated from this data type.
     * @param diffExprStateColIndex An {@code int} that is the index of the column storing 
     *                              the differential expression state generated from this data type.
     *                              Index starting from 1.
     * @return  A {@code String} that is the description of the column storing number of 
     *          supporting analyses in complete differential expression download files, 
     *          formated in HTML and HTML escaped if necessary.
     */
    private String getDiffSupportCountColDescription(String dataType, String diffExprStateColName, 
            int diffExprStateColIndex) {
        log.entry(dataType, diffExprStateColName, diffExprStateColIndex);
        return log.exit("<p>Number of " + dataType + " analyses supporting the " 
                + dataType + " call provided in <code>" + diffExprStateColName + "</code> (column " 
                + diffExprStateColIndex + "). Set to 0 if no data available by " + dataType 
                + ".</p>");
    }
    /**
     * Generates the description of the column storing for each data type the number of 
     * analyses in conflict with a differential expressionc call produced, 
     * in complete differential expression files.
     * 
     * @param dataType              A {@code String} that is the name of the data type used.
     * @param diffExprStateColName  A {@code String} that is the name of the column storing 
     *                              the differential expression state generated from this data type.
     * @param diffExprStateColIndex An {@code int} that is the index of the column storing 
     *                              the differential expression state generated from this data type.
     *                              Index starting from 1.
     * @return  A {@code String} that is the description of the column storing number of 
     *          conflicting analyses in complete differential expression download files, 
     *          formated in HTML and HTML escaped if necessary.
     */
    private String getDiffConflictCountColDescription(String dataType, String diffExprStateColName, 
            int diffExprStateColIndex) {
        log.entry(dataType, diffExprStateColName, diffExprStateColIndex);
        return log.exit("<p>Number of " + dataType + " analyses in conflict, generating "
                + "a call different from the call provided in <code>" + diffExprStateColName 
                + "</code> (column " + diffExprStateColIndex + "). Set to 0 if no data available by " 
                + dataType + ".</p>");
    }
    /**
     * Generates description of the expression state column. 
     * 
     * @param diffExprStateColName      A {@code String} that is the name of the column 
     *                                  containing the related differential expression state. 
     *                                  Index starting from 1.
     * @param diffExprStateColNumber    An {@code int} that is the index of the column 
     *                                  containing the related differential expression state. 
     *                                  Index starting from 1.
     * @param displayNA                 A {@code boolean} defining whether explanation 
     *                                  about the "N/A" quality should be provided. 
     * @param displayNoData             A {@code boolean} defining whether explanation 
     *                                  about the "no data" quality should be provided. 
     * @return  A {@code String} that is the description of the diff expression quality column 
     *          in download files (because we use it several times), formated in HTML 
     *          and HTML escaped if necessary.
     * @see #getDiffExprStateColDescription(int, int, int, boolean, boolean, String)
     */
    private String getDiffExprQualColDescription(String diffExprStateColName, 
            int diffExprStateColNumber, boolean displayNA, boolean displayNoData) {
        log.entry(diffExprStateColName, diffExprStateColNumber, displayNA, displayNoData);
        String desc = "<p>Confidence in the differential expression call provided in <code>"
                + diffExprStateColName + "</code> (column " + diffExprStateColNumber + "). One of: </p>"
                + "<ul class='doc_content'>"
                + "<li><span class='list_element_title'>high quality</span>: "
                + "differential expression reported as high quality, with no contradicting "
                + "call from same type of analysis (across anatomy/across life stages), "
                + "for same gene, in same anatomical entity and developmental stage, "
                + "(call generated either from multiple congruent analyses, "
                + "or from a single analysis);</li>"
                //TODO: change 'poor' to 'low' after we re-generate the file
                + "<li><span class='list_element_title'>poor quality</span>: "
                + "differential expression reported as low quality, or there exists a conflict "
                + "from same type of analysis (across anatomy/across life stages), "
                + "for the same gene, anatomical entity and developmental stage, "
                + "from different analyses of a same data type "
                + "(conflicts between different data types are treated differently); "
                + "for instance, an analysis showed a gene to be over-expressed on a condition, "
                + "while another analysis showed the same gene to be under-expressed or "
                //TODO: add link to data analyses section
                + "not differentially expressed in the same condition; such conflicts "
                + "are resolved by a voting system based on the number of conditions compared, "
                + "weighted by p-value.</li>";
        if (displayNA) {
            //TODO: merge N/A and 'no data' once we re-generate the files
            desc += "<li><span class='list_element_title'>N/A</span>: no quality applicable "
                + "when ambiguity state in <code>" + diffExprStateColName 
                + "</code> (column " + diffExprStateColNumber + ");</li>";
        }
        if (displayNoData) {
            desc += "<li><span class='list_element_title'>no data</span>: no data associated "
                    + "to <code>" + diffExprStateColName 
                    + "</code> (column " + diffExprStateColNumber + ");</li>";
        }
        desc += "</ul>";
        
        return log.exit(desc);
    } 
    
    /**
     * Get the description for columns providing number of over-expressed or under-expressed 
     * genes in multi-species simple differential expression files.
     * 
     * @param omaIdColNumber            An {@code int} that is the index of the column containing 
     *                                  the OMA ID (see {@link #OMA_ID_COL_NAME}). 
     *                                  Index starting from 1.
     * @param stageIdColNumber          An {@code int} that is the index of the column containing 
     *                                  the stage ID (see {@link #STAGE_ID_COL_NAME}). 
     *                                  Index starting from 1.
     * @param anatEntityIdsColNumber    An {@code int} that is the index of the column containing 
     *                                  the anatomical entity IDs (see 
     *                                  {@link #MULTI_ANAT_ENTITY_IDS_COL_NAME}). 
     *                                  Index starting from 1.
     * @param overExpressed             A {@code boolean} defining whether the column described 
     *                                  contains over-expressed gene count or under-expressed 
     *                                  gene count. If {@code true}, it contains 
     *                                  over-expressed gene count.
     * @return  A {@code String} that is the description of the over-/under-expressed gene count 
     *          column in multi-species diff expression download files, 
     *          formated in HTML and HTML escaped if necessary.
     */
    private String getOverUnderExprForSpeciesColDescription(int omaIdColNumber, 
            int stageIdColNumber, int anatEntityIdsColNumber, boolean overExpressed) {
        log.entry(omaIdColNumber, stageIdColNumber, anatEntityIdsColNumber, overExpressed);
        
        String desc = "<p>Number of genes, members of the OMA homologous gene group "
                + "with ID provided in <code>" + OMA_ID_COL_NAME + "</code> (column " 
                + omaIdColNumber + "), shown in one or more analyses to have a significant " +  
                (overExpressed ? "over-expression " : "under-expression ")
                + "in this condition (<code>" + MULTI_ANAT_ENTITY_IDS_COL_NAME + "</code> (column " 
                + anatEntityIdsColNumber + "), at <code>" + STAGE_ID_COL_NAME + "</code> (column " 
                + stageIdColNumber + ")), as compared to the expression levels "
                + "in other conditions of the analyses. This means that there were no conflicts "
                + "found between results generated from different data types "
                + "(result generated either from a single data type, or from "
                + "congruent analyses of different data types). Note that there can still "
                + "be conflicts between different analyses within a same data type, "
                //TODO: add link to data analyses documentation
                + "but such conflicts are resolved by a voting system based on the number "
                + "of conditions compared, weighted by p-value, in order to produce "
                + "a single differential expression call, taking into account all analyses "
                + "from a given data type.</p>";
        
        return log.exit(desc);
    }
    

    /**
     * @return  a {@code String} containing the HTML to create a table containing the description 
     *          of the header of a single species simple expression file (can be used 
     *          in "help" links).
     */
    public String getSingleSpeciesSimpleExprFileHeaderDesc() {
        log.entry();
        //TODO: change when we split the state and the qual
        return log.exit("<table class='download_file_header_desc'>"
                + "<tbody>"
                + "<tr><td>1</td><td>2</td><td>3</td><td>4</td><td>5</td><td>6</td><td>7</td></tr>"
                + "<tr>"
                + "<td>" + GENE_ID_COL_NAME + "</td>"
                + "<td>" + GENE_NAME_COL_NAME + "</td>"
                + "<td>" + STAGE_ID_COL_NAME + "</td>"
                + "<td>" + STAGE_NAME_COL_NAME + "</td>"
                + "<td>" + ANAT_ENTITY_ID_COL_NAME + "</td>"
                + "<td>" + ANAT_ENTITY_NAME_COL_NAME + "</td>"
                + "<td>" + EXPR_STATE_COL_NAME + "</td>"
                + "</tr>"
                + "</tbody>"
                + "</table>");
    }
    /**
     * @return  a {@code String} containing the HTML to create a table containing the description 
     *          of the header of a single species complete expression file (can be used 
     *          in "help" links).
     */
    public String getSingleSpeciesCompleteExprFileHeaderDesc() {
        log.entry();
        return log.exit("<table class='download_file_header_desc'>"
                + "<tbody>"
                + "<tr><td>1</td><td>2</td><td>3</td><td>4</td><td>5</td><td>6</td><td>7</td>"
                + "<td>8</td><td>9</td><td>10</td><td>11</td><td>12</td></tr>"
                + "<tr>"
                + "<td>" + GENE_ID_COL_NAME + "</td>"
                + "<td>" + GENE_NAME_COL_NAME + "</td>"
                + "<td>" + STAGE_ID_COL_NAME + "</td>"
                + "<td>" + STAGE_NAME_COL_NAME + "</td>"
                + "<td>" + ANAT_ENTITY_ID_COL_NAME + "</td>"
                + "<td>" + ANAT_ENTITY_NAME_COL_NAME + "</td>"
                + "<td>" + AFFY_EXPR_STATE_COL_NAME + "</td>"
                + "<td>" + EST_EXPR_STATE_COL_NAME + "</td>"
                + "<td>" + IN_SITU_EXPR_STATE_COL_NAME + "</td>"
                + "<td>" + RNA_SEQ_EXPR_STATE_COL_NAME + "</td>"
                + "<td>" + OBSERVED_DATA_COL_NAME + "</td>"
                + "<td>" + EXPR_STATE_COL_NAME + "</td>"
                + "</tr>"
                + "</tbody>"
                + "</table>");
    }
    /**
     * @return  a {@code String} containing the HTML to create a table containing the description 
     *          of the header of a single species simple over-/under-expression file (can be used 
     *          in "help" links).
     */
    public String getSingleSpeciesSimpleDiffExprFileHeaderDesc() {
        log.entry();
        return log.exit("<table class='download_file_header_desc'>"
                + "<tbody>"
                + "<tr><td>1</td><td>2</td><td>3</td><td>4</td><td>5</td><td>6</td><td>7</td>"
                + "<td>8</td></tr>"
                + "<tr>"
                + "<td>" + GENE_ID_COL_NAME + "</td>"
                + "<td>" + GENE_NAME_COL_NAME + "</td>"
                + "<td>" + STAGE_ID_COL_NAME + "</td>"
                + "<td>" + STAGE_NAME_COL_NAME + "</td>"
                + "<td>" + ANAT_ENTITY_ID_COL_NAME + "</td>"
                + "<td>" + ANAT_ENTITY_NAME_COL_NAME + "</td>"
                + "<td>" + DIFF_EXPR_STATE_COL_NAME + "</td>"
                + "<td>" + DIFF_EXPR_QUAL_COL_NAME + "</td>"
                + "</tr>"
                + "</tbody>"
                + "</table>");
    }
    /**
     * @return  a {@code String} containing the HTML to create a table containing the description 
     *          of the header of a single species complete differential expression file (can be used 
     *          in "help" links).
     */
    public String getSingleSpeciesCompleteDiffExprFileHeaderDesc() {
        log.entry();
        return log.exit("<table class='download_file_header_desc'>"
                + "<tbody>"
                + "<tr><td>1</td><td>2</td><td>3</td><td>4</td><td>5</td><td>6</td><td>7</td>"
                + "<td>8</td><td>9</td><td>10</td><td>11</td><td>12</td><td>13</td><td>14</td>"
                + "<td>15</td><td>16</td><td>17</td><td>18</td></tr>"
                + "<tr>"
                + "<td>" + GENE_ID_COL_NAME + "</td>"
                + "<td>" + GENE_NAME_COL_NAME + "</td>"
                + "<td>" + STAGE_ID_COL_NAME + "</td>"
                + "<td>" + STAGE_NAME_COL_NAME + "</td>"
                + "<td>" + ANAT_ENTITY_ID_COL_NAME + "</td>"
                + "<td>" + ANAT_ENTITY_NAME_COL_NAME + "</td>"
                + "<td>" + DIFF_EXPR_STATE_COL_NAME + "</td>"
                + "<td>" + DIFF_EXPR_QUAL_COL_NAME + "</td>"
                + "<td>" + AFFY_DIFF_EXPR_STATE_COL_NAME + "</td>"
                + "<td>" + AFFY_DIFF_EXPR_QUAL_COL_NAME + "</td>"
                + "<td>" + AFFY_DIFF_EXPR_P_VAL_COL_NAME + "</td>"
                + "<td>" + AFFY_DIFF_EXPR_SUPPORT_COUNT_COL_NAME + "</td>"
                + "<td>" + AFFY_DIFF_EXPR_CONFLICT_COUNT_COL_NAME + "</td>"
                + "<td>" + RNA_SEQ_DIFF_EXPR_STATE_COL_NAME + "</td>"
                + "<td>" + RNA_SEQ_DIFF_EXPR_QUAL_COL_NAME + "</td>"
                + "<td>" + RNA_SEQ_DIFF_EXPR_P_VAL_COL_NAME + "</td>"
                + "<td>" + RNA_SEQ_DIFF_EXPR_SUPPORT_COUNT_COL_NAME + "</td>"
                + "<td>" + RNA_SEQ_DIFF_EXPR_CONFLICT_COUNT_COL_NAME + "</td>"
                + "</tr>"
                + "</tbody>"
                + "</table>");
    }
    /**
     * @return  a {@code String} containing the HTML to create a table containing the description 
     *          of the header of a multi-species simple differential expression file (can be used 
     *          in "help" links).
     */
    public String getMultiSpeciesSimpleDiffExprFileHeaderDesc() {
        log.entry();
        return log.exit("<table class='download_file_header_desc'>"
                + "<tbody>"
                + "<tr><td>1</td><td>2</td><td>3</td><td>4</td><td>5</td><td>6</td><td>7</td>"
                + "<td>8</td><td>9</td><td>10</td><td>11</td><td>12</td><td>13</td><td>...</td></tr>"
                + "<tr>"
                + "<td>" + OMA_ID_COL_NAME + "</td>"
                + "<td>" + MULTI_ANAT_ENTITY_IDS_COL_NAME + "</td>"
                + "<td>" + MULTI_ANAT_ENTITY_NAMES_COL_NAME + "</td>"
                + "<td>" + STAGE_ID_COL_NAME + "</td>"
                + "<td>" + STAGE_NAME_COL_NAME + "</td>"
                + "<td>" + OVER_EXPRESSED_FOR_SPECIES_COL_NAME + " species1</td>"
                + "<td>" + UNDER_EXPRESSED_FOR_SPECIES_COL_NAME + " species1</td>"
                + "<td>" + NOT_DIFF_EXPRESSED_FOR_SPECIES_COL_NAME + " species1</td>"
                + "<td>" + NA_FOR_SPECIES_COL_NAME + " species1</td>"
                + "<td>" + OVER_EXPRESSED_FOR_SPECIES_COL_NAME + " species2</td>"
                + "<td>" + UNDER_EXPRESSED_FOR_SPECIES_COL_NAME + " species2</td>"
                + "<td>" + NOT_DIFF_EXPRESSED_FOR_SPECIES_COL_NAME + " species2</td>"
                + "<td>" + NA_FOR_SPECIES_COL_NAME + " species2</td>"
                + "<td>...</td>"
                + "</tr>"
                + "</tbody>"
                + "</table>");
    }
    /**
     * @return  a {@code String} containing the HTML to create a table containing the description 
     *          of the header of a multi-species complete differential expression file (can be used 
     *          in "help" links).
     */
    public String getMultiSpeciesCompleteDiffExprFileHeaderDesc() {
        log.entry();
        return log.exit("<table class='download_file_header_desc'>"
                + "<tbody>"
                + "<tr><td>1</td><td>2</td><td>3</td><td>4</td><td>5</td><td>6</td><td>7</td>"
                + "<td>8</td><td>9</td><td>10</td><td>11</td><td>12</td><td>13</td><td>14</td>"
                + "<td>15</td><td>16</td><td>17</td><td>18</td><td>19</td><td>20</td>"
                + "<td>21</td><td>22</td></tr>"
                + "<tr>"
                + "<td>" + OMA_ID_COL_NAME + "</td>"
                + "<td>" + MULTI_ANAT_ENTITY_IDS_COL_NAME + "</td>"
                + "<td>" + MULTI_ANAT_ENTITY_NAMES_COL_NAME + "</td>"
                + "<td>" + STAGE_ID_COL_NAME + "</td>"
                + "<td>" + STAGE_NAME_COL_NAME + "</td>"
                + "<td>" + SPECIES_LATIN_NAME_COL_NAME + "</td>"
                + "<td>" + GENE_ID_COL_NAME + "</td>"
                + "<td>" + GENE_NAME_COL_NAME + "</td>"
                + "<td>" + DIFF_EXPR_STATE_COL_NAME + "</td>"
                + "<td>" + DIFF_EXPR_QUAL_COL_NAME + "</td>"
                + "<td>" + AFFY_DIFF_EXPR_STATE_COL_NAME + "</td>"
                + "<td>" + AFFY_DIFF_EXPR_QUAL_COL_NAME + "</td>"
                + "<td>" + AFFY_DIFF_EXPR_P_VAL_COL_NAME + "</td>"
                + "<td>" + AFFY_DIFF_EXPR_SUPPORT_COUNT_COL_NAME + "</td>"
                + "<td>" + AFFY_DIFF_EXPR_CONFLICT_COUNT_COL_NAME + "</td>"
                + "<td>" + RNA_SEQ_DIFF_EXPR_STATE_COL_NAME + "</td>"
                + "<td>" + RNA_SEQ_DIFF_EXPR_QUAL_COL_NAME + "</td>"
                + "<td>" + RNA_SEQ_DIFF_EXPR_P_VAL_COL_NAME + "</td>"
                + "<td>" + RNA_SEQ_DIFF_EXPR_SUPPORT_COUNT_COL_NAME + "</td>"
                + "<td>" + RNA_SEQ_DIFF_EXPR_CONFLICT_COUNT_COL_NAME + "</td>"
                + "<td>" + ANAT_HOMOLOGY_CIO_ID_COL_NAME + "</td>"
                + "<td>" + ANAT_HOMOLOGY_CIO_NAME_COL_NAME + "</td>"
                + "</tr>"
                + "</tbody>"
                + "</table>");
    }

    //*******************************************************
    // MISCELLANEOUS 
    //*******************************************************
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
     * @return  A {@code String} that is a general introduction to the concept 
     *          of presence/absence calls of expression, to be used in various places 
     *          of the documentation, in HTML, and HTML escaped if necessary.
     */
    public String getExprCallExplanation() {
        log.entry();
        return log.exit("<p>Bgee provides calls of presence/absence of expression. A call "
                + "corresponds to a gene, with reported presence or absence of expression, "
                + "in an anatomical entity, during a developmental stage. Only \"normal\" "
                + "expression is considered in Bgee (i.e., no treatment, no disease, "
                + "no gene knock-out, etc.). Bgee collects data from different types, "
                + "from different studies, in different organisms, and provides a summary "
                + "from all these data as unique calls <code>gene - anatomical entity - "
                + "developmental stage</code>, with confidence information, notably taking "
                + "into account potential conflicts.</p>"
                + "<p>Calls of presence/absence of expression are very similar to the data "
                + "that can be reported using <i>in situ</i> hybridization methods; Bgee applies "
                + "dedicated statistical analyses to generate such calls from EST, Affymetrix, "
                + "and RNA-Seq data, with confidence information, and also collects "
                + "<i>in situ</i> hybridization calls from model organism databases. "
                + "This offers the possibility to aggregate and compare these calls of "
                + "presence/absence of expression between different experiments, "
                + "different data types, and different species, and to benefit from both "
                + "the high anatomy coverage provided by low-throughput methods, "
                + "and the high genomic coverage provided by high-throughput methods.</p>");
    }
    /**
     * @return  A {@code String} that is a general introduction to the concept 
     *          of over-/under-expression calls, to be used in various places 
     *          of the documentation, in HTML, and HTML escaped if necessary.
     */
    public String getDiffExprCallExplanation() {
        log.entry();
        return log.exit("<p>Bgee provides calls of over-/under-expression. A call "
                + "corresponds to a gene, with significant variation of "
                + "its level of expression, in an anatomical entity "
                + "during a developmental stage, as compared to, either: i) other anatomical entities "
                + "at the same (broad) developmental stage (over-/under-expression across anatomy); "
                + "ii) the same anatomical entity at different (precise) developmental stages "
                + "(over-/under-expression across life stages). "
                + "These analyses of differential expression are performed using Affymetrix "
                + "and RNA-Seq experiments with at least 3 suitable conditions (anatomical entity/"
                + "developmental stage), and at least 2 replicates for each; as for all data in Bgee, "
                + "only \"normal\" expression is considered (i.e., no treatment, no disease, "
                + "no gene knock-out, etc.). </p>"
                + "<p>Bgee runs all possible differential expression analyses for each experiment "
                + "independently, then collects all results and provides a summary "
                + "as unique calls <code>gene - anatomical entity - developmental stage</code>, "
                + "with confidence information, and conflicts within each data type resolved "
                + "using a voting system weighted by p-values (conflicts between different "
                + "data types are treated differently). This offers the possibility "
                + "to aggregate and compare these calls between different experiments, "
                + "different data types, and different species. </p>");
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
