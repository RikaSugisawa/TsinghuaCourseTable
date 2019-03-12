package tech.rika.tsinghuautils

import org.apache.http.HttpHost
import org.apache.http.NameValuePair
import org.apache.http.client.config.CookieSpecs
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.BasicCookieStore
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.client.LaxRedirectStrategy
import org.apache.http.impl.cookie.BasicClientCookie
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils
import java.util.*
import java.util.regex.Pattern

class Api {
    private val cookieStore = BasicCookieStore()
    private val httpClient = HttpClients.custom()
            .setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.DEFAULT).build())
            .setDefaultCookieStore(cookieStore)
            .setRedirectStrategy(LaxRedirectStrategy())
            .build()
    private val defaultHttpClient = HttpClients.custom()
            .setDefaultCookieStore(cookieStore)
            .build()

    private fun reassignCookiesDomain(domain: String) {
        val cookies = arrayListOf<BasicClientCookie>()
        for (cookie in cookieStore.cookies) {
            val newCookie = BasicClientCookie(cookie.name, cookie.value)
            newCookie.domain = domain
            cookies.add(newCookie)
        }
        cookieStore.clear()
        cookieStore.addCookies(cookies.toTypedArray())
    }

    fun login(username: String, password: String): Boolean {
        return try {
            httpClient.execute(HttpGet("http://zhjw.cic.tsinghua.edu.cn/jxpg/f/xs/Main"))
            reassignCookiesDomain("academic.tsinghua.edu.cn")
            val post = HttpPost("http://academic.tsinghua.edu.cn/Login")
            val body = LinkedList<NameValuePair>()
            body.add(BasicNameValuePair("userName", username))
            body.add(BasicNameValuePair("password", password))
            post.entity = UrlEncodedFormEntity(body)
            val resStr = EntityUtils.toString(httpClient.execute(post).entity)
            val token: String
            val tokenFind = Pattern.compile("ticket=([a-zA-Z0-9]+)\"").matcher(resStr)
            tokenFind.find()
            token = tokenFind.group(1)
            reassignCookiesDomain("zhjw.cic.tsinghua.edu.cn")
            httpClient.execute(HttpGet("http://zhjw.cic.tsinghua.edu.cn/j_acegi_login.do?url=/zhjw.do&m=jxmh_show&flag=bksjwjxxx&version=1&ticket=$token"))
            reassignCookiesDomain("zhjw.cic.tsinghua.edu.cn")
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getCalendar(begin: String, end: String): String? {
        try {
            val get = HttpGet("http://zhjw.cic.tsinghua.edu.cn/jxmh_out.do?" +
                    "m=bks_jxrl_all&" +
                    "p_start_date=$begin&" +
                    "p_end_date=$end&" +
                    "jsoncallback=_")

            val res = defaultHttpClient.execute(get)
            val resJson = EntityUtils.toString(res.entity)
            val reg = Pattern.compile("_\\((.*)\\)").matcher(resJson)
            reg.find()
            return reg.group(1)
//        val nashorn = ScriptEngineManager().getEngineByName("nashorn")
//        val events = nashorn.eval("(function(){return $resJson})()") as Map<*, *>
        } catch (e: Exception) {
            return null
        }
    }
}