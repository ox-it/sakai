package org.sakaiproject.search.queueing;

import com.rabbitmq.client.*;
import org.sakaiproject.search.indexing.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.concurrent.ExecutorService;

/**
 * TaskRunner using an AMQP server to obtain tasks.
 *
 * @author Colin Hebert
 */
public class AmqpRunner extends WaitingTaskRunner {
    private static final Logger logger = LoggerFactory.getLogger(AmqpQueueing.class);
    private ConnectionFactory connectionFactory;
    private ExecutorService executor;
    private boolean running = true;
    private Connection amqpConnection;
    private Channel channel;
    private String queueName;

    /**
     * Creates a connection and starts handling tasks sent by the AMQP server.
     */
    public void init() {
        try {
            amqpConnection = connectionFactory.newConnection(executor);
            amqpConnection.addShutdownListener(new AmqpHandlerShutdownListener());
            channel = amqpConnection.createChannel();
            channel.basicConsume(queueName, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag,
                                           Envelope envelope,
                                           AMQP.BasicProperties properties,
                                           byte[] body)
                        throws IOException {
                    long deliveryTag = envelope.getDeliveryTag();
                    runTask(deserialize(body));
                    channel.basicAck(deliveryTag, false);
                }
            });
        } catch (IOException e) {
            logger.error("Exception while trying to get tasks from the AMQP server", e);
        }
    }

    /**
     * Stops the connection to the AMQP server.
     */
    public void destroy() {
        synchronized (this) {
            try {
                running = false;
                channel.close();
                amqpConnection.close();
            } catch (IOException e) {
                logger.error("Exception while closing the connection to the AMQP server", e);
            }
        }
        executor.shutdownNow();
    }

    private Task deserialize(byte[] message) {
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new ByteArrayInputStream(message));
            return (Task) ois.readObject();
        } catch (Exception e) {
            logger.error("Couldn't deserialize the content", e);
        } finally {
            try {
                if (ois != null)
                    ois.close();
            } catch (IOException e) {
                logger.error("Couldn't close the ObjectInputStream", e);
            }
        }

        return null;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public void setExecutor(ExecutorService executor) {
        this.executor = executor;
    }

    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    private class AmqpHandlerShutdownListener implements ShutdownListener {

        @Override
        public void shutdownCompleted(ShutdownSignalException cause) {
            synchronized (this) {
                // TODO: Avoid looping reconnection?
                if (running)
                    init();
            }
        }
    }
}
