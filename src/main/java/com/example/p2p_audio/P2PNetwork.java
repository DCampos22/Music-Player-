package com.example.p2p_audio;

import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.MembershipKey;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.io.*;

public class P2PNetwork {

    /*
     * Basic instance variables, localAddress - ipv4 IP address
     * port - port number to use, for our application we will use 55123
     * ServerSocket - a serverSocket java object, which will be used to create a socket where our app will pass messages
     * PeerDiscovery object - the PeerDiscovery class is used to broadcast a message on the network where the application is running, whenever a new peer joins the
     * network, in order for the stream information to be shared between all nodes
     */
    private String localAddress;
    private int port;
    private ServerSocket serverSocket;
    private Thread listenerThread;
    private PeerDiscovery peerDiscovery;

    public P2PNetwork(String localAddress, int port) throws IOException {
        this.localAddress = localAddress;
        this.port = port;
        this.serverSocket = new ServerSocket(port);
        this.peerDiscovery = new PeerDiscovery(port);
        this.listenerThread = new Thread(new ConnectionListener());
        this.listenerThread.start();
    }

    public void discoverPeers() throws Exception {
        this.peerDiscovery.start();
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
        private int port;
        private String discoveryMessage;
        private Thread thread;
        private boolean running;

        public PeerDiscovery(int port) {
            this.port = port;
            this.discoveryMessage = "P2PNetworkDiscovery:" + port;
        }

        public void start() throws Exception {
            // Create a datagram channel
            DatagramChannel channel = DatagramChannel.open();
            channel.configureBlocking(false);
            channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);

            // Bind the channel to the multicast address and port
            InetAddress group = InetAddress.getByName("224.0.0.1");
            channel.bind(new InetSocketAddress(group, port));

            // Join the multicast group
            NetworkInterface networkInterface = NetworkInterface.getByInetAddress(InetAddress.getByName(localAddress));
            MembershipKey key = channel.join(group, networkInterface);

            // Get the IP address of the network interface
            InetAddress interfaceAddress = null;


            // Start the discovery thread
            running = true;
            thread = new Thread(() -> {
                while (running) {
                    try {
                        // Check if any datagrams are available
                        Selector selector = Selector.open();
                        channel.register(selector, SelectionKey.OP_READ);
                        selector.select();

                        // Read incoming datagrams
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        SocketAddress address = channel.receive(buffer);
                        buffer.flip();
                        String message = new String(buffer.array(), 0, buffer.limit());

                        // Check if the message is a discovery message
                        if (message.equals(discoveryMessage)) {
                            // Send a response message with network information
                            String responseMessage = "P2PNetworkResponse:" + interfaceAddress.getHostAddress() + ":" + port;
                            ByteBuffer responseBuffer = ByteBuffer.wrap(responseMessage.getBytes());
                            channel.send(responseBuffer, address);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
        }

        public void stop() {
            // Stop the discovery thread
            running = false;
            thread.interrupt();
        }
    }
}