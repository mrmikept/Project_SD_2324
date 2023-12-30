package Tests;

import Cliente.ClientController;
import Cliente.ClientSystem;
import Worker.Job;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

/**
 * Runnable class for the Test Class. ONLY FOR TESTS.
 */
public class TestThreads implements Runnable
{
    public String username;
    public String filename;

    public TestThreads(String username, String filename)
    {
        this.username = username;
        this.filename = filename;
    }

    @Override
    public void run()
    {
        ClientSystem clientSystem = new ClientSystem();
        clientSystem.start_connection("localhost",9090);
        String login = null;
        try {
            login = clientSystem.login(username,"1234");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (!login.startsWith("Failed;"))
        {
            Random r = new Random();
            try {
                byte[] jobCode = Files.readAllBytes(Path.of("/home/mikep/CloudServiceApp/ClientJobs/mikep/jobCodes",filename));
//                int memory = 0;
                for (int i = 0; i < 10; i++)
                {
                    int memory = r.nextInt(999) + 1;
//                    memory += 100;
                    clientSystem.jobExecRequest(i,jobCode,memory);
                    System.out.println("Sent job " + i + " of " + this.username + " memory " + memory);
                }
                for (int i = 0; i < 10; i++)
                {
                    Job job = clientSystem.waitJobResult();
                    if (job.getMemory() != -1)
                    {
                        System.out.println("Sucess executing job " + job.getId() + " from " + this.username);
                    } else System.out.println("Error executing job " + job.getId() + " from " + this.username + ";" + new String(job.getJobCode()));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
