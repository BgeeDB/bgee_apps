package org.bgee.model.data.common;

import org.bgee.model.data.sql.mysql.MySQLDAOFactory;

public abstract class DAOFactory 
{
	public enum DataSourceType {
		MYSQL;
	}
	
	public static synchronized DAOFactory getDAOFactory(DataSourceType type)
	{
		//TODO: when type == null, get the default value from the properties
		switch (type) {
		case MYSQL: 
			return new MySQLDAOFactory();
		default: 
			return null;
		}
	}
	
	public static synchronized DAOFactory getDAOFactory()
	{
		return DAOFactory.getDAOFactory(null);
	}
	
}
