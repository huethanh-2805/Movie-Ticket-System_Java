package server;

public class BookingRequest {
    private String name;
    private String phone;
    private int row;
    private int col;

    public BookingRequest(String name, String phone, int row, int col) {
        this.name = name;
        this.phone = phone;
        this.row = row;
        this.col = col;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }
    
    
}
