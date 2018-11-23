package org.shield.project.encryption.tool;

import org.apache.maven.plugin.MojoFailureException;
import org.shield.project.encryption.config.maven.EncryptEnum;
import org.shield.project.encryption.config.maven.MavenSupport;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.events.Event;
import org.yaml.snakeyaml.nodes.Node;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.shield.project.encryption.tool.ConstantInfo.COMMENT_INFO;

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

    private static Pattern pattern;

    public static List<File> searchFile(String secretKey, String configPath, String configSuffix) throws MojoFailureException {
        List<File> files = new ArrayList<>();
        File file = new File(configPath);
        if (!file.exists())
            throw new MojoFailureException("folder " + configPath + " does not exists");
        pattern = Pattern.compile(configSuffix);
        searchFile(file, files);
        return files;
    }

    public static void findAndReplace(String secretKey, Property property, File file) throws MojoFailureException {
        switch (property) {
            case PROPERTY:
                findAndReplaceByProperty(secretKey, file);
                break;
            case YAML:
                findAndReplaceByYAML(secretKey, file);
                break;
            default:
                //the block will be enter???
                break;
        }
    }

    static List<EncryptEnum> encryptEnumList = MavenSupport.encryptEnumList;

    public static void findAndReplaceByProperty(String secretKey, File file) throws MojoFailureException {

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
            String encryption = EncryptionTool.encryption(secretKey, value, encryptEnum);
            property.setProperty(key, encryption);
        }
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            property.store(fileOutputStream, COMMENT_INFO);
        } catch (Exception e) {
            throw new MojoFailureException(e.getMessage());
        }
    }

    public static void findAndReplaceByYAML(String secretKey, File file) {
    }

    /**
     * 查找所有与configSuffix匹配的文件
     *
     * @param file  待匹配文件
     * @param files 完成匹配的文件
     */
    private static void searchFile(File file, List<File> files) {
        if (file.isFile()) {
            Matcher matcher = ConfigTool.pattern.matcher(file.getName());
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

//    public static void main(String[] args) throws FileNotFoundException {
//        File file = new File("target/classes/application.yml");
//        Yaml yaml = new Yaml();
//        Map<String, Object> map = (Map<String, Object>) yaml.load(new FileReader(file));
//        findAndReplace4Yaml("123456", null, null, map);
//        System.out.println(map);
//        System.out.println(yaml.dumpAs(map, null, DumperOptions.FlowStyle.AUTO));
//    }

    private static void findAndReplace4Yaml(String secretKey, Object parent, Object key, Object data) {
        if (data instanceof Map) {
            Map<String, Object> dataMap = (Map<String, Object>) data;
            dataMap.entrySet().forEach(e -> findAndReplace4Yaml(secretKey, dataMap, e.getKey(), e.getValue()));
        } else if (data instanceof List) {
            ArrayList dataList = (ArrayList) data;
            for (int i = 0; i < dataList.size(); i++)
                findAndReplace4Yaml(secretKey, data, i, dataList.get(i));
        } else if (data instanceof String) {
            String needEncrypt = (String) data;
            Optional<EncryptEnum> any = encryptEnumList.stream().filter(e -> needEncrypt.startsWith(e.getPrefix()) && needEncrypt.endsWith(e.getSuffix())).findAny();
            if (!any.isPresent()) return;
            EncryptEnum encryptEnum = any.get();
            String encryption = EncryptionTool.encryption(secretKey, needEncrypt, encryptEnum);
            if (parent instanceof Map) {
                ((Map) parent).put(key, encryption);
            } else if (parent instanceof List) {
                int index = (int) key;
                ((ArrayList) parent).set(index, encryption);
            }
        }
    }
}
