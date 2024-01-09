$(document).ready( function () {
    $('.dataTable').DataTable();
    $( 'select.select2' ).select2( {
        theme: 'bootstrap-5'
    } );
    $('select[name=db]').change(function() {
        var pathComponents = window.location.pathname.split("/");
        pathComponents[1] = $(this).val();
        window.location.pathname = pathComponents.join("/");
    });
} );
