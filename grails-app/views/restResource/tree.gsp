<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <title>Tree ${treeVersion.tree.name}</title>
    <asset:stylesheet src="tree.css"/>

</head>

<body>
<div class="container-fluid tree-gsp">
    <div class="row">

        <h1>Tree <help>
            <i class="fa fa-info-circle"></i>

            <div>
                <p>A tree is a classification tree structure. The tree has tree_elements that hold the position of a Taxon Concept
                in an arrangement of taxon that we refer to generically as a tree.</p>

                <p>A tree structure can change, so a tree contains many versions. Each version of a tree is immutable, i.e it
                doesn't change. You can cite a tree element with confidence that the thing you cite will not change over time,
                while being able to trace the history all the way to the current placement.</p>
                <ul>
                    <li>At the bottom of this page are the citable links to this Instance object or just use the <i
                            class="fa fa-link"></i> icon.
                    You can "right click" in most browsers to copy it or open it in a new browser tab.</li>
                </ul>
            </div>
        </help>
        </h1>

        <g:set var="currentTreeVersion" value="${treeVersion.tree.currentTreeVersion}"/>
    </div>

    <div class="row">
        <div class="col">
            <div>
                <g:if test="${treeVersion == currentTreeVersion}">
                    <h3>${treeVersion.tree.name} <span class="text-muted">(${treeVersion.id})</span></h3>
                </g:if>
                <g:elseif test="${!treeVersion.published}">
                    <h3><span class="draftStamp"></span> Draft Version of ${treeVersion.tree.name}</h3>

                    <div style="display: inline-block">
                        This is a draft version of APC. The <b>current version</b> is
                    <st:preferredLink target="${currentTreeVersion}"
                                      useButton="${true}">${currentTreeVersion.id}</st:preferredLink>
                    </div>
                </g:elseif>
                <g:else>
                    <h3>Version ${treeVersion.id} of ${treeVersion.tree.name} (OLD)</h3>

                    <div style="display: inline-block">
                        This is an old version of APC. The current version is
                        <st:preferredLink target="${currentTreeVersion}"
                                          useButton="${true}">${currentTreeVersion.id}</st:preferredLink>
                    </div>
                </g:else>
                <div style="display: inline-block">
                    <tree:versionStats version="${treeVersion}">
                        ${elements} elements
                    </tree:versionStats>

                    <g:if test="${treeVersion.published}">
                        <st:preferredLink target="${treeVersion}"
                                          useButton="${true}">
                            published ${treeVersion.publishedAt.dateString} by ${treeVersion.publishedBy}
                        </st:preferredLink>
                    </g:if>
                    <g:else>NOT PUBLISHED</g:else>
                </div>

                <h4>Notes</h4>

                <p>${treeVersion.logEntry}</p>
            </div>

            <div>
                <h3>Other versions</h3>

                <p>Below are all revisions of the ${treeVersion.tree.name}. Versions older than
                <st:preferredLink
                        target="${currentTreeVersion}"
                        useButton="${true}">${currentTreeVersion.id} published ${currentTreeVersion.publishedAt.dateString}</st:preferredLink>
                are for reference only.</p>
                <table class="table">
                    <tr><th>Version</th><th>published</th><th>Notes</th><th>Remove action</th></tr>
                    <g:each in="${versions}" var="version">
                        <tr>
                            <td><st:preferredLink target="${version}">${version.id}</st:preferredLink></td>
                            <td>${version.published ? version.publishedAt.dateString : 'DRAFT'}</td>
                            <td>${version.published ? version.logEntry : version.draftName}</td>
                            <td>
                                <g:if test="${currentTreeVersion}">
                                    <g:if test="${version.id != currentTreeVersion.id}">
                                        <g:if test="${version.published}">
                                            <a href="${createLink(namespace: 'api', controller: 'treeVersion', action: 'diff', params: [v1: version.id, v2: currentTreeVersion.id])}"
                                               title="Diff to current version">&Delta;&nbsp;current</a>,<g:if
                                                test="${version.previousVersion}">
                                            <a href="${createLink(namespace: 'api', controller: 'treeVersion', action: 'diff', params: [v2: version.id, v1: version.previousVersion.id])}"
                                               title="Diff to current version">&Delta;&nbsp;previous</a>,</g:if>
                                        </g:if>
                                        <g:else>
                                            <a href="${createLink(namespace: 'api', controller: 'treeVersion', action: 'diff', params: [v1: currentTreeVersion.id, v2: version.id])}"
                                               title="Diff from current version">&Delta;&nbsp;current</a>,
                                            <a href="${createLink(namespace: 'api', controller: 'tree', action: 'checkCurrentSynonymy', params: [treeVersionId: version.id])}"
                                               title="Check Events">synonymy</a>,
                                            <a href="${createLink(namespace: 'api', controller: 'treeVersion', action: 'mergeReport', params: [draftId: version.id])}"
                                               title="Check Events">merge</a>,
                                        </g:else>
                                    </g:if>
                                </g:if>
                                <a href="${createLink(namespace: 'api', controller: 'treeVersion', action: 'validate', params: [version: version.id])}">validate</a>
                            </td>
                        </tr>
                    </g:each>
                </table>
            </div>

        </div>

        <div class="col indented">
            <g:each in="${children}" var="childElement">
                <div class="tr ${childElement.excluded ? 'excluded' : ''} level${childElement.depth}">
                    <div class="wrap">
                        <a href="${childElement.elementLink}">${raw(childElement.displayHtml)}</a>
                    </div>
                </div>
            </g:each>
        </div>
    </div>
</div>
</body>
</html>
