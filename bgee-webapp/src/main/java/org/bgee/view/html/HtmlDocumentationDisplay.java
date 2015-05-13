package org.bgee.view.html;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.view.DocumentationDisplay;


//XXX: what is this {@code displayType}?
/**
 * This class displays the documentation for the {@code displayType} HTML.
 *
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 13 May 2015
 * @since   Bgee 13
 */
public class HtmlDocumentationDisplay extends HtmlParentDisplay implements DocumentationDisplay {

    private final static Logger log = LogManager.getLogger(HtmlDocumentationDisplay.class.getName());

    //*******************************************************
    // MISCELLANEOUS STATIC METHODS
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
    public static String getGenomeMappingExplanation() {
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
    public static String getExprCallExplanation() {
        log.entry();
        return log.exit("<p>Bgee provides calls of presence/absence of expression. Each call "
                + "corresponds to a unique combination of a gene, an anatomical entity, "
                + "and a life stage, with reported presence or absence of expression. "
                + "Life stages describe development and aging. Only \"normal\" "
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
    public static String getDiffExprCallExplanation() {
        log.entry();
        return log.exit("<p>Bgee provides calls of over-/under-expression. A call "
                + "corresponds to a gene, with significant variation of "
                + "its level of expression, in an anatomical entity "
                + "during a developmental stage, as compared to, either: i) other anatomical entities "
                + "at the same (broadly defined) developmental stage (over-/under-expression "
                + "across anatomy); "
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

    
    
    //************************************
    // Instance attributes and methods
    //************************************
    
    /**
     * A {@code HtmlDocumentationCallFile} used to write the documentation 
     * about call download files (see {@link #displayCallDownloadFileDocumentation()}).
     */
    private final HtmlDocumentationCallFile callFileDoc;

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
        this(response, requestParameters, prop, null);
    }
    /**
     * Constructor providing other dependencies.
     *
     * @param response          A {@code HttpServletResponse} that will be used to display the 
     *                          page to the client.
     * @param requestParameters A {@code RequestParameters} handling the parameters of the 
     *                          current request, to determine the requested displayType, 
     *                          and for display purposes.
     * @param prop              A {@code BgeeProperties} instance that contains the properties
     *                          to use.
     * @param callFileDoc       A {@code HtmlDocumentationCallFile} used to write the documentation 
     *                          about call download files (see {@link 
     *                          #displayCallDownloadFileDocumentation()}). If {@code null}, 
     *                          the default implementation will be used 
     *                          ({@link HtmlDocumentationCallFile}).
     * @throws IOException      If there is an issue when trying to get or to use the
     *                          {@code PrintWriter}.
     */
    public HtmlDocumentationDisplay(HttpServletResponse response,
            RequestParameters requestParameters, BgeeProperties prop, 
            HtmlDocumentationCallFile callFileDoc) throws IOException {
        super(response, requestParameters, prop);
        if (callFileDoc == null) {
            this.callFileDoc = new HtmlDocumentationCallFile(response, requestParameters, prop);
        } else {
            this.callFileDoc = callFileDoc;
        }
    }
    
    @Override
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
        
//        this.writeln(this.getBackToTheTopLink());
        
        this.writeln("<div id='subsection2'>");
        this.writeln("<h2>" + subsectionName2 + "</h2>");
        this.writeln("<div class='documentationsection'>");
        this.writeln("<p> </p>");
        this.writeln("<p> </p>");
        this.writeln("</div>");
        this.writeln("</div>"); // end of subsection2
        
//        this.writeln(this.getBackToTheTopLink());
        
        this.writeln("<div id='subsection3'>");
        this.writeln("<h2>" + subsectionName3 + "</h2>");
        this.writeln("<div class='documentationsection'>");
        this.writeln("<p> </p>");
        this.writeln("<p> </p>");
        this.writeln("</div>");
        this.writeln("</div>"); // end of subsection3
        
//        this.writeln(this.getBackToTheTopLink());
        
        this.endDisplay();

        log.exit();
    }

    //*******************************************************
    // DOCUMENTATION FOR CALL DOWNLOAD FILES 
    //*******************************************************
    @Override
    public void displayCallDownloadFileDocumentation() {
        log.entry();
        
        this.startDisplay("download", "Download file documentation");
        
        this.callFileDoc.writeDocumentation();
        
        this.endDisplay();

        log.exit();
    }
    

    //*******************************************************
    // COMMON METHODS
    //*******************************************************
//    /**
//     * @return  the {@code String} that is the link of the back to the top.
//     */
//    private String getBackToTheTopLink() {
//        log.entry();
//        return log.exit("<a class='backlink' href='#sectionname'>Back to the top</a>");
//    }

    @Override
    protected void includeCss() {
        log.entry();
        super.includeCss();
        this.includeCss("documentation.css");
        log.exit();
    }
}
