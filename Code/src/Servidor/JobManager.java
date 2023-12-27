package Servidor;

import Worker.CompJobWaiters;
import Worker.Job;

import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class JobManager implements Runnable
{
    Queue<Job> pendingJobs;
    ReentrantLock pendingJobsLock;
    Condition pendingJobsCondition;
    Map<String, CompJobWaiters> jobWaitersMap; // Map to wait for job results. Key: Username, Value: CompJobWaiters
    private ReentrantLock completedJobLock; // Lock for the completedJob map
    private List<WorkerConnectionHandler> workers;

    public JobManager()
    {
        this.pendingJobs = new PriorityQueue<>();
        this.pendingJobsLock = new ReentrantLock();
        this.pendingJobsCondition = this.pendingJobsLock.newCondition();
        this.jobWaitersMap = new HashMap<>();
        this.completedJobLock = new ReentrantLock();
    }

    public void addCompletedJob(Job job)
    {
        this.completedJobLock.lock();
        try {
            CompJobWaiters jobWaiters = this.jobWaitersMap.get(job.getUser());
            if (jobWaiters == null)
            {
                jobWaiters = new CompJobWaiters(this.completedJobLock, job.getUser());
                this.jobWaitersMap.put(job.getUser(),jobWaiters);
            }
            jobWaiters.addJob(job);
            jobWaiters.condition.signalAll(); // Signal all the threads waiting for the completion for a job from a given user
        } finally {
            this.completedJobLock.unlock();
        }
    }

    public Job waitForJobCompletion(Job job)
    {
        this.completedJobLock.lock();
        try {
            CompJobWaiters jobWaiters = this.jobWaitersMap.get(job.getUser());
            if (jobWaiters == null)
            {
                jobWaiters = new CompJobWaiters(this.completedJobLock, job.getUser());
                this.jobWaitersMap.put(job.getUser(),jobWaiters);
            }
            jobWaiters.addWaiter();

            while (true)
            {
                Job result = jobWaiters.getJobResult(job.getId());
                if (result != null)
                {
                    jobWaiters.removeWaiter();
                    if (jobWaiters.isMapEmpty() && jobWaiters.getWaiters() == 0)
                    {
                        this.jobWaitersMap.remove(job.getUser());
                    }
                    return result;
                }
                jobWaiters.condition.await();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            this.completedJobLock.unlock();
        }
    }

    public void addPendingJob(Job job)
    {
        this.pendingJobsLock.lock();
        try
        {
            this.pendingJobs.add(job);
            this.pendingJobsCondition.signalAll();
        } finally {
            this.pendingJobsLock.unlock();
        }
    }

    public Job getPendingJob(int memoryAvailable)
    {
        this.pendingJobsLock.lock();
        try {
            Job job = null;
            while (this.pendingJobs.isEmpty())
            {
                try {
                    System.out.println("Waiting for new Jobs...");
                    this.pendingJobsCondition.await();
                } catch (InterruptedException e)
                {
                    System.out.println(e.getMessage());
                }
            }
            job = this.pendingJobs.peek();
            if (job.getMemory() <= memoryAvailable)
            {
                return this.pendingJobs.poll();
            } else return null;
        } finally {
            this.pendingJobsLock.unlock();
        }
    }

    public int countPendingJobs()
    {
        this.pendingJobsLock.lock();
        try
        {
            return this.pendingJobs.size();
        }
        finally
        {
            this.pendingJobsLock.unlock();
        }
    }


    @Override
    public void run()
    {
        System.out.println("Started Job Manager!");
        while (true)
        {
            this.pendingJobsLock.lock();
            try {
                Job job;
                while (this.pendingJobs.isEmpty())
                {
                    System.out.println("Waiting for new Jobs!");
                    try {
                        this.pendingJobsCondition.await();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                job = this.pendingJobs.peek();
                for (WorkerConnectionHandler worker : this.workers)
                {
                    if (job.getMemory() + worker.getUsedMemory() <= worker.getTotalMemory())
                    {
                        worker.sendJobRequest(this.pendingJobs.poll());
                        break;
                    }
                }
            }
            finally
            {
                this.pendingJobsLock.unlock();
            }
        }
    }
}
