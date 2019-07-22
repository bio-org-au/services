/**
 * Created by pmcneil on 17/09/14.
 * Javascript mainly to do with the apniFormat displays
 * todo maybe re-name to format.js and split out the apninFormat specific bits
 */
$(function () {
  if (typeof internetExplorer !== 'undefined') {
    $.ajaxSetup({cache: false});
  }
  var lazyLoad = function () {
    var delay = 0;
    $('div.results > div > div.unfetched.name').each(function () {
        var el = $(this);
        var getName = function () {
          var id = el.attr('id');
          var format = el.data('format');
          var product = el.data('product');
          if (format != 'noneFormat') {
            var url = format + "/name/" + id + "?product=" + product;
            window.console && console.log("found a name, getting " + url);
            $.get(url, function (data, status, request) {
              window.console && console.log('status ' + status + ', request ' + request);
              if (data !== undefined) {
                var parentDiv = el.parent('div');
                el.replaceWith(data);
                $('.toggleNext').unbind('click').click(function () {
                  toggleNext(this);
                });
                parentDiv.find("protologue-pdf").each(function () {
                  checkProtologue(this);
                });
                parentDiv.find('branch').click(function (event) {

                  if ($(event.target).is("a")) {
                    // default behaviour on a hyperlink click
                  }
                  else {
                    $(this).children('ul').toggle();
                    event.preventDefault();
                  }
                });
                parentDiv.find('date').each(function () {
                  var d = $(this).html();
                  $(this).html(jQuery.format.prettyDate(d));
                  $(this).html(jQuery.format.prettyDate(d));
                });
              }
            });
          }
        };
        window.setTimeout(getName, delay);
        delay = delay + 100;
      }
    )
    ;
  };
  window.setTimeout(lazyLoad, 100);

  $('.toggleNext').unbind('click').click(function () {
    toggleNext(this);
  });

  var toggleNext = function (el) {
    $(el).find('i').toggle();
    $(el).next('div').toggle(200);
  };

  $('.toggleNextRow').unbind('click').click(function () {
    toggleNextRow(this);
  });

  var toggleNextRow = function (el) {
    $(el).find('i').toggle();
    $(el).next('tr').toggle(200);
  };

  $(".closeForm").click(function () {
    $('.openForm').show();
    $('.closeForm').hide();
    $('.panel-body > form.closable').toggle();
  });

  $(".openForm").click(function () {
    $('.openForm').hide();
    $('.closeForm').show();
    $('.panel-body > form.closable').toggle();
  });

  $(".hideSearch").each(function () {
    $('.closeForm').addClass('highlight').focus();

    window.setTimeout(function () {
      $('.openForm').show();
      $('.closeForm').hide();
      $('.closeForm').removeClass('highlight');
      $('.panel-body > form.closable').toggle(200);
    }, 1000);
  });

  $("#expandAll").click(function () {
    //window.setTimeout(lazyLoad, 200);
    $('family').show();
    $('.instances').show();
    $('.fa-caret-down.toggleNext').hide();
    $('.fa-caret-up.toggleNext').show();
  });

  $("#collapseAll").click(function () {
    $('family').hide();
    $('.instances').hide();
    $('.fa-caret-down.toggleNext').show();
    $('.fa-caret-up.toggleNext').hide();
  });

  $(".loadFormat").click(function () {
    var url = $(this).attr('href');
    var parent = $(this).parent('div').parent('div').parent('div');
    $.get(url, function (data, status, request) {
      window.console && console.log('status ' + status + ', request ' + request);
      if (data != undefined) {
        parent.html(data);
      }
      $('.toggleNext').unbind('click').click(function () {
        toggleNext(this);
      });
    });
    return false;
  });

  $(".suggest").each(function () {
    var action = $(this).data('subject');
    var context = $(this).data('context');
    var quoted = $(this).data('quoted');
    var actionurl = function (request, response) {
      var url = baseContextPath + '/suggest/' + action + '?term=' + encodeURIComponent(request.term);
      if (context != undefined) {
        var contextElement = $("#" + context);
        var contextValue = contextElement.val();
        url += '&context=' + encodeURIComponent(contextValue);
      }
      $.get(url, function (data, status, request) {
        response(data);
      });
    };
    $(this).autocomplete({
      minLength: 1,
      source: actionurl,
      select: function (event, ui) {
        var qry = ui.item.value;
        if (quoted) {
          qry = '"' + ui.item.value + '"';
        }
        if (qry != '...') {
          $(this).val(qry);
        }
        event.cancel();
      }
    });
  });

  $(".checkbig").submit(function () {
    var data = $(this).serialize();
    if (data.length > 4096) {
      $(this).attr('method', 'POST');
    }
  });

  $("foa").each(function () {
    var url = $(this).data('id');
    window.console && console.log("found foa, getting " + url);
    $.get(url, function (data, status, request) {
      window.console && console.log('status ' + status + ', request ' + request);
      if (data != undefined) {
        var html = $.parseHTML(data);
        var content = $(html).find('div.foa-content');
        var footer = $(html).find('div.foa-footer');
        $("foa").append(content);
        $("div.foa-content").append(footer);
        $("#foaToggle").show();
      }
    });
  });

  var checkProtologue = function (el) {
    var thisEl = el;
    var url = $(el).data('id');
    var link = '<a title="Protologue PDF image" href="' + url + '"><i class="fa fa-file-pdf-o"></i></a>';
    window.console && console.log("found protologue pdf, trying " + url);
    $.ajax({
      type: "HEAD",
      async: true,
      url: url,
      success: function () {
        window.console && console.log("protologue pdf exists");
        $(thisEl).append(link);
      }
    });
  };

  $("protologue-pdf").each(function () {
    checkProtologue(this);
  });

  $("#fontToggle").click(function (event) {
    var results = $('.results');
    var font = results.css("font-family");
    if (font.startsWith("\"Book")) {
      results.css("font-family", "\"Lucida Grande\", \"Helvetica Nueue\", Arial, sans-serif");
      $.cookie('resultFont', 'sans', {expires: 365});
    } else {
      results.css("font-family", "\"Bookman\", Georgia, \"Times New Roman\", serif");
      $.cookie('resultFont', 'serif', {expires: 365});
    }
    event.preventDefault();
  });

  $("#inRank")
    .each(function () {
      var el = $(this);
      if (el.val()) {
        $('#rankName').prop('disabled', false);
      } else {
        $('#rankName').val('').prop('disabled', true);
      }
    })
    .change(function (even) {
      var el = $(this);
      if (el.val()) {
        $('#rankName').prop('disabled', false).focus();
      } else {
        $('#rankName').val('').prop('disabled', true);
      }
    });

//check user preference for sans serif fonts on results
  var prefFont = $.cookie('resultFont');
  if (prefFont) {
    if (prefFont == 'sans') {
      $('.results').css("font-family", "\"Lucida Grande\", \"Helvetica Nueue\", Arial, sans-serif");
    }
  }

  $('branch').click(function (event) {

    if ($(event.target).is("a")) {
      // default behaviour on a hyperlink click
    }
    else {
      $(this).children('ul').toggle();
      event.preventDefault();
    }
  });
})
;

function clearComment(el) {
  $(el).parent().parent('form').find('[name="comment"]').val('');
}

function clearDistribution(el) {
  $(el).parent().parent('form').find('[name="distribution"]').val('');
}

function clearValue(el) {
  $(el).parent().parent('form').find('[name="value"]').val('');
}

function confirmDelete(el) {
  $(el).prop('disabled', true).next('div').show().focus();
  $(el).parent('form').find('[name="reason"]').val('Deleted, incorrect.');
}

function cancelDelete(el) {
  $(el).parent().hide().prev('button').prop('disabled', false);
  $(el).parent().parent('form').find('[name="reason"]').val('Errata, typographic error');
}

