package org.bgee.model.source;

import java.util.Date;

import model.EntityDescription;

/**
 * Objects from this class represent data sources used in Bgee, 
 * such as ArrayExpress, Ensembl, ZFIN, ...
 * <p>
 * These objects can be used either to display information on data sources, 
 * e.g., date of the release used in Bgee, description, ...
 * Or to generate URLs to link to the data sources: generating a XRef link for a gene, 
 * or link to retrieve an Affymetrix experiment in ArrayExpress, an in situ image in ZFIN, etc.
 * <p>
 * Those URLs include "tags" such as "[gene_id]" in it, 
 * in order to replace those tags with the appropriate values 
 * (in that case, the gene ID for which we want to generate a link).
 * <p>
 * This class is manly used by the <code>view</code> for display purpose.
 * 
 * @author Frederic Bastian
 * @version Bgee 11, June 2012
 * @see model.gene.GeneXRef
 * @see model.gene.Gene
 * @see model.expressionData
 * @see view.html.HtmlGene#displayCrossReferences(Gene)
 * @see view.html.HtmlExpression
 * @since Bgee 11
 *
 */
public class Source extends EntityDescription
{
	/**
	 * a <code>String</code> representing the tag used in URLs 
	 * to be replaced by the <code>id</code> field of a <code>GeneXRef</code>.
	 * 
	 * @see #xRefUrl
	 */
	public static final String XREFIDTAG = "[xref_id]";
	/**
	 * a <code>String</code> representing the tag used in URLs 
	 * to be replaced by the <code>id</code> field of a <code>Gene</code>.
	 * 
	 * @see #xRefUrl
	 */
	public static final String GENEIDTAG = "[gene_id]";
	/**
	 * a <code>String</code> representing the tag used in URLs 
	 * to be replaced by the value returned by the <code>getSpeciesEnsembLink()</code> method 
	 * of a <code>Species</code> object (e.g., Drosophila_melanogaster, Homo_sapiens, ...).
	 * 
	 * @see #xRefUrl
	 * @see model.anatomy.Species#getSpeciesEnsembLink()
	 */
	public static final String SPECIESENSEMBLLINKTAG = "[species_ensembl_link]";
	/**
	 * a <code>String</code> representing the tag used in URLs 
	 * to be replaced by the <code>id</code> field of an experiment 
	 * (<code>AffymetrixExp</code>, <code>ESTLibrary</code>, 
	 * <code>InSituExp</code>, <code>RNASeqExp</code>).
	 * 
	 * @see #experimentUrl
	 * @see model.expressionData.affymetrixData.MicroarrayExperiment
	 * @see model.expressionData.estData.EstLibrary
	 * @see model.expressionData.inSituHybridizationData.InSituExperiment
	 * @see model.expressionData.rnaSeqData.RnaSeqExperiment
	 */
	public static final String EXPERIMENTIDTAG = "[experiment_id]";
	/**
	 * a <code>String</code> representing the tag used in URLs 
	 * to be replaced by the <code>id</code> field of a evidence 
	 * (<code>RNASeqLibrary, </code><code>AffymetrixChip</code>, <code>InSituEvidence</code>, 
	 * <code>EST</code>).
	 * 
	 * @see #evidenceUrl
	 * @see model.expressionData.affymetrixData.AffymetrixChip
	 * @see model.expressionData.estData.ExpressedSequenceTag
	 * @see model.expressionData.inSituHybridizationData.InSituEvidence
	 * @see model.expressionData.rnaSeqData.RnaSeqLibrary
	 */
	public static final String EVIDENCEIDTAG = "[evidence_id]";
	
	/**
	 * a <code>String</code> used to generate a XRef link for a gene 
	 * to this <code>DataSource</code>, 
	 * that includes "tags" to be replaced with appropriate parameters. 
	 * Allowed tags in this URL: 
	 * <ul>
	 * <li><code>XREFIDTAG</code>
	 * <li><code>GENEIDTAG</code>
	 * <li><code>SPECIESENSEMBLLINKTAG</code>
	 * </ul>
	 * 
	 * @see #XREFIDTAG
	 * @see #GENEIDTAG
	 * @see #SPECIESENSEMBLLINKTAG
	 */
    private String xRefUrl;
    /**
	 * a <code>String</code> used to generate a link for an experiment (EST, Affymetrix, in situ, or RNA-Seq) 
	 * to this <code>DataSource</code>, 
	 * that includes "tags" to be replaced with appropriate parameters. 
	 * Allowed tags in this URL: 
	 * <ul>
	 * <li><code>EXPERIMENTIDTAG</code>
	 * </ul>
	 * 
	 * @see #EXPERIMENTIDTAG
	 */
    private String experimentUrl;
    /**
	 * a <code>String</code> used to generate a link for an evidence (EST, Affymetrix, in situ, or RNA-Seq) 
	 * to this <code>DataSource</code>, 
	 * that includes "tags" to be replaced with appropriate parameters. 
	 * Allowed tags in this URL: 
	 * <ul>
	 * <li><code>EVIDENCEIDTAG</code>
	 * </ul>
	 * 
	 * @see #EVIDENCEIDTAG
	 */
    private String evidenceUrl;
    /**
     * a <code>String</code> representing the URL to the "home page" 
     * of the resource, to provide general link to it.
     */
    private String baseUrl;
    
