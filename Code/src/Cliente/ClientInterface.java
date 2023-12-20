package Cliente;

import java.util.Scanner;

public class ClientInterface
{
    public String startMenu()
    {
        return "# Cloud Computing #\n" +
                "1 - Log in\n" +
                "2 - Create Account\n" +
                "0 - Exit\n" +
                "\n" +
                "Chose an option:";
    }

    public String clientMenu(String username)
    {
        return "# Cloud Computing #" +
                "Logged in account: " + username + "\n" +
                "1 - Job execution request\n" +
                "2 - Job status\n" +
                "3 - Service status\n" +
                "0 - Logout\n\n" +
                "Chose an option:";
    }
}
