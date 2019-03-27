package com.hychul.webflux

import com.hychul.webflux.handler.SseHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.function.server.router
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter

@EnableWebFlux
@Configuration
class WebConfig {
    @Autowired
    lateinit var sseHandler: SseHandler

    @Bean
    fun webRouter() = router {
        path("/sse").nest {
            GET("/join") { sseHandler.listen(it) }
            GET("/send/{msg}") { sseHandler.broadcast(it) }
        }
    }.filter { request, next ->
        next.handle(request)
    }

    @Bean
    fun webSocketHandlerAdapter(): WebSocketHandlerAdapter {
        return WebSocketHandlerAdapter()
    }

    @Bean
    fun handlerMapping(): HandlerMapping {
        val map = HashMap<String, WebSocketHandler>()
        map["/websocket"] = WebSocketHandler {
            val out = it.receive().map { value -> it.textMessage(value.payloadAsText) }
            it.send(out)
        }

        val mapping = SimpleUrlHandlerMapping()
        mapping.urlMap = map
        mapping.order = -2 // Higher priority then other http requests (RouterFunction : -1, RequestMapping : 0)
        return mapping
    }
}