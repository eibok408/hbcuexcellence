package controllers;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.ParentReference;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.RetryOptions;
import com.google.appengine.api.taskqueue.TaskOptions;

import models.EmailQueue;
import models.EmailQueue.EmailType;
import play.Play;
import play.mvc.Controller;
import play.mvc.Http.Header;
import siena.Model;
import utils.Contact;
import utils.UploadedFile;

public class Utilities extends Controller 
{
	private static final String EmailSenderUsername = "HBCU Excellence Website";
	
	public static Drive getDriveService()
    {
    	try
    	{    		
    		String SERVICE_ACCOUNT_EMAIL = "1004041749929-compute@developer.gserviceaccount.com";
    		java.io.File KEY_FILE = Play.getFile("/private/HBCU-EXCELLENCE-490f562aaf9e.p12");
    		
    		HttpTransport httpTransport = new NetHttpTransport();
    		JacksonFactory jsonFactory = new JacksonFactory();
    		
			@SuppressWarnings("deprecation")
			GoogleCredential credential = new GoogleCredential.Builder()
    		      .setTransport(httpTransport)
    		      .setJsonFactory(jsonFactory)
    		      .setServiceAccountScopes(DriveScopes.DRIVE)
    		      .setServiceAccountId(SERVICE_ACCOUNT_EMAIL)
    		      .setServiceAccountPrivateKeyFromP12File(KEY_FILE)
    		      .build();
    		  
    		Drive service = new Drive.Builder(httpTransport, jsonFactory, null)
    		      .setHttpRequestInitializer(credential)
    		      .setApplicationName(Play.configuration.getProperty("application.name"))
    		      .build();
	        
	       return service;
    	}
    	catch(Exception e)
    	{
    		error(e.getMessage());
    		return null;
    	}
    }
	
	public static String createZipByteArray(List<UploadedFile> files, String parentFolderId, String parentFolderName) 
	{
        try 
        {
        	ByteArrayOutputStream output = new ByteArrayOutputStream();
            ZipOutputStream zipoutput = new ZipOutputStream(output);
            
        	for(UploadedFile file : files)
        	{
	        	ZipEntry zipEntry = new ZipEntry(file.filename + file.extention);
	            zipoutput.putNextEntry(zipEntry);
	            zipoutput.write(file.contents);
	            zipoutput.closeEntry();
        	}
        	
            zipoutput.close();
            output.close();
            
            byte[] zipfile = output.toByteArray();
            String zipfileContentType = "application/zip";
            
            Drive service = Utilities.getDriveService();
            String fileName = "GearUpApplication_" + parentFolderName.replace(" ", "_") + ".zip";
	    	
	        File body = new File();
	        body.setTitle(fileName);
	        body.setDescription(fileName);
	        body.setMimeType(zipfileContentType);
	        body.setParents(Arrays.asList(new ParentReference().setId(parentFolderId)));
	        ByteArrayContent bytes = new ByteArrayContent(zipfileContentType, zipfile);
	        
	        File drivefile = service.files().insert(body, bytes).execute();
	        return drivefile.getId();
        } 
        catch(Exception e) 
        {
        	return null;
        }        
	}
	
	public static void sendContactEmail(Contact contact) throws Exception
    {
		String subject = "HBCU Excellence Website Contact Request - " + contact.name;
        String to = Play.configuration.getProperty("mail.smtp.user2");
        String replyTo = contact.email;
        String htmlText = String.format("<b>Name: %s</b><br/><br/><p>%s</p>", contact.name, contact.message);
        sendEmail(subject, to, replyTo, htmlText);
    }
	
	private static void sendEmail(String subject, String to, String replyTo, String htmlText) throws Exception
    {    	
    	String senderUsername = Play.configuration.getProperty("mail.smtp.user");
        String password = Play.configuration.getProperty("mail.smtp.pass");
        
        Properties props = new Properties();
    	props.put("mail.smtp.channel", "ssl");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "587");
		props.put("mail.smtp.user", senderUsername);
		props.put("mail.smtp.pass", password);
        
