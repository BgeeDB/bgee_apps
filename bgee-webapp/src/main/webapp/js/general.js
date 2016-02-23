/**
 * This code is loaded and run along with the general page to handle
 * the user's actions, to proceed to update the display.
 * It is run when the document is fully loaded by using the jQuery method ready()
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 13, Aug 2015
 */
//Declaration of an object literal to contain the general page specific code.
//XXX: Should we let this code generate URL by using RequestParameters, or should all URLs 
//used to generate links provided server-side? (notably, documentation links)
var general = {
        // Declaration of the variables used throughout the general page and accessible
        // through the general object. Will be initialized by the init method.

        // Top level container, useful to play with the scroll height
        $container: null,
        
        // Species images
        $speciesImg: null,
        $speciesImgWidth: null,

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
            
            // Species images
            this.$speciesImg = $( ".species_img" );
            this.$speciesImgWidth = (100 / this.$speciesImg.length) + '%';
            
            // Creative commons
            this.$creativeCommonsTitleLink =  $( "#creativecommons_title a");
            this.$creativeCommons = $( "#creativecommons" );

            // Add width to species images
            this.$speciesImg.each(function() {
            	$( this ).css( "width", general.$speciesImgWidth );
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
        }

};
// Call the init function when the document is ready()
$( document ).ready( function(){ general.init() } );
