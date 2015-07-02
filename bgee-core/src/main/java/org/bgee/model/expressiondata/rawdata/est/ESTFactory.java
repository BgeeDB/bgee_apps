package org.bgee.model.expressiondata.rawdata.est;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class ESTFactory 
{
    public ESTFactory()
    {
    	
    }
    
//    public Collection<EST> getEstsByExpression(DataTypeTO expressionParam, 
//    		MultiSpeciesTO multiSpeciesTO)
//	{
//    	MysqlEstDAO data = new MysqlEstDAO();
//    	return this.getEsts(data.getEstsByExpression(expressionParam, multiSpeciesTO));
//	}
//    
//    private Collection<EST> getEsts(Collection<TransferObject> toCollection)
//    {
//    	Collection<EST> estList = new ArrayList<EST>();
//        Iterator<TransferObject> iterator = toCollection.iterator();
//    	
//    	while (iterator.hasNext()) {
//    		estList.add(this.createEst((EstTO) iterator.next()));
//    	}
//    	return estList;
//    }
//    
//    private EST createEst(EstTO estTO)
//    {
//    	EST est = null;
//    	if (estTO != null) {
//    		est = new EST();
//    		est.setId(estTO.id);
//    		est.setEstLibraryId(estTO.estLibraryId);
//    		est.setGeneId(estTO.geneId);
//    		est.setExpressionId(estTO.expressionId);
//    		est.setExpressionConfidence(estTO.expressionConfidence);
//    	}
//    	return est;
//    }
}
