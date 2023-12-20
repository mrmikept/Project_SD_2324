package Cliente;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class ClientController {
    private ClientSystem system;
    private ClientInterface cliInterface;

    public ClientController(ClientSystem system, ClientInterface cliInterface) {
        this.system = system;
        this.cliInterface = cliInterface;
    }

    public boolean isDigit(String string)
    {
        return string.matches("\\d");
    }

    public int startMenu() throws IOException, InterruptedException {
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
                            System.out.println(response);
                            System.out.println("Press any key to go back.");
                            input.readLine();
                            option = 0;
                            break;
                        } else {
                            System.out.println(response);
                            System.out.println("Press any key to go back.");
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

                        System.out.println(response);
                        System.out.println("Press any key to go back.");
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

    public void clientMenu()
    {
        int option = 0;
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        do {
            switch (option)
            {
                case 0:

                    break;
                case 1:

                    break;
                case 2:

                    break;
                case 3:

                    break;
                case 4:

                    break;
            }
        } while (option != 10);
    }


    public void run() throws IOException, InterruptedException {
            int sucess = this.startMenu();
            if (sucess == -1)
            {
                this.system.close();
                return;
            }
            this.clientMenu();

        }

}
