import java.io.*;
import java.nio.file.*;

public class FileHandler {

    // private static final String WEB_ROOT = "mesfichiers"; // Dossier contenant les fichiers

    private static final String WEB_ROOT = HttpServer.getWebRoot(); // Dossier contenant les fichiers

    public void handleGetRequest(OutputStream out, String path) throws IOException {
        File file;

        // Gestion des paramètres dans l'URL (si "?" est présent)
        if (path.contains("?")) {
            path = path.split("\\?")[0]; // Extraire uniquement la partie avant "?"
            System.out.println("PATH AVEC GET  :  " + path);
        }

        if (path.contains("/" + WEB_ROOT)) {
            // Trouver l'index de "/mesfichiers" dans le chemin
            int index = path.indexOf("/" + WEB_ROOT);
        
            // Retirer toute la partie avant (et y compris) "/mesfichiers"
            path = path.substring(index + ("/" + WEB_ROOT).length());
            System.out.println("Requested Path: " + path);
        }

        // path = Paths.get(WEB_ROOT, path).toAbsolutePath().toString();

        // Afficher le chemin absolu
        
        // Si le chemin est "/"
        if (path.equals("/")) {
            file = new File(WEB_ROOT + "/index.html");
    
            if (file.exists() && file.isFile()) {
                sendFile(out, file); // Envoie index.html
            } else {
                file = new File(WEB_ROOT + "/index.php");
                if (file.exists() && file.isFile()) {
                    sendPhpFile(out, file); // Exécuter et envoyer index.php
                } else {
                    listFiles(out, WEB_ROOT); // Lister les fichiers du répertoire racine
                }
            }
        } 
        // Si un chemin spécifique est demandé, il peut s'agir d'un fichier ou d'un dossier
        else {
         
            file = new File(WEB_ROOT + path);  // Construire le chemin absolu

            // Si c'est un dossier, lister son contenu
            if (file.isDirectory()) {
                listFiles(out, file.getAbsolutePath()); // Lister les fichiers dans ce dossier
            } 
            // Si c'est un fichier, l'envoyer ou l'exécuter si c'est un fichier PHP
            else if (file.exists() && file.isFile()) {
                if (path.endsWith(".php")) {
                    sendPhpFile(out, file); // Exécuter et envoyer le fichier PHP
                } else {
                    sendFile(out, file); // Envoyer le fichier statique
                }
            } else {
                sendErrorPage(out, "Le fichier ou dossier demandé est introuvable.");
            }
        }
    }
                
    private void listFiles(OutputStream out, String directoryPath) throws IOException {
        File directory = new File(directoryPath);
        
        if (!directory.exists() || !directory.isDirectory()) {
            sendErrorPage(out, "Le répertoire spécifié est introuvable.");
            return;
        }
    
        // Print pour afficher le répertoire que l'on explore
        System.out.println("Exploring directory: " + directoryPath);
        
        StringBuilder fileList = new StringBuilder("<html><body><h1>Liste des fichiers et dossiers</h1><ul>");
        
        // Afficher les sous-dossiers
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                String subDirLink = directoryPath + "/" + file.getName();  // Créer le lien pour le sous-dossier
                System.out.println("Found subdirectory: " + subDirLink); // Afficher le sous-dossier trouvé
                fileList.append("<li><a href=\"" + subDirLink + "/\">" + file.getName() + "/</a></li>");
            }
        }
    
        // Afficher les fichiers
        for (File file : directory.listFiles()) {
            if (file.isFile()) {
                String fileLink = directoryPath + "/" + file.getName(); // Créer le lien pour le fichier
                System.out.println("Found file: " + fileLink); // Afficher le fichier trouvé
                fileList.append("<li><a href=\"" + fileLink + "\">" + file.getName() + "</a></li>");
            }
        }
    
        fileList.append("</ul></body></html>");
        sendResponse(out, 200, "OK", "text/html", fileList.toString().getBytes());
    }
        
    private void sendFile(OutputStream out, File file) throws IOException {
        // Lire le contenu du fichier en bytes
        byte[] fileContent = Files.readAllBytes(file.toPath());

        // Détecter le type MIME à partir du fichier
        String contentType = Files.probeContentType(file.toPath());
        if (contentType == null) {
            contentType = getMimeTypeFromExtension(file.getName());
        }

        // Envoyer la réponse HTTP avec le bon type MIME
        sendResponse(out, 200, "OK", contentType, fileContent);
    }

    // Méthode pour détecter le type MIME en fonction de l'extension du fichier
    private String getMimeTypeFromExtension(String path) {
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (path.endsWith(".png")) {
            return "image/png";
        } else if (path.endsWith(".gif")) {
            return "image/gif";
        } else if (path.endsWith(".html")) {
            return "text/html";
        } else if (path.endsWith(".css")) {
            return "text/css";
        } else if (path.endsWith(".js")) {
            return "application/javascript";
        } else {
            return "application/octet-stream";  
        }
    }

    private void sendResponse(OutputStream out, int statusCode, String statusMessage, String contentType, byte[] content) throws IOException {
        PrintWriter writer = new PrintWriter(out);
        writer.printf("HTTP/1.1 %d %s\r\n", statusCode, statusMessage);
        System.out.printf("HTTP/1.1 %d %s\r\n", statusCode, statusMessage);

        writer.printf("Content-Type: %s\r\n", contentType);
        System.out.printf("Content-Type: %s\r\n", contentType);

        writer.printf("Content-Length: %d\r\n", content.length);
        System.out.printf("Content-Length: %d\r\n", content.length);

        writer.print("\r\n");
        System.out.println("\n");

        writer.flush();
        out.write(content);
        out.flush();
    }

    private void sendErrorPage(OutputStream out, String errorMessage) throws IOException {
        String errorContent = "<html><body><h1>Erreur 404</h1><p>" + errorMessage + "</p></body></html>";
        sendResponse(out, 404, "Not Found", "text/html", errorContent.getBytes());
    }

    // ENVOYER DES FICHIERS PHP
    private void sendPhpFile(OutputStream out, File file) throws IOException {
        // On lance le processus PHP avec le fichier demandé
        ProcessBuilder processBuilder = new ProcessBuilder("php", file.getAbsolutePath());
        Process process = processBuilder.start();
        
        // Récupérer le résultat du processus PHP
        InputStream inputStream = process.getInputStream();
        byte[] content = inputStream.readAllBytes();
        
        // Déterminer le type de contenu (ici on suppose que c'est du HTML)
        String contentType = "text/html";
    
        // Envoyer la réponse avec le contenu généré par PHP
        sendResponse(out, 200, "OK", contentType, content);
    }
    
}
