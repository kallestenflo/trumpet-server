$(function () {

    function geoLocationSuccess(position) {
        console.log(position.coords.latitude);
        console.log(position.coords.longitude);

        $.ajax({
            url: "/api/",
            success: function (data) {
                $.ajax({
                    url: data['_links']["create-trumpeteer"]["href"],
                    type: 'POST',
                    data: {latitude: position.coords.latitude, longitude: position.coords.longitude},
                    success: function (data) {
                        window.subscriptionUrl = data['_links']["sse-subscribe"]["href"];
                        window.trumpetUrl = data['_links']["trumpet"]["href"];
                        window.locationUrl = data['_links']["update-location"]["href"];

                        window.eventSource = new EventSource(subscriptionUrl);
                        window.eventSource.addEventListener('trumpet', function (event) {
                            var json = JSON.parse(event.data);
                            if(json.messageType !== 'trumpet'){
                                return;
                            }
                            json = json.message;

                            var message = "";
                            if (json.topic !== undefined) {
                                message += "[" + json.topic + "] ";
                            }
                            message += json.message + " (" + json.distanceFromSource + " meters)";

                            var alertType = json.sentByMe ? 'alert-success' : 'alert-info';

                            $('#trumpets').prepend("<div class='alert " + alertType + "'>" + message + "</div>");
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