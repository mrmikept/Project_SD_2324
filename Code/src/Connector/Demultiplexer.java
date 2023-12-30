package Connector;

import java.io.IOException;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Class to Demultiplex sending and receiving messages
 */
public class Demultiplexer implements Runnable
{
    private Connector connector;
    private Map<Integer, MessageTypeWaiters> mapType;
    private ReentrantLock lock;
    private Exception exeception;

    public Demultiplexer(Connector connector)
    {
        this.connector = connector;
        this.mapType = new HashMap<>();
        this.lock = new ReentrantLock();
        this.exeception = null;
    }

    /**
     * Sends a message
     * @param message Message to send
     */
    public void send(Message message)
    {
        this.connector.send(message);
    }

    /**
     * Sends a message
     * @param id id of the Message
     * @param type Message type
     * @param user Username of the user that sent the message
     * @param message message to send
     */
    public void send(String id, int type, String user, byte[] message)
    {
        this.connector.send(id,type,user,message);
    }

    /**
     * Function to wait for a specific message
     * @param type Message type
     * @return Message received
     */
    public byte[] receive(int type) throws Exception
    {
        this.lock.lock();
        try {
            MessageTypeWaiters typeWaiter = this.mapType.get(type);
            if (typeWaiter == null)
            {
                typeWaiter = new MessageTypeWaiters(this.lock);
                this.mapType.put(type,typeWaiter);
            }
            typeWaiter.addWaiter();
            while (true)
            {
                if (this.exeception != null)
                {
                    throw this.exeception;
                }
                if (!typeWaiter.isQueueEmpty())
                {
                    byte[] reply = typeWaiter.getMessage();
                    typeWaiter.removeWaiter();
                    if (typeWaiter.getWaiters() == 0 && typeWaiter.isQueueEmpty())
                    {
                        this.mapType.remove(type);
                    }
                    return reply;
                }
                typeWaiter.condition.await();
            }
        } finally {
            this.lock.unlock();
        }
    }

    /**
     * Run Function to listen to new messages
     */
    @Override
    public void run()
    {
        while (true) {
            Message message = null;
            try {
                message = this.connector.receive();
                if (message == null) {
                    break;
                }
            } catch (Exception e) {
                this.lock.lock();
                try {
                    this.exeception = e;
                    this.mapType.forEach((key, value) -> value.condition.signalAll());
                    break;
                } finally {
                    this.lock.unlock();
                }
            }
            this.lock.lock();
            try {
                if (message != null)
                {
                    MessageTypeWaiters typeWaiters = this.mapType.get(message.getType());
                    if (typeWaiters == null) {
                        typeWaiters = new MessageTypeWaiters(this.lock);
                        this.mapType.put(message.getType(), typeWaiters);
                    }
                    typeWaiters.addMessage(message.getMessage());
                    typeWaiters.condition.signalAll();
                } else break;
            } finally {
                this.lock.unlock();
            }
        }
    }

    /**
     * To close the input and output stream of the connector
     * @throws IOException
     */
    public void close() throws IOException {
        this.connector.close();
    }

}
