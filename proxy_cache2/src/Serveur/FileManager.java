package src.Serveur;
public class FileManager {
    public static String getFileHeader(String file) {
        // System.out.println(getFileMME(file));
        return "HTTP/1.1 200 OK\r\n" +
                "Content-Type: " + getFileMME(file) + "; charset=UTF-8\r\n" +
                "\r\n";
    }

    public static String getHeader(String contentType){
        return "HTTP/1.1 200 OK\r\n" +
                "Content-Type: " + contentType + "; charset=UTF-8\r\n" +
                "\r\n";
    }
    public static String getFileMME(String fileName) {
        String response = new String();
        if (fileName.endsWith(".jpeg")) {
            response = "image/jpeg";
        } else if (fileName.endsWith(".png")) {
            response = "image/png";
        } else if (fileName.endsWith(".json")) {
            response = "application/json";
        } else if (fileName.endsWith(".js")) {
            response = "application/javascript";
        } else if (fileName.endsWith("css")) {
            response = "text/css";
        } else if (fileName.endsWith(".html")) {
            response = "text/html";
        } else if (fileName.endsWith(".xml")) {
            response = "application/xml";
        } else if (fileName.endsWith(".ico")) {
            response = "image/x-icon";
        } else if (fileName.endsWith(".gif")) {
            response = "image/gif";
        } else {
            response = "text/html";
        }
        return response;
    }

}
