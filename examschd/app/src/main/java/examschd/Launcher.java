package examschd;

/**
 * Launcher class for JavaFX fat JAR compatibility.
 * JavaFX requires this workaround when running from a fat JAR because it checks
 * if the main class extends Application before the module system is initialized.
 */
public class Launcher {
    public static void main(String[] args) {
        App.main(args);
    }
}
