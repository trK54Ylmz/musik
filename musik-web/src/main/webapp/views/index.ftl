<#include "layout.ftl">

<#macro content>
<div class="container margin-top">
    <nav class="navbar navbar-default">
        <div class="container-fluid">
            <div class="navbar-header">
                <button type="button" class="navbar-toggle collapsed" data-toggle="collapse"
                        data-target="#navbar-collapse" aria-expanded="false">
                    <span class="sr-only">Toggle navigation</span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                </button>
                <a class="navbar-brand" href="#">Musik</a>
            </div>

            <div class="collapse navbar-collapse" id="navbar-collapse">
                <ul class="nav navbar-nav">
                    <li><a href="#">Link</a></li>
                    <li><a href="#">Link</a></li>
                </ul>
            </div>
        </div>
    </nav>

    <div class="margin-top-large center">
        <input name="sample" type="hidden" class="hidden" value="${sample?c}"/>
        <button class="btn btn-default" id="listen">Listen now ...</button>
    </div>
</div>
</#macro>

<#macro script>
<script src="/assets/js/mp3encoder.min.js"></script>
<script src="/assets/js/main.js"></script>
</#macro>