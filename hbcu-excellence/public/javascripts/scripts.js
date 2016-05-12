function getFilePaths()
{
     var filePath=$("#transcript").val();     
     if(!filePath)
	 {
    	alert("Please attach a transcript!");
    	return false;
	 }
     $("#transcriptFileName").val(filePath);
     
     filePath=$("#resume").val();
     if(!filePath)
	 {
    	alert("Please attach a Resume!");
    	return false;
	 }
     $("#resumeFileName").val(filePath);
 
     filePath=$("#letterOfRecommendation").val();
     if(!filePath)
	 {
    	alert("Please attach a letter of recommendation!");
    	return false;
	 }
     $("#letterOfRecommendationFileName").val(filePath);
     
     filePath=$("#photo").val();
     if(!filePath)
	 {
    	alert("Please attach a photo!");
    	return false;
	 }
     $("#photoFileName").val(filePath);
     
     $("#loading").show();
}

function getFileSize(input)
{
	// 5 MB
	var max_file_size = 5120000;
	var file = input.files[0];
	
	if(file.size > max_file_size)
	{
		alert("File cannot be larger than 5MB, this file is too large.");
		input.value = "";
	}
}

$(document).ready(function () {

    $("#slideshow").cycle({ fx: 'scrollLeft', delay: 7000, easing:  'easeInOutBack' });
});
