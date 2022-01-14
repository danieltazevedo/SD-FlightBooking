import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

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
                        + "1) Reservar uma viagem.\n"
                        + "2) Cancelar reserva de uma viagem.\n"
                        + "3) Obter lista de voos existentes.\n"
                        + "4) Lista de todos os percursos possíveis limitados a duas escalas\n"
                        + (username.equals("admin") ? "5) Inserir informação sobre voos.\n" : "")
                        + (username.equals("admin") ? "6) Encerramento de um dia.\n" : "")
                        + (username.equals("admin") ? "7) Obter lista de reservas.\n" : "")
                        + "0) Sair.\n\n"
                        + "Insira o valor corresponde à operação desejada: ");

                String option = stdIn.readLine();
                switch (option) {
                    case "0":
                        m.send(99, username, new byte[0]);
                        exit = true;
                        break;
                    case "1":
                        System.out.println("***RESERVAR VIAGEM***\n");
                        System.out.println("Insira o percurso completo. Digite END quando terminar:");
                        String line = stdIn.readLine();
                        List<String> cidades = new ArrayList<>();

                        // ler o percurso
                        while(!(line.equals("END"))){
                            cidades.add(line.toLowerCase());
                            line = stdIn.readLine();
                        }
                        StringBuilder sb = new StringBuilder();
                        sb.append(cidades.size());
                        for(String cid : cidades){
                            sb.append(";").append(cid);
                        }

                        // ler as datas
                        System.out.println("Insira as datas (DD/MM/AAAA): ");
                        System.out.print("Data inicio: ");
                        String dataInicio = stdIn.readLine();
                        System.out.print("Data final: ");
                        String dataFinal = stdIn.readLine();

                        // enviar percurso e datas:    lisboa;porto;terceira;20/12/2021;25/12/2021
                        sb.append(";").append(dataInicio).append(";").append(dataFinal);
                        m.send(20,username,sb.toString().getBytes());
                        String respostaReserva = new String(m.receive(20), StandardCharsets.UTF_8);
                        System.out.println(respostaReserva);
                        break;
                    case "2":
                        System.out.println("***CANCELAR RESERVA***\n\n"
                                + "Indique o código de reserva: ");
                        String codigoReserva = stdIn.readLine();

                        m.send(44,username,codigoReserva.getBytes());
                        String respostaCancelamento = new String(m.receive(44), StandardCharsets.UTF_8);
                        System.out.println(respostaCancelamento);

                        break;
                    case "3":
                        System.out.println("***LISTA DE VOOS***\n");
                        m.send(80,username,new byte[0]);
                        String resposta = new String(m.receive(80), StandardCharsets.UTF_8);
                        System.out.println(resposta);
                        break;
                    case "4":
                        System.out.println("***LISTA DE PERCURSOS LIMITADOS A DUAS ESCALAS***\n");
                        System.out.print("Origem: ");
                        String origem = stdIn.readLine().toLowerCase();
                        System.out.print("Destino: ");
                        String destino = stdIn.readLine().toLowerCase();
                        StringBuilder sbp = new StringBuilder(origem + ";" + destino);

                        m.send(33,username,sbp.toString().getBytes());
                        String percursos = new String(m.receive(33), StandardCharsets.UTF_8);
                        System.out.println("\n" + percursos);


                        break;
                    case "5":
                        if (username.equals("admin")) {
                            System.out.println("***INSERIR INFORMACAO SOBRE VOOS***\n\n");
                            System.out.print("Origem: ");
                            String ori = stdIn.readLine().toLowerCase();
                            System.out.print("Destino: ");
                            String des = stdIn.readLine().toLowerCase();
                            System.out.print("Capacidade: ");
                            String capacidade = stdIn.readLine();
                            System.out.println(capacidade);
                            StringBuilder sbb = new StringBuilder();
                            sbb.append(ori).append(";").append(des).append(";").append(capacidade);
                            m.send(55,username,sbb.toString().getBytes()); // quero inserir informacao

                            String response = new String(m.receive(55), StandardCharsets.UTF_8);
                            System.out.println(response);
                            break;
                        }
                    case "6":
                        if (username.equals("admin")) {
                            System.out.println("***ENCERRAMENTO DE DIA***\n\n");
                            System.out.print("Insira o dia(DD/MM/AAAA): ");
                            String dia = stdIn.readLine();
                            m.send(7,username,dia.getBytes());
                            String response = new String(m.receive(7), StandardCharsets.UTF_8);
                            System.out.println(response);
                            break;
                        }
                    case "7":
                        if (username.equals("admin")) {
                            System.out.println("***LISTA DE RESERVAS***\n\n");
                            m.send(11,username,new byte[0]);
                            String response = new String(m.receive(11), StandardCharsets.UTF_8);
                            System.out.println(response);
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
