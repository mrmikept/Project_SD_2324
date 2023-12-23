package Worker;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class CompJobWaiters
{
    private int waiters;
    private Map<Integer, byte[]> completed;
    public Condition condition;

    public CompJobWaiters(ReentrantLock lock)
    {
        this.waiters = 0;
        this.completed = new HashMap<>();
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

    public void addJob(int jobId, byte[] jobResult)
    {
        this.completed.put(jobId,jobResult);
    }

    public byte[] getJobResult(int jobId)
    {
        return this.completed.remove(jobId);
    }

    public boolean isMapEmpty()
    {
        return this.completed.isEmpty();
    }

    public int getWaiters()
    {
        return this.waiters;
    }

}
