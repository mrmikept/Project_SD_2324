package Worker;

import sd23.*;

public class SingleWorker implements Runnable
{
    WorkerServer server;
    Job job;
    int result;

    public SingleWorker(WorkerServer server, Job job)
    {
        this.server = server;
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
            this.result = 0;
            System.out.println("Sucess executing job " + this.job.getId() + " from user " + this.job.getUser() + ": returned " + output.length + " bytes.");
            return output;
        } catch (JobFunctionException e)
        {
            this.result = -1;
            System.out.println("Failed executing job " + this.job.getId() + " from user " + this.job.getUser() + ": returned code " + e.getCode() + " message: " + e.getMessage());
            return ("Failed;" + e.getCode() + ";" + e.getMessage()).getBytes();
        }
    }

    public void run()
    {
        byte[] response = this.execJob();
        this.server.addCompletedJob(this.job,response, this.result);
    }
}