		try
		{
			InternetAddress[] replyToList = new InternetAddress[] { new InternetAddress(replyTo, EmailSenderUsername) };

			// Create email 
			Session session = Session.getInstance(props);
			MimeMessage msg = new MimeMessage(session);
	        msg.setFrom(new InternetAddress(senderUsername, EmailSenderUsername));
	        msg.setReplyTo(replyToList);
	        msg.addRecipient(Message.RecipientType.TO, new InternetAddress(to, null));          
	        msg.setSubject(subject);
	        
	        // Set body
	        BodyPart body = new MimeBodyPart();
	        body.setContent(htmlText, "text/html");
	        
	        // Add parts to message
	        Multipart multipart = new MimeMultipart();
	        multipart.addBodyPart(body);
	        msg.setContent(multipart);
	        
	        Transport.send(msg);
		}
		catch (Exception e)
		{
			EmailQueue email = new EmailQueue();
			email.payload1 = subject;
			email.payload2 = to;
			email.payload3 = replyTo;
			email.payload4 = htmlText;
			email.creationDate = new Date();
			email.insert();
			
			throw e;
		}
    }
	
	public static HashMap<String, String> uploadFilesToGoogleDrive(List<UploadedFile> files, String parentFolderId, String parentFolderName)
	{
		Drive service = Utilities.getDriveService();
		HashMap<String, String> uploadedFileIds = new HashMap<String, String>();
		
		for(UploadedFile file : files)
		{
	    	String fileName = parentFolderName.replace(" ", "_") + "_" + file.filename;
	    	
	        File body = new File();
	        body.setTitle(fileName);
	        body.setDescription(file.filename);
	        body.setMimeType(file.contentType);
	        body.setParents(Arrays.asList(new ParentReference().setId(parentFolderId)));
	        ByteArrayContent bytes = new ByteArrayContent(file.contentType, file.contents);
	       
	        try
	        {	        
		        File drivefile = service.files().insert(body, bytes).execute();
		        uploadedFileIds.put(file.filename + "_GoogleDriveId", drivefile.getId());
	        }
	        catch(Exception e)
	        {}
		}
		
		return uploadedFileIds; 
	}
	
	public static String createGoogleDriveFolder(String folderName)
	{
		Drive service = getDriveService();
		
		// Create a Google drive folder for storing application materials.
        File body = new File();
        body.setTitle(folderName);
        body.setDescription("A Google drive folder for storing application materials");
        body.setMimeType("application/vnd.google-apps.folder");	
        body.setParents(Arrays.asList(new ParentReference().setId(Play.configuration.getProperty("drive.folderid"))));
        
        // Create the folder.
        try
        {
        	File file = service.files().insert(body).execute();
        	return file.getId();
        }
        catch(Exception e)
        {
        	return null;
        } 
	}
	
	public static void ping()
	{
		Header header = request.headers.get("x-appengine-cron");
    	
    	if(header == null || !header.value().equalsIgnoreCase("true"))
    	{
    		notFound();
    	}
	}
	
	public static void sendQueuedEmails()
    {
    	Header header = request.headers.get("x-appengine-cron");
    	
    	if(header == null || !header.value().equalsIgnoreCase("true"))
    	{
    		notFound();
    	}
    	
    	List<EmailQueue> queuedEmails = Model.all(EmailQueue.class).filter("sent", false).order("creationDate").fetch();
    	Queue queue = QueueFactory.getDefaultQueue();
    	TaskOptions taskOptions = null;
    	
    	for(EmailQueue email:queuedEmails)
    	{	
    		if(email.emailType == EmailType.Recommender)
    		{
    			taskOptions = TaskOptions.Builder.withUrl("/Utilities/sendRecommendationEmail");					
				taskOptions.param("recommenderEmail", email.payload1);
				taskOptions.param("applicationID", email.payload2);
    		}
    		
	        taskOptions.countdownMillis(10 * 1000);
	        taskOptions.retryOptions(RetryOptions.Builder.withTaskRetryLimit(0));
			            
			// Create Task and insert into PULL Queue	
			queue.add(taskOptions);
			email.sent = true;
			email.update();
    	}
    }	
}
