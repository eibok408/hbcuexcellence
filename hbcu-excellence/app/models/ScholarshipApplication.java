package models;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import play.Play;
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
	
	public String essayResponse2;
	
	public Date timeStamp;
	
	public String uploadedFileGoogleDriveIDs;
	
	public String googleDriveFolderID;
	
	public byte[] generateReport()
    {
    	try 
    	{   		
            // step 1
            Document document = new Document();
            // step 2
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, baos);
            // step 3
            document.open();
            
            Font sectionHeader = new Font();
            sectionHeader.setStyle("bold");
            sectionHeader.setSize(18);
            sectionHeader.setColor(142, 0, 0);
            
            Font label = new Font();
            label.setStyle("bold");
            label.setSize(11);
            
            Font info = new Font();
            info.setStyle("bold");
            info.setSize(11);
            
            Font title = new Font();
            title.setStyle("bold");
            title.setSize(25);
  
            Font bodyText = new Font();
            bodyText.setSize(11);
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy HH:mm:ss z");
    
            Paragraph titleParagraph = new Paragraph("2016 Application", title);
            titleParagraph.setAlignment(Element.ALIGN_CENTER);
            document.add(titleParagraph);
           
    		document.add(new Paragraph("\nSubmission Date: " + dateFormat.format(this.timeStamp) + "\n",info));
    		
                  	
        	document.add(new Paragraph("Basic Information\n",sectionHeader));
        	document.add(new Paragraph("Applicant Name: " + this.firstname + " " + this.lastname + "\n\n",info));
        	document.add(new Paragraph("Email Address: " + this.email + "\n\n",info));
        	document.add(new Paragraph("Telephone Number: " + this.phone + "\n",info));
	        
	        document.add(new Paragraph("High School Information\n",sectionHeader));
	        document.add(new Paragraph("High School: " + this.highSchool + "\n",bodyText));
	        document.add(new Paragraph("High School Secretary Name: " + this.schoolSecretaryName + "\n",bodyText));
	        document.add(new Paragraph("High School Secretary Contact Number: " + this.schoolSecretaryPhoneNumber + "\n",bodyText));
	        document.add(new Paragraph("Unweighted Cumulative GPA: " + this.gpa + "\n",bodyText));
	        
	        document.add(new Paragraph("College Information\n",sectionHeader));
	        document.add(new Paragraph("University attending in Fall 2016: " + this.university + "\n",bodyText));
	        document.add(new Paragraph("Major: " + this.major + "\n",bodyText));
	        document.add(new Paragraph("Has anyone in your immediate family attended an HBCU?: " + (this.hasFamilyAttendedHbcu ? "Yes" : "No") + "\n",bodyText));
	        
	        document.add(new Paragraph("Letter of Recommendation Information\n",sectionHeader));
	        document.add(new Paragraph("Recommender Name: " + this.recommenderName + "\n",bodyText));
	        document.add(new Paragraph("Occupation: " + this.recommenderOccupation + "\n",bodyText));
	        document.add(new Paragraph("Relationship to applicant: " + this.recommenderRelationship + "\n",bodyText));
	        document.add(new Paragraph("Contact Phone Number: " + this.recommenderPhone + "\n",bodyText));
	        
	        document.add(new Paragraph("Essay Questions\n",sectionHeader));
	        document.add(new Paragraph("Essay Prompt 1\n",label));
	        document.add(new Paragraph("Answer: " + this.essayResponse1 + "\n\n",bodyText));
        		
            // step 5
            document.close();
 
            // setting some response headers
            return baos.toByteArray();
        }
        catch(Exception e) 
        {
            return null;
        }
    }
}
