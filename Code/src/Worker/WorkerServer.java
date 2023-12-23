package Worker;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Queue;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import sd23.*;

public class WorkerServer implements Runnable
{
    private final int totalMemory; // Total memory of the server
    private Queue<Job> pendingJobs; // Queue with job with pending execution
    private ReentrantLock pendingLock;  // Lock for the pending execution queue
    private Condition pendingCondition; // Condition for the pending execution queue
    private Map<String, CompJobWaiters> completedJobs; // Key -> Username; Value -> CompJobWaiters
    private ReentrantLock completedJobLock; // Lock for the completedJob map
    private Condition completedCondition; // Condition for the completeJobs Map
    private int usedMemory; // Used memory
    private ReentrantLock memoryLock;

    public WorkerServer(int memory)
    {
        this.pendingJobs = new ArrayDeque<>();
        this.completedJobs = new HashMap<>();
        this.completedJobLock = new ReentrantLock();
        this.completedCondition = this.completedJobLock.newCondition();
        this.totalMemory = memory;
        this.usedMemory = 0;
        this.pendingLock = new ReentrantLock();
        this.pendingCondition = this.pendingLock.newCondition();
        this.memoryLock = new ReentrantLock();
    }

    /**
     * Adds memory from the used Memory
     * @param memory amount of memory to add
     */
    public void addMemory(int memory)
    {
        this.memoryLock.lock();
        try {
            this.usedMemory += memory;
        } finally {
            this.memoryLock.unlock();
        }
    }

    /**
     * Removes memory from the used Memory
     * @param memory amount of memory to remove
     */
    public void removeMemory(int memory)
    {
        this.memoryLock.lock();
        try {
            this.usedMemory -= memory;
        } finally {
            this.memoryLock.unlock();
        }
    }

    /**
     * Gets the used memory of the server
     * @return
     */
    public int getUsedMemory()
    {
        this.memoryLock.lock();
        try {
            return this.usedMemory;
        } finally {
            this.memoryLock.unlock();
        }
    }

    /**
     * Adds a job to wait to be executed
     * @param job Job to be executed
     */
    public void addPendingJob(Job job)
    {
        this.pendingLock.lock();
        try
        {
            System.out.println("Jog added");
            this.pendingJobs.add(job);
            this.pendingCondition.signalAll();
            System.out.println("Signaled");
        } finally {
            this.pendingLock.unlock();
        }
    }

    /**
     * Adds a job execution result.
     * @param job Job that was executed
     * @param result Result of the job
     */
    public void addCompletedJob(Job job, byte[] result)
    {
        this.completedJobLock.lock();
        try
        {
            CompJobWaiters jobWaiters = this.completedJobs.get(job.getUser());
            if (jobWaiters == null)
            {
                jobWaiters = new CompJobWaiters(this.completedJobLock);
                this.completedJobs.put(job.getUser(),jobWaiters);
            }
            jobWaiters.addJob(job.getId(),result);
            this.pendingLock.lock();
            try {
                this.removeMemory(job.getMemory());
                this.completedCondition.signalAll(); // Signal Thread that waits for memory to use
            } finally {
                this.pendingLock.unlock();
            }
            jobWaiters.condition.signalAll(); // Signal all the threads waiting for the completiong for a job from a certain user
        } finally {
            this.completedJobLock.unlock();
        }
    }

    /**
     * Waits for a job result
     * @param job Job to execute
     * @return byte array with the job result
     */
    public byte[] fetchCompletedJob(Job job)
    {
        this.completedJobLock.lock();
        try {
            CompJobWaiters jobWaiters = this.completedJobs.get(job.getUser());

            if (jobWaiters == null)
            {
                jobWaiters = new CompJobWaiters(this.completedJobLock);
                this.completedJobs.put(job.getUser(),jobWaiters);
            }
            jobWaiters.addWaiter();

            while (true)
            {
                byte[] result = jobWaiters.getJobResult(job.getId());
                if (result != null)
                {
                    jobWaiters.removeWaiter();
                    if (jobWaiters.isMapEmpty() && jobWaiters.getWaiters() == 0)
                    {
                        this.completedJobs.remove(job.getUser());
                    }
                    return result;
                }
                jobWaiters.condition.await(); // TODO Aqui esta condição acorda todas as threads que estão à espera na classe jobWaiters, ter em consideração que a maneira mais eficiente de fazer isto seria acordar apenas a thread à espera da resposta do seu especifico Job!
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            this.completedJobLock.unlock();
        }
    }

    /**
     * Created a Thread from SingleWorker class to execute a job.
     * @param job
     */
    public void execJob(Job job)
    {
        System.out.println("Creating thread to execute job");
        this.addMemory(job.getMemory());
        Thread worker = new Thread(new SingleWorker(this, job));
        worker.setName("Worker for job " + job.getId() + " from user " + job.getUser());
        worker.start();
    }

    /**
     * Run function that selects a job to be executed from the pending job's queue. Also verifies if the memory of the job don't pass the total memory of the server, if yes then waits
     */
    @Override
    public void run()
    {
        System.out.println("Started WorkerServer");
        while (true)
        {
            this.pendingLock.lock();
            Job job = null;
            try {
                while (this.pendingJobs.isEmpty())
                {
                    try {
                        this.pendingCondition.await(); // Waits for new jobs to execute
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                System.out.println("WorkerServer: New job found!");
                job = this.pendingJobs.poll();
            } finally {
                this.pendingLock.unlock();
            }
            while (this.getUsedMemory() + job.getMemory() >= this.totalMemory)
            {
                try {
                    System.out.println("Waiting for free memory");
                    this.completedCondition.await(); // Waits for memory
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            this.execJob(job);
        }
    }
}
