import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Example {
    private static final Log LOG = LogFactory.getLog(Example.class);

    public static void main(String[] args) {
        LOG.debug("This is a debug message");
        LOG.info("This is an info message");
        LOG.warn("This is a warning message");
        LOG.error("This is an error message");
    }
}
