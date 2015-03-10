package org.bgee.model.dao.api.annotation;

import java.sql.Date;
import java.util.Collection;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.exception.DAOException;


/**
 * DAO defining queries using or retrieving {@link RawSimilarityAnnotationTO}s. 
 *
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @see RawSimilarityAnnotationTO
 * @since Bgee 13
 */
public interface RawSimilarityAnnotationDAO extends DAO<RawSimilarityAnnotationDAO.Attribute> {

    /**
     * {@code Enum} used to define the attributes to populate in the 
     * {@code RawSimilarityAnnotationTO}s obtained from this {@code RawSimilarityAnnotationDAO}.
     * <ul>
     * <li>{@code ID}: corresponds to {@link RawSimilarityAnnotationDAO#getId()}.
     * <li>{@code NEGATED}: corresponds to {@link RawSimilarityAnnotationDAO#isNegated()}.
     * <li>{@code ECOID}: corresponds to {@link RawSimilarityAnnotationDAO#getECOId()}.
     * <li>{@code CIOID}: corresponds to {@link RawSimilarityAnnotationDAO#getCIOId()}.
     * <li>{@code REFERENCEID}: corresponds to {@link RawSimilarityAnnotationDAO#getReferenceId()}.
     * <li>{@code REFERENCETITLE}: corresponds to {@link RawSimilarityAnnotationDAO#getReferenceTitle()}.
     * <li>{@code SUPPORTINGTEXT}: corresponds to {@link RawSimilarityAnnotationDAO#getSupportingText()}.
     * <li>{@code ASSIGNEDBY}: corresponds to {@link RawSimilarityAnnotationDAO#getAssignedBy()}.
     * <li>{@code CURATOR}: corresponds to {@link RawSimilarityAnnotationDAO#getCurator()}.
     * <li>{@code ANNOTATIONDATE}: corresponds to {@link RawSimilarityAnnotationDAO#getAnnotationDate()}.
     * </ul>
     * @see org.bgee.model.dao.api.DAO#setAttributes(Collection)
     * @see org.bgee.model.dao.api.DAO#setAttributes(Enum[])
     * @see org.bgee.model.dao.api.DAO#clearAttributes()
     */
    public enum Attribute implements DAO.Attribute {
        SUMMARY_SIMILARITY_ANNOTATION_ID, NEGATED, ECOID, CIOID, REFERENCEID, REFERENCETITLE, 
        SUPPORTINGTEXT, ASSIGNEDBY, CURATOR, ANNOTATIONDATE;
    }

    /**
     * Retrieves all raw similarity annotations from data source.
     * <p>
     * The raw similarity annotations are retrieved and returned as a 
     * {@code RawSimilarityAnnotationTOResultSet}. It is the responsibility of  
     * the caller to close this {@code DAOResultSet} once results are retrieved.
     * 
     * @return              An {@code RawSimilarityAnnotationTOResultSet} containing all raw 
     *                      similarity annotations from data source.
     * @throws DAOException If an error occurred when accessing the data source. 
     */
    public RawSimilarityAnnotationTOResultSet getAllRawSimilarityAnnotations() throws DAOException;

    /**
     * Inserts the provided raw similarity annotations into the data source, 
     * represented as a {@code Collection} of {@code RawSimilarityAnnotationTO}s. 
     * 
     * @param rawSimilarityAnnotationTOs  A {@code Collection} of {@code RawSimilarityAnnotationTO}s
     *                                    to be inserted into the data source.
     * @return                            An {@code int} that is the number of inserted raw 
     *                                    similarity annotations.
     * @throws IllegalArgumentException   If {@code rawSimilarityAnnotationTOs} is empty or null. 
     * @throws DAOException               If a {@code SQLException} occurred while trying to insert 
     *                                    raw similarity annotations. The {@code SQLException} will  
     *                                    be wrapped into a {@code DAOException} ({@code DAO}s do 
     *                                    not expose these kind of implementation details).
     */
    public int insertRawSimilarityAnnotations(
            Collection<RawSimilarityAnnotationTO> rawSimilarityAnnotationTOs) 
            throws DAOException, IllegalArgumentException;

    /**
     * {@code DAOResultSet} specifics to {@code RawSimilarityAnnotationTO}s
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public interface RawSimilarityAnnotationTOResultSet 
            extends DAOResultSet<RawSimilarityAnnotationTO> {
    }

    /**
     * An {@code EntityTO} representing a raw similarity annotation, as stored in the Bgee database. 
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public final class RawSimilarityAnnotationTO extends TransferObject {

        private static final long serialVersionUID = -3914547838862781138L;

        /**
         * A {@code String} representing the ID of the associated 'summary' similarity annotation.
         */
        private final String summarySimilarityAnnotationId;

        /**
         * A {@code Boolean} defining whether this annotation is negated (using the NOT qualifier 
         * of the similarity annotation file: used to capture an information rejecting a putative 
         * relation between structures, that could otherwise seem plausible). 
         */
        private final Boolean negated;
        
        /**
         * A {@code String} representing the ID of the Evidence Ontology statement associated to 
         * this raw similarity annotation.
         */
        private final String ecoId;
        
        /**
         * A {@code String} representing the ID of the confidence statement associated to this 
         * raw similarity annotation.
         */
        private final String cioId;        

        /**
         * A {@code String} representing the ID of the source, cited as an authority 
         * for asserting the relation. 
         */
        private final String referenceId;        

