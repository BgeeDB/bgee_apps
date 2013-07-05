package org.bgee.model.dao.common.expressiondata.rawdata.est;

import java.io.Serializable;

import model.data.common.expressionData.ExpressionDataMappedToOntologiesTO;

public class ESTLibraryTO extends RawDataAnnotatedTO implements Serializable
{

	private static final long serialVersionUID = 42352L;
	
	public EstLibraryTO()
	{
		super();
		this.description = "";
		this.dataSourceId = "";
	}
	
	public String description;
	public String dataSourceId;

}
