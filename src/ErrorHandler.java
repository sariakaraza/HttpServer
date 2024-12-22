import java.io.*;

public class ErrorHandler {

    private OutputStream out;

    public ErrorHandler(OutputStream out) {
        this.out = out;
    }

    public void sendErrorResponse(int statusCode, String message) throws IOException {
        String errorMessage = String.format("<html><body><h1>Erreur %d : %s</h1></body></html>", statusCode, message);
        sendResponse(out, statusCode, message, "text/html", errorMessage.getBytes());
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
}
