var infowindow;
var map;
var marker;

var initMap = function() {
    var map;
    var mapOptions = {
        gestureHandling: 'greedy',
        mapId: 'mymapid',
    }

    var posStr = $("[name=latlng]").val();
    if (posStr == "") {
        var zoom = parseInt(window.localStorage.getItem('topomap-zoom'));
        if (isNaN(zoom)) {
            zoom = 10;
        }
        var lat = parseFloat(window.localStorage.getItem('topomap-lat'));
        var lng = parseFloat(window.localStorage.getItem('topomap-lng'));
        if (isNaN(lat) || isNaN(lng)) {
            lat = 47;
            lng = 8;
        }
        mapOptions.zoom = zoom;
        mapOptions.center = new google.maps.LatLng(lat,lng);
        map = new google.maps.Map(document.getElementById('mapCanvas'), mapOptions);
    } else {
        var posSplit = posStr.split(",");
        var lat = parseFloat(posSplit[0].trim());
        var lng = parseFloat(posSplit[1].trim());
        mapOptions.zoom = 16;
        mapOptions.center = new google.maps.LatLng(lat,lng);
        map = new google.maps.Map(document.getElementById('mapCanvas'), mapOptions);
        marker = new google.maps.Marker({'position': mapOptions.center});
        marker.setMap(map);
    }

    map.addListener("click", event => {
        console.log(event);
        if(marker) {
            marker.setMap(null);
        }
        marker = new google.maps.Marker({'position': event.latLng});
        marker.setMap(map);
        $("input[name=latlng]").val(event.latLng.lat() + "," + event.latLng.lng());
    });
}