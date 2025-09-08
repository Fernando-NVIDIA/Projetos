package Content.UDP_Code;

import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UDPServer {
	private static final int PORT = 12346;
	private static final int BUFFER_SIZE = 1024;

	public static void main(String[] args) {
		System.out.println("=== SERVIDOR UDP INICIADO ===");
		System.out.println("Escutando na porta " + PORT + "...");
		System.out.println("Aguardando pacotes UDP...");
		System.out.println("-----------------------------------");

		try (DatagramSocket socket = new DatagramSocket(PORT)) {
			byte[] buffer = new byte[BUFFER_SIZE];

			while (true) {
				// Cria um "envelope" vazio para receber dados
				DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);

				// BLOQUEIA aqui até receber um pacote
				socket.receive(receivePacket);

				// Extrai informações do pacote recebido
				String receivedMessage = new String(receivePacket.getData(), 0, receivePacket.getLength()).trim();
				InetAddress clientAddress = receivePacket.getAddress();
				int clientPort = receivePacket.getPort();

				String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

				System.out.println("[" + timestamp + "] Recebido de " +
								clientAddress.getHostAddress() + ":" + clientPort + " -> " + receivedMessage);

				// Gera resposta baseada na mensagem
				String response = processMessage(receivedMessage, clientAddress.getHostAddress());

				// Cria pacote de resposta
				byte[] responseData = response.getBytes();
				DatagramPacket responsePacket = new DatagramPacket(
								responseData, responseData.length, clientAddress, clientPort);

				// Envia resposta de volta
				socket.send(responsePacket);

				System.out.println("[" + timestamp + "] Enviado para " +
								clientAddress.getHostAddress() + ":" + clientPort + " -> " + response);
				System.out.println("-----------------------------------");

				// Se receber comando de parada
				if ("PARAR_SERVIDOR".equalsIgnoreCase(receivedMessage)) {
					System.out.println("Comando de parada recebido. Encerrando servidor...");
					break;
				}
			}

		} catch (IOException e) {
			System.err.println("Erro no servidor UDP: " + e.getMessage());
		}

		System.out.println("Servidor UDP encerrado.");
	}

	private static String processMessage(String message, String clientIP) {
		String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

		// Processa diferentes tipos de mensagem
		if (message.toLowerCase().contains("ola") || message.toLowerCase().contains("olá")) {
			return "Servidor [" + timestamp + "]: Olá! Como posso ajudar você?";

		} else if (message.toLowerCase().contains("hora")) {
			return "Servidor [" + timestamp + "]: Hora atual: " +
							LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

		} else if (message.toLowerCase().contains("ip")) {
			return "Servidor [" + timestamp + "]: Seu IP é " + clientIP;

		} else if (message.toLowerCase().contains("ping")) {
			return "Servidor [" + timestamp + "]: PONG! Conexão UDP funcionando.";

		} else if (message.toLowerCase().contains("info")) {
			return "Servidor [" + timestamp + "]: Servidor UDP Java - Porta " + PORT +
							" | Seu IP: " + clientIP;

		} else if ("PARAR_SERVIDOR".equalsIgnoreCase(message)) {
			return "Servidor [" + timestamp + "]: Comando recebido. Servidor será encerrado.";

		} else {
			return "Servidor [" + timestamp + "]: Echo UDP -> '" + message + "'";
		}
	}
}
