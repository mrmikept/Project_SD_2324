package Worker;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class CompJobWaiters
{
    private int waiters;
    private Map<Integer, Job> completed;
    private String user;
    public Condition condition;

    public CompJobWaiters(ReentrantLock lock, String user)
    {
        this.waiters = 0;
        this.completed = new HashMap<>();
        this.user = user;
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

    public void addJob(int jobId, byte[] jobResult, int result)
    {
        this.completed.put(jobId,new Job(jobId, this.user, jobResult, result));
    }

    public Job getJobResult(int jobId)
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
