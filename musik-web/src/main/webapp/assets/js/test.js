$(function () {
    var selected_file = $("#selected-file");
    var selected_error = $("#selected-error");

    $("input#audio-file").change(function () {
        var path = $(this).val();

        if (path.length === 0) {
            selected_file.empty().addClass("hidden");

            selected_error.text("Invalid file selected").removeClass("hidden");

            return
        }

        var parts = path.split('\\');
        var file_name = parts[parts.length - 1];

        selected_file.text("selected file is " + file_name).removeClass("hidden");

        parts = file_name.split(".");

        var file_type = parts[parts.length - 1];

        if (file_type === "mp3") {
            selected_error.addClass("hidden");

            show_contents();
        } else {
            selected_error.text("Invalid file type " + file_type).removeClass("hidden");
        }
    });

    var contents = $("#contents");

    function __get_content_from_js() {
        if (typeof Mp3LameEncoder === "undefined") {
            throw("Mp3 encoder could not loaded");
        }

        var encoder = new Mp3LameEncoder(44100, 24);

    }

    function __get_content_from_native() {
        $.ajax({
                   url: ""
               });
    }

    function show_contents() {
        contents.removeClass("hidden");

        __get_content_from_js();
        __get_content_from_native();
    }
});