package org.bgee.model.expressiondata.rawdata.insitu;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class InSituEvidenceFactory 
{
    public InSituEvidenceFactory()
    {
    	
    }

//	public InSituEvidence getEvidenceById(String inSituEvidenceId) 
//	{
//		MysqlInSituEvidenceDAO data = new MysqlInSituEvidenceDAO();
//		return this.createInSituEvidence(data.getEvidenceById(inSituEvidenceId));
//	}
//	
//	private Collection<InSituEvidence> getInSituEvidences(Collection<TransferObject> toCollection)
//	{
//		Collection<InSituEvidence> evidenceList = new ArrayList<InSituEvidence>();
//        Iterator<TransferObject> iterator = toCollection.iterator();
//    	
//    	while (iterator.hasNext()) {
//    		evidenceList.add(this.createInSituEvidence((InSituEvidenceTO) iterator.next()));
//    	}
//    	return evidenceList;
//	}
//	
//	private InSituEvidence createInSituEvidence(InSituEvidenceTO evidenceTO)
//	{
//		InSituEvidence evidence = null;
//		if (evidenceTO != null) {
//			evidence = new InSituEvidence();
//			evidence.setId(evidenceTO.id);
//			evidence.setInSituExperimentId(evidenceTO.inSituExperimentId);
//		}
//		return evidence;
//	}
}
