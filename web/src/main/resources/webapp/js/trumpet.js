$(function () {

    function geoLocationSuccess(position) {
        console.log(position.coords.latitude);
        console.log(position.coords.longitude);

        $.ajax({
            url: "/api/?latitude=" + position.coords.latitude + "&longitude=" + position.coords.longitude,
            success: function (data) {
                window.subscribeUrl = data['_links']["subscribe"]["href"];
                window.trumpetUrl = data['_links']["trumpet"]["href"];
                window.locationUrl = data['_links']["location"]["href"];

                window.eventSource = new EventSource(subscribeUrl);
                window.eventSource.addEventListener('trumpet', function (event) {
                    var json = JSON.parse(event.data);
                    $('#trumpets').prepend("<div class='alert alert-info'>" + json.message + " (" + json.distanceFromSource + " meters)</div>");
                    $("#elefant-mascot").effect("shake");

                    var msg = new SpeechSynthesisUtterance(json.message);
                    window.speechSynthesis.speak(msg);

                }, false);
            }
        });
    }

    $('#trumpet-btn').click(function () {
        $.ajax({
            url: window.trumpetUrl,
            type: 'POST',
            data: {
                message: $('#message').val()
            }
        });
        $('#message').val("").focus();
    });

    function geoLocationError(msg) {
    }

    function syncLocation(position) {
        console.log(position.coords.latitude);
        console.log(position.coords.longitude);

        $.ajax({
            url: window.locationUrl,
            type: 'PUT',
            data: {
                latitude: position.coords.latitude,
                longitude: position.coords.longitude
            }
        });
    }

    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(geoLocationSuccess, geoLocationError);

        // Update location every 20 seconds
        setInterval(function () {
            navigator.geolocation.getCurrentPosition(syncLocation, geoLocationError);
        }, 20 * 1000);
    }

    $("#message").keypress(function (e) {
        if (e.which == 13) {
            $('#trumpet-btn').click();
        }
    });
});