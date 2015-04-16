/**
 * This code is loaded and run along with the download page to handle
 * the user's actions, to proceed to the search and update the display.
 * It is run when the document is fully loaded by using the jQuery method ready()
 * 
 * @author Mathieu Seppey
 * @version Bgee 13, Jul 2014
 */
//Declaration of an object literal to contain the download page specific code.
var download = {
        // Declaration of the variables used throughout the download page and accessible
        // through the download object. Will be initialized by the init method.

        // Top level container, useful to play with the scroll height
        $container: null,
        // All the figure tag that represent the species and groups
        $species: null,
        // The detail box and its sub elements
        $bgeeDataSelection: null,
        $bgeeDataSelectionCross: null,
        $bgeeDataSelectionImg: null,
        $bgeeDataSelectionTextScientific: null,
        $bgeeDataSelectionTextCommon: null,
        $bgeeGroupDescription: null,
        $exprSimpleCsv: null,
        $exprCompleteCsv: null,
        $overUnderSimpleCsv: null,
        $overUnderCompleteCsv: null,
        // The search elements
        $bgeeSearchForm: null,
        $bgeeSearchBox: null,
        $bgeeSearchResults: null,
        $bgeeMoreResultsDown: null,
        $bgeeMoreResultsUp: null,
        initialText: "", // Initial text written in the search box when the page is loaded
        searchContent: [], // The var that contains the searchable content
        autoCompletionList: [], // The var that contains the auto completion values
        // The elements that are part of the image copyright in the bottom part
        $creativeCommonsTitleLink: null,
        $creativeCommons: null,
        // The value of the hash in the URL
        hash: "",

        /**
         * This function initializes all variables, DOM elements and listeners that have to be
         * loaded once the page is ready
         */
        init: function() {
            // Fetch all needed elements from the DOM
            this.$container = $( "html, body" );
            this.$species = $( "figure" );
            this.$bgeeDataSelection = $( "#bgee_data_selection" );
            this.$bgeeDataSelectionCross = $( "#bgee_data_selection_cross" );
            this.$bgeeDataSelectionImg = $( "#bgee_data_selection_img" );
            this.$bgeeDataSelectionTextScientific = 
                $( "#bgee_data_selection_text h1.scientificname" );
            this.$bgeeDataSelectionTextCommon = 
                $( "#bgee_data_selection_text h1.commonname" );
            this.$bgeeGroupDescription = $( "#bgee_data_selection_text p.groupdescription" );
            this.$exprSimpleCsv = $( "#expr_simple_csv" );
            this.$exprCompleteCsv = $( "#expr_complete_csv" );        
            this.$diffExprAnatomySimpleCsv = $( "#diffexpr_anatomy_simple_csv" );
            this.$diffExprAnatomyCompleteCsv = $( "#diffexpr_anatomy_complete_csv" );   
            this.$diffExprDevelopmentSimpleCsv = $( "#diffexpr_development_simple_csv" );
            this.$diffExprDevelopmentCompleteCsv = $( "#diffexpr_development_complete_csv" );
            this.$exprNoData = $( "#expr_no_data" );
            this.$diffExprAnatomyNoData = $( "#diffexpr_anatomy_no_data" );
            this.$diffExprDevelopmentNoData = $( "#diffexpr_development_no_data" );
            this.$exprComingSoon = $( "#expr_coming_soon" );
            this.$diffExprAnatomyComingSoon = $( "#diffexpr_anatomy_coming_soon" );
            this.$diffExprDevelopmentComingSoon = $( "#diffexpr_development_coming_soon" );
            this.$bgeeSearchForm = $( "#bgee_search_box form" );
            this.$bgeeSearchBox = $( "#bgee_search_box input" );
            this.$bgeeSearchResults = $( "#results_nb" );
            this.$bgeeMoreResultsDown = $( "#bgee_more_results_down" );
            this.$bgeeMoreResultsUp = $( "#bgee_more_results_up" );
            this.$creativeCommonsTitleLink =  $( "#creativecommons_title a");
            this.$creativeCommons = $( "#creativecommons" );
            // Initialize the values that have to be dynamically set, i.e. ids and search content  
            this.generateIds();
            this.generateSearchableContent();
            // Add the event listeners to all elements that have a dynamic behavior

            // Add a click listener to every species/group to load the corresponding details 
            // or to hide it if already displayed
            this.$species.click(function() {
                download.toggleDetail( $( this ) );
            });
            // Add a listener to the search input to trigger the search process
            this.$bgeeSearchBox.on( "input",function() {
                download.doSearch( $( this ).val() );
            });
            // Add a listener to the submit action of the search form, to block the action and
            // avoid the page to be reloaded. Also display a flash effect on several elements
            // to notify the user where the results are.
            this.$bgeeSearchForm.submit(function() {
                $( "figure.highlight" ).fadeIn( 100 ).fadeOut( 100 ).fadeIn( 100 );
                download.$bgeeSearchResults.fadeIn( 100 ).fadeOut( 100 ).fadeIn( 100 );
                if( ! ( download.$bgeeMoreResultsDown.css( "display" ) == "none" ) ){
                    download.$bgeeMoreResultsDown.fadeIn( 100 ).fadeOut( 100 ).fadeIn( 100 );
                }
                return false; // Block the submit process
            });
            // Add a listener to the scroll to evaluate whether the "more results" boxes
            $( window ).scroll(function() {                       // should be displayed
                var position = $( window ).scrollTop();
                download.$bgeeMoreResultsUp.hide();
                download.$bgeeMoreResultsDown.hide();
                $( ".highlight" ).each(function() {
                    if(! $( this ).visible( true,false,"vertical" ) ){ // the first true asks for 
                        if( $( this ).offset().top > position ){       // completely hidden elements
                            download.$bgeeMoreResultsDown.show();
                        } else if( $( this ).offset().top < position ){
                            download.$bgeeMoreResultsUp.show();
                        }
                    }
                });
            });
            // Add a listener to the cross to close the detail box
            this.$bgeeDataSelectionCross.click(function(){
                download.closeDetailBox();
            });
            // Add a listener to the search box to remove the initial displayed text if present
            // First, keep the initial text in a var for later use
            this.initialText = this.$bgeeSearchBox.val();
            this.$bgeeSearchBox.click(function() {
                if($( this ).val() == download.initialText){
                    $( this ).val( "" );
                }
            });
            // Add a listener to the link to show/hide the images copyright and change the text
            this.$creativeCommonsTitleLink.click( function(){
                download.$creativeCommons.toggle( "blind" );
                if($( this ).text().indexOf( "Hide" ) > -1){
                    $( this ).text($( this ).text().replace( "Hide","Show" ));
                }
                else{
                    $( this ).text($( this ).text().replace( "Show","Hide" ));
                }
            });
            // Add the autocompletion to the search box.
            this.$bgeeSearchBox.autocomplete({
                select: function( event, ui ) {
                    $( this ).val( ui.item.value );
                },
                close: function( event, ui ) {
                    download.search( $( this ).val() );
                    download.closeDetailBox();
                },
                source: download.autoCompletionList
            });
            // Load the initial species if passed in the URL
            // Read the id in the this.hash of the URL and load the corresponding details if present
            this.hash = window.location.hash;
            if( this.hash.slice( 0, 3 ) == "#id" ){
                var speciesId = this.hash.substr( 3 ); // Remove "id" before the real id that was 
                // added to avoid the automatic anchor behavior
            }
            if( speciesId ){                                  
                var $currentSpecies = $( "#"+speciesId );
                if( $currentSpecies.length > 0 ){
                    this.loadDetails( $currentSpecies );
                }
            }
        },

        /**
         * This function show/hide the detail box for the provided species
         * @param $species  The DOM element that represents the species/group to show/hide
         */
        toggleDetail: function( $species ){
            if($species.hasClass( "selected" )){
                this.closeDetailBox(); 
            }
            else{
                this.loadDetails( $species );
                this.resetSearch( false );
            }        
        },

        /**
         * This function search the provided value and update the display
         * @param value    The string to search
         */
        doSearch: function( value ){
            this.search( value );
            this.closeDetailBox();
        },

        /**
         * This function close the detail box and update the display
         */
        closeDetailBox: function(){
            this.$species.removeClass( "selected" );
            this.$bgeeDataSelection.hide( "blind" );
            //we don't put just an empty hash, otherwise it jumps to the top 
            //of the document
            window.location.hash = "!";   
        },

        /**
         * This function update and display the detail box for the species or group provided
         * 
         * @param $currentSpecies    The DOM element <figure> that contains the species or group 
         *                           to be loaded
         */
        loadDetails: function( $currentSpecies ){

            // Fetch all DOM elements and values needed to update the display
            var id = $currentSpecies.attr( "id" );
            // The images contain the data fields related to the species
            var $images = $currentSpecies.find( "img" ); 
            var bgeeSpeciesName = $images.data( "bgeespeciesname" ); // Only the last one is kept when 
            // there are multiple images, in the case of group, but the field is not used in this case,
            // so no need to care about 
            var bgeeSpeciesCommonNames = $images.data( "bgeespeciescommonname" );
            var bgeeGroupName = $currentSpecies.data( "bgeegroupname" );
            // Get files urls for the current species/group
            var bgeeExprSimpleFileUrl = $currentSpecies.data( "bgeeexprsimplefileurl" );
            var bgeeExprCompleteFileUrl = $currentSpecies.data( "bgeeexprcompletefileurl" );
            var bgeeDiffExprAnatomySimpleFileUrl =
            	$currentSpecies.data( "bgeediffexpranatomysimplefileurl" );
            var bgeeDiffExprAnatomyCompleteFileUrl =
            	$currentSpecies.data( "bgeediffexpranatomycompletefileurl" );
            var bgeeDiffExprDevelopmentSimpleFileUrl =
            	$currentSpecies.data( "bgeediffexprdevelopmentsimplefileurl" );
            var bgeeDiffExprDevelopmentCompleteFileUrl = 
            	$currentSpecies.data( "bgeediffexprdevelopmentcompletefileurl" );
            var bgeeExprSimpleFileSize = $currentSpecies.data( "bgeeexprsimplefilesize" );
            var bgeeExprCompleteFileSize = $currentSpecies.data( "bgeeexprcompletefilesize" );
            var bgeeDiffExprAnatomySimpleFileSize =
            	$currentSpecies.data( "bgeediffexpranatomysimplefilesize" );
            var bgeeDiffExprAnatomyCompleteFileSize = 
            	$currentSpecies.data( "bgeediffexpranatomycompletefilesize" );
            var bgeeDiffExprDevelopmentSimpleFileSize =
            	$currentSpecies.data( "bgeediffexprdevelopmentsimplefilesize" );
            var bgeeDiffExprDevelopmentCompleteFileSize =
            	$currentSpecies.data( "bgeediffexprdevelopmentcompletefilesize" );

            // Proceed to the update
            var numberOfSpecies = $images.size() ;
            var namesOfAllSpecies = "";
            this.$bgeeDataSelectionImg.empty();
            $images.each( function(){
                // For each image, clone its DOM structure into the detail box, update its src
                // to use the high resolution image and update its height and width depending on
                // the number of images to display at the same time ( i.e. more than one for a group )
                var $newElement = $( this ).clone();
                var newUrl = $newElement.attr( "src" ).replace( "_light" , "" );
                $newElement.attr( "src" , newUrl );
                download.$bgeeDataSelectionImg.append( $newElement );
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
                this.$bgeeDataSelectionTextScientific.text( "" );
                this.$bgeeDataSelectionTextCommon.text( bgeeGroupName );
                this.$bgeeGroupDescription.text( numberOfSpecies + " species: " );
                this.$bgeeGroupDescription.append( $( "<i></i>" ).append( namesOfAllSpecies ) );
            } else {
                this.$bgeeDataSelectionTextScientific.text( bgeeSpeciesName );
                this.$bgeeDataSelectionTextCommon.text( "("+ bgeeSpeciesCommonNames +")" );
                this.$bgeeGroupDescription.text( "" );
            }
            
            // Update the values of the download links and size files
            // Expression files
            if (bgeeExprSimpleFileUrl === undefined) {
            	this.$exprSimpleCsv.hide();
            	this.$exprCompleteCsv.hide();
            	//TODO remove when multi-species expression files are computed
            	if( bgeeGroupName ){
            		this.$exprNoData.hide();
            		this.$exprComingSoon.show();
            	} else {
            		this.$exprNoData.show();
            		this.$exprComingSoon.hide();
            	}
            } else {
            	this.$exprSimpleCsv.show();
            	this.$exprCompleteCsv.show();
            	this.$exprSimpleCsv.attr( "href", bgeeExprSimpleFileUrl );
            	this.$exprSimpleCsv.text( "Download simple file (" + bgeeExprSimpleFileSize + ")" );
            	this.$exprCompleteCsv.attr( "href", bgeeExprCompleteFileUrl );
            	this.$exprCompleteCsv.text( "Download complete file (" + bgeeExprCompleteFileSize + ")" );
            	this.$exprNoData.hide();
            	this.$exprComingSoon.hide();
            }

            // Differential expression - anatomy comparison
            if (bgeeDiffExprAnatomySimpleFileUrl === undefined) {
            	this.$diffExprAnatomySimpleCsv.hide();
            	this.$diffExprAnatomyCompleteCsv.hide();
            	this.$diffExprAnatomyNoData.show();
            	this.$diffExprAnatomyComingSoon.hide();
            } else {
            	this.$diffExprAnatomySimpleCsv.show();
            	this.$diffExprAnatomyCompleteCsv.show();
            	this.$diffExprAnatomyNoData.hide();
            	this.$diffExprAnatomyComingSoon.hide();
            	this.$diffExprAnatomySimpleCsv.attr( "href", bgeeDiffExprAnatomySimpleFileUrl );
            	this.$diffExprAnatomyCompleteCsv.attr( "href", bgeeDiffExprAnatomyCompleteFileUrl );
            	this.$diffExprAnatomySimpleCsv.text( 
            			"Download simple file (" + bgeeDiffExprAnatomySimpleFileSize + ")" );
            	this.$diffExprAnatomyCompleteCsv.text( 
            			"Download complete file (" + bgeeDiffExprAnatomyCompleteFileSize + ")" );
            }

            // Differential expression - development comparison
            if( bgeeDiffExprDevelopmentSimpleFileUrl === undefined ) {
            	this.$diffExprDevelopmentSimpleCsv.hide();
            	this.$diffExprDevelopmentCompleteCsv.hide();
            	//TODO remove when multi-species developmental diff. expression files are computed
                if( bgeeGroupName ){
                	this.$diffExprDevelopmentNoData.hide();
                	this.$diffExprDevelopmentComingSoon.show();
                } else {
                	this.$diffExprDevelopmentNoData.show();
                	this.$diffExprDevelopmentComingSoon.hide();
                }
            } else {
            	this.$diffExprDevelopmentSimpleCsv.show();
            	this.$diffExprDevelopmentCompleteCsv.show();
            	this.$diffExprDevelopmentNoData.hide();
            	this.$diffExprDevelopmentComingSoon.hide();
            	this.$diffExprDevelopmentSimpleCsv.attr( "href", bgeeDiffExprDevelopmentSimpleFileUrl );
            	this.$diffExprDevelopmentCompleteCsv.attr( "href", bgeeDiffExprDevelopmentCompleteFileUrl );
            	this.$diffExprDevelopmentSimpleCsv.text( 
            			"Download simple file (" + bgeeDiffExprDevelopmentSimpleFileSize + ")" );
            	this.$diffExprDevelopmentCompleteCsv.text( 
            			"Download complete file (" + bgeeDiffExprDevelopmentCompleteFileSize + ")" );
            }

            // Add the "selected" css class to the current species and display the detail 
            // box with a visual effect
            this.$species.removeClass( "selected" ); // Unselected all other species
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
                this.$bgeeDataSelection.insertAfter( $currentSpecies.nextAll().andSelf().last() );
            }
            else{
                this.$bgeeDataSelection.insertBefore( $nextLineFirstElement );
            }

            // Display the detail box and scroll to it
            this.$bgeeDataSelection.show( 120,function() {
                download.$container.animate({
                    scrollTop: download.$bgeeDataSelection.offset().top
                    - download.$container.offset().top 
                    - (1.5 * $currentSpecies.height()) // Let a bit of space on the top to see
                    // the selected figure
                },{duration:500}
                );
            });

            // Update the URL with the id, to allow the link to be copied and sent
            // Add "id" in front to avoid the automatic anchor behavior that would mess up the scroll
            window.location.hash = "#id"+id; 
        },

        /**
         * This function highlights the elements that contain the text provided
         * 
         * @param text    The text to search, that should come from the search box
         */
        search: function( text ) {
            // Remove the highlight class everywhere it is. Hide the "more results" box
            this.$species.removeClass( "highlight" );
            this.$bgeeMoreResultsDown.hide();
            if( text.length > 1 ){
                // Add the class on all species where the text match the searchContent 
                this.$species.each(function (){
                    if( download.searchContent[$( this ).attr( "id" )]
                    .indexOf( text.toLowerCase() ) > -1){
                        $( this ).addClass( "highlight" );
                    }
                });
                // Update the number of results
                this.$bgeeSearchResults.text( $( "figure.highlight" ).size()+ " result(s)" );
            }
            else{
                this.resetSearch( true ); // If the text is blank or one char, reset the search
            }
            $( ".highlight" ).each(function() { // Check whether all result are visible
                if(! $( this ).visible( true,false,"vertical" ) ){
                    download.$bgeeMoreResultsDown.show(); // If not, display the "more result" 
                }                                 // box on the bottom of the page
            }
            );
        },

        /**
         * This function reset the search elements
         * @param keepValue    A boolean to indicate whether the value has to be kept in the field
         *                     This is useful when the user is erasing or writing text.. the search
         *                     has to be reset, but the user's entry has to be kept.
         */
        resetSearch: function( keepValue ) {
            if( ! keepValue ){
                this.$bgeeSearchBox.val( this.initialText );
            }
            this.$bgeeSearchResults.text( "" );
            this.$bgeeMoreResultsDown.hide();
            this.$bgeeMoreResultsUp.hide();        
            // Remove the highlight class everywhere it could be.
            this.$species.removeClass( "highlight" );
        },

        /**
         * This function generates ids for species and groups based on the ids present in 
         * the data field in the images.
         */
        generateIds: function(){
            this.$species.each(function() {   
                var id = "";
                $( this ).find( "img" ).each(function() {
                    id = id + $( this ).data( "bgeespeciesid" ) + "_";
                });
                id = id.slice( 0, - 1 ); // Remove the extra _ at the end.
                $( this ).attr( "id", id ); // set the attr id of the species/group
            });
        },

        /**
         * This function fetches the words that can be searched and put it in an associative array
         * where the id of the species/group corresponds to its searchable content.
         * It also fill a list with all possible words that can be proposed to the user during
         * a search (autocompletion)
         */
        generateSearchableContent: function(){
            // Proceed for all species or group...
            this.$species.each(function() { 
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
                    download.addToAutoCompletionList( groupName ); // Add the value to the
                }                                                  // auto completion list
                // Then, fetch the values of the data fields contained in images
                $( this ).find( "img" ).each(function() {
                    var currentName = $( this ).data( "bgeespeciesname" ).toLowerCase();
                    if( currentName ){
                        names = names +  currentName + " "; // Concat the name of all images, 
                    }                                       // several values only in the case of group
                    download.addToAutoCompletionList( currentName ); // Add to the autocompl. list
                    var currentShortName = $( this ).data( "bgeespeciesshortname" ).toLowerCase();
                    if( currentShortName ){
                        shortNames = shortNames +  currentShortName + " "; // Same for short name
                    }
                    download.addToAutoCompletionList( currentShortName );
                    var currentCommonName = $( this ).data( "bgeespeciescommonname" ).toLowerCase();
                    if( currentCommonName ){
                        commonNames = commonNames + currentCommonName + " "; // Same for common name
                    }
                    download.addToAutoCompletionList( currentCommonName );
                    var currentAlternateNames = 
                        $( this ).data( "bgeespeciesalternatenames" ).toLowerCase();
                    if( currentAlternateNames ){
                        // Same for alternate name, except there are several alternate names in one 
                        // image, separated by comma, => split the values first.
                        currentAlternateNames.split( ", " ).forEach(function( element ) {
                            alternateNames = alternateNames + element + " ";
                            download.addToAutoCompletionList( element );
                        });
                    }
                });

                // Sort the autocompletion list alphabetically
                download.autoCompletionList.sort();

                // Generate search content for the current species by putting all fields together
                // in the array with the id as key
                var id = $( this ).attr( "id" );
                download.searchContent[id] = id.replace(/_/g, " ") + " " // remove "_" that separate                                                 
                + names                                                  // species ids to form the group id,
                + shortNames                                             // to be able to search on the 
                + commonNames                                            // individual id of a species
                + groupName                                              // but not on the group id that 
                + alternateNames ;                                       // has no biological significance.

            });    

        },

        /**
         * This function add the provided value to the autocompletion list if not blank
         * @param value        The value to add to the autocompletion list
         */
        addToAutoCompletionList: function( value ){
            if( this.autoCompletionList.indexOf( value ) == -1 ){
                this.autoCompletionList.push( value );
            }
        }
};
// Call the init function when the document is ready()
$( document ).ready( function(){ download.init() } );
