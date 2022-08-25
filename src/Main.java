import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Main {

    private static HttpServer makeServer() throws IOException {
        InetSocketAddress address = new InetSocketAddress("localhost", 9889);
        System.out.printf("Start server by address http://%s:%s%n", address.getHostName(), address.getPort());

        HttpServer server = HttpServer.create(address, 50);
        System.out.println("Server started successfully");

        return server;
    }

    private static void initRoutes(HttpServer server){
        server.createContext("/", Main::handleRequest);
        server.createContext("/apps/", Main::apps);
        server.createContext("/apps/profile", Main::profile);
        server.createContext("/index.html", Main::htmlMain);
    }
    private static void htmlMain(HttpExchange exchange){
        try {
            exchange.getResponseHeaders().add("html", "text/html");
            int response = 200;
            int length = 0;
            exchange.sendResponseHeaders(response, length);
            try(PrintWriter writer = getWriterFrom(exchange)){

                List<String> html = Files.readAllLines(Paths.get("C:\\Users\\Max\\Downloads\\homework\\homework\\index.html"), UTF_8);
                for (String s: html) {
                    writer.write(s);
                }
                writer.flush();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private static void handleRequest(HttpExchange exchange){
        try {
            exchange.getResponseHeaders().add("Content-type", "text/plain; charset=uft-8");
            int response = 200;
            int length = 0;
            exchange.sendResponseHeaders(response, length);

            try(PrintWriter writer = getWriterFrom(exchange)){
                String method = exchange.getRequestMethod();
                URI uri = exchange.getRequestURI();
                String ctxPath = exchange.getHttpContext().getPath();

                write(writer, "HTTP method", method);
                write(writer, "request", uri.toString());
                write(writer, "processed through", ctxPath);
                writeHeaders(writer, "Request Headers", exchange.getRequestHeaders());
                writeData(writer, exchange);
                writer.flush();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private static void apps(HttpExchange exchange){
        try {
            exchange.getResponseHeaders().add("Content-type", "html/text; charset=uft-8");
            int response = 404;
            int length = 0;
            exchange.sendResponseHeaders(response, length);

            try(PrintWriter writer = getWriterFrom(exchange)){
                write(writer, "Headers", String.valueOf(exchange.getResponseHeaders()));
                write(writer, "Response Code", String.valueOf(exchange.getResponseCode()));
                writer.flush();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private static void profile(HttpExchange exchange){
        try {
            exchange.getResponseHeaders().add("Content-type", "text/plain; charset=uft-8");
            int response = 200;
            int length = 0;
            exchange.sendResponseHeaders(response, length);

            try(PrintWriter writer = getWriterFrom(exchange)){
                write(writer, "Protocol", exchange.getProtocol());
                write(writer, "Address", String.valueOf(exchange.getLocalAddress()));
                write(writer, "Response Code", String.valueOf(exchange.getResponseCode()));
                writeData(writer, exchange);
                writer.flush();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private static PrintWriter getWriterFrom(HttpExchange exchange){
        OutputStream outputStream = exchange.getResponseBody();
        Charset charset = StandardCharsets.UTF_8;
        return new PrintWriter(outputStream, false, charset);
    }

    private static void writeHeaders(Writer writer, String type, Headers headers){
        write(writer, type, "");
        headers.forEach((k,v) -> write(writer, "\t" + k, v.toString()));
    }

    private static void write(Writer writer, String msg, String method){
        String date = String.format("%s: %s%n%n", msg, method);

        try {
            writer.write(date);
        }catch (Exception e){
            e.getStackTrace();
        }
    }

    private static BufferedReader getReader(HttpExchange exchange){
        InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        return new BufferedReader(isr);
    }

    private static void writeData(Writer writer, HttpExchange exchange){
        try (BufferedReader reader = getReader(exchange)){
            if(!reader.ready()) return;

            write(writer, "Data Block", "");
            reader.lines().forEach(e -> write(writer, "\t", e));
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try{
            HttpServer server = makeServer();
            server.start();
            initRoutes(server);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
