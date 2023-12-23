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
        ClientInterface vista = new ClientInterface();
        ClientController controlador = new ClientController(clientSystem, vista);
        if (clientSystem.start(serverAddr,port))
        {
            controlador.run();
        }
    }

}
