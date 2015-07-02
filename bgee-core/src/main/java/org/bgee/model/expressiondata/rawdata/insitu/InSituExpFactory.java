package org.bgee.model.expressiondata.rawdata.insitu;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class InSituExpFactory 
{
    public InSituExpFactory()
    {
    	
    }

//	public InSituExp getExperimentById(String inSituExperimentId) 
//	{
//		MysqlInSituExperimentDAO data = new MysqlInSituExperimentDAO();
//		return this.createInSituExperiment(data.getExperimentById(inSituExperimentId));
//	}
//	
//	private Collection<InSituExp> getInSituExperiments(Collection<TransferObject> toCollection)
//	{
//		Collection<InSituExp> expList = new ArrayList<InSituExp>();
//        Iterator<TransferObject> iterator = toCollection.iterator();
//    	
//    	while (iterator.hasNext()) {
//    		expList.add(this.createInSituExperiment((InSituExpTO) iterator.next()));
//    	}
//    	return expList;
//	}
//	
//	private InSituExp createInSituExperiment(InSituExpTO expTO)
//	{
//		InSituExp exp = null;
//		if (expTO != null) {
//			exp = new InSituExp();
//			exp.setId(expTO.id);
//			exp.setName(expTO.name);
//			exp.setDescription(expTO.description);
//			exp.setDataSourceId(expTO.dataSourceId);
//		}
//		return exp;
//	}
}
