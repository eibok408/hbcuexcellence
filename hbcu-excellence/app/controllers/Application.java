package controllers;

import play.*;
import play.data.validation.Valid;
import play.mvc.*;
import utils.Contact;
import utils.RecaptchaResponseVerification;
import utils.UploadedFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.RetryOptions;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.gson.Gson;

import models.*;

public class Application extends Controller
{
	private static final String RecaptchaVerificationUrl = "https://www.google.com/recaptcha/api/siteverify";
	
    public static void index()
    {
        render();
    }
     public static void pastwinners()
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
    		return;
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
    	if (validation.hasErrors())
    	{
    		render("@contact");
    		return;
    	}
    	
    	if (!validateRecaptcha(params.get("g-recaptcha-response"), request.remoteAddress))
    	{
    		render("@contact");
    		return;
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
    
    private static boolean validateRecaptcha(String recaptchaResponse, String ipAddress)
    {
    	if (recaptchaResponse == null || recaptchaResponse.isEmpty())
    	{
    		return false;
    	}
    	
    	if (ipAddress == null || ipAddress.isEmpty())
    	{
    		return false;
    	}
    	
    	try
    	{
	    	// Call ReCaptcha API.
	    	URL requestUrl = new URL(String.format(
    			"%s?secret=%s&response=%s&remoteip=%s",
    			RecaptchaVerificationUrl,
    			Play.configuration.getProperty("recaptcha.secret"),
    			recaptchaResponse,
    			ipAddress));
    	
	    	HttpURLConnection httpURLConnection = (HttpURLConnection)requestUrl.openConnection();
	    	httpURLConnection.setRequestMethod("POST");
	    	httpURLConnection.setDoOutput(true);
	    	httpURLConnection.setReadTimeout(10000);
    		httpURLConnection.connect();
    		
    		// Verify the request was successful.
    		int responseCode = httpURLConnection.getResponseCode();
    		if (responseCode >= 400)
    		{
    			return false;
    		}
    		
    		// Read the response body.
    		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
    		StringBuilder stringBuilder = new StringBuilder();
    		String line = null;
    		
    		while ((line = bufferedReader.readLine()) != null)
	        {
    			stringBuilder.append(line);
	        }
    		
    		// Deserialize the response body.
    		Gson gson = new Gson();
    		RecaptchaResponseVerification recaptchaResponseVerification = gson.fromJson(stringBuilder.toString(), RecaptchaResponseVerification.class);
    		return recaptchaResponseVerification.success;
    	}
    	catch (Exception e)
    	{
    		return false;
    	}
    }
}
