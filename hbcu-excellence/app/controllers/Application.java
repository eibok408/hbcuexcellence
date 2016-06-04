package controllers;

import play.*;
import play.data.validation.Valid;
import play.mvc.*;
import utils.Contact;
import utils.UploadedFile;

import java.util.*;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.RetryOptions;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.gson.Gson;

import models.*;

public class Application extends Controller
{

    public static void index()
    {
        render();
    }
	
	public static void aboutHbcus()
    {
        render();
    }

    public static void scholarshipOverview()
    {
        render();
    }
    
    public static void scholarshipApplication()
    {
        render();
    }
    
    public static void scholarshipApplicationPost(@Valid ScholarshipApplication submission)
    {
    	if (validation.hasErrors())
    	{
    		render("@scholarshipApplication");
    	}
    	
    	// If the form passes validation set the value of the time stamp immediately.
    	submission.timeStamp = new Date();
    	
    	// Extract all upload files from the HTTP POST parameters and add them to a list of uploaded files.
    	List<UploadedFile> uploads = new ArrayList<UploadedFile>();
    	uploads.add(new UploadedFile("Transcript", params.get("transcript", byte[].class), params.get("transcriptFileName")));
    	uploads.add(new UploadedFile("Resume", params.get("resume", byte[].class), params.get("resumeFileName")));
    	uploads.add(new UploadedFile("LetterOfRecommendation", params.get("letterOfRecommendation", byte[].class), params.get("letterOfRecommendationFileName")));
    	uploads.add(new UploadedFile("Photo", params.get("photo", byte[].class), params.get("photoFileName")));
    	uploads.add(new UploadedFile("Application", submission.generateReport(), "application.pdf"));
    	
    	// Create a folder for storing application materials.
    	String parentFolderName = submission.firstname + " " + submission.lastname;
    	submission.googleDriveFolderID = Utilities.createGoogleDriveFolder(parentFolderName);
		
		// Upload all files associated with the application to the Google drive folder.
		HashMap<String, String> uploadedFileIDs = Utilities.uploadFilesToGoogleDrive(uploads, submission.googleDriveFolderID, parentFolderName);
		submission.uploadedFileGoogleDriveIDs = new Gson().toJson(uploadedFileIDs);
		submission.insert();
    	
    	scholarshipApplicationConfirmation();
    }
    
    public static void scholarshipApplicationConfirmation()
    {
        render();
    }
    
    public static void contact(String successMessage)
    {
    	render(successMessage);
    }
    
    public static void contactPost(@Valid Contact contact)
    {
    	if(validation.hasErrors())
    	{
    		render("@contact");
    	}
    	
    	Queue queue = QueueFactory.getDefaultQueue();
		TaskOptions taskOptions = TaskOptions.Builder.withUrl("/Utilities/sendContactEmail");					
		taskOptions.param("contact.name", contact.name);
		taskOptions.param("contact.email", contact.email);
		taskOptions.param("contact.message", contact.message);
		taskOptions.countdownMillis(10 * 1000);
		taskOptions.retryOptions(RetryOptions.Builder.withTaskRetryLimit(0));
		queue.add(taskOptions);
    	
    	contact("Contact request has been sent.");
    }
}
