<%@ page import="au.org.biodiversity.nsl.Instance; au.org.biodiversity.nsl.Name" %>
<!DOCTYPE html>
<html>
<head>
  <meta name="layout" content="main">
  <title>${name.name}</title>
</head>

<body>
<h2>NSL Simple Name Representation
  <help>
    <i class="fa fa-info-circle"></i>

    <div>
      This is a flat repesentation of the name and it's place

    </div>
  </help>
</h2>

<div class="rest-resource-content">
  <dl class="dl-horizontal">
    <dt>name</dt><dd>${name.name}</dd>
    <dt>taxonName</dt><dd>${name.taxonName}</dd>
    <dt>nameElement</dt><dd>${name.nameElement}</dd>
    <dt>cultivarName</dt><dd>${name.cultivarName}</dd>
    <dt>simpleNameHtml</dt><dd>${raw(name.simpleNameHtml)}</dd>
    <dt>fullNameHtml</dt><dd><st:preferredLink
      target="${Name.get(name.id)}">${raw(name.fullNameHtml)}</st:preferredLink></dd>

    <dt class='divide'>&nbsp;</dt><dd class='divide'>&nbsp;</dd>
    <dt>apcInstance</dt><dd><st:preferredLink
      target="${name.apcInstance}">${raw(name.apcInstance?.reference?.citationHtml)}</st:preferredLink></dd>
    <dt>apcName</dt><dd><st:preferredLink
      target="${name.apcInstance?.citedBy?.name ?: name.apcInstance?.name}">${name.apcName}</st:preferredLink></dd>
    <dt>apcRelationshipType</dt><dd>${name.apcRelationshipType}</dd>
    <dt>apcProparte</dt><dd><af:tick val="${name.apcProparte}"/></dd>
    <dt>apcComment</dt><dd>${name.apcComment}</dd>
    <dt>apcDistribution</dt><dd>${name.apcDistribution}</dd>
    <dt>apcExcluded</dt><dd><af:tick val="${name.apcExcluded}"/></dd>

    <dt class='divide'>&nbsp;</dt><dd class='divide'>&nbsp;</dd>
    <dt>nameTypeName</dt><dd>${name.nameTypeName}</dd>
    <dt>homonym</dt><dd><af:tick val="${name.homonym}"/></dd>
    <dt>autonym</dt><dd><af:tick val="${name.autonym}"/></dd>
    <dt>hybrid</dt><dd><af:tick val="${name.hybrid}"/></dd>
    <dt>cultivar</dt><dd><af:tick val="${name.cultivar}"/></dd>
    <dt>formula</dt><dd><af:tick val="${name.formula}"/></dd>
    <dt>scientific</dt><dd><af:tick val="${name.scientific}"/></dd>
    <dt>nomStat</dt><dd>${name.nomStat}</dd>
    <dt>nomIlleg</dt><dd><af:tick val="${name.nomIlleg}"/></dd>
    <dt>nomInval</dt><dd><af:tick val="${name.nomInval}"/></dd>

    <dt class='divide'>&nbsp;</dt><dd class='divide'>&nbsp;</dd>
    <dt>authority</dt><dd>${name.authority}</dd>
    <dt>baseNameAuthor</dt><dd>${name.baseNameAuthor}</dd>
    <dt>exBaseNameAuthor</dt><dd>${name.exBaseNameAuthor}</dd>
    <dt>author</dt><dd>${name.author}</dd>
    <dt>exAuthor</dt><dd>${name.exAuthor}</dd>
    <dt>sanctioningAuthor</dt><dd>${name.sanctioningAuthor}</dd>

    <dt class='divide'>&nbsp;</dt><dd class='divide'>&nbsp;</dd>
    <dt>rank</dt><dd>${name.rank}</dd>
    <dt>rankSortOrder</dt><dd>${name.rankSortOrder}</dd>
    <dt>rankAbbrev</dt><dd>${name.rankAbbrev}</dd>

    <dt class='divide'>&nbsp;</dt><dd class='divide'>&nbsp;</dd>
    <dt>classifications</dt><dd>${name.classifications}</dd>
    <dt>apni</dt><dd><af:tick val="${name.apni}"/></dd>
    <dt>protoYear</dt><dd>${name.protoYear}</dd>

    <dt class='divide'>&nbsp;</dt><dd class='divide'>&nbsp;</dd>
    <dt>parentNsl</dt><dd><st:preferredLink
      target="${name.parentNsl}">${raw(name.parentNsl.fullNameHtml)}</st:preferredLink></dd>
    <dt>secondParentNsl</dt><dd><st:preferredLink
      target="${name.secondParentNsl}">${raw(name.secondParentNsl.fullNameHtml)}</st:preferredLink></dd>
    <dt>familyNsl</dt><dd><st:preferredLink
      target="${name.familyNsl}">${raw(name.familyNsl.fullNameHtml)}</st:preferredLink></dd>
    <dt>genusNsl</dt><dd><st:preferredLink
      target="${name.genusNsl}">${raw(name.genusNsl.fullNameHtml)}</st:preferredLink></dd>
    <dt>speciesNsl</dt><dd><st:preferredLink
      target="${name.speciesNsl}">${raw(name.speciesNsl.fullNameHtml)}</st:preferredLink></dd>

    <dt>classis</dt><dd>${name.classis}</dd>
    <dt>subclassis</dt><dd>${name.subclassis}</dd>
    <dt>apcFamilia</dt><dd>${name.apcFamilia}</dd>
    <dt>familia</dt><dd>${name.familia}</dd>
    <dt>genus</dt><dd>${name.genus}</dd>
    <dt>species</dt><dd>${name.species}</dd>
    <dt>infraspecies</dt><dd>${name.infraspecies}</dd>

    <dt class='divide'>&nbsp;</dt><dd class='divide'>&nbsp;</dd>
    <dt>updatedBy</dt><dd>${name.updatedBy}</dd>
    <dt>updatedAt</dt><dd>${name.updatedAt}</dd>
    <dt>createdBy</dt><dd>${name.createdBy}</dd>
    <dt>createdAt</dt><dd>${name.createdAt}</dd>

  </dl>
  <g:render template="links"/>

</div>

</body>
</html>
