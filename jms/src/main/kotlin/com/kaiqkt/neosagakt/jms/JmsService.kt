package com.kaiqkt.neosagakt.jms

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.kaiqkt.neosagakt.core.Abort
import com.kaiqkt.neosagakt.core.Finish
import com.kaiqkt.neosagakt.core.Retry
import com.kaiqkt.neosagakt.core.Process
import com.kaiqkt.neosagakt.core.Service
import com.kaiqkt.neosagakt.core.Transaction
import javax.jms.BytesMessage
import javax.jms.DeliveryMode
import javax.jms.MessageConsumer
import javax.jms.MessageListener
import javax.jms.Session

public class JmsService(
    private val session: Session,
    queueName: String,
    dlqQueueName: String? = null,
) : AutoCloseable, Service {

    private val queue = session.createQueue(queueName)
    private val producer = session.createProducer(queue).apply { deliveryMode = DeliveryMode.PERSISTENT }

    private val dlqQueue = dlqQueueName?.let(session::createQueue)
    private val dlqProducer = dlqQueue?.let(session::createProducer)?.apply { deliveryMode = DeliveryMode.PERSISTENT }

    private var consumers: List<MessageConsumer>? = null

    override fun publish(executor: com.kaiqkt.neosagakt.core.Executor) {
        when (executor) {
            is Process -> producer.send(executor.transaction.encode())
            is Retry -> producer.send(executor.transaction.encode())
            is Abort -> dlqProducer?.send(executor.transaction.encode())
            is Finish -> Unit
        }
    }

    public override fun subscribe(deliver: (Transaction) -> com.kaiqkt.neosagakt.core.Executor) {
        val listener = MessageListener { message ->
            if (message as? BytesMessage == null)
                return@MessageListener

            val transaction: Transaction = try {
                message.decode().copy(attempt = message.getIntProperty("JMSXDeliveryCount"))
            } catch (e: Exception) {
                return@MessageListener
            }

            val executor = deliver(transaction)
            if (executor !is Retry) {
                message.acknowledge()
            }
        }

        consumers = (0..1).map { session.createConsumer(queue).apply { messageListener = listener } }
    }

    private fun Transaction.encode() = jacksonObjectMapper().writeValueAsBytes(this).toByteMessages()

    private fun BytesMessage.decode(): Transaction = jacksonObjectMapper().readValue(this.toByteArray(), Transaction::class.java)

    private fun BytesMessage.toByteArray(): ByteArray = ByteArray(bodyLength.toInt()).apply(::readBytes)

    private fun ByteArray.toByteMessages(): BytesMessage = session.createBytesMessage().also { it.writeBytes(this) }

    override fun unsubscribe() {
        consumers?.map { it.close() }
    }

    override fun close() {
        unsubscribe()
        producer.close()
        dlqProducer?.close()
    }
}