package Connector;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Class to Demultiplex sending and receiving messages
 */
public class Demultiplexer implements Runnable
{
    private Connector connector;
    private Map<Integer, MessageTypeWaiters> mapType;
    private ReentrantLock lock;

    public Demultiplexer(Connector connector)
    {
        this.connector = connector;
        this.mapType = new HashMap<>();
        this.lock = new ReentrantLock();
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
    public byte[] receive(int type)
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
                if (!typeWaiter.messages.isEmpty())
                {
                    byte[] reply = typeWaiter.messages.poll();
                    typeWaiter.removeWaiter();
                    if (typeWaiter.waiters == 0 && typeWaiter.messages.isEmpty())
                    {
                        this.mapType.remove(type);
                    }
                    return reply;
                }
                typeWaiter.condition.await();
            }

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
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
        while (true)
        {
            Message message = this.connector.receive();
            if (message == null)
            {
                try {
                    this.connector.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            }
            this.lock.lock();
            try {
                MessageTypeWaiters typeWaiters = this.mapType.get(message.getType());
                if (typeWaiters == null)
                {
                    typeWaiters = new MessageTypeWaiters(this.lock);
                    this.mapType.put(message.getType(),typeWaiters);
                }
                typeWaiters.messages.add(message.getMessage());
                typeWaiters.condition.signalAll();
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
