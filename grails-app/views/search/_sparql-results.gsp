<%@ page import="grails.converters.JSON" %>
<div class="panel panel-default hide" id="sparql-spinner-container">
    <div class="panel-heading">
        <i class="fa fa-spinner fa-spin"></i> Querying &hellip;
    </div>

    <div class="panel-body">
    </div>
</div>
<div class="panel panel-danger hide" id="sparql-error-container">
    <div class="panel-heading">
        <i class="fa fa-warning"></i> Sparql Error
    </div>

    <div class="panel-body" id="sparql-error">
    </div>
</div>
<div class="panel panel-default hide" id="sparql-result-container">
    <div class="panel-heading">
        <i class="fa fa-check"></i> Sparql Result
    </div>

    <div class="panel-body" id="sparql-result">
        <table class="table-striped">
            <thead>
                <tr id="sparql-result-head"></tr>
            </thead>
            <tbody id="sparql-result-body">

            </tbody>
        </table>
    </div>
</div>

<g:if test="${query.sparql}">
    <script type="text/javascript">
        //<![CDATA[

        var rdfUriPrefixes =  ${raw((uriPrefixes as grails.converters.JSON).toString())} ;

        function display_sparql_error(responseText) {
            clear_sparql_result();
            $("#sparql-error-container").removeClass('hide');
            $("#sparql-error").html(responseText);
        }

        function display_sparql_result(data) {
            clear_sparql_result();
            $("#sparql-result-container").removeClass('hide');

            // first, handle the head

            for(var hi in data.head.vars) {
                var h = data.head.vars[hi];
                var th = document.createElement("th");
                $("#sparql-result-head").append(th);
                $(th).text(h);
            }

            for(var ri in data.results.bindings) {

                var r = data.results.bindings[ri];
                var tr = document.createElement("tr");
                $("#sparql-result-body").append(tr);

                for(var hi in data.head.vars) {
                    var h = data.head.vars[hi];

                    var td = document.createElement("td");
                    $(tr).append(td);
                    $(td).addClass('sparql-result-cell');

                    var v = r[h];

                    if(!v) {

                    }
                    else
                    if(v.type == 'bnode') {
                        td.innerHTML =  v.value;
                        $(td).addClass('sparql-search-bnode');
                    }
                    else
                    if(v.type == 'uri') {
                        if(v.value.indexOf('http://')==0) {
                            var link = document.createElement("a");
                            td.appendChild(link);
                            link.setAttribute('target', 'sparqlref');
                            link.setAttribute('href', v.value);

                            var value = v.value;

                            for(var prefix in rdfUriPrefixes) {
                                if(prefix.length > 0
                                        && value.length > prefix.length // >, not >=, because we want at least one character
                                        && prefix == value.substr(0, prefix.length)) {
                                    value = rdfUriPrefixes[prefix] + ':' + value.substr(prefix.length);
                                    break;
                                }
                            }


                            link.appendChild(document.createTextNode(value));
                        }
                        else {
                            td.innerHTML =  v.value;
                        }
                        $(td).addClass('sparql-search-uri');
                    }
                    else
                    if (v.type == 'typed-literal') {
                        td.innerHTML = v.value;

                        switch (v.datatype) {
                            case 'http://www.w3.org/2001/XMLSchema#string' :
                                $(td).addClass('sparql-search-text-literal');
                                break;

                            case 'http://www.w3.org/2001/XMLSchema#duration' :
                            case 'http://www.w3.org/2001/XMLSchema#dateTime' :
                            case 'http://www.w3.org/2001/XMLSchema#time' :
                            case 'http://www.w3.org/2001/XMLSchema#date' :
                            case 'http://www.w3.org/2001/XMLSchema#gYearMonth' :
                            case 'http://www.w3.org/2001/XMLSchema#gYear' :
                            case 'http://www.w3.org/2001/XMLSchema#gMonthDay' :
                            case 'http://www.w3.org/2001/XMLSchema#gDay' :
                            case 'http://www.w3.org/2001/XMLSchema#gMonth' :

                            case 'http://www.w3.org/2001/XMLSchema#double' :
                            case 'http://www.w3.org/2001/XMLSchema#decimal' :
                            case 'http://www.w3.org/2001/XMLSchema#integer' :
                            case 'http://www.w3.org/2001/XMLSchema#nonPositiveInteger' :
                            case 'http://www.w3.org/2001/XMLSchema#negativeInteger' :
                            case 'http://www.w3.org/2001/XMLSchema#long' :
                            case 'http://www.w3.org/2001/XMLSchema#int' :
                            case 'http://www.w3.org/2001/XMLSchema#short' :
                            case 'http://www.w3.org/2001/XMLSchema#byte' :
                            case 'http://www.w3.org/2001/XMLSchema#nonNegativeInteger' :
                            case 'http://www.w3.org/2001/XMLSchema#positiveInteger' :
                            case 'http://www.w3.org/2001/XMLSchema#unsignedLong' :
                            case 'http://www.w3.org/2001/XMLSchema#unsignedInt' :
                            case 'http://www.w3.org/2001/XMLSchema#unsignedShort' :
                            case 'http://www.w3.org/2001/XMLSchema#unsignedByte' :
                                $(td).addClass('sparql-search-numeric-literal');
                                break;

                            default:
                                $(td).addClass('sparql-search-typed-literal');
                                break;
                        }
                    }
                    else {
                        td.innerHTML = v.value;
                        $(td).addClass('sparql-search-text-literal');
                    }
                }
            }

        }

        function clear_sparql_result() {
            $("#sparql-result-container").addClass('hide');
            $("#sparql-spinner-container").addClass('hide');
            $("#sparql-error-container").addClass('hide');
            $("#sparql-result-head").empty();
            $("#sparql-result-body").empty();
            $("#sparql-error").empty();
        }

        function sparql_spinner_on() {
            $("#sparql-spinner-container").removeClass('hide');
        }

        //]]>
    </script>
</g:if>
