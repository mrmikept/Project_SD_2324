package Cliente;

public class Cliente {

    public static void main(String[] args) {
        Sistema sistema = new Sistema();
        ControladorCliente controlador = new ControladorCliente(sistema);
        VistaCliente vista = new VistaCliente(controlador);

        vista.run();
    }

}
