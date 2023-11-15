import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

//"/Users/luca/Desktop/LEI-UM/UM/3 ano/SD/Project_SD_2324/Code/src"

public class Ficheiro {


    public static void escreverResultado(int tarefa, int resultaddo, String path) throws IOException {

        String pasta = Paths.get(path).toAbsolutePath().getParent().toString();
        String pasta2 = pasta + "/Resultados";
        File dir = new File(pasta2);

        if (dir.exists()) {
            File ficheiro = new File(pasta2, "teste.csv");
            ficheiro.createNewFile();
            FileWriter f = new FileWriter(ficheiro);
            f.write("Tarefa " + tarefa + ": " + resultaddo);
            f.close();
        }
        else {
            dir.mkdirs();
            File ficheiro = new File(pasta2, "teste.csv");
            ficheiro.createNewFile();
            FileWriter f = new FileWriter(ficheiro);
            f.write("Tarefa " + tarefa + ": " + resultaddo);
            f.close();

        }
    }


    public static void escreverErro(int tarefa, String mensagem, String path) throws IOException{
        String pasta = Paths.get(path).toAbsolutePath().getParent().toString();
        String pasta2 = pasta + "/Resultados";
        File dir = new File(pasta2);

        if (dir.exists()) {
            File ficheiro = new File(pasta2, "teste2.csv");
            ficheiro.createNewFile();
            FileWriter f = new FileWriter(ficheiro);
            f.write("Tarefa " + tarefa + ": (Erro) " + mensagem);
            f.close();
        }
        else {
            dir.mkdirs();
            File ficheiro = new File(pasta2, "teste2.csv");
            ficheiro.createNewFile();
            FileWriter f = new FileWriter(ficheiro);
            f.write("Tarefa " + tarefa + ": (Erro) " + mensagem);
            f.close();

        }
    }
}
