<af:sortedInstances instances="${instances}" var="instance">
  <misapplication>
    ${instance.instanceType.hasLabel}:
    <st:preferredLink target="${instance.cites.name}"
                      api="api/apni-format">${raw(instance.cites.name.fullNameHtml)}</st:preferredLink>
    <st:preferredLink target="${instance.cites}"><i title="Link to use in reference"
                                                    class="fa fa-link hidden-print"></i></st:preferredLink>
    <name-status class="${instance.cites.name.nameStatus.name}">${instance.cites.name.nameStatus.name}</name-status>
    by ${raw(instance?.cites?.reference?.citationHtml)}: ${instance?.cites?.page ?: '-'}

  </misapplication>
</af:sortedInstances>