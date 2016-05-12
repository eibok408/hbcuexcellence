package models;

import java.util.Date;

import play.data.validation.Email;
import play.data.validation.Phone;
import play.data.validation.Required;
import siena.Generator;
import siena.Model;
import siena.Id;

public class ScholarshipApplication extends Model
{
	@Id(Generator.UUID)
	public String Id;
	
	@Required
	public String firstname;
	
	@Required
	public String lastname;
	
	@Phone
	@Required
	public String phone;
	
	@Email
	@Required
	public String email;

	@Required
	public String highSchool;
	
	@Required
	public String schoolSecretaryName;
	
	@Required
	public String schoolSecretaryPhoneNumber;
	
	@Required
	public String gpa;
	
	@Required
	public String university;
	
	@Required
	public String major;
	
	@Required
	public Boolean hasFamilyAttendedHbcu;
	
	@Required
	public String recommenderName;
	
	@Required
	public String recommenderOccupation;
	
	@Required
	public String recommenderRelationship;
	
	@Required
	public String recommenderPhone;
	
	@Required
	public String essayResponse1;
	
	@Required
	public String essayResponse2;
	
	public Date timeStamp;
	
	public String uploadedFileGoogleDriveIDs;
	
	public String googleDriveFolderID;
}
