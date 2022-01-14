import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.io.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Server {

    public static void main(String[] args) {

        try {
            ServerSocket ss = new ServerSocket(12345);

            final Contas conta;
            final Reservas reservas;
            final Voos voos;
            final DatasEncerradas datasEncerradas;

            // iniciar contas
            File f = new File("contas.ser");
            if (!f.exists())
                conta = new Contas();
            else
                conta = Contas.deserialize("contas.ser");

            // iniciar reservas
            f = new File("reservas.ser");
            if (!f.exists())
                reservas = new Reservas();
            else
                reservas = Reservas.deserialize("reservas.ser");

            // iniciar voos
            f = new File("voos.ser");
            if (!f.exists()) voos = new Voos();
            else voos = Voos.deserialize("voos.ser");

            // datas encerradas
            f = new File("datasEncerradas.ser");
            if (!f.exists())
                datasEncerradas = new DatasEncerradas();
            else
                datasEncerradas = DatasEncerradas.deserialize("contas.ser");

            ReentrantLock liuLock = new ReentrantLock();
            HashSet<String> loggedInUsers = new HashSet<>();

            ReentrantLock userLock = new ReentrantLock();
            Condition userCondition = userLock.newCondition();
            HashSet<String> Users = new HashSet<>();

            while (true) {
                Socket s = ss.accept();
                Conexao c = new Conexao(s);

                /* The worker responsible for handling a client's requests. */
                Runnable worker = () -> {
                    try (c) {
                        while (true) {
                            boolean loggedIn = false;
                            Frame frame = c.receive();

                            if (frame.tag == 0) {
                                System.out.println("Tentativa de autenticação.");
                                String nome = frame.username;
                                String password = new String(frame.data);
                                String stored_password;
                                conta.l.readLock().lock();
                                try {
                                    stored_password = conta.getPassword(nome);
                                } finally {
                                    conta.l.readLock().unlock();
                                }
                                if (stored_password != null) {
                                    if (stored_password.equals(password)) {
                                        c.send(0, "", "Sessão iniciada com sucesso!".getBytes());
                                        loggedIn = true;
                                        liuLock.lock();
                                        try { loggedInUsers.add(frame.username); }
                                        finally { liuLock.unlock(); }
                                    }
                                    else
                                        c.send(0, "", "Erro - palavra-passe errada.".getBytes());
                                } else
                                    c.send(0, "", "Erro - conta não existe.".getBytes());
                            }

                            else if (frame.tag == 1) {
                                System.out.println("Tentativa de registo.");
                                String nome = frame.username;
                                String password = new String(frame.data);
                                conta.l.writeLock().lock();
                                try {
                                    if (conta.accountExists(nome))
                                        c.send(1, "", "Este nome já se encontra associado a uma conta.".getBytes());
                                    else {
                                        conta.addAccount(nome, password);
                                        conta.serialize("contas.ser");
                                        c.send(frame.tag, "", "Registo efetuado com sucesso!".getBytes());
                                        loggedIn = true;
                                        liuLock.lock();
                                        try {
                                            loggedInUsers.add(frame.username);
                                        } finally {
                                            liuLock.unlock();
                                        }
                                    }
                                } finally {
                                    conta.l.writeLock().unlock();
                                }
                            } else if(frame.tag == 20){
                                System.out.println("Tentativa de Reserva de viagem");
                                String info = new String(frame.data, StandardCharsets.UTF_8);

                                //recebe percurso e datas:    3;lisboa;porto;terceira;20/12/2021;25/12/2021
                                String[] parts = info.split(";");
                                int numCidades = Integer.parseInt(parts[0]);

                                List<Voo> listaVoos = new ArrayList<>();
                                for(int i = 1; i < numCidades; i++){
                                    Voo v = new Voo(parts[i],parts[i+1]);
                                    listaVoos.add(v);
                                }


                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/MM/yyyy");
                                LocalDate inicio = LocalDate.parse(parts[numCidades + 1], formatter);
                                LocalDate fim = LocalDate.parse(parts[numCidades + 2], formatter);

                                Boolean b = false;


                                for(;(inicio.isBefore(fim) || inicio.isEqual(fim)) && !b; inicio = inicio.plusDays(1)){
                                    if(!(datasEncerradas.existeDataEncerrada(inicio))){
                                        reservas.l.readLock().lock();
                                        voos.l.readLock().lock();
                                        try {
                                            for(int i = 1; i < numCidades; i++){
                                                int ocupacao;
                                                ocupacao = reservas.getOcupacaoVoo(parts[i], parts[i + 1], inicio);
                                                b = voos.escalaPossivel(parts[i], parts[i + 1], ocupacao);

                                                if(!b) break;
                                            }
                                        } finally {
                                            reservas.l.readLock().unlock();
                                            voos.l.readLock().unlock();
                                        }
                                    }
                                }

                                    if(b){ // encontrou um dia com todas as escalas disponiveis
                                        String uniqueID = UUID.randomUUID().toString();
                                        while(reservas.reservaExists(uniqueID)){
                                            uniqueID = UUID.randomUUID().toString();
                                        }
                                        Reserva r = new Reserva(uniqueID,inicio.minusDays(1),LocalDate.now(),listaVoos);
                                        reservas.l.writeLock().lock();
                                        try{
                                            reservas.addReserva(uniqueID,r);
                                            reservas.serialize("reservas.ser");
                                        } finally {
                                            reservas.l.writeLock().unlock();
                                        }
                                        String sucesso = new String("Reserva efetuada com sucesso!\nCodigo Reserva: " + uniqueID);
                                        c.send(20,"",sucesso.getBytes());
                                    } else {
                                        String erro = new String("Erro - Não é possível efetuar a reserva (Tente escolher outras datas, verifique as escalas).\n");
                                        c.send(20,"",erro.getBytes());
                                    }

                            }else if(frame.tag == 44){
                                System.out.println("Pedido de cancelamento de Reserva.");
                                String codReserva = new String(frame.data, StandardCharsets.UTF_8);
                                boolean b = true;
                                reservas.l.writeLock().lock();
                                try {
                                    b = reservas.removeReserva(codReserva,LocalDate.now());
                                } finally {
                                    reservas.l.writeLock().unlock();
                                }

                                if(b){
                                    System.out.println("DEPOIS: " + reservas.getMapReservas().size());
                                    String sucessoCancelamento = new String("Reserva cancelada com sucesso.\n");
                                    c.send(44,"",sucessoCancelamento.getBytes());
                                } else {
                                    String erroCancelamento = new String("Erro ao cancelar reserva(reserva inexistente ou período de cancelamento expirado).\n");
                                    c.send(44,"",erroCancelamento.getBytes());
                                }
                            }
                            else if(frame.tag == 55) {
                                System.out.println("Tentativa de Inserir Voo (ADMIN).");
                                String informacao = new String(frame.data, StandardCharsets.UTF_8);
                                String[] tokens = informacao.split(";");
                                int idVoo;

                                voos.l.readLock().lock();
                                try {
                                    idVoo = voos.getVoos().size();
                                } finally {
                                    voos.l.readLock().unlock();
                                }
                                //System.out.println("Origem: " + tokens[0] + "\t" + "Destino: " + tokens[1]);
                                Voo v = new Voo(idVoo, tokens[0], tokens[1], Integer.parseInt(tokens[2]));

                                voos.l.writeLock().lock();
                                if (!(voos.verificaSeExiste(tokens[0], tokens[1]))) {
                                    try {
                                        voos.addVoo(idVoo, v);
                                        voos.serialize("voos.ser");
                                        c.send(55, "", "Voo inserido com sucesso!".getBytes());

                                    } finally {
                                        voos.l.writeLock().unlock();
                                    }

                                } else c.send(55, "", "Erro - Este voo já existe!".getBytes());

                            } else if(frame.tag == 80){
                                System.out.println("Pedido de lista de voos.");
                                Map<Integer,Voo> mv = new HashMap<>();
                                mv = voos.getVoos();
                                StringBuilder sb = new StringBuilder();
                                for(Voo v : mv.values()){
                                    sb.append(v.toString());
                                }
                                c.send(80,"",sb.toString().getBytes());
                            } else if(frame.tag == 7){
                                System.out.println("Encerramento de dia. (ADMIN)");

                                // receber data e defini-la como encerrada
                                String dataEncerramento = new String(frame.data, StandardCharsets.UTF_8);
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/MM/yyyy");
                                LocalDate dataEnc = LocalDate.parse(dataEncerramento, formatter);
                                System.out.println("STRING: " + dataEncerramento);

                                datasEncerradas.l.writeLock().lock();
                                try{
                                    datasEncerradas.addDataEncerrada(dataEnc);
                                    datasEncerradas.serialize("datasencerradas.ser");
                                } finally {
                                    datasEncerradas.l.writeLock().unlock();
                                }

                                reservas.l.writeLock().lock();
                                try{
                                    // eliminar reservas naquela data
                                    reservas.cancelaReservasByDate(dataEnc);
                                    reservas.serialize("reservas.ser");
                                } finally {
                                    reservas.l.writeLock().unlock();
                                }
                                String mensagem = new String("Dia encerrado com sucesso.\n");
                                c.send(7,"",mensagem.getBytes());
                            } else if(frame.tag == 33){
                                System.out.println("Pedido de todos os percursos limitados a 2 escalas");

                                String origemDestino = new String(frame.data, StandardCharsets.UTF_8);
                                String[] origemDest = origemDestino.split(";");
                                List<String> percursos = new ArrayList<>();
                                voos.l.readLock().lock();
                                try{
                                    percursos = voos.percursosPossiveis(origemDest[0],origemDest[1]);
                                } finally {
                                    voos.l.readLock().unlock();
                                }

                                StringBuilder sPercursos = new StringBuilder();
                                for(String sp : percursos){
                                    sPercursos.append(sp);
                                }
                                c.send(33,"",sPercursos.toString().getBytes());

                            }
                            else if(frame.tag == 99){
                                liuLock.lock();
                                try {
                                    loggedInUsers.remove(frame.username);
                                }
                                finally {
                                    liuLock.unlock();
                                }
                                userLock.lock();
                                try{
                                    if(frame.data.length > 0)
                                        Users.add(frame.username);
                                    userCondition.signalAll();
                                }
                                finally {
                                    userLock.unlock();
                                    break;
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                };
                new Thread(worker).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
