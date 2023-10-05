package com.kaiqkt.neosagakt.core

public interface Service {
    public fun publish(executor: Executor)
    public fun subscribe(deliver: (Transaction) -> Executor)
    public fun unsubscribe()
}