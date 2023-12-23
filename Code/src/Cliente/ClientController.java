package Cliente;

import Worker.Job;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.locks.ReentrantLock;

public class ClientController
{
    private final static String JOBSFOLDER = System.getProperty("user.home") + "/CloudServiceApp/ClientJobs/";
    private ClientSystem system;
    private ClientInterface cliInterface;
    private String username;

    public ClientController(ClientSystem system, ClientInterface cliInterface) {
        this.system = system;
        this.cliInterface = cliInterface;
        this.username = "";
    }

    public boolean isDigit(String string)
    {
        return string.matches("\\d+");
    }

    public int startMenu() throws IOException {
        int option = 0;
        String username, password, response;
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
            do {
                switch (option) {
                    case 0:
                        System.out.println(this.cliInterface.startMenu());
                        String in = input.readLine();
                        if (isDigit(in)) {
                            option = Integer.parseInt(in);
                            if (option > 2 || option < 0) {
                                option = 0;
                                break;
                            }
                            if (option == 0) {
                                option = 3;
                                break;
                            }
                            break;
                        }
                    case 1: // Login case
                        System.out.println("\n\n# Login Menu #\n\n");
                        System.out.println("Insert the username of the Account: ");
                        username = input.readLine();
                        System.out.println("\nInsert the password of the Account: ");
                        password = input.readLine();

                        response = this.system.login(username, password);
                        if (response.startsWith("Failed")) {
                            System.out.println("\n" + response);
                            System.out.println("\nPress any key to go back.");
                            input.readLine();
                            option = 0;
                            break;
                        } else {
                            this.username = username;
                            System.out.println("\n" + response);
                            System.out.println("\nPress any key to go back.");
                            input.readLine();
                            option = 10;
                            break;
                        }
                    case 2: // Register case
                        System.out.println("\n\n# Register an Account #\n\n");
                        System.out.println("\nInsert the username for the new Account: ");
                        username = input.readLine();
                        System.out.println("\nInsert the password for the new Account: ");
                        password = input.readLine();

                        response = this.system.register(username, password);

                        System.out.println("\n" + response);
                        System.out.println("\nPress any key to go back.");
                        input.readLine();
                        option = 0;
                        break;
                    case 3: // Exit case
                        System.out.println("Exiting Program...");
                        return -1;
                }
            } while (option != 10);
        return 0;

    }

    public void clientMenu() throws IOException {
        int option = 0;
        CodeGen mycodes = new CodeGen();
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        ReentrantLock printLock = new ReentrantLock();
        Thread completedJobNotificatorAndWriter = new Thread(() -> {
            do {
                Job result = this.system.waitJobResult();
                if (result.getMemory() == -1)
                {
                    String[] strings = new String(result.getJobCode()).split(";");
                    printLock.lock();
                    try {
                        System.out.println("\n[Job Result] Received result from Job " + result.getId());
                        System.out.println("Failed executing job, error code: " + strings[1] + "; Message: " + strings[2]);
                    } finally {
                        printLock.unlock();
                    }
                }
                else
                {
                    printLock.lock();
                    try {
                        System.out.println("\n[Job result] Received result from Job " + result.getId());
                        System.out.println("Sucess executing job, received " + result.getJobCode().length + " bytes\n");
                        System.out.println("Saving result into a file in results folder!");
                    } finally {
                        printLock.unlock();
                    }
                    this.saveJobResult(result);
                }
            } while (true);
        });
        completedJobNotificatorAndWriter.start();
        do {
            switch (option)
            {
                case 0: // Logged in main menu
                    System.out.println(this.cliInterface.clientMenu(this.username));
                    String in = input.readLine();
                    if (!this.isDigit(in))
                    {
                        option = 0;
                        break;
                    }
                    option = Integer.parseInt(in);
                    if (option < 0 || option > 3)
                    {
                        option = 0;
                        break;
                    }
                    if (option == 0)
                    {
                        option = 4;
                        break;
                    }
                    break;
                case 1: // Job execution request
                    System.out.println("# Job Execution Request #\n");
                    System.out.println("Please insert job code file name: ");
                    String filename = input.readLine();
                    byte[] fileData = this.readJobCodeFile(filename);
                    if (fileData == null)
                    {
                        System.out.println("\nPress any key to go back.");
                        input.readLine();
                        option = 0;
                        break;
                    }
                    System.out.println("\nPlease insert the total amout of memory required for this job: ");
                    in = input.readLine();
                    System.out.println("in: " + in);
                    if (!this.isDigit(in))
                    {
                        System.out.println("\nPlease insert a number value\n");
                        System.out.println("Press any key to go back.");
                        input.readLine();
                        option = 0;
                        break;
                    }

                    int memory = Integer.parseInt(in);
                    int jobCode = mycodes.newCode();
                    this.system.jobExecRequest(jobCode,fileData,memory);

                    System.out.println("\nSucessfully send request for job execution, with code " + jobCode + ".");
                    System.out.println("Press any key to go back.");
                    input.readLine();
                    option = 0;
                    break;
                case 2: // Jobs status

                    break;
                case 3: // Service status

                    break;
                case 4: // Logout
                    System.out.println("Logging out...");
                    if (this.system.logout())
                    {
                        System.out.println("Action sucessfull!\n");
                        option = 10;
                    }
                    else
                    {
                        System.out.println("Something went wrong... Try again later!\n");
                        option = 0;
                    }
                    System.out.println("Press any key to continue...");
                    input.readLine();
                    break;
            }
        } while (option != 10);
    }

    public void createJobsFolder()
    {
        File folder = new File(JOBSFOLDER);
        if (!folder.exists())
        {
            if ((new File(JOBSFOLDER + "/jobCodes/").mkdirs()))
            {
                System.out.println("Created folder for job Codes!");
            }
            if ((new File(JOBSFOLDER + "/jobResults/").mkdirs()))
            {
                System.out.println("Created folder to write job results!");
            }
        }
    }

    public byte[] readJobCodeFile(String filename)
    {
        try
        {
            return Files.readAllBytes(Path.of(JOBSFOLDER,"jobCodes",filename));
        } catch (IOException e)
        {
            e.printStackTrace();
            System.out.println("Failed: " + e.getMessage());
            return null;
        }
    }

    public void saveJobResult(Job job)
    {
        try
        {
            Files.write(Path.of(JOBSFOLDER,"jobResults","job " + job.getId() + "result"),job.getJobCode());
        } catch (IOException e)
        {
            System.out.println("Failed writing job result: " + e.getMessage());
        }
    }

    public void run() throws IOException, InterruptedException {
        this.createJobsFolder();
        int flag = 1;
        do {
            switch (flag)
            {
                case 0: // ClientMenu
                    this.clientMenu();
                    flag = 1;
                    break;
                case 1: // StartMenu
                    flag = this.startMenu();
                    break;
            }
        } while (flag != -1);

        this.system.close();
        }

}
