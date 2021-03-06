package hello

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Mono

@SpringBootApplication
class KotlinApplication {

    @Bean
    fun routes() = router {
        GET {
            ServerResponse.ok().body(Mono.just("Let the battle begin!"))
        }

        POST("/**", accept(APPLICATION_JSON)) { request ->
            request.bodyToMono(ArenaUpdate::class.java).flatMap { arenaUpdate ->
                println(arenaUpdate)
                val myState = arenaUpdate.arena.state[arenaUpdate._links.self.href]

                var move = "T"
                var canThrow = false
                var isTargetN = false
                var isTargetS = false
                var isTargetE = false
                var isTargetW = false

                for ((key, player) in arenaUpdate.arena.state) {
                    if (key != arenaUpdate._links.self.href) {
                        if (myState!!.y == player.y && Math.abs(myState.x - player.x) <= 3) {
                            if ((myState.x - player.x) < 0) {
                                isTargetE = true
                                if (myState.direction == "E") {
                                    canThrow = true
                                }
                            } else {
                                isTargetW = true
                                if (myState.direction == "W") {
                                    canThrow = true
                                }
                            }
                        }
                        if (myState.x == player.x && Math.abs(myState.y - player.y) <= 3) {
                            if ((myState.y - player.y) > 0) {
                                isTargetN = true
                                if (myState.direction == "N") {
                                    canThrow = true
                                }
                            } else {
                                isTargetS = true
                                if (myState.direction == "S") {
                                    canThrow = true
                                }
                            }
                        }
                    }
                    
                    if (canThrow) {
                        break
                    }
                }

                if (!canThrow) {
                    if (
                        (myState!!.direction == "N" && isTargetE) || 
                        (myState.direction == "E" && isTargetS) ||
                        (myState.direction == "S" && isTargetW) ||
                        (myState.direction == "W" && isTargetN)
                    ) {
                        move = "R"
                    }
                    if (
                        move == "T" && (
                            (myState.direction == "N" && isTargetW) ||
                            (myState.direction == "W" && isTargetS) ||
                            (myState.direction == "S" && isTargetE) ||
                            (myState.direction == "E" && isTargetN)
                        )
                    ) {
                        move = "L"
                    }
                    if ( move == "T") {
                        if (
                            (myState.direction == "N" && isTargetS) ||
                            (myState.direction == "W" && isTargetE) ||
                            (myState.direction == "S" && isTargetN) ||
                            (myState.direction == "E" && isTargetW)
                        ) {
                            move = "L"
                        } else {
                            move = listOf("F", "R", "L").random()
                        }
                    }
                }

                ServerResponse.ok().body(Mono.just(move))
            }
        }
    }
}

fun main(args: Array<String>) {
    runApplication<KotlinApplication>(*args)
}

data class ArenaUpdate(val _links: Links, val arena: Arena)
data class PlayerState(val x: Int, val y: Int, val direction: String, val score: Int, val wasHit: Boolean)
data class Links(val self: Self)
data class Self(val href: String)
data class Arena(val dims: List<Int>, val state: Map<String, PlayerState>)
