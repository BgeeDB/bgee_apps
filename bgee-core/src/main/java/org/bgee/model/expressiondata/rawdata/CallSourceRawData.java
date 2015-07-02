package org.bgee.model.expressiondata.rawdata;

import org.bgee.model.gene.Gene;

/**
 * Parent class of all classes holding expression data result of a gene 
 * (see 'see also' section).
 * This class holds methods and attributes related to this {@code Gene}. 
 * It also stores the {@code expressionId} and {@code expressionConfidence} 
 * of this expression result. 
 * 
 * @author Frederic Bastian
 * @version Bgee 11
 * @see model.expressionData.estData.ExpressedSequenceTag
 * @see model.expressionData.inSituHybridizationData.InSituSpot
 * @see model.expressionData.affymetrixData.AffymetrixProbeset
 * @see model.expressionData.rnaSeqData.RnaSeqResult
 * @since Bgee 09
 */
@Deprecated
public class CallSourceRawData extends RawData
{

	
	private String geneId;
	private Gene gene;
	
	private String expressionId;
	private String expressionConfidence;
	
	/**
	 * Default constructor. 
	 */
	public CallSourceRawData()
    {
    	super("");

		this.setGeneId("");
		this.setGene(null);
		
		this.setExpressionId("");
		this.setExpressionConfidence("");
    }
	
	
//	 ***************************************************
//	  GETTERS AND SETTERS
//	 ***************************************************
	public void setExpressionId(String expId)
	{
		this.expressionId = expId;
	}
	public String getExpressionId()
	{
		return this.expressionId;
	}
	
	public void setExpressionConfidence(String expConfidence)
	{
		this.expressionConfidence = expConfidence;
	}
	public String getExpressionConfidence()
	{
		if (this.expressionConfidence.equals("poor quality")) {
			return "low quality";
		}
		return this.expressionConfidence;
	}
	
	public void setGene(Gene gene)
	{
		this.gene = gene;
	}
	public Gene getGene()
	{
		return this.gene;
	}
    //---------------------------------
	public void setGeneId(String geneId)
	{
		this.geneId = geneId;
	}
	
	/**
	 * Returns either the value of {@code geneId}, 
	 * or the of the {@code id} of the {@code Gene} 
	 * stored in {@code gene}, depending on which one is set. 
	 * 
	 * @return 	the ID of the gene for which this object reports expression. 
	 * @see 	#geneId
	 * @see 	#gene
	 * @see 	#getIdByEntityOrId(Entity, String)
	 */
	public String getGeneId()
	{
//		return this.getIdByEntityOrId(this.getGene(), this.geneId);
	    //TODO
	    return null;
	}
}
