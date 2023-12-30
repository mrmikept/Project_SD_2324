package Cliente;

import Worker.Job;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.locks.ReentrantLock;
import java.util.List;

/**
 * Controller class for the client system and client interface
 */
public class ClientController
{
    private final static String JOBSFOLDER = System.getProperty("user.home") + "/CloudServiceApp/ClientJobs/";
    private ClientSystem system;
    private ClientInterface cliInterface;
    private String username;
    private Thread jobNotifier;

    /**
     * Constructor for the Client controller between Client Interface and Client System
     * @param system Client system with all the methods to operate the program
     * @param cliInterface Client Interface with all the menu options available
     */
    public ClientController(ClientSystem system, ClientInterface cliInterface) {
        this.system = system;
        this.cliInterface = cliInterface;
        this.username = "";
        ReentrantLock printLock = new ReentrantLock();
        this.jobNotifier = new Thread(() ->
        {
            while (this.system.isLoggedIn()) {
                Job result = null;
                try {
                    result = this.system.waitJobResult();
                } catch (Exception e) {
                    break;
                }
                if (result.getState() == Job.ERROR)
                {
                    String string = new String(result.getJobCode());
                    if (string.equals("ERROR"))
                    {
                        printLock.lock();
                        try {
                            System.out.println("\n[Job Result] Received result from job " + result.getId());
                            System.out.println("An error has ocorred, cannot compute job, too much memory needed.");
                        } finally {
                            printLock.unlock();
                        }
                    }
                    else
                    {
                        String[] strings = string.split(";");
                        printLock.lock();
                        try {
                            System.out.println("\n[Job Result] Received result from Job " + result.getId());
                            System.out.println("Failed executing job, error code: " + strings[1] + "; Message: " + strings[2]);
                        } finally {
                            printLock.unlock();
                        }
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
            }
        });
        this.jobNotifier.setName("Job Notifier Thread");
    }

    /**
     * Checks if a string is composed with only digits.
     * @param string string to check
     * @return true if it's only Digits and false otherwise
     */
    public boolean isDigit(String string)
    {
        return string.matches("\\d+");
    }

    /**
     * Controller for the loggin and register menu of the client
     * @return returns 0 if sucessfull or -1 to exit the program safelly.
     * @throws Exception
     */
    public int startMenu() throws Exception {
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
                        System.out.println("\n# Login Menu #\n");
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
                            System.out.println("\n" + response + " logging in!");
                            System.out.println("\nPress any key to continue.");
                            input.readLine();
                            option = 10;
                            break;
                        }
                    case 2: // Register case
                        System.out.println("\n# Register an Account #\n");
                        System.out.println("Insert the username for the new Account: ");
                        username = input.readLine();
                        System.out.println("\nInsert the password for the new Account: ");
                        password = input.readLine();

                        response = this.system.register(username, password);

                        System.out.println("\n" + response + "!");
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

    /**
     * Controller for the logged in menu
     * @return -1 to signal the program to close.
     * @throws Exception
     */
    public int clientMenu() throws Exception {
        this.createJobsFolder();
        int option = 0;
        CodeGen mycodes = new CodeGen();
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        this.jobNotifier.start();
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
                    if (this.system.jobExecRequest(jobCode,fileData,memory))
                    {
                        System.out.println("\nSucessfully send request for job execution, with code " + jobCode + ".");
                    }
                    else
                    {
                        System.out.println("\nSorry, job can't be computed at the moment, too much memory nedded.");
                    }
                    System.out.println("Press any key to go back.");
                    input.readLine();
                    option = 0;
                    break;
                case 2: // Jobs status
                    List<String> list = this.system.getJobStatus();
                    System.out.println("# Job Status #\n");
                    if (list.size() > 0)
                    {
                        for (String str : list)
                        {
                            System.out.println(str);
                        }
                    }
                    else
                    {
                        System.out.println("No job requests found!\n");
                    }
                    System.out.println("Press any key to go back.");
                    input.readLine();
                    option = 0;
                    break;
                case 3: // Service status
                    String info = this.system.RequestServiceStatus();
                    System.out.println("# Service Status #\n");
                    System.out.println(info);
                    System.out.println("\nPress any key to go back.");
                    input.readLine();
                    option = 0;
                    break;
                case 4: // Logout
                    System.out.println("Logging out...");
                    if (this.system.logout())
                    {
                        System.out.println("Action sucessfull!\nClosing Program.");
                        option = 10;
                    }
                    else
                    {
                        System.out.println("Something went wrong... Try again later!\n");
                        option = 0;
                        System.out.println("Press any key to continue...");
                        input.readLine();
                    }
                    break;
            }
        } while (option != 10);
        return -1;
    }

    /**
     * Creates the necessary folders to put job codes and job results!
     */
    public void createJobsFolder()
    {
        File folder = new File(JOBSFOLDER + this.username);
        if (!folder.exists())
        {
            if ((new File(JOBSFOLDER + this.username + "/jobCodes/").mkdirs()))
            {
                System.out.println("Created folder for job Codes!");
            }
            if ((new File(JOBSFOLDER + this.username + "/jobResults/").mkdirs()))
            {
                System.out.println("Created folder to write job results!");
            }
        }
    }

    /**
     * Reads a file
     * @param filename name of the file.
     * @return byte array with content of the file.
     */
    public byte[] readJobCodeFile(String filename)
    {
        try
        {
            return Files.readAllBytes(Path.of(JOBSFOLDER,this.username,"jobCodes",filename));
        } catch (IOException e)
        {
            e.printStackTrace();
            System.out.println("Failed: " + e.getMessage());
            return null;
        }
    }

    /**
     * Writes the Result of a Job in the results folder
     * @param job Job to write
     */
    public void saveJobResult(Job job)
    {
        try
        {
            Files.write(Path.of(JOBSFOLDER,this.username,"jobResults","job " + job.getId() + "- Result"),job.getJobCode());
        } catch (IOException e)
        {
            System.out.println("Failed writing job result: " + e.getMessage());
        }
    }

    public void run() throws IOException, InterruptedException {
        try {
            int flag = 1;
            do {
                switch (flag)
                {
                    case 0: // ClientMenu
                        flag = this.clientMenu();
                        break;
                    case 1: // StartMenu
                        flag = this.startMenu();
                        break;
                }
            } while (flag != -1);
        } catch (Exception e)
        {
            System.out.println("Something went wrong... Closing Program...");
        } finally {
            System.out.println("Waiting for " + jobNotifier.getName());
            this.jobNotifier.interrupt();
            this.jobNotifier.join();
        }
    }

}
