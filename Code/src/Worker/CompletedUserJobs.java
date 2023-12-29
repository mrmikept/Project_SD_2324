package Worker;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class CompletedUserJobs
{
    private Queue<Job> completedJobs;
    private ReentrantLock queueLock;
    private Condition queueCondition;

    public CompletedUserJobs()
    {
        this.completedJobs = new ArrayDeque<>();
        this.queueLock = new ReentrantLock();
        this.queueCondition = this.queueLock.newCondition();
    }

    public void addCompletedJob(Job job)
    {
        this.queueLock.lock();
        try
        {
            this.completedJobs.add(job);
            this.queueCondition.signalAll();
        } finally {
            this.queueLock.unlock();
        }
    }

    public Job getCompletedJob()
    {
        this.queueLock.lock();
        try
        {
            while (this.completedJobs.isEmpty())
            {
                this.queueCondition.await();
            }
            return this.completedJobs.poll();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            this.queueLock.unlock();
        }
    }

}
