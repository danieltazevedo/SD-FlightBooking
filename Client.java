import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    
    public static void main(String[] args) {
        try {
            if(args.length==2) { 
                
            Socket socket = new Socket("localhost", 1234); //abre porta de conexão
            
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // input
            PrintWriter out = new PrintWriter(socket.getOutputStream()); //output

            BufferedReader systemIn = new BufferedReader(new InputStreamReader(System.in)); //in=input terminal

            out.println(args[0]); 
            out.println(args[1]); //escreve no input o ultilizar que faz login
            
           // int type = Integer.parseInt(in.readLine());
            /*if (type != 0) {
            System.out.println("Indique a operação que deseja efectuar.");
            //cliente
            if(type==1) {
            System.out.println("1-Reservar Viagem.");
            System.out.println("2-Cancelar Reserva.");
            System.out.println("3-Obter lista de todos os voos.");
            }

            if(type==2) {
            //adimitrador
            System.out.println("1-Inserção de voos.");
            System.out.println("2-Encerrar dia.");
            }
            
        }
        else{System.out.println("Ultilizador inválido.");}*/
            
            out.flush();
            socket.shutdownOutput();
            socket.shutdownInput();
            socket.close();
            }
            else {System.out.println("Introduza nome e password.");}
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}