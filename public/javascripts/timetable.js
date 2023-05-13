$(document).ready( function () {
    var stops = new Bloodhound({
        datumTokenizer: Bloodhound.tokenizers.whitespace,
        queryTokenizer: Bloodhound.tokenizers.whitespace,
        prefetch: '/data/stops.json',
        cache: false
    });

    $('input[name^=stop]').typeahead({
            hint: true,
            highlight: true,
            minLength: 1
        }, {
            name: 'stops',
            source: stops
        }
    );

    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(function(position) {
            $('input[name^=coordinates]').val(position.coords.latitude + ", " + position.coords.longitude);
        });
    }
} );