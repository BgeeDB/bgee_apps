package org.bgee.model.dao.api.ontologycommon;

import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.EntityTO;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.exception.DAOException;


/**
 * DAO defining queries using or retrieving {@link CIOStatementTO}s. 
 *
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @see CIOStatementTO
 * @since Bgee 13
 */
public interface CIOStatementDAO extends DAO<CIOStatementDAO.Attribute> {
    
    /**
     * {@code Enum} used to define the attributes to populate in the {@code CIOStatementTO}s 
     * obtained from this {@code CIOStatementDAO}.
     * <ul>
     * <li>{@code ID}: corresponds to {@link CIOStatementDAO#getId()}.
     * <li>{@code NAME}: corresponds to {@link CIOStatementDAO#getName()}.
     * <li>{@code DESCRIPTION}: corresponds to {@link CIOStatementDAO#getDescription()}.
     * <li>{@code TRUSTED}: corresponds to {@link CIOStatementDAO#isTrusted()}.
     * </ul>
     * @see org.bgee.model.dao.api.DAO#setAttributes(Collection)
     * @see org.bgee.model.dao.api.DAO#setAttributes(Enum[])
     * @see org.bgee.model.dao.api.DAO#clearAttributes()
     */
    public enum Attribute implements DAO.Attribute {
        ID, NAME, DESCRIPTION, TRUSTED, CONFIDENCE_LEVEL, 
        EVIDENCE_CONCORDANCE, EVIDENCE_TYPE_CONCORDANCE;
    }
    
    /**
     * Retrieves all CIO statements from data source.
     * <p>
     * The CIO statements are retrieved and returned as a {@code CIOStatementTOResultSet}. It is 
     * the responsibility of the caller to close this {@code DAOResultSet} once results are 
     * retrieved.
     * 
     * @return              An {@code CIOStatementTOResultSet} containing all CIO statements
     *                      from data source.
     * @throws DAOException If an error occurred when accessing the data source. 
     */
    public CIOStatementTOResultSet getAllCIOStatements() throws DAOException;

    /**
     * Inserts the provided CIO statements into the data source, 
     * represented as a {@code Collection} of {@code CIOStatementTO}s. 
     * 
     * @param cioStatementTOs   A {@code Collection} of {@code CIOStatementTO}s to be inserted into 
     *                          the data source.
     * @return                  An {@code int} that is the number of inserted CIO statements.
     * @throws IllegalArgumentException If {@code cioStatementTOs} is empty or null. 
     * @throws DAOException             If a {@code SQLException} occurred while trying to insert 
     *                                  CIO statements. The {@code SQLException} will be 
     *                                  wrapped into a {@code DAOException} ({@code DAO}s do not 
     *                                  expose these kind of implementation details).
     */
    public int insertCIOStatements(Collection<CIOStatementTO> cioStatementTOs) 
            throws DAOException, IllegalArgumentException;

    /**
     * {@code DAOResultSet} specifics to {@code CIOStatementTO}s
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public interface CIOStatementTOResultSet extends DAOResultSet<CIOStatementTO> {
    }

    /**
     * An {@code EntityTO} representing a CIO term, as stored in the Bgee database. 
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public final class CIOStatementTO extends EntityTO {

        private static final long serialVersionUID = 7509933615802695073L;

        /**
         * {@code Logger} of the class. 
         */
        private final static Logger log = LogManager.getLogger(CIOStatementTO.class.getName());

        /**
         * An {@code Enum} used to define the level of confidence that can be put in a CIO statement.
         * These {@code Enum} fields correspond exactly to the labels of the relevant classes, 
         * leaves of the branch 'confidence level'.
         * <ul>
         * <li>{@code LOW_CONFIDENCE}:      the level of confidence is low.
         * <li>{@code MEDIUM_CONFIDENCE}:   the level of confidence is medium.
         * <li>{@code HIGH_CONFIDENCE}:     the level of confidence is high.
         *
         * @author 	Valentine Rech de Laval
         * @version Bgee 13
         * @since 	Bgee 13
         */
        public enum ConfidenceLevel implements EnumDAOField {
            LOW_CONFIDENCE("low confidence level"), MEDIUM_CONFIDENCE("medium confidence level"), 
            HIGH_CONFIDENCE("high confidence level");

            /**
             * Convert the {@code String} representation of a confidence level (for instance, 
             * retrieved from a database) into a {@code ConfidenceLevel}. Operation performed 
             * by calling {@link TransferObject#convert(Class, String)} with {@code ConfidenceLevel} 
             * as the {@code Class} argument, and {@code representation} 
             * as the {@code String} argument.
             * 
             * @param representation            A {@code String} representing a confidence level.
             * @return                          A {@code ConfidenceLevel} corresponding to 
             *                                  {@code representation}.
             * @throw IllegalArgumentException  If {@code representation} does not correspond 
             *                                  to any {@code ConfidenceLevel}.
             */
            public static final ConfidenceLevel convertToConfidenceLevel(String representation) {
                log.entry(representation);
                return log.exit(TransferObject.convert(ConfidenceLevel.class, representation));
            }

