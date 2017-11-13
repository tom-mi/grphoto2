package de.rfnbrgr.grphoto2.testutil

import de.rfnbrgr.grphoto2.util.UdpServer
import groovy.util.logging.Slf4j
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

@Slf4j
class SsdpTestService implements TestRule {

    Map<String, String> responsesBySearchTarget = [:]

    @Override
    Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            void evaluate() throws Throwable {
                def server = new UdpServer()
                server.multicast = true
                server.address = InetAddress.getByName('239.255.255.250')
                server.port = 1900
                server.handler = { InetAddress address, int port, String rawRequest ->
                    def request = parseSsdpRequest(rawRequest)
                    if (request && request.method == 'M-SEARCH') {
                        def response = responsesBySearchTarget.get(request.ST)
                        if (response) {
                            log.debug("Sending response to $address:$port:\n$response")
                            server.send(address, port, response)
                        }
                    }
                }
                log.debug('Starting')
                server.start()
                base.evaluate()
                log.debug('Stopping')
                server.stop()
                log.debug('Stopped')
            }
        }
    }

    private static parseSsdpRequest(String request) {
        def lines = request.readLines()
        def header = lines.first()
        def method = header.split().first()
        lines.tail().findAll().collectEntries { line ->
            if (line.contains(':')) {
                def (key, value) = line.split(':', 2)
                [(key): value.trim()]
            } else {
                [:]
            }
        } + [method: method]
    }
}
