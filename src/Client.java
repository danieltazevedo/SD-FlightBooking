import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Client {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    public static void main(String[] args) throws IOException {
        Demultiplexer m = null;
        try {
            Socket s = new Socket("localhost", 12345);
            m = new Demultiplexer(new Conexao(s));

            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
            m.start();

            String username = null;

            while (username == null) {
                System.out.println(ANSI_PURPLE +"**Reserva de Voos**\n\n"+ ANSI_RESET
                        + ANSI_WHITE + "Insira a opcao desejada\n"
                        + "1) Iniciar sessão.\n"
                        + "2) Registar nova conta.\n\n" + ANSI_RESET);

                String option = stdIn.readLine();
                if (option.equals("1")) {
                    System.out.print(ANSI_BLUE +"***INICIAR SESSÃO***\n\n" + ANSI_RESET
                            + ANSI_YELLOW + "Introduza o seu nome: " + ANSI_RESET);
                    String nome = stdIn.readLine();
                    System.out.print(ANSI_YELLOW + "Introduza a sua palavra-passe: " + ANSI_RESET);
                    String password = stdIn.readLine();
                    m.send(0, nome, password.getBytes());
                    String response = new String(m.receive(0));
                    if (!response.startsWith("Erro")) {
                        username = nome;
                        System.out.println(ANSI_GREEN + "\n" + response + "\n" + ANSI_RESET);
                    } else {
                        System.out.println(ANSI_RED +"\n" + response + "\n" + ANSI_RESET);
                    }
                    } else if (option.equals("2")) {
                    System.out.print(ANSI_BLUE +"***REGISTAR NOVA CONTA***\n" + ANSI_RESET
                            + "\n"
                            + ANSI_YELLOW + "Introduza o seu nome: " + ANSI_RESET);
                    String nome = stdIn.readLine();
                    System.out.print(ANSI_YELLOW + "Introduza a sua palavra-passe: " + ANSI_RESET);
                    String password = stdIn.readLine();
                    m.send(1, nome, password.getBytes());
                    String response = new String(m.receive(1));
                    if (!response.startsWith("Erro")) {
                        username = nome;
                        System.out.println(ANSI_GREEN + "\n" + response + "\n" + ANSI_RESET);
                    } else {
                        System.out.println(ANSI_RED +"\n" + response + "\n" + ANSI_RESET);
                    }
                }
            }

            boolean exit = false;
            while (!exit) {
                System.out.print(ANSI_PURPLE + "\n***Reserva de Voos***\n\n" + ANSI_RESET
                        + "O que pretende fazer?\n"
                        + "1) Reservar uma viagem.\n"
                        + "2) Cancelar reserva de uma viagem.\n"
                        + "3) Obter lista de voos existentes.\n"
                        + "4) Lista de todos os percursos possíveis limitados a duas escalas\n"
                        + "5) Obter minhas reservas\n"
                        + (username.equals("admin") ? "6) Inserir informação sobre voos.\n" : "")
                        + (username.equals("admin") ? "7) Encerramento de um dia.\n" : "")
                        + (username.equals("admin") ? "8) Obter lista de reservas.\n" : "")
                        + "0) Sair.\n\n"
                        + "Insira o valor corresponde à operação desejada: ");

                String option = stdIn.readLine();
                switch (option) {
                    case "0":
                        m.send(99, username, new byte[0]);
                        exit = true;
                        break;
                    case "1":
                        System.out.println(ANSI_PURPLE +"***RESERVAR VIAGEM***\n" + ANSI_RESET);
                        System.out.println(ANSI_YELLOW + "Insira o percurso completo. Digite END quando terminar:" + ANSI_RESET);
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
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/MM/yyyy");
                        System.out.println(ANSI_YELLOW +"Insira as datas (DD/MM/AAAA): "+ ANSI_RESET);
                        System.out.print(ANSI_YELLOW +"Data inicio: "+ ANSI_RESET);
                        String dataInicio = stdIn.readLine();
                        while(LocalDate.now().isAfter(LocalDate.parse(dataInicio, formatter))){
                            System.out.print(ANSI_RED +"Indique uma data válida\n"+ ANSI_RESET);
                            dataInicio = stdIn.readLine();
                        }
                        System.out.print(ANSI_YELLOW +"Data final: "+ ANSI_RESET);
                        String dataFinal = stdIn.readLine();
                        while(LocalDate.parse(dataInicio, formatter).isAfter(LocalDate.parse(dataFinal, formatter))){
                            System.out.print(ANSI_RED +"Indique uma data válida\n"+ ANSI_RESET);
                            dataFinal = stdIn.readLine();
                        }

                        // enviar percurso e datas:    lisboa;porto;terceira;20/12/2021;25/12/2021
                        sb.append(";").append(dataInicio).append(";").append(dataFinal);
                        m.send(20,username,sb.toString().getBytes());
                        String respostaReserva = new String(m.receive(20), StandardCharsets.UTF_8);
                        if(respostaReserva.startsWith("Erro")) {
                            System.out.println(ANSI_RED + respostaReserva + ANSI_RESET);
                        } else {
                            System.out.println(ANSI_GREEN + respostaReserva + ANSI_RESET);
                        }
                        break;
                    case "2":
                            System.out.println(ANSI_PURPLE + "***CANCELAR RESERVA***\n\n" + ANSI_RESET
                                + ANSI_YELLOW + "Indique o código de reserva: " + ANSI_RESET);
                        String codigoReserva = stdIn.readLine();

                        m.send(44,username,codigoReserva.getBytes());
                        String respostaCancelamento = new String(m.receive(44), StandardCharsets.UTF_8);
                        if(respostaCancelamento.startsWith("Erro")) {
                            System.out.println(ANSI_RED + respostaCancelamento + ANSI_RESET);
                        } else {
                            System.out.println(ANSI_GREEN + respostaCancelamento + ANSI_RESET);
                            }
                        break;
                    case "3":
                        System.out.println(ANSI_PURPLE +"***LISTA DE VOOS***\n" + ANSI_RESET);
                        m.send(80,username,new byte[0]);
                        String resposta = new String(m.receive(80), StandardCharsets.UTF_8);
                        System.out.println(resposta);
                        break;
                    case "4":
                        System.out.println(ANSI_PURPLE +"***LISTA DE PERCURSOS LIMITADOS A DUAS ESCALAS***\n"+ ANSI_RESET);
                        System.out.print(ANSI_YELLOW + "Origem: "+ ANSI_RESET);
                        String origem = stdIn.readLine().toLowerCase();
                        System.out.print(ANSI_YELLOW +"Destino: "+ ANSI_RESET);
                        String destino = stdIn.readLine().toLowerCase();
                        StringBuilder sbp = new StringBuilder(origem + ";" + destino);

                        m.send(33,username,sbp.toString().getBytes());
                        String percursos = new String(m.receive(33), StandardCharsets.UTF_8);
                        System.out.println("\n" + percursos);
                        break;
                    case "5":
                        System.out.println(ANSI_PURPLE +"***Obter minhas reservas***\n\n"+ ANSI_RESET);
                        m.send(15,username,new byte[0]);
                        String respons = new String(m.receive(15), StandardCharsets.UTF_8);
                        System.out.println(respons);
                        break;

                    case "6":
                        if (username.equals("admin")) {
                            System.out.println(ANSI_PURPLE +"***INSERIR INFORMACAO SOBRE VOOS***\n\n"+ ANSI_RESET);
                            System.out.print(ANSI_YELLOW +"Origem: "+ ANSI_RESET);
                            String ori = stdIn.readLine().toLowerCase();
                            System.out.print(ANSI_YELLOW +"Destino: "+ ANSI_RESET);
                            String des = stdIn.readLine().toLowerCase();
                            System.out.print(ANSI_YELLOW +"Capacidade: "+ ANSI_RESET);
                            String capacidade = stdIn.readLine();
                            System.out.println(capacidade);
                            StringBuilder sbb = new StringBuilder();
                            sbb.append(ori).append(";").append(des).append(";").append(capacidade);
                            m.send(55,username,sbb.toString().getBytes()); // quero inserir informacao

                            String response = new String(m.receive(55), StandardCharsets.UTF_8);
                            System.out.println(response);
                            break;
                        }
                    case "7":
                        if (username.equals("admin")) {
                            System.out.println(ANSI_PURPLE +"***ENCERRAMENTO DE DIA***\n\n"+ ANSI_RESET);
                            System.out.print(ANSI_YELLOW +"Insira o dia(DD/MM/AAAA): "+ ANSI_RESET);
                            String dia = stdIn.readLine();
                            m.send(7,username,dia.getBytes());
                            String response = new String(m.receive(7), StandardCharsets.UTF_8);
                            System.out.println(response);
                            break;
                        }
                    case "8":
                        if (username.equals("admin")) {
                            System.out.println(ANSI_PURPLE +"***LISTA DE RESERVAS***\n\n"+ ANSI_RESET);
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
