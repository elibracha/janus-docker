package io.kubemen.janus.extensions;

import io.kubemen.janus.annotations.Provide;
import io.kubemen.janus.core.DockerConnector;
import io.kubemen.janus.core.DockerPlatformManager;
import io.kubemen.janus.core.PlatformManager;
import io.kubemen.janus.domain.CommendConfig;
import io.kubemen.janus.exceptions.ImageNameMissingException;
import io.kubemen.janus.exceptions.PlatformFailedException;
import io.kubemen.janus.exceptions.PlatformFailedPullImageException;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Arrays;
import java.util.List;

public class ContainerExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback {

    private PlatformManager platformManager;

    @Override
    public void afterTestExecution(ExtensionContext extensionContext) throws Exception {
        platformManager.stop();
    }

    @Override
    public void beforeTestExecution(ExtensionContext extensionContext) throws Exception {
        extensionContext.getElement().ifPresent(e -> {
            boolean isPresent = e.isAnnotationPresent(Provide.class);
            if(isPresent){
                String image = e.getAnnotation(Provide.class).image();
                String tag = e.getAnnotation(Provide.class).tag();
                List<String> portForwarding = Arrays.asList(e.getAnnotation(Provide.class).portForwarding());

                CommendConfig commendConfig = new CommendConfig();
                if(!image.isBlank())
                    commendConfig.setImage(image);
                if(!tag.isBlank())
                    commendConfig.setTag(tag);
                if(!portForwarding.isEmpty())
                    commendConfig.setPortForwarding(portForwarding);

                this.platformManager = new DockerPlatformManager(new DockerConnector().connect());

                try {
                    platformManager.run(commendConfig);
                } catch (ImageNameMissingException | PlatformFailedException |
                        PlatformFailedPullImageException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
}