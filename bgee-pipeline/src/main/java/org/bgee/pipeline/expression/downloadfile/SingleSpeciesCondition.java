package org.bgee.pipeline.expression.downloadfile;

import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO;

/**
 * Class used by classes that generate single species expression TSV download files 
 * to store conditions. 
 *
 * @author  Valentine Rech de Laval
 * @version Bgee 13
 * @since   Bgee 13
 */
//FIXME: to reactivate?
public class SingleSpeciesCondition {
//
//    /**
//     * A {@code String} representing the ID of the anatomical entity.
//     */
//    private final String anatEntityId;
//    
//    /**
//     * A {@code String} representing the ID of the stage.
//     */
//    private final String stageId;
//    
//    /**
//     * Constructor providing the ID of the anatomical entity (see {@link #getAnatEntityId()}) 
//     * and the ID of the stage (see {@link #getStageId()}). 
//     *
//     * @param anatEntityId  A {@code String} representing the ID of the anatomical entity.
//     * @param stageId       A {@code String} representing the ID of the stage.
//     */
//    public SingleSpeciesCondition(String anatEntityId, String stageId) {
//        this.anatEntityId = anatEntityId;
//        this.stageId = stageId;
//    }
//    /**
//     * Constructor allowing to retrieve the condition information from a {@code CallTO}. 
//     *
//     * @param call  A {@code CallTO} to retrieve condition information from.
//     */
//    public SingleSpeciesCondition(CallTO call) {
//        this.anatEntityId = call.getAnatEntityId();
//        this.stageId = call.getStageId();
//    }
//    
//    /**
//     * @return  the {@code String} representing the ID of the anatomical entity.
//     */
//    public String getAnatEntityId() {
//        return anatEntityId;
//    }
//
//    /**
//     * @return  the {@code String} representing the ID of the stage.
//     */
//    public String getStageId() {
//        return stageId;
//    }
//
//    @Override
//    public int hashCode() {
//        final int prime = 31;
//        int result = 1;
//        result = prime * result + ((stageId == null) ? 0 : stageId.hashCode());
//        result = prime * result + ((anatEntityId == null) ? 0 : anatEntityId.hashCode());
//        return result;
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//        if (this == obj)
//            return true;
//        if (obj == null)
//            return false;
//        if (getClass() != obj.getClass())
//            return false;
//        SingleSpeciesCondition other = (SingleSpeciesCondition) obj;
//        if (stageId == null) {
//            if (other.stageId != null)
//                return false;
//        } else if (!stageId.equals(other.stageId))
//            return false;
//        if (anatEntityId == null) {
//            if (other.anatEntityId != null)
//                return false;
//        } else if (!anatEntityId.equals(other.anatEntityId))
//            return false;
//        return true;
//    }
//
//    @Override
//    public String toString() {
//        return "Anatomical entity ID: " + getAnatEntityId() + " - Stage ID:" + getStageId();
//    }
}
