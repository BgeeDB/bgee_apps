/**
 * JavaScript for the anatomical similarity page.
 *
 * @author  Valentine Rech de Laval
 * @version Bgee 14, May 2019
 * @since   Bgee 14, May 2019
 */
$( document ).ready( function() {
   
    $('table.anat-sim-result').DataTable({
        //enable ordering but apply no ordering during initialization
        "order": [],
        responsive: {
            details: {
                display: $.fn.dataTable.Responsive.display.modal({
                    header: function (row) {
                        var data = row.data();
                        return 'Details in ' + data[1];
                    }
                }),
                renderer: function (api, rowIdx, columns) {
                    var data =
                        '<tr><td>' + columns[0].title + '</td><td>' + columns[0].data + '</td></tr>' +
                        '<tr><td>' + columns[1].title + '</td><td>' + columns[1].data + '</td></tr>' +
                        '<tr><td>' + columns[1].title + '</td><td>' + columns[2].data + '</td></tr>';
                    return $('<table class="table"/>').append(data);
                }
            },
            breakpoints: [
                //make the default datatable breakpoints to be the same as bootstrap
                {name: 'desktop', width: Infinity},
                {name: 'tablet-l', width: 992},
                {name: 'tablet-p', width: 768},
                {name: 'mobile-l', width: 480},
                {name: 'mobile-p', width: 320},
                //(default datatable parameters: )
                //{ name: 'desktop',  width: Infinity },
                //{ name: 'tablet-l', width: 1024 },
                //{ name: 'tablet-p', width: 768 },
                //{ name: 'mobile-l', width: 480 },
                //{ name: 'mobile-p', width: 320 }

                //create breakpoints corresponding exactly to bootstrap
                {name: 'table_lg', width: Infinity},
                {name: 'table_md', width: 1200},
                {name: 'table_sm', width: 992},
                {name: 'table_xs', width: 768}
            ]
        },
        columnDefs: [ // Higher responsivePriority are removed first, target define the order
            {responsivePriority: 0, targets: 0}, // Anat. entities
            {responsivePriority: 1, targets: 1}, // Taxon
            {responsivePriority: 2, targets: 2} // Species
        ],
        columns: [ // sorting definition
            {"orderable": true}, // Anatomical entity - null = default sorting
            {"orderable": true}, // Anat. entity ID - null = default sorting
            {"orderable": true} // Developmental stage(s) - ordering disabled
        ]
    });
});