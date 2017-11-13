package de.rfnbrgr.grphoto2.util

import groovy.util.logging.Slf4j

@Slf4j
class UdpServer {

    private static final int PACKET_SIZE = 1024
    private static final int SOCKET_TIMEOUT_MS = 100

    private boolean running = true
    private DatagramSocket socket

    Closure handler
    int port = 0
    InetAddress address
    boolean multicast = false

    void start() {
        if (multicast) {
            socket = new MulticastSocket(port)
            socket.joinGroup(address)
        } else {
            socket = new DatagramSocket(port, address)
        }
        log.debug("Listening on ${multicast ? 'multicast ' : ''}${socket.localAddress}:${socket.localPort}")
        socket.setSoTimeout(SOCKET_TIMEOUT_MS)
        Thread.start {
            this.run()
        }
    }

    void stop() {
        log.debug("Stopping")
        running = false
    }

    void run() {
        while (running) {
            def receivePacket = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE)
            try {
                socket.receive(receivePacket)
            } catch (SocketTimeoutException ignored) {
                continue
            }
            def sourceAddress = receivePacket.address
            def sourcePort = receivePacket.port
            def data = new String(receivePacket.data)
            log.debug("Received packet from $sourceAddress:$sourcePort with length ${data.size()} and content\n--------\n$data\n--------")
            if (handler) {
                handler(sourceAddress, sourcePort, data)
            }
        }
    }

    void send(InetAddress address, int port, String message) {
        log.debug("Sending message to $address:$port\n--------\n$message\n--------")
        socket.send(new DatagramPacket(message.bytes, message.bytes.length, address, port))
    }
}
