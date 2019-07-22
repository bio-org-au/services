<synonym-of>
  <synonym-type>${instance.instanceType.ofLabel}:</synonym-type>
  <st:preferredLink target="${instance.citedBy.name}" api="api/apni-format">${raw(instance.citedBy.name.fullNameHtml)}</st:preferredLink>
  <st:preferredLink target="${instance.citedBy}"><i title="Link to use in reference" class="fa fa-link hidden-print"></i></st:preferredLink>
  <name-status class="${instance.citedBy.name.nameStatus?.name}">${instance.citedBy.name.nameStatus?.name}</name-status>

</synonym-of>