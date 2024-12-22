import java.io.*;
import java.net.*;
import java.util.Properties;
import java.util.concurrent.*;

public class HttpServer {

    private static int port;             // Port du serveur
    private static String host;          // Adresse IP (écoute sur toutes les interfaces)
    private static String webRoot;       // Nouveau dossier racine des fichiers statiques
    private static int threadPoolSize;   // Taille du pool de threads

    public static void main(String[] args) {
        // Charger la configuration depuis le fichier config.properties
        loadConfiguration();

        // Vérification ou création du répertoire "mesfichiers"
        File webRootDir = new File(webRoot);
        if (!webRootDir.exists()) {
            webRootDir.mkdir();
            System.out.println("Répertoire '" + webRoot + "' créé. Ajoutez vos fichiers HTML ici.");
        } else {
            System.out.println("Répertoire '" + webRoot + "' déjà existant.");
        }

        // Lancement du serveur avec un pool de threads
        ExecutorService threadPool = Executors.newFixedThreadPool(threadPoolSize);

        try (ServerSocket serverSocket = new ServerSocket(port, 50, InetAddress.getByName(host))) {
            System.out.println("Serveur HTTP démarré sur " + host + ":" + port);

            while (true) {
                // Accepter la connexion d'un client
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connecté depuis l'adresse : " + clientSocket.getInetAddress().getHostAddress());

                // Traiter la connexion dans un nouveau thread
                threadPool.execute(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            System.err.println("Erreur du serveur : " + e.getMessage());
        } finally {
            threadPool.shutdown();
        }
    }

    // Méthode pour charger la configuration à partir du fichier config.properties
    private static void loadConfiguration() {
        Properties config = new Properties();

        try (InputStream input = new FileInputStream("config.properties")) {
            // Charger les propriétés depuis le fichier
            config.load(input);

            // Lire les valeurs des propriétés, ou lancer une exception si une propriété est manquante
            String portValue = config.getProperty("server.port");
            if (portValue == null) {
                throw new IllegalArgumentException("La propriété 'server.port' est manquante dans le fichier de configuration.");
            }
            port = Integer.parseInt(portValue);

            host = config.getProperty("server.host");
            if (host == null) {
                throw new IllegalArgumentException("La propriété 'server.host' est manquante dans le fichier de configuration.");
            }

            webRoot = config.getProperty("server.webroot");
            if (webRoot == null) {
                throw new IllegalArgumentException("La propriété 'server.webroot' est manquante dans le fichier de configuration.");
            }

            String threadPoolSizeValue = config.getProperty("server.threadPoolSize");
            if (threadPoolSizeValue == null) {
                throw new IllegalArgumentException("La propriété 'server.threadPoolSize' est manquante dans le fichier de configuration.");
            }
            threadPoolSize = Integer.parseInt(threadPoolSizeValue);

            // Afficher les valeurs chargées pour vérifier
            System.out.println("Configuration chargée : ");
            System.out.println("Port: " + port);
            System.out.println("Host: " + host);
            System.out.println("Web Root: " + webRoot);
            System.out.println("Thread Pool Size: " + threadPoolSize);

        } catch (IOException e) {
            System.err.println("Erreur de lecture du fichier de configuration : " + e.getMessage());
            System.exit(1); // Quitter le programme si le fichier de configuration est absent ou invalide
        } catch (IllegalArgumentException e) {
            System.err.println("Erreur de configuration : " + e.getMessage());
            System.exit(1); // Quitter le programme si une propriété essentielle est manquante
        }
    }

    public static String getWebRoot() {
        return webRoot;
    }

}