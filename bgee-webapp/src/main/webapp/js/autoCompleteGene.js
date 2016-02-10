//ID of the completion box for genes. 
var completionBoxGeneId    = "#completionBoxGene";

/**
 * Function called when a gene is selected from the autocompletion box. 
 * 
 * @param event 	a jquery event obtained from the "select" action of <code>autocomplete</code>
 * @param ui 		a jquery object obtained from the "select" action of <code>autocomplete</code>, 
 * 					with one attribute, <code>item</code>, being the selected item 
 					from <code>autocomplete</code>.
 */
function autocompleteGeneSelected(event, ui) {
 	//we prevent the default action, being to add the name of gene 
 	//to the autocomplete input
 	event.preventDefault();
 	
 	var selectedGene = ui.item;
    // TODO Redirect to specific gene page
 	
	return this;
}

//global var to avoid instantiating this object at each key pressed. 
//TODO DRY: use urlParameters.getParamPage(), urlParameters.getParamAction(), and urlParameters.getParamDisplayType()
var autocompleteGeneRequestParameters = new requestParameters("", true, "&");
autocompleteGeneRequestParameters.addValue(
		new urlParameters.Parameter('page',false,false,false,128,null,'string'), 
		autocompleteGeneRequestParameters.PAGE_SEARCH());
autocompleteGeneRequestParameters.addValue(
		new urlParameters.Parameter('action',false,false,false,128,null,'string'),
		autocompleteGeneRequestParameters.ACTION_AUTO_COMPLETE_GENE_SEARCH());
autocompleteGeneRequestParameters.addValue(
		new urlParameters.Parameter('display_type',false,false,false,128,null,'string'),
		autocompleteGeneRequestParameters.DISPLAY_TYPE_XML());

//to cache the last query
var geneAutocompleteCache = {};
/**
 * Callback function for the "source" attribute of jQueryUI autocomplete. 
 * Will return an array of objects with label and value properties amongst others. 
 *
 * @param request 	A <code>request</code> object, with a single <code>term</code> property, 
 * 					which refers to the value currently in the text input. 
 * 					For example, if the user enters "new yo" in a city field, 
 * 					the Autocomplete term will equal "new yo".
 * @param responseCallback 	A response callback, which expects a single argument: 
 * 							the data to suggest to the user. This data should be filtered 
 * 							based on the provided term, and can be either an array of strings, or an array of objects. 
 * 							It's important when providing a custom source callback to handle errors during the request. 
 * 							You must always call the response callback even if you encounter an error. 
 * 							This ensures that the widget always has the correct state.
 */
function autocompleteGeneSource(request, responseCallback) {
	 if (request.term in geneAutocompleteCache) {
		 responseCallback(geneAutocompleteCache[request.term]);
		 return;
	 }

	//TODO use urlParameters.getParamSearch()
	 var search = new urlParameters.Parameter('search',false,false,false,128,null,'string');
	 autocompleteGeneRequestParameters.resetValues(search);
	 autocompleteGeneRequestParameters.addValue(search, request.term);
	 
	 $.get(autocompleteGeneRequestParameters.getRequestURL(), function(xmlResponse) {
		//we remove regex special chars from the search term, 
		//to be able to perform a 'replace all' with a regex, 
		//to highlight the term in the matching result
		var searchTerm = $.ui.autocomplete.escapeRegex(request.term);
		
		var data = 
		$(xmlResponse).find("gene").map(function() {
			 var myGene = $(this);
		     return {
			     label      : autocompleteGeneGenerateLabel(myGene, searchTerm), 
			     labelSource: myGene.attr("label_source"), 
			     id         : myGene.attr("id"), 
		         name       : (myGene.attr("label_source") === "synonym") ? myGene.attr("label"):myGene.attr("name"), 
			     speciesId  : myGene.attr("species_id"), 
			     speciesName: myGene.attr("species_name")
		     }
	     }).get();
		
		geneAutocompleteCache = {};
		geneAutocompleteCache[request.term] = data;
		responseCallback(data);
	 })
	 //in case of error, we need to call responseCallback anyway
	 .error(function(responseCallback) {
		 responseCallback("Error while requesting the server.");
	 });
}

