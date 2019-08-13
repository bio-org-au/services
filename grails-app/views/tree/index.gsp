<!DOCTYPE html>
<html>
<head>
  <meta name="layout" content="main">
  <title>Tree Services</title>
  <asset:stylesheet src="tree.css"/>

</head>

<body>
<div class="rest-resource-content tree-gsp">
  <h1>Classifications</h1>

  <div>
    <p>A Classification is an arrangement of taxon in a tree structure. A tree structure arranges
    taxa in a parent/child relationship. The tree has elements that hold the position of a Taxon Concept, and it's metadata
    in an arrangement of taxon that we refer to generically as a tree.</p>

    <p>A tree changes over time as our understanding of the taxa changes, so a tree can have many versions.
    Versions of a tree are Publications that can be referenced or cited. You can cite a published tree (version) using an
    identifier for the tree version or using an identifier for a tree element. This will return the element and
    then entire tree in the context of the published tree.

    Each version of a tree is immutable, i.e it doesn't change. You can cite a tree element with confidence that it will
    not change over time, while being able to trace the history all the way to the current placement.</p>
  </div>

  <!-- Modals -->
  <div class="modal fade" id="editTree" tabindex="-1" role="dialog" aria-labelledby="editTreeLabel" aria-hidden="true">
    <div class="modal-dialog" role="document">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title" id="editTreeLabel">Tree details</h5>
          <button type="button" class="close" data-dismiss="modal" aria-label="Close">
            <span aria-hidden="true">&times;</span>
          </button>
        </div>

        <form id="treeDetailsForm">
          <div class="modal-body">
            <input type="hidden" id="treeId" name="treeid"/>
            <label>Tree Name
              <input class="form-control" id="treeName" name="treeName" type="text"
                     placeholder="Display name of the tree"/>
            </label>

            <div>
              <label class="form-inline">Accepted classification
                <input class="form-control" id="acceptedTree" name="acceptedTree" type="checkbox"/>
              </label>
            </div>
            <label>HTML Description
              <textarea class="form-control" id="descriptionHtml" name="descriptionHtml"
                        placeholder="A description of this tree"></textarea>
            </label>
            <label>Link to home page
              <input class="form-control" id="linkToHomePage" name="linkToHomePage" type="text"
                     placeholder="Link to a web page about this classification."/>
            </label>

            <label>Editor Group
              <input class="form-control" id="groupName" name="groupName" type="text"
                     placeholder="A group name or blank for just you."/>
            </label>

            <label>Reference
              <input type="text" id="publication" name="publication" placeholder="For in-reference classifications."
                     class="form-control suggest" data-subject="publication" data-quoted="yes"/>
            </label>
          </div>

          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-dismiss="modal">Don't do it. <i
                class="fa fa-hand-o-left"></i></button>
            <button type="submit" class="btn btn-primary">Save changes  <i class="fa fa-refresh"></i></button>
          </div>
        </form>
      </div>
    </div>
  </div>

  <div class="modal fade" id="editDraft" tabindex="-1" role="dialog" aria-labelledby="editDraftLabel"
       aria-hidden="true">
    <div class="modal-dialog" role="document">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title" id="editDraftLabel">Draft details</h5>
          <button type="button" class="close" data-dismiss="modal" aria-label="Close">
            <span aria-hidden="true">&times;</span>
          </button>
        </div>

        <form id="draftDetailsForm">
          <div class="modal-body">
            <input type="hidden" id="vtreeId" name="treeId"/>
            <input type="hidden" id="versionId" name="versionId"/>
            <input type="hidden" id="fromVersionId" name="fromVersionId"/>

            <label>Draft Name
              <input class="form-control" id="draftName" name="draftName" type="text"
                     placeholder="e.g. November 2022"/>
            </label>

            <div>
              <label class="form-inline">Default draft
                <input class="form-control" id="defaultDraft" name="defaultDraft" type="checkbox"/>
              </label>
            </div>
          </div>

          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-dismiss="modal">Don't do it. <i
                class="fa fa-hand-o-left"></i></button>
            <button type="submit" class="btn btn-primary">Save changes  <i class="fa fa-refresh"></i></button>
          </div>
        </form>
      </div>
    </div>
  </div>

  <div class="modal fade" id="publishDraft" tabindex="-1" role="dialog" aria-labelledby="publishDraftLabel"
       aria-hidden="true">
    <div class="modal-dialog" role="document">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title" id="publishDraftLabel">Publish Draft</h5>
          <button type="button" class="close" data-dismiss="modal" aria-label="Close">
            <span aria-hidden="true">&times;</span>
          </button>
        </div>

        <form id="publishDraftForm">
          <div class="modal-body">
            <input type="hidden" id="draftVersionId" name="versionId"/>
            <label>Log entry
              <textarea class="form-control" id="logEntry" name="logEntry"
                        placeholder="Log the work done for this release."></textarea>
            </label>
          </div>

          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-dismiss="modal">Don't do it. <i
                class="fa fa-hand-o-left"></i></button>
            <button type="submit" class="btn btn-primary">Publish <i class="fa fa-refresh"></i></button>
          </div>
        </form>
      </div>
    </div>
  </div>

  <div class="modal fade" id="deleteDraft" tabindex="-1" role="dialog" aria-labelledby="deleteDraftLabel"
       aria-hidden="true">
    <div class="modal-dialog" role="document">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title" id="deleteDraftLabel">Delete Draft</h5>
          <button type="button" class="close" data-dismiss="modal" aria-label="Close">
            <span aria-hidden="true">&times;</span>
          </button>
        </div>

        <form id="deleteDraftForm">
          <div class="modal-body">
            <input type="hidden" id="deleteUrl" name="deleteUrl"/>

            <p>Are you sure you want to <strong>delete</strong> draft: <span id="deleteDraftName"></span></p>

            <p class="text-danger">You can't undo this. All changes will be lost.</p>

            <p>This may take a little time while we clean things up...</p>
          </div>

          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-dismiss="modal">Don't do it. <i
                class="fa fa-hand-o-left"></i></button>
            <button type="submit" class="btn btn-primary">Delete <i class="fa fa-trash"></i></button>
          </div>
        </form>
      </div>
    </div>
  </div>


  <h2>Available Trees</h2>
  <g:if test="${trees.empty}">
    <p>No trees currently listed in this service.</p>
  </g:if>
  <g:else>
    <div class="row">
      <g:each in="${trees}" var="tree">
        <div class="col-2">
          <st:preferredLink target="${tree}"><b>${tree.name}</b></st:preferredLink>
          <shiro:hasRole name="${tree.groupName}">
            <button type="button" class="btn btn-primary" data-toggle="modal"
                    data-target="#editTree"
                    data-name="${tree.name}"
                    data-group="${tree.groupName}"
                    data-desc="${tree.descriptionHtml}"
                    data-link="${tree.linkToHomePage}"
                    data-accepted="${tree.acceptedTree}"
                    data-ref="${tree.referenceId}"
                    data-tree="${tree.id}"
                    title="Edit tree details.">
              <i class="fa fa-edit"></i>
            </button>
            <g:if test="${!tree.currentTreeVersion}">
              <a href="${createLink(namespace: 'api', controller: 'tree', action: 'deleteTree', params: [treeId: tree.id])}">delete</a>
            </g:if>
          </shiro:hasRole>
          <br>
          (${tree.groupName})
          <br>
          <g:if test="${tree.acceptedTree}">
            <apc title="Accepted Tree"><i class="fa fa-check"></i>${tree.name}</apc>
          </g:if>
        </div>
        <div class="col-10">
          <div>${raw(tree.descriptionHtml)}</div>

          <div>
            <strong>Info:</strong>
            <g:if test="${tree.currentTreeVersion}">
              ${tree.currentTreeVersion?.draftName} published ${tree.currentTreeVersion?.publishedAt?.dateString} by ${tree.currentTreeVersion?.publishedBy}.
            </g:if>
            <g:else>
              Not currently published.
            </g:else>
          </div>

          <div>
            <strong>Web page:</strong> <a href="${tree.linkToHomePage}">${tree.linkToHomePage}</a>
          </div>

          <div>
            <strong>Log entry:</strong> ${tree.currentTreeVersion?.logEntry}
          </div>

          <div>
            <strong>drafts:</strong>
            <tree:drafts tree="${tree}">
              <div class="row">
                <span
                    class="cell">${raw(defaultDraft ? '<i class="fa fa-check"></i>' : '')} ${tree.name}: ${draft.draftName}</span>
                <span class="cell">&nbsp;
                  <shiro:hasRole name="${tree.groupName}">
                    <button type="button" class="btn btn-primary" data-toggle="modal"
                            data-target="#editDraft"
                            data-version-id="${draft.id}"
                            data-name="${draft.draftName}"
                            data-default-draft="true"
                            title="Edit draft ${draft.draftName}.">
                      edit
                    </button>
                    <button type="button" class="btn btn-danger" data-toggle="modal"
                            data-target="#deleteDraft"
                            data-url="${createLink(namespace: 'api', controller: 'treeVersion', action: 'delete', id: draft.id)}"
                            data-draft-name="${draft.draftName}"
                            title="Delete Draft ${draft.draftName}.">
                      delete
                    </button>
                    <button type="button" class="btn btn-primary" data-toggle="modal"
                            data-target="#publishDraft"
                            data-version-id="${draft.id}"
                            data-default-draft="true"
                            title="Publish Draft ${draft.draftName}.">
                      publish
                    </button>
                    <g:if test="${tree.currentTreeVersion}">
                      <a href="${createLink(namespace: 'api', controller: 'treeVersion', action: 'diff', params: [v1: tree.currentTreeVersion.id, v2: draft.id])}">diff</a>,
                    </g:if>
                    <a href="${createLink(namespace: 'api', controller: 'treeVersion', action: 'validate', params: [version: draft.id])}">validate</a>,
                    <a href="${createLink(namespace: 'api', controller: 'tree', action: 'eventReport', params: [treeId: tree.id])}">events</a>
                  </shiro:hasRole>
                </span>
              </div>
            </tree:drafts>

            <shiro:hasRole name="${tree.groupName}">
              <button type="button" class="btn btn-primary" data-toggle="modal"
                      data-target="#editDraft"
                      data-tree-id="${tree.id}"
                      data-default-draft="true"
                      title="Make a draft tree. (This can take a while)">
                Add a draft of ${tree.name} <i class="fa fa-plus"></i>
              </button>
            </shiro:hasRole>
          </div>
          <hr>
        </div>
      </g:each>
    </div>
  </g:else>
  <shiro:hasRole name="treebuilder">
    <button type="button" class="btn btn-primary" data-toggle="modal"
            data-target="#editTree"
            title="Create a new tree.">
      Create a tree <i class="fa fa-plus"></i>
    </button>
  </shiro:hasRole>

</div>
</body>
</html>

