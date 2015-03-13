package org.bgee.pipeline.annotations;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseBool;
import org.supercsv.cellprocessor.ParseDate;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.constraint.StrNotNullOrEmpty;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.comment.CommentStartsWith;
import org.supercsv.exception.SuperCsvCellProcessorException;
import org.supercsv.exception.SuperCsvException;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.prefs.CsvPreference;
import org.supercsv.util.CsvContext;

/**
 * Class providing convenient methods to use the anatomical similarity annotations (see 
 * <a href='https://github.com/BgeeDB/anatomical-similarity-annotations'>the tracker</a>). 
 * This class does not use the usual Bgee utils, to be provided as a standalone class. 
 * The dependencies required are: Super CSV, OWLTools, OWLAPI, Log4j2. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Mar. 2015
 * @since Bgee 13
 */
public class SimilarityAnnotationUtils {
    private final static Logger log = 
            LogManager.getLogger(SimilarityAnnotationUtils.class.getName());
    
    /**
     * A {@code CsvPreference} used to parse TSV files allowing commented line, 
     * starting with "//".
     */
    private final static CsvPreference TSV_COMMENTED = 
            new CsvPreference.Builder(CsvPreference.TAB_PREFERENCE).
            skipComments(new CommentStartsWith("//")).build();
    
    /**
     * A {@code String} that is the name of the column containing the HOM IDs 
     * of terms from the ontology of homology and related concepts.
     */
    public final static String HOM_COL_NAME = "HOM ID";
    /**
     * A {@code String} that is the name of the column containing the HOM names 
     * of terms from the ontology of homology and related concepts.
     */
    public final static String HOM_NAME_COL_NAME = "HOM name";
    /**
     * A {@code String} that is the name of the column containing the entity IDs 
     * in the similarity annotation file (for instance, "UBERON:0001905|UBERON:0001787").
     */
    public final static String ENTITY_COL_NAME = "entity";
    /**
     * A {@code String} that is the name of the column containing the entity names 
     * in the similarity annotation file (for instance, 
     * "pineal body|photoreceptor layer of retina").
     */
    public final static String ENTITY_NAME_COL_NAME = "entity name";
    /**
     * A {@code String} that is the name of the column containing the qualifier 
     * in the similarity annotation file (to state the an entity is <strong>not</stong> 
     * homologous in a taxon).
     */
    public final static String QUALIFIER_COL_NAME = "qualifier";
    /**
     * A {@code String} that is the name of the column containing the reference ID 
     * in the similarity annotation file (for instance, "PMID:16771606").
     */
    public final static String REF_COL_NAME = "reference";
    /**
     * A {@code String} that is the name of the column containing the reference name 
     * in the similarity annotation file (for instance, 
     * "Liem KF, Bemis WE, Walker WF, Grande L, Functional Anatomy of the Vertebrates: 
     * An Evolutionary Perspective (2001) p.500").
     */
    public final static String REF_TITLE_COL_NAME = "reference title";
    /**
     * A {@code String} that is the name of the column containing the ECO IDs 
     * in the similarity annotation file (for instance, "ECO:0000067").
     */
    public final static String ECO_COL_NAME = "ECO ID";
    /**
     * A {@code String} that is the name of the column containing the ECO name 
     * in the similarity annotation file (for instance, "developmental similarity evidence").
     */
    public final static String ECO_NAME_COL_NAME = "ECO name";
    /**
     * A {@code String} that is the name of the column containing the confidence code IDs 
     * in the similarity annotation file (for instance, "CIO:0000003").
     */
    public final static String CONF_COL_NAME = "CIO ID";
    /**
     * A {@code String} that is the name of the column containing the confidence code names 
     * in the similarity annotation file (for instance, "High confidence assertion").
     */
    public final static String CONF_NAME_COL_NAME = "CIO name";
    /**
     * A {@code String} that is the name of the column containing the taxon IDs 
     * in the similarity annotation file (for instance, 9606).
     */
    public final static String TAXON_COL_NAME = "taxon ID";
    /**
     * A {@code String} that is the name of the column containing the taxon names 
     * in the similarity annotation file (for instance, "Homo sapiens").
     */
    public final static String TAXON_NAME_COL_NAME = "taxon name";
    /**
     * A {@code String} that is the name of the column containing a relevant quote from
     * the reference, in the similarity annotation file.
     */
    public final static String SUPPORT_TEXT_COL_NAME = "supporting text";
    /**
     * A {@code String} that is the name of the column containing the database which made 
     * the annotation, in the similarity annotation file (for instance, "Bgee").
     */
    public final static String ASSIGN_COL_NAME = "assigned by";
    /**
     * A {@code String} that is the name of the column containing the code representing  
     * the annotator which made the annotation, in the similarity annotation file 
     * (for instance "ANN").
     */
    public final static String CURATOR_COL_NAME = "curator";
    /**
     * A {@code String} that is the name of the column containing the date   
     * when the annotation was made, in the similarity annotation file 
     * (for instance "2013-07-03").
     * @see #DATE_FORMAT
     */
    public final static String DATE_COL_NAME = "date";
    
