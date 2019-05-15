/**
 * JavaScript for the expression comparison page.
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 14, May 2019
 * @since   Bgee 14, May 2019
 */

$( document ).ready( function(){

    var t = $('table.expr_comp.multi-sp') ;
    $('table.expr_comp.multi-sp').DataTable( {
    	//enable ordering but apply no ordering during initialization
    	"order": [],
        responsive: {
            details: {
                display: $.fn.dataTable.Responsive.display.childRowImmediate,
                type: 'none',
                target: ''
            }, 
            breakpoints: [
                //make the default datatable breakpoints to be the same as bootstrap
                { name: 'desktop',  width: Infinity },
                { name: 'tablet-l', width: 992 },
                { name: 'tablet-p', width: 768 },
                { name: 'mobile-l', width: 480 },
                { name: 'mobile-p', width: 320 }, 
                //(default datatable parameters: )
                //{ name: 'desktop',  width: Infinity },
                //{ name: 'tablet-l', width: 1024 },
                //{ name: 'tablet-p', width: 768 },
                //{ name: 'mobile-l', width: 480 },
                //{ name: 'mobile-p', width: 320 }
                
                //create breakpoints corresponding exactly to bootstrap
                { name: 'table_lg', width: Infinity },
                { name: 'table_md', width: 1200 },
                { name: 'table_sm', width: 992 },
                { name: 'table_xs', width: 768 }
            ]
        },
        columnDefs: [ // Higher responsivePriority are removed first, target define the order
           { responsivePriority: 1, targets: 0 }, // Anatomical entities
           { responsivePriority: 2, targets: 1 }, // Gene count with presence of expression
           { responsivePriority: 2, targets: 2 }, // Gene count with absence of expression
           { responsivePriority: 2, targets: 3 }, // Gene count with no data
           { responsivePriority: 3, targets: 4 }, // Species count with presence of expression
           { responsivePriority: 3, targets: 5 }, // Species count with absence of expression
           { responsivePriority: 3, targets: 6 }, // Species count with no data
           { responsivePriority: 1, targets: 7 }  // Details
        ],
        columns: [ // sorting definition
           { "orderable": true }, // Anatomical entities
           { "orderable": true }, // Gene count with presence of expression
           { "orderable": true }, // Gene count with absence of expression
           { "orderable": true }, // Gene count with no data
           { "orderable": true }, // Species count with presence of expression
           { "orderable": true }, // Species count with absence of expression
           { "orderable": true }, // Species count with no data
           { "orderable": false } // Details
        ]
    });

    $('table.expr_comp.single-sp').DataTable( {
        //enable ordering but apply no ordering during initialization
        "order": [],
        responsive: {
            details: {
                display: $.fn.dataTable.Responsive.display.childRowImmediate,
                type: 'none',
                target: ''
            },
            breakpoints: [
                //make the default datatable breakpoints to be the same as bootstrap
                { name: 'desktop',  width: Infinity },
                { name: 'tablet-l', width: 992 },
                { name: 'tablet-p', width: 768 },
                { name: 'mobile-l', width: 480 },
                { name: 'mobile-p', width: 320 },
                //(default datatable parameters: )
                //{ name: 'desktop',  width: Infinity },
                //{ name: 'tablet-l', width: 1024 },
                //{ name: 'tablet-p', width: 768 },
                //{ name: 'mobile-l', width: 480 },
                //{ name: 'mobile-p', width: 320 }

                //create breakpoints corresponding exactly to bootstrap
                { name: 'table_lg', width: Infinity },
                { name: 'table_md', width: 1200 },
                { name: 'table_sm', width: 992 },
                { name: 'table_xs', width: 768 }
            ]
        },
        columnDefs: [ // Higher responsivePriority are removed first, target define the order
            { responsivePriority: 1, targets: 0 }, // Anatomical entities
            { responsivePriority: 2, targets: 1 }, // Gene count with presence of expression
            { responsivePriority: 2, targets: 2 }, // Gene count with absence of expression
            { responsivePriority: 2, targets: 3 }, // Gene count with no data
            { responsivePriority: 1, targets: 4 }  // Details
        ],
        columns: [ // sorting definition
            { "orderable": true }, // Anatomical entities
            { "orderable": true }, // Gene count with presence of expression
            { "orderable": true }, // Gene count with absence of expression
            { "orderable": true }, // Gene count with no data
            { "orderable": false } // Details
        ]
    });

    // expand the aggregated cells
    $('table tbody').on('click', '.expandable', function () {
        var t = $(this).text();
        var t2 = t.replace('+','-');
        if (t !== t2) {
            $(this).text(t2);
            $(this).parent().parent().find("ul").show(250);
        } else {
            $(this).text(t.replace('-','+'));
            $(this).parent().parent().find("ul").hide(250);
        }
    } );

} );