            /**
             * See {@link #getStringRepresentation()}
             */
            private final String stringRepresentation;

            /**
             * Constructor providing the {@code String} representation 
             * of this {@code ConfidenceLevel}.
             * 
             * @param stringRepresentation  A {@code String} corresponding to 
             *                              this {@code ConfidenceLevel}.
             */
            private ConfidenceLevel(String stringRepresentation) {
                this.stringRepresentation = stringRepresentation;
            }

            @Override
            public String getStringRepresentation() {
                return this.stringRepresentation;
            }
            @Override
            public String toString() {
                return this.getStringRepresentation();
            }
        }
        
        /**
         * An {@code Enum} used to define, when there are several evidence lines available related 
         * to a same assertion, whether there are of a same or different experimental or 
         * computational types. These {@code Enum} fields correspond exactly to the labels of the 
         * relevant classes, leaves of the branch 'evidence type concordance'.
         * 
         * <ul>
         * <li>{@code SINGLE_EVIDENCE}:         there is a single evidence line available.
         * <li>{@code CONGRUENT}:               there are several evidence lines available available 
         *                                      related to a same assertion which are congruent.
         * <li>{@code WEAKLY_CONFLICTING}:      there are several evidence lines available available 
         *                                      related to a same assertion which are weakly 
         *                                      conflicting.
         * <li>{@code STRONGLY_CONFLICTING}:    there are several evidence lines available available 
         *                                      related to a same assertion which are strongly 
         *                                      conflicting.
         * </ul>
         * 
         * @author  Valentine Rech de Laval
         * @version Bgee 13
         * @since   Bgee 13
         */
        public enum EvidenceConcordance implements EnumDAOField {
            SINGLE_EVIDENCE("single evidence"), CONGRUENT("congruent"), 
            WEAKLY_CONFLICTING("weakly conflicting"), STRONGLY_CONFLICTING("strongly conflicting");

            /**
             * Convert the {@code String} representation of a evidence concordance (for instance, 
             * retrieved from a database) into an {@code EvidenceConcordance}. Operation performed 
             * by calling {@link TransferObject#convert(Class, String)} with 
             * {@code EvidenceConcordance} as the {@code Class} argument, and {@code representation} 
             * as the {@code String} argument.
             * 
             * @param representation            A {@code String} representing a evidence concordance.
             * @return                          An {@code EvidenceConcordance} corresponding to 
             *                                  {@code representation}.
             * @throw IllegalArgumentException  If {@code representation} does not correspond 
             *                                  to any {@code EvidenceConcordance}.
             */
            public static final EvidenceConcordance convertToEvidenceConcordance(
                    String representation) {
                log.entry(representation);
                return log.exit(TransferObject.convert(EvidenceConcordance.class, representation));
            }

            /**
             * See {@link #getStringRepresentation()}
             */
            private final String stringRepresentation;

            /**
             * Constructor providing the {@code String} representation 
             * of this {@code EvidenceConcordance}.
             * 
             * @param stringRepresentation  A {@code String} corresponding to 
             *                              this {@code EvidenceConcordance}.
             */
            private EvidenceConcordance(String stringRepresentation) {
                this.stringRepresentation = stringRepresentation;
            }

            @Override
            public String getStringRepresentation() {
                return this.stringRepresentation;
            }
            @Override
            public String toString() {
                return this.getStringRepresentation();
            }
        }

        /**
         * An {@code Enum} used to define whether there are multiple evidence lines available 
         * related to an assertion, and whether they are congruent or conflicting. 
         * These {@code Enum} fields correspond exactly to the labels of the relevant classes, 
         * leaves of the branch 'evidence concordance'.
         * 
         * <ul>
         * <li>{@code SAME_TYPE}:       the evidence lines available related to this assertion are 
         *                              of a same experimental or computational types.
         * <li>{@code DIFFERENT_TYPE}:  the evidence lines available related to this assertion are 
         *                              of a different experimental or computational types.
         * </ul>
         *
         * @author  Valentine Rech de Laval
         * @version Bgee 13
         * @since   Bgee 13
         */
        public enum EvidenceTypeConcordance implements EnumDAOField {
            SAME_TYPE("same type"), DIFFERENT_TYPE("different type");

            /**
             * Convert the {@code String} representation of a evidence type concordance 
             * (for instance, retrieved from a database) into an {@code EvidenceTypeConcordance}. 
             * Operation performed by calling {@link TransferObject#convert(Class, String)} with 
             * {@code EvidenceTypeConcordance} as the {@code Class} argument, and 
             * {@code representation} as the {@code String} argument.
             * 
             * @param representation            A {@code String} representing a evidence type 
             *                                  concordance.
             * @return                          An {@code EvidenceTypeConcordance} corresponding to 
             *                                  {@code representation}.
             * @throw IllegalArgumentException  If {@code representation} does not correspond 
             *                                  to any {@code EvidenceTypeConcordance}.
             */
            public static final EvidenceTypeConcordance convertToEvidenceTypeConcordance(
                    String representation) {
                log.entry(representation);
                return log.exit(TransferObject.convert(EvidenceTypeConcordance.class, representation));
            }

