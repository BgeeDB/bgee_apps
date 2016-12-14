package org.bgee.model.dao.api.expressiondata;

import java.util.Collection;
import java.util.LinkedHashMap;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.EntityTO;
import org.bgee.model.dao.api.exception.DAOException;

/**
 * DAO defining queries using or retrieving {@link ExperimentExpressionTO}s. 
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 13, Dec. 2016
 * @since   Bgee 13, Dec. 2016
 */
public interface ExperimentExpressionDAO extends DAO<ExperimentExpressionDAO.Attribute> {
    
    /**
     * The attributes available for {@code ExperimentExpressionTO}
     * <ul>
     *   <li>@{code ID} corresponds to {@link ExperimentExpressionTO#getId()}
     *   <li>@{code EXPRESSION_ID} corresponds to {@link ExperimentExpressionTO#getExpressionId()}
     *   <li>@{code PRESENT_HIGH_COUNT} corresponds to {@link ExperimentExpressionTO#getPresentHighCount()}}
     *   <li>@{code PRESENT_LOW_COUNT} corresponds to {@link ExperimentExpressionTO#getPresentLowCount()}
     *   <li>@{code ABSENT_HIGH_COUNT} corresponds to {@link ExperimentExpressionTO#getAbsentHighCount()}
     *   <li>@{code EXPERIMENT_COUNT} corresponds to {@link ExperimentExpressionTO#getExperimentCount()}
     * </ul>
     */
    enum Attribute implements DAO.Attribute {
        ID, EXPRESSION_ID, PRESENT_HIGH_COUNT, PRESENT_LOW_COUNT, ABSENT_HIGH_COUNT, EXPERIMENT_COUNT;
    }

    /**
     * The attributes available to order retrieved {@code ExperimentExpressionTO}s
     * <ul>
     *   <li>@{code ID} uses {@link ExperimentExpressionTO#getId()}
     * </ul>
     */
    enum OrderingAttribute implements DAO.OrderingAttribute {
        ID
    }
    
    /**
     * Retrieve affymetrix experiment expressions from the data source.
     * <p>
     * The expressions are retrieved and returned as an {@code ExperimentExpressionTOResultSet}.
     * It is the responsibility of the caller to close this {@code DAOResultSet} once results are retrieved.
     * 
     * @param attributes            A {@code Collection} of {@code ExperimentExpressionDAO.Attribute}s 
     *                              defining the attributes to populate in the returned 
     *                              {@code ExperimentExpressionTO}s. If {@code null} or empty, 
     *                              all attributes are populated. 
     * @param orderingAttributes    A {@code LinkedHashMap} where keys are 
     *                              {@code ExperimentExpressionDAO.OrderingAttribute}s defining 
     *                              the attributes used to order the returned {@code ExperimentExpressionTO}s, 
     *                              the associated value being a {@code DAO.Direction} 
     *                              defining whether the ordering should be ascendant or descendant.
     *                              If {@code null} or empty, then no ordering is performed. 
     * @return                      An {@code ExperimentExpressionTOResultSet} allowing to obtain 
     *                              the requested {@code ExperimentExpressionTO}s.
     * @throws DAOException             If an error occurred while accessing the data source. 
     */
    public ExperimentExpressionTOResultSet getAffymetrixExperimentExpressions(Collection<Attribute> attributes, 
            LinkedHashMap<OrderingAttribute, DAO.Direction> orderingAttributes) throws DAOException;

    /**
     * Retrieve RNA-Seq experiment expressions from the data source.
     * <p>
     * The expressions are retrieved and returned as an {@code ExperimentExpressionTOResultSet}.
     * It is the responsibility of the caller to close this {@code DAOResultSet} once results are retrieved.
     * 
     * @param attributes            A {@code Collection} of {@code ExperimentExpressionDAO.Attribute}s 
     *                              defining the attributes to populate in the returned 
     *                              {@code ExperimentExpressionTO}s. If {@code null} or empty, 
     *                              all attributes are populated. 
     * @param orderingAttributes    A {@code LinkedHashMap} where keys are 
     *                              {@code ExperimentExpressionDAO.OrderingAttribute}s defining 
     *                              the attributes used to order the returned {@code ExperimentExpressionTO}s, 
     *                              the associated value being a {@code DAO.Direction} 
     *                              defining whether the ordering should be ascendant or descendant.
     *                              If {@code null} or empty, then no ordering is performed. 
     * @return                      An {@code ExperimentExpressionTOResultSet} allowing to obtain 
     *                              the requested {@code ExperimentExpressionTO}s.
     * @throws DAOException             If an error occurred while accessing the data source. 
     */
    public ExperimentExpressionTOResultSet getRNASeqExperimentExpressions(Collection<Attribute> attributes, 
            LinkedHashMap<OrderingAttribute, DAO.Direction> orderingAttributes) throws DAOException;

