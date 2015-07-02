package org.bgee.model.expressiondata.rawdata.insitu;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class InSituSpotFactory 
{
    public InSituSpotFactory()
    {
    	
    }

//	public Collection<InSituSpot> getSpotsByExpression(DataTypeTO expressionParam,
//			MultiSpeciesTO multiSpeciesTO) 
//	{
//		MysqlInSituSpotDAO data = new MysqlInSituSpotDAO();
//		return this.getInSituSpots(data.getSpotsByExpression(
//				expressionParam, multiSpeciesTO));
//	}
//	
//	private Collection<InSituSpot> getInSituSpots(Collection<TransferObject> toCollection)
//	{
//		Collection<InSituSpot> spotList = new ArrayList<InSituSpot>();
//        Iterator<TransferObject> iterator = toCollection.iterator();
//    	
//    	while (iterator.hasNext()) {
//    		spotList.add(this.createInSituSpot((InSituSpotTO) iterator.next()));
//    	}
//    	return spotList;
//	}
//	
//	private InSituSpot createInSituSpot(InSituSpotTO spotTO)
//	{
//		InSituSpot spot = null;
//		if (spotTO != null) {
//			spot = new InSituSpot();
//			spot.setId(spotTO.id);
//			spot.setInSituEvidenceId(spotTO.inSituEvidenceId);
//			spot.setOrganId(spotTO.organId);
//			spot.setStageId(spotTO.stageId);
//			spot.setGeneId(spotTO.geneId);
//			spot.setExpressionId(spotTO.expressionId);
//			spot.setExpressionConfidence(spotTO.expressionConfidence);
//			spot.setDetectionFlag(spotTO.detectionFlag);
//		}
//		return spot;
//	}
}
