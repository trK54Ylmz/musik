var buffers = undefined;
var encoder = undefined;

self.importScripts('/assets/js/mp3encoder.min.js');

self.onmessage = function (event) {
    var data = event.data;

    console.log(data);

    switch (data.command) {
        case "start":
            console.log("started");

            encoder = new Mp3LameEncoder(data.sampleRate, data.bitRate);
            buffers = data.process === 'separate' ? [] : undefined;

            console.log(data.process);

            break;
        case "record":
            if (buffers !== null) {
                buffers.push(data.buffers);
            } else {
                encoder.encode(data.buffers);
            }

            break;
        case "finish":
            if (buffers !== null) {
                while (buffers.length > 0) {
                    encoder.encode(buffers.shift());
                }
            }

            self.postMessage({blob: encoder.finish()});
            encoder = undefined;

            break;
        case "cancel":
            encoder.cancel();
            encoder = undefined;
    }
};