<!DOCTYPE html>
<head>
  <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <title>${st.scheme()} <g:layoutTitle default="${st.pageTitle()}"/></title>
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <link rel="shortcut icon" href="${assetPath(src: 'gears.png')}?v=2.1">

  <meta name="description" content="${st.pageTitle()} biodiversity taxonomy services"/>
  <meta property="og:title" content="${st.pageTitle()}"/>
  <meta property="og:description" content="Biodiversity taxonomy services providing bibiliography and classification data in ${params.product}"/>
  <meta property="og:url" content="${request.getContextPath()}"/>
  <meta property="og:image" content="${asset.assetPath(src:"${st.bannerImage().toString()}")}"/>

  <script type="application/javascript">
    baseContextPath = "${request.getContextPath()}";
  </script>

  <link rel="stylesheet" href="//maxcdn.bootstrapcdn.com/font-awesome/4.3.0/css/font-awesome.min.css">
  <asset:stylesheet src="application.css"/>
  <asset:stylesheet src="application.css" media="print"/>

  <asset:javascript src="application.js"/>
  <!--[if lt IE 9 ]>
  <style>
  @media(min-width:992px) {
    div.logo img {
        width: 50px;
    }
  }
  </style>
  <script type="application/javascript">
     internetExplorer = "<9";
  </script>
  <![endif]-->
  <!--[if IE 9 ]>
  <script type="application/javascript">
     internetExplorer = "9";
  </script>
  <![endif]-->
  <g:layoutHead/>
</head>

<body class="${st.scheme()}">

<g:render template="/common/service-navigation"/>

<st:systemNotification/>
<div id="main-content" class="container-fluid">
  <div class="row">
    <div class="col-sm-12 col-md-12 col-lg-12">
      <g:layoutBody/>
    </div>
  </div>
</div>

<div id="page-footer" class="footer">
  <div id="page-footer-inner">
    <span id="page-footer-inner-left" class="pull-left">
      Supported by
      <a href="http://www.anbg.gov.au/chah/">
        <asset:image src="CHAH-logo.png" height="42"/>
      </a>
    </span>
    <span id="page-footer-inner-right" class="pull-right">
      <a href="https://twitter.com/aubiodiversity" class="twitter-link"><i class="fa fa-twitter fa-2x"
                                                                           style="color: #55acee"></i>
      </a>
    </span>

    <div>
    </div>
    <!-- Version: <g:meta name="app.version"/> -->

    <div id="spinner" class="spinner" style="display:none;"><g:message code="spinner.alt"
                                                                       default="Loading&hellip;"/></div>

    <st:googleAnalytics/>

</body>
</html>
