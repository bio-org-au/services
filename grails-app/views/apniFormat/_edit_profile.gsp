<shiro:hasRole name="treebuilder">
  <span class="toggleNext"><i class="fa fa-edit"></i><i class="fa fa-edit" style="display: none"></i></span>

  <div class="panel panel-primary" style="display: none">
    <div class="panel-heading">
      Edit Comment
    </div>

    <div class="panel-body">
      <g:form action="editComment" controller="treeElement">
        <g:hiddenField name="taxonUri" value="${tve.elementLink}"/>
        <label>History:</label>
        <ul>
          <af:previousComments tve="${tve}">
            <li><undo><undo-value>${note.value ?: '(blank)'}</undo-value> <span
                class="small text-muted">updated <date>${note.updated_at}</date> - ${note.errata_reason ?: 'original'}
            </span>
            </undo></li>
          </af:previousComments>
        </ul>
        <af:treeComment tve="${tve}" showEmpty="${true}" createIfNull="${true}">
          <label>Comment <span
              class="small text-muted">updated <date>${note.updated_at}</date> - ${note.errata_reason ?: 'original'}
          </span>
            <g:textField name="comment" value="${note.value}" class="form-control"/>
          </label>
          <label>Reason
          <g:textField name="reason" value="Errata, typographic error" class="form-control"/>
          </label>
          <g:submitButton name="Update" class="btn btn-success"/>

          <button type="button"
                  class="btn btn-warning right"
                  name="delete_comment"
                  title="delete this comment"
                  onclick="confirmDelete(this);">Delete comment
          </button>

          <div style="display: none">
            <button
                class="btn btn-danger left"
                name="confirm_delete"
                title="confirm delete this comment"
                onclick="clearComment(this);">Confirm delete <i class="fa fa-trash"></i>
            </button>
            <button type="button"
                    class="btn right"
                    name="cancel_delete"
                    title="Cancel delete"
                    onclick="cancelDelete(this);">Cancel delete
            </button>
          </div>
        </af:treeComment>
      </g:form>
    </div>

    <div class="panel-heading">
      Edit Distribution
    </div>

    <div class="panel-body">
      <g:form action="editDistribution" controller="treeElement" class="form-horizontal">
        <g:hiddenField name="taxonUri" value="${tve.elementLink}"/>
        <label>History:</label>
        <ul>
          <af:previousDistribution tve="${tve}">
            <li><undo><undo-value>${note.value ?: '(blank)'}</undo-value> <span
                class="small text-muted">updated <date>${note.updated_at}</date> - ${note.errata_reason ?: 'original'}
            </span>
            </undo></li>
          </af:previousDistribution>
        </ul>
        <af:treeDistribution tve="${tve}" showEmpty="${true}" createIfNull="${true}">
          <label>Distribution <span
              class="small text-muted">updated <date>${note.updated_at}</date> - ${note.errata_reason ?: 'original'}
          </span>
            <g:textField name="distribution" value="${note.value}" class="form-control"/>
          </label>
          <label>Reason
          <g:textField name="reason" value="Errata, typographic error" class="form-control"/>
          </label>
          <g:submitButton name="Update" class="btn btn-success"/>

          <button type="button"
                  class="btn btn-warning right"
                  name="delete_distribution"
                  title="delete this distribution"
                  onclick="confirmDelete(this);">Delete distribution
          </button>

          <div style="display: none">
            <button
                class="btn btn-danger left"
                name="confirm_delete"
                title="confirm delete this distribution"
                onclick="clearDistribution(this);">Confirm delete <i class="fa fa-trash"></i>
            </button>
            <button type="button"
                    class="btn right"
                    name="cancel_delete"
                    title="Cancel delete"
                    onclick="cancelDelete(this);">Cancel delete
            </button>
          </div>
        </af:treeDistribution>
      </g:form>
    </div>
  </div>
</shiro:hasRole>
