<#include "layout.ftl">

<#macro content>
<div class="container margin-top">
    <div id="inner-container" class="margin-top-medium center">
        <input name="sample" type="hidden" class="hidden" value="${sample?c}"/>

        <div class="hidden" id="graph"></div>

        <div class="hidden" id="loading">
            <img src="/assets/images/octo-loader.gif" class="loading"/>
        </div>

        <button class="btn btn-default" id="listen">Listen now ...</button>
        <button class="btn btn-danger disabled" id="stop" disabled>Stop</button>
    </div>
</div>
</#macro>

<#macro script>
<script src="/assets/js/mp3encoder.min.js"></script>
<script src="/assets/js/d3.v3.min.js"></script>
<script src="/assets/js/graph.js"></script>
<script src="/assets/js/main.js"></script>
</#macro>