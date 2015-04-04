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

//    /**
//     * A {@code CellProcessorAdaptor} used to convert a {@code List} of {@code String}s 
//     * (as returned by the processor {@link ParseMultipleValuesCell}) into a {@code List} 
//     * of {@code Integer}s. 
//     * 
//     * @author Frederic Bastian
//     * @version Bgee 13 Mar. 2015
//     * @since Bgee 13
//     */
//    private static class ConvertToIntList extends CellProcessorAdaptor {
//        /**
//         * Default constructor, no other {@code CellProcessor} in the chain.
//         */
//        private ConvertToIntList() {
//                super();
//        }
//        /**
//         * Constructor allowing other processors to be chained after 
//         * {@code ConvertToIntList}.
//         * @param next  A {@code CellProcessor} that is the next to be called. 
//         */
//        private ConvertToIntList(CellProcessor next) {
//                super(next);
//        }
//        
//        @Override
//        public Object execute(Object value, CsvContext context) {
//            log.entry(value, context);
//            //throws an Exception if the input is null, as all CellProcessors usually do.
//            validateInputNotNull(value, context); 
//            
//            if (!(value instanceof List)) {
//                throw log.throwing(new SuperCsvCellProcessorException("The CellProcessor "
//                        + "ConvertToIntList can only be chained with a CellProcessor "
//                        + "returning a List of Strings", context, this));
//            }
//            
//            List<Integer> converted = new ArrayList<Integer>();
//            
//            for (Object element: (List<?>) value) {
//                if (!(element instanceof String)) {
//                    throw log.throwing(new SuperCsvCellProcessorException("The CellProcessor "
//                            + "ConvertToIntList can only be chained with a CellProcessor "
//                            + "returning a List of Strings", context, this));
//                }
//                converted.add(Integer.valueOf((String) element));
//            }
//            if (converted.isEmpty()) {
//                throw log.throwing(new SuperCsvCellProcessorException("Cell cannot be empty", 
//                        context, this));
//            }
//            //passes result to next processor in the chain
//            return log.exit(next.execute(converted, context));
//        }
//    }
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
    protected static class ParseMultipleValuesCell extends CellProcessorAdaptor {
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
        protected ParseMultipleValuesCell() {
                super();
        }
        /**
         * Constructor allowing other processors to be chained 
         * after {@code ParseMultipleValuesCell}.
         * @param next  A {@code CellProcessor} that is the next to be called. 
         */
        protected ParseMultipleValuesCell(CellProcessor next) {
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
            //passes result to next processor in the chain
            return log.exit(next.execute(values, context));
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
    protected static class ParseQualifierCell extends CellProcessorAdaptor {
        /**
         * Default constructor, no other {@code CellProcessor} in the chain.
         */
        protected ParseQualifierCell() {
                super();
        }
        /**
         * Constructor allowing other processors to be chained after 
         * {@code ParseQualifierCell}.
         * @param next  A {@code CellProcessor} that is the next to be called. 
         */
        protected ParseQualifierCell(CellProcessor next) {
                super(next);
        }
        @Override
        public Object execute(Object value, CsvContext context) {
            log.entry(value, context); 
            //this processor accepts null value
            boolean negate = false;
            if (NEGATE_QUALIFIER.equals(value)) {
                negate = true;
            }
            //passes result to next processor in the chain
            return log.exit(next.execute(negate, context));
        }
        
    }
    /**
     * Class parent of all bean storing similarity annotations, holding parameters common 
     * to all of them.
     * 
     * @author Frederic Bastian
     * @version Bgee 13 Mar. 2015
     * @since Bgee 13
     */
    public static abstract class AnnotationBean {
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
         * 0-argument constructor of the bean.
         */
        private AnnotationBean() {
            this(null, null, null, null, 0, null, false, null, null, null);
        }
    
        /**
         * Constructor providing all arguments of the class.
         * @param homId             See {@link #getHomId()}.
         * @param homLabel          See {@link #getHomLabel()}.
         * @param entityIds         See {@link #getEntityIds()}.
         * @param entityNames       See {@link #getEntityNames()}.
         * @param ncbiTaxonId       See {@link #getNcbiTaxonId()}.
         * @param taxonName         See {@link #getTaxonName()}.
         * @param negated           See {@link #isNegated()}.
         * @param cioId             See {@link #getCioId()}.
         * @param cioLabel          See {@link #getCioLabel()}.
         * @param supportingText    See {@link #getSupportingText()}.
         */
        private AnnotationBean(String homId, String homLabel,
                List<String> entityIds, List<String> entityNames,
                int ncbiTaxonId, String taxonName, boolean negated,
                String cioId, String cioLabel, String supportingText) {
            
            this.homId = homId;
            this.homLabel = homLabel;
            this.entityIds = entityIds;
            this.entityNames = entityNames;
            this.ncbiTaxonId = ncbiTaxonId;
            this.taxonName = taxonName;
            this.negated = negated;
            this.cioId = cioId;
            this.cioLabel = cioLabel;
            this.supportingText = supportingText;
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
         * @return  A {@code String} that can be an excerpt from the reference, highlighting 
         *          the annotation captured, or an explanation about reasons for inference 
         *          in case of an automated annotation, or underlying single-evidence 
         *          annotations in case of aggregated summary annotations.
         */
        public String getSupportingText() {
            return supportingText;
        }
        /**
         * @param supportingText    A {@code String} that is a text supporting the annotation.
         * @see #getSupportingText()
         */
        public void setSupportingText(String supportingText) {
            this.supportingText = supportingText;
        }
    
        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((cioId == null) ? 0 : cioId.hashCode());
            result = prime * result
                    + ((cioLabel == null) ? 0 : cioLabel.hashCode());
            result = prime * result
                    + ((entityIds == null) ? 0 : entityIds.hashCode());
            result = prime * result
                    + ((entityNames == null) ? 0 : entityNames.hashCode());
            result = prime * result + ((homId == null) ? 0 : homId.hashCode());
            result = prime * result
                    + ((homLabel == null) ? 0 : homLabel.hashCode());
            result = prime * result + ncbiTaxonId;
            result = prime * result + (negated ? 1231 : 1237);
            result = prime * result
                    + ((taxonName == null) ? 0 : taxonName.hashCode());
            result = prime
                    * result
                    + ((supportingText == null) ? 0 : supportingText.hashCode());
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
            if (!(obj instanceof AnnotationBean)) {
                return false;
            }
            AnnotationBean other = (AnnotationBean) obj;
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
            if (taxonName == null) {
                if (other.taxonName != null) {
                    return false;
                }
            } else if (!taxonName.equals(other.taxonName)) {
                return false;
            }
            if (supportingText == null) {
                if (other.supportingText != null) {
                    return false;
                }
            } else if (!supportingText.equals(other.supportingText)) {
                return false;
            }
            return true;
        }
    
        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "homId=" + homId + ", homLabel=" + homLabel
                    + ", entityIds=" + entityIds + ", entityNames="
                    + entityNames + ", ncbiTaxonId=" + ncbiTaxonId
                    + ", taxonName=" + taxonName + ", negated=" + negated
                    + ", cioId=" + cioId + ", cioLabel=" + cioLabel 
                    + ", supportingText=" + supportingText;
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
    public static class RawAnnotationBean extends AnnotationBean {
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
            super();
        }
    
        /**
         * Constructor providing all arguments of the class.
         * @param homId             See {@link #getHomId()}.
         * @param homLabel          See {@link #getHomLabel()}.
         * @param entityIds         See {@link #getEntityIds()}.
         * @param entityNames       See {@link #getEntityNames()}.
         * @param ncbiTaxonId       See {@link #getNcbiTaxonId()}.
         * @param taxonName         See {@link #getTaxonName()}.
         * @param negated           See {@link #isNegated()}.
         * @param ecoId             See {@link #getEcoId()}.
         * @param ecoLabel          See {@link #getEcoLabel()}.
         * @param cioId             See {@link #getCioId()}.
         * @param cioLabel          See {@link #getCioLabel()}.
         * @param refId             See {@link #getRefId()}.
         * @param refTitle          See {@link #getRefTitle()}.
         * @param supportingText    See {@link #getSupportingText()}.
         * @param assignedBy        See {@link #getAssignedBy()}.
         * @param curator           See {@link #getCurator()}.
         * @param curationDate      See {@link #getCurationDate()}.
         */
        public RawAnnotationBean(String homId, String homLabel,
                List<String> entityIds, List<String> entityNames,
                int ncbiTaxonId, String taxonName, boolean negated,
                String ecoId, String ecoLabel, String cioId, String cioLabel, 
                String refId, String refTitle, String supportingText,
                String assignedBy, String curator, Date curationDate) {
            
            super(homId, homLabel, entityIds, entityNames, ncbiTaxonId, taxonName, 
                    negated, cioId, cioLabel, supportingText);
            this.refId = refId;
            this.refTitle = refTitle;
            this.ecoId = ecoId;
            this.ecoLabel = ecoLabel;
            this.assignedBy = assignedBy;
            this.curator = curator;
            this.curationDate = curationDate;
        }
        
        /**
         * Copy constructor.
         * @param toCopy    A {@code RawAnnotationBean} to clone into this 
         *                  {@code RawAnnotationBean}.
         */
        public RawAnnotationBean(RawAnnotationBean toCopy) {
            this(toCopy.getHomId(), toCopy.getHomLabel(), toCopy.getEntityIds(), 
                    toCopy.getEntityNames(), toCopy.getNcbiTaxonId(), toCopy.getTaxonName(), 
                    toCopy.isNegated(), toCopy.getEcoId(), toCopy.getEcoLabel(), 
                    toCopy.getCioId(), toCopy.getCioLabel(), toCopy.getRefId(), 
                    toCopy.getRefTitle(), toCopy.getSupportingText(), toCopy.getAssignedBy(), 
                    toCopy.getCurator(), toCopy.getCurationDate());
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
            int result = super.hashCode();
            result = prime * result
                    + ((assignedBy == null) ? 0 : assignedBy.hashCode());
            result = prime * result
                    + ((curationDate == null) ? 0 : curationDate.hashCode());
            result = prime * result
                    + ((curator == null) ? 0 : curator.hashCode());
            result = prime * result + ((ecoId == null) ? 0 : ecoId.hashCode());
            result = prime * result
                    + ((ecoLabel == null) ? 0 : ecoLabel.hashCode());
            result = prime * result + ((refId == null) ? 0 : refId.hashCode());
            result = prime * result
                    + ((refTitle == null) ? 0 : refTitle.hashCode());
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
            if (!super.equals(obj)) {
                return false;
            }
            if (getClass() != obj.getClass()) {
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
            return true;
        }
        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "RawAnnotationBean [" 
                    + super.toString() + ", refId=" + refId + ", refTitle="
                    + refTitle + ", ecoId=" + ecoId + ", ecoLabel=" + ecoLabel
                    + ", assignedBy=" + assignedBy + ", curator=" + curator 
                    + ", curationDate=" + curationDate + "]";
        }
    }
    
    /**
     * A bean representing a row from the AGGREGATED EVIDENCE annotation file. 
     * These annotations aggregate RAW annotations with same HOM ID, Uberon IDs, taxon ID, 
     * and with RAW positive annotations with same HOM ID, Uberon IDs, mapped to an ancestor 
     * of the taxon of the current annotation, to compute a global confidence score.
     * <p>
     * Getter and setter names must follow standard bean definitions.
     * 
     * @author Frederic Bastian
     * @version Bgee 13 Mar. 2015
     * @since Bgee 13
     */
    public static class SummaryAnnotationBean extends AnnotationBean {
        /**
         * @see #isTrusted()
         */
        private boolean trusted;
//        /**
//         * @see #getPositiveEvidenceCount()
//         */
//        private int positiveEvidenceCount;
//        /**
//         * @see #getNegativeEvidenceCount()
//         */
//        private int negativeEvidenceCount;
//        /**
//         * @see #getPositiveEcoIds()
//         */
//        private List<String> positiveEcoIds;
//        /**
//         * @see #getPositiveEcoLabels()
//         */
//        private List<String> positiveEcoLabels;
//        /**
//         * @see #getNegativeEcoIds()
//         */
//        private List<String> negativeEcoIds;
//        /**
//         * @see #getNegativeEcoLabels()
//         */
//        private List<String> negativeEcoLabels;
//        /**
//         * @see #getAggregatedTaxonIds()
//         */
//        private List<Integer> aggregatedTaxonIds;
//        /**
//         * @see #getAggregatedTaxonNames()
//         */
//        private List<String> aggregatedTaxonNames;
//        /**
//         * @see #getAssignedBy()
//         */
//        private List<String> assignedBy;
        
        /**
         * 0-argument constructor of the bean.
         */
        public SummaryAnnotationBean() {
        }
        /**
         * Constructor providing all arguments of the class.
         * @param homId                 See {@link #getHomId()}.
         * @param homLabel              See {@link #getHomLabel()}.
         * @param entityIds             See {@link #getEntityIds()}.
         * @param entityNames           See {@link #getEntityNames()}.
         * @param ncbiTaxonId           See {@link #getNcbiTaxonId()}.
         * @param taxonName             See {@link #getTaxonName()}.
         * @param negated               See {@link #isNegated()}.
         * @param cioId                 See {@link #getCioId()}.
         * @param cioLabel              See {@link #getCioLabel()}.
         * @param trusted               See {@link #isTrusted()}.
         * @param supportingText        See {@link #getSupportingText()}.
         */
        public SummaryAnnotationBean(String homId, String homLabel,
                List<String> entityIds, List<String> entityNames,
                int ncbiTaxonId, String taxonName, boolean negated,
                String cioId, String cioLabel, boolean trusted, String supportingText
//                , int positiveEvidenceCount, int negativeEvidenceCount,  
//                List<String> positiveEcoIds, List<String> positiveEcoLabels, 
//                List<String> negativeEcoIds, List<String> negativeEcoLabels, 
//                List<Integer> aggregatedTaxonIds, List<String> aggregatedTaxonNames, 
//                List<String> assignedBy
                ) {
            
            super(homId, homLabel, entityIds, entityNames, ncbiTaxonId, taxonName, 
                    negated, cioId, cioLabel, supportingText);
            this.trusted = trusted;
//            this.positiveEvidenceCount = positiveEvidenceCount;
//            this.negativeEvidenceCount = negativeEvidenceCount;
//            this.positiveEcoIds = positiveEcoIds;
//            this.positiveEcoLabels = positiveEcoLabels;
//            this.negativeEcoIds = negativeEcoIds;
//            this.negativeEcoLabels = negativeEcoLabels;
//            this.aggregatedTaxonIds = aggregatedTaxonIds;
//            this.aggregatedTaxonNames = aggregatedTaxonNames;
//            this.assignedBy = assignedBy;
        }
        
        /**
         * @return  A {@code boolean} defining whether the CIO term associated to 
         *          this annotation is considered of sufficient confidence for Bgee.
         */
        public boolean isTrusted() {
            return trusted;
        }
        /**
         * @param trusted   A {@code boolean} defining whether the CIO term associated to 
         *                  this annotation is considered of sufficient confidence for Bgee.
         * @see #isTrusted()
         */
        public void setTrusted(boolean trusted) {
            this.trusted = trusted;
        }
//        /**
//         * @return  An {@code int} that is the number of positive RAW annotations 
//         *          that were aggregated to produce this SUMMARY annotation.
//         * @see #getNegativeEvidenceCount()
//         */
//        public int getPositiveEvidenceCount() {
//            return positiveEvidenceCount;
//        }
//        /**
//         * @param positiveEvidenceCount An {@code int} that is the number of positive 
//         *                              RAW annotations that were aggregated to produce 
//         *                              this SUMMARY annotation.
//         * @see #getPositiveEvidenceCount()
//         */
//        public void setPositiveEvidenceCount(int positiveEvidenceCount) {
//            this.positiveEvidenceCount = positiveEvidenceCount;
//        }
//        
//        /**
//         * @return  An {@code int} that is the number of negative RAW annotations 
//         *          that were aggregated to produce this SUMMARY annotation.
//         * @see #getPositiveEvidenceCount()
//         */
//        public int getNegativeEvidenceCount() {
//            return negativeEvidenceCount;
//        }
//        /**
//         * @param negativeEvidenceCount An {@code int} that is the number of negative 
//         *                              RAW annotations that were aggregated to produce 
//         *                              this SUMMARY annotation.
//         * @see #getNegativeEvidenceCount()
//         */
//        public void setNegativeEvidenceCount(int negativeEvidenceCount) {
//            this.negativeEvidenceCount = negativeEvidenceCount;
//        }
//        
//        /**
//         * @return  A {@code List} of {@code String}s that are the IDs of the ECO terms 
//         *          supporting the annotation. These terms come from positive annotations 
//         *          to same HOM ID - Uberon IDs, and to same taxon or to any parent taxa, 
//         *          that were aggregated with the current annotation.
//         * @see #getPositiveEcoLabels()
//         * @see #getNegativeEcoIds()
//         */
//        public List<String> getPositiveEcoIds() {
//            return positiveEcoIds;
//        }
//        /**
//         * @param positiveEcoIds    A {@code List} of {@code String}s that are the IDs 
//         *                          of the ECO terms supporting the annotation.
//         * @see #getPositiveEcoIds()
//         */
//        public void setPositiveEcoIds(List<String> positiveEcoIds) {
//            this.positiveEcoIds = positiveEcoIds;
//        }
//        
//        /**
//         * @return  A {@code List} of {@code String}s that are the labels of the ECO terms 
//         *          supporting the annotation. These terms come from positive annotations 
//         *          to same HOM ID - Uberon IDs, and to same taxon or to any parent taxa, 
//         *          that were aggregated with the current annotation.
//         * @see #getPositiveEcoIds()
//         * @see #getNegativeEcoLabels()
//         */
//        public List<String> getPositiveEcoLabels() {
//            return positiveEcoLabels;
//        }
//        /**
//         * @param positiveEcoLabels A {@code List} of {@code String}s that are the labels 
//         *                          of the ECO terms supporting the annotation.
//         * @see #getPositiveEcoLabels()
//         */
//        public void setPositiveEcoLabels(List<String> positiveEcoLabels) {
//            this.positiveEcoLabels = positiveEcoLabels;
//        }
//        
//        /**
//         * @return  A {@code List} of {@code String}s that are the IDs of the ECO terms 
//         *          rejecting the annotation. These terms come from negative annotations 
//         *          to same HOM ID - Uberon IDs, and to same taxon or to any sub-taxon, 
//         *          that were aggregated with the current annotation.
//         * @see #getNegativeEcoLabels()
//         * @see #getPositiveEcoIds()
//         */
//        public List<String> getNegativeEcoIds() {
//            return negativeEcoIds;
//        }
//        /**
//         * @param negativeEcoIds    A {@code List} of {@code String}s that are the IDs 
//         *                          of the ECO terms rejecting the annotation.
//         * @see #getNegativeEcoIds()
//         */
//        public void setNegativeEcoIds(List<String> negativeEcoIds) {
//            this.negativeEcoIds = negativeEcoIds;
//        }
//        
//        /**
//         * @return  A {@code List} of {@code String}s that are the labels of the ECO terms 
//         *          rejecting the annotation. These terms come from negative annotations 
//         *          to same HOM ID - Uberon IDs, and to same taxon or to any sub-taxon, 
//         *          that were aggregated with the current annotation.
//         * @see #getNegativeEcoIds()
//         * @see #getPositiveEcoLabels()
//         */
//        public List<String> getNegativeEcoLabels() {
//            return negativeEcoLabels;
//        }
//        /**
//         * @param positiveEcoLabels A {@code List} of {@code String}s that are the labels 
//         *                          of the ECO terms rejecting the annotation.
//         * @see #getNegativeEcoLabels()
//         */
//        public void setNegativeEcoLabels(List<String> negativeEcoLabels) {
//            this.negativeEcoLabels = negativeEcoLabels;
//        }
//        
//        /**
//         * Return the list of NCBI taxon IDs from positive annotations mapped to same 
//         * HOM ID - Uberon IDs as the current summary annotation, but mapped to an ancestor 
//         * of the taxon of the current summary annotation (this is done only 
//         * if the current summary annotation is positive). This is for the sake of being 
//         * able to trace back a summary annotation to its raw annotations it aggregates.
//         * <p>
//         * Indeed, if we have evidence that a structure is homologous in, for instance, 
//         * Tetrapoda, and also evidence that the structure might be homologous at 
//         * the Vertebrata level, then we are even more sure that it is homologous 
//         * at the Tetrapoda level.
//         * <p>
//         * Note that we could have aggregate negative annotations mapped to sub-taxa: 
//         * if we have evidence that a structure is NOT homologous at the Tetrapoda level, 
//         * then it is unlikely to be homologous at the Vertebrata level. But we cannot 
//         * rule out weird cases, where a structure is lost in a lineage, then reappears 
//         * through independent evolution in a sub-taxon?
//         * 
//         * @return  A {@code List} of {@code Integer}s that are the NCBI taxon IDs of other 
//         *          taxa examined to generate this summary annotation: parent taxa 
//         *          with positive annotations for the same HOM ID and Uberon IDs.
//         */
//        public List<Integer> getAggregatedTaxonIds() {
//            return aggregatedTaxonIds;
//        }
//        /**
//         * See {@link #getAggregatedTaxonIds()} for explanations.
//         * 
//         * @param aggregatedTaxonIds        A {@code List} of {@code Integer}s that are 
//         *                                  the NCBI taxon IDs of other taxa examined 
//         *                                  to generate this summary annotation.
//         * @see #getAggregatedTaxonIds()
//         */
//        public void setAggregatedTaxonIds(List<Integer> aggregatedTaxonIds) {
//            this.aggregatedTaxonIds = aggregatedTaxonIds;
//        }
//        
//        /**
//         * See {@link #getAggregatedTaxonIds()} for explanations.
//         * 
//         * @return  A {@code List} of {@code String}s that are the taxon name of other 
//         *          taxa examined to generate this summary annotation: parent taxa 
//         *          with positive annotations for the same HOM ID and Uberon IDs.
//         * @see #getAggregatedTaxonIds()
//         */
//        public List<String> getAggregatedTaxonNames() {
//            return aggregatedTaxonNames;
//        }
//        /**
//         * See {@link #getAggregatedTaxonIds()} for explanations.
//         * 
//         * @param parentAggregatedTaxonNames        A {@code List} of {@code String}s that are 
//         *                                          the taxon names of other taxa examined 
//         *                                          to generate this summary annotation.
//         * @see #getAggregatedTaxonNames()
//         * @see #getAggregatedTaxonIds()
//         */
//        public void setAggregatedTaxonNames(List<String> aggregatedTaxonNames) {
//            this.aggregatedTaxonNames = aggregatedTaxonNames;
//        }
//        
//        /**
//         * @return  A {@code List} of {@code String}s that are all the databases that 
//         *          contributed to the raw annotations that were aggregated into 
//         *          this summary annotation.
//         */
//        public List<String> getAssignedBy() {
//            return assignedBy;
//        }
//        /**
//         * @param assignedBy    A {@code List} of {@code String}s that are all the databases 
//         *                      that contributed to the raw annotations that were aggregated 
//         *                      into this summary annotation.
//         * @see #getAssignedBy()
//         */
//        public void setAssignedBy(List<String> assignedBy) {
//            this.assignedBy = assignedBy;
//        }
        
        
        
        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
//            result = prime
//                    * result
//                    + ((aggregatedTaxonIds == null) ? 0 : aggregatedTaxonIds
//                            .hashCode());
//            result = prime
//                    * result
//                    + ((aggregatedTaxonNames == null) ? 0
//                            : aggregatedTaxonNames.hashCode());
//            result = prime * result
//                    + ((assignedBy == null) ? 0 : assignedBy.hashCode());
//            result = prime
//                    * result
//                    + ((negativeEcoIds == null) ? 0 : negativeEcoIds.hashCode());
//            result = prime
//                    * result
//                    + ((negativeEcoLabels == null) ? 0 : negativeEcoLabels
//                            .hashCode());
//            result = prime * result + negativeEvidenceCount;
//            result = prime
//                    * result
//                    + ((positiveEcoIds == null) ? 0 : positiveEcoIds.hashCode());
//            result = prime
//                    * result
//                    + ((positiveEcoLabels == null) ? 0 : positiveEcoLabels
//                            .hashCode());
//            result = prime * result + positiveEvidenceCount;
            result = prime * result + (trusted ? 1231 : 1237);
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
            if (!super.equals(obj)) {
                return false;
            }
            if (!(obj instanceof SummaryAnnotationBean)) {
                return false;
            }
            SummaryAnnotationBean other = (SummaryAnnotationBean) obj;
//            if (aggregatedTaxonIds == null) {
//                if (other.aggregatedTaxonIds != null) {
//                    return false;
//                }
//            } else if (!aggregatedTaxonIds.equals(other.aggregatedTaxonIds)) {
//                return false;
//            }
//            if (aggregatedTaxonNames == null) {
//                if (other.aggregatedTaxonNames != null) {
//                    return false;
//                }
//            } else if (!aggregatedTaxonNames.equals(other.aggregatedTaxonNames)) {
//                return false;
//            }
//            if (assignedBy == null) {
//                if (other.assignedBy != null) {
//                    return false;
//                }
//            } else if (!assignedBy.equals(other.assignedBy)) {
//                return false;
//            }
//            if (negativeEcoIds == null) {
//                if (other.negativeEcoIds != null) {
//                    return false;
//                }
//            } else if (!negativeEcoIds.equals(other.negativeEcoIds)) {
//                return false;
//            }
//            if (negativeEcoLabels == null) {
//                if (other.negativeEcoLabels != null) {
//                    return false;
//                }
//            } else if (!negativeEcoLabels.equals(other.negativeEcoLabels)) {
//                return false;
//            }
//            if (negativeEvidenceCount != other.negativeEvidenceCount) {
//                return false;
//            }
//            if (positiveEcoIds == null) {
//                if (other.positiveEcoIds != null) {
//                    return false;
//                }
//            } else if (!positiveEcoIds.equals(other.positiveEcoIds)) {
//                return false;
//            }
//            if (positiveEcoLabels == null) {
//                if (other.positiveEcoLabels != null) {
//                    return false;
//                }
//            } else if (!positiveEcoLabels.equals(other.positiveEcoLabels)) {
//                return false;
//            }
//            if (positiveEvidenceCount != other.positiveEvidenceCount) {
//                return false;
//            }
            if (trusted != other.trusted) {
                return false;
            }
            return true;
        }
        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "SummaryAnnotationBean [" 
                    + super.toString() + ", trusted=" + trusted 
//                    + ", positiveEvidenceCount="
//                    + positiveEvidenceCount + ", negativeEvidenceCount="
//                    + negativeEvidenceCount + ", trusted=" + trusted
//                    + ", positiveEcoIds=" + positiveEcoIds
//                    + ", positiveEcoLabels=" + positiveEcoLabels
//                    + ", negativeEcoIds=" + negativeEcoIds
//                    + ", negativeEcoLabels=" + negativeEcoLabels
//                    + ", aggregatedTaxonIds=" + aggregatedTaxonIds
//                    + ", aggregatedTaxonNames=" + aggregatedTaxonNames
//                    + ", assignedBy=" + assignedBy + "]"
                    ;
        }
    }
    

    
    /**
     * A bean representing a row from the ANCESTRAL TAXA annotation file. 
     * These annotations are derived from the SUMMARY historical homology annotations, 
     * and try to identify for each structure the taxa it originates from. 
     * <p>
     * The only way to get several of these annotations with same Uberon IDs (so, 
     * associated to several taxa) is in case of independent evolution. Otherwise, 
     * as, in the vast majority of case, a structure appears only once during evolution, 
     * Uberon IDs appear most of the time only in one annotation, associated to 
     * only one ancestral taxon.
     * <p>
     * Such annotations are always positive.
     * <p>
     * Getter and setter names must follow standard bean definitions.
     * 
     * @author Frederic Bastian
     * @version Bgee 13 Mar. 2015
     * @since Bgee 13
     */
    public static class AncestralTaxaAnnotationBean extends AnnotationBean {
        /**
         * 0-argument constructor of the bean.
         */
        public AncestralTaxaAnnotationBean() {
        }
        /**
         * Constructor providing all arguments of the class.
         * @param homId                 See {@link #getHomId()}.
         * @param homLabel              See {@link #getHomLabel()}.
         * @param entityIds             See {@link #getEntityIds()}.
         * @param entityNames           See {@link #getEntityNames()}.
         * @param ncbiTaxonId           See {@link #getNcbiTaxonId()}.
         * @param taxonName             See {@link #getTaxonName()}.
         * @param cioId                 See {@link #getCioId()}.
         * @param cioLabel              See {@link #getCioLabel()}.
         * @param supportingText        See {@link #getSupportingText()}.
         */
        public AncestralTaxaAnnotationBean(String homId, String homLabel,
                List<String> entityIds, List<String> entityNames,
                int ncbiTaxonId, String taxonName, 
                String cioId, String cioLabel, String supportingText) {
            
            super(homId, homLabel, entityIds, entityNames, ncbiTaxonId, taxonName, 
                    false, cioId, cioLabel, supportingText);
        }

        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            //final int prime = 31;
            int result = super.hashCode();
            
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
            if (!super.equals(obj)) {
                return false;
            }
            if (!(obj instanceof AncestralTaxaAnnotationBean)) {
                return false;
            }
            
            return true;
        }
        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "AncestralTaxaAnnotationBean [" 
                    + super.toString() + "]";
        }
    }

    /**
     * A {@code CsvPreference} used to parse TSV files allowing commented line, 
     * starting with "//".
     */
    public final static CsvPreference TSV_COMMENTED = 
            new CsvPreference.Builder(CsvPreference.TAB_PREFERENCE).
            skipComments(new CommentStartsWith("//")).build();
    
    
    //****************************************************
    // COLUMNS COMMON TO ALL SIMILARITY ANNOTATION FILES
    //****************************************************
    /**
     * A {@code String} that is the name of the column containing the HOM IDs 
     * of terms from the ontology of homology and related concepts.
     */
    protected final static String HOM_COL_NAME = "HOM ID";
    /**
     * A {@code String} that is the name of the column containing the HOM names 
     * of terms from the ontology of homology and related concepts.
     */
    protected final static String HOM_NAME_COL_NAME = "HOM name";
    /**
     * A {@code String} that is the name of the column containing the entity IDs 
     * in the similarity annotation file (for instance, "UBERON:0001905|UBERON:0001787").
     */
    protected final static String ENTITY_COL_NAME = "entity";
    /**
     * A {@code String} that is the name of the column containing the entity names 
     * in the similarity annotation file (for instance, 
     * "pineal body|photoreceptor layer of retina").
     */
    protected final static String ENTITY_NAME_COL_NAME = "entity name";
    /**
     * A {@code String} that is the name of the column containing the taxon IDs 
     * in the similarity annotation file (for instance, 9606).
     */
    protected final static String TAXON_COL_NAME = "taxon ID";

    /**
     * A {@code String} that is the name of the column containing the taxon names 
     * in the similarity annotation file (for instance, "Homo sapiens").
     */
    protected final static String TAXON_NAME_COL_NAME = "taxon name";

    /**
     * A {@code String} that is the name of the column containing the qualifier 
     * in the similarity annotation file (to state the an entity is <strong>not</stong> 
     * homologous in a taxon).
     */
    protected final static String QUALIFIER_COL_NAME = "qualifier";
    /**
     * A {@code String} that is the name of the column containing the confidence code IDs 
     * in the similarity annotation file (for instance, "CIO:0000003").
     */
    protected final static String CONF_COL_NAME = "CIO ID";

    /**
     * A {@code String} that is the name of the column containing the confidence code names 
     * in the similarity annotation file (for instance, "High confidence assertion").
     */
    protected final static String CONF_NAME_COL_NAME = "CIO name";

    /**
     * A {@code String} that is the name of the column containing the database which made 
     * the annotation, in the similarity annotation file (for instance, "Bgee").
     */
    protected final static String ASSIGN_COL_NAME = "assigned by";

    //****************************************************
    // COLUMNS SPECIFIC TO RAW ANNOTATION FILES
    //****************************************************
    /**
     * A {@code String} that is the name of the column containing the reference ID 
     * in the similarity annotation file (for instance, "PMID:16771606").
     */
    protected final static String REF_COL_NAME = "reference";
    /**
     * A {@code String} that is the name of the column containing the reference name 
     * in the similarity annotation file (for instance, 
     * "Liem KF, Bemis WE, Walker WF, Grande L, Functional Anatomy of the Vertebrates: 
     * An Evolutionary Perspective (2001) p.500").
     */
    protected final static String REF_TITLE_COL_NAME = "reference title";
    /**
     * A {@code String} that is the name of the column containing the ECO IDs 
     * in the similarity annotation file (for instance, "ECO:0000067").
     */
    protected final static String ECO_COL_NAME = "ECO ID";
    /**
     * A {@code String} that is the name of the column containing the ECO name 
     * in the similarity annotation file (for instance, "developmental similarity evidence").
     */
    protected final static String ECO_NAME_COL_NAME = "ECO name";
    /**
     * A {@code String} that is the name of the column containing a relevant quote from
     * the reference, in the similarity annotation file.
     */
    protected final static String SUPPORT_TEXT_COL_NAME = "supporting text";

    /**
     * A {@code String} that is the name of the column containing the code representing  
     * the annotator which made the annotation, in the similarity annotation file 
     * (for instance "ANN").
     */
    protected final static String CURATOR_COL_NAME = "curator";

    /**
     * A {@code String} that is the name of the column containing the date   
     * when the annotation was made, in the similarity annotation file 
     * (for instance "2013-07-03").
     * @see #DATE_FORMAT
     */
    protected final static String DATE_COL_NAME = "date";

    //****************************************************
    // COLUMNS SPECIFIC TO AGGREGATED EVIDENCE ANNOTATION FILES
    //****************************************************
    /**
     * A {@code String} that is the name of the column containing the Bool value 
     * defining whether the CIO term associated to this annotation is considered 
     * of sufficient confidence.
     */
    protected final static String TRUSTED_COL_NAME = "trusted";
