/**
 * JavaScript for the gene page.
 * 
 * @author  Philippe Moret
 * @author  Valentine Rech de Laval
 * @version Bgee 13, June 2016
 * @since   Bgee 13
 */

$( document ).ready( function(){ 
    
    $('table.expression').DataTable( {
    	//enable ordering but apply no ordering during initialization
    	"order": [],
        responsive: {
            details: {
                display: $.fn.dataTable.Responsive.display.modal( {
                    header: function ( row ) {
                        var data = row.data();
                        return 'Details in ' + data[1];
                    }
                } ),
                renderer: function ( api, rowIdx, columns ) {
                	var data = 
                		'<tr><td>' + columns[0].title + ':</td><td>' + columns[0].data + '</td></tr>' +
                		'<tr><td>' + columns[1].title + ':</td><td>' + columns[1].data + '</td></tr>' +
                		'<tr><td>' + columns[2].title + '</td><td>' + columns[2].data.replace('[+]','').replace('<ul','<ol').replace('</ul>','</ol>') + '</td></tr>' +
                		'<tr><td>' + columns[3].title + '</td><td>' + columns[3].data.replace('<ul','<ol').replace('</ul>','</ol>') + '</td></tr>';
                    return $('<table class="table"/>').append( data );
                }
            }
        },
        columnDefs: [ // Higher responsivePriority are removed first, target define the order
           { responsivePriority: 2, targets: 0 }, // Anat. entity ID
           { responsivePriority: 1, targets: 1 }, // Anatomical entity
           { responsivePriority: 5, targets: 2 }, // Developmental stage(s)
           { responsivePriority: 3, targets: 3 }, // Score
           { responsivePriority: 4, targets: 4 }  // Quality
        ],
        columns: [ // sorting definition
           null, // Anatomical entity - null = default sorting
           null, // Anat. entity ID - null = default sorting
           { "orderable": false },  // Developmental stage(s) - ordering disabled
           { "orderDataType": "dom-text", "type": "score" }, // Score - custom function
           { "orderable": false } // Quality - ordering disabled
        ]
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
    
    jQuery.fn.dataTableExt.oSort['score-asc'] = function(a, b) {
    	// Example: "1,037.0
    	//           <ul class="masked score-list">
    	//               <li class="score">1,037.0</li>
    	//               <li class="score">21,200.0</li>
    	//           </ul>"
    	//substring: start including, end excluded
    	//parseFloat: doesn't deal with US comma separator for thousands
    	var x = parseFloat(a.substring(0, a.indexOf('<ul')).replace(/,/g,''));
    	var y = parseFloat(b.substring(0, b.indexOf('<ul')).replace(/,/g,''));
    	return ((x < y) ? -1 : ((x > y) ? 1 : 0));
    };
     
    jQuery.fn.dataTableExt.oSort['score-desc'] = function(a, b) {
    	var x = parseFloat(a.substring(0, a.indexOf('<ul')).replace(/,/g,''));
    	var y = parseFloat(b.substring(0, b.indexOf('<ul')).replace(/,/g,''));
        return ((x < y) ? 1 : ((x > y) ? -1 : 0));
    };

    loadAutocompleteGene();
} );
