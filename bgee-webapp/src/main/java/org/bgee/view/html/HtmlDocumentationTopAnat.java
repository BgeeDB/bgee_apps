package org.bgee.view.html;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;

/**
 * This class encapsulates the documentation for TopAnat. 
 * It does not implement any interface from the {@code view} package, it is meant to be used 
 * by the class {@link HtmlDocumentationDisplay}. The only reason is that the class 
 * {@code HtmlDocumentationDisplay} was getting too large and too complicated. 
 * 
 * @author  Valentine Rech de Laval
 * @see 	HtmlDocumentationDisplay
 * @version Bgee 13, Feb. 2016
 * @since   Bgee 13, Feb. 2016
 */
public class HtmlDocumentationTopAnat extends HtmlDocumentationDownloadFile {

	private static final Logger log = LogManager.getLogger(HtmlDocumentationTopAnat.class.getName());

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
    protected HtmlDocumentationTopAnat(HttpServletResponse response,
            RequestParameters requestParameters, BgeeProperties prop, HtmlFactory factory)
            throws IllegalArgumentException, IOException {
        super(response, requestParameters, prop, factory);
    }
    
    /**
     * Write the documentation for TopAnat. This method is not responsible 
     * for calling the methods {@code startDisplay} and {@code endDisplay}. Otherwise, 
     * all other elements of the page are written by this method.
     * 
     * @see HtmlDocumentationDisplay#displayTopAnatDocumentation()
     */
    protected void writeDocumentation() {
        log.entry();
        
        this.writeln("<h1>TopAnat documentation</h1>");

        this.writeln("<div class='row'>");
        
        // Introduction
        this.writeln("<div class='" + LARGE_ELEMENT_HTML_CLASS + "'>");
        this.writeln("<p>TopAnat is a tool to identify and visualize enriched anatomical terms, "
        		+ "from the expression patterns of a list of genes.</p>");
        this.writeln("<p>It allows to discover where genes from a set are preferentially expressed, "
        		+ "as compared to a background, represented by default by all expression data in Bgee "
        		+ "for the requested species. It is is similar to a Gene Ontology enrichment test, "
        		+ "except that it analyzes the anatomical structures where genes are expressed, "
        		+ "rather than their GO functional annotations.</p>");
        this.writeln("<p>See also our "
        		+ "<a href='https://bgeedb.wordpress.com/2015/11/24/topanat-go-like-enrichment-of-anatomical-terms-mapped-to-genes-by-expression-patterns/' "
        		+ "title='Link to the Bgee Wordpress blog' target='_blank'>blog post</a> "
        		+ "about TopAnat for more information.</p>");
        this.writeln("<p><strong>Please note that the results can be slow to compute</strong>, "
        		+ "typically from 1 to 30 minutes, depending on the amount of data to process.</p>");
        this.writeln("</div>");

        // Quick start
        this.writeln("<div class='" + LARGE_ELEMENT_HTML_CLASS + "'>");
        this.writeln("<h2>Quick start</h2>");
        this.writeln("</div>");
        
        // How to use
        this.writeln("<div class='" + LARGE_ELEMENT_HTML_CLASS + "'>");
        this.writeln("<h3>How to use</h3>");
        this.writeln("<ul class='help'>");
        this.writeln("<li>Enter a list of Ensembl identifiers into the first form field,</li>");
        this.writeln("<li>Optionally, enter a list of background genes,</li>");
        this.writeln("<li>Optionally, change the program parameters with the dropdown menu.</li>");
        this.writeln("<li>Click the 'Submit your job' button.</li>");
        this.writeln("</ul>");
        this.writeln("</div>");
        
        // Examples
        this.writeln("<div class='" + LARGE_ELEMENT_HTML_CLASS + "'>");
        this.writeln("<h3>Examples</h3>");
        this.writeln("<ul>");
        // Do not add a trailing slash to the example URLs, see comments in topanat.js
        this.writeln("<li><a href='?page=top_anat#/result/7919f27d143667bc6c137401ce0c91b51e257538' "
        		+ "title='TopAnat example'>Mouse genes mapped to the GO term 'spermatogenesis'</a>.</li>");
        this.writeln("<li><a href='?page=top_anat#/result/e37009ba698919c75d06e81b3eca5d48f78210a0' "
        		+ "title='TopAnat example'>Mouse genes mapped to the GO term 'neurological system process', "
        		+ "with decorrelation and high quality data only</a>.</li>");
        this.writeln("<li><a href='?page=top_anat#/result/7e8c74c073be03be4c40810c16c6be06c0bef1be' "
        		+ "title='TopAnat example'>Cow genes with the keyword 'muscle' "
        		+ "in their UniProtKB/Swiss-Prot description</a>.</li>");
        this.writeln("<li><a href='?page=top_anat#/result/5fc8ff1fcfed7cfba0f82f82a67b418ce8a709b6' "
        		+ "title='TopAnat example'>Platypus genes located on X chromosome</a>.</li>");
        this.writeln("</ul>");
        this.writeln("</div>");
        
        // Note of caution
        this.writeln("<div class='" + LARGE_ELEMENT_HTML_CLASS + "'>");
        this.writeln("<h2>Note of caution</h2>");
        this.writeln("</div>");
        this.writeln("<div class='" + LARGE_ELEMENT_HTML_CLASS + "'>");
        this.writeln("In your analyses, you should be extremely careful "
        		+ "about the definition of your universe (i.e., your background genes). "
        		+ "The cases where it is correct to use the default background (i.e., all genes "
        		+ "with data in Bgee for the selected species) should be actually rare. "
        		+ "For instance, if you are studying a list of genes assigned to a specific "
        		+ "GO category, then your universe should be 'all genes with a GO annotation', "
        		+ "and not 'all genes with data in Bgee'. Of course, it is still useful "
        		+ "to use the default background, for preliminary analyses, or when "
        		+ "the biological signal extracted from your gene list is very strong. "
        		+ "But it should often be more rigorously defined for data used in publications.");
        this.writeln("</div>");

        // More information
        this.writeln("<div class='" + LARGE_ELEMENT_HTML_CLASS + "'>");
        this.writeln("<h2>More information</h2>");
        this.writeln("</div>");
        this.writeln("<div class='" + LARGE_ELEMENT_HTML_CLASS + "'>");
        this.writeln("<p>TopAnat is based on "
        		+ "<a href='http://www.bioconductor.org/packages/release/bioc/html/topGO.html' "
        		+ "title='TopGO package in Bioconductor' target='_blank'>topGO</a>. "
        		+ "Adaptation of topGO courtesy of Adrian Alexa.</p>");
        this.writeln("</div>");
        
        this.writeln("</div>"); // close row

        log.exit();
    }
}
