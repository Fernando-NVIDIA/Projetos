package Content.TCP_Code;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class TCPClient1 {
	private static final String SERVER_HOST = "localhost";
	private static final int SERVER_PORT = 12345;

	public static void main(String[] args) {
		System.out.println("=== CLIENTE TCP ===");
		System.out.println("Conectando ao servidor " + SERVER_HOST + ":" + SERVER_PORT + "...");

		try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
		     PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
		     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		     Scanner scanner = new Scanner(System.in)) {

			System.out.println("Conectado com sucesso!");

			// Thread para receber mensagens do servidor
			Thread receiveThread = new Thread(() -> {
				try {
					String serverMessage;
					while ((serverMessage = in.readLine()) != null) {
						System.out.println(serverMessage);
					}
				} catch (IOException e) {
					System.err.println("Conexão com servidor perdida.");
				}
			});
			receiveThread.setDaemon(true);
			receiveThread.start();

			// Thread principal para enviar mensagens
			String userInput;
			while (true) {
				userInput = scanner.nextLine();
				out.println(userInput);

				if ("sair".equalsIgnoreCase(userInput.trim())) {
					break;
				}
			}

		} catch (ConnectException e) {
			System.err.println("Não foi possível conectar ao servidor. Verifique se o servidor está rodando.");
		} catch (IOException e) {
			System.err.println("Erro de comunicação: " + e.getMessage());
		}

		System.out.println("Cliente desconectado.");
	}
}
