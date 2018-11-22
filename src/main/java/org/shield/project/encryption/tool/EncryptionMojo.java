package org.shield.project.encryption.tool;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.List;
import java.util.Objects;


@Mojo(name = "encryption", defaultPhase = LifecyclePhase.COMPILE)
public class EncryptionMojo extends AbstractMojo {

    @Parameter(property = "secretKeyPath")
    private String secretKeyPath;
    @Parameter(property = "secretKey")
    private String secretKey = "123456";
    @Parameter(property = "configSuffix")
    private String configSuffix = "\\.properties";
    @Parameter(property = "configPath", required = true)
    private String configPath = "C:\\Users\\PC\\IdeaProjects\\encryption-tool-plugin\\src\\main\\resources\\a.properties";

    public void execute() throws MojoFailureException {
        ConfigTool.check(secretKeyPath, secretKey, configSuffix, configPath);
        String secret;
        if (Objects.nonNull(secretKey) && !secretKey.isEmpty())
            secret = secretKey;
        else {
            secret = ConfigTool.fetchSecret(configPath);
        }
        if (Objects.isNull(secret) && secret.isEmpty())
            throw new MojoFailureException("Did you forget set the secret?");
        List<File> files = ConfigTool.searchFile(secretKey, configPath, configSuffix);
        files.stream().forEach(
                e -> {
                    try {
                        ConfigTool.findAndReplace(secretKey, Property.PROPERTY, e);
                    } catch (MojoFailureException e1) {
                        e1.printStackTrace();
                    }
                }
        );
    }
}