import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;


@Mojo(name = "encryption", defaultPhase = LifecyclePhase.COMPILE)
public class EncryptionMojo extends AbstractMojo {

    @Parameter(property = "path")
    private String path;
    @Parameter(property = "configPath",required = true)
    private String configPath;

    private final String basePath = "/target";

    public void execute() {

    }
}