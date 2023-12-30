package Connector;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Class with information of the number os threads waiting for a specific message, and wait for them
 */
public class MessageTypeWaiters
{
    int waiters;
    Queue<byte[]> messages;
    Condition condition;
    ReentrantReadWriteLock.WriteLock writeLock;
    ReentrantReadWriteLock.ReadLock readLock;

    public MessageTypeWaiters(ReentrantLock lock)
    {
        this.waiters = 0;
        this.messages = new ArrayDeque<>();
        this.condition = lock.newCondition();
        ReentrantReadWriteLock l = new ReentrantReadWriteLock();
        this.writeLock = l.writeLock();
        this.readLock = l.readLock();
    }

    /**
     * Adds a waiter for a certain message
     */
    public void addWaiter()
    {
        this.writeLock.lock();
        try {
            this.waiters++;
        } finally {
            this.writeLock.unlock();
        }
    }

    /**
     * Removes the first message from the queue.
     * @return the message in the queue
     */
    public byte[] getMessage()
    {
        this.writeLock.lock();
        try
        {
            return this.messages.poll();
        } finally {
            this.writeLock.unlock();
        }
    }

    /**
     * Adds a Message to the queue to be retrieved later with the getMessage() method.
     * @param data The data of the message
     */
    public void addMessage(byte[] data)
    {
        this.writeLock.lock();
        try {
            this.messages.add(data);
        } finally {
            this.writeLock.unlock();
        }
    }

    /**
     * Verifies if the message queue is Empty.
     * @return True if the queue is empty or False otherwise
     */
    public boolean isQueueEmpty()
    {
        this.readLock.lock();
        try {
            return this.messages.isEmpty();
        } finally {
            this.readLock.unlock();
        }
    }

    /**
     * Removes a waiter for a certain message
     */
    public void removeWaiter()
    {
        this.writeLock.lock();
        try
        {
            this.waiters--;
        } finally {
            this.writeLock.unlock();
        }
    }

    /**
     * Gets the total number of waiters in this class.
     * @return
     */
    public int getWaiters()
    {
        this.readLock.lock();
        try
        {
            return this.waiters;
        } finally {
            this.readLock.unlock();
        }
    }
}