        /**
         * A {@code String} representing the title of the source, cited as an authority 
         * for asserting the relation. 
         */
        private final String referenceTitle;        
        
        /**
         * A {@code String} representing a quote from the reference, 
         * supporting this raw similarity annotation.
         */
        private final String supportingText;        
        
        /**
         * A {@code String} representing the database which made this raw similarity annotation.
         */
        private final String assignedBy;        
        
        /**
         * A {@code String} representing the code allowing to identify the curator who made 
         * this annotation, from the database defined above.
         */
        private final String curator;        
        
        /**
         * A {@code Date} representing the date when this annotation was made. 
         */
        private final Date annotationDate;
        
        /**
         * Constructor providing the ID of the associated 'summary' similarity annotation, a 
         * {@code Boolean} defining whether this annotation is negated, the ID of the Evidence 
         * Ontology, the ID of the confidence statement, the ID and the title of the source, a quote 
         * from the reference, the database which made this annotation, the code allowing to 
         * identify the curator, and the date when this annotation was made.
         * <p>
         * All of these parameters are optional, so they can be {@code null} when not used.
         * 
         * @param id                A {@code String} that is the ID of the associated 'summary' 
         *                          similarity annotation.
         * @param negated           A {@code Boolean} defining whether this annotation is negated.
         * @param ecoId             A {@code String} that is the ID of the Evidence Ontology 
         *                          statement associated to this raw similarity annotation.
         * @param cioId             A {@code String} that is the ID of the confidence statement
         *                          associated to this raw similarity annotation.
         * @param referenceId       A {@code String} that is the ID of the source cited as an 
         *                          authority for asserting the relation. 
         * @param referenceTitle    A {@code String} that is the title of the source cited as an 
         *                          authority for asserting the relation.
         * @param supportingText    A {@code String} that is a quote from the reference, 
         *                          supporting this raw similarity annotation.
         * @param assignedBy        A {@code String} that is the database which made this annotation.
         * @param curator           A {@code String} that is the code allowing to identify the 
         *                          curator who made this annotation.
         * @param annotationDate    A {@code Date} that is the date when this annotation was made.
         * @throws IllegalArgumentException
         */
        public RawSimilarityAnnotationTO(String summarySimilarityAnnotationId, Boolean negated, 
                String ecoId, String cioId, String referenceId, String referenceTitle, 
                String supportingText, String assignedBy, String curator, Date annotationDate) 
                throws IllegalArgumentException {
            this.summarySimilarityAnnotationId = summarySimilarityAnnotationId;
            this.negated = negated;
            this.ecoId = ecoId;
            this.cioId = cioId;
            this.referenceId = referenceId;
            this.referenceTitle = referenceTitle;
            this.supportingText = supportingText;
            this.assignedBy = assignedBy;
            this.curator = curator;
            this.annotationDate = annotationDate;
        }

        /**
         * @return  the {@code String} representing the ID of the associated 'summary' 
         *          similarity annotation.
         */
        public String getSummarySimilarityAnnotationId() {
            return this.summarySimilarityAnnotationId;
        }

        /**
         * @return  the {@code Boolean} defining whether this annotation is negated (using the NOT 
         *          qualifier of the similarity annotation file: used to capture an information 
         *          rejecting a putative relation between structures, that could otherwise seem 
         *          plausible). 
         */
        public Boolean isNegated() {
            return this.negated;
        }
        
        /**
         * @return  the {@code String} representing the ID of the Evidence Ontology statement 
         *          associated to this raw similarity annotation.
         */
        public String getECOId() {
            return this.ecoId;
        }
        
        /**
         * @return  the {@code String} representing the ID of the confidence statement associated 
         *          to this raw similarity annotation.
         */
        public String getCIOId() {
            return this.cioId;
        }
        
        /**
         * @return  the {@code String} representing the ID of the source, cited as an authority 
         *          for asserting the relation.
         */
        public String getReferenceId() {
            return this.referenceId;
        }
        
        /**
         * @return  the {@code String} representing the title of the source, cited as an authority 
         *          for asserting the relation.
         */
        public String getReferenceTitle() {
            return this.referenceTitle;
        }
        
        /**
         * @return  the {@code String} representing a quote from the reference, 
         *          supporting this raw similarity annotation.
         */
        public String getSupportingText() {
            return this.supportingText;
        }
        
        /**
         * @return  the {@code String} representing the database which made 
         *          this raw similarity annotation.
         */
        public String getAssignedBy() {
            return this.assignedBy;
        }
        
        /**
         * @return  the {@code String} representing the code allowing to identify the curator 
         *          who made this annotation, from the database defined above.
         */
        public String getCurator() {
            return this.curator;
        }
        
        /**
         * @return the {@code Date} representing the date when the annotation was made.
         */
        public Date getAnnotationDate() {
            return this.annotationDate;
        }

        @Override
        public String toString() {
            return " Summary similarity annotation ID: " + this.summarySimilarityAnnotationId + 
                    " - Negated: " + this.negated + " - ECO ID: " + this.ecoId + 
                    " - CIO ID: " + this.cioId + " - Reference ID: " + this.referenceId + 
                    " - Reference title: " + this.referenceTitle + 
                    " - Supporting Text: " + this.supportingText + 
                    " - AssignedBy: " + this.assignedBy + " - Curator: " + this.curator + 
                    " - AnnotationDate: " + this.annotationDate;
        }
    }
}
