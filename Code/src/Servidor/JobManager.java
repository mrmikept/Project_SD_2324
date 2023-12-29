package Servidor;

import Worker.CompletedUserJobs;
import Worker.Job;

import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

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

//    public Job waitForJobCompletion(Job job)
//    {
//        this.completedJobLock.lock();
//        try {
//            CompJobWaiters jobWaiters = this.completeJobsMap.get(job.getUser());
//            if (jobWaiters == null)
//            {
//                jobWaiters = new CompJobWaiters(this.completedJobLock, job.getUser());
//                this.completeJobsMap.put(job.getUser(),jobWaiters);
//            }
//            jobWaiters.addWaiter();
//
//            while (true)
//            {
//                Job result = jobWaiters.getJobResult(job.getId());
//                if (result != null)
//                {
//                    jobWaiters.removeWaiter();
//                    if (jobWaiters.isMapEmpty() && jobWaiters.getWaiters() == 0)
//                    {
//                        this.completeJobsMap.remove(job.getUser());
//                    }
//                    return result;
//                }
//                jobWaiters.condition.await();
//            }
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        } finally {
//            this.completedJobLock.unlock();
//        }
//    }

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

    public void removeWorker(WorkerConnectionHandler worker)
    {
        this.workerListLock.lock();
        try
        {
            this.workers.remove(worker);
            System.out.println("Removed worker with id " + worker.getId());
        } finally {
            this.workerListLock.unlock();
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
