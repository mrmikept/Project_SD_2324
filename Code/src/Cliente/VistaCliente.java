package Cliente;

import java.util.Scanner;

public class VistaCliente
{
    ControladorCliente cc;

    public VistaCliente(ControladorCliente cc){
        this.cc = cc;
    }
    public void run(){

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
                String email = cc.read_line();
                System.out.println("Introduza a sua password: ");
                String pass = cc.read_line();

                if (x == 1){
                    if (!cc.cliente_login(email, pass)) System.out.println("Login sem sucesso");
                }
                if (x == 2){
                    if (!cc.cliente_registo(email, pass)) System.out.println("Registo sem sucesso");
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
                        // Manda para o controlador as informações e no controlador chama a função para fazer o pedido
                        System.out.println("Código Tarefa: ");
                        String s1 = cc.read_line();

                        System.out.println("Memória necessária: ");
                        String s2 = cc.read_line();
                        cc.do_job(s1 + s2);

                    case 2:
                        // Manda para o controlador para ver a lista de pedidos

                    case 3:
                        // Manda para o controlador para ver o estado
                }
        } while (x != 0);
        System.exit(0);
    }


}
