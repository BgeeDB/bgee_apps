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

    private final static Logger log = LogManager.getLogger(HtmlDocumentationDisplay.class.getName());

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
                "Over-/Under-expression across anatomy or life stages</a>");
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
                "Over-/Under-expression across anatomy or life stages</a>");
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
                "Over-/Under-expression across anatomy or life stages</a>");
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
                "Over-/Under-expression across anatomy or life stages</a>");
        this.writeln("<ul>");
        this.writeln("<li><a href='#multi_diff_simple' title='Quick jump to this section'>" + 
                        "Simple file</a></li>");
        this.writeln("<li><a href='#multi_diff_complete' title='Quick jump to this section'>" + 
                        "Complete file</a></li>");
        this.writeln("</ul></li>");         //end of diff expression        
        this.writeln("</ul></li>");     // end of multi-species section
        
        this.writeln("</ul></div>");// end of documentationmenu
        
        //define names of columns common to several files
        String geneIdCol ="Gene ID";
        String geneNameCol ="Gene name";
        String stageIdCol ="Developmental stage ID";
        String stageNameCol ="Developmental stage name";
        String anatEntityIdCol ="Anatomical entity ID";
        String anatEntityNameCol ="Anatomical entity name";
        String expressionState = "Expression state";
        
        //Single species documentation
        this.writeln("<div>");
        this.writeln("<h2 id='single'>Single-species download files</h2>");
        this.writeln("<h3 id='single_expr'>Presence/absence of expression</h3>");
        this.writeln("<h4 id='single_expr_simple'>Simple file</h4>");
        this.writeln("<table class='call_download_file'>");
        this.writeln("<caption>Header and example line for single species simple expression file</caption>");
        this.writeln("<tbody>");
        this.writeln("<tr class='download_file_header_desc'>"
                + "<td>" + geneIdCol + "</td>"
                + "<td>" + geneNameCol + "</td>"
                + "<td>" + stageIdCol + "</td>"
                + "<td>" + stageNameCol + "</td>"
                + "<td>" + anatEntityIdCol + "</td>"
                + "<td>" + anatEntityNameCol + "</td>"
                //TODO: split into two columns once we regenerate the download files
                + "<td>" + expressionState + "</td>"
                + "</tr>");
        this.writeln("<tr class='download_file_example'>"
                + "<td>ENSG00000001631</td>"
                + "<td>KRIT1</td>"
                + "<td>HsapDv:0000092</td>"
                + "<td>human middle aged stage (human)</td>"
                + "<td>UBERON:0004720 </td>"
                + "<td>cerebellar vermis</td>"
                + "<td>expression high quality</td>"
                + "</tr>");
        this.writeln("</tbody>");
        this.writeln("</table>");
        
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
        this.writeln("</div>"); // end of subsection1
        
        this.writeln(this.getBackToTheTopLink());
        
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
