package Worker;

import sd23.*;

/**
 * Runnable Class to execute a Job using the sd23.jar function given by the Professors.
 */
public class JobExecutor implements Runnable
{
    Worker worker;
    Job job;
    int result;

    public JobExecutor(Worker worker, Job job)
    {
        this.worker = worker;
        this.job = job;
    }

    /**
     * Executes a job and returns the result. Inspired from the exemple of the Professors.
     * @JobFunctionExeption Catches this exeption and handle the error message
     * @return Returns the job result or the error code and message from the executon.
     */
    public byte[] execJob()
    {
        try
        {
            System.out.println("Executing job " + this.job.getId() + " from user " + job.getUser());
            byte[] output = JobFunction.execute(this.job.getJobCode());
            this.result = Job.SUCESS;
            System.out.println("Sucess executing job " + this.job.getId() + " from user " + this.job.getUser() + ": returned " + output.length + " bytes.");
            return output;
        } catch (JobFunctionException e)
        {
            this.result = Job.ERROR;
            System.out.println("Failed executing job " + this.job.getId() + " from user " + this.job.getUser() + ": returned code " + e.getCode() + " message: " + e.getMessage());
            return ("Failed;" + e.getCode() + ";" + e.getMessage()).getBytes();
        }
    }

    public void run()
    {
        byte[] response = this.execJob();
        this.worker.sendCompletedJob(new Job(this.job.getId(),this.job.getUser(),response,this.job.getMemory(),this.result));
    }
}
