/**
 * This code is loaded and run along with the download page to handle
 * the user's actions and update the display.
 * It is run when the document is fully loaded by using the method ready()
 * 
 * @author Mathieu Seppey
 * @version Bgee 13, Jul 2014
 */
$( document ).ready(function() {

	// Add a click listener to every figures
	$( "figure" ).click(function() {

		// fetch the img ( TODO : remove this step when the data fields have been moved in figure )
		var img = $( this ).find("img");
		
		// TODO ? Dynamic path name ? Generate the js dynamically from java ?
		var downloadPath = "download/";
		var imagePath = "img/species/";

		// fetch the texts to display in the detail box
		var latin = $( img ).data( "bgee-latin" );
		var name = $( img ).data( "bgee-name" );	

		// Generate the new image URL
		var newImg = imagePath.concat( name.concat( ".jpg" ) );

		// Generate all the downloadable files URLs
		var simplePresAbsUrl = downloadPath.concat(
				name.concat( "_simple_pres_abs.tsv" ));
		var completePresAbsUrl = downloadPath.concat(
				name.concat( "_complete_pres_abs.tsv" ));
		var simplePresOverUnder = downloadPath.concat(
				name.concat( "_simple_over_under.tsv" ));
		var completePresOverUnder = downloadPath.concat(
				name.concat( "_complete_over_under.tsv" ));

		// Display the detail box with a visual effect
		$(" #bgee_data_selection ").show( "blind" );
		
		// Update the fields
		$( "#bgee_data_selection_left img" ).attr( "src", newImg );
		$( "#bgee_data_selection_right h1" ).text( latin );
		$( "#simple_pres_abs" ).attr( "href",simplePresAbsUrl );
		$( "#complete_pres_abs" ).attr( "href",completePresAbsUrl );
		$( "#simple_over_under" ).attr( "href",simplePresOverUnder );
		$( "#complete_over_under" ).attr( "href",completePresOverUnder );

	});
});