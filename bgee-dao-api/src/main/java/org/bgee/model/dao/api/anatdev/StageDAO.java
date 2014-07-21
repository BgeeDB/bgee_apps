package org.bgee.model.dao.api.anatdev;

import java.util.Collection;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.ontologycommon.NestedSetModelElementTO;

/**
 * DAO defining queries using or retrieving {@link StageTO}s. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @see StageTO
 * @since Bgee 01
 */
public interface StageDAO extends DAO<StageDAO.Attribute> {

    /**
     * {@code Enum} used to define the attributes to populate in the {@code StageTO}s 
     * obtained from this {@code StageDAO}.
     * <ul>
     * <li>{@code ID}: corresponds to {@link StageTO#getId()}.
     * <li>{@code NAME}: corresponds to {@link StageTO#getName()}.
     * <li>{@code DESCRIPTION}: corresponds to {@link StageTO#getDescription()}.
     * <li>{@code LEFTBOUND}: corresponds to {@link 
     * org.bgee.model.dao.api.ontologycommon.NestedSetModelElementTO#getLeftBound() 
     * NestedSetModelElementTO#getLeftBound()}.
     * <li>{@code RIGHTBOUND}: corresponds to {@link 
     * org.bgee.model.dao.api.ontologycommon.NestedSetModelElementTO#getRightBound() 
     * NestedSetModelElementTO#getRightBound()}.
     * <li>{@code LEVEL}: corresponds to {@link 
     * org.bgee.model.dao.api.ontologycommon.NestedSetModelElementTO#getLevel() 
     * NestedSetModelElementTO#getLevel()}.
     * <li>{@code GRANULAR}: corresponds to {@link StageTO#isTooGranular()}.
     * <li>{@code GROUPING}: corresponds to {@link StageTO#isGroupingStage()}.
     * </ul>
     * @see org.bgee.model.dao.api.DAO#setAttributes(Collection)
     * @see org.bgee.model.dao.api.DAO#setAttributes(Enum[])
     * @see org.bgee.model.dao.api.DAO#clearAttributes()
     */
    public enum Attribute implements DAO.Attribute {
        ID, NAME, DESCRIPTION, LEFTBOUND, RIGHTBOUND, LEVEL, GRANULAR, GROUPING;
    }

    /**
     * Inserts the provided stages into the Bgee database, represented as 
     * a {@code Collection} of {@code StageTO}s.
     * 
     * @param stages    a {@code Collection} of {@code StageTO}s to be inserted 
     *                  into the database.
     * @throws DAOException     If a {@code SQLException} occurred while trying 
     *                          to insert {@code stages}. The {@code SQLException} 
     *                          will be wrapped into a {@code DAOException} ({@code DAOs} 
     *                          do not expose these kind of implementation details).
     */
    public int insertStages(Collection<StageTO> stages);
    

    /**
     * {@code DAOResultSet} specifics to {@code StageTO}s
     * 
     * @author Frederic Bastian
     * @version Bgee 13
     * @since Bgee 13
     */
    public interface StageTOResultSet extends DAOResultSet<StageTO> {
        
    }
    
    /**
     * {@code EntityTO} representing a developmental stage in the Bgee data source.
     * 
     * @author Frederic Bastian
     * @version Bgee 13
     * @since Bgee 01
     */
    public final class StageTO extends NestedSetModelElementTO {
        private static final long serialVersionUID = -1560561694015229894L;
        /**
         * See {@link #isTooGranular()}.
         */
        private final boolean tooGranular;
        /**
         * See {@link #isGroupingStage()}.
         */
        private final boolean groupingStage;

        /**
         * Constructor providing the ID, the name, the description, 
         * the left bound, the right bound, the level, whether it is a too granular stage 
         * (see {@link #isTooGranular()}), and whether it is a  grouping stage 
         * (see {@link #isGroupingStage()}).
         * <p>
         * All of these parameters are optional except {@code id}, so they can be 
         * {@code null} when not used.
         * 
         * @param id                A {@code String} that is the ID.
         * @param name              A {@code String} that is the name of this stage.
         * @param description       A {@code String} that is the description of this stage.
         * @param leftBound         An {@code int} that is the left bound of this stage 
         *                          in the nested set model representing the developmental 
         *                          stage ontology.
         * @param rightBound        An {@code int} that is the right bound of this stage 
         *                          in the nested set model representing the developmental 
         *                          stage ontology.
         * @param level             An {@code int} that is the level of this stage 
         *                          in the nested set model representing the developmental 
         *                          stage ontology.
         * @param tooGranular       A {@code boolean} defining whether this stage 
         *                          is too granular (see {@link #isTooGranular()}).
         * @param groupingStage     A {@code boolean} defining whether this stage 
         *                          is a grouping stage (see {@link #isGroupingStage()}).
         * @throws IllegalArgumentException If {@code id} is {@code null} or empty, or if any of 
         *                                  {code leftBound} or {code rightBound} or {code level} 
         *                                  is less than 0.
         */
        public StageTO(String id, String name, String description,
                int leftBound, int rightBound, int level, boolean tooGranular, 
                boolean groupingStage) throws IllegalArgumentException {
            
            super(id, name, description, leftBound, rightBound, level);
            this.tooGranular = tooGranular;
            this.groupingStage = groupingStage;
        }


        /**
         * @return  the {@code boolean} defining whether this stage is a very granular 
         *          developmental stage. Such stages are usually not used in expression 
         *          summaries, and are replaced by their closest parent not too granular.
         */
        public boolean isTooGranular() {
            return tooGranular;
        }
        /**
         * @return  the {@code boolean} defining whether this stage is a grouping stage, 
         *          broad enough to allow comparisons of anatomical features. For instance, 
         *          to compare expression in brain at stages such as "child", "early adulthood", 
         *          "late adulthood", rather than at stages such as "23 yo", "24yo", "25yo", ...
         */
        public boolean isGroupingStage() {
            return groupingStage;
        }
        

        @Override
        public String toString() {
            return "ID: " + this.getId() + " - Name: " + this.getName() + 
                    " - Left bound: " + this.getLeftBound() + " - Right bound: " + 
                    this.getRightBound() + " - Level: " + this.getLevel() + 
                    " - Is too granular: " + this.isTooGranular() + 
                    " - Is a grouping stage: " + this.isGroupingStage();
        }
    }
}
