<g:form name="search" role="form" controller="dashboard" method="GET">
    <div class="form-group">
        <div class="form-row">
            <div class="col-md-3">
                <label>User name
                    <input type="text" name="userName" placeholder="Enter a user name" value="${query.userName}"
                           class="form-control" size="30"/>
                </label>
            </div>
            <div class="col-md-2">
                <label>From
                    <input type="text" name="fromStr" class="form-control fromDate" value="${query.fromStr}">
                </label>
            </div>
            <div class="col-md-2">
                <label>To
                    <input type="text" name="toStr" class="form-control toDate" value="${query.toStr}">
                </label>
            </div>

            <div class="col-md-2">
                <label for="filterBy">Show only
                <g:set var="fopts" value="[all: 'All', name: 'Names', instance: 'Instances', reference: 'References', author: 'Authors', tree_element: 'Tree Elements', comment: 'Comments']"/>
                <g:select name="filterBy" from="${fopts}" optionKey="key" optionValue="value" id="filter-by" type="text" class="form-control filterBy" value="${query.filterBy}"/>
                </label>
            </div>
            <div class="col-md-1">
                <label for="search">
                    <g:actionSubmit type="submit" name="search" value="Search" class="btn btn-primary audit-search" action="audit"/>
                </label>
            </div>
            <div class="col-md-1">
                <label for="search">
                    <g:actionSubmit type="submit" name="stats" value="Stats" class="btn btn-primary audit-search" action="stats"/>
                </label>
            </div>

        </div>
    </div>
</g:form>
