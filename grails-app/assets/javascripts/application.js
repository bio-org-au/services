// This is a manifest file that'll be compiled into application.js.
//
// Any JavaScript file within this directory can be referenced here using a relative path.
//
// You're free to add application-wide JavaScript to this file, but it's generally better 
// to create separate JavaScript files as needed.
//
//= require jquery
//= require js/jquery-ui-1.10.4.custom
//= require jquery.cookie
//= require bootstrap
//= require apniFormat
//= require jquery-dateformat.min
//= require_self

var pendingReload = false;

function log(message) {
  window.console && console.log(message);
}

if (typeof jQuery !== 'undefined') {
  log("Yes we have JQuery");
  (function ($) {
    $('#spinner').ajaxStart(function () {
      $(this).fadeIn();
    }).ajaxStop(function () {
      $(this).fadeOut();
    });

  })(jQuery);
}

function editTreeData(event) {
  event.preventDefault();
  if (!pendingReload) {
    log("in edit tree data");
    var form = $("#treeDetailsForm");
    var data = {
      id: form.find('#treeId').val(),
      treeName: form.find('#treeName').val(),
      groupName: form.find('#groupName').val(),
      publication: form.find('#publication').val(),
      descriptionHtml: form.find('#descriptionHtml').val(),
      linkToHomePage: form.find('#linkToHomePage').val(),
      acceptedTree: form.find('#acceptedTree').prop("checked")
    };

    if (data.id && data.id !== "") {
      send(data, 'POST', baseContextPath + "/api/tree/editTree");
    } else {
      send(data, 'PUT', baseContextPath + "/api/tree/createTree");
    }
  }
}

function editDraftData(event) {
  event.preventDefault();
  if (!pendingReload) {
    var theForm = $(event.relatedTarget);
    log("in edit draft data");
    var form = $("#draftDetailsForm");
    var data = {
      treeId: form.find('#vtreeId').val(),
      versionId: form.find('#versionId').val(),
      fromVersionId: form.find('#fromVersionId').val(),
      draftName: form.find('#draftName').val(),
      defaultDraft: form.find('#defaultDraft').prop("checked")
    };

    if (data.versionId && data.versionId !== "") {
      send(data, "POST", baseContextPath + "/api/treeVersion/edit");
    } else {
      send(data, 'PUT', baseContextPath + "/api/tree/createVersion");
    }

  }
}

function publishDraftData(event) {
  event.preventDefault();
  if (!pendingReload) {
    log("in publish draft data");
    var form = $("#publishDraftForm");
    var data = {
      versionId: form.find('#draftVersionId').val(),
      logEntry: form.find('#logEntry').val()
    };

    send(data, "POST", baseContextPath + "/api/treeVersion/publish");
  }
}

function deleteDraftData(event) {
  event.preventDefault();
  if (!pendingReload) {
    log("in delete draft data");
    var form = $("#deleteDraftForm");
    var deleteUrl = form.find('#deleteUrl').val();

    disableForms();
    $.ajax({
      method: "POST",
      url: deleteUrl
    }).done(function () {
      location.reload(true);
    }).fail(function (jqxhr, statusText) {
      if (jqxhr) {
        if (jqxhr.status === 403) {
          log("status 403 forbidden");
          alert("Apparently you're not allowed to do that.");
        } else if (jqxhr.responseJSON) {
          log("Fail: " + statusText + ", " + jqxhr.responseJSON.error);
          alert('Failed to delete: ' + jqxhr.responseJSON.error);
        } else if (jqxhr.responseText) {
          log("Fail: " + statusText + ", " + jqxhr.responseText);
          alert("Failed to delete with status " + statusText + ". " + jqxhr.responseText);
        } else if (jqxhr.responseXML) {
          log("Fail: " + statusText + ", " + jqxhr.responseText);
          alert("Failed to delete with status " + statusText + ". XML:" + jqxhr.responseXML);
        }
      }
      enableForms();
    });
  }
}

function disableForms() {
  pendingReload = true;
  $('button').attr('disabled', 'disabled');
  $("button[type='submit'] i.fa").addClass('fa-spin');
}

function enableForms() {
  pendingReload = false;
  $('button').removeAttr('disabled');
  $("button[type='submit'] i.fa").removeClass('fa-spin');
}

function send(data, method, url) {
  disableForms();
  $.ajax({
    method: method,
    url: url,
    data: JSON.stringify(data),
    contentType: "application/json",
    dataType: "json"
  }).done(function () {
    location.reload(true);
  }).fail(function (jqxhr, statusText) {
    if (jqxhr.status === 403) {
      log("status 403 forbidden");
      alert("Apparently you're not allowed to do that.");
    } else if (jqxhr.responseJSON) {
      log("Fail: " + statusText + ", " + jqxhr.responseJSON.error);
      alert(jqxhr.responseJSON.error);
    } else if (jqxhr.responseText) {
      log("Fail: " + statusText + ", " + jqxhr.responseText);
      alert("That didn't work: " + statusText + ". " + jqxhr.responseText);
    }
    enableForms();
  });
}

