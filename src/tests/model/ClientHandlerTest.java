import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClientHandlerTest {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final InputStream originalIn = System.in;

    @BeforeEach
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
        System.setIn(originalIn);
    }

    @Test
    public void testClientHandler() throws IOException {
        // Mock client socket
        PipedOutputStream pipedOut = new PipedOutputStream();
        PipedInputStream pipedIn = new PipedInputStream(pipedOut);
        Socket mockSocket = new Socket() {
            public InputStream getInputStream() {
                return pipedIn;
            }
            public OutputStream getOutputStream() {
                return pipedOut;
            }
        };

        // Mock client sockets and names
        Set<Socket> clientSockets = new HashSet<>();
        Set<String> clientNames = new HashSet<>();

        // Create and start client handler
        ClientHandler clientHandler = new ClientHandler(mockSocket, clientSockets, clientNames);
        clientHandler.start();

        // Simulate client input
        pipedOut.write("TestName\n".getBytes());
        pipedOut.write("Hello from client\n".getBytes());

        // Wait for client handler to process input
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Check output
        String[] lines = outContent.toString().split(System.lineSeparator());
        assertEquals("Enter your name: ", lines[0]);
        assertEquals("Welcome TestName", lines[1]);
        assertEquals("Server: TestName has joined the chat.", lines[2]);
        assertEquals("TestName: Hello from client", lines[3]);
    }
}
