package de.rfnbrgr.grphoto2.testutil

import groovy.util.logging.Slf4j
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

import javax.jmdns.JmDNS
import javax.jmdns.ServiceInfo

@Slf4j
class MdnsTestService implements TestRule {

    final jmdns

    MdnsTestService(InetAddress bindAddress) {
        jmdns = JmDNS.create(bindAddress)
    }

    @Override
    Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            void evaluate() throws Throwable {
                base.evaluate()
                log.debug('Unregistering all services...')
                jmdns.unregisterAllServices()
                log.debug('Done unregistering all services...')
            }
        }
    }

    def registerService(String type, String name, int port, String text) {
        log.debug("Registering service $type - $name")
        ServiceInfo serviceInfo = ServiceInfo.create(type, name, port, text)
        jmdns.registerService(serviceInfo)
    }

}
