package com.hychul.webflux.config

import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class H2Config {
    private var webServer: org.h2.tools.Server? = null

    @EventListener(org.springframework.context.event.ContextRefreshedEvent::class)
    @Throws(java.sql.SQLException::class)
    fun start() {
        this.webServer = org.h2.tools.Server.createWebServer("-webPort", "8082", "-tcpAllowOthers").start()
    }

    @EventListener(org.springframework.context.event.ContextClosedEvent::class)
    fun stop() {
        this.webServer!!.stop()
    }
}