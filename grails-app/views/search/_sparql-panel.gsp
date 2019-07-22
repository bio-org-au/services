<div class="panel panel-default">
    <div class="panel-heading">
        <g:if test="${query.sparql}">
            <span id="QUERY-FORM-CLOSE" onclick="hide_query_form();" style="cursor: pointer;"><i class="fa fa-caret-up"></i> Hide search form</span>
            <span id="QUERY-FORM-OPEN" onclick="show_query_form();" style="cursor: pointer; display: none"><i class="fa fa-caret-down"></i> Show search form</span>

            <script type="text/javascript">

                function hide_query_form() {
                    $('#QUERY-FORM-OPEN').show();
                    $('#QUERY-FORM-CLOSE').hide();
                    $('#QUERY-FORM').hide();
                }

                function show_query_form() {
                    $('#QUERY-FORM-CLOSE').show();
                    $('#QUERY-FORM-OPEN').hide();
                    $('#QUERY-FORM').show();
                }

            </script>
        </g:if>
    </div>



    <div class="panel-body" id="QUERY-FORM">
        <div class="container-fluid">
            <div class="col-md-8">
                <g:if test="${query.sparql}">
                <%--  we are in sparql mode - executing the client-side functionality --%>
                    <div class="form-group">
                        <label style="width: 100%;">Query
                            <help><i class="fa fa-info-circle"></i>

                                <div>
                                    <ul>
                                        <li>This is a <a href=http://www.w3.org/TR/rdf-sparql-query/">SPARQL</a> query.</li>
                                        <li>Sample queries are listed to the right.</li>
                                        <li><strong>Please use a LIMIT clause in your query.</strong></li>
                                    </ul>
                                </div>
                            </help>
                            <br/>
                            <textarea id="SPARQLQUERY" name="query" placeholder="SPARQL Query" rows="20"
                                      class="form-control ">${query.query}</textarea>
                        </label>
                    </div>

                    <div class="form-group"><div class="btn-group">
                        <g:set var="formName" value="sparql"/>
                        <button class="btn btn-primary" onclick="send_sparql_query();">Search</button>
                    </div>
                    </div>
                </g:if>


                <g:else>
                <%--  need to refetch from the server to get the sparql-mode controls --%>
                    <g:form name="search" role="form" controller="search" action="search" method="POST">
                        <div class="form-group">
                            <label style="width: 100%;">Query
                                <help><i class="fa fa-info-circle"></i>

                                    <div>
                                        <ul>
                                            <li>This is a <a href=http://www.w3.org/TR/rdf-sparql-query/">SPARQL</a> query.</li>
                                            <li>Sample queries are listed to the right.</li>
                                            <li><strong>Please use a LIMIT clause in your query.</strong></li>
                                        </ul>
                                    </div>
                                </help>
                                <br/>
                                <textarea id="SPARQLQUERY" name="query" placeholder="SPARQL Query" rows="20"
                                          class="form-control">${query.query}</textarea>
                            </label>
                        </div>

                        <div class="form-group">
                            <g:set var="formName" value="sparql"/>
                            <div class="btn-group">
                                <button type="submit" name="sparql" value="true" class="btn btn-primary">Search</button>
                            </div>
                        </div>
                    </g:form>

                </g:else>
            </div>

            <div class="col-md-4">
              <g:render template="/search/sparqlSamples/sparql-samples"/>
            </div> %{-- samples --}%
        </div>
    </div>
</div>

<script type="text/javascript">
    //<![CDATA[

    $(function() {
        $("#sample-queries-accordion").find('button').click(function(event) {
            $("#SPARQLQUERY").text($(event.target).parent().find('> pre').text())
        });
    });

    //]]>
</script>


<g:if test="${query.sparql}">
    <script type="text/javascript">
        //<![CDATA[

        // I will jam all the javascript in here for a quick win.

        $(function () {
            send_sparql_query();
        });

        function send_sparql_query() {
            var query = document.getElementById("SPARQLQUERY").value;

            clear_sparql_result();
            sparql_spinner_on();

            $.ajax("/sparql/", {
                data: {
                    output: "json",
                    query: query // encodeURIComponent(
                },
                dataType: 'json',
                cache: 'false',
                success: function (data, textStatus, jqXHR) {
                    display_sparql_result(data);
                },
                error: function (jqXHR, textStatus, errorThrown) {
                    display_sparql_error(jqXHR.responseText);
                }
            });

        }


        //]]>
    </script>
</g:if>


