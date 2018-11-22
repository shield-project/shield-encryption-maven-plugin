package org.shield.project.encryption.tool;

import org.apache.maven.plugin.MojoFailureException;
import org.shield.project.encryption.config.maven.EncryptEnum;
import org.shield.project.encryption.config.maven.MavenSupport;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigTool {
    public static void check(String secretKeyPath, String secretKey, String configSuffix, String configPath) throws MojoFailureException {
        if ((Objects.isNull(secretKey) || secretKey.trim().isEmpty()) && (Objects.isNull(secretKeyPath) || secretKeyPath.trim().isEmpty()))
            throw new MojoFailureException("Did you forget to defined secretKey or secretKeyPath?");

        if (Objects.isNull(configPath) || configPath.isEmpty())
            throw new MojoFailureException("Did you forget to defined configPath?");
    }

    public static String fetchSecret(String secretKeyPath) throws MojoFailureException {
        File file = new File(secretKeyPath);
        if (!file.exists())
            throw new MojoFailureException("can not find the " + secretKeyPath + " file");
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            String secret = bufferedReader.readLine();
            bufferedReader.close();
            return secret;
        } catch (Exception e) {
            throw new MojoFailureException(e.getMessage());
        }
    }

    static Pattern pattern;

    public static List<File> searchFile(String secretKey,String configPath, String configSuffix) throws MojoFailureException {
        List<File> files = new ArrayList<>();
        File file = new File(configPath);
        if (!file.exists())
            throw new MojoFailureException("folder " + configPath + " does not exists");
        pattern = Pattern.compile(configSuffix);
        searchFile(file, files);
        return files;
    }

    public static void findAndReplace(String secretKey,Property property, File file) throws MojoFailureException {
        switch (property) {
            case PROPERTY:
                findAndReplaceByProperty(secretKey,file);
                break;
            case YAML:
                findAndReplaceByYAML(secretKey,file);
                break;
            default:
                //the block will be enter???
                break;
        }
    }

    public static void findAndReplaceByProperty(String secretKey,File file) throws MojoFailureException {
        List<EncryptEnum> encryptEnumList = MavenSupport.encryptEnumList;
        Properties property = new Properties();
        try {
            property.load(new FileInputStream(file));
        } catch (IOException e) {
            throw new MojoFailureException(e.getMessage());
        }
        Enumeration<?> enumeration = property.propertyNames();
        while (enumeration.hasMoreElements()) {
            String key = (String) enumeration.nextElement();
            String value = property.getProperty(key);
            Optional<EncryptEnum> any = encryptEnumList.stream().filter(e -> value.startsWith(e.getPrefix()) && value.endsWith(e.getSuffix())).findAny();
            if (!any.isPresent()) continue;
            EncryptEnum encryptEnum = any.get();
            String encryption = EncryptionTool.encryption(secretKey,value, encryptEnum);
            property.setProperty(key,encryption);
        }
        try(FileOutputStream fileOutputStream = new FileOutputStream(file)){
            property.store(fileOutputStream,"Encryption by shield-project-maven-plugin");
        }catch (Exception e){
            throw new MojoFailureException(e.getMessage());
        }
    }

    public static void findAndReplaceByYAML(String secretKey,File file) {

    }

    private static void searchFile(File file, List<File> files) {
        if (file.isFile()) {
            Matcher matcher = pattern.matcher(file.getName());
            if (matcher.find()) files.add(file);
        } else if (file.isDirectory()) {
            File[] files1 = file.listFiles();
            for (File file1 : files1) {
                searchFile(file1, files);
            }
        } else {
            //the block will be enter???
        }

    }

}
