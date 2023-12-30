package Cliente;

import java.io.IOException;

public class Client {

    public static void main(String[] args) throws IOException, InterruptedException {
        String serverAddr = "localhost";
        int port = 9090;
        if (args.length == 0)
        {
            System.out.println("Using default values for server Address: '" + serverAddr + "' and socket port: " + port);
        }
        if (args.length > 0)
        {
            serverAddr = args[0];
            System.out.println("Using server address: " + serverAddr);
        }
        if (args.length > 1)
        {
            port = Integer.parseInt(args[1]);
            System.out.println("Using socket port: " + port);
        }

        ClientSystem clientSystem = new ClientSystem();
        try {
            ClientInterface vista = new ClientInterface();
            ClientController controlador = new ClientController(clientSystem, vista);
            if (clientSystem.start(serverAddr,port))
            {
                controlador.run();
            } else System.out.println("Could'nt start Client Program..");
        } catch (Exception e)
        {
            System.out.println("Something went wrong.. Closing Program..");
        } finally {
            clientSystem.close();
        }

    }

}