function replaceDates() {
  $('date').each(function (element) {
    var d = $(this).html().toString();
    $(this).html(jQuery.format.prettyDate(d));
    $(this).attr('title', d);
  });
}

$(function () {

  function worker() {
    var logs = $('body').find('#logs').length;
    if (logs == 1) {
      $.ajax({
        url: 'logs',
        success: function (data, statusText, jqXHR) {
          //note redirects will be followed
          if (jqXHR.status == 200) {
            $('#logs').html(data);
            setTimeout(worker, 5000);
          } else {
            location.reload(true);
          }
        }
      });
    }
  }

  worker();


  $('#productDescription').on('closed.bs.alert', function () {
    var cookieName = 'close' + $(this).data('product') + 'Description';
    $.cookie(cookieName, 'true', {expires: 30});
  }).each(function () {
    var cookieName = 'close' + $(this).data('product') + 'Description';
    var closeProductDescription = $.cookie(cookieName);
    if (closeProductDescription) {
      $('#productDescription').alert('close');
    }
  });

  $('help').click(function (event) {
    $(this).children('div').toggle();
    event.preventDefault();
  });

  $('input.fromDate').datepicker({dateFormat: 'd/m/yy', defaultDate: '-1w', maxDate: 0});
  $('input.toDate').datepicker({dateFormat: 'd/m/yy', maxDate: 0});

  $('#editTree').on('show.bs.modal', function (event) {
    var button = $(event.relatedTarget);
    var name = button.data('name');
    var group = button.data('group');
    var ref = button.data('ref');
    var desc = button.data('desc');
    var link = button.data('link');
    var accepted = button.data('accepted');
    var treeId = button.data('tree');
    var title = button[0].title;

    var modal = $(this);
    modal.find('#treeId').val(treeId);
    modal.find('#treeName').val(name);
    modal.find('#groupName').val(group);
    modal.find('#publication').val(ref);
    modal.find('#descriptionHtml').val(desc);
    modal.find('#linkToHomePage').val(link);
    if (accepted) {
      modal.find('#acceptedTree').prop("checked", "checked");
      modal.find('#acceptedTree').attr("checked", "checked");
    } else {
      modal.find('#acceptedTree').removeAttr("checked");
    }
    modal.find('.modal-title').text(title);
  });


  $('#editDraft').on('show.bs.modal', function (event) {
    var button = $(event.relatedTarget);
    var name = button.data('name');
    var defaultDraft = button.data('default-draft');
    var treeId = button.data('tree-id');
    var versionId = button.data('version-id');
    var fromVersionId = button.data('from-version-id');
    var title = button[0].title;

    var modal = $(this);
    if (defaultDraft) {
      modal.find('#defaultDraft').prop("checked", "checked");
      modal.find('#defaultDraft').attr("checked", "checked");
    } else {
      modal.find('#defaultDraft').removeAttr("checked");
    }
    modal.find('#draftName').val(name);
    modal.find('#vtreeId').val(treeId);
    modal.find('#versionId').val(versionId);
    modal.find('#fromVersionId').val(fromVersionId);
    modal.find('.modal-title').text(title);
  });

  $('#publishDraft').on('show.bs.modal', function (event) {
    var button = $(event.relatedTarget);
    var versionId = button.data('version-id');
    var title = button[0].title;

    var modal = $(this);
    modal.find('#draftVersionId').val(versionId);
    modal.find('.modal-title').text(title);
  });

  $('#deleteDraft').on('show.bs.modal', function (event) {
    var button = $(event.relatedTarget);
    var url = button.data('url');
    var draftName = button.data('draft-name');
    var title = button[0].title;

    var modal = $(this);
    modal.find('#deleteUrl').val(url);
    modal.find('#deleteDraftName').text(draftName);
    modal.find('.modal-title').text(title);
  });


  $('#treeDetailsForm').on('submit', function (event) {
    editTreeData(event);
  });
  $('#draftDetailsForm').on('submit', function (event) {
    editDraftData(event);
  });
  $('#publishDraftForm').on('submit', function (event) {
    publishDraftData(event);
  });
  $('#deleteDraftForm').on('submit', function (event) {
    deleteDraftData(event);
  });

  replaceDates();

  // merge stuff
  $('div.tve-diff-from label').each(function () {
    var parentid = $(this).data('parentid');
    if (parentid !== undefined && parentid !== "") {
      $(this).addClass('disabled').find('input').attr('disabled', 'disabled');
    }
    $(this).find('input').on('click', function () {
      var parentid = $(this).data('diffid');
      console.log('Clicked from ' + parentid);
      var sel = 'div.tve-diff-from label[data-parentid="' + parentid + '"]';
      $(sel).removeClass('disabled').find('input').removeAttr('disabled').click();
    });
  });

  $('div.tve-diff-to label input').on('click', function () {
    var parentid = $(this).data('diffid');
    console.log('Clicked to ' + parentid);
    $('div.tve-diff-from label[data-parentid="' + parentid + '"]')
      .addClass('disabled')
      .find('input')
      .attr('disabled', 'disabled');
    $('div.tve-diff-to label[data-parentid="' + parentid + '"] input').click();
  });


});
