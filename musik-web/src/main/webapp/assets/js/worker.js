var buffers = undefined;
var encoder = undefined;
var bufferSize = 0;
var counter = 0;

self.importScripts('/assets/js/mp3encoder.min.js');

self.onmessage = function (event) {
    var data = event.data;

    switch (data.command) {
        case "set":
            console.log("variable set");

            bufferSize = data.bufferSize;
            break;
        case "start":
            console.log("started");

            encoder = new Mp3LameEncoder(data.sampleRate, data.bitRate);
            buffers = data.process === 'separate' ? [] : undefined;

            break;
        case "record":
            var optimized_buffers = [];

            for (var i = 0; i < data.buffers.length; i++) {
                optimized_buffers.push(Math.round(data.buffers[i] * 128));
            }

            if (buffers !== undefined) {
                buffers.push(optimized_buffers);
            }

            counter += buffers[0].length;

            if (counter === bufferSize) {
                encoder.encode(buffers);

                var countMsg = {command: "data", data: encoder.finish()};

                self.postMessage(countMsg);

                counter = 0;
                buffers = [];
            }

            break;
        case "cancel":
            encoder.cancel();
            encoder = undefined;
    }
};