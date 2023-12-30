package Servidor;

import Worker.CompletedUserJobs;
import Worker.Job;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class JobManager implements Runnable
{
    Queue<Job> pendingJobs; // Queue with pending jobs to be executed
    ReentrantLock pendingJobsLock; // Lock for the pending jobs queue
    Condition pendingJobsCondition; // Condition for the pending jobs lock
    Map<Integer,List<Job>> executingJobs; // Map for jobs being executed
    ReentrantLock executionJobsLock; // Lock for the executing jobs map
    Condition executionCondition;
    Map<String, CompletedUserJobs> completeJobsMap; // Map to wait for job results. Key: Username, Value: CompJobWaiters
    private ReentrantLock completedJobLock; // Lock for the completedJob map
    private List<WorkerConnectionHandler> workers; // List with workers connected
    private ReentrantLock workerListLock; // Lock for the Connected workers list
    private int maxWorkerMemory;
    private ReentrantReadWriteLock maxMemoryLock;

    public JobManager()
    {
        this.pendingJobs = new PriorityQueue<>();
        this.pendingJobsLock = new ReentrantLock();
        this.pendingJobsCondition = this.pendingJobsLock.newCondition();
        this.executingJobs = new HashMap<>();
        this.executionJobsLock = new ReentrantLock();
        this.executionCondition = this.executionJobsLock.newCondition();
        this.completeJobsMap = new HashMap<>();
        this.completedJobLock = new ReentrantLock();
        this.workers = new ArrayList<>();
        this.workerListLock = new ReentrantLock();
        this.maxWorkerMemory = 0;
        this.maxMemoryLock = new ReentrantReadWriteLock();
    }

    public int getAvailableMemory()
    {
        int usedMemory = 0;
        int totalMemory = 0;
        this.workerListLock.lock();
        try {
            for (WorkerConnectionHandler worker : this.workers)
            {
                usedMemory += worker.getUsedMemory();
                totalMemory += worker.getTotalMemory();
            }
            return totalMemory - usedMemory;
        } finally {
            this.workerListLock.unlock();
        }
    }

    public int getMaxWorkerMemory()
    {
        this.maxMemoryLock.readLock().lock();
        try {
            return this.maxWorkerMemory;
        } finally {
            this.maxMemoryLock.readLock().unlock();
        }
    }

    public void addCompletedJob(Job job, int workerId)
    {
        this.executionJobsLock.lock();
        try
        {
            List<Job> workerJobs = this.executingJobs.get(workerId);
            if (workerJobs == null)
            {
                workerJobs = new ArrayList<>();
                this.executingJobs.put(workerId,workerJobs);
            }
            workerJobs.remove(job);
            this.executionCondition.signalAll();
        } finally {
            this.executionJobsLock.unlock();
        }
        this.completedJobLock.lock();
        try {
            CompletedUserJobs completedUserJobs = this.completeJobsMap.get(job.getUser());
            if (completedUserJobs == null)
            {
                completedUserJobs = new CompletedUserJobs();
                this.completeJobsMap.put(job.getUser(),completedUserJobs);
            }
            completedUserJobs.addCompletedJob(job);
        } finally {
            this.completedJobLock.unlock();
        }
    }

    public void addErrorJob(Job job)
    {
        this.completedJobLock.lock();
        try {
            CompletedUserJobs completedUserJobs = this.completeJobsMap.get(job.getUser());
            if (completedUserJobs == null)
            {
                completedUserJobs = new CompletedUserJobs();
                this.completeJobsMap.put(job.getUser(),completedUserJobs);
            }
            completedUserJobs.addCompletedJob(job);
        } finally {
            this.completedJobLock.unlock();
        }
    }

    public Job waitForJobCompletion(String user)
    {
        this.completedJobLock.lock();
        CompletedUserJobs completedUserJobs;
        try {
            completedUserJobs = this.completeJobsMap.get(user);
            if (completedUserJobs == null)
            {
                completedUserJobs = new CompletedUserJobs();
                this.completeJobsMap.put(user,completedUserJobs);
            }
        } finally {
            this.completedJobLock.unlock();
        }
        return completedUserJobs.getCompletedJob();
    }

    public boolean addPendingJob(Job job)
    {
        this.pendingJobsLock.lock();
        try
        {
            if (job.getMemory() <= this.getMaxWorkerMemory())
            {
                this.pendingJobs.offer(job);
                this.pendingJobsCondition.signalAll();
                return true;
            } else return false;
        } finally {
            this.pendingJobsLock.unlock();
        }
    }

    public void addWorker(WorkerConnectionHandler worker)
    {
        this.workerListLock.lock();
        try
        {
            this.workers.add(worker);
            System.out.println("Added new worker with id " + worker.getId());
        } finally {
            this.workerListLock.unlock();
        }
    }


    public void updateMaxMemorySingle(int memory)
    {
        if (this.getMaxWorkerMemory() < memory)
        {
            this.maxMemoryLock.writeLock().lock();
            try {
                this.maxWorkerMemory = memory;
            } finally {
                this.maxMemoryLock.writeLock().unlock();
            }
        }
    }

    public void updateMaxMemory()
    {
        this.workerListLock.lock();
        int max = 0;
        try
        {
            for (WorkerConnectionHandler worker : this.workers)
            {
                int workerTotal = worker.getTotalMemory();
                if (max < workerTotal)
                {
                    max = workerTotal;
                }
            }
            this.maxMemoryLock.writeLock().lock();
            try {
                this.maxWorkerMemory = max;
            } finally {
                this.maxMemoryLock.writeLock().unlock();
            }
        } finally {
            this.workerListLock.unlock();
        }
    }

    public void removeWorker(WorkerConnectionHandler worker)
    {
        this.workerListLock.lock();
        try
        {
            this.workers.remove(worker);
            System.out.println("Removed worker with id " + worker.getId());
            this.updateMaxMemory();
            System.out.println(this.getMaxWorkerMemory());
        } finally {
            this.workerListLock.unlock();
        }
        this.dumpExecutingWorkerJobs(worker.getId());
    }

    /**
     * Puts all the jobs in execution in the worker server back into the pending jobs queue.
     * @param workerId Id of the worker server
     */
    public void dumpExecutingWorkerJobs(int workerId)
    {
        this.executionJobsLock.lock();
        List<Job> workerJobs = this.executingJobs.get(workerId);
        try {
            if (workerJobs == null)
            {
                return;
            }
            if (workerJobs.isEmpty())
            {
                return;
            }
        } finally {
            this.executionJobsLock.unlock();
        }
        for (Job job : workerJobs) {
            if (job.getMemory() <= this.maxWorkerMemory)
            {
                job.setState(Job.PENDING);
                this.addPendingJob(job);
            }
            else
            {
                this.addErrorJob((new Job(job.getId(),job.getUser(),"ERROR".getBytes(),job.getMemory(), Job.ERROR)));
            }
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
            Job job;
            try
            {
                while (this.pendingJobs.isEmpty())
                {
                    System.out.println("Waiting for new Jobs!");
                    try
                    {
                        this.pendingJobsCondition.await();
                    }
                    catch (InterruptedException e)
                    {
                        throw new RuntimeException(e);
                    }
                }
                job = this.pendingJobs.poll();
            }
            finally
            {
                this.pendingJobsLock.unlock();
            }
            boolean sucess = false;
            int workerId = -1;
            this.executionJobsLock.lock();
            try
            {
                while (!sucess)
                {
                    this.workerListLock.lock();
                    try {
                        for (WorkerConnectionHandler worker : this.workers)
                        {
                            if (job.getMemory() + worker.getUsedMemory() <= worker.getTotalMemory())
                            {
                                sucess = true;
                                workerId = worker.getId();
                                job.setState(Job.EXECUTING);
                                worker.sendJobRequest(job);
                                break;
                            }
                        }
                    } finally {
                        this.workerListLock.unlock();
                    }
                    if (!sucess)
                    {
                        this.executionCondition.await();
                    } else break;
                }
                if (workerId != -1)
                {
                    List<Job> workerJobs = this.executingJobs.get(workerId);
                    if (workerJobs == null)
                    {
                        workerJobs = new ArrayList<>();
                        this.executingJobs.put(workerId,workerJobs);
                    }
                    workerJobs.add(job);
                    System.out.println("Put job " + job.getId() + " from user " + job.getUser() + " in executing jobs map in worker " + workerId);
                } else System.out.println("SOMETHING WENTTT VERY VERY WRONGGGGGGGGGGGG");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally
            {
                this.executionJobsLock.unlock();
            }
        }
    }

}
