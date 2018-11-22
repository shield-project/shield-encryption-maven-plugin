package org.shield.project.encryption.tool;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.omg.CORBA.SystemException;

import java.io.IOException;
import java.util.Objects;


@Mojo(name = "encryption", defaultPhase = LifecyclePhase.COMPILE)
public class EncryptionMojo extends AbstractMojo {

    @Parameter(property = "secretKeyPath")
    private String secretKeyPath;
    @Parameter(property = "secretKey")
    private String secretKey;
    @Parameter(property = "configSuffix")
    private String configSuffix;
    @Parameter(property = "configPath", required = true)
    private String configPath;

    private PropertyDiscory propertyDiscory = new PropertyDiscory();

    public void execute() throws MojoFailureException {
        ConfigTools.check(secretKeyPath, secretKey, configSuffix, configPath);
        String secret;
        if (Objects.nonNull(secretKey) && !secretKey.isEmpty())
            secret = secretKey;
        else {
            secret = ConfigTools.fetchSecret(configPath);
        }
        if (Objects.isNull(secret) && secret.isEmpty())
            throw new MojoFailureException("Did you forget set the secret?");
        ConfigTools.searchFile(configPath,configSuffix);
    }
}