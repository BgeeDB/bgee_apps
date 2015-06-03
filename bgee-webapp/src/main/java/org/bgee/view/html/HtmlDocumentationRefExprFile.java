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
     * @return  A {@code String} that is the documentation menu for Affymetrix data, 
     *          formatted in HTML, with {@code ul} element including {@code li} elements.
     */
    private static String getAffyDocMenu() {
        log.entry();
        return log.exit("<ul>"
                + "<li><a href='#affy_exp' title='Quick jump to this section'>" + 
                "Experiments</a></li>"
                + "<li><a href='#affy_chip' title='Quick jump to this section'>" + 
                "Chips</a></li>"
                + "<li><a href='#affy_probeset' title='Quick jump to this section'>" + 
                "Probesets</a></li>"
                + "</ul>");
    }
    /**
     * @return  A {@code String} that is the documentation menu for RNA-Seq data, 
     *          formatted in HTML, with {@code ul} element including {@code li} elements.
     */
    private static String getRnaSeqDocMenu() {
        log.entry();
        return log.exit("<ul>"
                + "<li><a href='#rna-seq_exp' title='Quick jump to this section'>" + 
                "Experiments</a></li>"
                + "<li><a href='#rna-seq_lib' title='Quick jump to this section'>" + 
                "Libraries</a></li>"
                + "<li><a href='#rna-seq_gene' title='Quick jump to this section'>" + 
                "RNA-Seq read counts and RPKM values</a></li>"
                + "</ul>");
    }


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
     * Write the documentation for the call download files. This method is not responsible 
     * for calling the methods {@code startDisplay} and {@code endDisplay}. Otherwise, 
     * all other elements of the page are written by this method.
     * 
     * @see HtmlDocumentationDisplay#displayCallDownloadFileDocumentation()
     */
    protected void writeDocumentation() {
        log.entry();
        
        this.writeln("<h1 id='sectionname'>Download file documentation</h1>");
        RequestParameters urlDownloadGenerator = this.getNewRequestParameters();
        urlDownloadGenerator.setPage(RequestParameters.PAGE_DOWNLOAD);
        this.writeln("<p class='documentationintro'>Bgee provides calls of baseline "
                + "presence/absence of expression, and of differential over-/under-expression, "
                + "either for single species, or compared between species (orthologous genes "
                + "in homologous organs). This documentation describes the format of these "
                + "<a href='" + urlDownloadGenerator.getRequestURL()
                + "' title='Bgee expression data page'>download files</a>.</p>");
        
        //Documentation menu
        this.writeDocMenuForRefExprDownloadFiles();
        
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
//        this.writeSingleSpeciesExprCallFileDoc();
        //over/under
//        this.writeSingleSpeciesDiffExprCallFileDoc();
        this.writeln("</div>"); // end of single-species
        
        this.writeln("</div>");
        
        //multi-species documentation
        this.writeln("<div>");
        this.writeln("<h2 id='multi'>Multi-species download files</h2>");
        this.writeln("<div class='doc_content'>");
        this.writeln("<p>Bgee provides the ability to compare expression data between species, "
                + "with great anatomical detail, using formal concepts of homology: "
                + "orthology of genes, homology of anatomical entities. This allows to perform "
                + "accurate comparisons between species, even for distant species "
                + "for which the anatomy mapping might not be obvious.</p>");
        this.writeln("<ul class='doc_content'>"
                + "<li><span class='list_element_title'>homology of anatomical entities</span>: "
                + "When comparing multiple species, only anatomical entities homologous "
                + "between all species compared are considered, meaning, only anatomical entities "
                + "derived from an organ existing before the divergence of the species compared. "
                + "This requires careful annotations of the homology history of animal anatomy. "
                + "These annotations are described in a separate project maintained "
                + "by the Bgee team, see <a target='_blank' class='external_link' "
                + "href='https://github.com/BgeeDB/anatomical-similarity-annotations/' "
                + "title='See anatomical-similarity-annotations project on GitHub'>"
                + "homology annotation project on GitHub</a>. <br />"
                + "In practice, when comparing expression data between several species, "
                + "the anatomical entities used are those with a homology relation valid "
                + "for their Least Common Ancestor (LCA), or any of its ancestral taxa. "
                + "For instance, if comparing data between human and zebrafish, "
                + "the LCA would be the taxon <i>Euteleostomi</i>; as a result, "
                + "annotations to this taxon would be used, such as the relation of homology "
                + "between \"tetrapod parietal bone\" (UBERON:0000210) and "
                + "\"actinopterygian frontal bone\" (UBERON:0004866); but also, annotations "
                + "to ancestral taxa, such as the annotation stating that \"ophthalmic nerve\" "
                + "appeared in the <i>Vertebrata</i> common ancestor; annotations to more recent taxa "
                + "than the LCA would be discarded, such as the annotation to the \"forelimb\" "
                + "structure (UBERON:0002102), homologous in the <i>Tetrapoda</i> lineage.</li> "
                + "<li><span class='list_element_title'>orthology of genes</span>: relations of "
                + "orthology between genes are retrieved using the <a target='_blank' class='external_link' "
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
//        this.writeMultiSpeciesDiffExprCallFileDoc();
        this.writeln("</div>");
        this.writeln("</div>"); // end of multi-species download file

        log.exit();
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
        this.writeln(getAffyDocMenu());
        this.writeln("</li>"); // end of Affymetrix

        //RNA-Seq
        this.writeln("<li><a href='#rna-seq' title='Quick jump to this section'>" + 
                "RNA-Seq data download files</a>");
        this.writeln(getRnaSeqDocMenu());
        this.writeln("</li>"); // end of RNA-Seq

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
                + getAffyDocMenu());
        
        //simple expression file
        //this.writeSingleSpeciesSimpleExprCallFileDoc();
        
        //complete expression file
        //this.writeSingleSpeciesCompleteExprCallFileDoc(); //end of presence/absence of expression
        
        
        log.exit();
    }
}
