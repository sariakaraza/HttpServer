import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable {
    private Socket clientSocket;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             OutputStream out = clientSocket.getOutputStream()) {

            boolean keepAlive = true;

            // Boucle principale pour gérer plusieurs requêtes si keep-alive est demandé
            while (keepAlive) {
                // Lecture de la première ligne de la requête (ligne de commande HTTP)
                String requestLine = in.readLine();
                if (requestLine == null || requestLine.isEmpty()) break;

                System.out.println("Requête reçue : " + requestLine);

                // Créer un RequestHandler pour traiter cette requête
                RequestHandler requestHandler = new RequestHandler(out);
                requestHandler.handleRequest(requestLine, in);

                // Vérification des en-têtes pour détecter 'Connection: keep-alive'
                String headerLine;
                keepAlive = false; // Par défaut, on ferme la connexion sauf si spécifié
                while ((headerLine = in.readLine()) != null && !headerLine.isEmpty()) {
                    if (headerLine.equalsIgnoreCase("Connection: keep-alive")) {
                        keepAlive = true;
                    }
                }

                // Ajouter une réponse correcte pour les connexions persistantes
                if (keepAlive) {
                    System.out.println("Connexion maintenue ouverte pour la prochaine requête.");
                } else {
                    System.out.println("Connexion fermée après cette requête.");
                }
            }

        } catch (IOException e) {
            System.err.println("Erreur avec le client : " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
                System.out.println("Connexion définitivement fermée.");
            } catch (IOException e) {
                System.err.println("Erreur lors de la fermeture du socket : " + e.getMessage());
            }
        }
    }
}
