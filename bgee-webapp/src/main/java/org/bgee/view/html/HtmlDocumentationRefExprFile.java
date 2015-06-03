package org.bgee.view.html;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;

/**
 * This class encapsulates the documentation for reference expression download files. 
 * It does not implement any interface from the {@code view} package, it is meant to be used 
 * by the class {@link HtmlDocumentationDisplay}. The only reason is that the class 
 * {@code HtmlDocumentationDisplay} was getting too large and too complicated. 
 * 
 * @author  Frederic Bastian
 * @see HtmlDocumentationDisplay
 * @version Bgee 13 June 2015
 * @since   Bgee 13
 */
public class HtmlDocumentationRefExprFile extends HtmlDocumentationDownloadFile {
    private static final Logger log = LogManager.getLogger(HtmlDocumentationRefExprFile.class.getName());

    /**
     * Default constructor. 
     * 
     * @param response          A {@code HttpServletResponse} that will be used to display the 
     *                          page to the client
     * @param requestParameters The {@code RequestParameters} that handles the parameters of the 
     *                          current request.
     * @param prop              A {@code BgeeProperties} instance that contains the properties
     *                          to use.
     * @param factory           The {@code HtmlFactory} that instantiated the {@link HtmlDocumentation} 
     *                          object using this object.
     * @throws IllegalArgumentException If {@code factory} is {@code null}.
     * @throws IOException              If there is an issue when trying to get or to use the
     *                                  {@code PrintWriter} 
     */
    protected HtmlDocumentationRefExprFile(HttpServletResponse response,
            RequestParameters requestParameters, BgeeProperties prop, HtmlFactory factory)
            throws IllegalArgumentException, IOException {
        super(response, requestParameters, prop, factory);
    }
    
    /**
     * @return  A {@code String} that is the documentation menu for Affymetrix data, 
     *          formatted in HTML, with {@code ul} element including {@code li} elements.
     */
    private static String getAffyDocMenu() {
        log.entry();
        
        return log.exit("");
    }

    /**
     * Write the documentation menu related to reference expression  
     * download files. Anchors used in this method for quick jump links 
     * have to stayed in sync with id attributes of h2, h3 and h4 tags defined in 
     * {@link #writeAffyRefExprFileDoc()},  and
     * {@link #writeRnaSeqRefExprFileDoc()}.
     * 
     * @see #writeAffyRefExprFileDoc()
     * @see #writeRnaSeqRefExprFileDoc()
     */
    private void writeDocMenuForRefExprDownloadFiles() {
        log.entry();
        
        this.writeln("<div class='documentationmenu'><ul>");
        //Affymetrix
        this.writeln("<li><a href='#affy' title='Quick jump to this section'>" + 
                "Affymetrix data download files</a>");
        this.writeln("<ul>");
        this.writeln("<li><a href='#affy_exp' title='Quick jump to this section'>" + 
                "Affymetrix experiments</a></li>");   
        this.writeln("<li><a href='#affy_chip' title='Quick jump to this section'>" + 
                "Affymetrix chips</a></li>");
        this.writeln("<li><a href='#affy_probeset' title='Quick jump to this section'>" + 
                "Affymetrix probesets</a></li>");
        this.writeln("</ul></li>"); // end of Affymetrix

        //RNA-Seq
        this.writeln("<li><a href='#rna-seq' title='Quick jump to this section'>" + 
                "RNA-Seq data download files</a>");
        this.writeln("<ul>");
        this.writeln("<li><a href='#rna-seq_exp' title='Quick jump to this section'>" + 
                "RNA-Seq experiments</a></li>");   
        this.writeln("<li><a href='#rna-seq_lib' title='Quick jump to this section'>" + 
                "RNA-Seq libraries</a></li>");
        this.writeln("<li><a href='#rna-seq_gene' title='Quick jump to this section'>" + 
                "RNA-Seq read counts and RPKM values</a></li>");
        this.writeln("</ul></li>"); // end of RNA-Seq

        this.writeln("<li><a href='#troubleshooting' title='Quick jump to this section'>" + 
                "Troubleshooting</a>");
        this.writeln("</ul></div>");// end of documentationmenu
        
        log.exit();
    }
    
    /**
     * Write the documentation related to Affymetrix reference expression download files. 
     * Anchors used in this method for quick jump links 
     * have to stayed in sync with id attributes of h4 tags defined in 
     * {@link #writeAffyExpFileDoc()}, {@link #writeAffyChipFileDoc()}, and 
     * {@link #writeAffyProbesetFileDoc()}.
     * 
     * @see #writeAffyExpFileDoc()
     * @see #writeAffyChipFileDoc()
     * @see #writeAffyProbesetFileDoc()
     */
    private void writeAffyRefExprFileDoc() {
        log.entry();
        
        //Affymetrix
        this.writeln("<h3 id='single_expr'>Affymetrix processed and annotated data</h3>");
        //TODO: add link to data analyses documentation
        this.writeln("<p>Affymetrix data used in Bgee are retrieved from <a target='_blank' "
                + "class='external_link' title='External link to ArrayExpress' "
                + "href='http://www.ebi.ac.uk/arrayexpress/'>ArrayExpress</a> and "
                + "<a target='_blank' class='external_link' title='External link to GEO' "
                + "href='http://www.ncbi.nlm.nih.gov/geo/'>GEO</a>. They are annotated "
                + "to anatomical and developmental stage ontologies, filtered by quality controls "
                + "and analyzed to produce expression data. Only \"normal\" "
                + "expression data are integrated in Bgee (i.e., no treatment, no disease, "
                + "no gene knock-out, etc.). Here are described the format "
                + "of the files providing processed, annotated Affymetrix data.</p>");
        this.writeln("<p>Jump to format description for: </p>"
                + "<ul>"
                + "<li><a href='#single_expr_simple' title='Quick jump to simple file description'>"
                + "simple file</a></li>"
                + "<li><a href='#single_expr_complete' title='Quick jump to complete file description'>"
                + "complete file</a></li>"
                + "</ul>");
        
        //simple expression file
        //this.writeSingleSpeciesSimpleExprCallFileDoc();
        
        //complete expression file
        //this.writeSingleSpeciesCompleteExprCallFileDoc(); //end of presence/absence of expression
        
        
        log.exit();
    }
}