    /**
     * A {@code String} that is the value of the {@link #QUALIFIER_COL_NAME} column, 
     * when the annotation is negated.
     */
    public final static String NEGATE_QUALIFIER = "NOT";
    /**
     * A {@code String} that is the format of the date in the column named 
     * {@link #DATE_COL_NAME}.
     */
    public final static String DATE_FORMAT = "yyyy-MM-dd";

    /**
     * A {@code CellProcessorAdaptor} capable of parsing cells allowing to optionally 
     * contain multiple values, separated by {@link #SEPARATOR}. 
     * This {@code CellProcessorAdaptor} will return the values as a {@code List}, 
     * in the same order as in the cell read.
     * 
     * @author Frederic Bastian
     * @version Bgee 13 Mar. 2015
     * @since Bgee 13
     */
    private static class ParseMultipleValuesCell extends CellProcessorAdaptor {
        /**
         * An unmodifiable {@code List} of {@code String}s that are the allowed separators 
         * between values in cells potentially containing multiple values, 
         * in preferred order of use. 
         */
        public final static List<String> VALUE_SEPARATORS = 
                Collections.unmodifiableList(Arrays.asList("|", ","));
        /**
         * A {@code String} that is the pattern to use to split values in a cell 
         * potentially containing multiple values.
         */
        private final static String SPLIT_VALUE_PATTERN = generateSplitValuePattern();
        /**
         * Generate the pattern to split multiple values, based on {@link #VALUE_SEPARATORS}.
         * @return  A {@code String} that is the pattern to use to split values in a cell 
         *          potentially containing multiple values.
         */
        private final static String generateSplitValuePattern() {
            log.entry();
            String splitPattern = "";
            for (String separator: VALUE_SEPARATORS) {
                if (!splitPattern.equals("")) {
                    splitPattern += "|";
                }
                splitPattern += Pattern.quote(separator);
            }
            return log.exit(splitPattern);
        }
        
        /**
         * Default constructor, no other {@code CellProcessor} in the chain.
         */
        private ParseMultipleValuesCell() {
                super();
        }
        /**
         * Constructor allowing other processors to be chained after 
         * {@code ParseMultipleValuesCell}.
         * @param next  A {@code CellProcessor} that is the next to be called. 
         */
        private ParseMultipleValuesCell(CellProcessor next) {
                super(next);
        }
        
        @Override
        public Object execute(Object value, CsvContext context) 
                throws SuperCsvCellProcessorException {
            log.entry(value, context); 
            //throws an Exception if the input is null, as all CellProcessors usually do.
            validateInputNotNull(value, context);  
            
            List<String> values = new ArrayList<String>(
                    Arrays.asList(((String) value).split(SPLIT_VALUE_PATTERN)));
            if (values.isEmpty()) {
                throw log.throwing(new SuperCsvCellProcessorException("Cell cannot be empty", 
                        context, this));
            }
            return log.exit(values);
        }
    }
    /**
     * A {@code CellProcessorAdaptor} to parse the qualifier column (see 
     * {@link #QUALIFIER_COL_NAME}), in order to convert it into a {@code Boolean}. 
     * Basically, if the cell contains a value equals to {@link #NEGATE_QUALIFIER}, 
     * the processor will return {@code true}, otherwise it will return {@code false}.
     * 
     * @author Frederic Bastian
     * @version Bgee 13 Mar. 2015
     * @since Bgee 13
     */
    private static class ParseQualifierCell extends CellProcessorAdaptor {
        /**
         * Default constructor, no other {@code CellProcessor} in the chain.
         */
        private ParseQualifierCell() {
                super();
        }
        /**
         * Constructor allowing other processors to be chained after 
         * {@code ParseQualifierCell}.
         * @param next  A {@code CellProcessor} that is the next to be called. 
         */
        private ParseQualifierCell(CellProcessor next) {
                super(next);
        }
        @Override
        public Object execute(Object value, CsvContext context) {
            log.entry(value, context); 
            //this processor accepts null value
            if (NEGATE_QUALIFIER.equals(value)) {
                return log.exit(true);
            }
            return log.exit(false);
        }
        
    }
    
