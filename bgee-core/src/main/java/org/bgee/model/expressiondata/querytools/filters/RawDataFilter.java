package org.bgee.model.expressiondata.querytools.filters;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.rawdata.affymetrix.AffymetrixChip.AffymetrixDetectionType;
import org.bgee.model.expressiondata.rawdata.affymetrix.AffymetrixChip.AffymetrixNormalizationType;
import org.bgee.model.expressiondata.rawdata.rnaseq.RNASeqLibrary.RNASeqLibraryType;

/**
 * This class allows to perform filtering of query results based on 
 * source raw data, such as RNA-Seq experiment IDs, or Affymetrix chip IDs, 
 * or Affymetrix chip quality scores, or RNA-Seq platform IDs, ... 
 * This <code>Filter</code> can either be used alone, completely disconnected 
 * from the expression data calls generated (for instance to retrieve the 
 * <code>Gene</code>s present on a specific <code>AffymetrixChip</code>, 
 * or the <code>AnatomicalEntity</code>s studied in a specific <code>RNASeqExp</code>); 
 * or it can be used as part of a {@link CompositeCallFilter}, 
 * allowing to re-compute expression data calls using only a subset of 
 * the source raw data in Bgee, filtered thanks to a <code>RawDataFilter</code>. 
 * <p>
 * The parameters of this class are more coupled with the data in the data source 
 * than with the classes of this application.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class RawDataFilter implements Filter {
	/**
	 * <code>Logger</code> of the class. 
	 */
	private final static Logger log = LogManager.getLogger(RawDataFilter.class.getName());
	
	public RawDataFilter() {
		//ESTs
		this.estIds                       = new HashSet<String>();
		this.estLibraryIds                = new HashSet<String>();
		
		//AFFYMETRIX
		this.affymetrixProbesetIds        = new HashSet<String>();
		this.affymetrixChipTypeIds        = new HashSet<String>();
		this.affymetrixCdfNames           = new HashSet<String>();
		this.setAffymetrixMinQualScore(0);
		this.setAffymetrixMinPercentPresent(0);
		this.affymetrixDetectionTypes     = EnumSet.noneOf(AffymetrixDetectionType.class);
		this.affymetrixNormalizationTypes = 
				EnumSet.noneOf(AffymetrixNormalizationType.class);
		this.affymetrixChipIds            = new HashSet<String>();
		this.affymetrixExpIds             = new HashSet<String>();
		
		//IN SITU
		this.inSituSpotIds                = new HashSet<String>();
		this.inSituEvidenceIds            = new HashSet<String>();
		this.inSituExpIds                 = new HashSet<String>();
		
		//RNA-SEQ
		this.rnaSeqRunIds                 = new HashSet<String>();
		this.rnaSeqPlatformIds            = new HashSet<String>();
		this.setRNASeqMinPercentPresent(0);
		this.setRNASeqMinAlignedReadCount(0);
		this.setRNASeqMinReadLength(0);
		this.rnaSeqLibraryTypes           = EnumSet.noneOf(RNASeqLibraryType.class);
		this.rnaSeqLibraryIds             = new HashSet<String>();
		this.rnaSeqExpIds                 = new HashSet<String>();
	}
	//**********************************
	//  EST DATA
	//**********************************
	/**
	 * A <code>Set</code> of <code>String</code>s to filter ESTs based on their IDs.
	 */
	private final Set<String> estIds;
	/**
	 * Get the <code>Set</code> of <code>String</code>s defining the IDs of ESTs to use.
	 * 
	 * @return the IDs of allowed ESTs to used.
	 */
	public Set<String> getESTIds() {
		return estIds;
	}
	/**
	 * Add a <code>String</code> to the <code>Set</code> of <code>String</code>s 
	 * defining the IDs of ESTs to use.
	 * 
	 * @param estId 	the ID of an allowed EST
	 * @see #getESTIds()
	 */
	public void addESTId(String estId) {
		this.estIds.add(estId);
	}
	/**
	 * Add a <code>Collection</code> of <code>String</code>s 
	 * to the <code>Set</code> of <code>String</code>s defining the IDs of ESTs to use.
	 * 
	 * @param estIds 	the IDs of some allowed ESTs
	 * @see #getESTIds()
	 */
	public void addESTIds(Collection<String> estIds) {
		this.estIds.addAll(estIds);
	}
	
	/**
	 * A <code>Set</code> of <code>String</code>s to filter EST libraries 
	 * based on their IDs.
	 */
	private final Set<String> estLibraryIds;
	/**
	 * Get the <code>Set</code> of <code>String</code>s defining the IDs of 
	 * EST libraries to use.
	 * 
	 * @return the IDs of allowed EST libraries to used.
	 */
	public Set<String> getESTLibraryIds() {
		return estLibraryIds;
	}
	/**
	 * Add a <code>String</code> to the <code>Set</code> of <code>String</code>s 
	 * defining the IDs of EST libraries to use.
	 * 
	 * @param estLibraryId 	the ID of an allowed EST library
	 * @see #getESTLibraryIds()
	 */
	public void addESTLibraryId(String estLibraryId) {
		this.estLibraryIds.add(estLibraryId);
	}
	/**
	 * Add a <code>Collection</code> of <code>String</code>s 
	 * to the <code>Set</code> of <code>String</code>s defining the IDs of 
	 * EST libraries to use.
	 * 
	 * @param estLibraryIds 	the IDs of some allowed EST libraries
	 * @see #getESTLibraryIds()
	 */
	public void addESTLibraryIds(Collection<String> estLibraryIds) {
		this.estLibraryIds.addAll(estLibraryIds);
	}
	//**********************************
	//  AFFYMETRIX DATA
	//**********************************
	/**
	 * A <code>Set</code> of <code>String</code>s to filter probesets based on their IDs.
	 */
	private final Set<String> affymetrixProbesetIds;
	/**
	 * Get the <code>Set</code> of <code>String</code>s defining the IDs of 
	 * Affymetrix probesets to use.
	 * 
	 * @return the IDs of allowed Affymetrix probesets to used.
	 */
	public Set<String> getAffymetrixProbesetIds() {
		return this.affymetrixProbesetIds;
	}
	/**
	 * Add a <code>String</code> to the <code>Set</code> of <code>String</code>s 
	 * defining the IDs of Affymetrix probesets to use.
	 * 
	 * @param affymetrixProbesetId 	the ID of an allowed Affymetrix probeset
	 * @see #getAffymetrixProbesetIds()
	 */
	public void addAffymetrixProbesetId(String affymetrixProbesetId) {
		this.affymetrixProbesetIds.add(affymetrixProbesetId);
	}
	/**
	 * Add a <code>Collection</code> of <code>String</code>s 
	 * to the <code>Set</code> of <code>String</code>s defining the IDs of 
	 * Affymetrix probesets to use.
	 * 
	 * @param affymetrixProbesetIds	the IDs of some allowed Affymetrix probesets
	 * @see #getAffymetrixProbesetIds()
	 */
	public void addAffymetrixProbesetIds(Collection<String> affymetrixProbesetIds) {
		this.affymetrixProbesetIds.addAll(affymetrixProbesetIds);
	}
	
	/**
	 * A <code>Set</code> of <code>String</code>s defining the chip type IDs (for instance, 
	 * 'A-AFFY-15') of chips to use. There is no corresponding class in the application.
	 * Each chip type ID corresponds to one CDF name (see {@link #affymetrixCdfNames}).
	 */
	private final Set<String> affymetrixChipTypeIds;
	/**
	 * Get the <code>Set</code> of <code>String</code>s defining the IDs of 
	 * Affymetrix chip types to use (for instance, 'A-AFFY-15').
	 * 
	 * @return the IDs of allowed Affymetrix chip types to used.
	 */
	public Set<String> getAffymetrixChipTypeIds() {
		return this.affymetrixChipTypeIds;
	}
	/**
	 * Add a <code>String</code> to the <code>Set</code> of <code>String</code>s 
	 * defining the IDs of Affymetrix chip types to use.
	 * 
	 * @param chipTypeId 	the ID of an allowed Affymetrix chip type
	 * @see #getAffymetrixChipTypeIds()
	 */
	public void addAffymetrixChipTypeId(String chipTypeId) {
		this.affymetrixChipTypeIds.add(chipTypeId);
	}
	/**
	 * Add a <code>Collection</code> of <code>String</code>s 
	 * to the <code>Set</code> of <code>String</code>s defining the IDs of 
	 * Affymetrix chip types to use.
	 * 
	 * @param chipTypeIds	the IDs of some allowed Affymetrix chip types
	 * @see #getAffymetrixChipTypeIds()
	 */
	public void addAffymetrixChipTypeIds(Collection<String> chipTypeIds) {
		this.affymetrixChipTypeIds.addAll(chipTypeIds);
	}
	
	/**
	 * A <code>Set</code> of <code>String</code>s defining the CDF names (for instance, 
	 * 'HG_U95Av2') of chips to use. There is no corresponding class in the application. 
	 * Each CDF name corresponds to one chip type ID (see {@link #affymetrixChipTypeIds}).
	 */
	private final Set<String> affymetrixCdfNames;
	/**
	 * Get the <code>Set</code> of <code>String</code>s defining the allowed 
	 * CDF names of Affymetrix chips to use (for instance, 'HG_U95Av2').
	 * 
	 * @return the allowed CDF names of Affymetrix chips to used.
	 */
	public Set<String> getAffymetrixCdfNames() {
		return this.affymetrixCdfNames;
	}
	/**
	 * Add a <code>String</code> to the <code>Set</code> of <code>String</code>s 
	 * defining the allowed CDF names of Affymetrix chips to use.
	 * 
	 * @param cdfName 	an allowed CDF name of Affymetrix chips to use
	 * @see #getAffymetrixCdfNames()
	 */
	public void addAffymetrixCdfName(String cdfName) {
		this.affymetrixCdfNames.add(cdfName);
	}
	/**
	 * Add a <code>Collection</code> of <code>String</code>s 
	 * to the <code>Set</code> of <code>String</code>s defining the allowed CDF names  
	 * of Affymetrix chips to use.
	 * 
	 * @param cdfNames	some allowed CDF names of Affymetrix chips to use
	 * @see #getAffymetrixCdfNames()
	 */
	public void addAffymetrixCdfNames(Collection<String> cdfNames) {
		this.affymetrixCdfNames.addAll(cdfNames);
	}
	
	/**
	 * A <code>float</code> defining the minimum arIQR score of chips to use. 
	 * See also {@link #affymetrixMinPercentPresent} for another quality score. 
	 */
	private float affymetrixMinQualScore;
	/**
	 * Get the <code>float</code> defining the minimum arIQR score of chips to use. 
	 * See also {@link #getAffymetrixMinPercentPresent()} for another quality score. 
	 *
	 * @return a <code>float</code> being the minimum allowed arIQR score.
	 */
	public float getAffymetrixMinQualScore() {
		return this.affymetrixMinQualScore;
	}
	/**
	 * Set the <code>float</code> defining the minimum arIQR score of chips to use. 
	 * See also {@link #setAffymetrixMinPercentPresent(float)} for another quality score. 
	 * 
	 * @param affymetrixMinQualScore 	the <code>float</code> being the minimum allowed 
	 * 									arIQR score.
	 */
	public void setAffymetrixMinQualScore(float affymetrixMinQualScore) {
		this.affymetrixMinQualScore = affymetrixMinQualScore;
	}
	
	/**
	 * A <code>float</code> defining the minimum percentage of probesets identified 
	 * as 'present' by MAS5 on chips to use (a quality score, as 
	 * {@link #affymetrixMinQualScore}). 
	 */
	private float affymetrixMinPercentPresent;
	/**
	 * Get the <code>float</code> defining the minimum percentage of probesets 
	 * identified as 'present' by MAS5 on chips to use (a quality score, as 
	 * {@link #getAffymetrixMinQualScore()}). 
	 * 
	 * @return 	a <code>float</code> being the minimum allowed percentage of probesets 
	 * 			identified as 'present' by MAS5.
	 */
	public float getAffymetrixMinPercentPresent() {
		return affymetrixMinPercentPresent;
	}
	/**
	 * Set the <code>float</code> defining the minimum percentage of probesets 
	 * identified as 'present' by MAS5 on chips to use (a quality score, as 
	 * {@link #setAffymetrixMinQualScore(float)}). 
	 * 
	 * @param minPercentPresent			 	a <code>float</code> being the minimum 
	 * 										allowed percentage of probesets identified 
	 * 										as 'present' by MAS5.
	 * @throws IllegalArgumentException 	If <code>affymetrixMinPercentPresent</code> 
	 * 										is less than 0 or greater than 1. 
	 */
	public void setAffymetrixMinPercentPresent(float minPercentPresent) 
		throws IllegalArgumentException {
		log.entry(minPercentPresent);
		if (minPercentPresent < 0 || minPercentPresent > 1) {
			throw log.throwing(new IllegalArgumentException(
						"A percentage must be set to a value between 0 and 1."));
		}
		this.affymetrixMinPercentPresent = minPercentPresent;
		log.exit();
	}
	
	/**
	 * A <code>Set</code> of <code>AffymetrixDetectionType</code>s defining 
	 * the requested detection types (MAS5, Schuster method, ...) of chips to use. 
	 */
	private final Set<AffymetrixDetectionType> affymetrixDetectionTypes;
	/**
	 * Get the <code>Set</code> of <code>AffymetrixDetectionType</code>s defining 
	 * the requested detection types of Affymetrix chips to use. 
	 * 
	 * @return the requested Affymetrix detection types to used.
	 */
	public Set<AffymetrixDetectionType> getAffymetrixDetectionTypes() {
		return this.affymetrixDetectionTypes;
	}
	/**
	 * Add a <code>AffymetrixDetectionType</code> to the <code>Set</code> of 
	 * <code>AffymetrixDetectionType</code>s defining the requested detection types 
	 * of Affymetrix chips to use. 
	 * 
	 * @param chipType 	an allowed Affymetrix detection type
	 * @see #getAffymetrixDetectionTypes()
	 */
	public void addAffymetrixDetectionType(AffymetrixDetectionType chipType) {
		this.affymetrixDetectionTypes.add(chipType);
	}
	/**
	 * Add a <code>Collection</code> of <code>AffymetrixDetectionType</code>s 
	 * to the <code>Set</code> of <code>AffymetrixDetectionType</code>s defining 
	 * the requested detection types of Affymetrix chips to use. 
	 * 
	 * @param chipTypes	some allowed Affymetrix detection types.
	 * @see #getAffymetrixDetectionTypes()
	 */
	public void addAffymetrixDetectionTypes(Collection<AffymetrixDetectionType> chipTypes) {
		this.affymetrixDetectionTypes.addAll(chipTypes);
	}
	
	/**
	 * A <code>Set</code> of <code>AffymetrixNormalizationType</code>s defining 
	 * the requested normalization methods (MAS5, GCRMA, ...) of chips to use. 
	 */
	private final Set<AffymetrixNormalizationType> affymetrixNormalizationTypes;
	/**
	 * Get the <code>Set</code> of <code>AffymetrixNormalizationType</code>s defining 
	 * the requested normalization types of Affymetrix chips to use. 
	 * 
	 * @return the requested Affymetrix normalization types to used.
	 */
	public Set<AffymetrixNormalizationType> getAffymetrixNormalizationTypes() {
		return this.affymetrixNormalizationTypes;
	}
	/**
	 * Add a <code>AffymetrixNormalizationType</code> to the <code>Set</code> of 
	 * <code>AffymetrixNormalizationType</code>s defining the requested normalization types 
	 * of Affymetrix chips to use. 
	 * 
	 * @param normalizationType 	an allowed Affymetrix normalization type
	 * @see #getAffymetrixNormalizationTypes()
	 */
	public void addAffymetrixNormalizationType(AffymetrixNormalizationType normalizationType) {
		this.affymetrixNormalizationTypes.add(normalizationType);
	}
	/**
	 * Add a <code>Collection</code> of <code>AffymetrixNormalizationType</code>s 
	 * to the <code>Set</code> of <code>AffymetrixNormalizationType</code>s defining 
	 * the requested normalization types of Affymetrix chips to use. 
	 * 
	 * @param normalizationTypes	some allowed Affymetrix normalization types.
	 * @see #getAffymetrixNormalizationTypes()
	 */
	public void addAffymetrixNormalizationTypes(
			Collection<AffymetrixNormalizationType> normalizationTypes) {
		this.affymetrixNormalizationTypes.addAll(normalizationTypes);
	}
	
	/**
	 * A <code>Set</code> of <code>String</code>s to filter Affymetrix chips 
	 * based on their IDs.
	 */
	private final Set<String> affymetrixChipIds;
	/**
	 * Get the <code>Set</code> of <code>String</code>s defining the IDs of 
	 * Affymetrix chips to use. Note that these IDs are not unique over 
	 * different Affymetrix experiments, so you should also set restrictions 
	 * on Affymetrix experiment IDs (see {@link #getAffymetrixExpIds()}).
	 * 
	 * @return the IDs of allowed Affymetrix chips to used.
	 */
	public Set<String> getAffymetrixChipIds() {
		return this.affymetrixChipIds;
	}
	/**
	 * Add a <code>String</code> to the <code>Set</code> of <code>String</code>s 
	 * defining the IDs of Affymetrix chips to use. 
	 * Note that these IDs are not unique over 
	 * different Affymetrix experiments, so you should also set restrictions 
	 * on Affymetrix experiment IDs (see {@link #addAffymetrixExpId(String)}).
	 * 
	 * @param chipId 	the ID of an allowed Affymetrix chip
	 * @see #getAffymetrixChipIds()
	 */
	public void addAffymetrixChipId(String chipId) {
		this.affymetrixChipIds.add(chipId);
	}
	/**
	 * Add a <code>Collection</code> of <code>String</code>s 
	 * to the <code>Set</code> of <code>String</code>s defining the IDs of 
	 * the Affymetrix chips to use. Note that these IDs are not unique over 
	 * different Affymetrix experiments, so you should also set restrictions 
	 * on Affymetrix experiment IDs (see {@link #addAffymetrixExpIds(Collection)}).
	 * 
	 * @param chipIds	the IDs of some allowed Affymetrix chips
	 * @see #getAffymetrixChipIds()
	 */
	public void addAffymetrixChipIds(Collection<String> chipIds) {
		this.affymetrixChipIds.addAll(chipIds);
	}
	
	/**
	 * A <code>Set</code> of <code>String</code>s to filter Affymetrix experiments 
	 * based on their IDs.
	 */
	private final Set<String> affymetrixExpIds;
	/**
	 * Get the <code>Set</code> of <code>String</code>s defining the IDs of 
	 * Affymetrix experiments to use.
	 * 
	 * @return the IDs of the allowed Affymetrix experiments to used.
	 */
	public Set<String> getAffymetrixExpIds() {
		return this.affymetrixExpIds;
	}
	/**
	 * Add a <code>String</code> to the <code>Set</code> of <code>String</code>s 
	 * defining the IDs of Affymetrix experiments to use.
	 * 
	 * @param expId 	the ID of an allowed Affymetrix experiment
	 * @see #getAffymetrixExpIds()
	 */
	public void addAffymetrixExpId(String expId) {
		this.affymetrixExpIds.add(expId);
	}
	/**
	 * Add a <code>Collection</code> of <code>String</code>s 
	 * to the <code>Set</code> of <code>String</code>s defining the IDs of 
	 * Affymetrix experiments to use.
	 * 
	 * @param expIds	the IDs of some allowed Affymetrix experiments
	 * @see #getAffymetrixExpIds()
	 */
	public void addAffymetrixExpIds(Collection<String> expIds) {
		this.affymetrixExpIds.addAll(expIds);
	}
	
	//**********************************
	//  IN SITU DATA
	//**********************************
	/**
	 * A <code>Set</code> of <code>String</code>s to filter in situ spots 
	 * based on their IDs.
	 */
	private final Set<String> inSituSpotIds;
	/**
	 * Get the <code>Set</code> of <code>String</code>s defining the IDs of 
	 * in situ spots to use.
	 * 
	 * @return the IDs of the allowed in situ spots to used.
	 */
	public Set<String> getInSituSpotIds() {
		return this.inSituSpotIds;
	}
	/**
	 * Add a <code>String</code> to the <code>Set</code> of <code>String</code>s 
	 * defining the IDs of in situ spots to use.
	 * 
	 * @param spotId 	the ID of an allowed in situ spot
	 * @see #getInSituSpotIds()
	 */
	public void addInSituSpotId(String spotId) {
		this.inSituSpotIds.add(spotId);
	}
	/**
	 * Add a <code>Collection</code> of <code>String</code>s 
	 * to the <code>Set</code> of <code>String</code>s defining the IDs of 
	 * in situ spots to use.
	 * 
	 * @param spotIds	the IDs of some allowed in situ spots
	 * @see #getInSituSpotIds()
	 */
	public void addInSituSpotIds(Collection<String> spotIds) {
		this.inSituSpotIds.addAll(spotIds);
	}
	
	/**
	 * A <code>Set</code> of <code>String</code>s to filter in situ evidences 
	 * based on their IDs.
	 */
	private final Set<String> inSituEvidenceIds;
	/**
	 * Get the <code>Set</code> of <code>String</code>s defining the IDs of 
	 * in situ evidences to use.
	 * 
	 * @return the IDs of the allowed in situ evidences to used.
	 */
	public Set<String> getInSituEvidenceIds() {
		return this.inSituEvidenceIds;
	}
	/**
	 * Add a <code>String</code> to the <code>Set</code> of <code>String</code>s 
	 * defining the IDs of in situ evidences to use.
	 * 
	 * @param evidenceId 	the ID of an allowed in situ evidence
	 * @see #getInSituEvidenceIds()
	 */
	public void addInSituEvidenceId(String evidenceId) {
		this.inSituEvidenceIds.add(evidenceId);
	}
	/**
	 * Add a <code>Collection</code> of <code>String</code>s 
	 * to the <code>Set</code> of <code>String</code>s defining the IDs of 
	 * in situ evidences to use.
	 * 
	 * @param evidenceIds	the IDs of some allowed in situ evidences
	 * @see #getInSituEvidenceIds()
	 */
	public void addInSituEvidenceIds(Collection<String> evidenceIds) {
		this.inSituEvidenceIds.addAll(evidenceIds);
	}
	
	/**
	 * A <code>Set</code> of <code>String</code>s to filter in situ experiments 
	 * based on their IDs.
	 */
	private final Set<String> inSituExpIds;
	/**
	 * Get the <code>Set</code> of <code>String</code>s defining the IDs of 
	 * in situ experiments to use.
	 * 
	 * @return the IDs of the allowed in situ experiments to used.
	 */
	public Set<String> getInSituExpIds() {
		return this.inSituExpIds;
	}
	/**
	 * Add a <code>String</code> to the <code>Set</code> of <code>String</code>s 
	 * defining the IDs of in situ experiments to use.
	 * 
	 * @param expId 	the ID of an allowed in situ experiment
	 * @see #getInSituExpIds()
	 */
	public void addInSituExpId(String expId) {
		this.inSituExpIds.add(expId);
	}
	/**
	 * Add a <code>Collection</code> of <code>String</code>s 
	 * to the <code>Set</code> of <code>String</code>s defining the IDs of 
	 * in situ experiments to use.
	 * 
	 * @param expIds	the IDs of some allowed in situ experiments
	 * @see #getInSituExpIds()
	 */
	public void addInSituExpIds(Collection<String> expIds) {
		this.inSituExpIds.addAll(expIds);
	}
	
	//**********************************
	//  RNA-SEQ DATA
	//**********************************
	/**
	 * A <code>Set</code> of <code>String</code>s to filter data based on 
	 * the RNA-Seq run IDs. There is no corresponding class in this application.
	 */
	private final Set<String> rnaSeqRunIds;
	/**
	 * Get the <code>Set</code> of <code>String</code>s defining the IDs of 
	 * RNA-Seq runs to use.
	 * 
	 * @return the IDs of the allowed RNA-Seq runs to used.
	 */
	public Set<String> getRNASeqRunIds() {
		return this.rnaSeqRunIds;
	}
	/**
	 * Add a <code>String</code> to the <code>Set</code> of <code>String</code>s 
	 * defining the IDs of RNA-Seq runs to use.
	 * 
	 * @param runId 	the ID of an allowed RNA-Seq run
	 * @see #getRnaSeqRunIds()
	 */
	public void addRNASeqRunId(String runId) {
		this.rnaSeqRunIds.add(runId);
	}
	/**
	 * Add a <code>Collection</code> of <code>String</code>s 
	 * to the <code>Set</code> of <code>String</code>s defining the IDs of 
	 * RNA-Seq runs to use.
	 * 
	 * @param runIds	the IDs of some allowed RNA-Seq runs
	 * @see #getRnaSeqRunIds()
	 */
	public void addRNASeqRunIds(Collection<String> runIds) {
		this.rnaSeqRunIds.addAll(runIds);
	}
	
	/**
	 * A <code>Set</code> of <code>String</code>s defining the RNA-Seq platform IDs 
	 * of data to use. There is no corresponding class in this application.
	 */
	private final Set<String> rnaSeqPlatformIds;
	/**
	 * Get the <code>Set</code> of <code>String</code>s defining the IDs of 
	 * RNA-Seq platforms to use.
	 * 
	 * @return the IDs of the allowed RNA-Seq platforms to used.
	 */
	public Set<String> getRNASeqPlatformIds() {
		return this.rnaSeqPlatformIds;
	}
	/**
	 * Add a <code>String</code> to the <code>Set</code> of <code>String</code>s 
	 * defining the IDs of RNA-Seq platforms to use.
	 * 
	 * @param platformId 	the ID of an allowed RNA-Seq platform
	 * @see #getRnaSeqPlatformIds()
	 */
	public void addRNASeqPlatformId(String platformId) {
		this.rnaSeqPlatformIds.add(platformId);
	}
	/**
	 * Add a <code>Collection</code> of <code>String</code>s 
	 * to the <code>Set</code> of <code>String</code>s defining the IDs of 
	 * RNA-Seq platforms to use.
	 * 
	 * @param platformIds	the IDs of some allowed RNA-Seq platforms
	 * @see #getRnaSeqPlatformIds()
	 */
	public void addRNASeqPlatformIds(Collection<String> platformIds) {
		this.rnaSeqPlatformIds.addAll(platformIds);
	}
	
	/**
	 * A <code>float</code> defining the minimum percentage of genes identified 
	 * as 'expressed' in RNA-Seq libraries to use (amongst all genes: 
	 * protein-coding, etc).
	 */
	private float rnaSeqMinPercentPresent;
	/**
	 * Get the <code>float</code> defining the minimum percentage of genes identified 
	 * as 'present' in RNA-Seq libraries to use (amongst all genes: protein-coding, etc).
	 * 
	 * @return 	a <code>float</code> being the minimum allowed percentage of genes identified 
	 * 			as 'expressed' in RNA-Seq libraries to use.
	 */
	public float getRNASeqMinPercentPresent() {
		return rnaSeqMinPercentPresent;
	}
	/**
	 * Set the <code>float</code> defining the minimum percentage of genes identified 
	 * as 'present' in RNA-Seq libraries to use (amongst all genes: protein-coding, etc).
	 * 
	 * @param rnaSeqMinPercentPresent 	a <code>float</code> being the minimum allowed 
	 * 									percentage of genes identified as 'expressed' 
	 * 									in RNA-Seq libraries to use.
	 * @throws IllegalArgumentException 	If <code>rnaSeqMinPercentPresent</code> 
	 * 										is less than 0 or greater than 1. 
	 */
	public void setRNASeqMinPercentPresent(float rnaSeqMinPercentPresent) {
		if (rnaSeqMinPercentPresent < 0 || rnaSeqMinPercentPresent > 1) {
			throw log.throwing(new IllegalArgumentException(
						"A percentage must be set to a value between 0 and 1."));
		}
		this.rnaSeqMinPercentPresent = rnaSeqMinPercentPresent;
	}
	
	/**
	 * An <code>int</code> defining the minimum count of reads aligned 
	 * of RNA-Seq libraries to use. 
	 */
	private int rnaSeqMinAlignedReadCount;
	/**
	 * Get the <code>int</code> defining the minimum count of reads aligned 
	 * of RNA-Seq libraries to use.
	 * 
	 * @return 	an <code>int</code> being the minimum count of reads aligned 
	 * 			of RNA-Seq libraries to use.
	 */
	public int getRNASeqMinAlignedReadCount() {
		return rnaSeqMinAlignedReadCount;
	}
	/**
	 * Set the <code>int</code> defining the minimum count of reads aligned 
	 * of RNA-Seq libraries to use.
	 * 
	 * @param minAlignedReadCount 	An <code>int</code> being the minimum 
	 * 								count of reads aligned of RNA-Seq libraries 
	 * 								to use.
	 */
	public void setRNASeqMinAlignedReadCount(int minAlignedReadCount) {
		this.rnaSeqMinAlignedReadCount = minAlignedReadCount;
	}
	
	/**
	 * An <code>int</code> defining the minimum length of reads 
	 * of RNA-Seq libraries to use. 
	 */
	private int rnaSeqMinReadLength;
	/**
	 * Get the <code>int</code> defining the minimum read length of RNA-Seq libraries 
	 * to use. 
	 * 
	 * @return 	the <code>int</code> being the minimum read length of RNA-Seq 
	 * 			libraries to use.
	 */
	public int getRNASeqMinReadLength() {
		return rnaSeqMinReadLength;
	}
	/**
	 * Set the <code>int</code> defining the minimum read length of RNA-Seq libraries 
	 * to use.
	 * 
	 * @param rnaSeqMinReadLength 	the <code>int</code> being the minimum read length 
	 * 								of RNA-Seq libraries to use.
	 */
	public void setRNASeqMinReadLength(int rnaSeqMinReadLength) {
		this.rnaSeqMinReadLength = rnaSeqMinReadLength;
	}
	
	/**
	 * A <code>Set</code> of <code>RNASeqLibraryType</code>s defining 
	 * the allowed types of RNA-Seq libraries to use. 
	 */
	private final Set<RNASeqLibraryType> rnaSeqLibraryTypes;
	/**
	 * Get the <code>Set</code> of <code>RNASeqLibraryType</code>s defining 
	 * the requested types of RNA-Seq libraries to use. 
	 * 
	 * @return the requested RNA-Seq library types to used.
	 */
	public Set<RNASeqLibraryType> getRNASeqLibraryTypes() {
		return this.rnaSeqLibraryTypes;
	}
	/**
	 * Add a <code>RNASeqLibraryType</code> to the <code>Set</code> of 
	 * <code>RNASeqLibraryType</code>s defining the requested types 
	 * of RNA-Seq libraries to use. 
	 * 
	 * @param libraryType 	an allowed RNA-Seq library type
	 * @see #getRnaSeqLibraryTypes()
	 */
	public void addRNASeqLibraryType(RNASeqLibraryType libraryType) {
		this.rnaSeqLibraryTypes.add(libraryType);
	}
	/**
	 * Add a <code>Collection</code> of <code>RNASeqLibraryType</code>s 
	 * to the <code>Set</code> of <code>RNASeqLibraryType</code>s defining 
	 * the requested types of RNA-Seq libraries to use. 
	 * 
	 * @param libraryTypes	some allowed RNA-Seq library types.
	 * @see #getRnaSeqLibraryTypes()
	 */
	public void addRNASeqLibraryTypes(Collection<RNASeqLibraryType> libraryTypes) {
		this.rnaSeqLibraryTypes.addAll(libraryTypes);
	}
	
	/**
	 * A <code>Set</code> of <code>String</code>s to filter RNA-Seq libraries 
	 * based on their IDs.
	 */
	private final Set<String> rnaSeqLibraryIds;
	/**
	 * Get the <code>Set</code> of <code>String</code>s defining the IDs of 
	 * RNA-Seq libraries to use.
	 * 
	 * @return the IDs of the allowed RNA-Seq libraries to used.
	 */
	public Set<String> getRNASeqLibraryIds() {
		return this.rnaSeqLibraryIds;
	}
	/**
	 * Add a <code>String</code> to the <code>Set</code> of <code>String</code>s 
	 * defining the IDs of RNA-Seq libraries to use.
	 * 
	 * @param libId 	the ID of an allowed RNA-Seq library
	 * @see #getRnaSeqLibraryIds()
	 */
	public void addRNASeqLibraryId(String libId) {
		this.rnaSeqLibraryIds.add(libId);
	}
	/**
	 * Add a <code>Collection</code> of <code>String</code>s 
	 * to the <code>Set</code> of <code>String</code>s defining the IDs of 
	 * RNA-Seq libraries to use.
	 * 
	 * @param libIds	the IDs of some allowed RNA-Seq libraries
	 * @see #getRnaSeqLibraryIds()
	 */
	public void addRNASeqLibraryIds(Collection<String> libIds) {
		this.rnaSeqLibraryIds.addAll(libIds);
	}
	
	/**
	 * A <code>Set</code> of <code>String</code>s to filter RNA-Seq experiments 
	 * based on their IDs.
	 */
	private final Set<String> rnaSeqExpIds;
	/**
	 * Get the <code>Set</code> of <code>String</code>s defining the IDs of 
	 * RNA-Seq experiments to use.
	 * 
	 * @return the IDs of the allowed RNA-Seq experiments to used.
	 */
	public Set<String> getRNASeqExpIds() {
		return this.rnaSeqExpIds;
	}
	/**
	 * Add a <code>String</code> to the <code>Set</code> of <code>String</code>s 
	 * defining the IDs of RNA-Seq experiments to use.
	 * 
	 * @param expId 	the ID of an allowed RNA-Seq experiment
	 * @see #getRnaSeqExpIds()
	 */
	public void addRNASeqExpId(String expId) {
		this.rnaSeqExpIds.add(expId);
	}
	/**
	 * Add a <code>Collection</code> of <code>String</code>s 
	 * to the <code>Set</code> of <code>String</code>s defining the IDs of 
	 * RNA-Seq experiments to use.
	 * 
	 * @param expIds	the IDs of some allowed RNA-Seq experiments
	 * @see #getRnaSeqExpIds()
	 */
	public void addRNASeqExpIds(Collection<String> expIds) {
		this.rnaSeqExpIds.addAll(expIds);
	}
}
