package org.bgee.view.html;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;

/**
 * This class encapsulates the documentation for call download files. It does not implement 
 * any interface from the {@code view} package, it is meant to be used by the class 
 * {@link HtmlDocumentationDisplay}. The only reason is that the class 
 * {@code HtmlDocumentationDisplay} was getting too large and too complicated. 
 * 
 * @author  Frederic Bastian
 * @see HtmlDocumentationDisplay
 * @version Bgee 13 May 2015
 * @since   Bgee 13
 */
public class HtmlDocumentationCallFile extends HtmlDocumentationDownloadFile {
    private static final Logger log = LogManager.getLogger(HtmlDocumentationCallFile.class.getName());
    
    /**
     * A {@code String} that is the name of the gene name column in download files, 
     * HTML escaped if necessary.
     * @see #GENE_NAME_LINK_TITLE
     */
    private static final String GENE_NAME_COL_NAME ="Gene name";
    /**
     * A {@code String} to be used in {@code title} attribute of {@code a} tag linking to 
     * gene name column description (used several times), HTML escaped if necessary.
     * @see #GENE_NAME_COL_NAME
     */
    private static final String GENE_NAME_LINK_TITLE = "See " + GENE_NAME_COL_NAME + " column description";
    /**
     * A {@code String} that is the name of the gene names column in multi-species download files, 
     * HTML escaped if necessary.
     */
    private static final String GENE_NAMES_COL_NAME ="Gene names";
    /**
     * A {@code String} that is the name of the gene IDs column in multi-species download files, 
     * HTML escaped if necessary.
     */
    private static final String GENE_IDS_COL_NAME ="Gene IDs";
    /**
     * A {@code String} that is the name of the expression state column in download files, 
     * HTML escaped if necessary.
     * @see #EXPR_STATE_LINK_TITLE
     */
    private static final String EXPR_STATE_COL_NAME = "Expression";
    /**
     * A {@code String} to be used in {@code title} attribute of {@code a} tag linking to 
     * expression state column description (used several times), HTML escaped if necessary.
     * @see #EXPR_STATE_COL_NAME
     */
    private static final String EXPR_STATE_LINK_TITLE = "See " + EXPR_STATE_COL_NAME 
            + " column description";
    /**
     * A {@code String} that is the name of the expression quality column in download files, 
     * HTML escaped if necessary.
     * @see #EXPR_QUAL_LINK_TITLE
     */
    //TODO: split into two columns once we regenerate the download files
    private static final String EXPR_QUAL_COL_NAME = "Call quality";
    /**
     * A {@code String} to be used in {@code title} attribute of {@code a} tag linking to 
     * expression quality column description (used several times), HTML escaped if necessary.
     * @see #EXPR_QUAL_COL_NAME
     */
    private static final String EXPR_QUAL_LINK_TITLE = "See " + EXPR_STATE_COL_NAME 
            + " column description";
    
    /**
     * A {@code String} that is the name of the expression state column for affymetrix data 
     * in download files, HTML escaped if necessary.
     * @see #AFFY_EXPR_STATE_LINK_TITLE
     */
    private static final String AFFY_EXPR_STATE_COL_NAME = "Affymetrix data";
    /**
     * A {@code String} to be used in {@code title} attribute of {@code a} tag linking to 
     * affymetrix expression state column description (used several times), HTML escaped if necessary.
     * @see #AFFY_EXPR_STATE_COL_NAME
     */
    private static final String AFFY_EXPR_STATE_LINK_TITLE = "See " + AFFY_EXPR_STATE_COL_NAME 
            + " column description";
    /**
     * A {@code String} that is the name of the expression quality column  for Affymetrix data 
     * in download files, HTML escaped if necessary.
     * @see #AFFY_EXPR_QUAL_LINK_TITLE
     */
    //TODO: split into two columns once we regenerate the download files
    private static final String AFFY_EXPR_QUAL_COL_NAME = "Affymetrix call quality";
    /**
     * A {@code String} to be used in {@code title} attribute of {@code a} tag linking to 
     * affymetrix expression quality column description (used several times), HTML escaped if necessary.
     * @see #AFFY_EXPR_QUAL_COL_NAME
     */
    private static final String AFFY_EXPR_QUAL_LINK_TITLE = "See " + AFFY_EXPR_QUAL_COL_NAME 
            + " column description";
    /**
     * A {@code String} that is the name of the expression state column for EST data 
     * in download files, HTML escaped if necessary.
     * @see #EST_EXPR_STATE_LINK_TITLE
     */
    private static final String EST_EXPR_STATE_COL_NAME = "EST data";
    /**
     * A {@code String} to be used in {@code title} attribute of {@code a} tag linking to 
     * EST expression state column description (used several times), HTML escaped if necessary.
     * @see #EST_EXPR_STATE_COL_NAME
     */
    private static final String EST_EXPR_STATE_LINK_TITLE = "See " + EST_EXPR_STATE_COL_NAME 
            + " column description";
    /**
     * A {@code String} that is the name of the expression quality column  for EST data 
     * in download files, HTML escaped if necessary.
     * @see #EST_EXPR_QUAL_LINK_TITLE
     */
    //TODO: split into two columns once we regenerate the download files
    private static final String EST_EXPR_QUAL_COL_NAME = "EST call quality";
    /**
     * A {@code String} to be used in {@code title} attribute of {@code a} tag linking to 
     * EST expression quality column description (used several times), HTML escaped if necessary.
     * @see #EST_EXPR_QUAL_COL_NAME
     */
    private static final String EST_EXPR_QUAL_LINK_TITLE = "See " + EST_EXPR_QUAL_COL_NAME 
            + " column description";
    /**
     * A {@code String} that is the name of the expression state column for in situ data 
     * in download files, HTML escaped if necessary.
     * @see #IN_SITU_EXPR_STATE_LINK_TITLE
     */
    private static final String IN_SITU_EXPR_STATE_COL_NAME = "In situ data";
    /**
     * A {@code String} to be used in {@code title} attribute of {@code a} tag linking to 
     * in situ expression state column description (used several times), HTML escaped if necessary.
     * @see #IN_SITU_EXPR_STATE_COL_NAME
     */
    private static final String IN_SITU_EXPR_STATE_LINK_TITLE = "See " + IN_SITU_EXPR_STATE_COL_NAME 
            + " column description";
    /**
     * A {@code String} that is the name of the expression quality column  for in situ data 
     * in download files, HTML escaped if necessary.
     * @see #IN_SITU_EXPR_QUAL_LINK_TITLE
     */
    //TODO: split into two columns once we regenerate the download files
    private static final String IN_SITU_EXPR_QUAL_COL_NAME = "In situ call quality";
    /**
     * A {@code String} to be used in {@code title} attribute of {@code a} tag linking to 
     * EST expression quality column description (used several times), HTML escaped if necessary.
     * @see #IN_SITU_EXPR_QUAL_COL_NAME
     */
    private static final String IN_SITU_EXPR_QUAL_LINK_TITLE = "See " + IN_SITU_EXPR_QUAL_COL_NAME 
            + " column description";
    /**
     * A {@code String} that is the name of the expression state column for RNA-Seq data  
     * in download files, HTML escaped if necessary.
     * @see #RNA_SEQ_EXPR_STATE_LINK_TITLE
     */
    private static final String RNA_SEQ_EXPR_STATE_COL_NAME = "RNA-Seq data";
    /**
     * A {@code String} to be used in {@code title} attribute of {@code a} tag linking to 
     * RNA-Seq expression state column description (used several times), HTML escaped if necessary.
     * @see #RNA_SEQ_EXPR_STATE_COL_NAME
     */
    private static final String RNA_SEQ_EXPR_STATE_LINK_TITLE = "See " + RNA_SEQ_EXPR_STATE_COL_NAME 
            + " column description";
    /**
     * A {@code String} that is the name of the expression quality column  for in situ data 
     * in download files, HTML escaped if necessary.
     * @see #RNA_SEQ_EXPR_QUAL_LINK_TITLE
     */
    //TODO: split into two columns once we regenerate the download files
    private static final String RNA_SEQ_EXPR_QUAL_COL_NAME = "RNA-Seq call quality";
    /**
     * A {@code String} to be used in {@code title} attribute of {@code a} tag linking to 
     * EST expression quality column description (used several times), HTML escaped if necessary.
     * @see #RNA_SEQ_EXPR_QUAL_COL_NAME
     */
    private static final String RNA_SEQ_EXPR_QUAL_LINK_TITLE = "See " + RNA_SEQ_EXPR_QUAL_COL_NAME 
            + " column description";
    /**
     * A {@code String} that is the name of the column describing whether data were "observed",
     * in download files, HTML escaped if necessary.
     * @see #OBSERVED_DATA_LINK_TITLE
     */
    private static final String OBSERVED_DATA_COL_NAME = "Including observed data";
    /**
     * A {@code String} to be used in {@code title} attribute of {@code a} tag linking to 
     * the observed data column description (used several times), HTML escaped if necessary.
     * @see #OBSERVED_DATA_EXPR_STATE_COL_NAME
     */
    private static final String OBSERVED_DATA_LINK_TITLE = "See " + OBSERVED_DATA_COL_NAME 
            + " column description";
    /**
     * A {@code String} that is the name of the column describing whether Affymetrix data 
     * were "observed", in download files, HTML escaped if necessary.
     * @see #AFFY_OBSERVED_DATA_LINK_TITLE
     */
    private static final String AFFY_OBSERVED_DATA_COL_NAME = "Including Affymetrix observed data";
    /**
     * A {@code String} to be used in {@code title} attribute of {@code a} tag linking to 
     * the Affymetrix observed data column description (used several times), HTML escaped if necessary.
     * @see #AFFY_OBSERVED_DATA_EXPR_STATE_COL_NAME
     */
    private static final String AFFY_OBSERVED_DATA_LINK_TITLE = "See " + AFFY_OBSERVED_DATA_COL_NAME 
            + " column description";
    /**
     * A {@code String} that is the name of the column describing whether EST data 
     * were "observed", in download files, HTML escaped if necessary.
     * @see #EST_OBSERVED_DATA_LINK_TITLE
     */
    private static final String EST_OBSERVED_DATA_COL_NAME = "Including EST observed data";
    /**
     * A {@code String} to be used in {@code title} attribute of {@code a} tag linking to 
     * the EST observed data column description (used several times), HTML escaped if necessary.
     * @see #EST_OBSERVED_DATA_EXPR_STATE_COL_NAME
     */
    private static final String EST_OBSERVED_DATA_LINK_TITLE = "See " + EST_OBSERVED_DATA_COL_NAME 
            + " column description";
    /**
     * A {@code String} that is the name of the column describing whether in situ data 
     * were "observed", in download files, HTML escaped if necessary.
     * @see #IN_SITU_OBSERVED_DATA_LINK_TITLE
     */
    private static final String IN_SITU_OBSERVED_DATA_COL_NAME = "Including in situ observed data";
    /**
     * A {@code String} to be used in {@code title} attribute of {@code a} tag linking to 
     * the in situ observed data column description (used several times), HTML escaped if necessary.
     * @see #IN_SITU_OBSERVED_DATA_EXPR_STATE_COL_NAME
     */
    private static final String IN_SITU_OBSERVED_DATA_LINK_TITLE = "See " 
         + IN_SITU_OBSERVED_DATA_COL_NAME + " column description";
    /**
     * A {@code String} that is the name of the column describing whether EST data 
     * were "observed", in download files, HTML escaped if necessary.
     * @see #RNA_SEQ_OBSERVED_DATA_LINK_TITLE
     */
    private static final String RNA_SEQ_OBSERVED_DATA_COL_NAME = "Including RNA-Seq observed data";
    /**
     * A {@code String} to be used in {@code title} attribute of {@code a} tag linking to 
     * the EST observed data column description (used several times), HTML escaped if necessary.
     * @see #RNA_SEQ_OBSERVED_DATA_EXPR_STATE_COL_NAME
     */
    private static final String RNA_SEQ_OBSERVED_DATA_LINK_TITLE = "See " 
         + RNA_SEQ_OBSERVED_DATA_COL_NAME + " column description";
    /**
     * A {@code String} that is the name of the differential expression state column in download files, 
     * HTML escaped if necessary.
     * @see #DIFF_EXPR_STATE_LINK_TITLE
     */
    private static final String DIFF_EXPR_STATE_COL_NAME = "Differential expression";
    /**
     * A {@code String} to be used in {@code title} attribute of {@code a} tag linking to 
     * differential expression state column description (used several times), HTML escaped if necessary.
     * @see #DIFF_EXPR_STATE_COL_NAME
     */
    private static final String DIFF_EXPR_STATE_LINK_TITLE = "See " + DIFF_EXPR_STATE_COL_NAME 
            + " column description";
    /**
     * A {@code String} that is the name of the differential expression state column 
     * for Affymetrix data in download files, HTML escaped if necessary.
     */
    private static final String AFFY_DIFF_EXPR_STATE_COL_NAME = "Affymetrix data";
    /**
     * A {@code String} that is the name of the column storing the best p-value supporting 
     * an Affymetrix differential expression call in download files, HTML escaped if necessary.
     */
    private static final String AFFY_DIFF_EXPR_P_VAL_COL_NAME = "Affymetrix best supporting p-value";
    /**
     * A {@code String} that is the name of the column storing the count of analyses supporting 
     * an Affymetrix differential expression call in download files, HTML escaped if necessary.
     */
    private static final String AFFY_DIFF_EXPR_SUPPORT_COUNT_COL_NAME = 
            "Affymetrix analysis count supporting Affymetrix call";
    /**
     * A {@code String} that is the name of the column storing the count of analyses in conflict of  
     * an Affymetrix differential expression call in download files, HTML escaped if necessary.
     */
    private static final String AFFY_DIFF_EXPR_CONFLICT_COUNT_COL_NAME = 
            "Affymetrix analysis count in conflict with Affymetrix call";
    /**
     * A {@code String} that is the name of the differential expression state column 
     * for RNA-Seq data in download files, HTML escaped if necessary.
     */
    private static final String RNA_SEQ_DIFF_EXPR_STATE_COL_NAME = "RNA-Seq data";
    /**
     * A {@code String} that is the name of the column storing the best p-value supporting 
     * an RNA-Seq differential expression call in download files, HTML escaped if necessary.
     */
    private static final String RNA_SEQ_DIFF_EXPR_P_VAL_COL_NAME = "RNA-Seq best supporting p-value";
    /**
     * A {@code String} that is the name of the column storing the count of analyses supporting 
     * an RNA-Seq differential expression call in download files, HTML escaped if necessary.
     */
    private static final String RNA_SEQ_DIFF_EXPR_SUPPORT_COUNT_COL_NAME = 
            "RNA-Seq analysis count supporting RNA-Seq call";
    /**
     * A {@code String} that is the name of the column storing the count of analyses in conflict of  
     * an RNA-Seq differential expression call in download files, HTML escaped if necessary.
     */
    private static final String RNA_SEQ_DIFF_EXPR_CONFLICT_COUNT_COL_NAME = 
            "RNA-Seq analysis count in conflict with RNA-Seq call";

    /**
     * A {@code String} that is the name of the column storing homologous anatomical 
     * entity IDs in multi-species download files, HTML escaped if necessary.
     */
    private static final String MULTI_ANAT_ENTITY_IDS_COL_NAME ="Anatomical entity IDs";
    /**
     * A {@code String} that is the name of the column storing homologous anatomical 
     * entity names in multi-species download files, HTML escaped if necessary.
     */
    private static final String MULTI_ANAT_ENTITY_NAMES_COL_NAME ="Anatomical entity names";
    /**
     * A {@code String} that is the name of the column storing the OMA HOG ID 
     * in multi-species download files, HTML escaped if necessary.
     */
    private static final String OMA_ID_COL_NAME = "OMA ID";
    /**
     * A {@code String} that is the prefix of the name of the columns storing the number 
     * of over-expressed genes in a condition for a gene orthology group, for a given species 
     * (the suffix of the column name is the latin name of the species) in multi-species 
     * download file, HTML escaped if necessary.
     */
    private static final String OVER_EXPRESSED_FOR_SPECIES_COL_NAME = "Over-expressed gene count for";
    /**
     * A {@code String} that is the prefix of the name of the columns storing the number 
     * of under-expressed genes in a condition for a gene orthology group, for a given species 
     * (the suffix of the column name is the latin name of the species) in multi-species 
     * download file, HTML escaped if necessary.
     */
    private static final String UNDER_EXPRESSED_FOR_SPECIES_COL_NAME = "Under-expressed gene count for";
    /**
     * A {@code String} that is the prefix of the name of the columns storing the number 
     * of genes with no diff. expression or inconclusive results in a condition for 
     * a gene orthology group, for a given species 
     * (the suffix of the column name is the latin name of the species) in multi-species 
     * download file, HTML escaped if necessary.
     */
    private static final String NOT_DIFF_EXPRESSED_FOR_SPECIES_COL_NAME = "Not diff. expressed gene count for";
    /**
     * A {@code String} that is the prefix of the name of the columns storing the number 
     * of genes with no data in a condition for a gene orthology group, 
     * for a given species (the suffix of the column name is the latin name of the species) 
     * in multi-species download file, HTML escaped if necessary.
     */
    private static final String NA_FOR_SPECIES_COL_NAME = "NA gene count for";
    /**
     * A {@code String} that is the name of the column storing the latin name of species 
     * in multi-species complete download files, HTML escaped if necessary.
     */
    private static final String SPECIES_LATIN_NAME_COL_NAME = "Latin species name";
    /**
     * A {@code String} that is the name of the column storing the CIO ID associated to  
     * anatomical homology mapping, in multi-species complete download files, 
     * HTML escaped if necessary.
     */
    private static final String ANAT_HOMOLOGY_CIO_ID_COL_NAME = "Anatomy homology CIO ID";
    /**
     * A {@code String} that is the name of the column storing the CIO name associated to  
     * anatomical homology mapping, in multi-species complete download files, 
     * HTML escaped if necessary.
     */
    private static final String ANAT_HOMOLOGY_CIO_NAME_COL_NAME = "Anatomy homology CIO name";
    



