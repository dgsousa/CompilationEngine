import java.util.HashMap;

class SymbolTableBlock {
    private HashMap<String, SymbolTableRow> symbolBlock = new HashMap<String, SymbolTableRow>();
    private String blockName;
    
    public int getSize() {
        return this.symbolBlock.size();
    }
    
    public void addRow(String name, String type) {
        int index;
        SymbolTableRow row;
        index = this.symbolBlock.size();
        row = new SymbolTableRow(name, type, index);
        this.symbolBlock.put(name, row);
    }

    public void resetBlock() {
        this.symbolBlock = new HashMap<String, SymbolTableRow>();
    }

    public String getType(String name) {
        return this.symbolBlock
            .get(name)
            .getType();
    }

    public String getKind() {
        return this.blockName;
    }

    public int getIndex(String name) {
        if(this.symbolBlock.containsKey(name)) {
            return this.symbolBlock
                .get(name)
                .getIndex();
        }
        return -1;
    }

    public Boolean containsKey(String name) {
        return this.symbolBlock.containsKey(name);
    }

    public SymbolTableBlock(String blockName) {
        this.blockName = blockName;
    }
}