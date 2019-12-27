import java.util.HashMap;

class SymbolTableRow {
    private String name;
    private String type;
    private int index;
    
    public SymbolTableRow(String name, String type, int index) {
        this.name = name;
        this.type = type;
        this.index = index;
    }

    public String getName() {
        return this.name;
    }

    public String getType() {
        return this.type;
    }

    public int getIndex() {
        return this.index;
    }
}