    /**
     * A bean representing a row from the RAW annotation file. Getter and setter names 
     * must follow standard bean definitions.
     * 
     * @author Frederic Bastian
     * @version Bgee 13 Mar. 2015
     * @since Bgee 13
     */
    public static class RawAnnotationBean {
        /**
         * Map the columns of a CSV file to the attributes of {@code RawAnnotationBean}. 
         * This will then be used to populate the bean, using the standard setter name 
         * convention. 
         * <p>
         * Thanks to this method, we can adapt to any change in column names or column order.
         * 
         * @param header    An {@code Array} of {@code String}s representing the names 
         *                  of the columns of a RAW similarity annotation file.
         * @return          An {@code Array} of {@code String}s that are the names 
         *                  of the attributes of {@code RawAnnotationBean}, put in 
         *                  the {@code Array} at the same index as their corresponding column.
         * @throws IllegalArgumentException If a {@code String} in {@code header} 
         *                                  is not recognized.
         */
        private static String[] mapHeaderToAttributes(String[] header) 
                throws IllegalArgumentException {
            log.entry((Object[]) header);
            String[] mapping = new String[header.length];
            for (int i = 0; i < header.length; i++) {
                switch (header[i]) {
                    case HOM_COL_NAME: 
                        mapping[i] = "homId";
                        break;
                    case HOM_NAME_COL_NAME: 
                        mapping[i] = "homLabel";
                        break;
                    case ENTITY_COL_NAME: 
                        mapping[i] = "entityIds";
                        break;
                    case ENTITY_NAME_COL_NAME: 
                        mapping[i] = "entityNames";
                        break;
                    case QUALIFIER_COL_NAME: 
                        mapping[i] = "negated";
                        break;
                    case REF_COL_NAME: 
                        mapping[i] = "refId";
                        break;
                    case REF_TITLE_COL_NAME: 
                        mapping[i] = "refTitle";
                        break;
                    case ECO_COL_NAME: 
                        mapping[i] = "ecoId";
                        break;
                    case ECO_NAME_COL_NAME: 
                        mapping[i] = "ecoLabel";
                        break;
                    case CONF_COL_NAME: 
                        mapping[i] = "cioId";
                        break;
                    case CONF_NAME_COL_NAME: 
                        mapping[i] = "cioLabel";
                        break;
                    case TAXON_COL_NAME: 
                        mapping[i] = "ncbiTaxonId";
                        break;
                    case TAXON_NAME_COL_NAME: 
                        mapping[i] = "taxonName";
                        break;
                    case SUPPORT_TEXT_COL_NAME: 
                        mapping[i] = "supportingText";
                        break;
                    case ASSIGN_COL_NAME: 
                        mapping[i] = "assignedBy";
                        break;
                    case CURATOR_COL_NAME: 
                        mapping[i] = "curator";
                        break;
                    case DATE_COL_NAME: 
                        mapping[i] = "curationDate";
                        break;
                    default:
                        throw log.throwing(new IllegalArgumentException("Unrecognized header: " 
                                + header[i]));
                }
            }
            return log.exit(mapping);
        }
        /**
         * Map the columns of a CSV file to the {@code CellProcessor}s 
         * used to populate a {@code RawAnnotationBean}. This way, we can adapt to any 
         * change in column names or column order.
         * 
         * @param header    An {@code Array} of {@code String}s representing the names 
         *                  of the columns of a RAW similarity annotation file.
         * @return          An {@code Array} of {@code CellProcessor}s, put in 
         *                  the {@code Array} at the same index as the column they are supposed 
         *                  to process.
         * @throws IllegalArgumentException If a {@code String} in {@code header} 
         *                                  is not recognized.
         */
        private static CellProcessor[] mapHeaderToCellProcessors(String[] header) 
                throws IllegalArgumentException {
            log.entry((Object[]) header);
            CellProcessor[] processors = new CellProcessor[header.length];
            for (int i = 0; i < header.length; i++) {
                switch (header[i]) {
                    case ENTITY_COL_NAME: 
                    case ENTITY_NAME_COL_NAME: 
                        processors[i] = new ParseMultipleValuesCell();
                        break;
                    case QUALIFIER_COL_NAME: 
                        processors[i] = new ParseQualifierCell();
                        break;
                    case TAXON_COL_NAME: 
                        processors[i] = new ParseInt();
                        break;
                    case DATE_COL_NAME: 
                        processors[i] = new ParseDate(DATE_FORMAT);
                        break;
                    case HOM_COL_NAME: 
                    case HOM_NAME_COL_NAME: 
                    case REF_COL_NAME: 
                    case REF_TITLE_COL_NAME: 
                    case ECO_COL_NAME: 
                    case ECO_NAME_COL_NAME: 
                    case CONF_COL_NAME: 
                    case CONF_NAME_COL_NAME: 
                    case TAXON_NAME_COL_NAME: 
                    case SUPPORT_TEXT_COL_NAME: 
                    case ASSIGN_COL_NAME: 
                    case CURATOR_COL_NAME: 
                        processors[i] = new StrNotNullOrEmpty();
                        break;
                    default:
                        throw log.throwing(new IllegalArgumentException("Unrecognized header: " 
                                + header[i]));
                }
            }
            return log.exit(processors);
        }
        
