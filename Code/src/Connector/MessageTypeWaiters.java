package Connector;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Class with information of the number os threads waiting for a specific message, and wait for them
 */
public class MessageTypeWaiters
{
    int waiters;
    Queue<byte[]> messages;
    Condition condition;

    public MessageTypeWaiters(ReentrantLock lock)
    {
        this.waiters = 0;
        this.messages = new ArrayDeque<>();
        this.condition = lock.newCondition();
    }

    public void addWaiter()
    {
        this.waiters++;
    }

    public void removeWaiter()
    {
        this.waiters--;
    }
}
