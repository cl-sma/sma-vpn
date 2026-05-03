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

    fun findUserByUid(uid: String): LdapUser? = searchUser("uid", uid)

    fun findUserByMail(mail: String): LdapUser? = searchUser("mail", mail)

    private fun searchUser(attr: String, value: String): LdapUser? {
        return try {
            val results = ldapTemplate.search(
                query()
                    .base("ou=people")
                    .where(attr).`is`(value),
                object : AbstractContextMapper<LdapUser>() {
                    override fun doMapFromContext(ctx: DirContextOperations): LdapUser {
                        return LdapUser(
                            uid = ctx.getStringAttribute("uid") ?: value,
                            cn = ctx.getStringAttribute("cn") ?: value,
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