        /**
         * @see #getHomId()
         */
        private String homId;
        /**
         * @see #getHOMLabel()
         */
        private String homLabel;
        /**
         * @see #getEntityIds()
         */
        private List<String> entityIds;
        /**
         * @see #getEntityNames()
         */
        private List<String> entityNames;
        /**
         * @see #getNCBITaxonId()
         */
        private int ncbiTaxonId;
        /**
         * @see #getTaxonName()
         */
        private String taxonName;
        /**
         * @see #isNegated()
         */
        private boolean negated;
        /**
         * @see #getRefId()
         */
        private String refId;
        /**
         * @see #getRefTitle()
         */
        private String refTitle;
        /**
         * @see getEcoId()
         */
        private String ecoId;
        /**
         * @see getEcoLabel()
         */
        private String ecoLabel;
        /**
         * @see getCioId()
         */
        private String cioId;
        /**
         * @see getCioLabel()
         */
        private String cioLabel;
        /**
         * @see #getSupportingText()
         */
        private String supportingText;
        /**
         * @see getAssignedBy()
         */
        private String assignedBy;
        /**
         * @see #getCurator()
         */
        private String curator;
        /**
         * @see #getCurationDate()
         */
        private Date curationDate;
        
        /**
         * 0-argument constructor of the bean.
         */
        public RawAnnotationBean() {
        }

        /**
         * @return  A {@code String} that is the ID of a term from the HOM ontology, 
         *          providing the evolutionary concept captured by this annotation.
         * @see #getHOMLabel()
         */
        public String getHomId() {
            return homId;
        }
        /**
         * @param homId A {@code String} that is the ID of a term from the HOM ontology.
         * @see #getHomId()
         */
        public void setHomId(String homId) {
            this.homId = homId;
        }

        /**
         * @return  A {@code String} that is the name of a term from the HOM ontology, 
         *          providing the evolutionary concept captured by this annotation.
         * @see #getHomId()
         */
        public String getHomLabel() {
            return homLabel;
        }
        /**
         * @param homLabel  A {@code String} that is the name of a term from the HOM ontology.
         * @see #getHOMLabel()
         */
        public void setHomLabel(String homLabel) {
            this.homLabel = homLabel;
        }

        /**
         * @return  A {@code List} of {@code String}s that are the IDs of the anatomical entities 
         *          targeted by this annotation. There is most of the time only one entity 
         *          targeted. When several are targeted, they are provided in alphabetical order.
         * @see #getEntityLabels()
         */
        public List<String> getEntityIds() {
            return entityIds;
        }
        /**
         * @param entityIds A {@code List} of {@code String}s that are the IDs 
         *                  of the anatomical entities targeted by this annotation.
         * @see #getEntityIds()
         */
        public void setEntityIds(List<String> entityIds) {
            this.entityIds = entityIds;
        }

