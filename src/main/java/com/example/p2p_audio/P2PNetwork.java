package com.example.p2p_audio;

import javafx.scene.media.AudioSpectrumListener;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Arrays;
import java.util.Set;

public class P2PNetwork {

    /*
     * Basic instance variables, localAddress - ipv4 IP address
     * port - port number to use, for our application we will use 55123
     * ServerSocket - a serverSocket java object, which will be used to create a socket where our app will pass messages
     * PeerDiscovery object - the PeerDiscovery class is used to broadcast a message on the network where the application is running, whenever a new peer joins the
     * network, in order for the stream information to be shared between all nodes
     */
    private final String localAddress;
    private final int port;
    private final ServerSocket serverSocket;
    private final Thread listenerThread;
    private final PeerDiscovery peerDiscovery;

    public P2PNetwork(String localAddress, int port) throws Exception {
        this.localAddress = localAddress;
        this.port = port;
        this.serverSocket = new ServerSocket(port);
        this.peerDiscovery = new PeerDiscovery(port);
        this.listenerThread = new Thread(new ConnectionListener());
        this.listenerThread.start();

        peerDiscovery.start();
    }

    private class ConnectionListener implements Runnable {
        public void run() {
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    handleConnection(socket);
                } catch (IOException e) {
                    System.err.println("Error accepting connection: " + e.getMessage());
                }
            }
        }
    }

    private void handleConnection(Socket socket) {
        // handle incoming connections here

    }

    public class PeerDiscovery {
        private final int port;
        private final String discoveryMessage;
        private Thread thread;
        private boolean running;
        private Set<InetAddress> peers;
        private InetAddress group;

        private static final int PORT = 55123;
        private static final int BUFFER_SIZE = 1024;
        private static final String DISCOVERY_MESSAGE = "AudioStream_Discovery_Message";

        private DatagramChannel datagramChannel;
        private AudioSpectrumListener audioSpectrumListener;
        private InetAddress broadcastAddress;
        private InetAddress localAddress;
        private boolean isStreaming;

        public PeerDiscovery(int port) {
            this.port = port;
            this.discoveryMessage = "ANYONE_STREAMING" + port;
        }

        public void start() throws Exception {
            // Broadcast a discovery message to find out if there are other hosts on the network
            ByteBuffer buffer = ByteBuffer.wrap(discoveryMessage.getBytes());
            datagramChannel.send(buffer, new InetSocketAddress(broadcastAddress, PORT));

            // Listen for responses to the discovery message
            DatagramSocket datagramSocket = new DatagramSocket(PORT);
            byte[] responseBuffer = new byte[BUFFER_SIZE];
            DatagramPacket responsePacket = new DatagramPacket(responseBuffer, responseBuffer.length);

            boolean isFirstOnNetwork = true;
            while (isFirstOnNetwork) {
                Arrays.fill(responseBuffer, (byte) 0);
                datagramSocket.receive(responsePacket);

                String message = new String(responsePacket.getData()).trim();
                if (DISCOVERY_MESSAGE.equals(message)) {
                    // If another host responds to the discovery message, then this host is not the first on the network
                    isFirstOnNetwork = false;
                }
            }
        }

            public void stop () {
                // Stop the discovery thread
                running = false;
                thread.interrupt();
            }
        }
    }
