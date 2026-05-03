package com.clsma.vpnportal.service

import org.springframework.ldap.core.LdapTemplate
import org.springframework.ldap.core.support.AbstractContextMapper
import org.springframework.ldap.query.LdapQueryBuilder.query
import org.springframework.stereotype.Service
import javax.naming.directory.SearchControls
import javax.naming.ldap.LdapContext

data class LdapUser(
    val uid: String,
    val cn: String,
    val mail: String
)

@Service
class LdapService(private val ldapTemplate: LdapTemplate) {

    fun findUserByUid(uid: String): LdapUser? {
        return try {
            val results = ldapTemplate.search(
                query()
                    .base("ou=people")
                    .where("uid").`is`(uid),
                object : AbstractContextMapper<LdapUser>() {
                    override fun doMapFromContext(ctx: javax.naming.directory.DirContext): LdapUser {
                        val dirCtx = ctx as javax.naming.directory.DirContext
                        return LdapUser(
                            uid = getAttr(dirCtx, "uid") ?: uid,
                            cn = getAttr(dirCtx, "cn") ?: uid,
                            mail = getAttr(dirCtx, "mail") ?: ""
                        )
                    }
                }
            )
            results.firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    private fun getAttr(ctx: javax.naming.directory.DirContext, attr: String): String? {
        return try {
            ctx.attributes.get(attr)?.get()?.toString()
        } catch (e: Exception) {
            null
        }
    }
}
