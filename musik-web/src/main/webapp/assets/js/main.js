$(function () {
    var button = $("button#listen");
    var stop = $("button#stop");
    var graph = $("#graph");
    var loading = $("#loading");
    var container = $(".inner-container");

    var bufferSize = 4096;
    var bitRate = 192;

    var sample_size = parseInt($("input[name=sample]").val());

    /**
     * Print error message to error object
     *
     * @param msg the error message
     */
    function error(msg) {
        console.log(msg);

        $("#error").text(msg).removeClass("hidden");
    }

    // sample size must be selected
    if (sample_size === null) {
        error('Invalid sample size');

        return
    }

    if (typeof AudioContext === 'undefined' && typeof webkitAudioContext === 'undefined') {
        error('AudioContext does not support. Please upgrade your browser or change it!');

        return
    }

    // safari uses webkitAudioContext
    var cls = (typeof AudioContext !== 'undefined') ? AudioContext : webkitAudioContext;

    var ac = new cls();

    // define navigator media
    if (!navigator.getUserMedia) {
        navigator.getUserMedia = navigator.getUserMedia || navigator.webkitGetUserMedia
                                 || navigator.mozGetUserMedia || navigator.msGetUserMedia;
    } else {
        console.info("User media supported")
    }

    // define script node
    if (ac.createScriptProcessor === null) {
        ac.createScriptProcessor = ac.createJavaScriptNode;
    } else {
        console.info("Javascript node created")
    }

    var volume = ac.createGain();
    var microphone = null;
    var recorder = null;

    function saveRecording(blob) {
        console.log(blob);
    }

    /**
     * Select one of the channels to read data
     *
     * @param event the event data that received by onaudioprocess
     * @return Float32Array the signal data
     */
    function getBuffers(event) {
        return event.inputBuffer.getChannelData(0);
    }

    // initiates recording from microphone etc.
    function startRecordingProcess() {
        var processor = ac.createScriptProcessor(bufferSize, 1, 1);

        microphone.connect(processor);

        processor.connect(ac.destination);

        var setMsg = {command: "set", bufferSize: bufferSize};

        recorder.postMessage(setMsg);

        var recordMsg = {
            command: "start",
            process: "separate",
            sampleRate: ac.sampleRate,
            bitRate: bitRate
        };

        recorder.postMessage(recordMsg);

        // read signals from audio source
        processor.onaudioprocess = function (event) {
            var data = getBuffers(event);

            recorder.postMessage({command: "record", buffers: data});

            var counter = 0;

            for (var val in data) {
                counter += data[val];
            }

            var value = 128 + (counter * 2);

            if (value > 256) {
                value = 256;
            } else if (value < 0) {
                value = 0;
            }

            tick(value);
        };
    }

    function stopRecordingProcess() {
        recorder.postMessage({command: "cancel"});

        microphone.disconnect();
        recorder.terminate();

        ac.suspend();

        microphone = null;
    }

    // create microphone connection
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
        stop.attr("disabled", null).removeClass("disabled");
        graph.removeClass("hidden");
        loading.removeClass("hidden");
        container.removeClass("margin-top-large").addClass("margin-top");

        if (ac.state === "suspended") {
            ac.resume();
        }

        if (microphone === null) {
            connect();
        }

        // wait until microphone became available
        var interval = setInterval(function () {
            if (microphone === null) {
                return
            }

            clearInterval(interval);

            recorder = new Worker('/assets/js/worker.js');

            recorder.onmessage = function (event) {
                var command = event.data.command;

                switch (command) {
                    case "message":
                        console.log(event.data.message);
                        break;
                    case "count":
                        var count = event.data.count;

                        if (count === bufferSize) {
                            recorder.postMessage("finish")
                        }

                        break;
                    case "blob":
                        saveRecording(event.data.blob);
                        break;
                }
            };

            recorder.onerror = function (event) {
                console.log(event);
            };

            console.log("recording starting");

            startRecordingProcess();
        }, 1000);
    });

    stop.click(function () {
        stopRecordingProcess();

        button.attr("disabled", false);
        stop.attr("disabled", "").addClass("disabled");
        graph.addClass("hidden");
        loading.addClass("hidden");
    });
});