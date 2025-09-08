package Content.UDP_Code;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class UDPClient2 {
	private static final String SERVER_HOST = "localhost";
	private static final int SERVER_PORT = 12346;
	private static final int BUFFER_SIZE = 1024;

	public static void main(String[] args) {
		System.out.println("=== CLIENTE UDP ===");
		System.out.println("Conectando ao servidor UDP " + SERVER_HOST + ":" + SERVER_PORT);

		try (DatagramSocket socket = new DatagramSocket();
		     Scanner scanner = new Scanner(System.in)) {

			InetAddress serverAddress = InetAddress.getByName(SERVER_HOST);

			System.out.println("Pronto para enviar mensagens UDP!");
			System.out.println("Comandos especiais:");
			System.out.println("- 'hora' -> Ver hora atual");
			System.out.println("- 'ip' -> Ver seu IP");
			System.out.println("- 'ping' -> Testar conexão");
			System.out.println("- 'info' -> Informações do servidor");
			System.out.println("- 'sair' -> Encerrar cliente");
			System.out.println("- 'PARAR_SERVIDOR' -> Parar servidor (cuidado!)");
			System.out.println("=====================================");

			String userInput;
			while (true) {
				System.out.print("Digite sua mensagem: ");
				userInput = scanner.nextLine();

				// Verifica se deve sair
				if ("sair".equalsIgnoreCase(userInput.trim())) {
					System.out.println("Encerrando cliente UDP...");
					break;
				}

				// Cria pacote para enviar
				byte[] sendData = userInput.getBytes();
				DatagramPacket sendPacket = new DatagramPacket(
								sendData, sendData.length, serverAddress, SERVER_PORT);

				// Envia pacote
				socket.send(sendPacket);
				System.out.println("Mensagem enviada via UDP!");

				// Prepara para receber resposta
				byte[] receiveBuffer = new byte[BUFFER_SIZE];
				DatagramPacket receivePacket = new DatagramPacket(
								receiveBuffer, receiveBuffer.length);

				// Recebe resposta do servidor
				socket.receive(receivePacket);

				String serverResponse = new String(receivePacket.getData(), 0, receivePacket.getLength());
				System.out.println("Resposta: " + serverResponse);
				System.out.println("-------------------------------------");
			}

		} catch (UnknownHostException e) {
			System.err.println("Host não encontrado: " + e.getMessage());
		} catch (IOException e) {
			System.err.println("Erro de comunicação UDP: " + e.getMessage());
		}

		System.out.println("Cliente UDP desconectado.");
	}
}
