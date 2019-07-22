<!DOCTYPE html>
<!--[if lt IE 7 ]> <html lang="en" class="no-js ie6"> <![endif]-->
<!--[if IE 7 ]>    <html lang="en" class="no-js ie7"> <![endif]-->
<!--[if IE 8 ]>    <html lang="en" class="no-js ie8"> <![endif]-->
<!--[if IE 9 ]>    <html lang="en" class="no-js ie9"> <![endif]-->
<!--[if (gt IE 9)|!(IE)]><!--> <html lang="en" class="no-js"><!--<![endif]-->
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
  <title><g:layoutTitle default="Grails"/></title>
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <link rel="shortcut icon" href="${assetPath(src: 'gears.png')}?v=2.1">
  <script type="application/javascript">
    baseContextPath = "${request.getContextPath()}";
  </script>
  <asset:stylesheet src="application.css"/>
  <asset:javascript src="application.js"/>
  <!-- not the ideal way to include angular, but this will do for now -->
  <g:layoutHead/>
</head>

<body style="position: absolute; top: 0; bottom: 0; left: 0; right: 0;">
<div id="grailsLogo" role="banner"><a href="/"><asset:image src="global.logo.gif" alt="Biodiversity.org.au"/></a></div>

<div class="container" style="position: absolute; top: 60px; bottom: 3em; left: 8px; right: 8px;">
  <g:layoutBody/>
</div>

<div class="footer" role="contentinfo" style="position: absolute; bottom: 0; background-color: #E0F0E0;">foot</div>

<div id="spinner" class="spinner" style="display:none;"><g:message code="spinner.alt" default="Loading&hellip;"/>spin</div>
</body>
</html>
