package au.org.biodiversity.nsl

import org.apache.shiro.grails.LdapServer

import javax.naming.NamingEnumeration
import javax.naming.directory.Attribute
import javax.naming.directory.BasicAttribute
import javax.naming.directory.BasicAttributes
import javax.naming.directory.InitialDirContext
import javax.naming.directory.SearchResult
import java.util.regex.Matcher
import java.util.regex.Pattern

class ActiveDirectoryServer extends LdapServer {
    String memberAttribute = 'cn'
    String groupPattern = '(.*)'

    List<String> roles(String userName) {
        List<String> roles = []
        ldapSearch { InitialDirContext ctx, String ldapUrl ->
            BasicAttributes matchAttrs = new BasicAttributes(true)
            matchAttrs.put(new BasicAttribute(groupMemberElement, "$groupMemberPrefix$userName$groupMemberPostfix"))

            NamingEnumeration<SearchResult> result = ctx.search(groupOu, matchAttrs)

            while (result.hasMore()) {
                SearchResult group = result.next()
                Attribute cnAttr = group.attributes.get(memberAttribute)
                List<String> names = cnAttr.all.collect { it as String }
                List<String> matchingNames = new ArrayList<>()
                Pattern p = Pattern.compile(groupPattern)
                names.each {
                    Matcher m = p.matcher(it)
                    if (m.find()) {
                        matchingNames.add(m.group(1))
                    }
                }

                roles.addAll(matchingNames)
            }
        }
        return roles
    }

}
