import java.io.*;
import java.net.*;

public class ChatClient {
    private static final String SERVER_ADDRESS = "10.90.1.188"; // Endereço do servidor (altere para IP se for outra máquina)
    private static final int SERVER_PORT = 12345; // Mesma porta usada pelo servidor

    public static void main(String[] args) {
        try (
            // Cria conexão com o servidor
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            // Lê mensagens vindas do servidor
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // Lê entrada do teclado do usuário
            BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
            // Envia mensagens para o servidor
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        ) {
            // Thread para escutar e mostrar mensagens recebidas do servidor
            Thread listener = new Thread(() -> {
                String response;
                try {
                    while ((response = input.readLine()) != null) {
                        System.out.println(response); // Mostra mensagens recebidas no terminal
                    }
                } catch (IOException e) {
                    System.out.println("Desconectado do servidor.");
                }
            });

            listener.start(); // Inicia a escuta de mensagens

            String userInput;
            // Loop para ler mensagens digitadas e enviá-las ao servidor
            while ((userInput = keyboard.readLine()) != null) {
                out.println(userInput); // Envia mensagem
                if (userInput.equalsIgnoreCase("/sair")) {
                    break; // Sai se usuário digitar /sair
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
