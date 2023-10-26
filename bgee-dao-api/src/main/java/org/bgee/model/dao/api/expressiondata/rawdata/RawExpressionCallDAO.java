package org.bgee.model.dao.api.expressiondata.rawdata;

import java.util.Collection;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.exception.DAOException;

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
     * </ul>
     */
    public enum Attribute implements DAO.Attribute {
        ID("id"), BGEE_GENE_ID("bgeeGeneId"), CONDITION_ID("conditionId");
        
        /**
         * A {@code String} that is the corresponding field name in {@code RelationTO} class.
         * @see {@link Attribute#getTOFieldName()}
         */
        private final String fieldName;
        
        private Attribute(String fieldName) {
            this.fieldName = fieldName;
        }
        @Override
        public String getTOFieldName() {
            return this.fieldName;
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
     * @author  Valentine Rech de Laval
     * @author  Frederic Bastian
     * @version Bgee 15.0, Apr. 2021
     * @since   Bgee 14, Feb. 2017
     */
    public class RawExpressionCallTO extends TransferObject {
        
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
        
        public RawExpressionCallTO(Long id, Integer bgeeGeneId, Integer conditionId) {
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
