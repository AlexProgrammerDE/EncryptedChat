package net.pistonmaster.encryptedchat;

import net.pistonmaster.encryptedchat.client.ClientMain;
import net.pistonmaster.encryptedchat.crypto.CryptoGenerator;
import net.pistonmaster.encryptedchat.crypto.CryptoStorage;
import net.pistonmaster.encryptedchat.server.ServerMain;
import net.pistonmaster.encryptedchat.util.ConsoleInput;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Scanner;

public class EncryptedChat {
    public static final Path ROOT_PATH = Path.of("");
    public static final Path CLIENT_PATH = ROOT_PATH.resolve("client");
    public static final String SIGNATURE_VALUE = "EncryptedChat";

    public static void main(String[] args) {
        ConsoleInput consoleInput = new ConsoleInput();
        consoleInput.registerArrowKey();

        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter instance type: ");

        Type type = Type.valueOf(scanner.nextLine().toUpperCase());
        switch (type) {
            case SERVER -> {
                System.out.println("Starting server...");
                System.out.println("Port (38947): ");
                String text = scanner.nextLine();
                if (text.isEmpty()) {
                    text = "38947";
                }
                int serverPort = Integer.parseInt(text);

                ServerMain main = new ServerMain(scanner, consoleInput, serverPort);
                Runtime.getRuntime().addShutdownHook(new Thread(main::shutdown));
                main.run();
            }
            case CLIENT -> {
                System.out.println("Starting client...");
                System.out.println("Host (0.0.0.0): ");
                String targetHost = scanner.nextLine();
                if (targetHost.isEmpty()) {
                    targetHost = "0.0.0.0";
                }

                System.out.println("Port (38947): ");
                String text = scanner.nextLine();
                if (text.isEmpty()) {
                    text = "38947";
                }
                int targetPort = Integer.parseInt(text);

                System.out.println("Username (Test): ");
                String targetUsername = scanner.nextLine();
                if (targetUsername.isEmpty()) {
                    targetUsername = "Test";
                }

                ClientMain client = new ClientMain(scanner, consoleInput, targetHost, targetPort,
                        targetUsername,
                        getOrCreateKeyPair(targetUsername)
                );
                Runtime.getRuntime().addShutdownHook(new Thread(client::shutdown));
                client.run();
            }
        }
    }

    private static KeyPair getOrCreateKeyPair(String clientName) {
        Path clientFolder = CLIENT_PATH.resolve(clientName);
        Path privateKeyPath = clientFolder.resolve("private.key");
        if (Files.exists(privateKeyPath)) {
            PrivateKey privateKey = CryptoStorage.loadPrivateKey(privateKeyPath);
            PublicKey publicKey = CryptoGenerator.getPublicKey(privateKey);
            return new KeyPair(publicKey, privateKey);
        } else {
            try {
                Files.createDirectories(CLIENT_PATH.resolve(clientName));
            } catch (IOException e) {
                e.printStackTrace();
            }
            KeyPair keyPair = CryptoGenerator.generateRSAKey();
            CryptoStorage.saveKey(keyPair.getPrivate(), privateKeyPath);
            return keyPair;
        }
    }

    enum Type {
        SERVER,
        CLIENT
    }
}
