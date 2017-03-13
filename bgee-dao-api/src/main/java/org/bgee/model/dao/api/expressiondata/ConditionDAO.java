package org.bgee.model.dao.api.expressiondata;

import java.math.BigDecimal;
import java.util.Collection;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.EntityTO;
import org.bgee.model.dao.api.exception.DAOException;

/**
 * DAO defining queries using or retrieving {@link ConditionTO}s. 
 * 
 * @author  Valentine Rech de Laval
 * @author  Frederic Bastian
 * @version Bgee 14, Feb. 2017
 * @since   Bgee 14, Feb. 2017
 * @see ConditionTO
 */
public interface ConditionDAO extends DAO<ConditionDAO.Attribute> {

    /**
     * {@code Enum} used to define the attributes to populate in the {@code ConditionTO}s 
     * obtained from this {@code ConditionDAO}.
     * <ul>
     * <li>{@code ID}: corresponds to {@link ConditionTO#getId()}.
     * <li>{@code EXPR_MAPPED_CONDITION_ID}: corresponds to {@link ConditionTO#getExprMappedConditionId()}.
     * <li>{@code ANAT_ENTITY_ID}: corresponds to {@link ConditionTO#getAnatEntityId()}.
     * <li>{@code STAGE_ID}: corresponds to {@link ConditionTO#getStageId()}.
     * <li>{@code SPECIES_ID}: corresponds to {@link ConditionTO#getSpeciesId()}.
     * <li>{@code SEX}: corresponds to {@link ConditionTO#getSex()}.
     * <li>{@code SEX_INFERRED}: corresponds to {@link ConditionTO#getSexInferred()}.
     * <li>{@code STRAIN}: corresponds to {@link ConditionTO#getStrain()}.
     * <li>{@code AFFYMETRIX_MAX_RANK}: corresponds to {@link ConditionTO#getAffymetrixMaxRank()}.
     * <li>{@code RNA_SEQ_MAX_RANK}: corresponds to {@link ConditionTO#getRNASeqMaxRank()}.
     * <li>{@code EST_MAX_RANK}: corresponds to {@link ConditionTO#getESTMaxRank()}.
     * <li>{@code IN_SITU_MAX_RANK}: corresponds to {@link ConditionTO#getInSituMaxRank()}.
     * </ul>
     */
    public enum Attribute implements DAO.Attribute {
        ID("id", false), EXPR_MAPPED_CONDITION_ID("exprMappedConditionId", false), 
        SPECIES_ID("speciesId", false), 
        ANAT_ENTITY_ID("anatEntityId", true), STAGE_ID("stageId", true), 
        AFFYMETRIX_MAX_RANK("affymetrixMaxRank", false), RNA_SEQ_MAX_RANK("rnaSeqMaxRank", false), 
        EST_MAX_RANK("estMaxRank", false), IN_SITU_MAX_RANK("inSituMaxRank", false);

        /**
         * A {@code String} that is the corresponding field name in {@code AnatEntityTO} class.
         * @see {@link Attribute#getTOFieldName()}
         */
        private final String fieldName;
        /**
         * @see #isConditionParameter()
         */
        private final boolean conditionParameter;
        
        private Attribute(String fieldName, boolean conditionParameter) {
            this.fieldName = fieldName;
            this.conditionParameter = conditionParameter;
        }
        @Override
        public String getTOFieldName() {
            return this.fieldName;
        }
        /**
         * @return  A {@code boolean} defining whether this attribute corresponds 
         *          to a condition parameter (anat entity, stage, sex, strain), allowing to determine 
         *          which condition and expression tables to target for queries.
         */
        public boolean isConditionParameter() {
            return this.conditionParameter;
        }
    }
    
