package org.bgee.model.dao.api.expressiondata;

import org.bgee.model.dao.api.TransferObject;

/**
 * A {@code TransferObject} carrying information about calls present in the Bgee database, 
 * common to all types of calls (expression calls, no-expression calls, differential 
 * expression calls). A call is defined by a triplet 
 * gene/anatomical entity/developmental stage, with associated information about 
 * the data types that allowed to produce it, and with which confidence.
 * <p>
 * For simplicity, a {@code CallTO} can carry the {@link DataState}s associated to 
 * any data type, but the getters to retrieve this information are {@code protected}: 
 * it is the responsibility of subclasses to make public only the getters associated to 
 * the data types allowing to produce their type of call (expression, no-expression, 
 * differential expression; some data types do not allow to produce some types of call).
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public abstract class CallTO extends TransferObject {
    /**
     * An {@code enum} used to define, for each data type that allowed to generate 
     * a call (Affymetrix, RNA-Seq, ...), its contribution to the generation 
     * of the call.
     * <ul>
     * <li>{@code NODATA}: no data from the associated data type allowed to produce 
     * the call.
     * <li>{@code LOWQUALITY}: some data from the associated data type allowed 
     * to produce the call, but with a low quality.
     * <li>{@code HIGHQUALITY}: some data from the associated data type allowed 
     * to produce the call, and with a high quality.
     * </ul>
     * 
     * @author Frederic Bastian
     * @version Bgee 13
     * @since Bgee 13
     */
    public enum DataState {
        NODATA, LOWQUALITY, HIGHQUALITY;
    }
    
    //**************************************
    // ATTRIBUTES
    //**************************************
    //-----------core attributes: gene/anat entity/dev stage---------------
    /**
     * A {@code String} representing the ID of the gene associated to this call.
     */
    private String geneId;
    /**
     * A {@code String} representing the ID of the developmental stage associated to 
     * this call.
     */
    private String devStageId;
    /**
     * A {@code String} representing the ID of the anatomical entity associated to 
     * this call.
     */
    private String anatEntityId;
    
    //-----------DataState for each data type---------------
    /**
     * The {@code DataState} defining the contribution of Affymetrix data 
     * to the generation of this call.
     */
    private DataState affymetrixData;
    /**
     * The {@code DataState} defining the contribution of EST data 
     * to the generation of this call.
     */
    private DataState estData;
    /**
     * The {@code DataState} defining the contribution of <em>in situ</em> data 
     * to the generation of this call.
     */
    private DataState inSituData;
    /**
     * The {@code DataState} defining the contribution of RNA-Seq data 
     * to the generation of this call.
     */
    private DataState rnaSeqData;


    //**************************************
    // GETTERS/SETTERS
    //**************************************
    //-----------core attributes: gene/anat entity/dev stage---------------
    /**
     * @return the {@code String} representing the ID of the gene associated to this call.
     */
    public String getGeneId() {
        return geneId;
    }
    /**
     * @param geneId    the {@code String} representing the ID of the gene associated to 
     *                  this call.
     */
    void setGeneId(String geneId) {
        this.geneId = geneId;
    }
    /**
     * @return  the {@code String} representing the ID of the developmental stage 
     *          associated to this call.
     */
    public String getDevStageId() {
        return devStageId;
    }
    /**
     * @param devStageId    the {@code String} representing the ID of the 
     *                      developmental stage associated to this call.
     */
    void setDevStageId(String devStageId) {
        this.devStageId = devStageId;
    }
    /**
     * @return  the {@code String} representing the ID of the anatomical entity 
     *          associated to this call.
     */
    public String getAnatEntityId() {
        return anatEntityId;
    }
    /**
     * @param anatEntityId  the {@code String} representing the ID of the 
     *                      anatomical entity associated to this call.
     */
    void setAnatEntityId(String anatEntityId) {
        this.anatEntityId = anatEntityId;
    }
    
    //-----------DataState for each data type---------------
    /**
     * @return  the {@code DataState} defining the contribution of Affymetrix data 
     *          to the generation of this call.
     */
    protected DataState getAffymetrixData() {
        return affymetrixData;
    }
    /**
     * @param affymetrixData    the {@code DataState} defining the contribution 
     *                          of Affymetrix data to the generation of this call.
     */
    void setAffymetrixData(DataState affymetrixData) {
        this.affymetrixData = affymetrixData;
    }
    /**
     * @return  the {@code DataState} defining the contribution of EST data 
     *          to the generation of this call.
     */
    protected DataState getESTData() {
        return estData;
    }
    /**
     * @param estData   the {@code DataState} defining the contribution 
     *                  of EST data to the generation of this call.
     */
    void setESTData(DataState estData) {
        this.estData = estData;
    }
    /**
     * @return  the {@code DataState} defining the contribution of <em>in situ</em> data 
     *          to the generation of this call.
     */
    protected DataState getInSituData() {
        return inSituData;
    }
    /**
     * @param inSituData    the {@code DataState} defining the contribution 
     *                      of <em>in situ</em> data to the generation of this call.
     */
    void setInSituData(DataState inSituData) {
        this.inSituData = inSituData;
    }
    /**
     * @return  the {@code DataState} defining the contribution of RNA-Seq data 
     *          to the generation of this call.
     */
    protected DataState getRnaSeqData() {
        return rnaSeqData;
    }
    /**
     * @param rnaSeqData    the {@code DataState} defining the contribution 
     *                      of RNA-Seq data to the generation of this call.
     */
    void setRnaSeqData(DataState rnaSeqData) {
        this.rnaSeqData = rnaSeqData;
    }
}
