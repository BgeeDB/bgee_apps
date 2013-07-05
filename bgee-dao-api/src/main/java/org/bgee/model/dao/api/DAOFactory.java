package org.bgee.model.dao.api;

public abstract class DAOFactory 
{
	
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
