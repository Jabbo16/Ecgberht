package ecgberht.CherryVis;

import org.openbw.bwapi4j.type.UnitType;

import java.util.*;

class TraceData {
    int _version = 0;
    Map<String, HashMap<String, String>> board_updates = new TreeMap<>(Comparator.comparingInt(Integer::parseInt));
    private Map<String, List<Object>> draw_commands = new TreeMap<>(); // TODO change Object to class
    Map<String, Object> game_values = new TreeMap<>(); // TODO change Object to class
    private List<String> heatmaps = new ArrayList<>(); //TODO what is this?
    List<Object> logs = new ArrayList<>(); // TODO change Object to Log class
    private List<Object> tasks = new ArrayList<>(); //TODO what is this?
    Map<String, Object> tensors_summaries = new TreeMap<>(); //TODO what is this?
    private List<Object> trees = new ArrayList<>(); //TODO what is this?
    private Map<String, String> types_names = new TreeMap<>();
    Map<String, List<UnitSeenInfo>> units_first_seen = new TreeMap<>();
    Map<String, Object> units_logs = new TreeMap<>();
    Map<String, Object> units_updates = new TreeMap<>();

    TraceData() {
        for (UnitType it : UnitType.values()) {
            types_names.put(String.valueOf(it.getId()), it.name());
        }
    }
}
