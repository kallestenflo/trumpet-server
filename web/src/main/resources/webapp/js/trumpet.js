$(function () {

    function geoLocationSuccess(position) {
        console.log(position.coords.latitude);
        console.log(position.coords.longitude);

        $.ajax({
            url: "/api/?latitude=" + position.coords.latitude + "&longitude=" + position.coords.longitude,
            success: function (data) {
                window.subscriptionsUrl = data['_links']["subscriptions"]["href"];
                window.trumpetUrl = data['_links']["trumpet"]["href"];
                window.locationUrl = data['_links']["location"]["href"];

                $.ajax({
                    url: subscriptionsUrl,
                    type: 'POST',
                    data: {},
                    contentType: "application/x-www-form-urlencoded",
                    success: function (data) {
                        var subscriptionUrl = data['_links']['subscription']['href'];
                        window.eventSource = new EventSource(subscriptionUrl);
                        window.eventSource.addEventListener('trumpet', function (event) {
                            var json = JSON.parse(event.data);
                            var message = "";
                            if (json.topic !== undefined) {
                                message += "[" + json.topic + "] ";
                            }
                            message += json.message + " (" + json.distanceFromSource + " meters)";
                            $('#trumpets').prepend("<div class='alert alert-info'>" + message + "</div>");
                            $("#elefant-mascot").effect("shake");

                            var msg = new SpeechSynthesisUtterance(json.message);
                            window.speechSynthesis.speak(msg);

                        }, false);
                    }
                });
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