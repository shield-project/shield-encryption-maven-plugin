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
    private String secretKey;
    @Parameter(property = "configSuffix", required = true)
    private String configSuffix;
    @Parameter(property = "configPath", required = true)
    private String configPath;

    public void execute() throws MojoFailureException {
        ConfigTool.check(secretKeyPath, secretKey, configSuffix, configPath);
        if (configSuffix != null && !configSuffix.isEmpty())
            configSuffix = configSuffix.replace(".", "\\.").replace("*", ".*?").concat("$");
        String secret;
        if (Objects.nonNull(secretKey) && !secretKey.isEmpty())
            secret = secretKey;
        else {
            secret = ConfigTool.fetchSecret(configPath);
        }
        if (Objects.isNull(secret) && secret.isEmpty())
            throw new MojoFailureException("Did you forget set the secret?");
        List<File> files = ConfigTool.searchFile(configPath, configSuffix);
        for (File file : files) {
            String name = file.getName();
            Property property = null;
            if (name.endsWith("properties") || name.endsWith("PROPERTIES"))
                property = Property.PROPERTY;
            else if (name.endsWith("yml") || name.endsWith("yaml") || name.endsWith("YAML") || name.endsWith("YML"))
                property = Property.YAML;
            else {
                throw new MojoFailureException("This file not support yet " + name);
            }
            try {
                ConfigTool.findAndReplace(secretKey, property, file);
            } catch (MojoFailureException e1) {
                e1.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws MojoFailureException {
        EncryptionMojo encryptionMojo = new EncryptionMojo();
        encryptionMojo.configPath = "C:\\Users\\PC\\IdeaProjects\\qiyu-crm\\target\\classes";
        encryptionMojo.configSuffix = "*.yml";
        encryptionMojo.secretKey = "123123";
        encryptionMojo.execute();
    }
}