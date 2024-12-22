import java.io.*;

public class RequestHandler {

    private OutputStream out;
    private FileHandler fileHandler;
    private ErrorHandler errorHandler;

    public RequestHandler(OutputStream out) {
        this.out = out;
        this.fileHandler = new FileHandler();
        this.errorHandler = new ErrorHandler(out);
    }

    public void handleRequest(String requestLine, BufferedReader in) throws IOException {
        String[] parts = requestLine.split(" ");
        if (parts.length < 3) {
            errorHandler.sendErrorResponse(400, "Mauvaise requête");
            return;
        }

        String method = parts[0];
        String path = parts[1];

        if (method.equalsIgnoreCase("GET")) {
            fileHandler.handleGetRequest(out, path);
        } else if (method.equalsIgnoreCase("POST")) {
            handlePostRequest(in, out, path);
        } else {
            errorHandler.sendErrorResponse(405, "Méthode non autorisée");
        }
    }  
    
    private void handlePostRequest(BufferedReader in, OutputStream out, String path) throws IOException {
        int contentLength = 0;
        String line;
        try {
            // Lire les en-têtes pour récupérer Content-Length
            while (!(line = in.readLine()).isEmpty()) {
                if (line.startsWith("Content-Length:")) {
                    contentLength = Integer.parseInt(line.split(":")[1].trim());
                }
            }

            if (contentLength == 0) {
                errorHandler.sendErrorResponse(411, "Content-Length requis");
                return;
            }

            // Lire le corps de la requête
            char[] body = new char[contentLength];
            in.read(body, 0, contentLength);
            String requestBody = new String(body);

            // Afficher les données POST reçues
            System.out.println("POST Data Received: " + requestBody);

            // Sauvegarder les données dans un fichier
            File outputFile = new File(HttpServer.getWebRoot() + "/post_data.txt");
            try (FileWriter writer = new FileWriter(outputFile, true)) {
                writer.write(requestBody + "\n");
            }

            // Réponse au client
            String response = "<html><body><h1>POST Data Received</h1><p>" + requestBody + "</p></body></html>";
            sendResponse(out, 200, "OK", "text/html", response.getBytes());

        } catch (NumberFormatException e) {
            errorHandler.sendErrorResponse(400, "Content-Length invalide");
        } catch (Exception e) {
            errorHandler.sendErrorResponse(500, "Erreur interne du serveur");
        }
    }

    private void sendResponse(OutputStream out, int statusCode, String statusMessage, String contentType, byte[] content) throws IOException {
        PrintWriter writer = new PrintWriter(out, true);
        writer.println("HTTP/1.1 " + statusCode + " " + statusMessage);
        writer.println("Content-Type: " + contentType);
        writer.println("Content-Length: " + content.length);
        writer.println(); // Ligne vide pour terminer les en-têtes
        out.write(content);
        out.flush();
    
        // Affichage des détails de la réponse dans la console
        System.out.printf("Réponse envoyée : %d %s | Content-Type: %s | Content-Length: %d\n", 
            statusCode, statusMessage, contentType, content.length);
    }
    
}
