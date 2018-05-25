package ecgberht.Config;

public class EcgberhtConfig {
    public boolean debugConsole;
    public boolean debugScreen;
    public boolean debugText;
    public boolean sounds;
    public boolean enableLatCom;

    public EcgberhtConfig() {
    }

    public EcgberhtConfig(boolean debugConsole, boolean debugScreen, boolean debugText, boolean enableLatcom, boolean sounds) {
        this.debugConsole = debugConsole;
        this.debugScreen = debugScreen;
        this.debugText = debugText;
        this.sounds = sounds;
        this.enableLatCom = enableLatcom;
    }
}
