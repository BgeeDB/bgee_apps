package org.bgee.model.dao.api.anatdev.mapping;

import java.util.Set;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.exception.DAOException;

/**
 * A {@code DAO} allowing to retrieve mappings between stages for multi-species comparisons. 
 * This {@code DAO} is created to mirror 
 * {@link org.bgee.model.dao.api.anatdev.mapping.SummarySimilarityAnnotationDAO 
 * SummarySimilarityAnnotationDAO}, which allows to retrieve similarity relations 
 * between anatomical entities. 
 * <p>
 * As of Bgee 13, there is no explicit annotations mapping stages between species. 
 * We simply use high level developmental stages in Uberon (so, they could have been 
 * retrieved using {@link org.bgee.model.dao.api.anatdev.StageDAO StageDAO}). 
 * But this {@code DAO} allows a behavior comparable to the retrieval of anatomy mappings, 
 * and lets open the possibility to have formal stage mappings in the future. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Mar. 2015
 * @since Bgee 13
 */
//How to deal with Attributes here?
public interface StageGroupingDAO extends DAO {
    
    /**
     * Retrieve stage groups and the stages they are associated to, for comparisons 
     * between species. The groups will represent associations valid at the level 
     * of {@code ancestralTaxonId}, or any of its ancestral taxa. The stages retrieved 
     * will be defined as existing in all the provided species. If {@code speciesIds} 
     * is {@code null} or empty, then the stages retrieved will be defined as existing 
     * in any species.
     * <p>
     * This method mirrors the method to retrieve mappings between anatomical entities, 
     * see {@link SummarySimilarityAnnotationDAO#getSimAnnotToAnatEntity(String, Set)}. 
     * For anatomical entities, there can be relations between several of them, 
     * this is why it is needed to retrieve "groups". So we do the same for stages 
     * (as of Bgee 13, there is no mapping between stages, but we cannot rule out 
     * that it could be the case in the future). Potentially, stages with a mapping 
     * will have the same {@code groupId} (see {@link GroupToStageTO#getGroupId()}). 
     * As of Bgee 13, groups have only one stage.
     * <p>
     * The point of not inferring the ancestral taxon ID from the list of species 
     * is to be able to retrieve mappings valid between some species, but that are defined 
     * at a higher taxonomic level than their LCA (for instance, using only similarities 
     * arisen at the Bilateria level, while comparing species with an Euarchontoglires 
     * common ancestor).
     * <p>
     * Note that using the {@code setAttributes} methods (see {@link DAO}) has no effect 
     * on attributes retrieved in {@code GroupToStageTO}s.
     * 
     * @param ancestralTaxonId  An {@code Integer} that is the NCBI ID of the taxon 
     *                          for which the stages could be used for comparisons.
     * @param speciesIds        A {@code Set} of {@code Integer}s that are the IDs 
     *                          of the species for which the stages retrieved 
     *                          should be valid.
     * @return                  A {@code GroupToStageTOResultSet} allowing 
     *                          to retrieve the requested {@code GroupToStageTO}s.
     * @throws DAOException     If an error occurred when accessing the data source. 
     */
    public GroupToStageTOResultSet getGroupToStage(Integer ancestralTaxonId, 
            Set<Integer> speciesIds) throws DAOException;
    
    /**
     * {@code DAOResultSet} specifics to {@code GroupToStageTO}s.
     * 
     * @author Frederic Bastian
     * @version Bgee 13 Mar. 2015
     * @since Bgee 13
     */
    public interface GroupToStageTOResultSet 
                    extends DAOResultSet<GroupToStageTO> {
    }
    
    /**
     * A {@code TransferObject} representing relation between a stage group 
     * and a stage. 
     * <p>
     * This class defines a stage group ID (see {@link #getGroupId()} and 
     * a stage ID (see {@link #getStageId()}).
     * <p>
     * Note that this class is one of the few {@code TransferObject}s that are not 
     * an {@link org.bgee.model.dao.api.EntityTO}.
     * 
     * @author Frederic Bastian
     * @version Bgee 13 Mar. 2015
     * @since Bgee 13
     */
    public final class GroupToStageTO extends TransferObject {
        private static final long serialVersionUID = -5421638903472459273L;
        
        /**
         * A {@code String} representing the ID of the stage group.
         */
        private final String groupId;

        /**
         * A {@code String} representing the ID of the stage.         
         */
        private final String stageId;

        /**
         * Constructor providing the ID of the group (see {@link #getGroupId()}) 
         * and the ID of the stage (see {@link #getStageId()}).
         * 
         * @param groupId   A {@code String} that is the ID of the group.
         * @param stageId   A {@code String} that is the ID of the stage.
         */
        public GroupToStageTO(String groupId, String stageId) {
            super();
            this.groupId = groupId;
            this.stageId = stageId;
        }
        
        /**
         * @return  the {@code String} representing the ID of the group.
         */
        public String getGroupId() {
            return groupId;
        }
        /**
         * @return  the {@code String} representing the ID of the stage.
         */
        public String getStageId() {
            return stageId;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "GroupToStageTO [groupId=" + groupId + ", stageId="
                    + stageId + "]";
        }
    }
}
