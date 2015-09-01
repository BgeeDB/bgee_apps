package org.bgee.view.html;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeWebappProperties;
import org.bgee.controller.RequestParameters;

/**
 * Parent class of classes responsible for generating the download file documentation. 
 * These classes are meant to be used by the class {@link HtmlDocumentationDisplay}, and 
 * encapsulate specific parts of the documentation.
 * 
 * @author  Frederic Bastian
 * @see HtmlDocumentationDisplay
 * @see HtmlDocumentationCallFile
 * @see HtmlDocumentationRefExprFile
 * @version Bgee 13 June 2015
 * @since   Bgee 13
 */
public abstract class HtmlDocumentationDownloadFile extends HtmlParentDisplay {
    private static final Logger log = LogManager.getLogger(HtmlDocumentationDownloadFile.class.getName());
    
    /**
     * A {@code String} that is the name of the gene ID column in download files, 
     * HTML escaped if necessary.
     * @see #GENE_ID_LINK_TITLE
     */
    protected static final String GENE_ID_COL_NAME ="Gene ID";
    /**
     * A {@code String} to be used in {@code title} attribute of {@code a} tag linking to 
     * gene ID column description (used several times), HTML escaped if necessary.
     * @see #GENE_ID_COL_NAME
     */
    protected static final String GENE_ID_LINK_TITLE = "See " + GENE_ID_COL_NAME + " column description";
    /**
     * A {@code String} that is the name of the stage ID column in download files, 
     * HTML escaped if necessary.
     * @see #STAGE_ID_LINK_TITLE
     */
    protected static final String STAGE_ID_COL_NAME ="Developmental stage ID";
    /**
     * A {@code String} to be used in {@code title} attribute of {@code a} tag linking to 
     * stage ID column description (used several times), HTML escaped if necessary.
     * @see #STAGE_ID_COL_NAME
     */
    protected static final String STAGE_ID_LINK_TITLE = "See " + STAGE_ID_COL_NAME + " column description";
    /**
     * A {@code String} that is the name of the stage name column in download files, 
     * HTML escaped if necessary.
     * @see #STAGE_NAME_LINK_TITLE
     */
    protected static final String STAGE_NAME_COL_NAME ="Developmental stage name";
    /**
     * A {@code String} to be used in {@code title} attribute of {@code a} tag linking to 
     * stage name column description (used several times), HTML escaped if necessary.
     * @see #STAGE_NAME_COL_NAME
     */
    protected static final String STAGE_NAME_LINK_TITLE = "See " + STAGE_NAME_COL_NAME 
            + " column description";
    /**
     * A {@code String} that is the name of the anatomical entity ID column in download files, 
     * HTML escaped if necessary.
     * @see #ANAT_ENTITY_ID_LINK_TITLE
     */
    protected static final String ANAT_ENTITY_ID_COL_NAME ="Anatomical entity ID";
    /**
     * A {@code String} to be used in {@code title} attribute of {@code a} tag linking to 
     * anatomical entity ID column description (used several times), HTML escaped if necessary.
     * @see #ANAT_ENTITY_ID_COL_NAME
     */
    protected static final String ANAT_ENTITY_ID_LINK_TITLE = "See " + ANAT_ENTITY_ID_COL_NAME 
            + " column description";
    /**
     * A {@code String} that is the name of the anatomical entity name column in download files, 
     * HTML escaped if necessary.
     * @see #ANAT_ENTITY_NAME_LINK_TITLE
     */
    protected static final String ANAT_ENTITY_NAME_COL_NAME ="Anatomical entity name";
    /**
     * A {@code String} to be used in {@code title} attribute of {@code a} tag linking to 
     * anatomical entity name column description (used several times), HTML escaped if necessary.
     * @see #ANAT_ENTITY_NAME_COL_NAME
     */
    protected static final String ANAT_ENTITY_NAME_LINK_TITLE = "See " + ANAT_ENTITY_NAME_COL_NAME 
            + " column description";

    /**
     * @return  A {@code String} that is the description of the gene ID column 
     *          in download files (because we use it several times), formated in HTML 
     *          and HTML escaped if necessary.
     * @see #GENE_ID_COL_NAME
     */
    protected static String getGeneIdColDescription() {
        log.entry();
        return log.exit("<p>Unique identifier of gene from Ensembl. </p><p>" 
                + HtmlDocumentationDisplay.getGenomeMappingExplanation() + "</p>");
    }
    /**
     * @return  A {@code String} that is the description of the stage ID column 
     *          in download files (because we use it several times), formated in HTML 
     *          and HTML escaped if necessary.
     * @see #STAGE_ID_COL_NAME
     */
    protected static String getStageIdColDescription() {
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
    protected static String getStageNameColDescription(int colNumber) {
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
    protected static String getAnatEntityIdColDescription() {
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
    protected static String getAnatEntityNameColDescription(int colNumber) {
        log.entry();
        return log.exit("<p>Name of the anatomical entity defined by <code>" 
        + ANAT_ENTITY_ID_COL_NAME + "</code> (column " + colNumber + ")</p>");
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
    protected HtmlDocumentationDownloadFile(HttpServletResponse response,
            RequestParameters requestParameters, BgeeWebappProperties prop, HtmlFactory factory)
            throws IllegalArgumentException, IOException {
        super(response, requestParameters, prop, factory);
    }
}
