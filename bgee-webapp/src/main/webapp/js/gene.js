/**
 * JavaScript for the gene page.
 * 
 * @author  Philippe Moret
 * @author  Valentine Rech de Laval
 * @version Bgee 13, Jan 2016
 * @since   Bgee 13
 */

$( document ).ready( function(){ 
	
	$('table.expression').DataTable( {
		"ordering": false
	});

	// expand the aggregated stages
	$('table.expression tbody').on('click', '.expandable', function () {
		var t = $(this).text();
		var t2 = t.replace('+','-');
		if (t != t2) {
			$(this).text(t2);
			$(this).parent().parent().find("ul").show(250);
		} else {
			$(this).text(t.replace('-','+'));
			$(this).parent().parent().find("ul").hide(250);
		}
	} );

} );
