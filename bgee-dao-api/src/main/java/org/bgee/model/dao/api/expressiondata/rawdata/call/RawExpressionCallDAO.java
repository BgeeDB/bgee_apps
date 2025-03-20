package org.bgee.model.dao.api.expressiondata.rawdata.call;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.stream.Collectors;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.DAODataType;

/**
 * DAO defining queries using or retrieving {@link RawExpressionCallTO}s. 
 * 
 * @author  Valentine Rech de Laval
 * @author  Frederic Bastian
 * @version Bgee 15.0, Apr. 2021
 * @since   Bgee 14, Feb. 2017
 * @see RawExpressionCallTO
 */
public interface RawExpressionCallDAO extends DAO<RawExpressionCallDAO.Attribute> {
    
    /**
     * {@code Enum} used to define the attributes to populate in the {@code RawExpressionCallTO}s 
     * obtained from this {@code RawExpressionCallDAO}.
     * <ul>
     * <li>{@code ID}: corresponds to {@link RawExpressionCallTO#getId()}.
     * <li>{@code BGEE_GENE_ID}: corresponds to {@link RawExpressionCallTO#getBgeeGeneId()}.
     * <li>{@code CONDITION_ID}: corresponds to {@link RawExpressionCallTO#getConditionId()}.
     * <li>{@code SCORE}: corresponds to {@link RawExpressionCallTO#getScore()}.
     * <li>{@code CONDITION_ID}: corresponds to {@link RawExpressionCallTO#getConditionId()}.
     * <li>{@code CONDITION_ID}: corresponds to {@link RawExpressionCallTO#getConditionId()}.     
     * </ul>
     */
    public enum Attribute implements DAO.Attribute {
        EXPRESSION_ID("expressionId", false), BGEE_GENE_ID("bgeeGeneId", false), CONDITION_ID("conditionId", false),
        SCORE("exprScore", true), PVALUE("pValue", true), WEIGHT("weight", true);
        
        /**
         * A {@code String} that is the corresponding field name in {@code RelationTO} class.
         * @see {@link Attribute#getTOFieldName()}
         */
        private final String fieldName;
        /**
         * A {@code boolean} defining if the {@code Attribute} is data type dependant.
         * If true then the {@code RawExpressionCallTO} will have a different attribute for each
         * data type.
         */
        private final boolean dataTypeDependant;
        
        private Attribute(String fieldName, boolean dataTypeDependant) {
            this.fieldName = fieldName;
            this.dataTypeDependant = dataTypeDependant;
        }
        @Override
        public String getTOFieldName() {
            return this.fieldName;
        }
        public boolean isDataTypeDependant() {
            return dataTypeDependant;
        }
        
        public static EnumSet<Attribute> getDataTypeDependentAttributes () {
            return EnumSet.allOf(Attribute.class).stream().filter(a -> a.isDataTypeDependant())
                    .collect(Collectors.toCollection(() -> EnumSet.noneOf(Attribute.class)));
        }
        
    }

    /**
     * Allows to retrieve {@code RawExpressionCallTO}s according to the provided filters.
     * <p>
     * The {@code RawExpressionCallOTFTO}s are retrieved and returned as a
     * {@code RawExpressionCallOTFTOResultSet}. It is the responsibility of the caller to close this
     * {@code DAOResultSet} once results are retrieved.
     *
     * @param rawCallFilter     A {@code DAORawCallFilter} allowing to filter raw expression
     *                          calls to retrieve
     * @return                  A {@code RawExpressionCallTOResultSet} allowing to retrieve the
     *                          targeted {@code RawExpressionCallTO}s.
     * @throws DAOException     If an error occurred while accessing the data source.
     */
    public RawExpressionCallTOResultSet getRawExpressionCalls(DAORawCallFilter rawCallFilter) throws DAOException;

    /**
     * {@code DAOResultSet} specifics to {@code RawExpressionCallTO}s
     * 
     * @author  Valentine Rech de Laval
     * @version Bgee 14, Feb. 2017
     * @since   Bgee 14, Feb. 2017
     */
    public interface RawExpressionCallTOResultSet extends DAOResultSet<RawExpressionCallTO> {
    }

    /**
    * {@code EntityTO} representing a raw expression call in the Bgee database.
    * 
    * @author  Julien Wollbrett
    * @version Bgee 16.0, Mar. 2025
    * @since   Bgee 16.0, Mar. 2025
    */
   public class RawExpressionCallTO extends ExpressionCallTO {

       private static final long serialVersionUID = -6659443741328805241L;

       private final Map<DAODataType, DAORawCallValues> rawCallValuesPerDataType;

       public RawExpressionCallTO(Long id, Integer bgeeGeneId, Integer conditionId,
               Map<DAODataType, DAORawCallValues> rawCallValuesPerDataType) {
           super(id, bgeeGeneId, conditionId);
           this.rawCallValuesPerDataType = rawCallValuesPerDataType;
       }

        public Map<DAODataType, DAORawCallValues> getRawCallValuesPerDataType() {
            return rawCallValuesPerDataType;
        }

        @Override
        public String toString() {
            return "RawExpressionCallTO [rawCallValuesPerDataType=" + rawCallValuesPerDataType + ", getId()=" + getId()
                    + ", getBgeeGeneId()=" + getBgeeGeneId() + ", getConditionId()=" + getConditionId() + "]";
        }

   }

   /** 
    * Retrieve raw expression calls for a requested collection of gene IDs, ordered by gene IDs
    * and expression IDs.
    * 
    * @param geneIds               A {@code Collection of {@code Integer}s that are the Bgee IDs 
    *                              of the genes to retrieve calls for.
    * @return                      A {@code RawExpressionCallTOResultSet} allowing to obtain 
    *                              the requested {@code RawExpressionCallTO}s.
    * @throws DAOException             If an error occurred while accessing the data source.
    * @throws IllegalArgumentException If {@code geneIds} is {@code null} or empty.
    */
   public RawExpressionCallTOResultSet getExpressionCallsOrderedByGeneIdAndExprId(
           Collection<Integer> geneIds) throws DAOException, IllegalArgumentException;
    
    /**
     * {@code EntityTO} representing an expression call in the Bgee database.
     * 
     * @author  Valentine Rech de Laval
     * @author  Frederic Bastian
     * @version Bgee 15.0, Apr. 2021
     * @since   Bgee 14, Feb. 2017
     */
    public abstract class ExpressionCallTO extends TransferObject {
        
        private static final long serialVersionUID = -1057540315343857464L;

        private final Long id;
        /**
         * An {@code Integer} representing the ID of the gene associated to this call.
         */
        private final Integer bgeeGeneId;
        /**
         * An {@code Integer} representing the ID of the condition associated to this call.
         */
        private final Integer conditionId;
        
        public ExpressionCallTO(Long id, Integer bgeeGeneId, Integer conditionId) {
            this.id = id;
            this.bgeeGeneId = bgeeGeneId;
            this.conditionId = conditionId;
        }

        public Long getId() {
            return this.id;
        }
        public Integer getBgeeGeneId() {
            return this.bgeeGeneId;
        }
        public Integer getConditionId() {
            return this.conditionId;
        }
        
        @Override
        public String toString() {
            return "RawExpressionCallTO [id=" + this.getId() + ", bgeeGeneId=" + bgeeGeneId 
                    + ", conditionId=" + conditionId + "]";
        }
    }
}
