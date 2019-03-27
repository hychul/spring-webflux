package com.hychul.webflux.handler

import com.hychul.webflux.extension.Logging
import com.hychul.webflux.extension.logger
import org.springframework.http.codec.ServerSentEvent
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters.fromObject
import org.springframework.web.reactive.function.BodyInserters.fromServerSentEvents
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse.ok
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.UnicastProcessor
import java.time.Instant

@Component
class SseHandler : Logging {

    private val log = logger()

    private val hotSource = UnicastProcessor.create<String>()
    private val hotFlux = hotSource.publish()

    init {
        hotFlux.connect()
    }

    fun listen(request: ServerRequest) =
        request.bodyToMono(String::class.java).doOnNext {
            log.info("log")
        }.flatMap {
            ok().body(fromServerSentEvents(Flux.from(hotFlux)
                                               .map { msg ->
                                                   ServerSentEvent.builder<String>()
                                                       .id("")
                                                       .event("message")
                                                       .data("msg '$msg' ${Instant.now()}")
                                                       .build()
                                               }))
        }


    fun broadcast(request: ServerRequest) =
        Mono.just(request).map { it.pathVariable("msg") }.flatMap { msg ->
                hotSource.onNext(msg)
            ok().body(fromObject("done $msg"))
        }
}