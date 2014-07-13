package org.bgee.model.download;

public class Download
{
	private String path;
	private String date;
	private boolean    availablility;
	private int size;
	
	
	public Download()
	{
		super();
		this.setPath("");
		this.setDate("");
		this.setAvailability(false);
		this.setSize(0);
	}

	public void setPath(String path)
	{
		this.path = path;
	}
	
	public void setDate(String l) 
	{
		this.date = l;
	}
	
	public void setAvailability(boolean availability)
	{
		this.availablility = availability;
	}
	
	public void setAvailability(int availability)
	{
		if (availability == 1) {
		    this.availablility = true;
		} else {
			this.availablility = false;
		}
	}
	
	
	public String getPath()
	{
		return this.path;
	}
	
	public String getDate()
	{
		return this.date;
	}
	
	public boolean isAvailable()
	{
		return this.availablility;
	}
	
	public void setSize(int siz)
	{
		this.size = siz;
	}
	public int getSize()
	{
		return this.size;
	}
}
