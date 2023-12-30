package Worker;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Class to store all the complete Jobs from a given User.
 */
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

    /**
     * Adds a completed job in the queue and signall all the threads waiting for a job result.
     * @param job Job to add.
     */
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

    /**
     * Removes a completed job from the queue. If the queue is empty the thread waits for a job to be added.
     * @return The job in front of the queue.
     */
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
            return null;
        } finally {
            this.queueLock.unlock();
        }
    }

}