//    /**
//     * A {@code String} that is the name of the column containing the number of positive 
//     * RAW annotations that were aggregated to produce this SUMMARY annotation.
//     */
//    protected final static String POSITIVE_COUNT_COL_NAME = "positive evidence count";
//    /**
//     * A {@code String} that is the name of the column containing the number of negative 
//     * RAW annotations that were aggregated to produce this SUMMARY annotation.
//     */
//    protected final static String NEGATIVE_COUNT_COL_NAME = "negative evidence count";
//    /**
//     * A {@code String} that is the name of the column containing IDs of the ECO terms 
//     * supporting the annotation, in the AGGREGATED EVIDENCE annotation files. These terms 
//     * come from positive annotations to same HOM ID - Uberon IDs, and to same taxon or 
//     * to any parent taxa, that were aggregated with the current annotation.
//     */
//    protected final static String POSITIVE_ECO_COL_NAME = "positive ECO ID";
//    /**
//     * A {@code String} that is the name of the column containing names of the ECO terms 
//     * supporting the annotation, in the AGGREGATED EVIDENCE annotation files. These terms 
//     * come from positive annotations to same HOM ID - Uberon IDs, and to same taxon or 
//     * to any parent taxa, that were aggregated with the current annotation.
//     */
//    protected final static String POSITIVE_ECO_NAME_COL_NAME = "positive ECO name";
//    /**
//     * A {@code String} that is the name of the column containing IDs of the ECO terms 
//     * invalidating the annotation, in the AGGREGATED EVIDENCE annotation files. These terms 
//     * come from negative annotations to same HOM ID - Uberon IDs - taxon ID, 
//     * that were aggregated with the current annotation.
//     */
//    protected final static String NEGATIVE_ECO_COL_NAME = "negative ECO ID";
//    /**
//     * A {@code String} that is the name of the column containing names of the ECO terms 
//     * invalidating the annotation, in the AGGREGATED EVIDENCE annotation files. These terms 
//     * come from negative annotations to same HOM ID - Uberon IDs - taxon ID, 
//     * that were aggregated with the current annotation.
//     */
//    protected final static String NEGATIVE_ECO_NAME_COL_NAME = "negative ECO name";
//    /**
//     * A {@code String} that is the name of the column containing the related taxon IDs, 
//     * in the AGGREGATED EVIDENCE annotation files, of parent taxa with positive annotations 
//     * for the same HOM ID and Uberon IDs, that were aggregated with the current annotation 
//     * (only if the current annotation is positive).
//     */
//    protected final static String AGGREGATED_TAXA_COL_NAME = "Other taxon aggregated ID";
//    /**
//     * A {@code String} that is the name of the column containing the related taxon names, 
//     * in the AGGREGATED EVIDENCE annotation files, of parent taxa with positive annotations 
//     * for the same HOM ID and Uberon IDs, that were aggregated with the current annotation 
//     * (only if the current annotation is positive).
//     */
//    protected final static String AGGREGATED_TAXA_NAME_COL_NAME = "Other taxon aggregated name";

    //****************************************************
    // SPECIAL VALUES
    //****************************************************
    /**
     * A {@code String} that is the value of the {@link #QUALIFIER_COL_NAME} column, 
     * when the annotation is negated.
     */
    protected final static String NEGATE_QUALIFIER = "NOT";
    /**
     * A {@code String} that is the format of the date in the column named 
     * {@link #DATE_COL_NAME}.
     */
    protected final static String DATE_FORMAT = "yyyy-MM-dd";
    /**
     * An unmodifiable {@code List} of {@code String}s that are the allowed separators 
     * between values in cells potentially containing multiple values, 
     * in preferred order of use. 
     * @see ParseMultipleValuesCell
     * @see #multipleValuesToString(List)
     */
    protected final static List<String> VALUE_SEPARATORS = 
            Collections.unmodifiableList(Arrays.asList("|", ","));

    /**
     * Transform a {@code List} of {@code String}s into a {@code String} where each element 
     * is separated by the first separator in {@link #VALUE_SEPARATORS}.
     * 
     * @param values    A {@code List} of {@code String}s to be transformed into 
     *                  a single {@code String}.
     * @return          A {@code String} where each element in {code values} is separated 
     *                  by the first separator in {@link #VALUE_SEPARATORS}.
     * @throws IllegalArgumentException If {@code values} is {@code null} or empty.
     */
    protected static String multipleValuesToString(List<String> values) 
            throws IllegalArgumentException {
        log.entry(values);
        if (values == null || values.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("The provided values cannot be "
                    + "null or empty"));
        }
        
        String valuesToString = "";
        boolean firstIteration = true;
        for (String value: values) {
            if (!firstIteration) {
                valuesToString += VALUE_SEPARATORS.get(0);
            }
            valuesToString += value;
            firstIteration = false;
        }
        return log.exit(valuesToString);
    }
    /**
     * Extracts annotations from the provided RAW similarity annotation file. It returns a 
     * {@code List} of {@code RawAnnotationBean}s, where each {@code RawAnnotationBean} 
     * represents a row in the file. The elements in the {@code List} are ordered 
     * as they were read from the file. 
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
        return log.exit(extractAnnotations(similarityFile, RawAnnotationBean.class));
    }

    /**
     * Extracts annotations from the provided AGGREGATED EVIDENCE similarity annotation file. 
     * It returns a {@code List} of {@code SummaryAnnotationBean}s, where each 
     * {@code SummaryAnnotationBean} represents a row in the file. The elements 
     * in the {@code List} are ordered as they were read from the file. 
     * 
     * @param similarityFile    A {@code String} that is the path to an AGGREGATED EVIDENCE 
     *                          similarity annotation file. 
     * @return                  A {@code List} of {@code SummaryAnnotationBean}s where each 
     *                          element represents a row in the file, ordered as 
     *                          they were read from the file.
     * @throws FileNotFoundException    If {@code similarityFile} could not be found.
     * @throws IOException              If {@code similarityFile} could not be read.
     * @throws IllegalArgumentException If {@code similarityFile} did not allow to retrieve 
     *                                  any annotation or could not be properly parsed.
     */
    public static List<SummaryAnnotationBean> extractSummaryAnnotations(String similarityFile) 
            throws FileNotFoundException, IOException, IllegalArgumentException {
        log.entry(similarityFile);
        return log.exit(extractAnnotations(similarityFile, SummaryAnnotationBean.class));
    }

    /**
     * Extracts annotations from the provided ANCESTRAL TAXA annotation file. 
     * It returns a {@code List} of {@code AncestralTaxaAnnotationBean}s, where each 
     * {@code AncestralTaxaAnnotationBean} represents a row in the file. The elements 
     * in the {@code List} are ordered as they were read from the file. 
     * 
     * @param similarityFile    A {@code String} that is the path to an ANCESTRAL TAXA 
     *                          annotation file. 
     * @return                  A {@code List} of {@code SummaryAnnotationBean}s where each 
     *                          element represents a row in the file, ordered as 
     *                          they were read from the file.
     * @throws FileNotFoundException    If {@code similarityFile} could not be found.
     * @throws IOException              If {@code similarityFile} could not be read.
     * @throws IllegalArgumentException If {@code similarityFile} did not allow to retrieve 
     *                                  any annotation or could not be properly parsed.
     */
    public static List<AncestralTaxaAnnotationBean> extractAncestralTaxaAnnotations(
            String similarityFile) throws FileNotFoundException, IOException, 
            IllegalArgumentException {
        log.entry(similarityFile);
        return log.exit(extractAnnotations(similarityFile, AncestralTaxaAnnotationBean.class));
    }

    /**
     * Extracts annotations from the provided annotation file containing information 
     * capable of populating {@code AnnotationBean} of the requested type. 
     * It returns a {@code List} of {@code AnnotationBean}s of the requested type, where each 
     * {@code AnnotationBean} represents a row in the file. The elements 
     * in the {@code List} are ordered as they were read from the file. 
     * 
     * @param similarityFile    A {@code String} that is the path to a annotation file, 
     *                          containing information capable of populating 
     *                          {@code AnnotationBean} of type {@code beanType}. 
     * @param beanType          A {@code Class} defining the type of {@code AnnotationBean} 
     *                          that will be populated.
     * @param <T>               The type of {@code beanType}.
     * @return                  A {@code List} of {@code T}s where each 
     *                          element represents a row in the file, ordered as 
     *                          they were read from the file.
     * @throws FileNotFoundException    If {@code similarityFile} could not be found.
     * @throws IOException              If {@code similarityFile} could not be read.
     * @throws IllegalArgumentException If {@code similarityFile} did not allow to retrieve 
     *                                  any annotation or could not be properly parsed.
     */
    private static <T extends AnnotationBean> List<T> extractAnnotations(
            String similarityFile, Class<T> beanType) throws FileNotFoundException, 
            IOException, IllegalArgumentException {
        log.entry(similarityFile, beanType);
        
        try (ICsvBeanReader annotReader = new CsvBeanReader(new FileReader(similarityFile), 
                TSV_COMMENTED)) {
            
            List<T> annots = new ArrayList<T>();
            final String[] header = annotReader.getHeader(true);
            String[] attributeMapping = mapHeaderToAttributes(header, beanType);
            CellProcessor[] cellProcessorMapping = mapHeaderToCellProcessors(header, beanType);
            T annot;
            while((annot = annotReader.read(beanType, attributeMapping, 
                    cellProcessorMapping)) != null ) {
                annots.add(annot);
            }
            if (annots.isEmpty()) {
                throw log.throwing(new IllegalArgumentException("The provided file " 
                        + similarityFile + " did not allow to retrieve any annotation"));
            }
            return log.exit(annots);
            
        } catch (SuperCsvException e) {
            //hide implementation details
            throw log.throwing(new IllegalArgumentException("The provided file " 
                    + similarityFile + " could not be properly parsed", e));
        }
    }

    /**
     * Map the column names of a CSV file to the attributes of an {@code AnnotationBean} 
     * of the requested type. This mapping will then be used to populate the bean, 
     * using standard setter name convention. 
     * <p>
     * Thanks to this method, we can adapt to any change in column names or column order.
     * 
     * @param header    An {@code Array} of {@code String}s representing the names 
     *                  of the columns of a similarity annotation file.
     * @param beanType  A {@code Class} defining the type of {@code AnnotationBean} 
     *                  that will be populated.
     * @return          An {@code Array} of {@code String}s that are the names 
     *                  of the attributes of the requested {@code AnnotationBean} type, put in 
     *                  the {@code Array} at the same index as their corresponding column.
     * @throws IllegalArgumentException If a {@code String} in {@code header} 
     *                                  is not recognized, or if {@code beanType} is not 
     *                                  recognized.
     */
    protected static String[] mapHeaderToAttributes(String[] header, 
            Class<? extends AnnotationBean> beanType) throws IllegalArgumentException {
        log.entry(header, beanType);
        
        if (!beanType.equals(RawAnnotationBean.class) && 
                !beanType.equals(SummaryAnnotationBean.class) && 
                !beanType.equals(AncestralTaxaAnnotationBean.class)) {
            throw log.throwing(new IllegalArgumentException("Unrecognized "
                    + "AnnotationBean type: " + beanType));
        }
        
        String[] mapping = new String[header.length];
        for (int i = 0; i < header.length; i++) {
            // *** Attributes common to all AnnotationBean types ***
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
                case TAXON_COL_NAME: 
                    mapping[i] = "ncbiTaxonId";
                    break;
                case TAXON_NAME_COL_NAME: 
                    mapping[i] = "taxonName";
                    break;
                case CONF_COL_NAME: 
                    mapping[i] = "cioId";
                    break;
                case CONF_NAME_COL_NAME: 
                    mapping[i] = "cioLabel";
                    break;
                case SUPPORT_TEXT_COL_NAME: 
                    mapping[i] = "supportingText";
                    break;
            }
            //if it was one of the column common to all AnnotationBeans, 
            //iterate next column name
            if (mapping[i] != null) {
                continue;
            }
            
            if (beanType.equals(RawAnnotationBean.class)) {
                switch (header[i]) {
                // *** Attributes specific to RawAnnotationBean ***
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
                    case ASSIGN_COL_NAME: 
                        mapping[i] = "assignedBy";
                        break;
                    case CURATOR_COL_NAME: 
                        mapping[i] = "curator";
                        break;
                    case DATE_COL_NAME: 
                        mapping[i] = "curationDate";
                        break;
                }
            } else if (beanType.equals(SummaryAnnotationBean.class)) {
                switch (header[i]) {
                // *** Attributes specific to SummaryAnnotationBean ***
                    case TRUSTED_COL_NAME: 
                        mapping[i] = "trusted";
                        break;
//                    case POSITIVE_COUNT_COL_NAME: 
//                        mapping[i] = "positiveEvidenceCount";
//                        break;
//                    case NEGATIVE_COUNT_COL_NAME: 
//                        mapping[i] = "negativeEvidenceCount";
//                        break;
//                    case POSITIVE_ECO_COL_NAME: 
//                        mapping[i] = "positiveEcoIds";
//                        break;
//                    case POSITIVE_ECO_NAME_COL_NAME: 
//                        mapping[i] = "positiveEcoLabels";
//                        break;
//                    case NEGATIVE_ECO_COL_NAME: 
//                        mapping[i] = "negativeEcoIds";
//                        break;
//                    case NEGATIVE_ECO_NAME_COL_NAME: 
//                        mapping[i] = "negativeEcoLabels";
//                        break;
//                    case AGGREGATED_TAXA_COL_NAME: 
//                        mapping[i] = "aggregatedTaxonIds";
//                        break;
//                    case AGGREGATED_TAXA_NAME_COL_NAME: 
//                        mapping[i] = "aggregatedTaxonNames";
//                        break;
//                    case ASSIGN_COL_NAME: 
//                        mapping[i] = "assignedBy";
//                        break;
                }
            } else if (beanType.equals(AncestralTaxaAnnotationBean.class)) {
                //no attributes specific to AncestralTaxaAnnotationBean for now
            } 
            
            if (mapping[i] == null) {
                throw log.throwing(new IllegalArgumentException("Unrecognized header: " 
                        + header[i] + " for AnnotationBean type: " + beanType.getSimpleName()));
            }
        }
        return log.exit(mapping);
    }

    /**
     * Map the column names of a CSV file to the {@code CellProcessor}s 
     * used to populate an {@code AnnotationBean} of the requested type. This way, 
     * we can adapt to any change in column names or column order.
     * 
     * @param header    An {@code Array} of {@code String}s representing the names 
     *                  of the columns of a similarity annotation file.
     * @param beanType  A {@code Class} defining the type of {@code AnnotationBean} 
     *                  that will be populated.
     * @return          An {@code Array} of {@code CellProcessor}s, put in 
     *                  the {@code Array} at the same index as the column they are supposed 
     *                  to process.
     * @throws IllegalArgumentException If a {@code String} in {@code header} 
     *                                  is not recognized, or if {@code beanType} is not 
     *                                  recognized.
     */
    private static CellProcessor[] mapHeaderToCellProcessors(String[] header, 
            Class<? extends AnnotationBean> beanType) throws IllegalArgumentException {
        log.entry(header, beanType);
        
        if (!beanType.equals(RawAnnotationBean.class) && 
                !beanType.equals(SummaryAnnotationBean.class) && 
                !beanType.equals(AncestralTaxaAnnotationBean.class)) {
            throw log.throwing(new IllegalArgumentException("Unrecognized "
                    + "AnnotationBean type: " + beanType));
        }
        
        CellProcessor[] processors = new CellProcessor[header.length];
        for (int i = 0; i < header.length; i++) {
            switch (header[i]) {
            // *** CellProcessors common to all AnnotationBean types ***
                case ENTITY_COL_NAME: 
                case ENTITY_NAME_COL_NAME: 
                    processors[i] = new ParseMultipleValuesCell();
                    break;
                case TAXON_COL_NAME: 
                    processors[i] = new ParseInt();
                    break;
                case HOM_COL_NAME: 
                case HOM_NAME_COL_NAME: 
                case CONF_COL_NAME: 
                case CONF_NAME_COL_NAME: 
                case TAXON_NAME_COL_NAME: 
                    processors[i] = new StrNotNullOrEmpty();
                    break;
                case QUALIFIER_COL_NAME: 
                    processors[i] = new ParseQualifierCell();
                    break;
                case SUPPORT_TEXT_COL_NAME: 
                    processors[i] = new Optional();
                    break;
            }
            //if it was one of the column common to all AnnotationBeans, 
            //iterate next column name
            if (processors[i] != null) {
                continue;
            }
            
            if (beanType.equals(RawAnnotationBean.class)) {
                switch (header[i]) {
                // *** Attributes specific to RawAnnotationBean ***
                    case DATE_COL_NAME: 
                        processors[i] = new ParseDate(DATE_FORMAT);
                        break; 
                    case ECO_COL_NAME: 
                    case ECO_NAME_COL_NAME: 
                    case ASSIGN_COL_NAME: 
                        processors[i] = new StrNotNullOrEmpty();
                        break;
                    //these fields are not mandatory in case of inferred annotations
                    case CURATOR_COL_NAME: 
                    case REF_COL_NAME: 
                    case REF_TITLE_COL_NAME:
                        processors[i] = new Optional();
                        break;
                }
            } else if (beanType.equals(SummaryAnnotationBean.class)) {
                switch (header[i]) {
                // *** Attributes specific to SummaryAnnotationBean ***
                    case TRUSTED_COL_NAME: 
                        processors[i] = new ParseBool();
                        break;
//                    case ASSIGN_COL_NAME: 
//                        processors[i] = new ParseMultipleValuesCell();
//                        break;
//                    case POSITIVE_ECO_COL_NAME: 
//                    case POSITIVE_ECO_NAME_COL_NAME: 
//                    case NEGATIVE_ECO_COL_NAME: 
//                    case NEGATIVE_ECO_NAME_COL_NAME: 
//                    case AGGREGATED_TAXA_NAME_COL_NAME:
//                        processors[i] = new Optional(new ParseMultipleValuesCell());
//                        break;
//                    case POSITIVE_COUNT_COL_NAME: 
//                    case NEGATIVE_COUNT_COL_NAME:
//                        processors[i] = new ParseInt();
//                        break;
//                    case AGGREGATED_TAXA_COL_NAME: 
//                        processors[i] = new Optional(new ParseMultipleValuesCell(
//                                new ConvertToIntList()));
//                        break;
                }
            } else if (beanType.equals(AncestralTaxaAnnotationBean.class)) {
                //no columns specific to AncestralTaxaAnnotationBean for now
            } 
            
            if (processors[i] == null) {
                throw log.throwing(new IllegalArgumentException("Unrecognized header: " 
                        + header[i] + " for AnnotationBean type: " + beanType));
            }
        }
        return log.exit(processors);
    }
}
