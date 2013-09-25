package org.bgee.model.expressiondata.querytools.filters;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.TestAncestor;
import org.bgee.model.expressiondata.rawdata.affymetrix.AffymetrixChip.AffymetrixDetectionType;
import org.bgee.model.expressiondata.rawdata.affymetrix.AffymetrixChip.AffymetrixNormalizationType;
import org.bgee.model.expressiondata.rawdata.rnaseq.RNASeqLibrary.RNASeqLibraryType;
import org.junit.Test;

/**
 * Test the functionalities of {@link RawDataFilter}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class RawDataFilterTest extends TestAncestor {
	
	private final static Logger log = LogManager.getLogger(RawDataFilterTest.class.getName());

	/**
	 * Default constructor.
	 */
	public RawDataFilterTest()
	{
		super();
	}
	@Override
	protected Logger getLogger() {
		return log;
	}
	
	/**
	 * Test the setters and getters. 
	 */
	@Test
	public void shouldSetAndGet() {
		RawDataFilter filter = new RawDataFilter();
		
		filter.addAffymetrixCdfName("cdfname1");
		filter.addAffymetrixCdfNames(Arrays.asList("cdfname2", "cdfname3"));
		filter.addAffymetrixChipId("chipid1");
		filter.addAffymetrixChipIds(Arrays.asList("chipid2", "chipid3"));
		filter.addAffymetrixChipTypeId("chiptypeid1");
		filter.addAffymetrixChipTypeIds(Arrays.asList("chiptypeid2", "chiptypeid3"));
		filter.addAffymetrixDetectionType(AffymetrixDetectionType.MAS5);
		filter.addAffymetrixDetectionTypes(Arrays.asList(AffymetrixDetectionType.SCHUSTER));
		filter.addAffymetrixExpId("expid1");
		filter.addAffymetrixExpIds(Arrays.asList("expid2", "expid3"));
		filter.addAffymetrixNormalizationType(AffymetrixNormalizationType.MAS5);
		filter.addAffymetrixNormalizationTypes(
				Arrays.asList(AffymetrixNormalizationType.GCRMA, AffymetrixNormalizationType.RMA));
		filter.addAffymetrixProbesetId("probesetid1");
		filter.addAffymetrixProbesetIds(Arrays.asList("probesetid2", "probesetid3"));
		filter.setAffymetrixMinPercentPresent(0.1f);
		filter.setAffymetrixMinQualScore(0.2f);
		
		filter.addESTId("estid1");
		filter.addESTIds(Arrays.asList("estid2", "estid3"));
		filter.addESTLibraryId("estlibraryid1");
		filter.addESTLibraryIds(Arrays.asList("estlibraryid2", "estlibraryid3"));
		
		filter.addInSituEvidenceId("evidenceid1");
		filter.addInSituEvidenceIds(Arrays.asList("evidenceid2", "evidenceid3"));
		filter.addInSituExpId("insituexpid1");
		filter.addInSituExpIds(Arrays.asList("insituexpid2", "insituexpid3"));
		filter.addInSituSpotId("spotid1");
		filter.addInSituSpotIds(Arrays.asList("spotid2", "spotid3"));
		
		filter.addRNASeqExpId("rnaseqexpid1");
		filter.addRNASeqExpIds(Arrays.asList("rnaseqexpid2", "rnaseqexpid3"));
		filter.addRNASeqLibraryId("rnaseqlibid1");
		filter.addRNASeqLibraryIds(Arrays.asList("rnaseqlibid2", "rnaseqlibid3"));
		filter.addRNASeqLibraryType(RNASeqLibraryType.SINGLE);
		filter.addRNASeqLibraryTypes(Arrays.asList(RNASeqLibraryType.PAIRED));
		filter.addRNASeqPlatformId("platformid1");
		filter.addRNASeqPlatformIds(Arrays.asList("platformid2", "platformid3"));
		filter.addRNASeqRunId("runid1");
		filter.addRNASeqRunIds(Arrays.asList("runid2", "runid3"));
		filter.setRNASeqMinAlignedReadCount(2);
		filter.setRNASeqMinPercentPresent(0.3f);
		filter.setRNASeqMinReadLength(3);
		
		
		assertTrue("incorrect cdf names set", filter.getAffymetrixCdfNames().size() == 3 && 
				filter.getAffymetrixCdfNames().contains("cdfname1") && 
				filter.getAffymetrixCdfNames().contains("cdfname2") && 
				filter.getAffymetrixCdfNames().contains("cdfname3"));
		assertTrue("incorrect chip IDs set", filter.getAffymetrixChipIds().size() == 3 && 
				filter.getAffymetrixChipIds().contains("chipid1") && 
				filter.getAffymetrixChipIds().contains("chipid2") && 
				filter.getAffymetrixChipIds().contains("chipid3"));
		assertTrue("incorrect chip type IDs set", 
				filter.getAffymetrixChipTypeIds().size() == 3 && 
				filter.getAffymetrixChipTypeIds().contains("chiptypeid1") && 
				filter.getAffymetrixChipTypeIds().contains("chiptypeid2") && 
				filter.getAffymetrixChipTypeIds().contains("chiptypeid3"));
		assertTrue("incorrect detection types set", 
				filter.getAffymetrixDetectionTypes().size() == 2 && 
				filter.getAffymetrixDetectionTypes().contains(AffymetrixDetectionType.MAS5) && 
				filter.getAffymetrixDetectionTypes().contains(AffymetrixDetectionType.SCHUSTER));
		assertTrue("incorrect affymetrix experiment IDs set", 
				filter.getAffymetrixExpIds().size() == 3 && 
				filter.getAffymetrixExpIds().contains("expid1") && 
				filter.getAffymetrixExpIds().contains("expid2") && 
				filter.getAffymetrixExpIds().contains("expid3"));
		assertTrue("incorrect Affymetrix normalization type set", 
				filter.getAffymetrixNormalizationTypes().size() == 3 && 
				filter.getAffymetrixNormalizationTypes().contains(AffymetrixNormalizationType.MAS5) && 
				filter.getAffymetrixNormalizationTypes().contains(AffymetrixNormalizationType.RMA) && 
				filter.getAffymetrixNormalizationTypes().contains(AffymetrixNormalizationType.GCRMA));
		assertTrue("incorrect Affymetrix probeset IDs set", 
				filter.getAffymetrixProbesetIds().size() == 3 && 
				filter.getAffymetrixProbesetIds().contains("probesetid1") && 
				filter.getAffymetrixProbesetIds().contains("probesetid2") && 
				filter.getAffymetrixProbesetIds().contains("probesetid3"));
		assertEquals("Incorrect Affymetrix min percent present", 0.1, 
				filter.getAffymetrixMinPercentPresent(), 0.000001);
		assertEquals("Incorrect Affymetrix min quality score", 0.2f, 
				filter.getAffymetrixMinQualScore(), 0.000001);

		assertTrue("incorrect EST IDs set", filter.getESTIds().size() == 3 && 
				filter.getESTIds().contains("estid1") && 
				filter.getESTIds().contains("estid2") && 
				filter.getESTIds().contains("estid3"));
		assertTrue("incorrect EST library IDs set", 
				filter.getESTLibraryIds().size() == 3 && 
				filter.getESTLibraryIds().contains("estlibraryid1") && 
				filter.getESTLibraryIds().contains("estlibraryid2") && 
				filter.getESTLibraryIds().contains("estlibraryid3"));

		assertTrue("incorrect in situ evidence IDs set", 
				filter.getInSituEvidenceIds().size() == 3 && 
				filter.getInSituEvidenceIds().contains("evidenceid1") && 
				filter.getInSituEvidenceIds().contains("evidenceid2") && 
				filter.getInSituEvidenceIds().contains("evidenceid3"));
		assertTrue("incorrect in situ experiment IDs set", 
				filter.getInSituExpIds().size() == 3 && 
				filter.getInSituExpIds().contains("insituexpid1") && 
				filter.getInSituExpIds().contains("insituexpid2") && 
				filter.getInSituExpIds().contains("insituexpid3"));
		assertTrue("incorrect in situ spot IDs set", 
				filter.getInSituSpotIds().size() == 3 && 
				filter.getInSituSpotIds().contains("spotid1") && 
				filter.getInSituSpotIds().contains("spotid2") && 
				filter.getInSituSpotIds().contains("spotid3"));

		assertTrue("incorrect RNA-Seq experiment IDs set", 
				filter.getRNASeqExpIds().size() == 3 && 
				filter.getRNASeqExpIds().contains("rnaseqexpid1") && 
				filter.getRNASeqExpIds().contains("rnaseqexpid2") && 
				filter.getRNASeqExpIds().contains("rnaseqexpid3"));
		assertTrue("incorrect RNA-Seq library IDs set", 
				filter.getRNASeqLibraryIds().size() == 3 && 
				filter.getRNASeqLibraryIds().contains("rnaseqlibid1") && 
				filter.getRNASeqLibraryIds().contains("rnaseqlibid2") && 
				filter.getRNASeqLibraryIds().contains("rnaseqlibid3"));
		assertTrue("incorrect RNA-Seq library types set", 
				filter.getRNASeqLibraryTypes().size() == 2 && 
				filter.getRNASeqLibraryTypes().contains(RNASeqLibraryType.SINGLE) && 
				filter.getRNASeqLibraryTypes().contains(RNASeqLibraryType.PAIRED));
		assertTrue("incorrect RNA-Seq platform IDs set", 
				filter.getRNASeqPlatformIds().size() == 3 && 
				filter.getRNASeqPlatformIds().contains("platformid1") && 
				filter.getRNASeqPlatformIds().contains("platformid2") && 
				filter.getRNASeqPlatformIds().contains("platformid3"));
		assertTrue("incorrect RNA-Seq run IDs set", filter.getRNASeqRunIds().size() == 3 && 
				filter.getRNASeqRunIds().contains("runid1") && 
				filter.getRNASeqRunIds().contains("runid2") && 
				filter.getRNASeqRunIds().contains("runid3"));
		assertEquals("Incorrect min aligned read count", 2, 
				filter.getRNASeqMinAlignedReadCount());
		assertEquals("Incorrect min RNA-Seq percent present", 0.3, 
				filter.getRNASeqMinPercentPresent(), 0.000001);
		assertEquals("Incorrect min read length", 3, 
				filter.getRNASeqMinReadLength());
		
	}
	
	/**
	 * Test that {@link RawDataFilter#setAffymetrixMinPercentPresent(short)} and 
	 * {@link RawDataFilter.setRNASeqMinPercentPresent(short)} correctly throw 
	 * an {@code IllegalArgumentException} when assigned a {@code short} 
	 * less than 0 or greater than 1 (meaning, not a percentage).
	 */
	@Test
	public void shouldThrowIllegalArgumentException() {
		RawDataFilter filter = new RawDataFilter();
		
		try {
			filter.setAffymetrixMinPercentPresent(-0.1f);
			//if we reach that point, test failed
			throw new AssertionError(
			"setAffymetrixMinPercentPresent did not throw an exception when assigned -0.1");
		} catch (IllegalArgumentException e) {
			//test passed
			getLogger().catching(Level.DEBUG, e);
		}
		try {
			filter.setAffymetrixMinPercentPresent(1.1f);
			//if we reach that point, test failed
			throw new AssertionError(
			"setAffymetrixMinPercentPresent did not throw an exception when assigned 1.1");
		} catch (IllegalArgumentException e) {
			//test passed
			getLogger().catching(Level.DEBUG, e);
		}

		try {
			filter.setRNASeqMinPercentPresent(-0.1f);
			//if we reach that point, test failed
			throw new AssertionError(
			"setRNASeqMinPercentPresent did not throw an exception when assigned -0.1");
		} catch (IllegalArgumentException e) {
			//test passed
			getLogger().catching(Level.DEBUG, e);
		}
		try {
			filter.setRNASeqMinPercentPresent(1.1f);
			//if we reach that point, test failed
			throw new AssertionError(
			"setRNASeqMinPercentPresent did not throw an exception when assigned 1.1");
		} catch (IllegalArgumentException e) {
			//test passed
			getLogger().catching(Level.DEBUG, e);
		}
		
		//values should not have been set
		assertEquals("Affymetrix min percent present set", 0, 
				filter.getAffymetrixMinPercentPresent(), 0.00001);
		assertEquals("RNA-Seq min percent present set", 0, 
				filter.getRNASeqMinPercentPresent(), 0.00001);
	}

}