/**
 * Build the label that will be used by the jqueryui autocomplete widget, 
 * using a DOM element from an AJAX XML response, corresponding to an entry in the autocomplete box 
 * (= a gene). 
 * Will return an HTML string with three "columns"(separated by <code>span</code> elements): 
 * name of the gene, species, ID of the gene. 
 * The parameter <code>searchTerm</code> corresponds to the text entered in the autocomplete widget.
 * It will be highlighted in the matching result (with <code>strong</code> tags). 
 * Regex specialchars are already escaped from this parameter, so that it can be used directly in 
 * <code>new RegExp()</code>
 * @param xmlGene 		a DOM element from an AJAX XML response corresponding to an entry in the list of source results 
 * 						(= a gene).  
 * @param searchTerm 	A <code>String</code> corresponding to the term entered 
 * 						in the autocomplete widget, with regexp special chars escaped.
 * @return 				A <code>String</code> corresponding to the label needed by the autocomplete widget. 
 * 						It is HTML (simulating three columns using <code>span</code> elements), 
 * 						but html entities are still escaped  
 * 						from the information extracted from <code>xmlGene</code>
 * @see #autocompleteGeneSource(request, responseCallback)
 */
function autocompleteGeneGenerateLabel(xmlGene, searchTerm) {
	var labelSource = xmlGene.attr("label_source");
	var toDisplay = "";
	
	//first "column": name or synonym
	var name = xmlGene.attr("name");
	toDisplay += "<span>";
	//if the match comes from the synonym, we display it
	if (labelSource === "synonym") {
		toDisplay += "<i>Syn.: </i>";
		//and it that case, the synonym is provided in the "label" attribute, not "name"
		//("label" always display the match, so is equals to "name" if the match commes from name, etc)
		name = xmlGene.attr("label");
	}
	//we highlight the searchTerm in the name even if the match comes from another source 
	toDisplay += highlightEscape(name, searchTerm) + "</span>";
	
	//second "column": ID
	//we display the match in the ID even if the match comes from another source
	toDisplay += "<span>" + highlightEscape(xmlGene.attr("id"), searchTerm) + "</span>";
	
	//third "column": species name
	//we escape html entities, and we do not hightlight search term
	toDisplay += "<span>" + $('<div/>').text(xmlGene.attr("species_name")).html() + "</span>";
	
	return toDisplay;
}
    
var textMinLength = 1; // some genes have a one-letter name (for instance, gene 'E')
function loadAutocompleteGene() {
	
	$(completionBoxGeneId).autocomplete({
        source: function(request, responseCallback) {
        	autocompleteGeneSource(request, responseCallback); 
        }, 
        change: function(event, ui) {
        	if ($(completionBoxGeneId).val().length < textMinLength) {
        		var remainingCount = textMinLength - $(completionBoxGeneId).val().length;
        		$('#completionGeneCountLetters').text('Type ' + remainingCount + 
        				' letter' + ((remainingCount === 1) ? '': 's') + ': ');
        	}
        }, 
        search: function(event, ui) {
        	$('#completionGeneCountLetters').text("Searching for '" + $(completionBoxGeneId).val() + "'...");
        }, 
        response: function(event, ui) {
        	if (ui.content.length === 0) {
        		$('#completionGeneCountLetters').text('No result');
        	} else {
        	    $('#completionGeneCountLetters').text('');
        	}
        }, 
        open: function() {
        	//format the menu list so that it looks like a table
        	$("ul.ui-autocomplete").formatListToTable(20, 20);
        }, 
        focus: function(event, ui) {
        	event.preventDefault();
        }, 
        select: function(event, ui) {
        	autocompleteGeneSelected(event, ui);
        }, 
        //displayLabel is a custom option for Bgee, 
        //see the overridden version of $.ui.autocomplete.prototype._renderItem 
        //in jquery_ui_autocomplete_modif.js
        displayLabel: "html", 
        minLength   : textMinLength, 
        delay       : 300
    });
}