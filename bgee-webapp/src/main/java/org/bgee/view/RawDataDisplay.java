package org.bgee.view;

import org.bgee.model.expressiondata.rawdata.insitu.InSituSpot;
import org.bgee.model.expressiondata.rawdata.microarray.AffymetrixProbeset;

import java.util.stream.Stream;

/**
 * Interface defining methods to be implemented by views related to {@code RawCallSourceContainer}s.
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Sept. 2018
 * @since   Bgee 14, Aug. 2018
 */
public interface RawDataDisplay {

	/**
	 * Displays the default raw call page, when no arguments are given.
	 */
	void displayRawCallHomePage();
	
	/**
	 * Displays specific raw call page.
	 * 
	 * @param affymetrixProbesets
	 * @param rnaSeqResults
	 * @param inSituSpots
	 * @param ests
	 */
//	void displayRawCallPage(Stream<AffymetrixProbeset> affymetrixProbesets, Stream<RNASeqResult> rnaSeqResults, Stream<InSituSpot> inSituSpots, Stream<EST> ests);
	void displayRawCallPage(Stream<AffymetrixProbeset> affymetrixProbesets, Stream<InSituSpot> inSituSpots);
}
