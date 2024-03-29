/**
 * This code is loaded and run along with the download page to handle
 * the user's actions, to proceed to the search and update the display.
 * It is run when the document is fully loaded by using the jQuery method ready()
 * 
 * @author 	Mathieu Seppey
 * @author 	Valentine Rech de Laval
 * @version Bgee 14, May 2017
 */
//Declaration of an object literal to contain the download page specific code.
//XXX: Should we let this code generate URL by using RequestParameters, or should all URLs 
//used to generate links provided server-side? (notably, documentation links)
var download = {
        // Declaration of the variables used throughout the download page and accessible
        // through the download object. Will be initialized by the init method.

        // Top level container, useful to play with the scroll height
        $container: null,
        // All the figure tag that represent the species and groups
        $species: null,
        //
        $exprCalls: null,
        $refExpr: null,
        // The detail box and its sub elements
        $bgeeDataSelection: null,
        $bgeeDataSelectionCross: null,
        $bgeeDataSelectionImg: null,
        $bgeeDataSelectionTextScientific: null,
        $bgeeDataSelectionTextCommon: null,
        $switchPageLink: null,
        $bgeeGroupDescription: null,
        $bgeeExprDataFormInputs: null,
        $bgeeExprDataFormSubmit: null,
        $bgeeExprDataFormAnatEntityCheck: null,
        $bgeeExprDataFormAllCondCheck: null,
        $bgeeExprDataFormAdvancedColumnsYes: null,
        $bgeeExprDataFormAdvancedColumnsNo: null,
        $orthologButtons: null,
        $orthologCvs: null,
        $exprSimpleData: null,
        $diffExprAnatomyData: null,
        $diffExprAnatomySimpleCsv: null,
        $diffExprAnatomyCompleteCsv: null,   
        $diffExprDevelopmentData: null,
        $diffExprDevelopmentSimpleCsv: null,
        $diffExprDevelopmentCompleteCsv: null,
        $rnaSeqData: null,
        $rnaSeqNoData: null,
        $rnaSeqDataCsv: null,
        $rnaSeqAnnotCsv: null,
        $rnaSeqDataRoot: null,
        $affyData: null,
        $affyNoData: null,
        $affyDataCsv: null,
        $affyAnnotCsv: null,
        $affyDataRoot: null,
        $fullLengthData: null,
        $fullLengthNoData: null,
        $fullLengthDataCsv: null,
        $fullLengthAnnotCsv: null,
        $fullLengthDataRoot: null,
        $inSituData: null,
        $inSituNoData: null,
        $inSituDataCsv: null,
        $inSituAnnotCsv: null,
        $estData: null,
        $estNoData: null,
        $estDataCsv: null,
        $estAnnotCsv: null,
        $exprHelp: null,
        $diffAnatHelp: null,
        $diffDevHelp: null,
        $showSingleSimpleDiffexprAnatomyHeaders: null,
        $showSingleCompleteDiffexprAnatomyHeaders: null,
        $showMultiSimpleDiffexprAnatomyHeaders: null,
        $showMultiCompleteDiffexprAnatomyHeaders: null,
        $showSingleSimpleDiffexprDevelopmentHeaders: null,
        $showSingleCompleteDiffexprDevelopmentHeaders: null,
        $exprNoData: null,
        $diffExprAnatomyNoData: null,
        $diffExprDevelopmentNoData: null,
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
        	$("[data-toggle='popover']").popover();
        	
            // Fetch all needed elements from the DOM
            this.$container = $( "html, body" );
            this.$species = $( "figure" );
            //
            this.$exprCalls = $( "#expr_calls" );
            this.$refExpr= $( "#proc_values" );
            // bgee_data_selection
            this.$bgeeDataSelection = $( "#bgee_data_selection" );
            this.$bgeeDataSelectionCross = $( "#bgee_data_selection_cross" );
            this.$bgeeDataSelectionImg = $( "#bgee_data_selection_img" );
            this.$bgeeDataSelectionTextScientific = 
                $( "#bgee_data_selection_text span.scientificname" );
            this.$bgeeDataSelectionTextCommon = 
                $( "#bgee_data_selection_text span.commonname" );
            this.$switchPageLink = $( "#switch_page_link" );
            this.$bgeeGroupDescription = $( "#bgee_data_selection_text p.groupdescription" );
            // Form to download files
            this.$bgeeExprDataFormInputs = $( "#expr_data_form input" );
            this.$bgeeExprDataFormSubmit = $( "#download_expr_data" );
            this.$bgeeExprDataFormAnatEntityCheck = $( "#expr_data_form input.anatEntityCheck" );
            this.$bgeeExprDataFormAllCondCheck = $( "#expr_data_form input.anatEntityCheck" );
            this.$bgeeExprDataFormAdvancedColumnsYes = $( "#advancedDataRadioYes" );
            this.$bgeeExprDataFormAdvancedColumnsNo = $( "#advancedDataRadioNo" );
            // Data
            this.$orthologButtons = $( "#ortholog_file_buttons" );
            this.$orthologCvs = $( "#ortholog_csv" );
            this.$exprButtons = $( "#expr_buttons" );
            this.$exprSimpleData = $( "#expr_data" );
            this.$diffExprAnatomyButtons = $( "#diffexpr_anatomy_buttons" );
            this.$diffExprAnatomyData = $( "#diffexpr_anatomy_data" );
            this.$diffExprAnatomySimpleCsv = $( "#diffexpr_anatomy_simple_csv" );
            this.$diffExprAnatomyCompleteCsv = $( "#diffexpr_anatomy_complete_csv" );   
            this.$diffExprDevelopmentButtons = $( "#diffexpr_stage_buttons" );
            this.$diffExprDevelopmentData = $( "#diffexpr_development_data" );
            this.$diffExprDevelopmentSimpleCsv = $( "#diffexpr_development_simple_csv" );
            this.$diffExprDevelopmentCompleteCsv = $( "#diffexpr_development_complete_csv" );
            // RNA-Seq processed expression values
            this.$rnaSeqData = $( "#rnaseq_data" );
            this.$rnaSeqNoData = $( "#rnaseq_no_data" );
            this.$rnaSeqDataCsv = $( "#rnaseq_data_csv" );
            this.$rnaSeqAnnotCsv = $( "#rnaseq_annot_csv" );
            this.$rnaSeqDataRoot = $( "#rna_seq_data_root_link" );
            // Single cell full length RNA-Seq processed expression values
            this.$fullLengthData = $( "#full_length_data" );
            this.$fullLengthNoData = $( "#full_length_no_data" );
            this.$fullLengthDataCsv = $( "#full_length_data_csv" );
            this.$fullLengthAnnotCsv = $( "#full_length_annot_csv" );
            this.$fullLengthDataRoot = $( "#full_length_data_root_link" );
            // Affymetrix processed expression values
            this.$affyData = $( "#affy_data" );
            this.$affyNoData = $( "#affy_no_data" );
            this.$affyDataCsv = $( "#affy_data_csv" );
            this.$affyAnnotCsv = $( "#affy_annot_csv" );
            this.$affyDataRoot = $( "#affy_data_root_link" );
            // In situ processed expression values
            this.$inSituData = $( "#in_situ_data" );
            this.$inSituNoData = $( "#in_situ_no_data" );
            this.$inSituDataCsv = $( "#in_situ_data_csv" );
            this.$inSituAnnotCsv = $( "#in_situ_annot_csv" );
            // EST processed expression values
            this.$estData = $( "#est_data" );
            this.$estNoData = $( "#est_no_data" );
            this.$estDataCsv = $( "#est_data_csv" );
            this.$estAnnotCsv = $( "#est_annot_csv" );
            //Help
            this.$exprHelp = $( "#expr_help" );
            this.$diffAnatHelp = $( "#diffexpr_anatomy_help" );
            this.$diffDevHelp = $( "#diffexpr_development_help" );
            this.$orthologsHelp = $( "#orthologs_help" );
            // Show headers
            this.$showSingleSimpleDiffexprAnatomyHeaders = $( "#show_single_simple_diffexpr_anatomy_headers" );
            this.$showSingleCompleteDiffexprAnatomyHeaders = $( "#show_single_complete_diffexpr_anatomy_headers" );
            this.$showMultiSimpleDiffexprAnatomyHeaders = $( "#show_multi_simple_diffexpr_anatomy_headers" );
            this.$showMultiCompleteDiffexprAnatomyHeaders = $( "#show_multi_complete_diffexpr_anatomy_headers" );
            this.$showSingleSimpleDiffexprDevelopmentHeaders = $( "#show_single_simple_diffexpr_development_headers" );
            this.$showSingleCompleteDiffexprDevelopmentHeaders = $( "#show_single_complete_diffexpr_development_headers" );
            this.$showOrthologsHeaders = $( "#show_ortholog_headers" );
            // No data 
            this.$exprNoData = $( "#expr_no_data" );
            this.$diffExprAnatomyNoData = $( "#diffexpr_anatomy_no_data" );
            this.$diffExprDevelopmentNoData = $( "#diffexpr_development_no_data" );
            // Search box
            this.$bgeeSearchForm = $( "#bgee_species_search form" );
            this.$bgeeSearchBox = $( "#bgee_species_search input" );
            this.$bgeeSearchResults = $( "#bgee_species_search_msg" );
            this.$bgeeMoreResultsDown = $( "#bgee_more_results_down" );
            this.$bgeeMoreResultsUp = $( "#bgee_more_results_up" );
            // Creative commons
            this.$creativeCommonsTitleLink =  $( "#creativecommons_title a");
            this.$creativeCommons = $( "#creativecommons" );
            // Initialize the values that have to be dynamically set, i.e. ids and search content  
            this.generateSearchableContent();
            // Add the event listeners to all elements that have a dynamic behavior

            //Google Analytics tracking
            //(see https://developers.google.com/analytics/devguides/collection/analyticsjs/events)
            $("div#bgee_data_selection a").on("click", function() {
        		ga ('send', 'event', 'link', 'click', $( this ).attr("href"));
            });

            // Add a click listener to every species/group to load the corresponding details 
            // or to hide it if already displayed
            this.$species.on("click", function() {
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
            this.$bgeeDataSelectionCross.on("click", function(){
                download.closeDetailBox();
            });

            // Add a listener to the search box to remove the initial displayed text if present
            // First, keep the initial text in a var for later use
            this.initialText = this.$bgeeSearchBox.val();
            this.$bgeeSearchBox.on("click", function() {
                if($( this ).val() == download.initialText){
                    $( this ).val( "" );
                }
            });
            // Add a listener to the link to show/hide the images copyright and change the text
            this.$creativeCommonsTitleLink.on("click", function(){
                download.$creativeCommons.toggle( "blind" );
                if($( this ).text().indexOf( "Hide" ) > -1){
                    $( this ).text($( this ).text().replace( "Hide","Show" ));
                }
                else{
                    $( this ).text($( this ).text().replace( "Show","Hide" ));
                }
            });
            
            // For each links with show-header class, 
            // we add listener to show/hide headers and toggle plus/minus src, title and alt image.          
            $( "a.show-header" ).each(function() {
                $( this ).on("click", function(){
                	var headerTableId = $( this ).attr( "id" ).replace( "show_" , "" );
                	$( "#" + headerTableId ).toggle( "blind" );
                	
                	var source =  $( this ).find( "img" ).attr( "src" );
                	var title = $( this ).find( "img" ).attr( "title" );
                	var alt = $( this ).find( "img" ).attr( "alt" );
                	if ( source.indexOf( "plus" ) > -1 ) {
                		source = source.replace( "plus", "minus" );
                		title = title.replace( "Show", "Hide" );
                		alt = alt.replace( "Plus", "Minus" );
                	} else {
                		source = source.replace( "minus", "plus" );
                		title = title.replace( "Hide", "Show" );
                		alt = alt.replace( "Minus", "Plus");
                	}
                	$( this ).find( "img" ).attr( "src" , source );
                	$( this ).find( "img" ).attr( "title" , title );
                	$( this ).find( "img" ).attr( "alt" , alt );
                });
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
                //Google Analytics tracking
                //(see https://developers.google.com/analytics/devguides/collection/analyticsjs/events)
                var name = speciesData[$species.attr("id")].name;
                ga ('send', 'event', 'detailbox', 'click', name);
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
         * This function update the URL of the download form button.
         */
        updateFormURL: function(allCondCompleteFileUrl, allCondSimpleFileUrl,
        		organCompleteFileUrl, organSimpleFileUrl) {
        	// Form
            var exprDataForm = document.forms["expr_data_form"];
            
        	// Get actual form parameters
        	var isAnatEntity = exprDataForm.elements["anatEntityCheck"].checked;
        	var isAllCond = exprDataForm.elements["allCondCheck"].checked;
        	var isAdvancedColumns = exprDataForm.elements["advancedDataRadioYes"].checked;

        	var url = undefined;
        	if (isAnatEntity && isAllCond) {
        		if (isAdvancedColumns) {
        			url = allCondCompleteFileUrl;
        		} else {
        			url= allCondSimpleFileUrl;
        		}
        	} else {
        		if (isAdvancedColumns) {
        			url = organCompleteFileUrl;
        		} else {
        			url= organSimpleFileUrl;
        		}
        	}
        	
        	this.$bgeeExprDataFormSubmit.attr( "href", url );
        },
        
        // /**
		 // * Gets the url of the file of the given category (undefined if not found)
		 // */
        // getUrlForFileCategory: function(files, category) {
        // 	return getUrlForFileCategory(files, category, undefined);
        // },

        /**
		 * Gets the url of the file of the given category and conditions combination
		 * (undefined if not found)
		 */
        getUrlForFileCategory: function(files, category, conditionParams) {
        	for (var idx = 0; idx < files.length; idx++) {
        		var file = files[idx];
        		// '===' works  because if you know that the array is in the same order 
        		if (file.category === category && (conditionParams === undefined
        				|| download.compareArrays(conditionParams.sort(), file.conditionParameters.sort()))) {
        			return file.path;
        		}
        	}
        },
        
        /**
		 * Defines whether two arrays are equals
		 */
        compareArrays: function(array1, array2) {
            if (!array1 && !array2) {
            	return true;
            }

            if (!array1) {
            	return false;
            }

            if (array1.length != array2.length) {
            	return false;
            }

            for (var i = 0, l = array1.length; i < l; i++) {
                if (array1[i] instanceof Array && array2[i] instanceof Array) {
                    if (!array1[i].equals(array2[i])) {
                    	return false;
                    }
                } else if (array1[i] != array2[i]) {
                    return false;
                }
            }
            return true;
        },
        
        /**
         * Returns the formatted size for the file of the given category (undefined if no file found)
         */
         getSizeForFileCategory: function(files, category, conditionParams) {
         	for (var idx = 0; idx < files.length; idx++) {
         		var file = files[idx];
         		if (file.category === category && (conditionParams === undefined
        				|| download.compareArrays(conditionParams.sort(), file.conditionParameters.sort()))) {
         			var size = file.size;
         			// formatting file size
         			if (size > (1 << 30) * 0.4) {
         				return ""+  Math.round((10*size / (1024*1024*1024)))/10.0 +" GB";
         			}
         			if (size > 1024 * 1024 * 0.4) {
         				return ""+  Math.round((10*size / (1024*1024)))/10.0 +" MB";
         			} else if (size > 1024*0.4){
         				return "" + Math.round((10*size / (1024)))/10.0+" KB";
         			} else {
         				return "" + size +" B";
         			}
         		}
         	}
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
            
            //manage link to processed values/gene expression calls
            var requestSwitchPage = new requestParameters();
            requestSwitchPage.setURLHash(hashToUse);
            
        	if ( this.$exprCalls.length > 0 ) {
        		requestSwitchPage.addValue(urlParameters.getParamPage(), 
        				requestSwitchPage.PAGE_DOWNLOAD());
        		requestSwitchPage.addValue(urlParameters.getParamAction(), 
        				requestSwitchPage.ACTION_DOWLOAD_PROC_VALUE_FILES());
        		this.$switchPageLink.text( "See processed expression values" );
        	} else if ( this.$refExpr.length > 0 ) {
        		requestSwitchPage.addValue(urlParameters.getParamPage(), 
        				requestSwitchPage.PAGE_DOWNLOAD());
        		requestSwitchPage.addValue(urlParameters.getParamAction(), 
        				requestSwitchPage.ACTION_DOWLOAD_CALL_FILES());
        		this.$switchPageLink.text( "See gene expression calls" );    
        	}
        	this.$switchPageLink.attr( "href", 
        			requestSwitchPage.getRequestURL());
            
            var species = null, bgeeSpeciesCommonNames =null, bgeeSpeciesName = null;
        	var groupData = speciesData[id];	
        	// Get files urls for the current species/group
            var files = groupData.downloadFiles;
            var bgeeIsGroup = groupData.members.length > 1;
            var bgeeGroupName = groupData.name;
            
            if (!bgeeIsGroup) {
            	 species = groupData.members[0]
            	 bgeeSpeciesCommonNames = species.name;
            	 bgeeSpeciesName = species.genus +" " +species.speciesName;
                
            }
        	var $images = $currentSpecies.find( ".species_img" );
             
             // there are multiple images, in the case of group, but the field is not used in this case,
             // so no need to care about 
             
            var getUrlForFileCategory = download.getUrlForFileCategory;
            var getSizeForFileCategory = download.getSizeForFileCategory;
          
            //var bgeeOrthologFileUrl = $currentSpecies.data( "bgeeorthologfileurl" );
            var bgeeOrthologFileUrl = getUrlForFileCategory(files, "ortholog");
            //var bgeeExprSimpleFileUrl = $currentSpecies.data( "bgeeexprsimplefileurl" );
            var bgeeExprOrganSimpleFileUrl = getUrlForFileCategory(files, "expr_simple", [ "anat_entity" ]);
            var bgeeExprOrganCompleteFileUrl = getUrlForFileCategory(files, "expr_advanced", [ "anat_entity" ]);
            var bgeeExprAllCondSimpleFileUrl = getUrlForFileCategory(files, "expr_simple", [ "anat_entity", "dev_stage", "sex", "strain" ]);
            var bgeeExprAllCondCompleteFileUrl = getUrlForFileCategory(files, "expr_advanced", [ "anat_entity", "dev_stage", "sex", "strain" ]);
            var bgeeDiffExprAnatomySimpleFileUrl = getUrlForFileCategory(files, "diff_expr_anatomy_simple");
            var bgeeDiffExprAnatomyCompleteFileUrl = getUrlForFileCategory(files, "diff_expr_anatomy_complete");
            var bgeeDiffExprDevelopmentSimpleFileUrl = getUrlForFileCategory(files, "diff_expr_dev_simple");
            var bgeeDiffExprDevelopmentCompleteFileUrl = getUrlForFileCategory(files, "diff_expr_dev_complete");

             // get file sizes
            var bgeeOrthologFileSize = getSizeForFileCategory(files, "ortholog");
            var bgeeExprOrganSimpleFileSize = getSizeForFileCategory(files, "expr_simple", [ "anat_entity" ]);
            var bgeeExprOrganCompleteFileSize = getSizeForFileCategory(files, "expr_advanced", [ "anat_entity" ]);
            var bgeeExprAllCondSimpleFileSize = getSizeForFileCategory(files, "expr_simple", [ "anat_entity", "dev_stage", "sex", "strain" ]);
            var bgeeExprAllCondCompleteFileSize = getSizeForFileCategory(files, "expr_advanced", [ "anat_entity", "dev_stage", "sex", "strain" ]);
            var bgeeDiffExprAnatomySimpleFileSize =	getSizeForFileCategory(files, "diff_expr_anatomy_simple");
            var bgeeDiffExprAnatomyCompleteFileSize = getSizeForFileCategory(files, "diff_expr_anatomy_complete");
            var bgeeDiffExprDevelopmentSimpleFileSize = getSizeForFileCategory(files, "diff_expr_dev_simple");
            var bgeeDiffExprDevelopmentCompleteFileSize = getSizeForFileCategory(files, "diff_expr_dev_complete");
            
           // RNA-Seq processed expression values
            var bgeeRnaSeqDataFileUrl = getUrlForFileCategory(files, "rnaseq_data");
            var bgeeRnaSeqAnnotFileUrl = getUrlForFileCategory(files, "rnaseq_annot");
            var bgeeRNASeqDataRootURL = getUrlForFileCategory(files, "rnaseq_root");
            var bgeeRnaSeqDataFileSize = getSizeForFileCategory(files, "rnaseq_data");
            var bgeeRnaSeqAnnotFileSize = getSizeForFileCategory(files, "rnaseq_annot");
            
           // Single cell full length RNA-Seq processed expression values
            var bgeeFullLengthDataFileUrl = getUrlForFileCategory(files, "full_length_data");
            var bgeeFullLengthAnnotFileUrl = getUrlForFileCategory(files, "full_length_annot");
            var bgeeFullLengthDataRootURL = getUrlForFileCategory(files, "full_length_root");
            var bgeeFullLengthDataFileSize = getSizeForFileCategory(files, "full_length_data");
            var bgeeFullLengthAnnotFileSize = getSizeForFileCategory(files, "full_length_annot");

            // Affymetrix processed expression values
            var bgeeAffyDataFileUrl = getUrlForFileCategory(files, "affy_data");
            var bgeeAffyAnnotFileUrl = getUrlForFileCategory(files, "affy_annot");
            var bgeeAffyDataRootURL = getUrlForFileCategory(files, "affy_root");
            var bgeeAffyDataFileSize = getSizeForFileCategory(files, "affy_data");
            var bgeeAffyAnnotFileSize = getSizeForFileCategory(files, "affy_annot");
            
            // In situ processed expression values
            var bgeeInSituDataFileUrl = $currentSpecies.data( "bgeeinsitudatafileurl" );
            var bgeeInSituAnnotFileUrl = $currentSpecies.data( "bgeeinsituannotfileurl" );
            var bgeeInSituDataFileSize = $currentSpecies.data( "bgeeinsitudatafilesize" );
            var bgeeInSituAnnotFileSize = $currentSpecies.data( "bgeeinsituannotfilesize" );

            // EST processed expression values
            var bgeeEstDataFileUrl = $currentSpecies.data( "bgeeestdatafileurl" );
            var bgeeEstAnnotFileUrl = $currentSpecies.data( "bgeeestannotfileurl" );
            var bgeeEstDataFileSize = $currentSpecies.data( "bgeeestdatafilesize" );
            var bgeeEstAnnotFileSize = $currentSpecies.data( "bgeeestannotfilesize" );

            // Proceed to the update
            var numberOfSpecies = groupData.members.length;
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
                var newHeight = 100 / ( Math.ceil( Math.sqrt( numberOfSpecies ) ) ); 
                // Assume that the image is a square, so height and width are the same
                $newElement.css( "height" , newHeight + "%" ).css( "width" , newHeight + "%" ); 
                // Add the species short name to the name of all species field
                
                namesOfAllSpecies = namesOfAllSpecies + $( this ).attr("alt") + ", ";
            });
            namesOfAllSpecies = namesOfAllSpecies.slice( 0, - 2 ); // Remove the extra ' ,'
            // if it is a group, use the group name as label, else the species name
            if( bgeeIsGroup ){
            	this.$switchPageLink.hide();
                this.$bgeeDataSelectionTextScientific.text( "" );
                this.$bgeeDataSelectionTextCommon.text( bgeeGroupName );
                this.$bgeeGroupDescription.text( numberOfSpecies + " species: " );
                this.$bgeeGroupDescription.append( $( "<i></i>" ).append( namesOfAllSpecies ) );
                this.$showMultiSimpleDiffexprAnatomyHeaders.show();
                this.$showMultiCompleteDiffexprAnatomyHeaders.show();
                this.$showSingleSimpleDiffexprAnatomyHeaders.hide();
                this.$showSingleCompleteDiffexprAnatomyHeaders.hide();
                if ( this.$exprCalls.length > 0 ) {
                    var urlDoc = new requestParameters();
                    urlDoc.addValue(urlParameters.getParamPage(), 
                    		urlDoc.PAGE_DOCUMENTATION());
                    urlDoc.addValue(urlParameters.getParamAction(), 
                    		urlDoc.ACTION_DOC_CALL_DOWLOAD_FILES());
                    urlDoc.setURLHash(urlDoc.HASH_DOC_CALL_MULTI());
                	this.$exprHelp.attr( "href", urlDoc.getRequestURL());
                	this.$diffDevHelp.attr( "href", urlDoc.getRequestURL());
                	this.$diffAnatHelp.attr( "href", urlDoc.getRequestURL());
                	
                	urlDoc.setURLHash(urlDoc.HASH_DOC_CALL_OMA());
                	this.$orthologsHelp.attr( "href", urlDoc.getRequestURL());
                } 
            } else {
            	this.$switchPageLink.show();
                this.$bgeeDataSelectionTextScientific.text( bgeeSpeciesName );
                //we display the group name as subtitle rather than the species common name, 
                //because we used to have incorrect common names at some point, 
                //and because this allows more flexibility (e.g. "human including GTEx data")
                if (bgeeGroupName && bgeeGroupName !== bgeeSpeciesName) {
                	this.$bgeeDataSelectionTextCommon.text( "("+ bgeeGroupName +")" );
                } else {
                    this.$bgeeDataSelectionTextCommon.text( "" );
                }
                this.$bgeeGroupDescription.text( "" );
                this.$showMultiSimpleDiffexprAnatomyHeaders.hide();
                this.$showMultiCompleteDiffexprAnatomyHeaders.hide();
                this.$showSingleSimpleDiffexprAnatomyHeaders.show();
                this.$showSingleCompleteDiffexprAnatomyHeaders.show();
                if ( this.$exprCalls.length > 0 ) {
                	var urlDoc = new requestParameters("", true, "&");
                    urlDoc.addValue(urlParameters.getParamPage(), urlDoc.PAGE_DOCUMENTATION());
                    urlDoc.addValue(urlParameters.getParamAction(), urlDoc.ACTION_DOC_CALL_DOWLOAD_FILES());
                    
                    urlDoc.setURLHash(urlDoc.HASH_DOC_CALL_SINGLE_EXPR());
                	this.$exprHelp.attr( "href", urlDoc.getRequestURL());
                	urlDoc.setURLHash(urlDoc.HASH_DOC_CALL_SINGLE_DIFF());
                	this.$diffDevHelp.attr( "href", urlDoc.getRequestURL());
                	this.$diffAnatHelp.attr( "href", urlDoc.getRequestURL());
                    this.updateFormURL( bgeeExprAllCondCompleteFileUrl, bgeeExprAllCondSimpleFileUrl,
                    		bgeeExprOrganCompleteFileUrl, bgeeExprOrganSimpleFileUrl );
                }
            }
            
            this.$bgeeExprDataFormInputs.on("click", function() {
                download.updateFormURL( bgeeExprAllCondCompleteFileUrl, bgeeExprAllCondSimpleFileUrl,
                		bgeeExprOrganCompleteFileUrl, bgeeExprOrganSimpleFileUrl );
            });
            
            // Hide all header table to hide tables already opened in another detail box (banner)
            $( ".header_table" ).each(function() {
                $( this ).hide();
            });
            
            // Reset all header details to plus image
            $( "a.show-header img.details" ).each(function() {
            	$( this ).attr( "src" , GLOBAL_PROPS.getImgURLStart() + "plus.png" );
            });

            // Update the values of the download links and size files
            // Ortholog file
            if (bgeeOrthologFileUrl === undefined) {
            	this.$orthologButtons.hide();
            } else {
            	this.$orthologButtons.show();
            	this.$orthologCvs.attr( "href", bgeeOrthologFileUrl );
            	this.$orthologCvs.text( "Download file (" + bgeeOrthologFileSize + ")" );
            }

            // Expression files
            if (bgeeExprOrganSimpleFileUrl === undefined) {
            	this.$exprSimpleData.hide();
            	//TODO remove when multi-species expression files are computed
            	if( bgeeIsGroup ){
            		this.$exprButtons.hide();
            	} else {
            		this.$exprButtons.show();
            		this.$exprNoData.show();
            		this.$exprHelp.show();
            	}
            } else {
        		this.$exprButtons.show();
            	this.$exprSimpleData.show();
        		this.$exprHelp.show();
            	this.$exprNoData.hide();
            }

            // Differential expression - anatomy comparison
            if (bgeeDiffExprAnatomySimpleFileUrl === undefined) {
            	this.$diffExprAnatomyData.hide(); 
            	this.$diffExprAnatomyNoData.show();
            } else {
            	this.$diffExprAnatomyData.show(); 
            	this.$diffExprAnatomyNoData.hide();
            	this.$diffExprAnatomySimpleCsv.attr( "href", bgeeDiffExprAnatomySimpleFileUrl );
            	this.$diffExprAnatomyCompleteCsv.attr( "href", bgeeDiffExprAnatomyCompleteFileUrl );
            	this.$diffExprAnatomySimpleCsv.text( 
            			"Download simple file (" + bgeeDiffExprAnatomySimpleFileSize + ")" );
            	this.$diffExprAnatomyCompleteCsv.text( 
            			"Download complete file (" + bgeeDiffExprAnatomyCompleteFileSize + ")" );
            }

            // Differential expression - development comparison
            if( bgeeDiffExprDevelopmentSimpleFileUrl === undefined ) {
            	this.$diffExprDevelopmentData.hide();
            	//TODO remove when multi-species developmental diff. expression files are computed
                if( bgeeIsGroup ){
            		this.$diffExprDevelopmentButtons.hide();
                } else {
            		this.$diffExprDevelopmentButtons.show();
                	this.$diffExprDevelopmentNoData.show();
                	this.$diffDevHelp.show();
                }
            } else {
            	this.$diffExprDevelopmentButtons.show();
            	this.$diffExprDevelopmentData.show(); 
            	this.$diffDevHelp.show();
            	this.$diffExprDevelopmentNoData.hide();
            	this.$diffExprDevelopmentSimpleCsv.attr( "href", bgeeDiffExprDevelopmentSimpleFileUrl );
            	this.$diffExprDevelopmentCompleteCsv.attr( "href", bgeeDiffExprDevelopmentCompleteFileUrl );
            	this.$diffExprDevelopmentSimpleCsv.text( 
            			"Download simple file (" + bgeeDiffExprDevelopmentSimpleFileSize + ")" );
            	this.$diffExprDevelopmentCompleteCsv.text( 
            			"Download complete file (" + bgeeDiffExprDevelopmentCompleteFileSize + ")" );
            }
            
            // RNA-Seq processed expression values
            if (!this.$refExpr.length || bgeeRnaSeqDataFileUrl === undefined) {
            	this.$rnaSeqData.hide(); 
            	this.$rnaSeqNoData.show();
            } else {
            	this.$rnaSeqData.show(); 
            	this.$rnaSeqNoData.hide();
            	this.$rnaSeqDataCsv.attr( "href", bgeeRnaSeqDataFileUrl );
            	this.$rnaSeqAnnotCsv.attr( "href", bgeeRnaSeqAnnotFileUrl );
            	this.$rnaSeqAnnotCsv.text( "Download experiments/libraries info (" + bgeeRnaSeqAnnotFileSize + ")" );
            	this.$rnaSeqDataCsv.text( "Download read counts, TPMs, and FPKMs (" + bgeeRnaSeqDataFileSize + ")" );
            	this.$rnaSeqDataRoot.attr("href", rnaSeqExprValuesDirs[id]);
            }

            // Affymetrix processed expression values
            if (!this.$refExpr.length || bgeeAffyDataFileUrl === undefined) {
            	this.$affyData.hide(); 
            	this.$affyNoData.show();
            } else {
            	this.$affyData.show(); 
            	this.$affyNoData.hide();
            	this.$affyDataCsv.attr( "href", bgeeAffyDataFileUrl );
            	this.$affyAnnotCsv.attr( "href", bgeeAffyAnnotFileUrl );
            	this.$affyAnnotCsv.text( "Download experiments/chips info (" + bgeeAffyAnnotFileSize + ")" );
            	this.$affyDataCsv.text( "Download signal intensities (" + bgeeAffyDataFileSize + ")" );
            	this.$affyDataRoot.attr("href", affyExprValuesDirs[id]);
            }
            
         // Single cell full length RNA-Seq processed expression values
            if (!this.$refExpr.length || bgeeFullLengthDataFileUrl === undefined) {
            	this.$fullLengthData.hide(); 
            	this.$fullLengthNoData.show();
            } else {
            	this.$fullLengthData.show(); 
            	this.$fullLengthNoData.hide();
            	this.$fullLengthDataCsv.attr( "href", bgeeFullLengthDataFileUrl );
            	this.$fullLengthAnnotCsv.attr( "href", bgeeFullLengthAnnotFileUrl );
            	this.$fullLengthAnnotCsv.text( "Download experiments/libraries info (" + bgeeFullLengthAnnotFileSize + ")" );
            	this.$fullLengthDataCsv.text( "Download read counts, TPMs, and FPKMs (" + bgeeFullLengthDataFileSize + ")" );
            	this.$fullLengthDataRoot.attr("href", fullLengthExprValuesDirs[id]);
            }

            // In situ processed expression values
            if (!this.$refExpr.length || bgeeInSituDataFileUrl === undefined) {
            	this.$inSituData.hide(); 
            	this.$inSituNoData.hide();
            	//TODO remove when in situ data files are computed
            } else {
            	this.$inSituData.show(); 
            	this.$inSituNoData.hide();
            	this.$inSituDataCsv.attr( "href", bgeeInSituDataFileUrl );
            	this.$inSituAnnotCsv.attr( "href", bgeeInSituAnnotFileUrl );
            	this.$inSituAnnotCsv.text( "Download annotation file (" + bgeeInSituAnnotFileSize + ")" );
            	this.$inSituDataCsv.text( "Download data file (" + bgeeInSituDataFileSize + ")" );
            }

            // EST processed expression values
            if (!this.$refExpr.length || bgeeEstDataFileUrl === undefined) {
            	this.$estData.hide(); 
            	this.$estNoData.hide();
            	//TODO remove when EST data files are computed
            } else {
            	this.$estData.show(); 
            	this.$estNoData.hide();
            	this.$estDataCsv.attr( "href", bgeeEstDataFileUrl );
            	this.$estAnnotCsv.attr( "href", bgeeEstAnnotFileUrl );
            	this.$estDataCsv.text( "Download data file (" + bgeeEstDataFileSize + ")" );
            	this.$estAnnotCsv.text( "Download annotation file (" + bgeeEstAnnotFileSize + ")" );
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
                this.$bgeeDataSelection.insertAfter( $currentSpecies.nextAll().addBack().last() );
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
            window.location.hash = '#' + hashToUse; 
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
                    for (var i = 0; i < download.searchContent[$( this ).attr( "id" )].length; i++) {
                        if (download.searchContent[$( this ).attr( "id" )][i].toLowerCase()
                                .indexOf( text.toLowerCase() ) > -1){
                            $( this ).addClass( "highlight" );
                            break;
                        }
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
         * This function fetches the words that can be searched and put it in an associative array
         * where the id of the species/group corresponds to its searchable content.
         * It also fill a list with all possible words that can be proposed to the user during
         * a search (autocompletion)
         */
        generateSearchableContent: function(){
        	//we use the server-side generate variables
        	download.autoCompletionList = autocomplete;
        	download.searchContent = keywords;
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
