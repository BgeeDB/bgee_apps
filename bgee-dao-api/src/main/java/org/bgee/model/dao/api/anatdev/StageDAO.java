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
        ID, NAME, DESCRIPTION, LEFT_BOUND, RIGHT_BOUND, LEVEL, GRANULAR, GROUPING;
    }

    /**
     * Retrieves stages from data source existing in any of the requested species.
     * <p>
     * The stages are retrieved and returned as a {@code StageTOResultSet}. It is the
     * responsibility of the caller to close this {@code DAOResultSet} once results are retrieved.
     * 
     * @param speciesIds    A {@code Collection} of {@code Integer}s that are the IDs of species 
     *                      allowing to filter the stages to use.
     * @return              An {@code StageTOResultSet} containing all stages from data source.
     * @throws DAOException If an error occurred when accessing the data source. 
     */
    public StageTOResultSet getStagesBySpeciesIds(Collection<Integer> speciesIds) throws DAOException;
    
    /**
     * Retrieves stages from data source existing in any of the requested species, 
     * potentially filtered based on whether stages are grouping stages, and on the dev. stage levels.
     * to use. 
     * <p>
     * The stages are retrieved and returned as a {@code StageTOResultSet}. It is the
     * responsibility of the caller to close this {@code DAOResultSet} once results are retrieved.
     * 
     * @param speciesIds        A {@code Collection} of {@code Integer}s that are the IDs of species 
     *                          allowing to filter the stages to use.
     * @param isGroupingStage   A {@code Boolean} defining whether this stage is a grouping stage, 
     *                          broad enough to allow comparisons of anatomical features. 
     *                          If {@code null}, no filter is apply.
     * @param level             An {@code Integer} that is the level of dev. stages 
     *                          allowing to filter the dev. stages.
     * @return                  The {@code StageTOResultSet} containing stages from data source.
     * @throws DAOException     If an error occurred when accessing the data source. 
     */
    public StageTOResultSet getStagesBySpeciesIds(Collection<Integer> speciesIds, Boolean isGroupingStage,
            Integer level) throws DAOException;

    /**
     * Retrieves stages from data source according to a {@code Set} of {@code String}s
     * that are the IDs of dev. stages allowing to filter the entities to use.
     * <p>
     * The stages are retrieved and returned as a {@code StageTOResultSet}. It is the
     * responsibility of the caller to close this {@code DAOResultSet} once results are retrieved.
     * 
     * @param stagesIds     A {@code Collection} of {@code String}s that are the IDs of dev. stages 
     *                      allowing to filter the stages to use.
     * @return              An {@code StageTOResultSet} containing stages from data source.
     * @throws DAOException If an error occurred when accessing the data source. 
     */
    public StageTOResultSet getStagesByIds(Collection<String> stagesIds) throws DAOException;

    /**
     * Retrieves stages from data source according to species filtering, filtering on 
     * dev. stages IDs, on whether stages are grouping stages, and on the dev. stage levels. 
     * If an entity in {@code stageIds} does not exists in any of the requested species, 
     * it will not be returned.
     * <p>
     * The stages are retrieved and returned as a {@code StageTOResultSet}. It is the
     * responsibility of the caller to close this {@code DAOResultSet} once results are retrieved.
     * 
     * @param speciesIds        A {@code Collection} of {@code Integer}s that are the IDs of species 
     *                          allowing to filter the stages to use.
     * @param stageIds          A {@code Collection} of {@code String}s that are the IDs of dev. stages
     *                          allowing to filter the stages to use.
     * @param isGroupingStage   A {@code Boolean} defining whether this stage is a grouping stage, 
     *                          broad enough to allow comparisons of anatomical features. 
     *                          If {@code null}, no filter is apply.
     * @param level             An {@code Integer} that is the level of dev. stages 
     *                          allowing to filter the dev. stages.
     * @return                  The {@code StageTOResultSet} containing filtered stages
     *                          from data source.
     * @throws DAOException     If an error occurred when accessing the data source. 
     */
    public StageTOResultSet getStages(Collection<Integer> speciesIds, Collection<String> stageIds,
            Boolean isGroupingStage, Integer level) throws DAOException;

    /**
     * Retrieves stages from data source according to species filtering, filtering on 
     * dev. stages IDs, on whether stages are grouping stages, and on the dev. stage levels. 
     * If an entity in {@code stageIds} does not exists according to the species filtering, 
     * it will not be returned. 
     * <p>
     * The stages are retrieved and returned as a {@code StageTOResultSet}. It is the
     * responsibility of the caller to close this {@code DAOResultSet} once results are retrieved.
     * 
     * @param speciesIds        A {@code Collection} of {@code Integer}s that are the IDs of species 
     *                          allowing to filter the stages to use.
     * @param anySpecies        A {@code Boolean} defining, when {@code speciesIds} contains several IDs, 
     *                          whether the entities retrieved should be valid in any 
     *                          of the requested species (if {@code true}), or in all 
     *                          of the requested species (if {@code false}).
     * @param stageIds          A {@code Collection} of {@code String}s that are the IDs of dev. stages
     *                          allowing to filter the stages to use.
     * @param isGroupingStage   A {@code Boolean} defining whether this stage is a grouping stage, 
     *                          broad enough to allow comparisons of anatomical features. 
     *                          If {@code null}, no filter is apply.
     * @param level             An {@code Integer} that is the level of dev. stages 
     *                          allowing to filter the dev. stages.
     * @param attributes        A {@code Collection} of {@code StageDAO.Attribute}s 
     *                          defining the attributes to populate in the returned 
     *                          {@code StageTO}s. If {@code null} or empty, 
     *                          all attributes are populated. 
     * @return                  The {@code StageTOResultSet} containing filtered stages
     *                          from data source.
     * @throws DAOException     If an error occurred when accessing the data source. 
     */
    public StageTOResultSet getStages(Collection<Integer> speciesIds, Boolean anySpecies, 
            Collection<String> stageIds, Boolean isGroupingStage, Integer level, 
            Collection<StageDAO.Attribute> attributes) throws DAOException;

    /**
     * Inserts the provided stages into the Bgee database, represented as 
     * a {@code Collection} of {@code StageTO}s.
     * 
     * @param stages    a {@code Collection} of {@code StageTO}s to be inserted 
     *                  into the database.
     * @throws IllegalArgumentException If {@code stages} is empty or null. 
     * @throws DAOException             If a {@code SQLException} occurred while trying 
     *                                  to insert {@code stages}. The {@code SQLException} 
     *                                  will be wrapped into a {@code DAOException} ({@code DAOs} 
     *                                  do not expose these kind of implementation details).
     */
    public int insertStages(Collection<StageTO> stages) 
            throws DAOException, IllegalArgumentException;

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
    public final class StageTO extends NestedSetModelElementTO<String> {
        private static final long serialVersionUID = -1560561694015229894L;
        /**
         * See {@link #isTooGranular()}.
         */
        private final Boolean tooGranular;
        /**
         * See {@link #isGroupingStage()}.
         */
        private final Boolean groupingStage;

        /**
         * Constructor providing the ID, the name, the description, 
         * the left bound, the right bound, the level, whether it is a too granular stage 
         * (see {@link #isTooGranular()}), and whether it is a  grouping stage 
         * (see {@link #isGroupingStage()}).
         * <p>
         * All of these parameters are optional, so they can be {@code null} when not used.
         * 
         * @param id                A {@code String} that is the ID.
         * @param name              A {@code String} that is the name of this stage.
         * @param description       A {@code String} that is the description of this stage.
         * @param leftBound         An {@code Integer} that is the left bound of this stage 
         *                          in the nested set model representing the developmental 
         *                          stage ontology.
         * @param rightBound        An {@code Integer} that is the right bound of this stage 
         *                          in the nested set model representing the developmental 
         *                          stage ontology.
         * @param level             An {@code Integer} that is the level of this stage 
         *                          in the nested set model representing the developmental 
         *                          stage ontology.
         * @param tooGranular       A {@code Boolean} defining whether this stage 
         *                          is too granular (see {@link #isTooGranular()}).
         * @param groupingStage     A {@code Boolean} defining whether this stage 
         *                          is a grouping stage (see {@link #isGroupingStage()}).
         * @throws IllegalArgumentException If {@code id} is empty, or if any of 
         *                                  {code leftBound} or {code rightBound} or {code level} 
         *                                  is not {@code null} and less than 0.
         */
        public StageTO(String id, String name, String description, Integer leftBound, 
                Integer rightBound, Integer level, Boolean tooGranular, Boolean groupingStage) 
                        throws IllegalArgumentException {
            super(id, name, description, leftBound, rightBound, level);
            this.tooGranular = tooGranular;
            this.groupingStage = groupingStage;
        }

        /**
         * @return  the {@code Boolean} defining whether this stage is a very granular 
         *          developmental stage. Such stages are usually not used in expression 
         *          summaries, and are replaced by their closest parent not too granular.
         */
        public Boolean isTooGranular() {
            return tooGranular;
        }
        /**
         * @return  the {@code Boolean} defining whether this stage is a grouping stage, 
         *          broad enough to allow comparisons of anatomical features. For instance, 
         *          to compare expression in brain at stages such as "child", "early adulthood", 
         *          "late adulthood", rather than at stages such as "23 yo", "24yo", "25yo", ...
         */
        public Boolean isGroupingStage() {
            return groupingStage;
        }
        

        @Override
        public String toString() {
            return "ID: " + this.getId() + " - Name: " + this.getName() + 
                    " - Desc: " + this.getDescription() + 
                    " - Left bound: " + this.getLeftBound() + " - Right bound: " + 
                    this.getRightBound() + " - Level: " + this.getLevel() + 
                    " - Is too granular: " + this.isTooGranular() + 
                    " - Is a grouping stage: " + this.isGroupingStage();
        }
    }
}
