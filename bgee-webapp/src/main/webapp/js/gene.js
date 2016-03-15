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
                	var data = 
                		'<tr><td>' + columns[0].title + ':</td><td>' + columns[0].data + '</td></tr>' +
                		'<tr><td>' + columns[1].title + ':</td><td>' + columns[1].data + '</td></tr>' +
                		'<tr><td>' + columns[2].title + '</td><td>' + columns[2].data.replace('[+]','').replace('<ul','<ol').replace('</ul>','</ol>') + '</td></tr>' +
                		'<tr><td>' + columns[3].title + '</td><td>' + columns[3].data.replace('<ul','<ol').replace('</ul>','</ol>') + '</td></tr>';
                    return $('<table class="table"/>').append( data );
                }
            }
        },
        columnDefs: [ // Higher responsivePriority are remove first
           { responsivePriority: 1, targets: 1 }, // Anatomical entity
           { responsivePriority: 2, targets: 0 }, // Anat. entity ID
           { responsivePriority: 3, targets: 3 }, // Quality
           { responsivePriority: 4, targets: 2 }  // Developmental stage(s)
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

    loadAutocompleteGene();
} );
