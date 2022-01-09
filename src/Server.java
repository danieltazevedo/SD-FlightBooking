import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.io.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Server {
    public static void main(String[] args) {

        try {
            ServerSocket ss = new ServerSocket(12345);

            final Contas conta;
            final Viagem viagens;

            File f = new File("contas.ser");
            if (!f.exists())
                conta = new Contas();
            else
                conta = Contas.deserialize("contas.ser");

            f = new File("viagens.ser");
            if (!f.exists())
                viagens = new Viagem();
            else
                viagens = Viagem.deserialize("viagens.ser");

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
                            } else if(frame.tag == 99){
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
