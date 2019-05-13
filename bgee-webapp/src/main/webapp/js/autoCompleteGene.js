//ID of the completion box for genes. 
var completionBoxGeneId    = "#bgee_gene_search_completion_box";

/**
 * Function called when a term is selected from the autocompletion box.
 *
 * @param event 	a jquery event obtained from the "select" action of <code>autocomplete</code>
 * @param ui 		a jquery object obtained from the "select" action of <code>autocomplete</code>,
 * 					with one attribute, <code>item</code>, being the selected item
 from <code>autocomplete</code>.
 */
function autocompleteTermSelected(event, ui) {
	//we prevent the default action, being to add the term to the autocomplete input
	event.preventDefault();
	var html = $.parseHTML(ui.item.value); //parseHTML return HTMLCollection
	var selectedTerm = $(html).text();
	//fixme remove 	newLabel = newLabel.replace(/:myStrongOpeningTag:/g, '<strong class="search-match">')
	// 		.replace(/:myStrongClosingTag:/g, '</strong>');
	//XXX: should we use the requestParameters object?
	window.location.href = GLOBAL_PROPS.getWebAppURLStart() + "?page=gene&query=" + encodeURIComponent(selectedTerm);
	return this;
}

/**
 * Callback function for the "source" attribute of jQueryUI autocomplete.
 * Will return a list of labels amongst others.
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
function autocompleteTermSource(request, responseCallback) {

	 var autocompleteTermRequestParameters = "?page=search&action=auto_complete_gene_search&" +
	 		"display_type=xml&ajax=1&query=" + encodeURIComponent(request.term);

	$.ajax({
		type: "GET",
		dataType: "xml",
		url: autocompleteTermRequestParameters,
		success: function(xmlResponse) {
			//we remove regex special chars from the search term, 
			//to be able to perform a 'replace all' with a regex, 
			//to highlight the term in the matching result
			var searchTerm = $.ui.autocomplete.escapeRegex(request.term);

			var data = $(xmlResponse).find("match").map(function() {
				var myMatch = $(this);
				var match = myMatch.attr("hit");
				return autocompleteTermGenerateLabel(match, searchTerm);
			}).get();

			var subdata = data.slice(0, 20);
			responseCallback(subdata);
		},
		error: function() {
			$("#bgee_species_search_msg").empty();
			$("#bgee_species_search_msg").append($("<span />")
				.attr("class", 'errorMessage').text("Error"));
		}
	});
}

/**
 * Build the label that will be used by the jqueryui autocomplete widget, 
 * using a DOM element from an AJAX XML response, corresponding to an entry in the autocomplete box 
 * (= a term).
 * Will return an HTML string a <code>span</code> element with the term.
 * The parameter <code>searchTerm</code> corresponds to the text entered in the autocomplete widget.
 * It will be highlighted in the matching result (with <code>strong</code> tags). 
 * Regex specialchars are already escaped from this parameter, so that it can be used directly in 
 * <code>new RegExp()</code>
 * @param label         A <code>String</code> corresponding to a term in the list of source results.
 * @param searchTerm    A <code>String</code> corresponding to the term entered 
 * 						in the autocomplete widget, with regexp special chars escaped.
 * @return 				A <code>String</code> corresponding to the label needed by the autocomplete widget. 
 * 						It is HTML, but html entities are still escaped from the <code>label</code>
 * @see #autocompleteTermSource(request, responseCallback)
 */
function autocompleteTermGenerateLabel(label, searchTerm) {
	var newLabel = label;
	//we modify the string to highlight the search term
	//(if we escaped html entities BEFORE the replacement, span
	//then it would not be possible to highlight a html entities term when used as a search term).
	//why using ":myStrongOpeningTag:" and ":myStrongClosingTag:"? 
	//Because it's unlikely to be present in the label :p
	newLabel = newLabel.replace(new RegExp('(' + searchTerm + ')', 'gi'),
		':myStrongOpeningTag:$1:myStrongClosingTag:');
	//then we escape html entities
	newLabel = $('<div/>').text(newLabel).html();
	//and then we replace the <strong> tag
	newLabel = newLabel.replace(/:myStrongOpeningTag:/g, '<strong class="search-match">')
		.replace(/:myStrongClosingTag:/g, '</strong>');

	
	return newLabel;
}

var textMinLength = 1; // some genes have a one-letter name (for instance, gene 'E')
function loadAutocompleteGene() {

	var waitingElement = "#bgee_species_search_msg";

	$(completionBoxGeneId).autocomplete({
		source: function(request, responseCallback) {
			autocompleteTermSource(request, responseCallback);
		},
		search: function(event, ui) {
			$(waitingElement).text('').append($("<img />").attr("class", 'ajax_waiting')
				.attr("src", "img/wait.gif")
				.attr("alt", 'Loading'));
			$("ul.ui-autocomplete").hide();
		},
		response: function(event, ui) {
			$("ul.ui-autocomplete").show();
			$(waitingElement).empty();
			var text = '';
			if (ui.content.length === 0) {
				text = 'No result';
			}
			if (this.value.length === 100) {
				text += ' - Query cannot be more than 100 characters';
			}
			$(waitingElement).text(text);
		},
		focus: function(event, ui) {
			event.preventDefault();
		},
		select: function(event, ui) {
			$(waitingElement).append($("<img />").attr("class", 'ajax_waiting')
				.attr("src", "img/wait.gif")
				.attr("alt", 'Loading'));
			setTimeout(autocompleteTermSelected(event, ui), 2000);
		},
		//displayLabel is a custom option for Bgee, 
		//see the overridden version of $.ui.autocomplete.prototype._renderItem 
		//in jquery_ui_autocomplete_modif.js
		displayLabel: "html",
		minLength   : textMinLength,
		delay       : 300
	});
}