    /**
     * a <code>Date</code> representing the date of the release used in Bgee 
     * for this <code>DataSource</code>.
     */
    private Date releaseDate;  
    /**
     * a <code>String</code> giving details on the release used in Bgee 
     * for this <code>DataSource</code> (e.g., 'Ensembl release 67', 'CVS version x.xx').
     */
    private String releaseVersion;
    
    /**
     * a <code>boolean</code> defining whether this <code>DataSource</code> should be displayed 
     * when listing data used in Bgee (some are used only for xref).
     */
    private boolean toDisplay;
    /**
     * a <code>String</code> representing the category of this <code>DataSource</code> 
     * (e.g., 'Ontology', 'Affymetrix data').
     */
    private String category;
    
    
    /**
     * Default constructor.
     */
    public DataSource()
    {
    	super();
    	this.setXRefUrl(null);
    	this.setExperimentUrl(null);
    	this.setEvidenceUrl(null);
    	this.setBaseUrl(null);
    	this.setReleaseDate(null);
    	this.setReleaseVersion(null);
    	this.setToDisplay(false);
    	this.setCategory(null);
    }
    
    /**
     * Take the ID in Bgee of an experiment, coming from this <code>DataSource</code>, 
     * to be filtered to be prepared for use in XRef URLs. For instance, sometimes a prefix is added by Bgee 
     * to the ID used in the source database. This method would remove it.
     * 
     * @param experimentId
     * @return
     */
    public String filterExperimentIdForXRef(String experimentId)
    {
    	if (this.getName().equalsIgnoreCase("mgi")) {
    		return experimentId.replaceFirst("MGI_", "");
    	} else if (this.getName().equalsIgnoreCase("bdgp")) {
    		return experimentId.replaceFirst("BDGP_", "");
    	}
    	return experimentId;
    }
    
    /**
     * Take the ID in Bgee of an evidence, coming from this <code>DataSource</code>, 
     * to be filtered to be prepared for use in XRef URLs. For instance, sometimes a prefix is added by Bgee 
     * to the ID used in the source database. This method would remove it.
     * 
     * @param evidenceId
     * @return
     */
    public String filterEvidenceIdForXRef(String evidenceId)
    {
    	if (this.getName().equalsIgnoreCase("mgi")) {
    		return evidenceId.replaceFirst("MGI_", "");
    	} else if (this.getName().equalsIgnoreCase("bdgp")) {
    		return evidenceId.replaceFirst("BDGP_", "");
    	}
    	return evidenceId;
    }
    
//********************************************
//    GETTERS AND SETTERS
//********************************************
	/**
	 * @return 	the xRefUrl
	 * @see 	#xRefUrl
	 */
	public String getXRefUrl() {
		return this.xRefUrl;
	}
	/**
	 * @param 	xRefUrl the xRefUrl to set
	 * @see 	#xRefUrl
	 */
	public void setXRefUrl(String xRefUrl) {
		this.xRefUrl = xRefUrl;
	}
	/**
	 * @return 	the experimentUrl
	 * @see 	#experimentUrl
	 */
	public String getExperimentUrl() {
		return this.experimentUrl;
	}
	/**
	 * @param 	experimentUrl the experimentUrl to set
	 * @see 	#experimentUrl
	 */
	public void setExperimentUrl(String experimentUrl) {
		this.experimentUrl = experimentUrl;
	}
	/**
	 * @return 	the evidenceUrl
	 * @see 	#evidenceUrl
	 */
	public String getEvidenceUrl() {
		return this.evidenceUrl;
	}
	/**
	 * @param 	evidenceUrl the evidenceUrl to set
	 * @see 	#evidenceUrl
	 */
	public void setEvidenceUrl(String evidenceUrl) {
		this.evidenceUrl = evidenceUrl;
	}
	/**
	 * @return 	the baseUrl
	 * @see 	#baseUrl
	 */
	public String getBaseUrl() {
		return this.baseUrl;
	}
	/**
	 * @param 	baseUrl the baseUrl to set
	 * @see 	#baseUrl
	 */
	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}
	/**
	 * @return 	the releaseDate
	 * @see 	#releaseDate
	 */
	public Date getReleaseDate() {
		return this.releaseDate;
	}
	/**
	 * @param 	releaseDate the releaseDate to set
	 * @see 	#releaseDate
	 */
	public void setReleaseDate(Date releaseDate) {
		this.releaseDate = releaseDate;
	}
	/**
	 * @return 	the releaseVersion
	 * @see 	#releaseVersion
	 */
	public String getReleaseVersion() {
		return this.releaseVersion;
	}
	/**
	 * @param 	releaseVersion the releaseVersion to set
	 * @see 	#releaseVersion
	 */
	public void setReleaseVersion(String releaseVersion) {
		this.releaseVersion = releaseVersion;
	}
	/**
	 * @return 	the toDisplay
	 * @see 	#toDisplay
	 */
	public boolean isToDisplay() {
		return this.toDisplay;
	}
	/**
	 * @param 	toDisplay the toDisplay to set
	 * @see 	#toDisplay
	 */
	public void setToDisplay(boolean toDisplay) {
		this.toDisplay = toDisplay;
	}
	/**
	 * @return 	the category
	 * @see 	#category
	 */
	public String getCategory() {
		return this.category;
	}
	/**
	 * @param 	category the category to set
	 * @see 	#category
	 */
	public void setCategory(String category) {
		this.category = category;
	}
}