    /**
     * Retrieves conditions from data source according to a {@code Collection} of {@code Integer}s
     * that are the IDs of species allowing to filter the conditions to use.
     * <p>
     * The conditions are retrieved and returned as a {@code ConditionTOResultSet}. It is the
     * responsibility of the caller to close this {@code DAOResultSet} once results are retrieved.
     * 
     * @param speciesIds            A {@code Collection} of {@code Integer}s that are the IDs of species 
     *                              allowing to filter the conditions to use.
     * @param conditionParameters   A {@code Collection} of {@code ConditionDAO.Attribute}s defining the
     *                              combination of condition parameters that were requested for queries, 
     *                              allowing to determine which condition and expression tables to target
     *                              (see {@link Attribute#isConditionParameter()}).
     *                              It is different from {@code attributes}, because you might want 
     *                              to retrieve, for instance, only anatomical entity IDs, 
     *                              while your expression query was using a stage ID parameter for filtering, 
     *                              and thus the targeted tables must include information for both 
     *                              anatomical entities and stages.
     * @param attributes            A {@code Collection} of {@code ConditionDAO.Attribute}s defining the
     *                              attributes to populate in the returned {@code ConditionTO}s.
     *                              If {@code null} or empty, all attributes are populated. 
     * @return                      An {@code ConditionTOResultSet} containing all conditions 
     *                              from data source.
     * @throws DAOException If an error occurred when accessing the data source.
     * @throws IllegalArgumentException If one of the {@code Attribute}s in {@code conditionParameters}
     *                                  is not a condition parameter attributes (see 
     *                                  {@link Attribute#isConditionParameter()}). 
     */
    public ConditionTOResultSet getConditionsBySpeciesIds(Collection<Integer> speciesIds,
        Collection<Attribute> conditionParameters, Collection<Attribute> attributes) 
            throws DAOException, IllegalArgumentException;
    
    /**
     * Retrieve the maximum of condition IDs in the appropriate table specified by {@code conditionParameters}.
     * 
     * @param conditionParameters   A {@code Collection} of {@code ConditionDAO.Attribute}s defining the
     *                              combination of condition parameters that were requested for queries, 
     *                              allowing to determine which condition and expression tables to target
     *                              (see {@link Attribute#isConditionParameter()}). This is to make sure
     *                              that attributes requested for insertion are not accidentally missing 
     *                              from the {@code ConditionTO}s.
     * @return                      An {@code int} that is maximum of condition IDs in the appropriate table.
     *                              If there is no condition, return 0.
     * @throws DAOException             If an error occurred when accessing the data source. 
     * @throws IllegalArgumentException If one of the {@code Attribute}s in {@code conditionParameters}
     *                                  is not a condition parameter attributes (see 
     *                                  {@link Attribute#isConditionParameter()}).
     */
    public int getMaxConditionId(Collection<Attribute> conditionParameters) 
            throws DAOException, IllegalArgumentException;
    
    /**
     * Insert into the datasource the provided {@code ConditionTO}s. Which condition table 
     * should be targeted will be determined by {@code conditionParameters}. 
     * 
     * @param conditionTOs          A {@code Collection} of {@code ConditionTO}s to be inserted 
     *                              into the datasource.
     * @param conditionParameters   A {@code Collection} of {@code ConditionDAO.Attribute}s defining the
     *                              combination of condition parameters that were requested for queries, 
     *                              allowing to determine which condition and expression tables to target
     *                              (see {@link Attribute#isConditionParameter()}).
     * @return                      An {@code int} that is the number of conditions inserted.
     * @throws DAOException If an error occurred while inserting the conditions.
     * @throws IllegalArgumentException If an attribute necessary for the targeted tables is missing, 
     *                                  or if one of the {@code Attribute}s in {@code conditionParameters}
     *                                  is not a condition parameter attributes (see 
     *                                  {@link Attribute#isConditionParameter()}).
     */
    public int insertConditions(Collection<ConditionTO> conditionTOs, 
            Collection<Attribute> conditionParameters) throws DAOException, IllegalArgumentException;
        
    /**
     * {@code DAOResultSet} specifics to {@code ConditionTO}s
     * 
     * @author  Valentine Rech de Laval
     * @version Bgee 14, Feb. 2017
     * @since   Bgee 14, Feb. 2017
     */
    public interface ConditionTOResultSet extends DAOResultSet<ConditionTO> {
    }
    
