import java.util.HashMap;

class SymbolTableBlock {
    private HashMap<String, SymbolTableRow> symbolBlock = new HashMap<String, SymbolTableRow>();
    private HashMap<String, Integer> indexTable = new HashMap<String, Integer>();
    private String blockName;
    
    public int getSize() {
        return this.symbolBlock.size();
    }
    
    public void addRow(String name, String type) {
        int index;
        SymbolTableRow row;
        if(this.indexTable.containsKey(type)) {
            this.indexTable.put(type, this.indexTable.get(type) + 1);
        } else {
            this.indexTable.put(type, 0);
        }
        index = this.indexTable.get(type);
        row = new SymbolTableRow(name, type, index);
        this.symbolBlock.put(name, row);
    }

    public void resetBlock() {
        this.symbolBlock = new HashMap<String, SymbolTableRow>();
        this.indexTable = new HashMap<String, Integer>();
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
        return this.symbolBlock
            .get(name)
            .getIndex();
    }

    public Boolean containsKey(String name) {
        return this.symbolBlock.containsKey(name);
    }

    public SymbolTableBlock(String blockName) {
        this.blockName = blockName;
    }
}