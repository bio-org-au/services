package au.org.biodiversity.nsl

import org.apache.shiro.authc.AuthenticationInfo
import org.apache.shiro.authc.AuthenticationToken
import org.apache.shiro.authc.UsernamePasswordToken
import org.apache.shiro.grails.LdapServer

import javax.naming.AuthenticationException
import javax.naming.Context
import javax.naming.NamingEnumeration
import javax.naming.directory.Attribute
import javax.naming.directory.InitialDirContext
import javax.naming.directory.SearchControls
import javax.naming.directory.SearchResult
import java.util.regex.Matcher
import java.util.regex.Pattern

class ActiveDirectoryServer extends LdapServer {
    String memberAttribute = 'cn'
    String groupPattern = '(.*)'
    SearchControls searchCtls = new SearchControls();
    Hashtable env = new Hashtable()

    List<String> roles(String userName) {
        List<String> roles = []
        ldapSearch { InitialDirContext ctx, String ldapUrl ->
            NamingEnumeration<SearchResult> result = ctx.search(groupOu, "$groupMemberElement=$groupMemberPrefix$userName$groupMemberPostfix", searchCtls)

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

    protected void getBaseLDAPEnvironment(String user, String password) {
        env[Context.INITIAL_CONTEXT_FACTORY] = "com.sun.jndi.ldap.LdapCtxFactory"
        env[Context.REFERRAL] = 'follow'
        if (user) {
            // Non-anonymous access for the search.
            env[Context.SECURITY_AUTHENTICATION] = "simple"
            env[Context.SECURITY_PRINCIPAL] = user
            env[Context.SECURITY_CREDENTIALS] = password
        }
    }

    protected InitialDirContext getLDAPContext2(String user, String password, String ldapUrl) {
        // Set up the configuration for the LDAP search we are about to do.
        getBaseLDAPEnvironment(user, password)
        env[Context.PROVIDER_URL] = ldapUrl
        return new InitialDirContext(env)
    }

    protected ldapSearch(Closure work) {
        String ldapUrl = findLDAPServerUrlToUse(searchUser, searchPass)
        if (ldapUrl) {
            InitialDirContext ctx = getLDAPContext2(searchUser, searchPass, ldapUrl)
            return work(ctx, ldapUrl)
        } else {
            throw new AuthenticationException("No LDAP server available.")
        }
    }

    Map getUserAttributes(String userName) {
        Map attMap = [:]
        ldapSearch { InitialDirContext ctx, String ldapUrl ->

            // Look up the DN for the LDAP entry that has a 'uid' value
            // matching the given username.
            NamingEnumeration<SearchResult> result = ctx.search(searchBase, "$usernameAttribute=$userName", searchCtls)
            if (result.hasMore()) {
                SearchResult searchResult = result.next()
                searchResult.attributes.all.each { Attribute attr ->
                    List vals = attr.all.collect { it }
                    if (vals.size() > 1) {
                        attMap.put(attr.ID, vals)
                    } else {
                        attMap.put(attr.ID, vals.first())
                    }
                }
            }
        }
        return attMap
    }
    @Override
    boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
        UsernamePasswordToken userToken = (UsernamePasswordToken) token
        String password = new String(userToken.getPassword())
        def log = LOG

        ldapSearch { InitialDirContext ctx, String ldapUrl ->

            // Look up the DN for the LDAP entry that has a 'uid' value matching the given username.
            NamingEnumeration<SearchResult> result = ctx.search(searchBase, "$usernameAttribute=$userToken.username", searchCtls)
            if (!result.hasMore()) {
                return false
            }

            //check we can log in as the user we just found using the password supplied
            SearchResult searchResult = result.next()
            try {
                getLDAPContext(searchResult.nameInNamespace, password, ldapUrl)
                //we don't care about the context, just that we don't get an exception from logging in
                return true

            } catch (AuthenticationException ex) {
                log.info "Invalid password $ex.message"
                return false
            }
        }
    }
}
