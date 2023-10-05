package com.kaiqkt.neosagakt.core

import org.slf4j.LoggerFactory

public class Orchestrator(
    private val service: Service
) : AutoCloseable {

    private val logger = LoggerFactory.getLogger(this::class.java)

    init { service.subscribe(::onReceive) }

    public fun start(id: String, node: Node, context: Map<String, Any>) {
        logger.info("Execute orchestration id $id")

        val transaction = Transaction(id, node, context)
        val execution = Process(transaction)
        service.publish(execution)
    }


    private fun onReceive(transaction: Transaction): Executor {
        logger.info("Receive transaction ${transaction.id} to execute command ${transaction.node.command}")

        val execution = execute(transaction)
        service.publish(execution)
        return execution
    }

    private fun execute(transaction: Transaction): Executor {
        val command = transaction.node.command.getDeclaredConstructor().newInstance()

        return try {
            val context = transaction.context.toMutableMap().apply(command::execute).toMap()

            val executedTransaction = transaction.copy(context = context)

            executedTransaction.nextOnSuccess()
                ?.let(::Process)
                ?: Finish(transaction).also { logger.info("Finishing process of transaction ${transaction.id}") }
        } catch (e: Throwable) {
            if (!transaction.exceedMaxTryRetryAttempts()) {
                Retry(transaction, e).also { logger.info("Failure executing transaction ${transaction.id} in command $command, retrying") }
            } else {
                transaction.takeUnless { e is AbortException }
                    ?.nextOnFailure()
                    ?.let(::Process).also { logger.info("Failure executing transaction ${transaction.id} in command $command, starting command compensation") }
                    ?: Abort(transaction, e).also { logger.info("Failure executing transaction ${transaction.id} in command $command, aborted") }
            }
        }
    }

    override fun close() {
        service.unsubscribe()
    }
}