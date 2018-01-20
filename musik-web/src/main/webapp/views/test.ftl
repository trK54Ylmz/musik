<#include "layout.ftl">

<#macro content>
<div class="container margin-top">
    <div class="row">
        <div class="col-xs-12 col-md-3"></div>
        <div class="col-xs-12 col-md-6">
            <div class="text-center">
                <div class="gray small">Please choose an MP3 file</div>
                <label class="btn btn-default btn-file margin-top">
                    Browse <input type="file" id="audio-file" class="hidden"/>
                </label>

                <div id="selected-file" class="gray small margin-top hidden"></div>
                <div id="selected-error" class="alert-danger alert margin-top hidden"></div>
            </div>

            <div class="margin-top-large hidden" id="contents">
                <div class="">
                    <span class="gray">Javascript content</span>
                    <div id="js-content" class="margin-top"></div>
                </div>

                <div class="margin-top-medium">
                    <span class="gray">Native content</span>
                    <div id="native-content" class="margin-top"></div>
                </div>
            </div>
        </div>
        <div class="col-xs-12 col-md-3"></div>
    </div>
</div>
</#macro>

<#macro script>
<script src="/assets/js/mp3encoder.min.js"></script>
<script src="/assets/js/d3.v3.min.js"></script>
<script src="/assets/js/graph.js"></script>
<script src="/assets/js/main.js"></script>
<script src="/assets/js/test.js"></script>
</#macro>