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
 * any data type, despite the fact that specific subclasses might not be associated to 
 * all of them (some data types do not allow to produce some types of call). But 
 * in that case, the {@code DataState} of such non-available data types will simply 
 * be {@code NODATA}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public abstract class CallTO implements TransferObject {
	private static final long serialVersionUID = 2157139618099008406L;
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
     * The {@code DataState} defining the contribution of "relaxed" <em>in situ</em> 
     * data to the generation of this call. "Relaxed" <em>in situ</em> data are used 
     * to infer absence of expression, by considering <em>in situ</em> data as complete: 
     * absence of expression of a gene is assumed in any organ existing at 
     * the developmental stage studied by some <em>in situ</em> data, with no report 
     * of expression.
     */
    private DataState relaxedInSituData;
    /**
     * The {@code DataState} defining the contribution of RNA-Seq data 
     * to the generation of this call.
     */
    private DataState rnaSeqData;


    /**
     * Default constructor.
     */
    public CallTO() {
        super();
        this.setGeneId(null);
        this.setAnatEntityId(null);
        this.setDevStageId(null);
        this.setAffymetrixData(DataState.NODATA);
        this.setESTData(DataState.NODATA);
        this.setInSituData(DataState.NODATA);
        this.setRelaxedInSituData(DataState.NODATA);
        this.setRNASeqData(DataState.NODATA);
    }
    
    /**
     * Constructor providing the gene ID, the anatomical entity ID, the developmental stage ID,  
     * the contribution of Affymetrix, EST, <em>in situ</em>, "relaxed" <em>in situ</em> and, 
     * RNA-Seq data to the generation of this call.
     * 
     * @param geneId               A {@code String} that is the ID of the gene associated to 
     *                             this call.
     * @param anatEntityId         A {@code String} that is the ID of the anatomical entity
     *                             associated to this call. 
     * @param devStageId           A {@code String} that is the ID of the developmental stage 
     *                             associated to this call. 
     * @param affymetrixData       A {@code DataSate} that is the contribution of Affymetrix data 
     *                             to the generation of this call.
     * @param estData              A {@code DataSate} that is the contribution of EST data
     *                             to the generation of this call.
     * @param inSituData           A {@code DataSate} that is the contribution of <em>in situ</em>
     *                             data to the generation of this call.
     * @param relaxedInSituData    A {@code DataSate} that is the contribution of "relaxed" 
     *                             <em>in situ</em> data to the generation of this call.
     * @param rnaSeqData           A {@code DataSate} that is the contribution of RNA-Seq data
     *                             to the generation of this call.
     */
    public CallTO(String geneId, String anatEntityId, String devStageId, DataState affymetrixData,
            DataState estData, DataState inSituData, DataState relaxedInSituData, 
            DataState rnaSeqData) {
        super();
        this.setGeneId(geneId);
        this.setAnatEntityId(anatEntityId);
        this.setDevStageId(devStageId);
        this.setAffymetrixData(affymetrixData);
        this.setESTData(estData);
        this.setInSituData(inSituData);
        this.setRelaxedInSituData(relaxedInSituData);
        this.setRNASeqData(rnaSeqData);
    }

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
    public DataState getAffymetrixData() {
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
    public DataState getESTData() {
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
    public DataState getInSituData() {
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
     * "Relaxed" <em>in situ</em> data are used to infer absence of expression, 
     * by considering <em>in situ</em> data as complete: absence of expression 
     * of a gene is assumed in any organ existing at the developmental stage 
     * studied by some <em>in situ</em> data, with no report of expression.
     * 
     * @return  the {@code DataState} defining the contribution of relaxed 
     *          <em>in situ</em> data to the generation of this call.
     */
    public DataState getRelaxedInSituData() {
        return relaxedInSituData;
    }
    /**
     * "Relaxed" <em>in situ</em> data are used to infer absence of expression, 
     * by considering <em>in situ</em> data as complete: absence of expression 
     * of a gene is assumed in any organ existing at the developmental stage 
     * studied by some <em>in situ</em> data, with no report of expression.
     * 
     * @param inSituData    the {@code DataState} defining the contribution 
     *                      of relaxed <em>in situ</em> data to the generation 
     *                      of this call.
     */
    void setRelaxedInSituData(DataState inSituData) {
        this.relaxedInSituData = inSituData;
    }
    /**
     * @return  the {@code DataState} defining the contribution of RNA-Seq data 
     *          to the generation of this call.
     */
    public DataState getRNASeqData() {
        return rnaSeqData;
    }
    /**
     * @param rnaSeqData    the {@code DataState} defining the contribution 
     *                      of RNA-Seq data to the generation of this call.
     */
    void setRNASeqData(DataState rnaSeqData) {
        this.rnaSeqData = rnaSeqData;
    }
}
