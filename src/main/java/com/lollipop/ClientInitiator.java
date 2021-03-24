import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientInitiator {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("127.0.0.1", 9999);
        PrintWriter writer = new PrintWriter(socket.getOutputStream());
        writer.print(args[0]);
        writer.flush();
        writer.close();
    }
}
