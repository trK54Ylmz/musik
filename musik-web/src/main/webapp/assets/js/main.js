$(function () {
    var button = $("button#listen");

    var sample_size = parseInt($("input[name=sample]").val());

    if (sample_size === null) {
        alert("Invalid ajax");

        return
    }

    var ac = new AudioContext();

    // define navigator media
    if (!navigator.getUserMedia) {
        navigator.getUserMedia = navigator.getUserMedia || navigator.webkitGetUserMedia
                                 || navigator.mozGetUserMedia || navigator.msGetUserMedia;
    }

    if (ac.createScriptProcessor === null) {
        ac.createScriptProcessor = ac.createJavaScriptNode;
    }

    var source = ac.createGain();
    var microphone = null;

    button.click(function () {
        button.attr("disabled", true);

        if (microphone === null) {
            microphone.connect(microphoneLevel);

            var input = null;

            var stream = function (stream) {
                console.log(stream);

                input = ac.createMediaStreamSource(stream);
                input.connect(micro);

                microphone = audioContext.createMediaStreamSource(stream);
                microphone.connect(microphoneLevel);

            };

            var error = function (error) {
                window.alert("Could not get audio input.");
            };

            navigator.getUserMedia({audio: true}, stream, error);
        }
    })
});