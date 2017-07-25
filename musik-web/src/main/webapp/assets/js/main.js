$(function () {
    var button = $("button#listen");
    var loading = $("#loading");
    var container = $(".inner-container");

    var sample_size = parseInt($("input[name=sample]").val());

    function error(msg) {
        $("#error").text(msg).removeClass("hidden");
    }

    if (sample_size === null) {
        error('Invalid sample size');

        return
    }

    if (typeof AudioContext === 'undefined') {
        error('AudioContext does not support. Please upgrade your browser or change it!');

        return
    }

    if (typeof Mp3LameEncoder === 'undefined') {
        error('Mp3LameEncoder could not loaded! Please refresh the page');

        return
    }

    var ac = new AudioContext();

    // define navigator media
    if (!navigator.getUserMedia) {
        navigator.getUserMedia = navigator.getUserMedia || navigator.webkitGetUserMedia
                                 || navigator.mozGetUserMedia || navigator.msGetUserMedia;
    } else {
        console.info("User media supported")
    }

    if (ac.createScriptProcessor === null) {
        ac.createScriptProcessor = ac.createJavaScriptNode;
    } else {
        console.info("Javascript node created")
    }

    var volume = ac.createGain();
    var microphone = null;
    var worker = null;

    function saveRecording(blob) {
        console.log(blob)
    }

    function getBuffers(event) {
        var buffers = [];

        for (var ch = 0; ch < 2; ++ch) {
            buffers[ch] = event.inputBuffer.getChannelData(ch);
        }

        return buffers;
    }

    function startRecordingProcess() {
        var bufferSize = 4096;
        var bitRate = 192;

        var processor = ac.createScriptProcessor(bufferSize, 2, 2);

        microphone.connect(processor);

        processor.connect(ac.destination);

        var message = {
            command: "start",
            process: "separate",
            sampleRate: ac.sampleRate,
            bitRate: bitRate
        };

        worker.postMessage(message);

        processor.onaudioprocess = function (event) {
            worker.postMessage({command: "record", buffers: getBuffers(event)});
        };
    }

    function connect() {
        const connect = function (stream) {
            microphone = ac.createMediaStreamSource(stream);
            microphone.connect(volume);
        };

        const err = function (error) {
            loading.addClass("hidden");

            console.log(error);

            window.alert("Could not get audio input.");
        };

        navigator.getUserMedia({audio: true}, connect, err);
    }

    button.click(function () {
        button.attr("disabled", true);
        loading.removeClass("hidden");
        container.removeClass("margin-top-large").addClass("margin-top");

        if (microphone === null) {
            connect();
        }

        var interval = setInterval(function () {
            if (microphone === null) {
                return
            }

            clearInterval(interval);

            worker = new Worker('/assets/js/worker.js');

            worker.onmessage = function (event) {
                saveRecording(event.data.blob);
            };

            startRecordingProcess();
        }, 1000);
    })
});