    /**
     * @param colNumber An {@code int} that is the index of the column containing 
     *                  the gene ID (see {@link #GENE_ID_COL_NAME}). 
     *                  Index starting from 1.
     * @return  A {@code String} that is the description of the gene name column 
     *          in download files (because we use it several times), formated in HTML 
     *          and HTML escaped if necessary.
     * @see #GENE_NAME_COL_NAME
     */
    private static String getGeneNameColDescription(int colNumber) {
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
    private static String getOMAIdColDescription() {
        log.entry();
        return log.exit("<p>Unique identifier of the OMA gene orthology group. Note that "
                + "these identifiers are not stable between releases, and cannot be used "
                + "to retrieve data from <a target='_blank' "
                + "href='http://omabrowser.org/oma/hogs/' title='External link to OMA browser'>"
                + "the OMA browser</a>. They are provided solely to group data "
                + "from orthologous genes belonging to a same orthology group. "
                + "Genes member of a OMA gene orthology group can be retrieved "
                + "through the associated "
                + "<a href='#" + RequestParameters.HASH_DOC_CALL_OMA 
                + "' title='Jump to hierarchical orthologous groups "
                + "file documentation'>hierarchical orthologous groups file</a>.</p>");
    }

    /**
     * @return  A {@code String} that is the description of the stage ID column 
     *          in multi-species download files (because we use it several times), formated in HTML 
     *          and HTML escaped if necessary.
     * @see #STAGE_ID_COL_NAME
     */
    private static String getMultiSpeciesStageIdColDescription() {
        log.entry();
        return log.exit("<p>Unique identifier of the developmental stage, from the Uberon ontology. "
                + "For multi-species analyses, only broad developmental stages are used, "
                + "common to the species being compared.</p>");
    }
    /**
     * @return  A {@code String} that is the description of the column for multiple 
     *          anatomical IDs for multi-species download files (because we use it several times), 
     *          formated in HTML and HTML escaped if necessary.
     * @see #MULTI_ANAT_ENTITY_IDS_COL_NAME
     */
    private static String getMultiAnatEntityIdsColDescription() {
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
    private static String getMultiAnatEntityNamesColDescription(int colNumber) {
        log.entry();
        return log.exit("<p>Names of the anatomical entities defined by <code>" 
            + MULTI_ANAT_ENTITY_IDS_COL_NAME + "</code> (column " + colNumber + "). "
            + "Cardinality 1 or greater. When more than "
            + "one anatomical entity is used, they are separated with the character "
            + "<code>|</code>. See <code>"+ MULTI_ANAT_ENTITY_IDS_COL_NAME + "</code> column description "
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
    private static String getColumnListForCall(int geneIdColNumber, int stageIdColNumber, 
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
     * @param exprQualColNumber         An {@code int} that is the index of the column 
     *                                  containing the quality associated to the expression state, 
     *                                  (see {@link #EXPR_QUAL_COL_NAME}). Index starting from 1.
     * @param singleSpecies             A {@code boolean} defining whether the column names 
     *                                  used should be defined for a single-species file, 
     *                                  or a multi-species file. 
     * @return  A {@code String} that is the description of the expression state column 
     *          in download files (because we use it several times), formated in HTML 
     *          and HTML escaped if necessary.
     * @see #EXPR_STATE_COL_NAME
     */
    private static String getExprStateColDescription(int geneIdColNumber, int stageIdColNumber, 
            int anatEntityIdColNumber, int exprQualColNumber, boolean singleSpecies) {
        log.entry(geneIdColNumber, stageIdColNumber, anatEntityIdColNumber, 
                exprQualColNumber, singleSpecies);
        return log.exit("<p>Call generated from all data types for " 
                + getColumnListForCall(geneIdColNumber, stageIdColNumber, 
                        anatEntityIdColNumber, singleSpecies) + ". One of: </p>"
                + "<ul class='doc_content'>"
                + "<li><span class='list_element_title'>present</span>: "
                + "report of presence of expression, from Bgee statistical tests and/or from "
                + "<i>in situ</i> data sources. See <code>" + EXPR_QUAL_COL_NAME 
                + "</code> (column " + exprQualColNumber + ") for associated quality level.</li>"
                + "<li><span class='list_element_title'>absent</span>: "
                + "report of absence of expression, from Bgee statistical tests and/or "
                + "from <i>in situ</i> data sources. In Bgee, calls of absence of expression "
                + "are always discarded if there exists a contradicting call of expression, "
                + "from the same data type and for the same gene, in the same anatomical entity "
                + "and developmental stage, or in a child entity or child developmental stage. "
                + "See <code>" + EXPR_QUAL_COL_NAME + "</code> (column " + exprQualColNumber 
                + ") for associated quality level.</li>"
                + "<li><span class='list_element_title'>low ambiguity</span>: "
                + "there exists a call of expression generated from a data type, but "
                + "there exists a call of absence of expression generated from another data type "
                + "for the same gene in a parent anatomical entity at the same developmental "
                + "stage. For instance, gene A is reported to be expressed in the midbrain "
                + "at young adult stage from Affymetrix data, but is reported to be not expressed "
                + "in the brain at young adult stage from RNA-Seq data.</li>"
                + "<li><span class='list_element_title'>high ambiguity</span>: "
                + "there exists a call of expression generated from a data type, but "
                + "there exists a call of absence of expression generated from another data type "
                + "for the same gene, anatomical entity and developmental stage. For instance, "
                + "gene A is reported to be expressed in the midbrain at young adult stage "
                + "from Affymetrix data, but is reported to be not expressed in the midbrain "
                + "at young adult stage from RNA-Seq data.</li>"
                + "</ul>");
    }
    /**
     * Generates description of the expression quality column. 
     * 
     * @param exprStateColNumber        An {@code int} that is the index of the column 
     *                                  containing the expression state (see 
     *                                  {@link #EXPR_STATE_COL_NAME}). Index starting from 1.
     * @return  A {@code String} that is the description of the expression quality column 
     *          in download files (because we use it several times), formated in HTML 
     *          and HTML escaped if necessary.
     * @see #EXPR_QUAL_COL_NAME
     */
    private static String getExprQualColDescription(int exprStateColNumber) {
        log.entry(exprStateColNumber);
        return log.exit("<p>Quality associated to the call in column <code>" +  EXPR_STATE_COL_NAME
                + "</code> (column" + exprStateColNumber + "). One of: </p>"
                + "<ul class='doc_content'>"
                + "<li><span class='list_element_title'>high quality</span>: "
                    + "<ul>"
                    + "<li>In case of report of expression, expression reported as high quality "
                    + "from Bgee statistical tests and/or from <i>in situ</i> data sources, "
                    + "with no contradicting call of absence of expression for same gene, "
                    + "in same anatomical entity and developmental stage (call generated "
                    + "either from multiple congruent data, or from single data).</li>"
                    + "<li>In case of report of absence of expression, call reported as high quality "
                    + "either from Bgee statistical tests and/or from <i>in situ</i> data sources. "
                    + "In Bgee, calls of absence of expression are always discarded "
                    + "if there exists a contradicting call of expression, from the same "
                    + "data type and for the same gene, in the same anatomical entity "
                    + "and developmental stage, or in a child entity or child developmental stage. "
                    + "This is why they are always considered of high quality.</li>"
                    + "</ul>"
                + "</li>"
                + "<li><span class='list_element_title'>poor quality</span>: "
                + "in case of report of expression, expression reported as low quality "
                + "from Bgee statistical tests and/or from <i>in situ</i> data sources, "
                + "or because there exists a conflict of presence/absence of expression "
                + "for the same gene, anatomical entity and developmental stage, from "
                + "different data of a same type (conflicts between different data types "
                + "are treated differently, see <code>ambiguity</code> states in column <code>" 
                +  EXPR_STATE_COL_NAME + "</code> ).</li>"
                + "<li><span class='list_element_title'>NA</span>: when the call in column <code>" 
                +  EXPR_STATE_COL_NAME + "</code> is ambiguous.</li>"
                + "</ul>");
    }
    /**
     * Generates description of the observed data column. 
     * 
     * @param observedDataColName   A {@code String} that is the name of the column 
     *                              for which the description is being generated. 
     * @return  A {@code String} that is the description of the observed data column 
     *          in download files, formated in HTML and HTML escaped if necessary.
     * @see #OBSERVED_DATA_COL_NAME
     * @see #AFFY_OBSERVED_DATA_COL_NAME
     * @see #EST_OBSERVED_DATA_COL_NAME
     * @see #IN_SITU_OBSERVED_DATA_COL_NAME
     * @see #RNA_SEQ_OBSERVED_DATA_COL_NAME
     */
    private static String getObservedDataColDescription(String observedDataColName) {
        log.entry(observedDataColName);
        if (StringUtils.isBlank(observedDataColName)) {
            throw log.throwing(new IllegalArgumentException("Blank column name provided."));
        }
        
        String desc = "<p>Values permitted: <code>yes</code> and <code>no</code>.</p>"
                + "<p>Defines whether a call was generated from propagation only, "
                + "or whether this call in this anatomical entity/developmental stage condition "
                + "was actually seen in experimental data (in which case, the call will also "
                + "be present in the expression simple file).</p>";
        switch (observedDataColName) {
            case OBSERVED_DATA_COL_NAME: 
                desc += "<p>In this column, the information is provided by considering all "
                        + "data types together.</p>";
                break;
            case AFFY_OBSERVED_DATA_COL_NAME: 
                desc += "<p>In this column, the information is provided by solely considering "
                        + "Affymetrix data.</p>";
                break;
            case EST_OBSERVED_DATA_COL_NAME: 
                desc += "<p>In this column, the information is provided by solely considering "
                        + "EST data.</p>";
                break;
            case IN_SITU_OBSERVED_DATA_COL_NAME: 
                desc += "<p>In this column, the information is provided by solely considering "
                        + "<i>in situ</i> data.</p>";
                break;
            case RNA_SEQ_OBSERVED_DATA_COL_NAME: 
                desc += "<p>In this column, the information is provided by solely considering "
                        + "RNA-Seq data.</p>";
                break;
            default: 
                throw log.throwing(new IllegalArgumentException("Unrecognized column name: " 
                        + observedDataColName));
        } 
        
        return log.exit(desc);
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
    private static String getDiffExprStateColDescription(int geneIdColNumber, int stageIdColNumber, 
            int anatEntityIdColNumber, boolean singleSpecies, boolean noDiffExpr, 
            boolean ambiguity, boolean noData, String dataType) {
        log.entry(geneIdColNumber, stageIdColNumber, anatEntityIdColNumber, singleSpecies, 
                noDiffExpr, ambiguity, noData, dataType);
        
        String desc = "<p>Call generated from " + dataType + " for " 
                + getColumnListForCall(geneIdColNumber, stageIdColNumber, 
                        anatEntityIdColNumber, singleSpecies) + ". One of: </p>"
                + "<ul class='doc_content'>"
                + "<li><span class='list_element_title'>over-expression</span>: "
                + "the gene was shown in one or more analyses to have a significant over-expression "
                + "in this condition, as compared to the expression levels in other conditions "
                + "of the analyses.</li>"
                + "<li><span class='list_element_title'>under-expression</span>: "
                + "the gene was shown in one or more analyses to have a significant under-expression "
                + "in this condition, as compared to the expression levels in other conditions "
                + "of the analyses.</li>";
        if (noDiffExpr) {
            desc += "<li><span class='list_element_title'>no diff expression</span>: "
                    + "the gene was tested for differential expression in this condition, "
                    + "but was never shown to have a significant variation of expression "
                    + "as compared to the other conditions of the analyses.</li>";
        }
        if (ambiguity) {
                //TODO: change 'weak' to 'low' when files will be re-generated
            desc += "<li><span class='list_element_title'>weak ambiguity</span>: "
                + "there exists a call of over-expression or under-expression generated "
                + "from a data type, but another data type showed no significant variation "
                + "of the level of expression of this gene in the same condition; or, a gene "
                + "was shown to be never expressed in a condition by some analyses of a given data type, "
                + "but other analyses of different data types produced a call of over-expression "
                + "or of absence of differential expression for the same gene, "
                + "in the same condition (note that conflicts where a data type produced "
                + "an under-expression call in a condition, while another data type showed the same gene "
                + "to be never expressed in that condition, do not produce a <code>weak ambiguity</code> "
                + "call, but a call of <code>under-expression low quality</code>).</li>"
                //TODO: change 'weak' to 'low' when files will be re-generated
                + "<li><span class='list_element_title'>strong ambiguity</span>: "
                + "there exists a call of over-expression or under-expression generated "
                + "from a data type, but there exists a call in the opposite direction "
                + "generated from another data type for the same gene, anatomical entity "
                + "and developmental stage. For instance, gene A is reported to be over-expressed "
                + "in the midbrain at young adult stage from Affymetrix data, but is reported "
                + "to be under-expressed in the midbrain at young adult stage from RNA-Seq data.</li>";
        }
        if (noData) {
            desc += "<li><span class='list_element_title'>no data</span>: "
                    + "no analyses of this data type compared expression level of this gene "
                    + "in this condition.</li>";
        }
        desc += "</ul>";
        
        return log.exit(desc);
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
     *                                  about the "NA" quality should be provided. 
     * @param displayNoData             A {@code boolean} defining whether explanation 
     *                                  about the "no data" quality should be provided. 
     * @return  A {@code String} that is the description of the diff expression quality column 
     *          in download files (because we use it several times), formated in HTML 
     *          and HTML escaped if necessary.
     * @see #getDiffExprStateColDescription(int, int, int, boolean, boolean, String)
     */
    private static String getDiffExprQualColDescription(String diffExprStateColName, 
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
                + "or from a single analysis).</li>"
                //TODO: change 'poor' to 'low' after we re-generate the file
                + "<li><span class='list_element_title'>poor quality</span>: "
                + "differential expression reported as low quality, or there exists a conflict "
                + "for the same gene, anatomical entity and developmental stage, "
                + "from different analyses of a same data type "
                + "(conflicts between different data types are treated differently). "
                + "For instance, an analysis showed a gene to be over-expressed in a condition, "
                + "while another analysis showed the same gene to be under-expressed or "
                //TODO: add link to data analyses section
                + "not differentially expressed in the same condition. Such conflicts "
                + "are resolved by a voting system based on the number of conditions compared, "
                + "weighted by p-value. Note that in one case, this quality level is used "
                + "to reconcile conflicting calls from different data types: "
                + "when a data type produced an under-expression call, while a different "
                + "data type has shown that the same gene was never seen as expressed "
                + "in the same condition. In that case, the overall summary "
                + "is <code>under-expression low quality</code>.</li>";
        if (displayNA) {
            //TODO: merge NA and 'no data' once we re-generate the files
            desc += "<li><span class='list_element_title'>NA</span>: no quality applicable "
                + "when ambiguity state in <code>" + diffExprStateColName 
                + "</code> (column " + diffExprStateColNumber + ").</li>";
        }
        if (displayNoData) {
            desc += "<li><span class='list_element_title'>no data</span>: no data associated "
                    + "to <code>" + diffExprStateColName 
                    + "</code> (column " + diffExprStateColNumber + ").</li>";
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
    private static String getDiffExprPvalColDescription(String dataType, String diffExprStateColName, 
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
    private static String getDiffSupportCountColDescription(String dataType, String diffExprStateColName, 
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
    private static String getDiffConflictCountColDescription(String dataType, String diffExprStateColName, 
            int diffExprStateColIndex) {
        log.entry(dataType, diffExprStateColName, diffExprStateColIndex);
        return log.exit("<p>Number of " + dataType + " analyses in conflict, generating "
                + "a call different from the call provided in <code>" + diffExprStateColName 
                + "</code> (column " + diffExprStateColIndex + "). Set to 0 if no data available by " 
                + dataType + ".</p>");
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
    private static String getOverUnderExprForSpeciesColDescription(int omaIdColNumber, 
            int stageIdColNumber, int anatEntityIdsColNumber, boolean overExpressed) {
        log.entry(omaIdColNumber, stageIdColNumber, anatEntityIdsColNumber, overExpressed);
        
        String desc = "<p>Number of genes, members of the OMA orthologous gene group "
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
    public static String getSingleSpeciesSimpleExprFileHeaderDesc() {
        log.entry();
        //TODO: change when we split the state and the qual
        return log.exit("<table class='download_file_header_desc'>"
                + "<tbody>"
                + "<tr><td>1</td><td>2</td><td>3</td><td>4</td><td>5</td><td>6</td><td>7</td>"
                + "<td>8</td></tr>" + getSingleSpeciesSimpleExprFileHeader(true)
                + "</tbody>"
                + "</table>");
    }
    /**
     * @return  a {@code String} containing the HTML to create a table containing the header 
     *          and example lines of a single species simple expression file.
     */
    public static String getSingleSpeciesSimpleExprFileExample() {
        log.entry();
        return log.exit("<table class='call_download_file_example'>"
                + "<caption>Example lines for single species simple expression file</caption>"
                + "<thead>" 
                + getSingleSpeciesSimpleExprFileHeader(false) 
                + "</thead>"
                + "<tbody>"
                + "<tr><td>FBgn0005533</td><td>RpS17</td><td>UBERON:0015230</td>"
                + "<td>dorsal vessel heart</td><td>FBdv:00007124</td>"
                + "<td>day 49 of adulthood (Drosophila)</td><td>present</td><td>high quality</td></tr>"
                + "<tr><td>FBgn0005536</td><td>Mbs</td><td>FBbt:00003023</td>"
                + "<td>adult abdomen (Drosophila)</td><td>UBERON:0000066</td>"
                + "<td>fully formed stage</td><td>present</td><td>poor quality</td></tr>"
                + "<tr><td>FBgn0005558</td><td>ey</td><td>FBbt:00001684</td>"
                + "<td>embryonic/larval hemocyte (Drosophila)</td><td>FBdv:00005339</td>"
                + "<td>third instar larval stage (Drosophila)</td><td>absent</td>"
                + "<td>high quality</td></tr>"
                + "</tbody>"
                + "</table>");
    }
    /**
     * Get the header of single species simple expression file as a HTML 'tr' element, 
     * with column being either 'td' or 'th' elements depending on argument {@code withTd}.
     * @param withTd    A {@code boolean} defining whether the column type should be 'td' 
     *                  or 'th'. If {@code true}, 'td' is used.
     * @return          A {@code String} that is the header of single species simple 
     *                  expression file as a HTML 'tr' element.
     */
    private static String getSingleSpeciesSimpleExprFileHeader(boolean withTd) {
        log.entry(withTd);
        String colType ="td";
        if (!withTd) {
            colType = "th";
        }
        return log.exit("<tr>"
                + "<" + colType + ">" + GENE_ID_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + GENE_NAME_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + ANAT_ENTITY_ID_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + ANAT_ENTITY_NAME_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + STAGE_ID_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + STAGE_NAME_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + EXPR_STATE_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + EXPR_QUAL_COL_NAME + "</" + colType + ">"
                + "</tr>");
    }
    /**
     * @return  a {@code String} containing the HTML to create a table containing the description 
     *          of the header of a single species complete expression file (can be used 
     *          in "help" links).
     */
    public static String getSingleSpeciesCompleteExprFileHeaderDesc() {
        log.entry();
        return log.exit("<table class='download_file_header_desc'>"
                + "<tbody>"
                + "<tr><td>1</td><td>2</td><td>3</td><td>4</td><td>5</td><td>6</td><td>7</td>"
                + "<td>8</td><td>9</td><td>10</td><td>11</td><td>12</td>"
                + "<td>13</td><td>14</td><td>15</td><td>16</td><td>17</td>"
                + "<td>18</td><td>19</td><td>20</td><td>21</td></tr>"
                + getSingleSpeciesCompleteExprFileHeader(true)
                + "</tbody>"
                + "</table>");
    }
    /**
     * @return  a {@code String} containing the HTML to create a table containing the header 
     *          and example lines of a single species complete expression file.
     */
    public static String getSingleSpeciesCompleteExprFileExample() {
        log.entry();
        return log.exit("<table class='call_download_file_example'>"
                + "<caption>Example lines for single species complete expression file</caption>"
                + "<thead>" 
                + getSingleSpeciesCompleteExprFileHeader(false) 
                + "</thead>"
                + "<tbody>"
                
                + "<tr><td>ENSDARG00000000002</td><td>ccdc80</td><td>UBERON:0000965</td>"
                + "<td>lens of camera-type eye</td><td>ZFS:0000033</td>"
                + "<td>Hatching:Long-pec (Danio)</td>"
                + "<td>present</td><td>high quality</td><td>yes</td>"
                + "<td>no data</td><td>no data</td><td>no</td>"
                + "<td>no data</td><td>no data</td><td>no</td>"
                + "<td>present</td><td>high quality</td><td>yes</td>"
                + "<td>no data</td><td>no data</td><td>no</td></tr>"
                
                + "<tr><td>ENSDARG00000000175</td><td>hoxb2a</td><td>UBERON:0004734</td>"
                + "<td>gastrula</td><td>ZFS:0000017</td><td>Gastrula:50%-epiboly (Danio)</td>"
                + "<td>absent</td><td>high quality</td><td>yes</td>"
                + "<td>absent</td><td>high quality</td><td>no</td>"
                + "<td>no data</td><td>no data</td><td>no</td>"
                + "<td>absent</td><td>high quality</td><td>yes</td>"
                + "<td>no data</td><td>no data</td><td>no</td></tr>"
                
                + "<tr><td>ENSDARG00000000241</td><td>slc40a1</td><td>UBERON:0000922</td>"
                + "<td>embryo</td><td>ZFS:0000019</td><td>Gastrula:Shield (Danio)</td>"
                + "<td>low ambiguity</td><td>NA</td><td>no</td>"
                + "<td>absent</td><td>high quality</td><td>no</td>"
                + "<td>no data</td><td>no data</td><td>no</td>"
                + "<td>present</td><td>high quality</td><td>no</td>"
                + "<td>no data</td><td>no data</td><td>no</td></tr>"
                + "</tbody>"
                + "</table>");
    }
    /**
     * Get the header of single species complete expression file as a HTML 'tr' element, 
     * with column being either 'td' or 'th' elements depending on argument {@code withTd}.
     * @param withTd    A {@code boolean} defining whether the column type should be 'td' 
     *                  or 'th'. If {@code true}, 'td' is used.
     * @return          A {@code String} that is the header of single species complete 
     *                  expression file as a HTML 'tr' element.
     */
    private static String getSingleSpeciesCompleteExprFileHeader(boolean withTd) {
        log.entry(withTd);
        String colType ="td";
        if (!withTd) {
            colType = "th";
        }
        return log.exit("<tr>"
                + "<" + colType + ">" + GENE_ID_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + GENE_NAME_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + ANAT_ENTITY_ID_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + ANAT_ENTITY_NAME_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + STAGE_ID_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + STAGE_NAME_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + EXPR_STATE_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + EXPR_QUAL_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + OBSERVED_DATA_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + AFFY_EXPR_STATE_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + AFFY_EXPR_QUAL_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + AFFY_OBSERVED_DATA_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + EST_EXPR_STATE_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + EST_EXPR_QUAL_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + EST_OBSERVED_DATA_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + IN_SITU_EXPR_STATE_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + IN_SITU_EXPR_QUAL_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + IN_SITU_OBSERVED_DATA_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + RNA_SEQ_EXPR_STATE_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + RNA_SEQ_EXPR_QUAL_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + RNA_SEQ_OBSERVED_DATA_COL_NAME + "</" + colType + ">"
                + "</tr>");
    }
    /**
     * @return  a {@code String} containing the HTML to create a table containing the description 
     *          of the header of a single species simple over-/under-expression file (can be used 
     *          in "help" links).
     */
    public static String getSingleSpeciesSimpleDiffExprFileHeaderDesc() {
        log.entry();
        return log.exit("<table class='download_file_header_desc'>"
                + "<tbody>"
                + "<tr><td>1</td><td>2</td><td>3</td><td>4</td><td>5</td><td>6</td><td>7</td>"
                + "<td>8</td></tr>"
                + getSingleSpeciesSimpleDiffExprFileHeader(true)
                + "</tbody>"
                + "</table>");
    }
    /**
     * @return  a {@code String} containing the HTML to create a table containing the header 
     *          and example lines of a single species simple differential expression file.
     */
    public static String getSingleSpeciesSimpleDiffExprFileExample() {
        log.entry();
        return log.exit("<table class='call_download_file_example'>"
                + "<caption>Example lines for single species simple differential expression file</caption>"
                + "<thead>" 
                + getSingleSpeciesSimpleDiffExprFileHeader(false) 
                + "</thead>"
                + "<tbody>"
                + "<tr><td>ENSG00000000003</td><td>TSPAN6</td><td>UBERON:0000922</td>"
                + "<td>embryo</td><td>HsapDv:0000017</td><td>Carnegie stage 10 (human)</td>"
                + "<td>over-expression</td><td>low quality</td></tr>"
                + "<tr><td>ENSG00000000419</td><td>DPM1</td><td>UBERON:0000922</td>"
                + "<td>embryo</td><td>HsapDv:0000020</td><td>Carnegie stage 13 (human)</td>"
                + "<td>under-expression</td><td>low quality</td></tr>"
                + "<tr><td>ENSG00000000457</td><td>SCYL3</td><td>UBERON:0000178</td>"
                + "<td>blood</td><td>HsapDv:0000094</td><td>65-79 year-old human stage (human)</td>"
                + "<td>over-expression</td><td>low quality</td></tr>"
                + "</tbody>"
                + "</table>");
    }
    /*
    /**
     * Get the header of single species simple over-/under-expression file as a HTML 'tr' element, 
     * with column being either 'td' or 'th' elements depending on argument {@code withTd}.
     * @param withTd    A {@code boolean} defining whether the column type should be 'td' 
     *                  or 'th'. If {@code true}, 'td' is used.
     * @return          A {@code String} that is the header of single species simple 
     *                  over-/under-expression file as a HTML 'tr' element.
     */
    private static String getSingleSpeciesSimpleDiffExprFileHeader(boolean withTd) {
        log.entry(withTd);
        String colType ="td";
        if (!withTd) {
            colType = "th";
        }
        return log.exit("<tr>"
                + "<" + colType + ">" + GENE_ID_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + GENE_NAME_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + ANAT_ENTITY_ID_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + ANAT_ENTITY_NAME_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + STAGE_ID_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + STAGE_NAME_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + DIFF_EXPR_STATE_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + EXPR_QUAL_COL_NAME + "</" + colType + ">"
                + "</tr>");
    }
    /**
     * @return  a {@code String} containing the HTML to create a table containing the description 
     *          of the header of a single species complete differential expression file (can be used 
     *          in "help" links).
     */
    public static String getSingleSpeciesCompleteDiffExprFileHeaderDesc() {
        log.entry();
        return log.exit("<table class='download_file_header_desc'>"
                + "<tbody>"
                + "<tr><td>1</td><td>2</td><td>3</td><td>4</td><td>5</td><td>6</td><td>7</td>"
                + "<td>8</td><td>9</td><td>10</td><td>11</td><td>12</td><td>13</td><td>14</td>"
                + "<td>15</td><td>16</td><td>17</td><td>18</td></tr>"
                + getSingleSpeciesCompleteDiffExprFileHeader(true)
                + "</tbody>"
                + "</table>");
    }
    /**
     * @return  a {@code String} containing the HTML to create a table containing the header 
     *          and example lines of a single species complete differential expression file.
     */
    public static String getSingleSpeciesCompleteDiffExprFileExample() {
        log.entry();
        return log.exit("<table class='call_download_file_example'>"
                + "<caption>Example lines for single species complete differential expression file</caption>"
                + "<thead>" 
                + getSingleSpeciesCompleteDiffExprFileHeader(false) 
                + "</thead>"
                + "<tbody>"
                //TODO: change 'poor' to 'low'
                + "<tr><td>ENSMUSG00000000001</td><td>Gnai3</td><td>UBERON:0000081</td>"
                + "<td>metanephros</td><td>MmusDv:0000027</td><td>Theiler stage 20 (mouse)</td>"
                + "<td>no diff expression</td><td>high quality</td>"
                + "<td>no diff expression</td><td>high quality</td><td>0.22166589</td>"
                + "<td>1</td><td>0</td><td>no data</td><td>no data</td><td>1.0</td>"
                + "<td>0</td><td>0</td></tr>"
                + "<tr><td>ENSMUSG00000000028</td><td>Cdc45</td><td>UBERON:0000992</td>"
                + "<td>female gonad</td><td>MmusDv:0000035</td><td>Theiler stage 26 (mouse)</td>"
                + "<td>under-expression</td><td>poor quality</td>"
                + "<td>under-expression</td><td>poor quality</td><td>6.386149E-4</td>"
                + "<td>1</td><td>1</td><td>no data</td><td>no data</td><td>1.0</td>"
                + "<td>0</td><td>0</td></tr>"
                + "<tr><td>ENSMUSG00000000031</td><td>H19</td><td>UBERON:0002037</td>"
                + "<td>cerebellum</td><td>MmusDv:0000036</td><td>Theiler stage 27 (mouse)</td>"
                + "<td>over-expression</td><td>high quality</td>"
                + "<td>over-expression</td><td>high quality</td><td>1.2336E-6</td>"
                + "<td>2</td><td>0</td><td>no data</td><td>no data</td><td>1.0</td>"
                + "<td>0</td><td>0</td></tr>"
                + "</tbody>"
                + "</table>");
    }
    /**
     * Get the header of single species simple over-/under-expression file as a HTML 'tr' element, 
     * with column being either 'td' or 'th' elements depending on argument {@code withTd}.
     * @param withTd    A {@code boolean} defining whether the column type should be 'td' 
     *                  or 'th'. If {@code true}, 'td' is used.
     * @return          A {@code String} that is the header of single species simple 
     *                  over-/under-expression file as a HTML 'tr' element.
     */
    private static String getSingleSpeciesCompleteDiffExprFileHeader(boolean withTd) {
        log.entry(withTd);
        String colType ="td";
        if (!withTd) {
            colType = "th";
        }
        return log.exit("<tr>"
                + "<" + colType + ">" + GENE_ID_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + GENE_NAME_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + ANAT_ENTITY_ID_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + ANAT_ENTITY_NAME_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + STAGE_ID_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + STAGE_NAME_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + DIFF_EXPR_STATE_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + EXPR_QUAL_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + AFFY_DIFF_EXPR_STATE_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + AFFY_EXPR_QUAL_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + AFFY_DIFF_EXPR_P_VAL_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + AFFY_DIFF_EXPR_SUPPORT_COUNT_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + AFFY_DIFF_EXPR_CONFLICT_COUNT_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + RNA_SEQ_DIFF_EXPR_STATE_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + RNA_SEQ_EXPR_QUAL_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + RNA_SEQ_DIFF_EXPR_P_VAL_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + RNA_SEQ_DIFF_EXPR_SUPPORT_COUNT_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + RNA_SEQ_DIFF_EXPR_CONFLICT_COUNT_COL_NAME + "</" + colType + ">"
                + "</tr>");
    }

    /**
     * @return  a {@code String} containing the HTML to create a table containing the description 
     *          of the header of OMA HOG files (can be used in "help" links).
     */
    public static String getOMAGroupFileHeaderDesc() {
        log.entry();
        //TODO: change when we split the state and the qual
        return log.exit("<table class='download_file_header_desc'>"
                + "<tbody>"
                + "<tr><td>1</td><td>2</td><td>3</td></tr>" 
                + getOMAGroupFileHeader(true)
                + "</tbody>"
                + "</table>");
    }
    /**
     * @return  a {@code String} containing the HTML to create a table containing the header 
     *          and example lines of a OMA HOG file.
     */
    public static String getOMAGroupFileExample() {
        log.entry();
        return log.exit("<table class='call_download_file_example'>"
                + "<caption>Example lines for a OMA Hierarchical orthologous groups file</caption>"
                + "<thead>" 
                + getOMAGroupFileHeader(false) 
                + "</thead>"
                + "<tbody>"
                + "<tr><td>98828</td><td>ENSG00000158473</td><td>CD1D</td></tr>"
                + "<tr><td>98828</td><td>ENSMUSG00000028076</td><td>Cd1d1</td></tr>"
                + "<tr><td>98828</td><td>ENSMUSG00000041750</td><td>Cd1d2</td></tr>"
                + "</tbody>"
                + "</table>");
    }
    /**
     * Get the header of a OMA HOG file as a HTML 'tr' element, 
     * with column being either 'td' or 'th' elements depending on argument {@code withTd}.
     * @param withTd    A {@code boolean} defining whether the column type should be 'td' 
     *                  or 'th'. If {@code true}, 'td' is used.
     * @return          A {@code String} that is the header of a OMA HOG file as a HTML 'tr' element.
     */
    private static String getOMAGroupFileHeader(boolean withTd) {
        log.entry(withTd);
        String colType ="td";
        if (!withTd) {
            colType = "th";
        }
        return log.exit("<tr>"
                + "<" + colType + ">" + OMA_ID_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + GENE_ID_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + GENE_NAME_COL_NAME + "</" + colType + ">"
                + "</tr>");
    }
    /**
     * @return  a {@code String} containing the HTML to create a table containing the description 
     *          of the header of a multi-species simple differential expression file (can be used 
     *          in "help" links).
     */
    public static String getMultiSpeciesSimpleDiffExprFileHeaderDesc() {
        log.entry();
        return log.exit("<table class='download_file_header_desc'>"
                + "<tbody>"
                + "<tr><td>1</td><td>2</td><td>3</td><td>4</td><td>5</td><td>6</td><td>7</td>"
                + "<td>8</td><td>9</td><td>10</td><td>11</td><td>12</td><td>13</td><td>...</td>"
                + "<td>(species*4 + 6)</td><td>(species*4 + 7)</td></tr>"
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
                + "<td>" + GENE_IDS_COL_NAME + "</td>"
                + "<td>" + GENE_NAMES_COL_NAME + "</td>"
                + "</tr>"
                + "</tbody>"
                + "</table>");
    }
    /**
     * @return  a {@code String} containing the HTML to create a table containing the header 
     *          and example lines of a multi species simple differential expression file.
     */
    public static String getMultiSpeciesSimpleDiffExprFileExample() {
        log.entry();
        return log.exit("<table class='call_download_file_example'>"
                + "<caption>Example lines for multi-species simple differential expression file</caption>"
                + "<thead>" 
                + "<tr>"
                + "<th>" + OMA_ID_COL_NAME + "</th>"
                + "<th>" + MULTI_ANAT_ENTITY_IDS_COL_NAME + "</th>"
                + "<th>" + MULTI_ANAT_ENTITY_NAMES_COL_NAME + "</th>"
                + "<th>" + STAGE_ID_COL_NAME + "</th>"
                + "<th>" + STAGE_NAME_COL_NAME + "</th>"
                + "<th>" + OVER_EXPRESSED_FOR_SPECIES_COL_NAME + " Homo sapiens</th>"
                + "<th>" + UNDER_EXPRESSED_FOR_SPECIES_COL_NAME + " Homo sapiens</th>"
                + "<th>" + NOT_DIFF_EXPRESSED_FOR_SPECIES_COL_NAME + " Homo sapiens</th>"
                + "<th>" + NA_FOR_SPECIES_COL_NAME + " Homo sapiens</th>"
                + "<th>" + OVER_EXPRESSED_FOR_SPECIES_COL_NAME + " Mus musculus</th>"
                + "<th>" + UNDER_EXPRESSED_FOR_SPECIES_COL_NAME + " Mus musculus</th>"
                + "<th>" + NOT_DIFF_EXPRESSED_FOR_SPECIES_COL_NAME + " Mus musculus</th>"
                + "<th>" + NA_FOR_SPECIES_COL_NAME + " Mus musculus</th>"
                + "<th>" + GENE_IDS_COL_NAME + "</th>"
                + "<th>" + GENE_NAMES_COL_NAME + "</th>"
                + "</tr>"
                + "</thead>"
                + "<tbody>"
                + "<tr><td>93</td><td>UBERON:0000473</td><td>testis</td>"
                + "<td>UBERON:0000113</td><td>post-juvenile adult stage</td>"
                + "<td>0</td><td>1</td><td>0</td><td>0</td>"
                + "<td>0</td><td>1</td><td>0</td><td>0</td>"
                + "<td>ENSG00000162512|ENSMUSG00000025743</td><td>SDC3|Sdc3</td></tr>"
                + "<tr><td>93</td><td>UBERON:0000955</td><td>brain</td>"
                + "<td>UBERON:0000113</td><td>post-juvenile adult stage</td>"
                + "<td>1</td><td>0</td><td>0</td><td>0</td>"
                + "<td>1</td><td>0</td><td>0</td><td>0</td>"
                + "<td>ENSG00000162512|ENSMUSG00000025743</td><td>SDC3|Sdc3</td></tr>"
                + "<tr><td>93</td><td>UBERON:0001134</td><td>skeletal muscle tissue</td>"
                + "<td>UBERON:0000113</td><td>post-juvenile adult stage</td>"
                + "<td>0</td><td>1</td><td>0</td><td>0</td>"
                + "<td>0</td><td>1</td><td>0</td><td>0</td>"
                + "<td>ENSG00000162512|ENSMUSG00000025743</td><td>SDC3|Sdc3</td></tr>"
                + "</tbody>"
                + "</table>");
    }
    /**
     * @return  a {@code String} containing the HTML to create a table containing the description 
     *          of the header of a multi-species complete differential expression file (can be used 
     *          in "help" links).
     */
    public static String getMultiSpeciesCompleteDiffExprFileHeaderDesc() {
        log.entry();
        return log.exit("<table class='download_file_header_desc'>"
                + "<tbody>"
                + "<tr><td>1</td><td>2</td><td>3</td><td>4</td><td>5</td><td>6</td><td>7</td>"
                + "<td>8</td><td>9</td><td>10</td><td>11</td><td>12</td><td>13</td><td>14</td>"
                + "<td>15</td><td>16</td><td>17</td><td>18</td><td>19</td><td>20</td>"
                + "<td>21</td><td>22</td></tr>"
                + getMultiSpeciesCompleteDiffExprFileHeader(true)
                + "</tbody>"
                + "</table>");
    }
    /**
     * @return  a {@code String} containing the HTML to create a table containing the header 
     *          and example lines of a multi species complete differential expression file.
     */
    public static String getMultiSpeciesCompleteDiffExprFileExample() {
        log.entry();
        return log.exit("<table class='call_download_file_example'>"
                + "<caption>Example lines for multi-species complete differential expression file</caption>"
                + "<thead>" 
                + getMultiSpeciesCompleteDiffExprFileHeader(false) 
                + "</thead>"
                + "<tbody>"
                + "<tr><td>59</td><td>ENSMUSG00000030516</td><td>Tjp1</td>"
                + "<td>UBERON:0000948</td><td>heart</td>"
                + "<td>UBERON:0018241</td><td>prime adult stage</td>"
                + "<td>Mus_musculus</td>"
                + "<td>over-expression</td><td>high quality</td>"
                + "<td>over-expression</td><td>high quality</td><td>0.0</td>"
                + "<td>5</td><td>0</td><td>no data</td><td>no data</td><td>1.0</td>"
                + "<td>0</td><td>0</td><td>CIO:0000004</td>"
                + "<td>medium confidence from single evidence</td></tr>"
                + "<tr><td>59</td><td>ENSMMUG00000017878</td><td>Tjp1</td>"
                + "<td>UBERON:0000948</td><td>heart</td>"
                + "<td>UBERON:0018241</td><td>prime adult stage</td>"
                + "<td>Macaca_mulatta</td>"
                + "<td>no diff expression</td><td>high quality</td>"
                + "<td>no data</td><td>no data</td><td>1.0</td>"
                + "<td>0</td><td>0</td><td>no diff expression</td><td>high quality</td>"
                + "<td>0.6239275</td><td>2</td><td>0</td><td>CIO:0000004</td>"
                + "<td>medium confidence from single evidence</td></tr>"
                + "<tr><td>59</td><td>ENSBTAG00000015398</td><td>ZO1</td>"
                + "<td>UBERON:0000948</td><td>heart</td>"
                + "<td>UBERON:0018241</td><td>prime adult stage</td>"
                + "<td>Bos_taurus</td>"
                + "<td>over-expression</td><td>high quality</td>"
                + "<td>no data</td><td>no data</td><td>1.0</td>"
                + "<td>0</td><td>0</td><td>over-expression</td><td>high quality</td>"
                + "<td>8.741838E-4</td><td>1</td><td>0</td><td>CIO:0000004</td>"
                + "<td>medium confidence from single evidence</td></tr>"
                + "</tbody>"
                + "</table>");
    }
    /**
     * Get the header of multi-species complete differential expression file as a HTML 'tr' element, 
     * with column being either 'td' or 'th' elements depending on argument {@code withTd}.
     * @param withTd    A {@code boolean} defining whether the column type should be 'td' 
     *                  or 'th'. If {@code true}, 'td' is used.
     * @return          A {@code String} that is the header of multi-species complete differential 
     *                  expression file as a HTML 'tr' element.
     */
    private static String getMultiSpeciesCompleteDiffExprFileHeader(boolean withTd) {
        log.entry(withTd);
        String colType ="td";
        if (!withTd) {
            colType = "th";
        }
        return log.exit("<tr>"
                + "<" + colType + ">" + OMA_ID_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + GENE_ID_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + GENE_NAME_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + MULTI_ANAT_ENTITY_IDS_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + MULTI_ANAT_ENTITY_NAMES_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + STAGE_ID_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + STAGE_NAME_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + SPECIES_LATIN_NAME_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + DIFF_EXPR_STATE_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + EXPR_QUAL_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + AFFY_DIFF_EXPR_STATE_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + AFFY_EXPR_QUAL_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + AFFY_DIFF_EXPR_P_VAL_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + AFFY_DIFF_EXPR_SUPPORT_COUNT_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + AFFY_DIFF_EXPR_CONFLICT_COUNT_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + RNA_SEQ_DIFF_EXPR_STATE_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + RNA_SEQ_EXPR_QUAL_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + RNA_SEQ_DIFF_EXPR_P_VAL_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + RNA_SEQ_DIFF_EXPR_SUPPORT_COUNT_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + RNA_SEQ_DIFF_EXPR_CONFLICT_COUNT_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + ANAT_HOMOLOGY_CIO_ID_COL_NAME + "</" + colType + ">"
                + "<" + colType + ">" + ANAT_HOMOLOGY_CIO_NAME_COL_NAME + "</" + colType + ">"
                + "</tr>");
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
     * @param factory           The {@code HtmlFactory} that instantiated this object.
     * @throws IllegalArgumentException If {@code factory} is {@code null}.
     * @throws IOException              If there is an issue when trying to get or to use the
     *                                  {@code PrintWriter} 
     */
    protected HtmlDocumentationCallFile(HttpServletResponse response,
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
        
        this.writeln("<h1>Expression call download file documentation</h1>");
        RequestParameters urlDownloadGenerator = this.getNewRequestParameters();
        urlDownloadGenerator.setPage(RequestParameters.PAGE_DOWNLOAD);
        urlDownloadGenerator.setAction(RequestParameters.ACTION_DOWLOAD_CALL_FILES);
        this.writeln("<p class='documentationintro'>Bgee provides calls of baseline "
                + "presence/absence of expression, and of differential over-/under-expression, "
                + "either for single species, or compared between species (orthologous genes "
                + "in homologous organs). This documentation describes the format of these "
                + "<a href='" + urlDownloadGenerator.getRequestURL()
                + "' title='Bgee expression data page'>download files</a>.</p>");
        
        //Documentation menu
        this.writeDocMenuForCallDownloadFiles();
        
        //Single species documentation
        this.writeln("<div>");
        this.writeln("<h2 id='" + RequestParameters.HASH_DOC_CALL_SINGLE 
                + "'>Single-species download files</h2>");
        this.writeln("<div class='doc_content'>");
        this.writeln("<p>Jump to: </p>"
                + "<ul>"
                + "<li><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR 
                + "' title='Quick jump to presence/absence of expression'>"
                + "Presence/absence of expression</a></li>"
                + "<li><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_DIFF
                + "' title='Quick jump to differential expression'>"
                + "Over-/under-expression across anatomy or life stages</a></li>"
                + "</ul>");
        //presence/absence
        this.writeSingleSpeciesExprCallFileDoc();
        //over/under
        this.writeSingleSpeciesDiffExprCallFileDoc();
        this.writeln("</div>");   //end of doc_content
        
        this.writeln("</div>");// end of single-species
        
        //multi-species documentation
        this.writeln("<div>");
        this.writeln("<h2 id='" + RequestParameters.HASH_DOC_CALL_MULTI 
                + "'>Multi-species download files</h2>");
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
                + "by the Bgee team, see <a target='_blank' "
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
                + "orthology between genes are retrieved using <a target='_blank' "
                + "href='http://omabrowser.org/oma/hogs/' title='External link to OMA browser'>"
                + "OMA</a>; when comparing several species, "
                + "Bgee identifies their Least Common Ancestor (LCA), and retrieve genes "
                + "that have descended from a single common ancestral gene in that LCA. "
                + "Relations of orthology between genes are provided in Bgee through "
                + "<a href='#" + RequestParameters.HASH_DOC_CALL_OMA 
                + "' title='Jump to hierarchical orthologous groups "
                + "file documentation'>hierarchical orthologous groups files</a>.</li>"
                + "</ul>");
        this.writeln("<p>Jump to: </p>"
                + "<ul>"
                + "<li><a href='#" + RequestParameters.HASH_DOC_CALL_OMA 
                + "' title='Quick jump to OMA HOG file'>"
                + "OMA Hierarchical orthologous groups</a></li>"
//                + "<li><a href='#" + RequestParameters.HASH_DOC_CALL_MULTI_EXPR 
//                + "' title='Quick jump to presence/absence of expression'>"
//                + "Presence/absence of expression</a></li>"
                + "<li><a href='#" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF
                + "' title='Quick jump to differential expression'>"
                + "Over-/under-expression across anatomy or life stages</a></li>"
                + "</ul>");

        //OMA HOG file
        this.writeOMAGroupFileDoc();
        //over/under
        this.writeMultiSpeciesDiffExprCallFileDoc();
        this.writeln("</div>");   //end of doc_content
        this.writeln("</div>"); // end of multi-species download file

        log.exit();
    }
    

    /**
     * Write the documentation menu related to presence/absence and over/under expression call 
     * download files. Anchors used in this method for quick jump links 
     * have to stayed in sync with id attributes of h2, h3 and h4 tags defined in 
     * {@link #writeSingleSpeciesExprCallFileDoc()}, 
     * {@link #writeSingleSpeciesSimpleExprCallFileDoc()},  
     * {@link #writeSingleSpeciesCompleteExprCallFileDoc()}, and 
     * {@link #writeMultiSpeciesDiffExprCallFileDoc()}.
     * 
     * @see #writeSingleSpeciesExprCallFileDoc()
     * @see #writeSingleSpeciesSimpleExprCallFileDoc()
     * @see #writeSingleSpeciesCompleteExprCallFileDoc()
     * @see #writeMultiSpeciesDiffExprCallFileDoc()
     */
    private void writeDocMenuForCallDownloadFiles() {
        log.entry();
        
        this.writeln("<div class='documentationmenu'><ul>");
        //Single-species
        this.writeln("<li><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE
                + "' title='Quick jump to this section'>" + 
                "Single-species download files</a>");
        //presence/absence
        this.writeln("<ul>");           
        this.writeln("<li><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR 
                + "' title='Quick jump to this section'>" + 
                "Presence/absence of expression</a>");
        //Actually there explanations common to simple and complete files, so we don't provide
        //direct links to simple/common files, that would skip the common explanations.
//        this.writeln("<ul>");
//        this.writeln("<li><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR_SIMPLE 
//                + "' title='Quick jump to this section'>" + 
//                "Simple file</a></li>");
//        this.writeln("<li><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR_COMPLETE 
//                + "' title='Quick jump to this section'>" + 
//                "Complete file</a></li>");
//        this.writeln("</ul>");   
        this.writeln("</li>");              //end of presence/absence
        //diff expression
        this.writeln("<li><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_DIFF
                + "' title='Quick jump to this section'>" + 
                "Over-/under-expression across anatomy or life stages</a>");
        //Actually there explanations common to simple and complete files, so we don't provide
        //direct links to simple/common files, that would skip the common explanations.
//        this.writeln("<ul>");
//        this.writeln("<li><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_DIFF_SIMPLE 
//                + "' title='Quick jump to this section'>" + 
//                "Simple file</a></li>");
//        this.writeln("<li><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_DIFF_COMPLETE 
//                + "' title='Quick jump to this section'>" + 
//                "Complete file</a></li>");
//        this.writeln("</ul>"); 
        this.writeln("</li>");              //end of diff expression 
        this.writeln("</ul></li>");     // end of single-species section
        
        //multi-species
        this.writeln("<li><a href='#" + RequestParameters.HASH_DOC_CALL_MULTI
                + "' title='Quick jump to this section'>" + 
                "Multi-species download files</a>");
        //diff expression
        this.writeln("<ul>");    
        this.writeln("<li><a href='#" + RequestParameters.HASH_DOC_CALL_OMA 
                + "' title='Quick jump to OMA HOG file'>"
                + "OMA Hierarchical orthologous groups</a></li>");
        this.writeln("<li><a href='#" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF
                + "' title='Quick jump to this section'>" + 
                "Over-/under-expression across anatomy or life stages</a>");
        //Actually there explanations common to simple and complete files, so we don't provide
        //direct links to simple/common files, that would skip the common explanations.
//        this.writeln("<ul>");
//        this.writeln("<li><a href='#" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_SIMPLE 
//                + "' title='Quick jump to this section'>" + 
//                "Simple file</a></li>");
//        this.writeln("<li><a href='#" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_COMPLETE 
//                + "' title='Quick jump to this section'>" + 
//                "Complete file</a></li>");
//        this.writeln("</ul>");         
        this.writeln("</li>");              //end of diff expression        
        this.writeln("</ul></li>");     // end of multi-species section

        this.writeln("<li><a href='#troubleshooting' title='Quick jump to this section'>" + 
                "Troubleshooting</a>");
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
        this.writeln("<h3 id='" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR 
                + "'>Presence/absence of expression</h3>");
        //TODO: add link to data analyses documentation
        this.writeln(HtmlDocumentationDisplay.getExprCallExplanation());
        this.writeln("<p>After presence/absence calls are generated from the raw data, "
                + "they are propagated using anatomical and life stage ontologies: </p>"
                + "<ul class='doc_content'>"
                + "<li><span class='list_element_title'>calls of expression</span> "
                + "are propagated to parent anatomical entities "
                + "and parent developmental stages. For instance, if gene A is expressed "
                + "in midbrain at young adult stage, it will also be considered as expressed "
                + "in brain at adult stage.</li>"
                + "<li><span class='list_element_title'>calls of absence of expression</span> "
                + "are propagated to child anatomical entities "
                + "(and not to child developmental stages). For instance, if gene A is reported "
                + "as not expressed in the brain at young adult stage, it will also be considered "
                + "as not expressed in the midbrain at young adult stage. This is only permitted "
                + "when it does not generate any contradiction with expression calls from "
                + "the same data type (for instance, no contradiction permitted of reported "
                + "absence of expression by RNA-Seq, with report of expression by RNA-Seq "
                + "for the same gene, in the same anatomical entity and developmental stage, "
                + "or any child anatomical entity or child developmental stage).</li>"
                + "</ul>"
                + "<p>Call propagation allows a complete integration of the data, "
                + "even if provided at different anatomical or developmental levels. "
                + "For instance: if gene A is reported to be expressed in the midbrain dura mater "
                + "at young adult stage; gene B is reported to be expressed "
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
                + "<li><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR_SIMPLE 
                + "' title='Quick jump to simple file description'>"
                + "simple file</a></li>"
                + "<li><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR_COMPLETE 
                + "' title='Quick jump to complete file description'>"
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
        
        this.writeln("<h4 id='" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR_SIMPLE 
                + "'>Simple file</h4>");
        this.writeln("<p>In simple files, propagated presence/absence expression calls "
                + "are provided, but only calls in conditions of anatomical entity/developmental stage "
                + "actually used in experimental data are displayed (no calls generated "
                + "from propagation only).</p>");
        this.writeln("<table class='call_download_file_desc'>");
        this.writeln("<caption>Format description for single species simple expression file</caption>");
        this.writeln("<thead>");
        this.writeln("<tr><th>Column</th><th>Content</th><th>Example</th></tr>");
        this.writeln("</thead>");
        this.writeln("<tbody>");
        this.writeln("<tr><td>1</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR_SIMPLE 
                + "_col1' title='" 
                + GENE_ID_LINK_TITLE + "'>" + GENE_ID_COL_NAME 
                + "</a></td><td>FBgn0005427</td></tr>");
        this.writeln("<tr><td>2</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR_SIMPLE 
                + "_col2' title='" 
                + GENE_NAME_LINK_TITLE + "'>" + GENE_NAME_COL_NAME 
                + "</a></td><td>ewg</td></tr>");
        this.writeln("<tr><td>3</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR_SIMPLE 
                + "_col3' title='" 
                + ANAT_ENTITY_ID_LINK_TITLE + "'>" + ANAT_ENTITY_ID_COL_NAME 
                + "</a></td><td>FBbt:00003404</td></tr>");
        this.writeln("<tr><td>4</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR_SIMPLE 
                + "_col4' title='" 
                + ANAT_ENTITY_NAME_LINK_TITLE + "'>" + ANAT_ENTITY_NAME_COL_NAME 
                + "</a></td><td>mesothoracic extracoxal depressor muscle 66 (Drosophila)</td></tr>");
        this.writeln("<tr><td>5</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR_SIMPLE 
                + "_col5' title='" 
                + STAGE_ID_LINK_TITLE + "'>" + STAGE_ID_COL_NAME 
                + "</a></td><td>FBdv:00005348</td></tr>");
        this.writeln("<tr><td>6</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR_SIMPLE 
                + "_col6' title='" 
                + STAGE_NAME_LINK_TITLE + "'>" + STAGE_NAME_COL_NAME 
                + "</a></td><td>prepupal stage P4(ii) (Drosophila)</td></tr>");
        this.writeln("<tr><td>7</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR_SIMPLE 
                + "_col7' title='" 
                + EXPR_STATE_LINK_TITLE + "'>" + EXPR_STATE_COL_NAME 
                + "</a></td><td>present</td></tr>");
        this.writeln("<tr><td>8</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR_SIMPLE 
                + "_col8' title='" 
                + EXPR_QUAL_LINK_TITLE + "'>" + EXPR_QUAL_COL_NAME 
                + "</a></td><td>high quality</td></tr>");
        this.writeln("</tbody>");
        this.writeln("</table>");
        this.writeln(getSingleSpeciesSimpleExprFileExample());
        
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR_SIMPLE 
                + "_col1'>" + GENE_ID_COL_NAME + " (column 1)</h5>");
        this.writeln(getGeneIdColDescription());
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR_SIMPLE 
                + "_col2'>" + GENE_NAME_COL_NAME + " (column 2)</h5>");
        this.writeln(getGeneNameColDescription(1));
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR_SIMPLE 
                + "_col3'>" + ANAT_ENTITY_ID_COL_NAME + " (column 3)</h5>");
        this.writeln(getAnatEntityIdColDescription());
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR_SIMPLE 
                + "_col4'>" + ANAT_ENTITY_NAME_COL_NAME + " (column 4)</h5>");
        this.writeln(getAnatEntityNameColDescription(3));
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR_SIMPLE 
                + "_col5'>" + STAGE_ID_COL_NAME + " (column 5)</h5>");
        this.writeln(getStageIdColDescription());
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR_SIMPLE 
                + "_col6'>" + STAGE_NAME_COL_NAME + " (column 6)</h5>");
        this.writeln(getStageNameColDescription(4));
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR_SIMPLE 
                + "_col7'>" + EXPR_STATE_COL_NAME + " (column 7)</h5>");
        this.writeln(getExprStateColDescription(1, 5, 3, 8, true)); 
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR_SIMPLE 
                + "_col8'>" + EXPR_QUAL_COL_NAME + " (column 8)</h5>");
        this.writeln(getExprQualColDescription(7)); 
        this.writeln("<p><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR 
                + "'>Back to presence/absence of expression menu</a></p>");
        
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
        
        this.writeln("<h4 id='" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR_COMPLETE 
                + "'>Complete file</h4>");
        this.writeln("<p>The differences between simple and complete files are that, "
                + "in complete files: </p>"
                + "<ul class='doc_content'>"
                + "<li>details of expression status generated from each data type are provided.</li>"
                + "<li>all calls are provided, propagated to all possible anatomical entities "
                + "and developmental stages, including in conditions not annotated in experimental data "
                + "(calls generated from propagation only).</li>"
                + "<li>a column allows to determine whether a call was generated from propagation "
                + "only, or whether the anatomical entity/developmental stage was actually "
                + "seen in experimental data (such a call would then also be present "
                + "in simple file).</li>"
                + "</ul>");
        this.writeln("<table class='call_download_file_desc'>");
        this.writeln("<caption>Format description for single species complete expression file</caption>");
        this.writeln("<thead>");
        this.writeln("<tr><th>Column</th><th>Content</th><th>Example</th></tr>");
        this.writeln("</thead>");
        this.writeln("<tbody>");
        this.writeln("<tr><td>1</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR_COMPLETE 
                + "_col1' title='" 
                + GENE_ID_LINK_TITLE + "'>" + GENE_ID_COL_NAME 
                + "</a></td><td>ENSDARG00000070769</td></tr>");
        this.writeln("<tr><td>2</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR_COMPLETE 
                + "_col2' title='" 
                + GENE_NAME_LINK_TITLE + "'>" + GENE_NAME_COL_NAME 
                + "</a></td><td>foxg1a</td></tr>");
        this.writeln("<tr><td>3</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR_COMPLETE 
                + "_col3' title='" 
                + ANAT_ENTITY_ID_LINK_TITLE + "'>" + ANAT_ENTITY_ID_COL_NAME 
                + "</a></td><td>UBERON:0000955</td></tr>");
        this.writeln("<tr><td>4</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR_COMPLETE 
                + "_col4' title='" 
                + ANAT_ENTITY_NAME_LINK_TITLE + "'>" + ANAT_ENTITY_NAME_COL_NAME 
                + "</a></td><td>brain</td></tr>");
        this.writeln("<tr><td>5</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR_COMPLETE 
                + "_col5' title='" 
                + STAGE_ID_LINK_TITLE + "'>" + STAGE_ID_COL_NAME 
                + "</a></td><td>UBERON:0000113</td></tr>");
        this.writeln("<tr><td>6</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR_COMPLETE 
                + "_col6' title='" 
                + STAGE_NAME_LINK_TITLE + "'>" + STAGE_NAME_COL_NAME 
                + "</a></td><td>post-juvenile adult stage</td></tr>");
        this.writeln("<tr><td>7</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR_COMPLETE 
                + "_col7' title='" 
                + EXPR_STATE_LINK_TITLE + "'>" + EXPR_STATE_COL_NAME 
                + "</a></td><td>present</td></tr>");
        this.writeln("<tr><td>8</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR_COMPLETE 
                + "_col8' title='" 
                + EXPR_QUAL_LINK_TITLE + "'>" + EXPR_QUAL_COL_NAME 
                + "</a></td><td>high quality</td></tr>");
        this.writeln("<tr><td>9</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR_COMPLETE 
                + "_col9' title='" 
                + OBSERVED_DATA_LINK_TITLE + "'>" + OBSERVED_DATA_COL_NAME 
                + "</a></td><td>yes</td></tr>");
        this.writeln("<tr><td>10</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR_COMPLETE 
                + "_col10' title='" 
                + AFFY_EXPR_STATE_LINK_TITLE + "'>" + AFFY_EXPR_STATE_COL_NAME 
                + "</a></td><td>present</td></tr>");
        this.writeln("<tr><td>11</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR_COMPLETE 
                + "_col11' title='" 
                + AFFY_EXPR_QUAL_LINK_TITLE + "'>" + AFFY_EXPR_QUAL_COL_NAME 
                + "</a></td><td>high quality</td></tr>");
        this.writeln("<tr><td>12</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR_COMPLETE 
                + "_col12' title='" 
                + AFFY_OBSERVED_DATA_LINK_TITLE + "'>" + AFFY_OBSERVED_DATA_COL_NAME 
                + "</a></td><td>yes</td></tr>");
        this.writeln("<tr><td>13</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR_COMPLETE 
                + "_col13' title='" 
                + EST_EXPR_STATE_LINK_TITLE + "'>" + EST_EXPR_STATE_COL_NAME 
                + "</a></td><td>present</td></tr>");
        this.writeln("<tr><td>14</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR_COMPLETE 
                + "_col14' title='" 
                + EST_EXPR_QUAL_LINK_TITLE + "'>" + EST_EXPR_QUAL_COL_NAME 
                + "</a></td><td>poor quality</td></tr>");
        this.writeln("<tr><td>15</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR_COMPLETE 
                + "_col15' title='" 
                + EST_OBSERVED_DATA_LINK_TITLE + "'>" + EST_OBSERVED_DATA_COL_NAME 
                + "</a></td><td>yes</td></tr>");
        this.writeln("<tr><td>16</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR_COMPLETE 
                + "_col16' title='" 
                + IN_SITU_EXPR_STATE_LINK_TITLE + "'>" + IN_SITU_EXPR_STATE_COL_NAME 
                + "</a></td><td>present</td></tr>");
        this.writeln("<tr><td>17</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR_COMPLETE 
                + "_col17' title='" 
                + IN_SITU_EXPR_QUAL_LINK_TITLE + "'>" + IN_SITU_EXPR_QUAL_COL_NAME 
                + "</a></td><td>high quality</td></tr>");
        this.writeln("<tr><td>18</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR_COMPLETE 
                + "_col18' title='" 
                + IN_SITU_OBSERVED_DATA_LINK_TITLE + "'>" + IN_SITU_OBSERVED_DATA_COL_NAME 
                + "</a></td><td>yes</td></tr>");
        this.writeln("<tr><td>19</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR_COMPLETE 
                + "_col19' title='" 
                + RNA_SEQ_EXPR_STATE_LINK_TITLE + "'>" + RNA_SEQ_EXPR_STATE_COL_NAME 
                + "</a></td><td>no data</td></tr>");
        this.writeln("<tr><td>20</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR_COMPLETE 
                + "_col20' title='" 
                + RNA_SEQ_EXPR_QUAL_LINK_TITLE + "'>" + RNA_SEQ_EXPR_QUAL_COL_NAME 
                + "</a></td><td>no data</td></tr>");
        this.writeln("<tr><td>21</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR_COMPLETE 
                + "_col21' title='" 
                + RNA_SEQ_OBSERVED_DATA_LINK_TITLE + "'>" + RNA_SEQ_OBSERVED_DATA_COL_NAME 
                + "</a></td><td>no</td></tr>");
        this.writeln("</tbody>");
        this.writeln("</table>");
        this.writeln(getSingleSpeciesCompleteExprFileExample());
        
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR_COMPLETE 
                + "_col1'>" + GENE_ID_COL_NAME + " (column 1)</h5>");
        this.writeln(getGeneIdColDescription());
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR_COMPLETE 
                + "_col2'>" + GENE_NAME_COL_NAME + " (column 2)</h5>");
        this.writeln(getGeneNameColDescription(1));
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR_COMPLETE 
                + "_col3'>" + ANAT_ENTITY_ID_COL_NAME + " (column 3)</h5>");
        this.writeln(getAnatEntityIdColDescription());
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR_COMPLETE 
                + "_col4'>" + ANAT_ENTITY_NAME_COL_NAME + " (column 4)</h5>");
        this.writeln(getAnatEntityNameColDescription(3));
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR_COMPLETE 
                + "_col5'>" + STAGE_ID_COL_NAME + " (column 5)</h5>");
        this.writeln(getStageIdColDescription());
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR_COMPLETE 
                + "_col6'>" + STAGE_NAME_COL_NAME + " (column 6)</h5>");
        this.writeln(getStageNameColDescription(5));
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR_COMPLETE 
                + "_col7'>" + EXPR_STATE_COL_NAME + " (column 7)</h5>");
        this.writeln(getExprStateColDescription(1, 5, 3, 8, true)); 
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR_COMPLETE 
                + "_col8'>" + EXPR_QUAL_COL_NAME + " (column 8)</h5>");
        this.writeln(getExprQualColDescription(7));
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR_COMPLETE 
                + "_col9'>" + OBSERVED_DATA_COL_NAME + " (column 9)</h5>");
        this.writeln(getObservedDataColDescription(OBSERVED_DATA_COL_NAME));
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR_COMPLETE 
                + "_col10'>" + AFFY_EXPR_STATE_COL_NAME + " (column 10)</h5>");
        //TODO: add links to data analyses documentation
        this.writeln("<p>Call generated by Affymetrix data for " 
                + getColumnListForCall(1, 5, 3, true) + ". One of: </p>"
                + "<ul class='doc_content'>"
                + "<li><span class='list_element_title'>present</span>: "
                + "report of presence of expression from Bgee statistical tests. "
                + "See <code>" + AFFY_EXPR_QUAL_COL_NAME + "</code> (column 11) "
                + "for associated quality level.</li>"
                + "<li><span class='list_element_title'>absent</span>: "
                + "report of absence of expression from Bgee statistical tests, "
                + "with no contradicting call of presence of expression generated by other "
                + "Affymetrix probesets or chips for the same gene, in the same anatomical entity "
                + "and developmental stage, or in a child entity or child developmental stage.</li>"
                + "<li><span class='list_element_title'>no data</span>: no Affymetrix data "
                + "available for this gene/anatomical entity/developmental stage (data either "
                + "not available, or discarded by Bgee quality controls).</li>"
                + "</ul>");
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR_COMPLETE 
                + "_col11'>" + AFFY_EXPR_QUAL_COL_NAME + " (column 11)</h5>");
        //TODO: add links to data analyses documentation
        this.writeln("<p>Quality associated to the call in column <code>" +  AFFY_EXPR_STATE_COL_NAME
                + "</code> (column 10). One of: </p>"
                + "<ul class='doc_content'>"
                + "<li><span class='list_element_title'>high quality</span>: "
                    + "<ul>"
                    + "<li>In case of report of expression, expression reported as high quality "
                    + "from Bgee statistical tests, "
                    + "with no contradicting call of absence of expression for same gene, "
                    + "in same anatomical entity and developmental stage, that would have been "
                    + "generated by other Affymetrix probesets or chips "
                    + "(meaning that the call was either generated from multiple congruent data, "
                    + "or from a single probeset/chip).</li>"
                    + "<li>In case of report of absence of expression, call reported as high quality "
                    + "from Bgee statistical tests, with no contradicting call of presence "
                    + "of expression generated by other Affymetrix probesets or chips "
                    + "for the same gene, in the same anatomical entity "
                    + "and developmental stage, or in a child entity or child developmental stage.</li>"
                    + "</ul>"
                + "</li>"
                + "<li><span class='list_element_title'>poor quality</span>: "
                + "in case of report of expression, expression reported as low quality "
                + "either from Bgee statistical tests, "
                + "or because there exists a conflict of presence/absence of expression "
                + "for the same gene, anatomical entity and developmental stage, generated from "
                + "other Affymetrix probesets/chips.</li>"
                + "<li><span class='list_element_title'>no data</span>: no Affymetrix data "
                + "available for this gene/anatomical entity/developmental stage (data either "
                + "not available, or discarded by Bgee quality controls).</li>"
                + "</ul>");
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR_COMPLETE 
                + "_col12'>" + AFFY_OBSERVED_DATA_COL_NAME + " (column 12)</h5>");
        this.writeln(getObservedDataColDescription(AFFY_OBSERVED_DATA_COL_NAME));
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR_COMPLETE 
                + "_col13'>" + EST_EXPR_STATE_COL_NAME + " (column 13)</h5>");
        //TODO: add links to data analyses documentation
        this.writeln("<p>Call generated by EST data for " 
                + getColumnListForCall(1, 5, 3, true) + ". Note that EST data are not used "
                + "to produce calls of absence of expression. One of: </p>"
                + "<ul class='doc_content'>"
                + "<li><span class='list_element_title'>present</span>: "
                + "expression reported from Bgee statistical tests. "
                + "See <code>" + EST_EXPR_QUAL_COL_NAME + "</code> (column 14) "
                + "for associated quality level.</li>"
                + "<li><span class='list_element_title'>no data</span>: no EST data "
                + "available for this gene/anatomical entity/developmental stage (data either "
                + "not available, or discarded by Bgee quality controls).</li>"
                + "</ul>");
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR_COMPLETE 
                + "_col14'>" + EST_EXPR_QUAL_COL_NAME + " (column 14)</h5>");
        //TODO: add links to data analyses documentation
        this.writeln("<p>Quality associated to the call in column <code>" +  EST_EXPR_STATE_COL_NAME
                + "</code> (column 13). One of: </p>"
                + "<ul class='doc_content'>"
                + "<li><span class='list_element_title'>high quality</span>: "
                + "expression reported as high quality from Bgee statistical tests.</li>"
                + "<li><span class='list_element_title'>poor quality</span>: "
                + "expression reported as poor quality from Bgee statistical tests.</li>"
                + "<li><span class='list_element_title'>no data</span>: no EST data "
                + "available for this gene/anatomical entity/developmental stage (data either "
                + "not available, or discarded by Bgee quality controls).</li>"
                + "</ul>");
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR_COMPLETE 
                + "_col15'>" + EST_OBSERVED_DATA_COL_NAME + " (column 15)</h5>");
        this.writeln(getObservedDataColDescription(EST_OBSERVED_DATA_COL_NAME));
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR_COMPLETE 
                + "_col16'>" + IN_SITU_EXPR_STATE_COL_NAME + " (column 16)</h5>");
        //TODO: add links to data analyses documentation
        this.writeln("<p>Call generated by <i>in situ</i> data for " 
                + getColumnListForCall(1, 5, 3, true) + ". One of: </p>"
                + "<ul class='doc_content'>"
                + "<li><span class='list_element_title'>present</span>: "
                + "report of presence of expression from <i>in situ</i> data sources. "
                + "See <code>" + IN_SITU_EXPR_QUAL_COL_NAME 
                + "</code> (column 17) for associated quality level.</li>"
                + "<li><span class='list_element_title'>absent</span>: "
                + "report of absence of expression from <i>in situ</i> data sources, "
                + "with no contradicting call of presence of expression generated by other "
                + "<i>in situ</i> hybridization evidence lines "
                + "for the same gene, in the same anatomical entity "
                + "and developmental stage, or in a child entity or child developmental stage.</li>"
                + "<li><span class='list_element_title'>no data</span>: no <i>in situ</i> data "
                + "available for this gene/anatomical entity/developmental stage (data either "
                + "not available, or discarded by Bgee quality controls).</li>"
                + "</ul>");
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR_COMPLETE 
                + "_col17'>" + IN_SITU_EXPR_QUAL_COL_NAME + " (column 17)</h5>");
        //TODO: add links to data analyses documentation
        this.writeln("<p>Quality associated to the call in column <code>" + IN_SITU_EXPR_STATE_COL_NAME
                + "</code> (column 16). One of: </p>"
                + "<ul class='doc_content'>"
                + "<li><span class='list_element_title'>high quality</span>: "
                    + "<ul>"
                    + "<li>In case of report of expression, expression reported as high quality "
                    + "from <i>in situ</i> data sources, "
                    + "with no contradicting call of absence of expression for same gene, "
                    + "in same anatomical entity and developmental stage "
                    + "(meaning that the call was either generated from multiple congruent "
                    + "<i>in situ</i> hybridization evidence lines, or from a single hybridization).</li>"
                    + "<li>In case of report of absence of expression, call reported as high quality "
                    + "from <i>in situ</i> data sources, "
                    + "with no contradicting call of presence of expression generated by other "
                    + "<i>in situ</i> hybridization evidence lines "
                    + "for the same gene, in the same anatomical entity "
                    + "and developmental stage, or in a child entity or child developmental stage.</li>"
                    + "</ul>"
                + "</li>"
                + "<li><span class='list_element_title'>poor quality</span>: "
                + "in case of report of expression, expression reported as low quality "
                + "either from <i>in situ</i> data sources, "
                + "or because there exists a conflict of presence/absence of expression "
                + "for the same gene, anatomical entity and developmental stage, generated from "
                + "different <i>in situ</i> hybridization evidence lines.</li>"
                + "<li><span class='list_element_title'>no data</span>: no <i>in situ</i> data "
                + "available for this gene/anatomical entity/developmental stage (data either "
                + "not available, or discarded by Bgee quality controls).</li>"
                + "</ul>");
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR_COMPLETE 
                + "_col18'>" + IN_SITU_OBSERVED_DATA_COL_NAME 
                + " (column 18)</h5>");
        this.writeln(getObservedDataColDescription(IN_SITU_OBSERVED_DATA_COL_NAME));
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR_COMPLETE 
                + "_col19'>" + RNA_SEQ_EXPR_STATE_COL_NAME + " (column 19)</h5>");
        //TODO: add links to data analyses documentation
        this.writeln("<p>Call generated by RNA-Seq data for " 
                + getColumnListForCall(1, 5, 3, true) + ". One of: </p>"
                + "<ul class='doc_content'>"
                + "<li><span class='list_element_title'>present</span>: "
                + "report of presence of expression from Bgee statistical tests. "
                + "See <code>" + RNA_SEQ_EXPR_QUAL_COL_NAME + "</code> (column 20) "
                + "for associated quality level.</li>"
                + "<li><span class='list_element_title'>absent</span>: "
                + "report of absence of expression from Bgee statistical tests, "
                + "with no contradicting call of presence of expression generated by other "
                + "RNA-Seq libraries for the same gene, in the same anatomical entity "
                + "and developmental stage, or in a child entity or child developmental stage.</li>"
                + "<li><span class='list_element_title'>no data</span>: no RNA-Seq data "
                + "available for this gene/anatomical entity/developmental stage (data either "
                + "not available, or discarded by Bgee quality controls).</li>"
                + "</ul>");
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR_COMPLETE 
                + "_col20'>" + RNA_SEQ_EXPR_QUAL_COL_NAME + " (column 20)</h5>");
        //TODO: add links to data analyses documentation
        this.writeln("<p>Quality associated to the call in column <code>" +  RNA_SEQ_EXPR_STATE_COL_NAME
                + "</code> (column 19). One of: </p>"
                + "<ul class='doc_content'>"
                + "<li><span class='list_element_title'>high quality</span>: "
                    + "<ul>"
                    + "<li>In case of report of expression, expression reported as high quality "
                    + "from Bgee statistical tests, "
                    + "with no contradicting call of absence of expression for same gene, "
                    + "in same anatomical entity and developmental stage, that would have been "
                    + "generated from other RNA-Seq libraries (meaning that the call was either "
                    + "generated from several libraries providing congruent results, "
                    + "or from a single library).</li>"
                    + "<li>In case of report of absence of expression, call reported as high quality "
                    + "from Bgee statistical tests, with no contradicting call of presence "
                    + "of expression generated by other RNA-Seq libraries "
                    + "for the same gene, in the same anatomical entity "
                    + "and developmental stage, or in a child entity or child developmental stage.</li>"
                    + "</ul>"
                + "</li>"
                + "<li><span class='list_element_title'>poor quality</span>: "
                + "in case of report of expression, expression reported as low quality "
                + "either from Bgee statistical tests, "
                + "or because there exists a conflict of presence/absence of expression "
                + "for the same gene, anatomical entity and developmental stage, generated from "
                + "other RNA-Seq libraries.</li>"
                + "<li><span class='list_element_title'>no data</span>: no RNA-Seq data "
                + "available for this gene/anatomical entity/developmental stage (data either "
                + "not available, or discarded by Bgee quality controls).</li>"
                + "</ul>");
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR_COMPLETE 
                + "_col21'>" + RNA_SEQ_OBSERVED_DATA_COL_NAME 
                + " (column 21)</h5>");
        this.writeln(getObservedDataColDescription(RNA_SEQ_OBSERVED_DATA_COL_NAME));
        this.writeln("<p><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_EXPR 
                + "'>Back to presence/absence of expression menu</a></p>");
        this.writeln("<p>This corresponds to the same expression state summary column "
                + "as in simple files (column 7 of presence/absence simple file)</p>");
        
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
        this.writeln("<h3 id='" + RequestParameters.HASH_DOC_CALL_SINGLE_DIFF 
                + "'>Over-/under-expression across anatomy or life stages</h3>");
        //TODO: add link to data analyses documentation
        this.writeln(HtmlDocumentationDisplay.getDiffExprCallExplanation());
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
                + "<li><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_DIFF_SIMPLE 
                + "' title='Quick jump to simple file description'>"
                + "simple file</a></li>"
                + "<li><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_DIFF_COMPLETE 
                + "' title='Quick jump to complete file description'>"
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
        
        this.writeln("<h4 id='" + RequestParameters.HASH_DOC_CALL_SINGLE_DIFF_SIMPLE 
                + "'>Simple file</h4>");
        this.writeln("<p>In simple files, only calls of over-expression and under-expression "
                + "are provided, summarizing the contribution "
                + "of each data type to the call.</p>");
        this.writeln("<table class='call_download_file_desc'>");
        this.writeln("<caption>Format description for single species simple differential expression file</caption>");
        this.writeln("<thead>");
        this.writeln("<tr><th>Column</th><th>Content</th><th>Example</th></tr>");
        this.writeln("</thead>");
        this.writeln("<tbody>");
        this.writeln("<tr><td>1</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_DIFF_SIMPLE 
                + "_col1' title='" 
                + GENE_ID_LINK_TITLE + "'>" + GENE_ID_COL_NAME 
                + "</a></td><td>ENSG00000000419</td></tr>");
        this.writeln("<tr><td>2</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_DIFF_SIMPLE 
                + "_col2' title='" 
                + GENE_NAME_LINK_TITLE + "'>" + GENE_NAME_COL_NAME 
                + "</a></td><td>DPM1</td></tr>");
        this.writeln("<tr><td>3</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_DIFF_SIMPLE 
                + "_col3' title='" 
                + ANAT_ENTITY_ID_LINK_TITLE + "'>" + ANAT_ENTITY_ID_COL_NAME 
                + "</a></td><td>UBERON:0009834</td></tr>");
        this.writeln("<tr><td>4</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_DIFF_SIMPLE 
                + "_col4' title='" 
                + ANAT_ENTITY_NAME_LINK_TITLE + "'>" + ANAT_ENTITY_NAME_COL_NAME 
                + "</a></td><td>dorsolateral prefrontal cortex</td></tr>");
        this.writeln("<tr><td>5</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_DIFF_SIMPLE 
                + "_col5' title='" 
                + STAGE_ID_LINK_TITLE + "'>" + STAGE_ID_COL_NAME 
                + "</a></td><td>HsapDv:0000083</td></tr>");
        this.writeln("<tr><td>6</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_DIFF_SIMPLE 
                + "_col6' title='" 
                + STAGE_NAME_LINK_TITLE + "'>" + STAGE_NAME_COL_NAME 
                + "</a></td><td>infant stage (human)</td></tr>");
        this.writeln("<tr><td>7</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_DIFF_SIMPLE 
                + "_col7' title='" 
                + DIFF_EXPR_STATE_LINK_TITLE + "'>" + DIFF_EXPR_STATE_COL_NAME 
                + "</a></td><td>under-expression</td></tr>");
        this.writeln("<tr><td>8</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_DIFF_SIMPLE 
                + "_col8' title='" 
                + EXPR_QUAL_LINK_TITLE + "'>" + EXPR_QUAL_COL_NAME 
                + "</a></td><td>high quality</td></tr>");
        this.writeln("</tbody>");
        this.writeln("</table>");
        this.writeln(getSingleSpeciesSimpleDiffExprFileExample());
        
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_SINGLE_DIFF_SIMPLE 
                + "_col1'>" + GENE_ID_COL_NAME + " (column 1)</h5>");
        this.writeln(getGeneIdColDescription());
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_SINGLE_DIFF_SIMPLE 
                + "_col2'>" + GENE_NAME_COL_NAME + " (column 2)</h5>");
        this.writeln(getGeneNameColDescription(1));
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_SINGLE_DIFF_SIMPLE 
                + "_col3'>" + ANAT_ENTITY_ID_COL_NAME + " (column 3)</h5>");
        this.writeln(getAnatEntityIdColDescription());
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_SINGLE_DIFF_SIMPLE 
                + "_col4'>" + ANAT_ENTITY_NAME_COL_NAME + " (column 4)</h5>");
        this.writeln(getAnatEntityNameColDescription(3));
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_SINGLE_DIFF_SIMPLE 
                + "_col5'>" + STAGE_ID_COL_NAME + " (column 5)</h5>");
        this.writeln(getStageIdColDescription());
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_SINGLE_DIFF_SIMPLE 
                + "_col6'>" + STAGE_NAME_COL_NAME + " (column 6)</h5>");
        this.writeln(getStageNameColDescription(5));
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_SINGLE_DIFF_SIMPLE 
                + "_col7'>" + DIFF_EXPR_STATE_COL_NAME + " (column 7)</h5>");
        this.writeln(getDiffExprStateColDescription(1, 5, 3, true, false, true, false, "all data types")); 
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_SINGLE_DIFF_SIMPLE 
                + "_col8'>" + EXPR_QUAL_COL_NAME + " (column 8)</h5>");
        this.writeln(getDiffExprQualColDescription(DIFF_EXPR_STATE_COL_NAME, 7, true, false)); 
        this.writeln("<p><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_DIFF
                + "'>Back to over-/under-expression menu</a></p>");
        
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
        
        this.writeln("<h4 id='" + RequestParameters.HASH_DOC_CALL_SINGLE_DIFF_COMPLETE 
                + "'>Complete file</h4>");
        this.writeln("<p>The differences between simple and complete files are that, "
                + "in complete files: </p>"
                + "<ul class='doc_content'>"
                + "<li>details of the contribution of each data type to the final calls "
                + "are provided, notably with information about best p-values, or number "
                + "of supporting/conflicting analyses.</li>"
                + "<li>calls representing absence of differential expression are provided, "
                + "allowing to determine all genes and conditions tested for differential "
                + "expression.</li>"
                + "</ul>");
        this.writeln("<table class='call_download_file_desc'>");
        this.writeln("<caption>Format description for single species complete differential expression file</caption>");
        this.writeln("<thead>");
        this.writeln("<tr><th>Column</th><th>Content</th><th>Example</th></tr>");
        this.writeln("</thead>");
        this.writeln("<tbody>");
        this.writeln("<tr><td>1</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_DIFF_COMPLETE 
                + "_col1' title='" 
                + GENE_ID_LINK_TITLE + "'>" + GENE_ID_COL_NAME 
                + "</a></td><td>ENSMUSG00000093930</td></tr>");
        this.writeln("<tr><td>2</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_DIFF_COMPLETE 
                + "_col2' title='" 
                + GENE_NAME_LINK_TITLE + "'>" + GENE_NAME_COL_NAME 
                + "</a></td><td>Hmgcs1</td></tr>");
        this.writeln("<tr><td>3</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_DIFF_COMPLETE 
                + "_col3' title='" 
                + ANAT_ENTITY_ID_LINK_TITLE + "'>" + ANAT_ENTITY_ID_COL_NAME 
                + "</a></td><td>UBERON:0002107</td></tr>");
        this.writeln("<tr><td>4</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_DIFF_COMPLETE 
                + "_col4' title='" 
                + ANAT_ENTITY_NAME_LINK_TITLE + "'>" + ANAT_ENTITY_NAME_COL_NAME 
                + "</a></td><td>liver</td></tr>");
        this.writeln("<tr><td>5</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_DIFF_COMPLETE 
                + "_col5' title='" 
                + STAGE_ID_LINK_TITLE + "'>" + STAGE_ID_COL_NAME 
                + "</a></td><td>UBERON:0000113</td></tr>");
        this.writeln("<tr><td>6</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_DIFF_COMPLETE 
                + "_col6' title='" 
                + STAGE_NAME_LINK_TITLE + "'>" + STAGE_NAME_COL_NAME 
                + "</a></td><td>post-juvenile adult stage</td></tr>");
        this.writeln("<tr><td>7</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_DIFF_COMPLETE 
                + "_col7' title='" 
                + DIFF_EXPR_STATE_LINK_TITLE + "'>" + DIFF_EXPR_STATE_COL_NAME 
                + "</a></td><td>over-expression</td></tr>");
        this.writeln("<tr><td>8</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_DIFF_COMPLETE 
                + "_col8' title='" 
                + EXPR_QUAL_LINK_TITLE + "'>" + EXPR_QUAL_COL_NAME 
                + "</a></td><td>high quality</td></tr>");
        this.writeln("<tr><td>9</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_DIFF_COMPLETE 
                + "_col9' title='" 
                + "See " + AFFY_DIFF_EXPR_STATE_COL_NAME + " column description'>" 
                + AFFY_DIFF_EXPR_STATE_COL_NAME 
                + "</a></td><td>over-expression</td></tr>");
        this.writeln("<tr><td>10</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_DIFF_COMPLETE 
                + "_col10' title='" 
                + "See " + AFFY_EXPR_QUAL_COL_NAME + " column description'>" 
                + AFFY_EXPR_QUAL_COL_NAME
        //TODO: change 'poor' to 'low'
                + "</a></td><td>poor quality</td></tr>");
        this.writeln("<tr><td>11</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_DIFF_COMPLETE 
                + "_col11' title='" 
                + "See " + AFFY_DIFF_EXPR_P_VAL_COL_NAME + " column description'>" 
                + AFFY_DIFF_EXPR_P_VAL_COL_NAME 
                + "</a></td><td>0.0035659347</td></tr>");
        this.writeln("<tr><td>12</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_DIFF_COMPLETE 
                + "_col12' title='" 
                + "See " + AFFY_DIFF_EXPR_SUPPORT_COUNT_COL_NAME + " column description'>" 
                + AFFY_DIFF_EXPR_SUPPORT_COUNT_COL_NAME 
                + "</a></td><td>1</td></tr>");
        this.writeln("<tr><td>13</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_DIFF_COMPLETE 
                + "_col13' title='" 
                + "See " + AFFY_DIFF_EXPR_CONFLICT_COUNT_COL_NAME + " column description'>" 
                + AFFY_DIFF_EXPR_CONFLICT_COUNT_COL_NAME 
                + "</a></td><td>1</td></tr>");
        this.writeln("<tr><td>14</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_DIFF_COMPLETE 
                + "_col14' title='" 
                + "See " + RNA_SEQ_DIFF_EXPR_STATE_COL_NAME + " column description'>" 
                + RNA_SEQ_DIFF_EXPR_STATE_COL_NAME 
                + "</a></td><td>over-expression</td></tr>");
        this.writeln("<tr><td>15</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_DIFF_COMPLETE 
                + "_col15' title='" 
                + "See " + RNA_SEQ_EXPR_QUAL_COL_NAME + " column description'>" 
                + RNA_SEQ_EXPR_QUAL_COL_NAME 
                + "</a></td><td>high quality</td></tr>");
        this.writeln("<tr><td>16</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_DIFF_COMPLETE 
                + "_col16' title='" 
                + "See " + RNA_SEQ_DIFF_EXPR_P_VAL_COL_NAME + " column description'>" 
                + RNA_SEQ_DIFF_EXPR_P_VAL_COL_NAME 
                + "</a></td><td>2.96E-8</td></tr>");
        this.writeln("<tr><td>17</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_DIFF_COMPLETE 
                + "_col17' title='" 
                + "See " + RNA_SEQ_DIFF_EXPR_SUPPORT_COUNT_COL_NAME + " column description'>" 
                + RNA_SEQ_DIFF_EXPR_SUPPORT_COUNT_COL_NAME 
                + "</a></td><td>2</td></tr>");
        this.writeln("<tr><td>18</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_DIFF_COMPLETE 
                + "_col18' title='" 
                + "See " + RNA_SEQ_DIFF_EXPR_CONFLICT_COUNT_COL_NAME + " column description'>" 
                + RNA_SEQ_DIFF_EXPR_CONFLICT_COUNT_COL_NAME 
                + "</a></td><td>0</td></tr>");
        this.writeln("</tbody>");
        this.writeln("</table>");
        this.writeln(getSingleSpeciesCompleteDiffExprFileExample());
        
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_SINGLE_DIFF_COMPLETE 
                + "_col1'>" + GENE_ID_COL_NAME + " (column 1)</h5>");
        this.writeln(getGeneIdColDescription());
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_SINGLE_DIFF_COMPLETE 
                + "_col2'>" + GENE_NAME_COL_NAME + " (column 2)</h5>");
        this.writeln(getGeneNameColDescription(1));
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_SINGLE_DIFF_COMPLETE 
                + "_col3'>" + ANAT_ENTITY_ID_COL_NAME + " (column 3)</h5>");
        this.writeln(getAnatEntityIdColDescription());
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_SINGLE_DIFF_COMPLETE 
                + "_col4'>" + ANAT_ENTITY_NAME_COL_NAME + " (column 4)</h5>");
        this.writeln(getAnatEntityNameColDescription(3));
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_SINGLE_DIFF_COMPLETE 
                + "_col5'>" + STAGE_ID_COL_NAME + " (column 5)</h5>");
        this.writeln(getStageIdColDescription());
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_SINGLE_DIFF_COMPLETE 
                + "_col6'>" + STAGE_NAME_COL_NAME + " (column 6)</h5>");
        this.writeln(getStageNameColDescription(5));
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_SINGLE_DIFF_COMPLETE 
                + "_col7'>" + DIFF_EXPR_STATE_COL_NAME + " (column 7)</h5>");
        this.writeln(getDiffExprStateColDescription(1, 3, 5, true, true, true, false, "all data types")); 
        this.writeln("<p>This corresponds to the same differential expression state summary column "
                + "as in simple files (column 7 of over-/under-expression simple file)</p>");
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_SINGLE_DIFF_COMPLETE 
                + "_col8'>" + EXPR_QUAL_COL_NAME + " (column 8)</h5>");
        this.writeln(getDiffExprQualColDescription(DIFF_EXPR_STATE_COL_NAME, 7, true, false)); 
        this.writeln("<p>This corresponds to the same differential expression quality column "
                + "as in simple files (column 8 of over-/under-expression simple file)</p>");
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_SINGLE_DIFF_COMPLETE 
                + "_col9'>" + AFFY_DIFF_EXPR_STATE_COL_NAME + " (column 9)</h5>");
        this.writeln(getDiffExprStateColDescription(1, 3, 5, true, true, false, true, "Affymetrix data")); 
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_SINGLE_DIFF_COMPLETE 
                + "_col10'>" + AFFY_EXPR_QUAL_COL_NAME + " (column 10)</h5>");
        this.writeln(getDiffExprQualColDescription(AFFY_DIFF_EXPR_STATE_COL_NAME, 9, false, true)); 
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_SINGLE_DIFF_COMPLETE 
                + "_col11'>" + AFFY_DIFF_EXPR_P_VAL_COL_NAME 
                + " (column 11)</h5>");
        this.writeln(getDiffExprPvalColDescription(
                "Affymetrix", AFFY_DIFF_EXPR_STATE_COL_NAME, 9));
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_SINGLE_DIFF_COMPLETE 
                + "_col12'>" + AFFY_DIFF_EXPR_SUPPORT_COUNT_COL_NAME 
                + " (column 12)</h5>");
        this.writeln(getDiffSupportCountColDescription(
                "Affymetrix", AFFY_DIFF_EXPR_STATE_COL_NAME, 9));
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_SINGLE_DIFF_COMPLETE 
                + "_col13'>" + AFFY_DIFF_EXPR_CONFLICT_COUNT_COL_NAME 
                + " (column 13)</h5>");
        this.writeln(getDiffConflictCountColDescription(
                "Affymetrix", AFFY_DIFF_EXPR_STATE_COL_NAME, 9));
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_SINGLE_DIFF_COMPLETE 
                + "_col14'>" + RNA_SEQ_DIFF_EXPR_STATE_COL_NAME 
                + " (column 14)</h5>");
        this.writeln(getDiffExprStateColDescription(1, 3, 5, true, true, false, true, "RNA-Seq data")); 
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_SINGLE_DIFF_COMPLETE 
                + "_col15'>" + RNA_SEQ_EXPR_QUAL_COL_NAME 
                + " (column 15)</h5>");
        this.writeln(getDiffExprQualColDescription(RNA_SEQ_DIFF_EXPR_STATE_COL_NAME, 14, false, true)); 
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_SINGLE_DIFF_COMPLETE 
                + "_col16'>" + RNA_SEQ_DIFF_EXPR_P_VAL_COL_NAME 
                + " (column 16)</h5>");
        this.writeln(getDiffExprPvalColDescription(
                "RNA-Seq", RNA_SEQ_DIFF_EXPR_STATE_COL_NAME, 14));
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_SINGLE_DIFF_COMPLETE 
                + "_col17'>" + RNA_SEQ_DIFF_EXPR_SUPPORT_COUNT_COL_NAME 
                + " (column 17)</h5>");
        this.writeln(getDiffSupportCountColDescription(
                "RNA-Seq", RNA_SEQ_DIFF_EXPR_STATE_COL_NAME, 14));
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_SINGLE_DIFF_COMPLETE 
                + "_col18'>" + RNA_SEQ_DIFF_EXPR_CONFLICT_COUNT_COL_NAME 
                + " (column 18)</h5>");
        this.writeln(getDiffConflictCountColDescription(
                "RNA-Seq", RNA_SEQ_DIFF_EXPR_STATE_COL_NAME, 14));
        
        this.writeln("<p><a href='#" + RequestParameters.HASH_DOC_CALL_SINGLE_DIFF
                + "'>Back to over-/under-expression menu</a></p>");
        
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
        this.writeln("<h3 id='" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF
                + "'>Over-/under-expression across anatomy or life stages in multiple species</h3>");
        //TODO: add link to data analyses documentation
        this.writeln(HtmlDocumentationDisplay.getDiffExprCallExplanation());
        this.writeln("<p>In multi-species files, results are made comparable "
                + "between orthologous genes, in homologous anatomical entities and comparable "
                + "developmental stages: only genes sharing a common ancestral gene "
                + "in the least common ancestor of the species compared are studied, "
                + "and only in anatomical entities sharing a homology relation between "
                + "all species compared, with data mapped to broad developmental stages "
                + "shared across animal kingdom (see <a href='#" 
                + RequestParameters.HASH_DOC_CALL_MULTI              + "' "
                + "title='Quick jump to multi-species file description'>"
                + "use of homology in multi-species files</a>).</p>");
        this.writeln("<p>Note that, as opposed to calls of presence/absence of expression, "
                + "no propagation of differential expression calls is performed "
                + "using anatomical and life stage ontologies.</p>");
        this.writeln("<p>Over-/under-expression calls are then filtered and presented differently "
                + "depending on whether a <code>simple file</code>, "
                + "or a <code>complete file</code> is used. Notably: <code>simple files</code> "
                + "aim at providing one line per gene orthology group and homologous "
                + "anatomical entities/developmental stage, and only for anatomical entities "
                + "with a homology relation defined with good level of confidence. "
                + "<code>complete files</code> aim at reporting all information, for each gene "
                + "of the orthology groups, using all available homology relations between "
                + "anatomical entities, and allowing for instance to retrieve the contribution "
                + "of each data type to a call, or to retrieve all genes and conditions tested, "
                + "including genes having no differential expression in these conditions.</p>");
        this.writeln("<p>Jump to format description for: </p>"
                + "<ul>"
                + "<li><a href='#" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_SIMPLE 
                + "' title='Quick jump to simple file description'>"
                + "simple file</a></li>"
                + "<li><a href='#" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_COMPLETE 
                + "' title='Quick jump to complete file description'>"
                + "complete file</a></li>"
                + "</ul>");
        
        //simple diff expression file
        this.writeMultiSpeciesSimpleDiffExprCallFileDoc();
        
        //complete diff expression file
        this.writeMultiSpeciesCompleteDiffExprCallFileDoc(); 
        
        
        log.exit();
    }
    /**
     * Write the documentation related to OMA Groups 
     * download files. The id attribute used in h3-h4 tag must stay in sync with anchors used 
     * in quick jump links defined in method {@link #writeMultiSpeciesDiffExprCallFileDoc()}.
     * If the header of this file changes, {@link #getOMAGroupFileHeaderDesc()} 
     * must be updated.
     * 
     * @see #writeMultiSpeciesDiffExprCallFileDoc()
     * @see #getOMAGroupFileHeaderDesc()
     */
    private void writeOMAGroupFileDoc() {
        log.entry();
        
        this.writeln("<h3 id='" + RequestParameters.HASH_DOC_CALL_OMA 
                + "'>OMA Hierarchical orthologous groups file</h3>");
        this.writeln("<p>OMA Hierarchical orthologous groups files provide "
                + "gene orthology relations, by grouping genes that have descended "
                + "from a single common ancestral gene in the taxon of interest. The targeted "
                + "taxon is provided in the file name. Orthologous genes are grouped "
                + "by common OMA IDs, provided in the column " + OMA_ID_COL_NAME 
                + " (column 1, see below).</p>");
        this.writeln("<table class='call_download_file_desc'>");
        this.writeln("<caption>Format description for OMA Hierarchical orthologous groups file</caption>");
        this.writeln("<thead>");
        this.writeln("<tr><th>Column</th><th>Content</th><th>Example</th></tr>");
        this.writeln("</thead>");
        this.writeln("<tbody>");
        this.writeln("<tr><td>1</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_OMA 
                + "_col1' title='" 
                + "See " + OMA_ID_COL_NAME + " column description'>" + OMA_ID_COL_NAME 
                + "</a></td><td>10</td></tr>");
        this.writeln("<tr><td>2</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_OMA 
                + "_col2' title='" 
                + GENE_ID_LINK_TITLE + "'>" + GENE_ID_COL_NAME 
                + "</a></td><td>ENSG00000105298</td></tr>");
        this.writeln("<tr><td>3</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_OMA 
                + "_col3' title='" 
                + GENE_NAME_LINK_TITLE + "'>" + GENE_NAME_COL_NAME 
                + "</a></td><td>CACTIN</td></tr>");
        this.writeln("</tbody>");
        this.writeln("</table>");
        this.writeln(getOMAGroupFileExample());

        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_OMA 
                + "_col1'>" + OMA_ID_COL_NAME + " (column 1)</h5>");
        this.writeln(getOMAIdColDescription());
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_OMA 
                + "_col2'>" + GENE_ID_COL_NAME + " (column 2)</h5>");
        this.writeln(getGeneIdColDescription());
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_OMA 
                + "_col3'>" + GENE_NAME_COL_NAME + " (column 3)</h5>");
        this.writeln(getGeneNameColDescription(2));

        this.writeln("<p><a href='#" + RequestParameters.HASH_DOC_CALL_MULTI
                + "'>Back to multi-species download files menu</a></p>");
        
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
        
        this.writeln("<h4 id='" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_SIMPLE 
                + "'>Simple file</h4>");
        this.writeln("<p>In simple files, each line provides information for a gene orthology group, "
                + "in a condition (homologous anatomical entity/comparable developmental stage); "
                + "columns then provide, for each species, the number of genes over-expressed, "
                + "under-expressed, not differentially expressed or with inconclusive results, "
                + "and with no data. This means that the number of columns is variable "
                + "depending on the number of species compared.</p>");
        this.writeln("<p>In simple files, only lines with data in at least two species, and at least "
                + "one over-expression or under-expression call in a species, are provided, "
                + "and only for anatomical entities with a homology relation defined "
                + "with a good level of confidence.</p>");
        this.writeln("<p>Relations of orthology between genes member of a same orthology "
                + "gene group are provided through the associated "
                + "<a href='#" + RequestParameters.HASH_DOC_CALL_OMA 
                + "' title='Jump to hierarchical orthologous groups "
                + "file documentation'>hierarchical orthologous groups file</a>.</p>");
        this.writeln("<table class='call_download_file_desc'>");
        this.writeln("<caption>Format description for multi-species simple differential expression file</caption>");
        this.writeln("<thead>");
        this.writeln("<tr><th>Column</th><th>Content</th><th>Cardinality</th><th>Example</th></tr>");
        this.writeln("</thead>");
        this.writeln("<tbody>");
        this.writeln("<tr><td>1</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_SIMPLE 
                + "_col1' title='" 
                + "See " + OMA_ID_COL_NAME + " column description'>" 
                + OMA_ID_COL_NAME 
                + "</a></td><td>1</td><td>80</td></tr>");
        //TODO: change order of columns anat entity/stage once we re-generate the files.
        this.writeln("<tr><td>2</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_SIMPLE 
                + "_col2' title='" 
                + "See " + MULTI_ANAT_ENTITY_IDS_COL_NAME + " column description'>" 
                + MULTI_ANAT_ENTITY_IDS_COL_NAME 
                + "</a></td><td>1 or greater</td><td>UBERON:0001898</td></tr>");
        this.writeln("<tr><td>3</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_SIMPLE 
                + "_col3' title='" 
                + "See " + MULTI_ANAT_ENTITY_NAMES_COL_NAME + " column description'>" 
                + MULTI_ANAT_ENTITY_NAMES_COL_NAME 
                + "</a></td><td>1 or greater</td><td>hypothalamus</td></tr>");
        this.writeln("<tr><td>4</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_SIMPLE 
                + "_col4' title='" 
                + STAGE_ID_LINK_TITLE + "'>" + STAGE_ID_COL_NAME 
                + "</a></td><td>1</td><td>UBERON:0000113</td></tr>");
        this.writeln("<tr><td>5</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_SIMPLE 
                + "_col5' title='" 
                + STAGE_NAME_LINK_TITLE + "'>" + STAGE_NAME_COL_NAME 
                + "</a></td><td>1</td><td>post-juvenile adult stage</td></tr>");
        this.writeln("<tr><td>6</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_SIMPLE 
                + "_col6' title='" 
                + "See " + OVER_EXPRESSED_FOR_SPECIES_COL_NAME + " species1 column description'>" 
                + OVER_EXPRESSED_FOR_SPECIES_COL_NAME + " species1</a> (e.g., <code>"
                + OVER_EXPRESSED_FOR_SPECIES_COL_NAME + " Homo sapiens</code>)"
                + "</td><td>1</td><td>1</td></tr>");
        this.writeln("<tr><td>7</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_SIMPLE 
                + "_col7' title='" 
                + "See " + UNDER_EXPRESSED_FOR_SPECIES_COL_NAME + " species1 column description'>" 
                + UNDER_EXPRESSED_FOR_SPECIES_COL_NAME + " species1</a> (e.g., <code>"
                + UNDER_EXPRESSED_FOR_SPECIES_COL_NAME + " Homo sapiens</code>)"
                + "</td><td>1</td><td>0</td></tr>");
        this.writeln("<tr><td>8</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_SIMPLE 
                + "_col8' title='" 
                + "See " + NOT_DIFF_EXPRESSED_FOR_SPECIES_COL_NAME + " species1 column description'>" 
                + NOT_DIFF_EXPRESSED_FOR_SPECIES_COL_NAME + " species1</a> (e.g., <code>"
                + NOT_DIFF_EXPRESSED_FOR_SPECIES_COL_NAME + " Homo sapiens</code>)"
                + "</td><td>1</td><td>0</td></tr>");
        this.writeln("<tr><td>9</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_SIMPLE 
                + "_col9' title='" 
                + "See " + NA_FOR_SPECIES_COL_NAME + " species1 column description'>" 
                + NA_FOR_SPECIES_COL_NAME + " species1</a> (e.g., <code>"
                + NA_FOR_SPECIES_COL_NAME + " Homo sapiens</code>)"
                + "</td><td>1</td><td>0</td></tr>");
        this.writeln("<tr><td>10</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_SIMPLE 
                + "_col6' title='" 
                + "See " + OVER_EXPRESSED_FOR_SPECIES_COL_NAME + " species2 column description'>" 
                + OVER_EXPRESSED_FOR_SPECIES_COL_NAME + " species2</a> (e.g., <code>"
                + OVER_EXPRESSED_FOR_SPECIES_COL_NAME + " Mus musculus</code>)"
                + "</td><td>1</td><td>1</td></tr>");
        this.writeln("<tr><td>11</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_SIMPLE 
                + "_col7' title='" 
                + "See " + UNDER_EXPRESSED_FOR_SPECIES_COL_NAME + " species2 column description'>" 
                + UNDER_EXPRESSED_FOR_SPECIES_COL_NAME + " species2</a> (e.g., <code>"
                + UNDER_EXPRESSED_FOR_SPECIES_COL_NAME + " Mus musculus</code>)"
                + "</td><td>1</td><td>0</td></tr>");
        this.writeln("<tr><td>12</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_SIMPLE 
                + "_col8' title='" 
                + "See " + NOT_DIFF_EXPRESSED_FOR_SPECIES_COL_NAME + " species2 column description'>" 
                + NOT_DIFF_EXPRESSED_FOR_SPECIES_COL_NAME + " species2</a> (e.g., <code>"
                + NOT_DIFF_EXPRESSED_FOR_SPECIES_COL_NAME + " Mus musculus</code>)"
                + "</td><td>1</td><td>0</td></tr>");
        this.writeln("<tr><td>13</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_SIMPLE 
                + "_col9' title='" 
                + "See " + NA_FOR_SPECIES_COL_NAME + " species2 column description'>" 
                + NA_FOR_SPECIES_COL_NAME + " species2</a> (e.g., <code>"
                + NA_FOR_SPECIES_COL_NAME + " Mus musculus</code>)"
                + "</td><td>1</td><td>0</td></tr>");
        this.writeln("<tr><td>...</td><td>" 
                + OVER_EXPRESSED_FOR_SPECIES_COL_NAME + " speciesXX "
                + "</td><td>1</td><td>...</td></tr>");
        this.writeln("<tr><td>...</td><td>...</td><td></td><td></td></tr>");
        this.writeln("<tr><td>(species*4 + 6)</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_SIMPLE 
                + "_col_gene_ids' title='" 
                + "See " + GENE_IDS_COL_NAME + " column description'>"+ GENE_IDS_COL_NAME + "</a></td>"
                + "<td>2 or greater</td><td>ENSG00000169057|ENSMUSG00000031393</td></tr>");
        this.writeln("<tr><td>(species*4 + 7)</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_SIMPLE 
                + "_col_gene_names' title='" 
                + "See " + GENE_NAMES_COL_NAME + " column description'>"+ GENE_NAMES_COL_NAME + "</a></td>"
                + "<td>2 or greater</td><td>MECP2|Mecp2</td></tr>");
        this.writeln("</tbody>");
        this.writeln("</table>");
        this.writeln(getMultiSpeciesSimpleDiffExprFileExample());
        
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_SIMPLE 
                + "_col1'>" + OMA_ID_COL_NAME + " (column 1)</h5>");
        this.writeln(getOMAIdColDescription());
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_SIMPLE 
                + "_col2'>" + MULTI_ANAT_ENTITY_IDS_COL_NAME 
                + " (column 2)</h5>");
        this.writeln(getMultiAnatEntityIdsColDescription());
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_SIMPLE 
                + "_col3'>" + MULTI_ANAT_ENTITY_NAMES_COL_NAME 
                + " (column 3)</h5>");
        this.writeln(getMultiAnatEntityNamesColDescription(2));
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_SIMPLE 
                + "_col4'>" + STAGE_ID_COL_NAME + " (column 4)</h5>");
        this.writeln(getMultiSpeciesStageIdColDescription());
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_SIMPLE 
                + "_col5'>" + STAGE_NAME_COL_NAME + " (column 5)</h5>");
        this.writeln(getStageNameColDescription(4));
        
        String totalNumberOfGenes = "<p>Please note that the list of all genes member of "
                + "the OMA orthologous gene group with ID provided in <code>" 
                + OMA_ID_COL_NAME + "</code> (column 1) is provided through "
                + "the <a href='#" + RequestParameters.HASH_DOC_CALL_OMA 
                + "' title='Jump to hierarchical orthologous groups "
                + "file documentation'>hierarchical orthologous groups file</a>.</p>";
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_SIMPLE 
                + "_col6'>" + OVER_EXPRESSED_FOR_SPECIES_COL_NAME 
                + " speciesXX</h5>");
        this.writeln(getOverUnderExprForSpeciesColDescription(1, 4, 2, true));
        this.writeln(totalNumberOfGenes);
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_SIMPLE 
                + "_col7'>" + UNDER_EXPRESSED_FOR_SPECIES_COL_NAME 
                + " speciesXX</h5>");
        this.writeln(getOverUnderExprForSpeciesColDescription(1, 4, 2, false));
        this.writeln(totalNumberOfGenes);
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_SIMPLE 
                + "_col8'>" + NOT_DIFF_EXPRESSED_FOR_SPECIES_COL_NAME 
                + " speciesXX</h5>");
        this.writeln("<p>Number of genes, members of the OMA orthologous gene group "
                + "with ID provided in <code>" + OMA_ID_COL_NAME + "</code> (column 1), "
                + "that were tested for differential expression in this condition (<code>" 
                + MULTI_ANAT_ENTITY_IDS_COL_NAME + "</code> (column 2), at <code>" 
                + STAGE_ID_COL_NAME + "</code> (column 4)), "
                + "but that were never shown to have a significant variation of "
                + "their level of expression as compared to the other conditions "
                + "of the analyses, or for which conflicting results were generated "
                + "from different data types.</p>");
        this.writeln(totalNumberOfGenes);
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_SIMPLE 
                + "_col9'>" + NA_FOR_SPECIES_COL_NAME 
                + " speciesXX</h5>");
        this.writeln("<p>Number of genes, members of the OMA orthologous gene group "
                + "with ID provided in <code>" + OMA_ID_COL_NAME + "</code> (column 1), "
                + "that were not tested for differential expression in this condition (<code>" 
                + MULTI_ANAT_ENTITY_IDS_COL_NAME + "</code> (column 2), at <code>" 
                + STAGE_ID_COL_NAME + "</code> (column 4)).</p>");
        this.writeln(totalNumberOfGenes);
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_SIMPLE 
                + "_col_gene_ids'>" + GENE_IDS_COL_NAME + "</h5>");
        this.writeln("<p>IDs of the genes member of the OMA orthologous gene group "
                + "with ID provided in <code>" + OMA_ID_COL_NAME + "</code> (column 1). "
                + "Cardinality 2 or greater. IDs are separated with the character |.</p>"
                + "<p>This column is provided as additional information, members "
                + "of OMA orthologous gene groups can be retrieved through the use "
                + "of the <a href='#" + RequestParameters.HASH_DOC_CALL_OMA 
                + "' title='Jump to hierarchical orthologous groups "
                + "file documentation'>hierarchical orthologous groups file</a>.</p>");
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_SIMPLE 
                + "_col_gene_names'>" + GENE_NAMES_COL_NAME + "</h5>");
        this.writeln("<p>Name of the genes member of the OMA orthologous gene group "
                + "with ID provided in <code>" + OMA_ID_COL_NAME + "</code> (column 1). "
                + "Cardinality 2 or greater. Names are separated with the character |.</p>"
                + "<p>This column is provided as additional information, members "
                + "of OMA orthologous gene groups can be retrieved through the use "
                + "of the <a href='#" + RequestParameters.HASH_DOC_CALL_OMA 
                + "' title='Jump to hierarchical orthologous groups "
                + "file documentation'>hierarchical orthologous groups file</a>.</p>");

        this.writeln("<p><a href='#" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF
                + "'>Back to over-/under-expression menu</a></p>");
        
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
        
        this.writeln("<h4 id='" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_COMPLETE 
                + "'>Complete file</h4>");
        this.writeln("<p>In complete files, information for all genes are provided, "
                + "in all conditions tested, for anatomical entities homologous between "
                + "all species compared, and comparable broad developmental stages. "
                + "As opposed to simple multi-species files, all homology relations available "
                + "for the anatomical entities are considered, even from homology "
                + "hypotheses with low support; a column allows to retrieve the level of confidence "
                + "in the homology hypothesis used. Also, the number of columns in complete files "
                + "is not variable, whatever the number of species compared is.</p>");
        this.writeln("<p>Relations of orthology between genes can be retrieved through the use "
                + "of the <a href='#" + RequestParameters.HASH_DOC_CALL_OMA 
                + "' title='Jump to hierarchical orthologous groups "
                + "file documentation'>hierarchical orthologous groups file</a>. "
                + "This allows notably to detect genes with no data "
                + "for a condition: if a gene is listed as a member of an orthology group, "
                + "but there is no call for this gene in a given condition, it means "
                + "that there is no data available for this gene in this condition.</p>");
        this.writeln("<table class='call_download_file_desc'>");
        this.writeln("<caption>Format description for multi-species complete differential "
                + "expression file</caption>");
        this.writeln("<thead>");
        this.writeln("<tr><th>Column</th><th>Content</th><th>Cardinality</th><th>Example</th></tr>");
        this.writeln("</thead>");
        this.writeln("<tbody>");
        this.writeln("<tr><td>1</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_COMPLETE 
                + "_col1' title='" 
                + "See " + OMA_ID_COL_NAME + " column description'>" + OMA_ID_COL_NAME 
                + "</a></td><td>1</td><td>42865</td></tr>");
        this.writeln("<tr><td>2</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_COMPLETE 
                + "_col2' title='" 
                + GENE_ID_LINK_TITLE + "'>" + GENE_ID_COL_NAME 
                + "</a></td><td>1</td><td>ENSMMUG00000012094</td></tr>");
        this.writeln("<tr><td>3</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_COMPLETE 
                + "_col3' title='" 
                + GENE_NAME_LINK_TITLE + "'>" + GENE_NAME_COL_NAME 
                + "</a></td><td>1</td><td>RAB17</td></tr>");
        this.writeln("<tr><td>4</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_COMPLETE 
                + "_col4' title='" 
                + "See " + MULTI_ANAT_ENTITY_IDS_COL_NAME + " column description'>" 
                + MULTI_ANAT_ENTITY_IDS_COL_NAME 
                + "</a></td><td>1 or greater</td><td>UBERON:0002037</td></tr>");
        this.writeln("<tr><td>5</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_COMPLETE 
                + "_col5' title='" 
                + "See " + MULTI_ANAT_ENTITY_NAMES_COL_NAME + " column description'>" 
                + MULTI_ANAT_ENTITY_NAMES_COL_NAME 
                + "</a></td><td>1 or greater</td><td>cerebellum</td></tr>");
        this.writeln("<tr><td>6</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_COMPLETE 
                + "_col6' title='" 
                + STAGE_ID_LINK_TITLE + "'>" + STAGE_ID_COL_NAME 
                + "</a></td><td>1</td><td>UBERON:0018241</td></tr>");
        this.writeln("<tr><td>7</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_COMPLETE 
                + "_col7' title='" 
                + STAGE_NAME_LINK_TITLE + "'>" + STAGE_NAME_COL_NAME 
                + "</a></td><td>1</td><td>prime adult stage</td></tr>");
        this.writeln("<tr><td>8</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_COMPLETE 
                + "_col8' title='" 
                + "See " + SPECIES_LATIN_NAME_COL_NAME + " column description'>" 
                + SPECIES_LATIN_NAME_COL_NAME 
                + "</a></td><td>1</td><td>Macaca_mulatta</td></tr>");
        this.writeln("<tr><td>9</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_COMPLETE 
                + "_col9' title='" 
                + DIFF_EXPR_STATE_LINK_TITLE + "'>" + DIFF_EXPR_STATE_COL_NAME 
                + "</a></td><td>1</td><td>under-expression</td></tr>");
        this.writeln("<tr><td>10</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_COMPLETE 
                + "_col10' title='" 
                + EXPR_QUAL_LINK_TITLE + "'>" + EXPR_QUAL_COL_NAME 
                + "</a></td><td>1</td><td>high quality</td></tr>");
        this.writeln("<tr><td>11</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_COMPLETE 
                + "_col11' title='" 
                + "See " + AFFY_DIFF_EXPR_STATE_COL_NAME + " column description'>" 
                + AFFY_DIFF_EXPR_STATE_COL_NAME 
                + "</a></td><td>1</td><td>no data</td></tr>");
        this.writeln("<tr><td>12</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_COMPLETE 
                + "_col12' title='" 
                + "See " + AFFY_EXPR_QUAL_COL_NAME + " column description'>" 
                + AFFY_EXPR_QUAL_COL_NAME
        //TODO: change 'poor' to 'low'
                + "</a></td><td>1</td><td>no data</td></tr>");
        this.writeln("<tr><td>13</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_COMPLETE 
                + "_col13' title='" 
                + "See " + AFFY_DIFF_EXPR_P_VAL_COL_NAME + " column description'>" 
                + AFFY_DIFF_EXPR_P_VAL_COL_NAME 
                + "</a></td><td>1</td><td>1.0</td></tr>");
        this.writeln("<tr><td>14</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_COMPLETE 
                + "_col14' title='" 
                + "See " + AFFY_DIFF_EXPR_SUPPORT_COUNT_COL_NAME + " column description'>" 
                + AFFY_DIFF_EXPR_SUPPORT_COUNT_COL_NAME 
                + "</a></td><td>1</td><td>0</td></tr>");
        this.writeln("<tr><td>15</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_COMPLETE 
                + "_col15' title='" 
                + "See " + AFFY_DIFF_EXPR_CONFLICT_COUNT_COL_NAME + " column description'>" 
                + AFFY_DIFF_EXPR_CONFLICT_COUNT_COL_NAME 
                + "</a></td><td>1</td><td>0</td></tr>");
        this.writeln("<tr><td>16</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_COMPLETE 
                + "_col16' title='" 
                + "See " + RNA_SEQ_DIFF_EXPR_STATE_COL_NAME + " column description'>" 
                + RNA_SEQ_DIFF_EXPR_STATE_COL_NAME 
                + "</a></td><td>1</td><td>under-expression</td></tr>");
        this.writeln("<tr><td>17</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_COMPLETE 
                + "_col17' title='" 
                + "See " + RNA_SEQ_EXPR_QUAL_COL_NAME + " column description'>" 
                + RNA_SEQ_EXPR_QUAL_COL_NAME 
                + "</a></td><td>1</td><td>high quality</td></tr>");
        this.writeln("<tr><td>18</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_COMPLETE 
                + "_col18' title='" 
                + "See " + RNA_SEQ_DIFF_EXPR_P_VAL_COL_NAME + " column description'>" 
                + RNA_SEQ_DIFF_EXPR_P_VAL_COL_NAME 
                + "</a></td><td>1</td><td>8.82E-7</td></tr>");
        this.writeln("<tr><td>19</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_COMPLETE 
                + "_col19' title='" 
                + "See " + RNA_SEQ_DIFF_EXPR_SUPPORT_COUNT_COL_NAME + " column description'>" 
                + RNA_SEQ_DIFF_EXPR_SUPPORT_COUNT_COL_NAME 
                + "</a></td><td>1</td><td>1</td></tr>");
        this.writeln("<tr><td>20</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_COMPLETE 
                + "_col20' title='" 
                + "See " + RNA_SEQ_DIFF_EXPR_CONFLICT_COUNT_COL_NAME + " column description'>" 
                + RNA_SEQ_DIFF_EXPR_CONFLICT_COUNT_COL_NAME 
                + "</a></td><td>1</td><td>0</td></tr>");
        this.writeln("<tr><td>21</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_COMPLETE 
                + "_col21' title='" 
                + "See " + ANAT_HOMOLOGY_CIO_ID_COL_NAME + " column description'>" 
                + ANAT_HOMOLOGY_CIO_ID_COL_NAME 
                + "</a></td><td>1</td><td>CIO:0000003</td></tr>");
        this.writeln("<tr><td>22</td><td><a href='#" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_COMPLETE 
                + "_col22' title='" 
                + "See " + ANAT_HOMOLOGY_CIO_NAME_COL_NAME + " column description'>" 
                + ANAT_HOMOLOGY_CIO_NAME_COL_NAME 
                + "</a></td><td>1</td><td>high confidence from single evidence</td></tr>");
        this.writeln("</tbody>");
        this.writeln("</table>");
        this.writeln(getMultiSpeciesCompleteDiffExprFileExample());
        
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_COMPLETE 
                + "_col1'>" + OMA_ID_COL_NAME + " (column 1)</h5>");
        this.writeln(getOMAIdColDescription());
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_COMPLETE 
                + "_col2'>" + GENE_ID_COL_NAME + " (column 2)</h5>");
        this.writeln(getGeneIdColDescription());
        this.writeln("<p>Please note that the list of all genes member of "
                + "the OMA ortholoogous gene group with ID provided in <code>" 
                + OMA_ID_COL_NAME + "</code> (column 1) is provided through the "
                + "<a href='#" + RequestParameters.HASH_DOC_CALL_OMA 
                + "' title='Jump to hierarchical orthologous groups "
                + "file documentation'>hierarchical orthologous groups file</a>. "
                + "If a gene listed in this file has no call for the condition "
                + "<code>" + MULTI_ANAT_ENTITY_IDS_COL_NAME + "</code> (column 4), at <code>" 
                + STAGE_ID_COL_NAME + "</code> (column 6), it means "
                + "that there is no data available for this gene in this condition.</p>");
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_COMPLETE 
                + "_col3'>" + GENE_NAME_COL_NAME + " (column 3)</h5>");
        this.writeln(getGeneNameColDescription(2));
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_COMPLETE 
                + "_col4'>" + MULTI_ANAT_ENTITY_IDS_COL_NAME 
                + " (column 4)</h5>");
        this.writeln(getMultiAnatEntityIdsColDescription());
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_COMPLETE 
                + "_col5'>" + MULTI_ANAT_ENTITY_NAMES_COL_NAME 
                + " (column 5)</h5>");
        this.writeln(getMultiAnatEntityNamesColDescription(4));
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_COMPLETE 
                + "_col6'>" + STAGE_ID_COL_NAME + " (column 6)</h5>");
        this.writeln(getMultiSpeciesStageIdColDescription());
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_COMPLETE 
                + "_col7'>" + STAGE_NAME_COL_NAME + " (column 7)</h5>");
        this.writeln(getStageNameColDescription(6));
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_COMPLETE 
                + "_col8'>" + SPECIES_LATIN_NAME_COL_NAME 
                + " (column 8)</h5>");
        this.writeln("<p>The latin name of the species which the gene in " + GENE_ID_COL_NAME 
                + " (column 2) belongs to.</p>");
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_COMPLETE 
                + "_col9'>" + DIFF_EXPR_STATE_COL_NAME 
                + " (column 9)</h5>");
        this.writeln(getDiffExprStateColDescription(2, 6, 4, false, true, true, false, 
                "all data types")); 
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_COMPLETE 
                + "_col10'>" + EXPR_QUAL_COL_NAME 
                + " (column 10)</h5>");
        this.writeln(getDiffExprQualColDescription(DIFF_EXPR_STATE_COL_NAME, 2, true, false)); 
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_COMPLETE 
                + "_col11'>" + AFFY_DIFF_EXPR_STATE_COL_NAME 
                + " (column 11)</h5>");
        this.writeln(getDiffExprStateColDescription(2, 6, 4, false, true, false, true, 
                "Affymetrix data")); 
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_COMPLETE 
                + "_col12'>" + AFFY_EXPR_QUAL_COL_NAME 
                + " (column 12)</h5>");
        this.writeln(getDiffExprQualColDescription(AFFY_DIFF_EXPR_STATE_COL_NAME, 9, false, true)); 
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_COMPLETE 
                + "_col13'>" + AFFY_DIFF_EXPR_P_VAL_COL_NAME 
                + " (column 13)</h5>");
        this.writeln(getDiffExprPvalColDescription(
                "Affymetrix", AFFY_DIFF_EXPR_STATE_COL_NAME, 11));
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_COMPLETE 
                + "_col14'>" + AFFY_DIFF_EXPR_SUPPORT_COUNT_COL_NAME 
                + " (column 14)</h5>");
        this.writeln(getDiffSupportCountColDescription(
                "Affymetrix", AFFY_DIFF_EXPR_STATE_COL_NAME, 11));
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_COMPLETE 
                + "_col15'>" + AFFY_DIFF_EXPR_CONFLICT_COUNT_COL_NAME 
                + " (column 15)</h5>");
        this.writeln(getDiffConflictCountColDescription(
                "Affymetrix", AFFY_DIFF_EXPR_STATE_COL_NAME, 11));
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_COMPLETE 
                + "_col16'>" + RNA_SEQ_DIFF_EXPR_STATE_COL_NAME 
                + " (column 16)</h5>");
        this.writeln(getDiffExprStateColDescription(2, 6, 4, false, true, false, true, "RNA-Seq data")); 
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_COMPLETE 
                + "_col17'>" + RNA_SEQ_EXPR_QUAL_COL_NAME 
                + " (column 17)</h5>");
        this.writeln(getDiffExprQualColDescription(RNA_SEQ_DIFF_EXPR_STATE_COL_NAME, 16, false, true)); 
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_COMPLETE 
                + "_col18'>" + RNA_SEQ_DIFF_EXPR_P_VAL_COL_NAME 
                + " (column 18)</h5>");
        this.writeln(getDiffExprPvalColDescription(
                "RNA-Seq", RNA_SEQ_DIFF_EXPR_STATE_COL_NAME, 16));
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_COMPLETE 
                + "_col19'>" + RNA_SEQ_DIFF_EXPR_SUPPORT_COUNT_COL_NAME 
                + " (column 19)</h5>");
        this.writeln(getDiffSupportCountColDescription(
                "RNA-Seq", RNA_SEQ_DIFF_EXPR_STATE_COL_NAME, 16));
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_COMPLETE 
                + "_col20'>" + RNA_SEQ_DIFF_EXPR_CONFLICT_COUNT_COL_NAME 
                + " (column 20)</h5>");
        this.writeln(getDiffConflictCountColDescription(
                "RNA-Seq", RNA_SEQ_DIFF_EXPR_STATE_COL_NAME, 16));
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_COMPLETE 
                + "_col21'>" + ANAT_HOMOLOGY_CIO_ID_COL_NAME 
                + " (column 21)</h5>");
        this.writeln("<p>Unique identifier from the "
                + "<a target='_blank' title='External link to CIO in OBO' "
                + "href='https://github.com/BgeeDB/confidence-information-ontology/blob/master/src/ontology/cio-simple.obo'>"
                + "Confidence Information Ontology</a>, providing the confidence "
                + "in the annotation of homology of anatomical entities defined in <code>"
                + MULTI_ANAT_ENTITY_IDS_COL_NAME + "</code> (column 4). This ontology is an attempt "
                + "to provide a mean to capture the confidence in annotations. "
                + "See <a target='_blank' title='External link to CIO project' "
                + "href='https://github.com/BgeeDB/confidence-information-ontology'>"
                + "project home</a> for more details.</p>");
        this.writeln("<h5 id='" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF_COMPLETE 
                + "_col22'>" + ANAT_HOMOLOGY_CIO_NAME_COL_NAME 
                + " (column 22)</h5>");
        this.writeln("<p>Name of the CIO term defined by <code>" 
                + ANAT_HOMOLOGY_CIO_ID_COL_NAME + "</code> (column 21)</p>");
        
        this.writeln("<p><a href='#" + RequestParameters.HASH_DOC_CALL_MULTI_DIFF
                + "'>Back to over-/under-expression menu</a></p>");
        
        log.exit();
    }
}
