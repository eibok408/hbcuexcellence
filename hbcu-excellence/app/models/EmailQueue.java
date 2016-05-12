package models;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import controllers.Utilities;

import play.data.validation.Required;
import siena.Filter;
import siena.Generator;
import siena.Id;
import siena.Model;
import siena.Query;

public class EmailQueue extends Model {
	
	@Id(Generator.UUID)
	public String ID;
	
	public String payload1;
	
	public String payload2;
	
	public String payload3;
	
	public String payload4;
	
	public int emailType;
	
	public Date creationDate;
	
	public boolean sent;
	
	public static class EmailType
	{		
		public static final int Recommender = 0;
	}
}