    /**
     * {@code EntityTO} representing a condition in the Bgee database.
     * 
     * @author  Valentine Rech de Laval
     * @version Bgee 14, Feb. 2017
     * @since   Bgee 14, Feb. 2017
     */
    public class ConditionTO extends EntityTO<Integer> {

        private static final long serialVersionUID = -1057540315343857464L;

        private final Integer exprMappedConditionId;
        private final String anatEntityId;
        private final String stageId;
        private final Integer speciesId;
        /**
         * @see #getAffymetrixMaxRank()
         */
        private final BigDecimal affymetrixMaxRank;
        /**
         * @see #getRNASeqMaxRank()
         */
        private final BigDecimal rnaSeqMaxRank;
        /**
         * @see #getESTMaxRank()
         */
        private final BigDecimal estMaxRank;
        /**
         * @see #getInSituMaxRank()
         */
        private final BigDecimal inSituMaxRank;
        
        public ConditionTO(Integer id, Integer exprMappedConditionId, String anatEntityId,
            String stageId, Integer speciesId, BigDecimal affymetrixMaxRank, 
            BigDecimal rnaSeqMaxRank, BigDecimal estMaxRank, BigDecimal inSituMaxRank) {
            super(id);
            this.exprMappedConditionId = exprMappedConditionId;
            this.anatEntityId = anatEntityId;
            this.stageId = stageId;
            this.speciesId = speciesId;
            this.affymetrixMaxRank = affymetrixMaxRank;
            this.rnaSeqMaxRank = rnaSeqMaxRank;
            this.estMaxRank = estMaxRank;
            this.inSituMaxRank = inSituMaxRank;
        }
        
        /**
         * @return  The {@code Integer} that is the condition ID that should be used for insertion
         *          into the expression table: too-granular conditions (e.g., 43 yo human stage,
         *          or sexInferred=1) are mapped to less granular conditions for summary.
         *          Equal to {@code #getId()} if condition is not too granular.
         */
        public Integer getExprMappedConditionId() {
            return exprMappedConditionId;
        }
        /**
         * @return  The {@code String} that is the Uberon anatomical entity ID.
         */
        public String getAnatEntityId() {
            return anatEntityId;
        }
        /**
         * @return  The {@code String} that is the Uberon stage ID.
         */
        public String getStageId() {
            return stageId;
        }
        /**
         * @return  The {@code String} that is the NCBI species taxon ID.
         */
        public Integer getSpeciesId() {
            return speciesId;
        }

        /**
         * @return  A {@code BigDecimal} that is the max rank in this condition, over all genes, 
         *          based on Affymetrix data.
         */
        public BigDecimal getAffymetrixMaxRank() {
            return affymetrixMaxRank;
        }
        /**
         * @return  A {@code BigDecimal} that is the max rank in this condition, over all genes, 
         *          based on RNA-Seq data.
         */
        public BigDecimal getRNASeqMaxRank() {
            return rnaSeqMaxRank;
        }
        /**
         * @return  A {@code BigDecimal} that is the max rank in this condition, over all genes, 
         *          based on EST data.
         */
        public BigDecimal getESTMaxRank() {
            return estMaxRank;
        }
        /**
         * @return  A {@code BigDecimal} that is the max rank in this condition, over all genes, 
         *          based on in situ data.
         */
        public BigDecimal getInSituMaxRank() {
            return inSituMaxRank;
        }
        
        @Override
        public String toString() {
            return "ConditionTO [id=" + getId() + ", exprMappedConditionId=" + exprMappedConditionId
                + ", anatEntityId=" + anatEntityId + ", stageId=" + stageId + ", speciesId=" + speciesId
                + ", affymetrixMaxRank=" + affymetrixMaxRank + ", rnaSeqMaxRank=" + rnaSeqMaxRank
                + ", estMaxRank=" + estMaxRank + ", inSituMaxRank=" + inSituMaxRank + "]";
        }
    }
}