            /**
             * See {@link #getStringRepresentation()}
             */
            private final String stringRepresentation;

            /**
             * Constructor providing the {@code String} representation 
             * of this {@code EvidenceTypeConcordance}.
             * 
             * @param stringRepresentation  A {@code String} corresponding to 
             *                              this {@code EvidenceTypeConcordance}.
             */
            private EvidenceTypeConcordance(String stringRepresentation) {
                this.stringRepresentation = stringRepresentation;
            }

            @Override
            public String getStringRepresentation() {
                return this.stringRepresentation;
            }
            
            @Override
            public String toString() {
                return this.getStringRepresentation();
            }
        }

        /**
         * A {@code Boolean} defining whether this CIO term is used to capture a trusted evidence 
         * line ({@code true}), or whether it indicates that the evidence should not be trusted 
         * ({@code false}).
         */
        private final Boolean trusted;
        
        /**
         * A {@code ConfidenceLevel} defining the level of confidence that can be put 
         * in this CIO statement. 
         */
        private final ConfidenceLevel confidenceLevel;
        
        /**
         * An {@code EvidenceConcordance} defining, when there are several evidence lines available 
         * related to a same assertion, whether there are of a same or different experimental or 
         * computational types.
         */
        private final EvidenceConcordance evidenceConcordance;
        
        /**
         * An {@code EvidenceTypeConcordance} defining whether there are multiple evidence lines 
         * available related to an assertion, and whether they are congruent or conflicting. 
         * <p>
         * It is only applicable when a statement doesn't have an {@code EvidenceConcordance} equals
         * to {@code SINGLE_EVIDENCE} (so this field is {@code null} for, and only for, 
         * {@code EvidenceConcordance} from {@code SINGLE_EVIDENCE}).
         */
        private final EvidenceTypeConcordance evidenceTypeConcordance;
        
        /**
         * Constructor providing the ID, the name, the description, and the {@code Boolean} defining
         * whether this CIO term is used to capture a trusted evidence line, or whether it indicates
         * that the evidence should not be trusted.
         * <p>
         * All of these parameters are optional, so they can be {@code null} when not used.
         * 
         * @param id                        A {@code String} that is the ID of this CIO term. 
         * @param name                      A {@code String} that is the name of this CIO term.
         * @param description               A {@code String} that is the description of this CIO term.
         * @param trusted                   A {@code Boolean} defining whether this CIO term is 
         *                                  used to capture a trusted evidence line ({@code true}), 
         *                                  or whether it indicates that the evidence should not be 
         *                                  trusted ({@code false}).
         * @param confidenceLevel           A {@code ConfidenceLevel} defining the level of 
         *                                  confidence that can be put in this CIO statement. 
         * @param evidenceConcordance       An {@code EvidenceConcordance} defining, when there are 
         *                                  several evidence lines available related to a same 
         *                                  assertion, whether there are of a same or different 
         *                                  experimental or computational types.
         * @param evidenceTypeConcordance   An {@code EvidenceTypeConcordance} defining whether 
         *                                  there are multiple evidence lines available related to 
         *                                  an assertion, and whether they are congruent or 
         *                                  conflicting.
         * @throws IllegalArgumentException If {@code id} is empty.
         */
        public CIOStatementTO(String id, String name, String description, Boolean trusted,
                ConfidenceLevel confidenceLevel, EvidenceConcordance evidenceConcordance,
                EvidenceTypeConcordance evidenceTypeConcordance) 
                throws IllegalArgumentException {
            super(id, name, description);
            this.trusted = trusted;
            this.confidenceLevel = confidenceLevel;
            this.evidenceConcordance = evidenceConcordance;
            this.evidenceTypeConcordance = evidenceTypeConcordance;
        }

        /**
         * @return  the {@code Boolean} defining whether this CIO term is used to capture a trusted 
         *          evidence line ({@code true}), or whether it indicates that the evidence should 
         *          not be trusted ({@code false}).
         */
        public Boolean isTrusted() {
            return this.trusted;
        }

        /**
         * @return  the {@code ConfidenceLevel} defining the level of 
         *          confidence that can be put in this CIO statement. 
         */
        public ConfidenceLevel getConfidenceLevel() {
            return this.confidenceLevel;
        }
        
        /**
         * @return  the {@code EvidenceConcordance} defining, when there are several evidence lines
         *          available related to a same assertion, whether there are of a same or different
         *          experimental or computational types.
         */
        public EvidenceConcordance getEvidenceConcordance() {
            return this.evidenceConcordance;
        }
        
        /**
         * @return  the {@code EvidenceTypeConcordance} defining whether there are multiple 
         *          evidence lines available related to an assertion, and whether they are 
         *          congruent or conflicting. 
         */
        public EvidenceTypeConcordance getEvidenceTypeConcordance() {
            return this.evidenceTypeConcordance;
        }
        
        @Override
        public String toString() {
            return super.toString() + " - Trusted: " + trusted + 
                    " - Confidence level: " + confidenceLevel + 
                    " - Evidence concordance: " + evidenceConcordance + 
                    " - Evidence type concordance: " + evidenceTypeConcordance;
        }
    }
}
