<!DOCTYPE html>
<head>
  <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <title>${st.scheme()} <g:layoutTitle default="${st.pageTitle()}"/></title>
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <link rel="shortcut icon" href="${assetPath(src: 'gears.png')}?v=2.1">

  <meta name="description" content="${st.pageTitle()} biodiversity taxonomy services"/>
  <meta property="og:title" content="${st.pageTitle()}"/>
  <meta property="og:description"
        content="Biodiversity taxonomy services providing bibiliography and classification data in ${params.product}"/>
  <meta property="og:url" content="${request.getContextPath()}"/>
  <meta property="og:image" content="${asset.assetPath(src: "${st.bannerImage().toString()}")}"/>

  <script type="application/javascript">
    baseContextPath = "${request.getContextPath()}";
  </script>

  %{--Fontawesome--}%
  <link rel="stylesheet" href="https://use.fontawesome.com/releases/v5.7.2/css/all.css"
        integrity="sha384-fnmOCqbTlWIlj8LyTjo7mOUStjsKC4pOpQbqyi7RrhN7udi9RwhKkMHpvLbHG9Sr" crossorigin="anonymous">

  %{-- Bootstrap --}%
  <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css"
        integrity="sha384-ggOyR0iXCbMQv3Xipma34MD+dH/1fQ784/j6cY/iJTQUOhcWr7x9JvoRxT2MZw1T" crossorigin="anonymous">

  <asset:stylesheet src="application.css"/>
  <asset:stylesheet src="application.css" media="print"/>
  %{-- JQuery --}%
  <script
      src="https://code.jquery.com/jquery-3.4.1.min.js"
      integrity="sha256-CSXorXvZcTkaix6Yvo6HppcZGetbYMGWSFlBw8HfCJo="
      crossorigin="anonymous"></script>

  %{-- bootstrap 4.3, Popper --}%
  <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.14.7/umd/popper.min.js"
          integrity="sha384-UO2eT0CpHqdSJQ6hJty5KVphtPhzWj9WO1clHTMGa3JDZwrnQq4sF86dIHNDz0W1"
          crossorigin="anonymous"></script>
  <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/js/bootstrap.min.js"
          integrity="sha384-JjSmVgyd0p3pXB1rRibZUAYoIIy6OrQ6VrjIEaFf/nJGzIxFDsf4x0xIM+B07jRM"
          crossorigin="anonymous"></script>

  %{-- jQuery UI --}%
  <script
      src="https://code.jquery.com/ui/1.12.1/jquery-ui.min.js"
      integrity="sha256-VazP97ZCwtekAsvgPBSUwPFKdrwD3unUfSGVYrahUqU="
      crossorigin="anonymous"></script>

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
  <g:layoutBody/>
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
      <a href="https://twitter.com/aubiodiversity" class="twitter-link">
        <i class="fab fa-twitter fa-2x"></i>
      </a>
    </span>

    <!-- Version: span<g:meta name="app.version"/> -->

    <div id="spinner" class="spinner" style="display:none;"><g:message code="spinner.alt"
                                                                       default="Loading&hellip;"/></div>

    <st:googleAnalytics/>
  </div>
</div>
</body>
</html>
