/**
 * This code is loaded and run along with the download page to handle
 * the user's actions, to proceed to the search and update the display.
 * It is run when the document is fully loaded by using the method ready()
 * 
 * @author Mathieu Seppey
 * @version Bgee 13, Jul 2014
 */
$( document ).ready(function() {
	
	/* Code executed when the page is ready */

	/* Fetch all needed elements from the DOM that are never changed by the user's actions */

	// Top level container, useful to play with the scroll height
	var $container = $( "html, body" );
	// Fetch all the figure tag that represent the species and groups
	var $species = $( "figure" );
	// Fetch the detail box and its sub elements
	var $bgeeDataSelection = $( "#bgee_data_selection" );
	var $bgeeDataSelectionCross = $( "#bgee_data_selection_cross" );
	var $bgeeDataSelectionImg = $( "#bgee_data_selection_img" );
	var $bgeeDataSelectionTextScientific = $( "#bgee_data_selection_text h1.scientificname" );
	var $bgeeDataSelectionTextCommon = $( "#bgee_data_selection_text h1.commonname" );
	var $bgeeGroupDescription = $( "#bgee_data_selection_text p.groupdescription" );
	var $exprSimpleCsv = $( "#expr_simple_csv" );
	var $exprCompleteCsv = $( "#expr_complete_csv" );        
	var $overUnderSimpleCsv = $( "#overunder_simple_csv" );
	var $overUnderCompleteCsv = $( "#overunder_complete_csv" );   
	//    Fetch the search elements
	var $bgeeSearchForm = $( "#bgee_search_box form" );
	var $bgeeSearchBox = $( "#bgee_search_box input" );
	var $bgeeSearchResults = $( "#results_nb" );
	var $bgeeMoreResultsDown = $( "#bgee_more_results_down" );
	var $bgeeMoreResultsUp = $( "#bgee_more_results_up" );
	// Fetch the elements that are part of the image copyright in the bottom part
	var $creativeCommonsTitleLink =  $( "#creativecommons_title a");
	var $creativeCommons = $( "#creativecommons" );

	/* Initialize the values that have to be dynamically set, i.e. ids and search data */

	generateIds();
	var searchContent = []; // Initialize the var that contains the searchable content
	var autocompletionList = []; // Initialize the var tha contains the auto completion values
	generateSearchableContent();

	/* Add the event listeners to all elements that have a dynamic behavior */

	// Add a click listener to every species/group to load the corresponding details 
	// or to hide it if already displayed
	$species.click(function() {
		toggleDetail( $( this ) );
	});
	// Add a listener to the search input to trigger the search process
	$bgeeSearchBox.on( "input",function() {
		doSearch( $( this ).val() );
	});

	// Add a listener to the submit action of the search form, to block the action and avoid 
	// the page to be reloaded. Also display a flash effect on several elements
	// to notify the user where the results are.
	$bgeeSearchForm.submit(function() {
		$( "figure.highlight" ).fadeIn( 100 ).fadeOut( 100 ).fadeIn( 100 );
		$bgeeSearchResults.fadeIn( 100 ).fadeOut( 100 ).fadeIn( 100 );
		if( ! ( $bgeeMoreResultsDown.css( "display" ) == "none" ) ){
			$bgeeMoreResultsDown.fadeIn( 100 ).fadeOut( 100 ).fadeIn( 100 );
		}
		return false; // Block the submit process
	});

	// Add a listener to the scroll to evaluate whether the "more results" boxes should be displayed
	$( window ).scroll(function() {
		var position = $( window ).scrollTop();
		$bgeeMoreResultsUp.hide();
		$bgeeMoreResultsDown.hide();
		$( ".highlight" ).each(function() {
			if(! $( this ).visible( true,false,"vertical" ) ){ // the first true is for completely hidden
				if( $( this ).offset().top > position ){
					$bgeeMoreResultsDown.show();
				} else if( $( this ).offset().top < position ){
					$bgeeMoreResultsUp.show();
				}
			}
		});        
	});

	// Add a listener to the cross to close the detail box
	$bgeeDataSelectionCross.click(function(){
		closeDetailBox();
	});

	// Add a listener to the search box to remove the initial displayed text if present
	// First, keep the initial text in a var for later use
	var initialText = $bgeeSearchBox.val();
	$bgeeSearchBox.click(function() {
		if($( this ).val() == initialText){
			$( this ).val( "" );
		}
	});

	// Add a listener to the link to show/hide the images copyright and change the text
	$creativeCommonsTitleLink.click( function(){
		$creativeCommons.toggle( "blind" );
		if($( this ).text().indexOf( "Hide" ) > -1){
			$( this ).text($( this ).text().replace( "Hide","Show" ));
		}
		else{
			$( this ).text($( this ).text().replace( "Show","Hide" ));
		}
	});

	// Add the autocompletion to the search box.
	$bgeeSearchBox.autocomplete({
		select: function( event, ui ) {
			$( this ).val( ui.item.value );
		},
		close: function( event, ui ) {
			search( $( this ).val() );
			closeDetailBox();
		},
		source: autocompletionList
	});

	/* Load the initial species if passed in the URL */

	// Read the id in the hash of the URL and load the corresponding details if present
	var hash = window.location.hash;
	if( hash.slice( 0, 3 ) == "#id" ){
		var speciesId = hash.substr( 3 ); // Remove "id" before the real id that was 
		// added to avoid the automatic anchor behavior
	}
	if( speciesId ){                                  
		var $currentSpecies = $( "#"+speciesId );
		if( $currentSpecies.length > 0 ){
			loadDetails( $currentSpecies );
		}
	}
	
	/* End of the code executed when the page is ready */

	/**
	 * This function show/hide the detail box for the provided species
	 * @param $species    The DOM element that represents the species/group to show/hide
	 */
	function toggleDetail( $species ){
		if($species.hasClass( "selected" )){
			closeDetailBox(); 
		}
		else{
			loadDetails( $species );
			resetSearch( false );
		}        
	}

	/**
	 * This function search the provided value and update the display
	 * @param value        The string to search
	 */
	function doSearch( value ){
		search( value );
		closeDetailBox();
	}

	/**
	 * This function close the detail box and update the display
	 */
	function closeDetailBox(){
		$species.removeClass( "selected" );
		$bgeeDataSelection.hide( "blind" );
		window.location.hash = "";   
	}

	/**
	 * This function update and display the detail box for the species or group provided
	 * 
	 * @param $currentSpecies    The DOM element <figure> that contains the species or group 
	 *                             to be loaded
	 */
	function loadDetails( $currentSpecies ){

		// Fetch all DOM elements and values needed to update the display
		var id = $currentSpecies.attr( "id" );
		// The images contain the data fields related to the species
		var $images = $currentSpecies.find( "img" ); 
		var bgeeSpeciesName = $images.data( "bgeespeciesname" ); // Only the last one is kept when 
		// there are multiple images, in the case of group, but the field is not used in this case,
		// so no need to care about this.
		var bgeeSpeciesCommonNames = $images.data( "bgeespeciescommonname" );
		var bgeeGroupName = $currentSpecies.data( "bgeegroupname" );
		// Load the files urls for the current species/group
		var urls = getUrls( id );     
		// Proceed to the update
		var numberOfSpecies = $images.size() ;
		var namesOfAllSpecies = "";
		$bgeeDataSelectionImg.empty();
		$images.each( function(){
			// For each image, clone its DOM structure into the detail box, update its src
			// to use the high resolution image and update its height and width depending on
			// the number of images to display at the same time ( i.e. more than one for a group )
			var $newElement = $( this ).clone();
			var newUrl = $newElement.attr( "src" ).replace( "_light" , "" );
			$newElement.attr( "src" , newUrl );
			$bgeeDataSelectionImg.append( $newElement );
			// Calculate the height so it would allow the images to fit into the space.
			// Divide the height by 1 for 1 image, by 2 for 2,3,4 img, by 3 for 5,6,7,8,9 and etc.
			var newHeight = $newElement.height() / ( Math.ceil( Math.sqrt( numberOfSpecies ) ) ); 
			// Assume that the image is a square, so height and width are the same
			$newElement.css( "height" , newHeight ).css( "width" , newHeight ); 
			// Add the species short name to the name of all species field
			namesOfAllSpecies = namesOfAllSpecies + $( this ).data( "bgeespeciesshortname" )
			+ ", ";
		});
		namesOfAllSpecies = namesOfAllSpecies.slice( 0, - 2 ); // Remove the extra ' ,'
		// if it is a group, use the group name as label, else the species name
		if( bgeeGroupName ){
			$bgeeDataSelectionTextScientific.text( "" );
			$bgeeDataSelectionTextCommon.text( bgeeGroupName );
			$bgeeGroupDescription.text( numberOfSpecies + " species: " );
			$bgeeGroupDescription.append( $( "<i></i>" ).append( namesOfAllSpecies ) );
		}
		else {
			$bgeeDataSelectionTextScientific.text( bgeeSpeciesName );
			$bgeeDataSelectionTextCommon.text( "("+ bgeeSpeciesCommonNames +")" );
			$bgeeGroupDescription.text( "" );
		}
		// Update the values of the download links 
		$exprSimpleCsv.attr( "href", urls["expr_simple_csv"] );
		$exprCompleteCsv.attr( "href", urls["expr_complete_csv"] );        
		$overUnderSimpleCsv.attr( "href", urls["overunder_simple_csv"] );
		$overUnderCompleteCsv.attr( "href", urls["overunder_complete_csv"] );        

		// Add the "selected" css class to the current species and display the detail 
		// box with a visual effect
		$species.removeClass( "selected" ); // Unselected all other species
		$currentSpecies.addClass( "selected" );
		// Calculate before which element the detail box has to be placed, depending on its
		// vertical position
		var yPos = $currentSpecies.position().top;
		var $nextLineFirstElement = null; // Will contain the element before which the detail 
		// box will be moved
		$currentSpecies.nextAll().each(function() {
			// Find the first element that has a yPos different from the element the user clicked on
			if( $( this ).position().top > yPos ){
				$nextLineFirstElement = $( this );
				return false; // Exit from the each loop when found
			}
		});

		// Move the detail box, before the next line first element if it was found, else 
		// after the last element ( the click was on the last line and thus there is no 
		// element on the next line )
		if( $nextLineFirstElement == null ){
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
	 * @param text    The text to search, that should come from the search box
	 */
	function search( text ) {
		// Remove the highlight class everywhere it is. Hide the "more results" box
		$species.removeClass( "highlight" );
		$bgeeMoreResultsDown.hide();
		if( text.length > 1 ){
			// Add the class on all species where the text match the searchContent 
			$species.each(function (){
				if( searchContent[$( this ).attr( "id" )].indexOf( text.toLowerCase() ) > -1){
					$( this ).addClass( "highlight" );
				}
			});
			// Update the number of results
			$bgeeSearchResults.text( $( "figure.highlight" ).size()+ " result(s)" );
		}
		else{
			resetSearch( true ); // If the text is blank or one char, reset the search
		}
		$( ".highlight" ).each(function() { // Check whether all result are visible
			if(! $( this ).visible( true,false,"vertical" ) ){
				$bgeeMoreResultsDown.show(); // If not, display the "more result" box on the bottom
			}                                 // of the page
		}
		);
	};

	/**
	 * This function reset the search elements
	 * @param keepValue    A boolean to indicate whether the value has to be kept in the field
	 *                     This is useful when the user is erasing or writing text.. the search
	 *                     has to be reset, but the user's entry has to be kept.
	 */
	function resetSearch( keepValue ) {
		if( ! keepValue ){
			$bgeeSearchBox.val( initialText );
		}
		$bgeeSearchResults.text( "" );
		$bgeeMoreResultsDown.hide();
		$bgeeMoreResultsUp.hide();        
		// Remove the highlight class everywhere it could be.
		$species.removeClass( "highlight" );
	}

	/**
	 * This function contains a table with all the download URL and return a subset 
	 * corresponding to the provided id
	 * 
	 * @param id    The id corresponding to the URLs to return
	 * @return        A subset of the whole URLs table corresponding to the provided id
	 */
	function getUrls( id ){

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

		// TODO, temp to avoid bug due to missing data, remove me !
		if( id != "10090" ){
			id = "9606"
		}

		return urls[id];
	}


	/**
	 * This function generates ids for species and groups based on the ids present in 
	 * the data field in the images.
	 */
	function generateIds(){
		$species.each(function() {   
			var id = "";
			$( this ).find( "img" ).each(function() {
				id = id + $( this ).data( "bgeespeciesid" ) + "_";
			});
			id = id.slice( 0, - 1 ); // Remove the extra _ at the end.
			$( this ).attr( "id", id ); // set the attr id of the species/group
		});
	}

	/**
	 * This function fetches the words that can be searched and put it in an associative array
	 * where the id of the species/group corresponds to its searchable content.
	 * It also fill a list with all possible words that can be proposed to the user during
	 * a search (autocompletion)
	 */
	function generateSearchableContent(){
		// Proceed for all species or group...
		$species.each(function() { 
			// Initialize empty strings for every kind of data that are searchable
			var names = "";
			var shortNames = "";
			var commonNames = "";
			var alternateNames = "";
			var groupName = $( this ).data( "bgeegroupname" ); // First, fetch the group name
			if( ! groupName ){
				groupName = ""; // To avoid "null" or "undefined" as searchable strings
			}
			else{
				groupName = groupName.toLowerCase() + " "; // Add the value to the searchable
				// content, in lower case
				addToAutoCompletionList( groupName ); // Add the value to the auto completion list
			}
			// Then, fetch the values of the data fields contained in images
			$( this ).find( "img" ).each(function() {
				var currentName = $( this ).data( "bgeespeciesname" ).toLowerCase();
				if( currentName ){
					names = names +  currentName + " "; // Concat the name of all images, 
				}                                       // several values only in the case of group
				addToAutoCompletionList( currentName ); // Add the name to the autocompl. list
				var currentShortName = $( this ).data( "bgeespeciesshortname" ).toLowerCase();
				if( currentShortName ){
					shortNames = shortNames +  currentShortName + " "; // Same for short name
				}
				addToAutoCompletionList( currentShortName );
				var currentCommonName = $( this ).data( "bgeespeciescommonname" ).toLowerCase();
				if( currentCommonName ){
					commonNames = commonNames + currentCommonName + " "; // Same for common name
				}
				addToAutoCompletionList( currentCommonName );
				var currentAlternateNames = $( this ).data( "bgeespeciesalternatenames" ).toLowerCase();
				if( currentAlternateNames ){
					// Same for alternate name, except there are several alternate names in one 
					// image, separated by comma, => split the values first.
					currentAlternateNames.split( ", " ).forEach(function( element ) {
						alternateNames = alternateNames + element + " ";
						addToAutoCompletionList( element );
					});
				}
			});

			// Sort the autocompletion list alphabetically
			autocompletionList.sort();

			// Generate search content for the current species by putting all fields together
			// in the array with the id as key
			var id = $( this ).attr( "id" );
			searchContent[id] = id.replace(/_/g, " ") + " " // remove the "_" that separate                                                 
			+ names                                         // species ids to form the group id,
			+ shortNames                                    // to be able to search on the 
			+ commonNames                                   // individual id of a species
			+ groupName                                     // but not on the group id that 
			+ alternateNames ;                              // has no biological significance.

		});    

	}

	/**
	 * This function add the provided value to the autocompletion list if not blank
	 * @param value        The value to add to the autocompletion list
	 */
	function addToAutoCompletionList( value ){
		if( autocompletionList.indexOf( value ) == -1 ){
			autocompletionList.push( value );
		}
	}

});
