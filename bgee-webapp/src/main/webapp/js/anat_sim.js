/**
 * JavaScript for the anatomical similarity page.
 *
 * @author  Valentine Rech de Laval
 * @version Bgee 14, May 2019
 * @since   Bgee 14, May 2019
 */
$( document ).ready( function() {

    $('#bgee_anatsim_submit').on("click",  function(){
        var messageElmt = "#bgee_anatsim_msg";

        var message = '';
        
        // Check that at least 2 species are selected 
        var checked = $("input[type=checkbox]:checked").length;
        if (checked === 0 || checked === 1) {
            message = "You must select at least two species. ";
        }
        // Check that something is written in the anat. entity textarea.
        if ($("#bgee_ae_list").val().length === 0) {
            message += "You must enter least one Uberon ID. ";
        }
        // Check if there is an error.
        if (message!== '') {
            $(messageElmt).empty();
            $(messageElmt).append($("<span />").attr("class", 'errorMessage').text(message));
            return false;
        }

        // Set loading img or text
        $(messageElmt).text('').append($("<img />").attr("class", 'ajax_waiting')
            .attr("src", "img/wait.gif")
            .attr("alt", 'Loading'));
    });

    $('table.anat-sim-result').DataTable({
        //enable ordering but apply no ordering during initialization
        "order": [],
        responsive: {
            details: {
                display: $.fn.dataTable.Responsive.display.childRowImmediate
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
            {responsivePriority: 2, targets: 2}  // Species
        ],
        columns: [ // sorting definition
            {"orderable": true}, // Anat. entities
            {"orderable": true}, // Taxon
            {"orderable": true}  // Species
        ]
    });
});