        /**
         * @return  A {@code List} of {@code String}s that are the names of the anatomical 
         *          entities targeted by this annotation. There is most of the time only 
         *          one entity targeted. When several are targeted, they are returned 
         *          in the same order as their corresponding ID, as returned by 
         *          {@link #getEntityIds()}.
         * @see #getEntityIds()
         */
        public List<String> getEntityNames() {
            return entityNames;
        }
        /**
         * @param entityNames   A {@code List} of {@code String}s that are the names 
         *                      of the anatomical entities targeted by this annotation.
         * @see #getEntityNames()
         */
        public void setEntityNames(List<String> entityNames) {
            this.entityNames = entityNames;
        }

        /**
         * @return  An {@code int} that is the NCBI ID of the taxon targeted by this annotation.
         * @see getTaxonName()
         */
        public int getNcbiTaxonId() {
            return ncbiTaxonId;
        }
        /**
         * @param ncbiTaxonId   An {@code int} that is the NCBI ID of the taxon 
         *                      targeted by this annotation.
         * @see #getNcbiTaxonId()
         */
        public void setNcbiTaxonId(int ncbiTaxonId) {
            this.ncbiTaxonId = ncbiTaxonId;
        }

        /**
         * @return  A {@code String} that is the name of the taxon targeted by this annotation.
         * @see #getNcbiTaxonId()
         */
        public String getTaxonName() {
            return taxonName;
        }
        /**
         * @param taxonName A {@code String} that is the name of the taxon targeted 
         *                  by this annotation.
         */
        public void setTaxonName(String taxonName) {
            this.taxonName = taxonName;
        }

        /**
         * @return  A {@code boolean} defining whether this annotation is negated, using 
         *          a NOT qualifier. 
         */
        public boolean isNegated() {
            return negated;
        }
        /**
         * @param negated   A {@code boolean} defining whether this annotation is negated, 
         *                  using a NOT qualifier. 
         */
        public void setNegated(boolean negated) {
            this.negated = negated;
        }

        /**
         * @return  A {@code String} that is the ID of the reference where the evidence 
         *          annotated comes from.
         * @see #getRefTitle()
         */
        public String getRefId() {
            return refId;
        }
        /**
         * @param refId A {@code String} that is the ID of the reference where the evidence 
         *              annotated comes from.
         * @see #getRefId()
         */
        public void setRefId(String refId) {
            this.refId = refId;
        }

        /**
         * @return  A {@code String} that is the title of the reference where the evidence 
         *          annotated comes from.
         * @see #getRefId()
         */
        public String getRefTitle() {
            return refTitle;
        }
        /**
         * @param refTitle  A {@code String} that is the title of the reference 
         *                  where the evidence annotated comes from.
         */
        public void setRefTitle(String refTitle) {
            this.refTitle = refTitle;
        }

        /**
         * @return  A {@code String} that is the ID of the term from the ECO ontology, 
         *          used to capture the evidence type used in this annotation.
         * @see #getEcoLabel()
         */
        public String getEcoId() {
            return ecoId;
        }
        /**
         * @param ecoId A {@code String} that is the ID of the term from the ECO ontology, 
         *              used to capture the evidence type used in this annotation.
         * @see #getEcoId()
         */
        public void setEcoId(String ecoId) {
            this.ecoId = ecoId;
        }

        /**
         * @return  A {@code String} that is the label of the term from the ECO ontology, 
         *          used to capture the evidence type used in this annotation.
         * @see #getEcoId()
         */
        public String getEcoLabel() {
            return ecoLabel;
        }
        /**
         * @param ecoLabel  A {@code String} that is the label of the term from the ECO ontology, 
         *                  used to capture the evidence type used in this annotation.
         * @see #getEcoLabel()
         */
        public void setEcoLabel(String ecoLabel) {
            this.ecoLabel = ecoLabel;
        }

        /**
         * @return  A {@code String} that is the ID of the term from the CIO ontology, 
         *          used to capture the confidence in the evidence used in this annotation.
         * @see #getCioLabel()
         */
        public String getCioId() {
            return cioId;
        }
        /**
         * @param cioId A {@code String} that is the ID of the term from the CIO ontology, 
         *              used to capture the confidence in the evidence used in this annotation.
         * @see #getCioId()
         */
        public void setCioId(String cioId) {
            this.cioId = cioId;
        }

        /**
         * @return  A {@code String} that is the label of the term from the CIO ontology, 
         *          used to capture the confidence in the evidence used in this annotation.
         * @see #getCioId()
         */
        public String getCioLabel() {
            return cioLabel;
        }
        /**
         * @param cioLabel  A {@code String} that is the ID of the term from the CIO ontology, 
         *                  used to capture the confidence in the evidence used 
         *                  in this annotation.
         * @see #getCioLabel()
         */
        public void setCioLabel(String cioLabel) {
            this.cioLabel = cioLabel;
        }

