//package eu.project.rapid.demo;


import java.io.IOException;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.*;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DemoApp {
    
    private static double bytesToDouble(byte[] bytes) {
        return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getDouble();
    }

    public static void main(String[] argv) {
        int port = 12345;
        
        try (ServerSocket serverSocket = new ServerSocket(port, 50, InetAddress.getByName("0.0.0.0"))) {
            System.out.println("Server is listening on port " + port);
            while (true) {
                try (Socket socket = serverSocket.accept();
                     InputStream inputStream = socket.getInputStream();
                     DataInputStream input = new DataInputStream(inputStream);
                     DataOutputStream output = new DataOutputStream(socket.getOutputStream())) {

                    // Reading double parameters
                    byte[] doubleBytes = new byte[8];
                    input.readFully(doubleBytes);
                    double start_x = bytesToDouble(doubleBytes);

                    input.readFully(doubleBytes);
                    double start_y = bytesToDouble(doubleBytes);

                    input.readFully(doubleBytes);
                    double end_x = bytesToDouble(doubleBytes);

                    input.readFully(doubleBytes);
                    double end_y = bytesToDouble(doubleBytes);

                    // Reading int parameters: cycles and size of charMap
                    int cycles = input.readInt();
                    int size = input.readInt();

                    System.out.println("Received start_x: " + start_x);
                    System.out.println("Received start_y: " + start_y);
                    System.out.println("Received end_x: " + end_x);
                    System.out.println("Received end_y: " + end_y);
                    System.out.println("Received cycles: " + cycles);
                    System.out.println("Received charMap size: " + size);

                    // Read charMap data
                    byte[] charMap = new byte[size];
                    input.readFully(charMap);
                    //System.out.println("Received charMap data (partial): " + Arrays.toString(Arrays.copyOf(charMap, 10)));

                    //TODO: Change potential to float everywhere
                    byte[] costs = charMap; // Placeholder, replace with actual data
                    float[] potential = new float[0]; // Placeholder, replace with actual data
                    // Call the constructor with the parameters received from the client

                    //todo:CALL DIJKSTRA
                    Dijkstra dijkstra = new Dijkstra();
                    DijkstraResult demo = dijkstra.calculatePotentials(costs, start_x, start_y, end_x, end_y, cycles, potential);

//                    //SENDING RESULTS(POTENTIALS ARRAY AND BOOLEAN) BACK TO CPP
//                    ByteBuffer buffer = ByteBuffer.allocate(demo.result.potential.length * Float.BYTES);
//                    buffer.order(ByteOrder.BIG_ENDIAN); // Ensure big-endian to match network byte order
//                    for (float value : demo.result.potential) {
//                        buffer.putFloat(value);
//                    }

                    // Send foundLegal
                    output.writeBoolean(demo.result.result);

//                    // Send the size of potentialArray
//                    output.writeInt(buffer.array().length);
//
//                    // Send potentialArray as byte array
//                    output.write(buffer.array());


                    //SENDING RESULTS IN TUPLE(POTENTIALS AS FLOAT AND INDEX AS INT)
                    // Send the length of the tuple array
                    try {
                        output.writeInt(demo.result.newPotential.size());
                    } catch (NullPointerException ex){
                        ex.printStackTrace();
                    }


                    // Serialize and send each tuple
                    ByteBuffer bufferTuple = ByteBuffer.allocate(8);


                    for (int i = 0; i < demo.result.newPotential.size(); i++) {
                        ((Buffer)bufferTuple).clear();
                        bufferTuple.putFloat(demo.result.newPotential.get(i).floatValue());
                        bufferTuple.putInt(demo.result.newPotentialIndex.get(i).intValue());
//                        System.out.println("Tuple: (" +demo.result.newPotential.get(i).floatValue() + ", (" + demo.result.newPotentialIndex.get(i).intValue() + ")");
                        output.write(bufferTuple.array());
                    }

                    output.flush();
                    // For demonstration, sending back a simple acknowledgment
                    output.writeInt(1); // Acknowledgment size
                    output.writeByte(1); // Acknowledgment byte

                } catch (IOException e) {
                    System.out.println("Exception in client communication: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }

        System.exit(0);
    }
}
