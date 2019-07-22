<shiro:hasRole name="treebuilder">
  <span class="toggleNext"><i class="fa fa-edit"></i><i class="fa fa-edit" style="display: none"></i></span>

  <div class="panel panel-primary" style="display: none">
    <div class="panel-heading">
      Edit Comment
    </div>

    <div class="panel-body">
      <g:if test="${notes.comment}">
        <g:form action="editInstanceNote" controller="instance">
          <g:hiddenField name="id" value="${notes.comment.id}"/>

          <div class="text-warning"><i
              class="fa fa-warning"></i> There is no undo. You can't get back a deleted comment.</div>

          <label>Comment <span
              class="small text-muted">updated <date>${notes.comment.updatedAt}</date>
          </span>
            <g:textField name="value" value="${notes.comment.value}" class="form-control"/>
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
                onclick="clearValue(this);">Confirm delete <i class="fa fa-trash"></i>
            </button>
            <button type="button"
                    class="btn right"
                    name="cancel_delete"
                    title="Cancel delete"
                    onclick="cancelDelete(this);">Cancel delete
            </button>
          </div>
        </g:form>
      </g:if>
      <g:else>No comment.</g:else>
    </div>

    <div class="panel-heading">
      Edit Distribution
    </div>

    <div class="panel-body">
      <g:if test="${notes.dist}">
        <g:form action="editInstanceNote" controller="instance" class="form-horizontal">
          <g:hiddenField name="id" value="${notes.dist.id}"/>

          <div class="text-warning"><i
              class="fa fa-warning"></i> There is no undo. You can't get back a deleted distribution.</div>

          <label>Distribution <span
              class="small text-muted">updated <date>${notes.dist.updatedAt}</date>
          </span>
            <g:textField name="value" value="${notes.dist.value}" class="form-control"/>
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
                onclick="clearValue(this);">Confirm delete <i class="fa fa-trash"></i>
            </button>
            <button type="button"
                    class="btn right"
                    name="cancel_delete"
                    title="Cancel delete"
                    onclick="cancelDelete(this);">Cancel delete
            </button>
          </div>
        </g:form>
      </g:if>
      <g:else>No Distribution.</g:else>
    </div>
  </div>
</shiro:hasRole>
