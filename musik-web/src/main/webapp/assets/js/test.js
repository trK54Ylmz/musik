$(function () {
    var selected_file = $("#selected-file");
    var selected_error = $("#selected-error");

    $("input#audio-file").change(function (ev) {
        var path = $(this).val();

        if (path.length === 0) {
            selected_file.empty().addClass("hidden");

            selected_error.text("Invalid file selected").removeClass("hidden");

            return
        }

        var file = ev.target.files.length > 0 ? ev.target.files[0] : null;

        if (file !== null) {
            if (file.type !== "audio/mp3") {
                selected_error.text("Invalid file type " + file.type).removeClass("hidden");

                return
            }

            selected_file.text("selected file is " + file.name).removeClass("hidden");

            selected_error.addClass("hidden");

            show_contents(file);
        } else {
            selected_error.text("Invalid file selected").removeClass("hidden");
        }
    });

    var contents = $("#contents");

    /**
     * Get signal content as array from selected audio file
     *
     * @param file the audio file
     * @param callback function the response callback function
     * @returns Uint8Array
     */
    function get_content_as_buffer(file, callback) {
        if (!window.FileReader || !window.Blob) {
            throw("File read API does not supported")
        }

        var reader = new FileReader();

        reader.onload = function (f) {
            if (typeof f.target === "undefined" || f.target.readyState !== FileReader.DONE) {
                throw("Probably EOF problem");
            }

            if (typeof f.target.result === "undefined") {
                throw ("Incorrect object reading");
            }

            callback(new Uint8Array(f.target.result));
        };

        reader.readAsArrayBuffer(file);
    }

    /**
     * Get signal content as binary from selected audio file
     *
     * @param file File the audio file
     * @param callback function the response callback function
     * @returns string
     */
    function get_content_as_binary(file, callback) {
        if (!window.FileReader || !window.Blob) {
            throw("File read API does not supported")
        }

        var reader = new FileReader();

        reader.onload = function (f) {
            if (typeof f.target === "undefined" || f.target.readyState !== FileReader.DONE) {
                throw("Probably EOF problem");
            }

            if (typeof f.target.result === "undefined") {
                throw ("Incorrect object reading");
            }

            callback(f.target.result);
        };

        reader.readAsDataURL(file);
    }

    var x_line = function (d, i) {
        return x(now - (limit - 1 - i) * duration)
    };

    var y_line = function (d) {
        return y(d)
    };

    /**
     * Draw graph for signal content
     *
     * @param id the DOM object id
     * @param data the signal content
     */
    function draw_graph(id, data) {
        var width = 500, height = 256;

        var groups = {
            output: {
                value: 0,
                color: "grey",
                data: d3.range(limit).map(function () {
                    return 0
                })
            }
        };

        var x = d3.time.scale().domain([now - (limit - 2), now - duration]).range([0, width]);

        var y = d3.scale.linear().domain([0, 256]).range([height, 0]);

        var line = d3.svg.line().interpolate("basis").x(x_line()).y(y_line);

        var svg = d3.select("#" + id).append("svg").attr("class", "chart").attr("width", width).attr("height", height);

        var paths = svg.append("g");

        for (var name in groups) {
            var g = groups[name];

            g.path = paths.append("path").data([g.data]).attr("class", name + " group").style("stroke", group.color);
        }
    }

    /**
     * Load content via Javascript
     *
     * @param file the audio file
     */
    function get_content_from_js(file) {
        if (typeof Mp3LameEncoder === "undefined") {
            throw("Mp3 encoder could not loaded");
        }

        var callback = function (data) {
            var encoder = new Mp3LameEncoder(44100, 24);

            encoder.encode(data);

            var signals = encoder.finish();

            draw_graph("js-graph", signals);
        };

        get_content_as_buffer(file, callback);
    }

    /**
     * Load content via native code
     *
     * @param file the audio file
     */
    function get_content_from_native(file) {
        var callback = function (data) {
            var formData = new FormData();
            formData.append("data", data);

            var request = {
                url: "/test/native",
                data: data,
                method: "post",
                dataType: "json",
                cache: false,
                processData: false,
                contentType: false,
                success: function (response) {
                    if (!response) {
                        // print error

                        return
                    }

                    var content = response.content;

                    draw_graph("native-graph", content);
                },
                error: function () {
                    selected_error.text("Native application signal processing error").removeClass("hidden");
                }
            };

            $.ajax(request);
        };

        get_content_as_binary(file, callback);
    }

    /**
     * Load file content by file object
     *
     * @param file File
     */
    function show_contents(file) {
        try {
            contents.removeClass("hidden");

            get_content_from_js(file);
            get_content_from_native(file);
        } catch (e) {
            console.error(e);

            selected_error.text("Error! " + e).removeClass("hidden");
        }
    }
});