$(function() {

    function geoLocationSuccess(position) {
        console.log(position.coords.latitude);
        console.log(position.coords.longitude);

        $.ajax({
            url: "/api/?latitude=" + position.coords.latitude + "&longitude=" + position.coords.longitude,
            success: function(data){
                window.subscribeUrl = data['_links']["subscribe"];
                window.trumpetUrl = data['_links']["trumpet"];

                window.eventSource = new EventSource(subscribeUrl);
                window.eventSource.addEventListener('trumpet', function(event) {
                    var json = JSON.parse(event.data);
                    $('#trumpets').append("<div class='alert alert-info'>" + json.message + " (" + json.distanceFromSource + " meters)</div>");


                }, false);
            }
        });
    }

    $('#trumpet-btn').click(function(){
        $.ajax({
            url: window.trumpetUrl,
            type: 'POST',
            data: {
                message : $('#message').val()
            }
        });
        $('#message').val("").focus();
    });

    function geoLocationError(msg) {

    }

    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(geoLocationSuccess, geoLocationError);
    }

});