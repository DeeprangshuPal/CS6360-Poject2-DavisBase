public class column_info{
    private String column_name;
    private String data_type;
    private byte ordinal_position;
    private String isnullable;
    private String column_key;

    public column_info(String column_name, String data_type, byte ordinal_position, String isnullable, String column_key) {
        this.column_name = column_name;
        this.data_type = data_type;
        this.ordinal_position = ordinal_position;
        this.isnullable = isnullable;
        this.column_key = column_key;
    }

    public column_info() {
        this.column_name = "";
        this.data_type = "";
        this.ordinal_position = -1;
        this.isnullable = "";
        this.column_key = "";
    }

    public String getColumn_name() {
        return column_name;
    }

    public void setColumn_name(String column_name) {
        this.column_name = column_name;
    }

    public String getData_type() {
        return data_type;
    }

    public void setData_type(String data_type) {
        this.data_type = data_type;
    }

    public byte getOrdinal_position() {
        return ordinal_position;
    }

    public void setOrdinal_position(byte ordinal_position) {
        this.ordinal_position = ordinal_position;
    }

    public String getIsnullable() {
        return isnullable;
    }

    public void setIsnullable(String isnullable) {
        this.isnullable = isnullable;
    }

    public String getColumn_key() {
        return column_key;
    }

    public void setColumn_key(String column_key) {
        this.column_key = column_key;
    }
}
