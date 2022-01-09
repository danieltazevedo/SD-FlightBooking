import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class Client {

    public static void main(String[] args) throws IOException {
        Demultiplexer m = null;
        try {
            Socket s = new Socket("localhost", 12345);
            m = new Demultiplexer(new Conexao(s));

            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
            m.start();

            String username = null;

            while (username == null) {
                System.out.println("**Reserva de Voos**\n\n"
                        + "Insira a opcao desejada\n"
                        + "1) Iniciar sessão.\n"
                        + "2) Registar nova conta.\n\n");

                String option = stdIn.readLine();
                if (option.equals("1")) {
                    System.out.print("***INICIAR SESSÃO***\n\n"
                            + "Introduza o seu nome: ");
                    String nome = stdIn.readLine();
                    System.out.print("Introduza a sua palavra-passe: ");
                    String password = stdIn.readLine();
                    m.send(0, nome, password.getBytes());
                    String response = new String(m.receive(0));
                    if (!response.startsWith("Erro")) {
                        username = nome;
                    }
                    System.out.println("\n" + response + "\n");
                } else if (option.equals("2")) {
                    System.out.print("***REGISTAR NOVA CONTA***\n"
                            + "\n"
                            + "Introduza o seu nome: ");
                    String nome = stdIn.readLine();
                    System.out.print("Introduza a sua palavra-passe: ");
                    String password = stdIn.readLine();
                    m.send(1, nome, password.getBytes());
                    String response = new String(m.receive(1));
                    if (!response.startsWith("Erro")) {
                        username = nome;
                    }
                    System.out.println("\n" + response + "\n");
                }
            }

            boolean exit = false;
            while (!exit) {
                System.out.print("\n***Reserva de Voos***\n\n"
                        + "O que pretende fazer?\n"
                        + "1) Reserva de uma viagem.\n"
                        + "2) Cancelamento da reserva de uma viagem.\n"
                        + "3) Obter da lista de todas os voos existentes.\n"
                        + (username.startsWith("admin") ? "4) Inserção de informação sobre voos.\n" : "")
                        + (username.startsWith("admin") ? "5) Encerramento de um dia.\n" : "")
                        + "0) Sair.\n\n"
                        + "Insira o valor corresponde à operação desejada: ");

                String option = stdIn.readLine();
                switch (option) {
                    case "0":
                        m.send(99, username, new byte[0]);
                        exit = true;
                        break;
                    case "1":
                        System.out.println("Você escolheu reservar viagem\n");
                        break;
                    case "2":
                        System.out.println("Você escolheu cancelar a reserva de uma viagem.\n");
                        break;
                    case "3":
                        System.out.println("Você escolheu Obter da lista de todas os voos existentes.\n");
                        break;
                    case "4":
                        if (username.startsWith("admin")) {
                            System.out.println("Você escolheu Obter da lista de todas os voos existentes.\n");
                            break;
                        }
                    case "5":
                        if (username.startsWith("admin")) {
                            System.out.println("Você escolheu Encerramento de um dia.\n");
                            break;
                        }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        m.close();
    }
}
