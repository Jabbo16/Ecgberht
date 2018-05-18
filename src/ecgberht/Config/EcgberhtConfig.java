package ecgberht.Config;

public class EcgberhtConfig {
    public boolean debugText;
    public boolean debugScreen;
    public boolean sounds;

    public EcgberhtConfig(){ }

    public EcgberhtConfig(boolean debugText, boolean debugScreen, boolean sounds){
        this.debugText = debugText;
        this.debugScreen = debugScreen;
        this.sounds = sounds;
    }
}
