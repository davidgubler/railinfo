$(document).ready( function () {
    var stops = new Bloodhound({
        datumTokenizer: Bloodhound.tokenizers.whitespace,
        queryTokenizer: Bloodhound.tokenizers.whitespace,
        prefetch: '/' + $('#timetablejs').data('cc') + '/data/stops.json',
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



    getLocation = function() {
        const options = {
            enableHighAccuracy: true,
            timeout: 5000,
            maximumAge: 0
        };
        navigator.geolocation.getCurrentPosition(function(position) {
            $('input[name^=coordinates]').val(position.coords.latitude + ", " + position.coords.longitude);
        }, function (error) {
            console.log(error);
        }, options)
    }

    if (navigator.permissions && navigator.permissions.query) {
        navigator.permissions.query({ name: 'geolocation' }).then(function(result) {
            const permission = result.state;
            if ( permission === 'granted' || permission === 'prompt' ) {
                getLocation();
            }
        });
    } else if (navigator.geolocation) {
        getLocation();
    }
} );