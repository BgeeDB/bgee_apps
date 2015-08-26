/**
 * This code is loaded and run along with the general page to handle
 * the user's actions, to proceed to update the display.
 * It is run when the document is fully loaded by using the jQuery method ready()
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 13, Aug 2015
 */
// TODO: This file has been created to insert quickly species in the home page.
// 		 Almost code come from download.js. It should be deleted or replaced 
//       after the recoding of the home page
//Declaration of an object literal to contain the general page specific code.
//XXX: Should we let this code generate URL by using RequestParameters, or should all URLs 
//used to generate links provided server-side? (notably, documentation links)
var general = {
        // Declaration of the variables used throughout the general page and accessible
        // through the general object. Will be initialized by the init method.

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
        $geneExpressionCallsPageLink: null,
        
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
            // bgee_data_selection
            this.$bgeeDataSelection = $( "#bgee_data_selection" );
            this.$bgeeDataSelectionCross = $( "#bgee_data_selection_cross" );
            this.$bgeeDataSelectionImg = $( "#bgee_data_selection_img" );
            this.$bgeeDataSelectionTextScientific = 
                $( "#bgee_data_selection_text h1.scientificname" );
            this.$bgeeDataSelectionTextCommon = 
                $( "#bgee_data_selection_text h1.commonname" );
            this.$geneExpressionCallsPageLink = $( "#gene_expression_calls_link" );
            this.$procExprValuesPageLink = $( "#processed_expression_values_link" );
            // Creative commons
            this.$creativeCommonsTitleLink =  $( "#creativecommons_title a");
            this.$creativeCommons = $( "#creativecommons" );
            // Initialize the values that have to be dynamically set, i.e. ids  
            this.generateIds();

            // Add the event listeners to all elements that have a dynamic behavior

            // Add a click listener to every species/group to load the corresponding details 
            // or to hide it if already displayed
            this.$species.click(function() {
            	general.toggleDetail( $( this ) );
            });

            // Add a listener to the cross to close the detail box
            this.$bgeeDataSelectionCross.click(function(){
            	general.closeDetailBox();
            });

            // Add a listener to the link to show/hide the images copyright and change the text
            this.$creativeCommonsTitleLink.click( function(){
            	general.$creativeCommons.toggle( "blind" );
                if($( this ).text().indexOf( "Hide" ) > -1){
                    $( this ).text($( this ).text().replace( "Hide","Show" ));
                }
                else{
                    $( this ).text($( this ).text().replace( "Show","Hide" ));
                }
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
            }        
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
            // Generate value for the hash.
            // Add "id" in front to avoid the automatic anchor behavior 
            //that would mess up the scroll
            //TODO: this should be a proper data parameter stored in hash, 
            //like, e.g., species_id=id
            var hashToUse = "id"+id
            
            //manage link to gene expression calls from home page
            var requestGeneExprCallsPage = new requestParameters();
            requestGeneExprCallsPage.setURLHash(hashToUse);
            requestGeneExprCallsPage.addValue(urlParameters.getParamPage(), 
            		requestGeneExprCallsPage.PAGE_DOWNLOAD());
            requestGeneExprCallsPage.addValue(urlParameters.getParamAction(), 
            		requestGeneExprCallsPage.ACTION_DOWLOAD_CALL_FILES());
        	this.$geneExpressionCallsPageLink.attr( "href", 
        			//TODO: handle the hash exactly as another parameter (see TODO 
        			//in RequestParameters.java)
        			requestGeneExprCallsPage.getRequestURL());

            this.$procExprValuesPageLink = $( "#processed_expression_values_link" );
            //manage link to gene expression calls from home page
            var requestProcExprValuesPage = new requestParameters();
            requestProcExprValuesPage.setURLHash(hashToUse);
            requestProcExprValuesPage.addValue(urlParameters.getParamPage(), 
            		requestProcExprValuesPage.PAGE_DOWNLOAD());
            requestProcExprValuesPage.addValue(urlParameters.getParamAction(), 
            		requestProcExprValuesPage.ACTION_DOWLOAD_PROC_VALUE_FILES());
        	this.$procExprValuesPageLink.attr( "href", 
        			//TODO: handle the hash exactly as another parameter (see TODO 
        			//in RequestParameters.java)
        			requestProcExprValuesPage.getRequestURL());

            // The images contain the data fields related to the species
            var $images = $currentSpecies.find( ".species_img" ); 
            var bgeeSpeciesName = $images.data( "bgeespeciesname" ); // Only the last one is kept when 
            // there are multiple images, in the case of group, but the field is not used in this case,
            // so no need to care about 
            var bgeeSpeciesCommonNames = $images.data( "bgeespeciescommonname" );

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
                general.$bgeeDataSelectionImg.append( $newElement );
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

            this.$bgeeDataSelectionTextScientific.text( bgeeSpeciesName );
            this.$bgeeDataSelectionTextCommon.text( "("+ bgeeSpeciesCommonNames +")" );
            
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
            this.$bgeeDataSelection.show(500);

            // Update the URL with the id, to allow the link to be copied and sent
            // Add "id" in front to avoid the automatic anchor behavior that would mess up the scroll
            window.location.hash = '#' + hashToUse; 
        },

        /**
         * This function generates ids for species and groups based on the ids present in 
         * the data field in the images.
         */
        generateIds: function(){
            this.$species.each(function() {   
                var id = "";
                $( this ).find( ".species_img" ).each(function() {
                    id = id + $( this ).data( "bgeespeciesid" ) + "_";
                });
                id = id.slice( 0, - 1 ); // Remove the extra _ at the end.
                $( this ).attr( "id", id ); // set the attr id of the species/group
            });
        },

};
// Call the init function when the document is ready()
$( document ).ready( function(){ general.init() } );
