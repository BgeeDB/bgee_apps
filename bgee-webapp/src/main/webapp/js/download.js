/**
 * This code is loaded and run along with the download page to handle
 * the user"s actions and update the display.
 * It is run when the document is fully loaded by using the method ready()
 * 
 * @author Mathieu Seppey
 * @version Bgee 13, Jul 2014
 */
$( document ).ready(function() {

	// Fetch the top level container to play with scroll.
	var $container = $("html, body");
	// Fetch all the figure tag that represent the species or groups
	var $species = $( "figure" );
	// Fetch the detail box
	var $bgeeDataSelection = $( "#bgee_data_selection" );
	// Generate the correct id from the bgeespeciesid contained in the images
	var $bgeeDataSelectionCross = $( "#bgee_data_selection_cross" );

	// Declare a var that will contains the searchable datas and another for the autocompletion
	// list
	var searchContent = [];
	var autocompletionList = [];

	// Generate ids and search datas
	generateIdsAndSearchData();

	// Read the id in the hash of the URL and load the corresponding details if present
	var hash = window.location.hash;
	if(hash.slice(0, 3) == "#id"){
		var speciesId = hash.substr( 3 ); // Remove "id" before the real id that was 
		// added to avoid the automatic anchor behavior
	}
	if( speciesId ){								  
		var $currentSpecies = $( "#"+speciesId );
		if($currentSpecies.length > 0){
			loadDetails( $currentSpecies );
		}
	}

//	Add a click listener to every figures to load the corresponding details or to hide it
	$species.click(function() {
		if($( this ).hasClass( "selected" )){
			// Hide the detail box and unselected the current species.
			$( this ).removeClass("selected");
			$bgeeDataSelection.hide( "blind" );
			window.location.hash = ""; 
		}
		else{
			loadDetails($( this ));
			resetSearch(false);
		}
	});
//	Fetch the search elements in the DOM
	var $bgeeSearchForm = $( "#bgee_search_box form" );
	var $bgeeSearchBox = $( "#bgee_search_box input" );
	var $bgeeSearchResults = $( "#results_nb" );
	var $bgeeMoreResultsDown = $( "#bgee_more_results_down" );
	var $bgeeMoreResultsUp = $( "#bgee_more_results_up" );
//	Add a listener to several event to trigger the search
	$bgeeSearchBox.on( "input", function() {
		// Todo sortir ce code en une fonction
		search( $( this ).val() );
		// Hide the detail box and unselected the current species.
		$species.removeClass("selected");
		$bgeeDataSelection.hide( "blind" );
		window.location.hash = ""; 
	});
//	Block the submit action to avoid the page to be reloaded and display a flash effect 
//	when the enter key is pressed
	$bgeeSearchForm.submit(function() {
		$( "figure.highlight" ).fadeIn(100).fadeOut(100).fadeIn(100);
		$bgeeSearchResults.fadeIn(100).fadeOut(100).fadeIn(100);
		if(!($bgeeMoreResultsDown.css("display") == "none")){
			$bgeeMoreResultsDown.fadeIn(100).fadeOut(100).fadeIn(100);
		}
		return false;
	});

	$bgeeSearchBox.autocomplete({
		select: function( event, ui ) {
			$( this ).val( ui.item.value );
		},
		close: function( event, ui ) {
			// Todo sortir ce code en une fonction
			search( $( this ).val() );
			// Hide the detail box and unselected the current species.
			$species.removeClass("selected");
			$bgeeDataSelection.hide( "blind" );
			window.location.hash = ""; 
		},
		source: autocompletionList
	});

//	Add a listener to the scroll to evaluate whether the "more results" boxes should be displayed
	$( window ).scroll(function() {
		var position = $( window ).scrollTop();
		$bgeeMoreResultsUp.hide();
		$bgeeMoreResultsDown.hide();
		$(".highlight").each(function() {
			if(! $(this).visible(true,false,"vertical")){
				if($( this ).offset().top > position){
					$bgeeMoreResultsDown.show();
				} else if($( this ).offset().top < position){
					$bgeeMoreResultsUp.show();
				}
			}
		});		
	});

//	Add a listener to the cross to close the detail box
	$bgeeDataSelectionCross.click(function(){
		// Hide the detail box and unselected the current species.
		// TODO close method
		$( "figure.selected" ).removeClass("selected");
		$bgeeDataSelection.hide( "blind" );
		window.location.hash = ""; 		
	});

	var defaultText = $bgeeSearchBox.val();
	$bgeeSearchBox.click(function() {
		if($( this ).val() == defaultText){
			$( this ).val( "" );
		}
	});

	$( "#creativecommons_title a").click( function(){
		$( "#creativecommons" ).toggle( "blind" );
		if($( this ).text().indexOf("Show") > -1){
			$( this ).text($( this ).text().replace("Show","Hide"));
		}
		else{
			$( this ).text($( this ).text().replace("Hide","Show"));
		}
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
		var $bgeeDataSelectionTextScientific = $( "#bgee_data_selection_text h1.scientificname" );
		var $bgeeDataSelectionTextCommon = $( "#bgee_data_selection_text h1.commonname" );
		var $bgeeGroupDescription = $( "#bgee_data_selection_text p.groupdescription" );
		var $exprSimpleCsv = $( "#expr_simple_csv" );
		var $exprCompleteCsv = $( "#expr_complete_csv" );		
		var $overUnderSimpleCsv = $( "#overunder_simple_csv" );
		var $overUnderCompleteCsv = $( "#overunder_complete_csv" );		
		var numberOfSpecies = 0 ;
		var namesOfAllSpecies = "";
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
			// Assume that the image is a square, so height and width are the same
			$newElement.css("height",newHeight).css("width",newHeight);		
			numberOfSpecies++;
			namesOfAllSpecies = namesOfAllSpecies + $( this ).data( "bgeespeciesname" )
			+ ", ";
		});
		namesOfAllSpecies = namesOfAllSpecies.slice( 0, - 2 ); // Remove the extra  ,
		// if it is a group, use the group name as label, else the species name
		if(bgeeGroupName){
			$bgeeDataSelectionTextScientific.text("");
			$bgeeDataSelectionTextCommon.text(bgeeGroupName);
			$bgeeGroupDescription.text(numberOfSpecies + " species: ");
			$bgeeGroupDescription.append( $( "<i></i>" ).append( namesOfAllSpecies ) );
		}
		else {
			$bgeeDataSelectionTextScientific.text(bgeeSpeciesName);
			$bgeeDataSelectionTextCommon.text("("+bgeeSpeciesCommonNames+")");
			$bgeeGroupDescription.text("");
		}
		// Update the values of the download links 
		$exprSimpleCsv.attr("href",urls["expr_simple_csv"]);
		$exprCompleteCsv.attr("href",urls["expr_complete_csv"]);		
		$overUnderSimpleCsv.attr("href",urls["overunder_simple_csv"]);
		$overUnderCompleteCsv.attr("href",urls["overunder_complete_csv"]);		

		// Set the "selected" css class to the current species figure and display the detail 
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
		$bgeeDataSelection.show( 120,function() {
			$container.animate({
				scrollTop: $bgeeDataSelection.offset().top - $container.offset().top 
				- (1.5 * $currentSpecies.height()) // Let a bit of space on the top to see
				// the selected figure
			},{duration:500}
			);			
		});

		// Update the URL with the id, to allow the link to be copied and sent
		// Add "id" in front to avoid the automatic anchor behavior that would mess up the scroll
		window.location.hash = "#id"+id; 

	}

	/**
	 * This function highlights the elements that contain the text provided
	 * 
	 * @param text	The text to search, that should come from the search box
	 */
	function search(text) {
		// Remove the highlight class everywhere it could be and hide the more results box
		$species.removeClass("highlight");
		$bgeeMoreResultsDown.hide();
		if(text.length > 1){
			// Add the class wherever it matches the text 
			$species.each(function (){
				if(searchContent[$( this ).attr( "id" )].indexOf(text.toLowerCase()) > -1){
					$( this ).addClass("highlight");
				}
			});
			// Update the number of results
			$bgeeSearchResults.text($("figure.highlight").size()+ " result(s)");
		}
		else{
			resetSearch(true);
		}
		$(".highlight").each(function() {
			if(! $(this).visible(true,false,"vertical")){
				$bgeeMoreResultsDown.show();
			}
		}
		);
	};

	/**
	 * This function reset the search elements
	 * @param keepValue    A boolean to indicate whether the value has to be kept in the field
	 * 					   This is useful when one character is present in the input.
	 * 					   In this case, no result is provided and if the user is erasing, there
	 * 					   is the need to reset the search... but if the user is currently writing
	 * 					   this single letter has to be kept.
	 */
	function resetSearch(keepValue) {
		if(! keepValue){
			$bgeeSearchBox.val( "" );
		}
		$bgeeSearchResults.text( "" );
		$bgeeMoreResultsDown.hide();
		$bgeeMoreResultsUp.hide();		
		// Remove the highlight class everywhere it could be.
		$species.removeClass("highlight");
	}

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
					"expr_complete_csv" : "http://www.isb-sib.ch/?3",
					"overunder_simple_csv" : "http://www.isb-sib.ch/?5",
					"overunder_complete_csv" : "http://www.isb-sib.ch/?7",
				},
				"10090":
				{
					"expr_simple_csv" : "http://www.isb-sib.ch/?9",
					"expr_complete_csv" : "http://www.isb-sib.ch/?11",
					"overunder_simple_csv" : "http://www.isb-sib.ch/?13",
					"overunder_complete_csv" : "http://www.isb-sib.ch/?15",
				}
		}

		// temp to avoid bug due to missing data, remove me !
		if(id != "10090"){
			id = "9606"
		}

		return urls[id];
	}