    /**
     * Retrieve EST experiment expressions from the data source.
     * <p>
     * The expressions are retrieved and returned as an {@code ExperimentExpressionTOResultSet}.
     * It is the responsibility of the caller to close this {@code DAOResultSet} once results are retrieved.
     * 
     * @param attributes            A {@code Collection} of {@code ExperimentExpressionDAO.Attribute}s 
     *                              defining the attributes to populate in the returned 
     *                              {@code ExperimentExpressionTO}s. If {@code null} or empty, 
     *                              all attributes are populated. 
     * @param orderingAttributes    A {@code LinkedHashMap} where keys are 
     *                              {@code ExperimentExpressionDAO.OrderingAttribute}s defining 
     *                              the attributes used to order the returned {@code ExperimentExpressionTO}s, 
     *                              the associated value being a {@code DAO.Direction} 
     *                              defining whether the ordering should be ascendant or descendant.
     *                              If {@code null} or empty, then no ordering is performed. 
     * @return                      An {@code ExperimentExpressionTOResultSet} allowing to obtain 
     *                              the requested {@code ExperimentExpressionTO}s.
     * @throws DAOException             If an error occurred while accessing the data source. 
     */
    public ExperimentExpressionTOResultSet getESTExperimentExpressions(Collection<Attribute> attributes, 
            LinkedHashMap<OrderingAttribute, DAO.Direction> orderingAttributes) throws DAOException;

    /**
     * Retrieve <em>in situ</em> experiment expressions from the data source.
     * <p>
     * The expressions are retrieved and returned as an {@code ExperimentExpressionTOResultSet}.
     * It is the responsibility of the caller to close this {@code DAOResultSet} once results are retrieved.
     * 
     * @param attributes            A {@code Collection} of {@code ExperimentExpressionDAO.Attribute}s 
     *                              defining the attributes to populate in the returned 
     *                              {@code ExperimentExpressionTO}s. If {@code null} or empty, 
     *                              all attributes are populated. 
     * @param orderingAttributes    A {@code LinkedHashMap} where keys are 
     *                              {@code ExperimentExpressionDAO.OrderingAttribute}s defining 
     *                              the attributes used to order the returned {@code ExperimentExpressionTO}s, 
     *                              the associated value being a {@code DAO.Direction} 
     *                              defining whether the ordering should be ascendant or descendant.
     *                              If {@code null} or empty, then no ordering is performed. 
     * @return                      An {@code ExperimentExpressionTOResultSet} allowing to obtain 
     *                              the requested {@code ExperimentExpressionTO}s.
     * @throws DAOException             If an error occurred while accessing the data source. 
     */
    public ExperimentExpressionTOResultSet getInSituExperimentExpressions(Collection<Attribute> attributes, 
            LinkedHashMap<OrderingAttribute, DAO.Direction> orderingAttributes) throws DAOException;

    /**
     * {@code DAOResultSet} specifics to {@code ExperimentExpressionTO}s
     * 
     * @author  Valentine Rech de Laval
     * @version Bgee 13, Dec. 2016
     * @since   Bgee 13, Dec. 2016
     */
    public interface ExperimentExpressionTOResultSet extends DAOResultSet<ExperimentExpressionTO> {
    }
    
    /**
     * {@code TransferObject} representing an experiment expression in the Bgee database.
     * 
     * @author  Valentine Rech de Laval
     * @version Bgee 13, Dec. 2016
     * @since   Bgee 13, Dec. 2016
     */
    public class ExperimentExpressionTO extends EntityTO {

        private static final long serialVersionUID = 3464643420374159955L;

        /**
         * An {@code Integer} that is the experiment ID of this experiment expression.
         */
        private final Integer experimentId;
        
        /**
         * An {@code Integer} that is the count of experiments that produced
         * this experiment expression as present high.
         */
        private final Integer presentHighCount;
        
        /**
         * An {@code Integer} that is the count of experiments that produced
         * this experiment expression as present low.
         */
        private final Integer presentLowCount;
        
