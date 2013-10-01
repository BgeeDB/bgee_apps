package org.bgee.model.dao.api.expressiondata;

import org.bgee.model.dao.api.expressiondata.CallTO.DataState;

/**
 * This class allows to provide the parameters when using a {@link CallDAO}, 
 * to filter the {@link ExpressionCallTO}s retrieved.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class ExprCallQueryParams extends CallQueryParams {
    /**
     * A {@code ExpressionCallTO} that will hold some parameters of this {
     * @code ExprCallQueryParams}. this is because they have some parameters in common, 
     * so the corresponding methods will be delegated to {@code referenceCallTO}.
     * <p>
     * Only the appropriate methods will be exposed.
     */
    private final ExpressionCallTO referenceCallTO;
    
    public ExprCallQueryParams() {
        super();
        this.referenceCallTO = new ExpressionCallTO();
    }

    
    //***********************************************
    // GETTERS/SETTERS DELEGATED TO referenceCallTO
    //***********************************************
    /**
     * Returns the {@code boolean} defining whether the {@code ExpressionCallTO}s 
     * retrieved should be based on calls generated using data from anatomical 
     * entities, and all of their descendants by <em>is_a</em> and <em>part_of</em> 
     * relations (expression taking into account substructures), or without taking 
     * into account substructures. If {@code true}, all the descendants will be 
     * considered.
     * 
     * @return  A {@code boolean} defining whether expression in substructures 
     *          of an anatomical entity should be considered.
     */
    public boolean isIncludeSubstructures() {
        return referenceCallTO.isIncludeSubstructures();
    }
    /**
     * Sets the {@code boolean} defining whether the {@code ExpressionCallTO}s 
     * retrieved should be based on calls generated using data from anatomical 
     * entities, and all of their descendants by <em>is_a</em> and <em>part_of</em> 
     * relations (expression taking into account substructures), or without taking 
     * into account substructures. If {@code true}, all the descendants will be 
     * considered.
     * 
     * @param include   A {@code boolean} defining whether expression 
     *                  in substructures of an anatomical entity should be considered.
     */
    public void setIncludeSubstructures(boolean include) {
        this.referenceCallTO.setIncludeSubstructures(include);
    }

    /**
     * Returns the {@code boolean} defining whether the {@code ExpressionCallTO}s 
     * retrieved should be based on calls generated using data from developmental 
     * stages, and all of their descendants by <em>is_a</em> and <em>part_of</em> 
     * relations (expression taking into account sub-stages), or without taking 
     * into account sub-stages. If {@code true}, all the sub-stages will be 
     * considered.
     * 
     * @return  A {@code boolean} defining whether expression in sub-stages 
     *          of a developmental stage should be considered.
     */
    public boolean isIncludeSubStages() {
        return referenceCallTO.isIncludeSubStages();
    }
    /**
     * Sets the {@code boolean} defining whether the {@code ExpressionCallTO}s 
     * retrieved should be based on calls generated using data from developmental 
     * stages, and all of their descendants by <em>is_a</em> and <em>part_of</em> 
     * relations (expression taking into account sub-stages), or without taking 
     * into account sub-stages. If {@code true}, all the sub-stages will be 
     * considered.
     * 
     * @param include   A {@code boolean} defining whether expression 
     *                  in sub-stages of a developmental stage should be considered.
     */
    public void setIncludeSubStages(boolean include) {
        this.referenceCallTO.setIncludeSubStages(include);
    }

    /**
     * @return  the {@code DataState} defining the requested minimum contribution 
     *          of Affymetrix data to the generation of the {@code CallTO}s 
     *          to be retrieved.
     */
    public DataState getAffymetrixData() {
        return this.referenceCallTO.getAffymetrixData();
    }
    /**
     * @param minContribution   the {@code DataState} defining the requested minimum 
     *                          contribution of Affymetrix data to the generation 
     *                          of the {@code CallTO}s to be retrieved.
     */
    public void setAffymetrixData(DataState minContribution) {
        this.referenceCallTO.setAffymetrixData(minContribution);
    }

    /**
     * @return  the {@code DataState} defining the requested minimum contribution 
     *          of Affymetrix data to the generation of the {@code CallTO}s 
     *          to be retrieved.
     */
    public DataState getESTData() {
        return this.referenceCallTO.getESTData();
    }
    /**
     * @param minContribution   the {@code DataState} defining the requested minimum 
     *                          contribution of EST data to the generation 
     *                          of the {@code CallTO}s to be retrieved.
     */
    public void setESTData(DataState minContribution) {
        this.referenceCallTO.setESTData(minContribution);
    }

    /**
     * @return  the {@code DataState} defining the requested minimum contribution 
     *          of <em>in situ</em> data to the generation of the {@code CallTO}s 
     *          to be retrieved.
     */
    public DataState getInSituData() {
        return this.referenceCallTO.getInSituData();
    }
    /**
     * @param minContribution   the {@code DataState} defining the requested minimum 
     *                          contribution of <em>in situ</em> data to the generation 
     *                          of the {@code CallTO}s to be retrieved.
     */
    public void setInSituData(DataState minContribution) {
        this.referenceCallTO.setInSituData(minContribution);
    }

    /**
     * @return  the {@code DataState} defining the requested minimum contribution 
     *          of RNA-Seq data to the generation of the {@code CallTO}s 
     *          to be retrieved.
     */
    public DataState getRNASeqData() {
        return this.referenceCallTO.getRNASeqData();
    }
    /**
     * @param minContribution   the {@code DataState} defining the requested minimum 
     *                          contribution of RNA-Seq data to the generation 
     *                          of the {@code CallTO}s to be retrieved.
     */
    public void setRNASeqData(DataState minContribution) {
        this.referenceCallTO.setRNASeqData(minContribution);
    }
}
