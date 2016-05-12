package utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import play.libs.MimeTypes;

public class UploadedFile 
{	
	public String filename;
	public String extention;
	public String contentType;
	public byte[] contents;
	
	public UploadedFile(String filename, byte[] contents, String fileNameAndType)
	{
		this.filename = filename;
		this.contents = contents;
		
		int lastDotIndex = fileNameAndType.lastIndexOf(".");
		this.extention =  fileNameAndType.substring(lastDotIndex + 1);
		
		if(this.extention.equals("txt"))
		{
			this.contentType = "text/plain";
		}
		else
		{
			this.contentType = MimeTypes.getContentType(fileNameAndType);
		}
	}
}