//	TODO split in two functions
	function generateIdsAndSearchData() {
		// Generate ids
		$species.each(function() {
			var id = "";
			var names = "";
			var shortNames = "";
			var commonNames = "";
			var alternateNames = "";
			var groupName = $( this ).data("bgeegroupname");
			if(! groupName){
				groupName = "";
			}
			else{
				groupName = groupName.toLowerCase() + " ";
				addToAutoCompletionList(groupName);
			}

			$( this ).find( "img" ).each(function() {
				id = id + $( this ).data( "bgeespeciesid" ) + "_";
				var currentName = $( this ).data("bgeespeciesname").toLowerCase();
				if(currentName){
					names = names +  currentName + " ";
				}
				addToAutoCompletionList(currentName);
				var currentShortName = $( this ).data("bgeespeciesshortname").toLowerCase();
				if(currentShortName){
					shortNames = shortNames +  currentShortName + " ";
				}
				addToAutoCompletionList(currentShortName);
				var currentCommonName = $( this ).data("bgeespeciescommonname").toLowerCase();
				if(currentCommonName){
					commonNames = commonNames + currentCommonName + " ";
				}
				addToAutoCompletionList(currentCommonName);
				var currentAlternateNames = $( this ).data("bgeespeciesalternatenames").toLowerCase();
				if(currentAlternateNames){
					currentAlternateNames.split(", ").forEach(function(element){
						addToAutoCompletionList(element);
						alternateNames = alternateNames + element + " ";
					});
				}
			});
			id = id.slice( 0, - 1 ); // Remove the extra _
			$( this ).attr( "id", id );

			// Generate search content for the current species
			searchContent[id] = id.replace(/_/g, " ") + " "
			+ names
			+ shortNames
			+ commonNames
			+ groupName
			+ alternateNames ;

			autocompletionList.sort();

		});	

	}
	function addToAutoCompletionList(value){
		if(autocompletionList.indexOf(value) == -1){
			autocompletionList.push(value);
		}
	}

});
