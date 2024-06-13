package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class ClientHandler implements Runnable {

    private Socket socket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private List<Event> events;

    public ClientHandler(Socket socket, List<Event> events) throws IOException {
        this.socket = socket;
        this.events = events;
        ois = new ObjectInputStream(socket.getInputStream());
        oos = new ObjectOutputStream(socket.getOutputStream());
    }

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    private List<List<Boolean>> readEditFile(String filePath) throws IOException {
        List<List<Boolean>> data = new ArrayList<>();
        List<String> lines = Files.readAllLines(Paths.get(filePath));

        for (String line : lines) {
            String[] values = line.split(",");
            List<Boolean> row = new ArrayList<>();
            for (String value : values) {
                row.add(Boolean.parseBoolean(value));
            }
            data.add(row);
        }

        return data;
    }
    
    //Đọc giá
    private List<Integer> readPricesFromCSV(String filePath) throws IOException {
        List<Integer> prices = new ArrayList<>();
        List<String> lines = Files.readAllLines(Paths.get(filePath));

        for (String line : lines) {
            prices.add(Integer.parseInt(line.trim()));
        }

        return prices;
    }

    private void sendPriceForScreening(int screening) {
        try {
            List<Integer> prices = readPricesFromCSV("../Data/fare.csv");
            if (screening >= 1 && screening <= prices.size()) {
                int price = prices.get(screening - 1); // Lấy giá vé tương ứng với khu vực
                oos.writeObject(price); // Gửi giá vé về client
            } else {
                oos.writeObject("INVALID_SCREENING"); // Trường hợp không tìm thấy giá vé
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            Object request;
            while ((request = ois.readObject()) != null) {
                if (request.equals("GET_EVENTS")) {
                    System.out.println("Received GET_EVENTS request");
                    oos.writeObject(events);
                } else if (request.equals("GET_SEATS")) {
                    System.out.println("Received GET_SEATS request");
                    List<List<Boolean>> seats = readEditFile("../Data/editSeat.csv");
                    oos.writeObject(seats);
                } else if (request instanceof BookingRequest) {
                    handleBookingRequest((BookingRequest) request);
                } else if (request.equals("GET_PRICE_FOR_SCREENING")) {
                    // Đọc yêu cầu lấy giá vé từ client và gửi lại giá vé tương ứng
                    int screening = ois.readInt();
                    sendPriceForScreening(screening);
                }
            }
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    private void handleBookingRequest(BookingRequest request) {
        // Xử lý yêu cầu đặt vé và cập nhật trạng thái chỗ ngồi
        int row = request.getRow();
        int col = request.getCol();
        String name = request.getName();
        String phone = request.getPhone();

        try {
            List<List<Boolean>> seats = readEditFile("../Data/editSeat.csv");
            seats.get(row).set(col, false);
            updateSeatFile("../Data/editSeat.csv", seats);

            saveBookingInfo("../Data/bookings.csv", name, phone, row, col);
            oos.writeObject("BOOKING_SUCCESS");
        } catch (IOException ex) {
            ex.printStackTrace();
            try {
                oos.writeObject("BOOKING_FAILED");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateSeatFile(String filePath, List<List<Boolean>> seats) throws IOException {
        List<String> lines = new ArrayList<>();
        for (List<Boolean> row : seats) {
            String line = row.stream().map(String::valueOf).collect(Collectors.joining(","));
            lines.add(line);
        }
        Files.write(Paths.get(filePath), lines);
    }

    private void saveBookingInfo(String filePath, String name, String phone, int row, int col) throws IOException {
        String bookingInfo = String.format("%s,%s,%d,%d", name, phone, row, col);
        Files.write(Paths.get(filePath), Collections.singletonList(bookingInfo), StandardOpenOption.APPEND);
    }
}
