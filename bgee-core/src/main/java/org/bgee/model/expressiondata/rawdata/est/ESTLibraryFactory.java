package org.bgee.model.expressiondata.rawdata.est;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class ESTLibraryFactory 
{
    public ESTLibraryFactory()
    {
    	
    }


//	public ESTLibrary getEstLibraryById(String estLibraryId) 
//	{
//		MysqlEstLibraryDAO data = new MysqlEstLibraryDAO();
//		return this.createEstLibrary(data.getEstLibraryById(estLibraryId));
//	}
//    
//    private Collection<ESTLibrary> getEstLibraries(Collection<TransferObject> toCollection)
//    {
//    	Collection<ESTLibrary> estLibraryList = new ArrayList<ESTLibrary>();
//        Iterator<TransferObject> iterator = toCollection.iterator();
//    	
//    	while (iterator.hasNext()) {
//    		estLibraryList.add(this.createEstLibrary((EstLibraryTO) iterator.next()));
//    	}
//    	return estLibraryList;
//    }
//    
//    private ESTLibrary createEstLibrary(EstLibraryTO estLibraryTO)
//    {
//    	ESTLibrary eSTLibrary = null;
//    	if (estLibraryTO != null) {
//    		eSTLibrary = new ESTLibrary();
//    		eSTLibrary.setId(estLibraryTO.id);
//    		eSTLibrary.setName(estLibraryTO.name);
//    		eSTLibrary.setDescription(estLibraryTO.description);
//    		eSTLibrary.setOrganId(estLibraryTO.organId);
//    		eSTLibrary.setStageId(estLibraryTO.stageId);
//    		eSTLibrary.setDataSourceId(estLibraryTO.dataSourceId);
//    	}
//    	return eSTLibrary;
//    }
}
