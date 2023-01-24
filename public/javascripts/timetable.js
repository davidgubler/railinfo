$(document).ready( function () {
    var stops = new Bloodhound({
        datumTokenizer: Bloodhound.tokenizers.whitespace,
        queryTokenizer: Bloodhound.tokenizers.whitespace,
        prefetch: '/data/stops.json'
    });

    $('input[name=stop]').typeahead({
            hint: true,
            highlight: true,
            minLength: 1
        }, {
            name: 'stops',
            source: stops
        }
    );
} );