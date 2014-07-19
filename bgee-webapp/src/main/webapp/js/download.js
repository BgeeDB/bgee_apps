/**
 * This code is loaded and run along with the download page to handle
 * the user"s actions and update the display.
 * It is run when the document is fully loaded by using the method ready()
 * 
 * @author Mathieu Seppey
 * @version Bgee 13, Jul 2014
 */
$( document ).ready(function() {

	// Fetch all the figure tag that represent the species or groups
	var $species = $( "figure" );
	// Fetch the detail box
	var $bgeeDataSelection = $( "#bgee_data_selection" );
	// Generate the correct id from the bgeespeciesid contained in the images
	$species.each(function() {
		var id = "";
		$( this ).find( "img" ).each(function() {
			id = id + $( this ).data( "bgeespeciesid" ) + "_";
		});
		id = id.slice( 0, - 1 ); // Remove the extra _
		$( this ).attr( "id", id );
	});
	// Read the id in the hash of the URL and load the corresponding details if present
	var speciesId = window.location.hash.substr( 3 ); // Remove 'id' before the real id that was  
	if( speciesId ){								  // added to avoid the automatic anchor behavior
		var $currentSpecies = $( "#"+speciesId );
		loadDetails( $currentSpecies );
	}
	// Add a click listener to every figures to load the corresponding details or to hide it
	$species.click(function() {
		if($( this ).hasClass( "selected" )){
			$( this ).removeClass("selected");
			$bgeeDataSelection.hide( "blind" );
			window.location.hash = ""; 
		}
		else{
			loadDetails($( this ));
			// Reset the search
			$bgeeSearchBox.val( "" );
			search($bgeeSearchBox.val());
		}
	});
	// Fetch the search box in the DOM
	var $bgeeSearchBox = $( "#bgee_search_box input" );
	// Add a listener to several event to trigger the search
	$bgeeSearchBox.bind( "keyup change paste cut", function() {
		search( $( this ).val() );
	});
	// And to reset the search in the case of the event focus
	$bgeeSearchBox.focus(function() {
		$( this ).val( "" );
		search($( this ).val());
	});

	/**
	 * This function update and display the detail box for the species or group provided
	 * 
	 * @param $currentSpecies	The DOM element <figure> that contains the species or group 
	 * 							to be loaded
	 */
	function loadDetails($currentSpecies){

		// Fetch all DOM elements and values needed to update the display
		var id = $currentSpecies.attr( "id" );
		// The images contain the data fields related to the species
		var $images = $currentSpecies.find( "img" ); 
		var bgeeSpeciesName = $images.data( "bgeespeciesname" ); // the last one is kept when 
		// multiple values, no consequence
		var bgeeSpeciesCommonNames = $images.data( "bgeespeciescommonname" );
		var bgeeGroupName = $currentSpecies.data( "bgeegroupname" );
		// Load the files urls for the current species/group
		var urls = getUrls( id );
		// Fetch all DOM elements to be updated
		var $bgeeDataSelectionImg = $( "#bgee_data_selection_img" );
		var $bgeeDataSelectionText = $( "#bgee_data_selection_text h1" );
		var $exprSimpleCsv = $( "#expr_simple_csv" );
		var $exprSimpleTsv = $( "#expr_simple_tsv" );
		var $exprCompleteCsv = $( "#expr_complete_csv" );		
		var $exprCompleteTsv = $( "#expr_complete_tsv" );	
		var $overUnderSimpleCsv = $( "#overunder_simple_csv" );
		var $overUnderSimpleTsv = $( "#overunder_simple_tsv" );
		var $overUnderCompleteCsv = $( "#overunder_complete_csv" );		
		var $overUnderCompleteTsv = $( "#overunder_complete_tsv" );
		// Proceed to the update
		$bgeeDataSelectionImg.empty();
		$images.each( function(){
			// For each image, clone its DOM structure into the detail box, update its src
			// to use the high resolution image and update its height and width depending on
			// the number of images to display at the same time ( i.e. more than one for a group )
			var quantity = $images.size();
			var $newElement = $( this ).clone();
			var newUrl = $newElement.attr("src").replace("_light","");
			$newElement.attr("src",newUrl);
			$bgeeDataSelectionImg.append($newElement);
			// Calculate the height so it would allow the images to fit into the space.
			// Divide the height by 1 for 1 image, by 2 for 2,3,4 img, by 3 for 5,6,7,8,9 and etc.
			var newHeight = $newElement.height() / (Math.ceil(Math.sqrt(quantity))); 
			if(quantity > 1){
				newHeight = newHeight * 0.8; // Make the img a bit smaller because of the margins
			}
			// Assume that the image is a square, so height and width are the same
			$newElement.css("height",newHeight).css("width",newHeight);			
		});
		// if it is a group, use the group name as label, else the species name
		if(bgeeGroupName){
			$bgeeDataSelectionText.text(bgeeGroupName);
		}
		else {
			$bgeeDataSelectionText.text(bgeeSpeciesName);
		}
		// Update the values of the download links 
		$exprSimpleCsv.attr("href",urls["expr_simple_csv"]);
		$exprSimpleTsv.attr("href",urls["expr_simple_tsv"]);
		$exprCompleteCsv.attr("href",urls["expr_complete_csv"]);		
		$exprCompleteTsv.attr("href",urls["expr_complete_tsv"]);	
		$overUnderSimpleCsv.attr("href",urls["overunder_simple_csv"]);
		$overUnderSimpleTsv.attr("href",urls["overunder_simple_tsv"]);
		$overUnderCompleteCsv.attr("href",urls["overunder_complete_csv"]);		
		$overUnderCompleteTsv.attr("href",urls["overunder_complete_tsv"]);	

		// Set the 'selected' css class to the current species figure and display the detail 
		// box with a visual effect
		$species.removeClass("selected");
		$currentSpecies.addClass("selected");
		// Calculate before which element the detail box has to be placed
		var yPos = $currentSpecies.position().top;
		var $nextLineFirstElement = null; // The element before which the detail box will be moved
		$currentSpecies.nextAll().each(function() {
			// Find the first element that has a yPos different from the element the user clicked on
			if($( this ).position().top > yPos){
				$nextLineFirstElement = $( this );
				return false; // Exit from the each when found
			}
		});

		// Move the detail box, before the next line first element if it exists, or 
		// after the last element if the click was on the last line and thus there is no 
		// element on the next line
		if($nextLineFirstElement == null){
			$bgeeDataSelection.insertAfter( $currentSpecies.nextAll().andSelf().last() );

		}
		else{
			$bgeeDataSelection.insertBefore( $nextLineFirstElement );
		}

		// Display the detail box and scroll to it
		var $container = $("html, body");
		$bgeeDataSelection.show( 120,function() {
			$container.animate({
				scrollTop: $bgeeDataSelection.offset().top - $container.offset().top 
				- (1.5 * $currentSpecies.height()) // Let a bit of space on the top to see
				// the selected figure
			},{duration:500}
			);			
		});
		
		// Update the URL with the id, to allow the link to be copied and sent
		// Add 'id' in front to avoid the automatic anchor behavior that would mess up the scroll
		window.location.hash = "#id"+id; 

	}

	/**
	 * This function highlights the elements that contain the text provided
	 * 
	 * @param text	The text to search, that should come from the search box
	 */
	function search(text) {
		// Remove the highlight class everywhere it could be.
		$species.removeClass("highlight");
		$species.parent().removeClass("highlight");
		if(text){
			// Add the class wherever it matches the text 
			$( "figcaption:containsIN('"+text.toLowerCase()+"')" ).each(function (){ 
				$( this ).parent().addClass("highlight");
				$( this ).parent().parent().addClass("highlight");
			});
		}
	};

	/**
	 * This function extends the jQuery contains function to make it case insensitive, it does
	 * seems to exist natively
	 * 
	 * @author http://jsfiddle.net/bipen/dyfRa/
	 * 
	 */
	$.extend($.expr[":"], {
		"containsIN": function(elem, i, match, array) {
			return (elem.textContent || elem.innerText || "").toLowerCase().indexOf((match[3] 
			|| "").toLowerCase()) >= 0;
		}
	});

	/**
	 * This function contains a table with all the download URL and return a subset 
	 * corresponding to the provided id
	 * 
	 * @param id	The id corresponding to the URLs to return
	 * @return		A subset of the whole URLs table corresponding to the provided id
	 */
	function getUrls(id){

		// Declaration of the table
		var urls = { 
				"9606":
				{
					"expr_simple_csv" : "http://www.isb-sib.ch/?1",
					"expr_simple_tsv" : "http://www.isb-sib.ch/?2",
					"expr_complete_csv" : "http://www.isb-sib.ch/?3",
					"expr_complete_tsv" : "http://www.isb-sib.ch/?4",
					"overunder_simple_csv" : "http://www.isb-sib.ch/?5",
					"overunder_simple_tsv" : "http://www.isb-sib.ch/?6",
					"overunder_complete_csv" : "http://www.isb-sib.ch/?7",
					"overunder_complete_tsv" : "http://www.isb-sib.ch/?8"
				},
				"10090":
				{
					"expr_simple_csv" : "http://www.isb-sib.ch/?9",
					"expr_simple_tsv" : "http://www.isb-sib.ch/?10",
					"expr_complete_csv" : "http://www.isb-sib.ch/?11",
					"expr_complete_tsv" : "http://www.isb-sib.ch/?12",
					"overunder_simple_csv" : "http://www.isb-sib.ch/?13",
					"overunder_simple_tsv" : "http://www.isb-sib.ch/?14",
					"overunder_complete_csv" : "http://www.isb-sib.ch/?15",
					"overunder_complete_tsv" : "http://www.isb-sib.ch/?16",
				}
		}

		// temp to avoid bug due to missing data, remove me !
		if(id != "10090"){
			id = "9606"
		}

		return urls[id];
	}

});