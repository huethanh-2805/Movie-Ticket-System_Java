package server;

import java.io.Serializable;

public class Show implements Serializable {
    private String timeStart;
    private String timeEnd;
    private int rows;
    private int cols;

    public Show(String timeStart, String timeEnd, int rows, int cols) {
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
        this.rows = rows;
        this.cols = cols;
    }

    public String getTimeStart() {
        return timeStart;
    }

    public String getTimeEnd() {
        return timeEnd;
    }

    public boolean bookSeat(int row, int col) {
        // Implementation of seat booking logic
        return true;
    }
}
