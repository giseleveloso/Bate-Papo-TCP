import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class ChatServer {
    private static final int PORT = 12345; // Porta que o servidor vai escutar
    private static Set<ClientHandler> clients = new HashSet<>(); // Conjunto de clientes conectados
        private static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");


    public static void main(String[] args) {
        System.out.println("Servidor de Chat iniciado...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) { // Cria o servidor escutando na porta
            while (true) {
                Socket socket = serverSocket.accept(); // Aguarda conexão de um cliente
                ClientHandler clientThread = new ClientHandler(socket); // Cria uma nova thread para esse cliente
                clients.add(clientThread); // Adiciona o cliente à lista de clientes
                clientThread.start(); // Inicia a thread de atendimento ao cliente
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Envia mensagem para todos os clientes, exceto quem enviou
    static void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    // Remove cliente da lista de conectados
    static void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    //  Lista os usernames que estão no bate papo
    static List<String> getUsernames() {
        List<String> usernames = new ArrayList<>();
        for (ClientHandler client : clients) {
            usernames.add(client.name);
        }
        return usernames;
    }

    // Classe interna que representa um cliente conectado
    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private String name;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        // Envia mensagem ao cliente atual
        public void sendMessage(String message) {
            out.println(message);
        }

        public void run() {
            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Lê dados do cliente
            ) {
                out = new PrintWriter(socket.getOutputStream(), true); // Envia dados para o cliente

                // Solicita nome do usuário
                out.println("Digite seu nome: ");
                name = in.readLine(); // Lê o nome digitado
                broadcast("🟢 " + name + " entrou no chat!", this); // Notifica os outros clientes
                sendMessage("✔️ Bem-vindo, " + name + "!");
                sendMessage("Digite /ajuda para ver os comandos.");


                String message;
                // Loop para ler mensagens do cliente
                while ((message = in.readLine()) != null) {
                    if (message.equalsIgnoreCase("/sair")) { // Comando de saída
                        break;
                    }
                    else if (message.equalsIgnoreCase("/usuarios")) {
                        sendMessage("👥 Usuários online: " + String.join(", ", getUsernames()));
                    } else if (message.equalsIgnoreCase("/ajuda")) {
                        sendMessage("📌 Comandos disponíveis:");
                        sendMessage("/usuarios - lista os usuários conectados");
                        sendMessage("/sair     - sair do chat");
                        sendMessage("/ajuda    - mostrar esta ajuda");
                    } else {
                        String time = "[" + sdf.format(new Date()) + "]";
                        broadcast(time + " [" + name + "]: " + message, this);  // Envia mensagem para os outros
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close(); // Fecha conexão
                } catch (IOException e) {}
                removeClient(this); // Remove cliente da lista
                broadcast("🔴 " + name + " saiu do chat.", this); // Notifica os outros
            }
        }
    }
}
