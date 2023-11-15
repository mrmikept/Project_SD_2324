package Cliente;

import java.util.Scanner;

public class VistaCliente
{
    ControladorCliente cc;
    public void run(){

        this.cc = new ControladorCliente();

        menuInicio();
        MenuCliente();
    }
    public void menuInicio(){

        Scanner scanner = new Scanner(System.in);
        int x = scanner.nextInt();

        do {
            System.out.println("1- Inicie a Sessão");
            System.out.println("2- Registar Conta");
            System.out.println("0- Sair");

            if (x == 1 || x == 2){
                System.out.println("Insira o seu email: ");
                cc.read_email();
                System.out.println("Introduza a sua password: ");
                cc.read_pass();

                if (x == 1){
                    if (!cc.cliente_login()) System.out.println("Login sem sucesso");
                }
                if (x == 2){
                    if (!cc.cliente_registo()) System.out.println("Registo sem sucesso");
                }
            }
        }while (x != 0);

        if (x == 0){
            System.out.println("A fechar o programa...");
            System.exit(0);
        }
    }

    public void MenuCliente(){

        Scanner scanner = new Scanner(System.in);
        int x = scanner.nextInt();

        do {
            System.out.println("1- Fazer pedido");
            System.out.println("2- Ver lista de pedidos");
            System.out.println("3- Ver estado");
            System.out.println("0- Sair");

                switch (x){
                    case 1:
                        System.out.println("Código Tarefa: ");
                        //cc.readTarefa()

                        System.out.println("Memória necessária: ");
                        //cc.readMemory()

                        // Manda para o controlador as informações e no controlador chama a função para fazer o pedido

                    case 2:
                        // Manda para o controlador para ver a lista de pedidos

                    case 3:
                        // Manda para o controlador para ver o estado
                }
        } while (x != 0);

    }


}
