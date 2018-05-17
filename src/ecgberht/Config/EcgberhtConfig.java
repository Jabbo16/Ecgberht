package ecgberht.Config;

public class EcgberhtConfig {
    public boolean debug;
    public boolean sounds;

    public EcgberhtConfig(){
    }

    public EcgberhtConfig(boolean debug, boolean sounds){
        this.debug = debug;
        this.sounds = sounds;

    }
}
