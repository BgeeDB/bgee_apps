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
		"ordering": false,
		responsive: {
			details: {
				display: $.fn.dataTable.Responsive.display.modal( {
					header: function ( row ) {
						var data = row.data();
						return 'Details in ' + data[1];
					}
				} ),
				renderer: function ( api, rowIdx, columns ) {
					var data = $.map( columns, function ( col, i ) {
						return '<tr>'+
						'<td>'+col.title+':'+'</td> '+
						'<td>'+col.data+'</td>'+
						'</tr>';
					} ).join('');

					return $('<table class="table"/>').append( data );
				}
			}
		}
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

	loadAutocompleteGene();
} );