        /**
         * An {@code Integer} that is the count of experiments that produced
         * this experiment expression as absent high.
         */
        private final Integer absentHighCount;
        
        /**
         * An {@code Integer} that is the number of experiments with that exact combination of counts 
         * of present high/present low/absent high/absent low that produced this experiment expression.
         */
        private final Integer experimentCount;
        
        /**
         * Constructor providing the experiment ID and counts of experiments that produced
         * this experiment expression.
         * <p>
         * All of these parameters are optional, so they can be {@code null} when not used.
         * Other attributes are set to {@code null}.
         * 
         * @param id                A {@code String} that is the ID of this {@code ExperimentExpressionTO}.
         * @param experimentId      An {@code Integer} that is the experiment ID.
         * @param presentHighCount  An {@code Integer} that is the count of experiments that
         *                          produced this experiment expression as present high.
         * @param presentLowCount   An {@code Integer} that is the count of experiments that
         *                          produced this experiment expression as present low.
         * @param absentHighCount   An {@code Integer} that is the count of experiments 
         *                          produced this experiment expression as absent high.
         * @param experimentCount   An {@code Integer} that is the number of experiments with that 
         *                          exact combination of counts of present high/present low/absent 
         *                          high/absent low that produced this experiment expression.
         */
        public ExperimentExpressionTO(String id, Integer experimentId, Integer presentHighCount,
            Integer presentLowCount, Integer absentHighCount, Integer experimentCount) {
            super(id);
            this.experimentId = experimentId;
            this.presentHighCount = presentHighCount;
            this.presentLowCount = presentLowCount;
            this.absentHighCount = absentHighCount;
            this.experimentCount = experimentCount;
        }

        /**
         * @return  The {@code Integer} that is the experiment ID of this experiment expression.
         */
        public Integer getExperimentId() {
            return experimentId;
        }

        /**
         * @return  The {@code Integer} that is the count of experiments that produced
         *          this experiment expression as present high
         */
        public Integer getPresentHighCount() {
            return presentHighCount;
        }

        /**
         * @return  The {@code Integer} that is the count of experiments that produced
         *          this experiment expression as present low
         */
        public Integer getPresentLowCount() {
            return presentLowCount;
        }

        /**
         * @return  The {@code Integer} that is the count of experiments that produced
         *          this experiment expression as absent high
         */
        public Integer getAbsentHighCount() {
            return absentHighCount;
        }

        /**
         * @return  The {@code Integer} that is the number of experiments with that
         *          exact combination of counts of present high/present low/absent high/absent low 
         *          that produced this experiment expression.
         */
        public Integer getExperimentCount() {
            return experimentCount;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + ((experimentId == null) ? 0 : experimentId.hashCode());
            result = prime * result + ((presentHighCount == null) ? 0 : presentHighCount.hashCode());
            result = prime * result + ((presentLowCount == null) ? 0 : presentLowCount.hashCode());
            result = prime * result + ((absentHighCount == null) ? 0 : absentHighCount.hashCode());
            result = prime * result + ((experimentCount == null) ? 0 : experimentCount.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!super.equals(obj))
                return false;
            if (getClass() != obj.getClass())
                return false;
            ExperimentExpressionTO other = (ExperimentExpressionTO) obj;
            if (experimentId == null) {
                if (other.experimentId != null)
                    return false;
            } else if (!experimentId.equals(other.experimentId))
                return false;
            if (presentHighCount == null) {
                if (other.presentHighCount != null)
                    return false;
            } else if (!presentHighCount.equals(other.presentHighCount))
                return false;
            if (presentLowCount == null) {
                if (other.presentLowCount != null)
                    return false;
            } else if (!presentLowCount.equals(other.presentLowCount))
                return false;
            if (absentHighCount == null) {
                if (other.absentHighCount != null)
                    return false;
            } else if (!absentHighCount.equals(other.absentHighCount))
                return false;
            if (experimentCount == null) {
                if (other.experimentCount != null)
                    return false;
            } else if (!experimentCount.equals(other.experimentCount))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return super.toString() + " - Experiment ID: " + getExperimentId();
//                + " - Present high count: " + getPresentHighCount(),
//                + " - Present low count: " + getPresentLowCount() 
//                + " - Absent high count: " + getAbsentHighCount()
//                + " - Experiment count: " + getExperimentCount();
        }
    }
}
