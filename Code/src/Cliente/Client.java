package Cliente;

import java.io.IOException;

public class Client {

    public static void main(String[] args) throws IOException, InterruptedException {
        String serverAddr = "localhost";
        int port = 9090;
        if (args.length < 2)
        {
            System.out.println("Please insert the Server address before starting the program.\n For exemple: ' java Client <server address> <port to use> '");
        }
        if (args.length > 0)
        {
            serverAddr = args[0];
        }
        if (args.length > 1)
        {
            port = Integer.parseInt(args[1]);
        }

        ClientSystem clientSystem = new ClientSystem();
        ClientInterface vista = new ClientInterface();
        ClientController controlador = new ClientController(clientSystem, vista);
        clientSystem.start(serverAddr,port);
        controlador.run();
    }

}
