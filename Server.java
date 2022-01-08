import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.io.*;

public class Server {
    public static void main(String[] args) {
        try {
            File f = new File("contas.ser");
            Contas contas;
            if(!f.exists())
            contas = new Contas();
            else
            contas = Contas.deserialize("contas.ser");

            f=new File("viagens.ser");
            Viagem viagens;
            if(!f.exists())
            viagens = new Viagem();
            else
            viagens = Viagem.deserialize("viagens.ser");
           

            ServerSocket ss = new ServerSocket(1234);
            Socket socket = ss.accept();
            BufferedReader ini = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream());
            String user = ini.readLine();
            String pass = ini.readLine();
            System.out.println(user);///
            System.out.println(pass);///
            out.println("1");
           /* if (contas.accountExists(user)) {
                String p =contas.getPassword(user);
                if (p.equals(pass)) {
                    out.println("1");
                }
                else{out.println("1");}
            }*/

            
            

                        
            
            
            socket.shutdownOutput();
            socket.shutdownInput();
            socket.close();
            
        } catch (IOException e) {
            e.printStackTrace();}
        catch (ClassNotFoundException e) {
            e.printStackTrace();
            } 
    }
}