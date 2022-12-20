package org.pjgg.temporal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jboss.logging.Logger;
import org.junit.jupiter.api.extension.ExtensionContext;

import io.quarkus.test.QuarkusProdModeTest;

public class QuarkusTemporalAppBuilder extends QuarkusProdModeTest {

    private static final Logger LOG = Logger.getLogger(QuarkusTemporalAppBuilder.class);
    private TemporalServerResource temporalServer;

    public QuarkusTemporalAppBuilder(TemporalServerResource temporalServer) {
        this.temporalServer = temporalServer;
        temporalServer.start();
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        super.afterAll(extensionContext);
        temporalServer.stop();
    }

    @Override
    public QuarkusProdModeTest setCommandLineParameters(String... commandLineParameters) {
        List<String> args = new ArrayList<>(Arrays.asList(commandLineParameters));
        args.add("-p " + temporalServer.getPort());

        String[] cmd = args.toArray(commandLineParameters);
        LOG.info("Running command:");
        LOG.info(String.join(" ", cmd));

        return super.setCommandLineParameters(cmd);
    }
}
