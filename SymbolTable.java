import java.util.HashMap;

public class SymbolTable {
    HashMap<String, SymbolTableBlock> table = new HashMap<String, SymbolTableBlock>() {{
        put("field", new SymbolTableBlock("field"));
        put("static", new SymbolTableBlock("static"));
        put("var", new SymbolTableBlock("var"));
        put("argument", new SymbolTableBlock("argument"));
    }};

    public void define(String name, String type, String kind) {
        this.table
            .get(kind)
            .addRow(name, type);
    }

    public void startSubroutine() {
        this.table.put("var", new SymbolTableBlock("var"));
        this.table.put("argument", new SymbolTableBlock("argument"));
    }

    public int getVarCount(String kind) {
        return this.table
            .get(kind)
            .getSize();
    }

    private SymbolTableBlock getBlockByName(String name) {
        if(this.table.get("var").containsKey(name)) {
            return this.table.get("var");
        } else if(this.table.get("argument").containsKey(name)) {
            return this.table.get("argument");
        } else if(this.table.get("field").containsKey(name)) {
            return this.table.get("field");
        } else {
            return this.table.get("static");
        }
    }

    public String typeOf(String name) {
        return this
            .getBlockByName(name)
            .getType(name);
    }

    public String kindOf(String name) {
        return this
            .getBlockByName(name)
            .getKind();
    }

    public int indexOf(String name) {
        return this
            .getBlockByName(name)
            .getIndex(name);
    }

    public String getSymbolString(String name) {
        SymbolTableBlock block = getBlockByName(name);
        return (
            " name: " + name + "\n" +
            " category: " + block.getKind() + "\n" +
            " isStandard: true\n" +
            " index: " + block.getIndex(name) + "\n" +
            " usage: definition\n"
        );
    }
}