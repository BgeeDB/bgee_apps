/**
 * Override the <code>_renderItem</code> method of the autocomplete jqueryui widget,
 * to handle a new optional option, "displayLabel", configuring the following behaviors:
 * <ul>
 * <li>if equals to "html", the label is assumed to be html, html entities are not escaped.
 * <li>if equals to "highlight", the method takes care of highlighting
 * the search term in the matching labels (by surrounding it with <code>strong</code> tags),
 * and escape other html entities from the original label.
 * <li>If undefined or other value, default autocomplete behavior, html entities are escaped, no highlighting.
 * </ul>
 * @param ul	The ul DOM element containing the autocomplete menu, which entries are appended to.
 * @param item 	The item selected from the menu.
 */
$.ui.autocomplete.prototype._renderItem = function(ul, item) {
	var display = "text";
	if (typeof this.options.displayLabel !== "undefined") {
		var display = this.options.displayLabel;
	}
	if (display === "highlight") {
		//we remove regex special chars from the search term,
		//to be able to perform a 'replace all' with a regex
		//(see RegExp.escape in common.js)
		var searchTerm = $.ui.autocomplete.escapeRegex(this.term);
		//then we modify the matching term (item.label) to highlight the search term
		//we do not use the tag <strong> yet, so that we can escape htmlentities after the replacement
		//(if we escaped html entities BEFORE the replacement,
		//then it would not be possible to highlight a html entities term when used as a search term).
		//why using ":myStrongOpeningTag:" and ":myStrongClosingTag:"?
	    //Because it's unlikely to be present in the label :p
		var toDisplay = item.label.replace(new RegExp('(' + searchTerm + ')', 'gi'),
				     ':myStrongOpeningTag:$1:myStrongClosingTag:');
		//then we escape html entities
		toDisplay = $('<div/>').text(toDisplay).html();

		//and then we replace the <strong> tag
		toDisplay = toDisplay.replace(/:myStrongOpeningTag:/g, '<strong>')
		                     .replace(/:myStrongClosingTag:/g, '</strong>');

		return $("<li>")
			.append($("<a>").html(toDisplay))
			.appendTo(ul);
	} else if (display === "html") {
		return $( "<li>" )
		.append( $( "<a>" ).html( item.label ) )
		.appendTo( ul );
	} else {
		return $( "<li>" )
		.append( $( "<a>" ).text( item.label ) )
		.appendTo( ul );
	}
};