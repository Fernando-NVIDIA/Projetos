package Content.TCP_Code;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

@SuppressWarnings("ALL")
public class TCPServer {
	private static final int PORT = 12345;
	private static final Set<ClientHandler> clients = Collections.synchronizedSet(new HashSet<>());
	private static final ExecutorService executor = Executors.newCachedThreadPool();

	public static void main(String[] args) {
		System.out.println("=== SERVIDOR TCP INICIADO ===");
		System.out.println("Aguardando conexões na porta " + PORT + "...");

		try (ServerSocket serverSocket = new ServerSocket(PORT)) {
			while (true) {
				// Bloqueia aqui até um cliente se conectar
				Socket clientSocket = serverSocket.accept();

				// Cria um manipulador para este cliente específico
				ClientHandler clientHandler = new ClientHandler(clientSocket);
				clients.add(clientHandler);

				// Executa o manipulador em uma thread separada
				executor.execute(clientHandler);

				System.out.println("Nova conexão: " + clientSocket.getInetAddress().getHostAddress());
				System.out.println("Total de clientes: " + clients.size());
			}
		} catch (IOException e) {
			System.err.println("Erro no servidor: " + e.getMessage());
		} finally {
			executor.shutdown();
		}
	}

	// Metodo para enviar mensagem para todos os clientes conectados
	public static void broadcastMessage(String message, ClientHandler sender) {
		synchronized (clients) {
			Iterator<ClientHandler> iterator = clients.iterator();
			while (iterator.hasNext()) {
				ClientHandler client = iterator.next();
				if (client != sender) { // Não envia para quem enviou
					if (!client.sendMessage(message)) {
						iterator.remove(); // Remove cliente desconectado
						System.out.println("Cliente desconectado. Total: " + clients.size());
					}
				}
			}
		}
	}

	public static void removeClient(ClientHandler client) {
		clients.remove(client);
		System.out.println("Cliente removido. Total: " + clients.size());
	}
}

// Classe que gerencia cada cliente individualmente
class ClientHandler implements Runnable {
	private final Socket socket;
	private PrintWriter out;
	private BufferedReader in;
	private String clientName;

	public ClientHandler(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		try {
			// Configura streams de entrada e saída
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);

			// Solicita nome do cliente
			out.println("=== BEM-VINDO AO CHAT TCP ===");
			out.println("Digite seu nome:");
			clientName = in.readLine();

			if (clientName == null || clientName.trim().isEmpty()) {
				clientName = "Usuario_" + socket.getInetAddress().getHostAddress();
			}

			out.println("Olá, " + clientName + "! Você está conectado ao chat.");
			out.println("Digite 'sair' para desconectar.");

			// Notifica outros clientes
			TCPServer.broadcastMessage("[SISTEMA] " + clientName + " entrou no chat", this);

			// Loop principal: lê mensagens do cliente
			String message;
			while ((message = in.readLine()) != null) {
				if ("sair".equalsIgnoreCase(message.trim())) {
					break;
				}

				String formattedMessage = "[" + clientName + "]: " + message;
				System.out.println(formattedMessage);
				TCPServer.broadcastMessage(formattedMessage, this);
			}

		} catch (IOException e) {
			System.err.println("Erro na comunicação: " + e.getMessage());
		} finally {
			cleanup();
		}
	}

	public boolean sendMessage(String message) {
		try {
			out.println(message);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private void cleanup() {
		try {
			if (clientName != null) {
				TCPServer.broadcastMessage("[SISTEMA] " + clientName + " saiu do chat", this);
			}

			if (socket != null && !socket.isClosed()) {
				socket.close();
			}
			TCPServer.removeClient(this);

		} catch (IOException e) {
			System.err.println("Erro ao fechar conexão: " + e.getMessage());
		}
	}
}