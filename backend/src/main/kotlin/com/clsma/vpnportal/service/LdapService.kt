package com.clsma.vpnportal.service

import org.springframework.ldap.core.DirContextOperations
import org.springframework.ldap.core.LdapTemplate
import org.springframework.ldap.core.support.AbstractContextMapper
import org.springframework.ldap.query.LdapQueryBuilder.query
import org.springframework.stereotype.Service

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
                    override fun doMapFromContext(ctx: DirContextOperations): LdapUser {
                        return LdapUser(
                            uid = ctx.getStringAttribute("uid") ?: uid,
                            cn = ctx.getStringAttribute("cn") ?: uid,
                            mail = ctx.getStringAttribute("mail") ?: ""
                        )
                    }
                }
            )
            results.firstOrNull()
        } catch (e: Exception) {
            null
        }
    }
}