        /**
         * @return  A {@code String} that is an excerpt from the reference, highlighting 
         *          the annotation captured.
         */
        public String getSupportingText() {
            return supportingText;
        }
        /**
         * @param supportingText    A {@code String} that is an excerpt from the reference, 
         *                          highlighting the annotation captured.
         * @see #getSupportingText()
         */
        public void setSupportingText(String supportingText) {
            this.supportingText = supportingText;
        }

        /**
         * @return  A {@code String} identifying the database that made the annotation.
         */
        public String getAssignedBy() {
            return assignedBy;
        }
        /**
         * @param assignedBy    A {@code String} identifying the database 
         *                      that made the annotation.
         * @see #getAssignedBy()
         */
        public void setAssignedBy(String assignedBy) {
            this.assignedBy = assignedBy;
        }

        /**
         * @return  A {@code String} identifying the curator who made the annotation, 
         *          part of the database returned by {@link #getAssignedBy()}.
         * @see #getAssignedBy()
         */
        public String getCurator() {
            return curator;
        }
        /**
         * @param curator   A {@code String} identifying the curator who made the annotation.
         * @see #getCurator()
         */
        public void setCurator(String curator) {
            this.curator = curator;
        }

        /**
         * @return  A {@code Date} when the annotation was created.
         */
        public Date getCurationDate() {
            return curationDate;
        }
        /**
         * @param curationDate  A {@code Date} when the annotation was created.
         * @see #getCurationDate()
         */
        public void setCurationDate(Date curationDate) {
            this.curationDate = curationDate;
        }
        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result
                    + ((assignedBy == null) ? 0 : assignedBy.hashCode());
            result = prime * result + ((cioId == null) ? 0 : cioId.hashCode());
            result = prime * result
                    + ((cioLabel == null) ? 0 : cioLabel.hashCode());
            result = prime * result
                    + ((curationDate == null) ? 0 : curationDate.hashCode());
            result = prime * result
                    + ((curator == null) ? 0 : curator.hashCode());
            result = prime * result + ((ecoId == null) ? 0 : ecoId.hashCode());
            result = prime * result
                    + ((ecoLabel == null) ? 0 : ecoLabel.hashCode());
            result = prime * result
                    + ((entityIds == null) ? 0 : entityIds.hashCode());
            result = prime * result
                    + ((entityNames == null) ? 0 : entityNames.hashCode());
            result = prime * result + ((homId == null) ? 0 : homId.hashCode());
            result = prime * result
                    + ((homLabel == null) ? 0 : homLabel.hashCode());
            result = prime * result + ncbiTaxonId;
            result = prime * result + (negated ? 1231 : 1237);
            result = prime * result + ((refId == null) ? 0 : refId.hashCode());
            result = prime * result
                    + ((refTitle == null) ? 0 : refTitle.hashCode());
            result = prime
                    * result
                    + ((supportingText == null) ? 0 : supportingText.hashCode());
            result = prime * result
                    + ((taxonName == null) ? 0 : taxonName.hashCode());
            return result;
        }
        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof RawAnnotationBean)) {
                return false;
            }
            RawAnnotationBean other = (RawAnnotationBean) obj;
            if (assignedBy == null) {
                if (other.assignedBy != null) {
                    return false;
                }
            } else if (!assignedBy.equals(other.assignedBy)) {
                return false;
            }
            if (cioId == null) {
                if (other.cioId != null) {
                    return false;
                }
            } else if (!cioId.equals(other.cioId)) {
                return false;
            }
            if (cioLabel == null) {
                if (other.cioLabel != null) {
                    return false;
                }
            } else if (!cioLabel.equals(other.cioLabel)) {
                return false;
            }
            if (curationDate == null) {
                if (other.curationDate != null) {
                    return false;
                }
            } else if (!curationDate.equals(other.curationDate)) {
                return false;
            }
            if (curator == null) {
                if (other.curator != null) {
                    return false;
                }
            } else if (!curator.equals(other.curator)) {
                return false;
            }
            if (ecoId == null) {
                if (other.ecoId != null) {
                    return false;
                }
            } else if (!ecoId.equals(other.ecoId)) {
                return false;
            }
            if (ecoLabel == null) {
                if (other.ecoLabel != null) {
                    return false;
                }
            } else if (!ecoLabel.equals(other.ecoLabel)) {
                return false;
            }
            if (entityIds == null) {
                if (other.entityIds != null) {
                    return false;
                }
            } else if (!entityIds.equals(other.entityIds)) {
                return false;
            }
            if (entityNames == null) {
                if (other.entityNames != null) {
                    return false;
                }
            } else if (!entityNames.equals(other.entityNames)) {
                return false;
            }
            if (homId == null) {
                if (other.homId != null) {
                    return false;
                }
            } else if (!homId.equals(other.homId)) {
                return false;
            }
            if (homLabel == null) {
                if (other.homLabel != null) {
                    return false;
                }
            } else if (!homLabel.equals(other.homLabel)) {
                return false;
            }
            if (ncbiTaxonId != other.ncbiTaxonId) {
                return false;
            }
            if (negated != other.negated) {
                return false;
            }
            if (refId == null) {
                if (other.refId != null) {
                    return false;
                }
            } else if (!refId.equals(other.refId)) {
                return false;
            }
            if (refTitle == null) {
                if (other.refTitle != null) {
                    return false;
                }
            } else if (!refTitle.equals(other.refTitle)) {
                return false;
            }
            if (supportingText == null) {
                if (other.supportingText != null) {
                    return false;
                }
            } else if (!supportingText.equals(other.supportingText)) {
                return false;
            }
            if (taxonName == null) {
                if (other.taxonName != null) {
                    return false;
                }
            } else if (!taxonName.equals(other.taxonName)) {
                return false;
            }
            return true;
        }
        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "RawAnnotationBean [homId=" + homId + ", homLabel="
                    + homLabel + ", entityIds=" + entityIds + ", entityNames="
                    + entityNames + ", ncbiTaxonId=" + ncbiTaxonId
                    + ", taxonName=" + taxonName + ", negated=" + negated
                    + ", refId=" + refId + ", refTitle=" + refTitle
                    + ", ecoId=" + ecoId + ", ecoLabel=" + ecoLabel
                    + ", cioId=" + cioId + ", cioLabel=" + cioLabel
                    + ", supportingText=" + supportingText + ", assignedBy="
                    + assignedBy + ", curator=" + curator + ", curationDate="
                    + curationDate + "]";
        }
    }
    
    /**
     * Extracts annotations from the provided RAW similarity annotation file. It returns a 
     * {@code List} of {@code RawAnnotationBean}s, where each {@code RawAnnotationBean} 
     * represents a row in the file. The elements in the {@code List} are ordered 
     * as they were read from the file. 
     * </ul>
     * 
     * @param similarityFile    A {@code String} that is the path to a RAW similarity 
     *                          annotation file. 
     * @return                  A {@code List} of {@code RawAnnotationBean}s where each 
     *                          element represents a row in the file, ordered as 
     *                          they were read from the file.
     * @throws FileNotFoundException    If {@code similarityFile} could not be found.
     * @throws IOException              If {@code similarityFile} could not be read.
     * @throws IllegalArgumentException If {@code similarityFile} did not allow to retrieve 
     *                                  any annotation or could not be properly parsed.
     */
    public static List<RawAnnotationBean> extractRawAnnotations(String similarityFile) 
            throws FileNotFoundException, IOException, IllegalArgumentException {
        log.entry(similarityFile);
        
        try (ICsvBeanReader annotReader = new CsvBeanReader(new FileReader(similarityFile), 
                TSV_COMMENTED)) {
            
            List<RawAnnotationBean> annots = new ArrayList<RawAnnotationBean>();
            final String[] header = annotReader.getHeader(true);
            
            RawAnnotationBean annot;
            while((annot = annotReader.read(RawAnnotationBean.class, 
                    RawAnnotationBean.mapHeaderToAttributes(header), 
                    RawAnnotationBean.mapHeaderToCellProcessors(header))) != null ) {
                
                annots.add(annot);
            }
            if (annots.isEmpty()) {
                throw log.throwing(new IllegalArgumentException("The provided file " 
                        + similarityFile + " did not allow to retrieve any annotation"));
            }
            
            return log.exit(annots);
            
        } catch (SuperCsvException e) {
            //do not expose implementation details
            throw log.throwing(new IllegalArgumentException("The provided file " 
                    + similarityFile + " could not be properly parsed", e));
        }
    }
    
}
