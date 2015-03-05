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
    public void displayDocumentationPage() {
        log.entry();
        
        String subsectionName1 = "Subsection 1 - H2 section";
        String subsectionName2 = "Subsection 2 - H2 section";
        String subsectionName3 = "Subsection 3 - H2 section";
        
        this.startDisplay("download", "Bgee release 13 documentation page");
        
        this.writeln("<h1 id='sectionname'>Section name</h1>");
        this.writeln("<div class='documentationmenu'>");
        this.writeln("<ul>");
        this.writeln("<li><a href='#subsection1' title='Quick jump to this section'>" + 
                            subsectionName1 + "</a></li>");
        this.writeln("<li><a href='#subsection2' title='Quick jump to this section'>" + 
                            subsectionName2 + "</a></li>");
        this.writeln("<li><a href='#subsection2' title='Quick jump to this section'>" + 
                            subsectionName3 + "</a></li>");
        this.writeln("</ul>");
        this.writeln("</div>"); // end of documentationmenu

        this.writeln("<div id='subsection1'>");
        this.writeln("<h2>" + subsectionName1 + "</h2><a class='backlink' href='#sectionname'>Back</a>");
        this.writeln("<div class='documentationsubsection'>");
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

        this.writeln("<div id='subsection2'>");
        this.writeln("<h2>" + subsectionName2 + "</h2><a class='backlink' href='#sectionname'>Back</a>");
        this.writeln("<div class='documentationsubsection'>");
        this.writeln("<p> </p>");
        this.writeln("<p> </p>");
        this.writeln("</div>");
        this.writeln("</div>"); // end of subsection2

        this.writeln("<div id='subsection3'>");
        this.writeln("<h2>" + subsectionName3 + "</h2><a class='backlink' href='#sectionname'>Back</a>");
        this.writeln("<div class='documentationsubsection'>");
        this.writeln("<p> </p>");
        this.writeln("<p> </p>");
        this.writeln("</div>");
        this.writeln("</div>"); // end of subsection3

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
