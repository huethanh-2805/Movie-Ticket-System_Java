package client;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import server.BookingRequest;
import server.Event;
import server.Show;

public class Client extends javax.swing.JFrame {

    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private boolean connected = false;
    private List<Event> events = new ArrayList<>();
    private String lastSelectedPosition = null; // Biến để lưu trữ vị trí ghế của dòng trước đó

    public Client() {
        initComponents();
        ViewProgram();
        ViewTable();
        initializeButtonListeners();
        initializeTableListener();
    }

    public void ViewProgram() {
        setLocationRelativeTo(null);
        getContentPane().setBackground(Color.decode("#FCFFE7"));

        this.jPanel2.setBackground(Color.decode("#FCFFE7"));
        this.jPanel1.setBackground(Color.decode("#2B3467"));
        this.jPanel3.setBackground(Color.BLACK);
        this.jPanel4.setBackground(Color.decode("#FCFFE7"));
        this.jPanel5.setBackground(Color.decode("#FCFFE7"));
        this.jPanel6.setBackground(Color.decode("#FCFFE7"));
        this.jPanel8.setBackground(Color.decode("#FCFFE7"));
        this.jPanel9.setBackground(Color.decode("#FCFFE7"));
        this.jPanel10.setBackground(Color.decode("#EFF8FE"));
        this.jPanel11.setBackground(Color.decode("#EFF8FE"));
        this.jPanel13.setBackground(Color.RED);
        this.jPanel14.setBackground(Color.LIGHT_GRAY);
        Border border = BorderFactory.createLineBorder(Color.decode("#2B3467"), 2);
        jPanel10.setBorder(border);
        jPanel11.setBorder(border);
        jPanel12.setBackground(Color.LIGHT_GRAY);

        //Custom button
        btnDisconnect.setBackground(Color.RED);
        btnDisconnect.setForeground(Color.WHITE);

        btnConnect.setBackground(Color.decode("#BAD7E9"));
        btnConnect.setForeground(Color.decode("#2B3467"));

        Color grayColor = new Color(204, 204, 204); // Tạo màu xám

        for (Component comp : jPanel4.getComponents()) {
            if (comp instanceof JButton) {
                JButton button = (JButton) comp;
                button.setBackground(grayColor);
            }
        }

        btnBook.setBackground(Color.decode("#CCFFFF"));
    }

    public void ViewTable() {
        // Customize table header
        JTableHeader header = tblListMovie.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 16)); // Set font to bold
        header.setPreferredSize(new Dimension(100, 30)); // Increase header height

        // Set column width for STT
        TableColumn sttColumn = tblListMovie.getColumnModel().getColumn(0);
        sttColumn.setPreferredWidth((int) (0.1 * 435)); // 20% tổng độ rộng

        TableColumn scColumn = tblListMovie.getColumnModel().getColumn(1);
        scColumn.setPreferredWidth((int) (0.3 * 435)); // 50% tổng độ rộng

        TableColumn movieColumn = tblListMovie.getColumnModel().getColumn(2);
        movieColumn.setPreferredWidth((int) (0.6 * 435)); // 30% tổng độ rộng

        // Set row height
        tblListMovie.setRowHeight(25); // Increase row height
    }

    private void handleServerResponse(Object response) {
        if (response instanceof List<?>) {
            List<List<Boolean>> seats = (List<List<Boolean>>) response;
            System.out.println("Seats received: " + seats);
            updateSeatAvailability(seats);
        }
    }

    private void getDataFromServer() {
        try {
            // Gửi yêu cầu lấy danh sách sự kiện
            oos.writeObject("GET_EVENTS");
            Object response = ois.readObject();
            if (response instanceof List<?>) {
                events = (List<Event>) response;
            }

            // Gửi yêu cầu lấy danh sách chỗ ngồi
            oos.writeObject("GET_SEATS");
            response = ois.readObject();
            handleServerResponse(response); // Xử lý dữ liệu ghế ngồi từ server
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi lấy dữ liệu từ máy chủ: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Hàm để cập nhật danh sách sự kiện lên giao diện người dùng
    private void updateEventList(List<Event> events) {
        String selectedDateStr = (String) selectDate.getSelectedItem();
        if (selectedDateStr == null) {
            return;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy");
        try {
            Date selectedDate = dateFormat.parse(selectedDateStr);
            DefaultTableModel tableModel = (DefaultTableModel) tblListMovie.getModel();
            tableModel.setRowCount(0); // Clear existing rows
            int stt = 1;
            for (Event event : events) {
                if (dateFormat.format(event.getDate()).equals(selectedDateStr)) {
                    for (Show show : event.getShows()) {
                        tableModel.addRow(new Object[]{stt++, show.getTimeStart() + " - " + show.getTimeEnd(), event.getName()});
                    }
                }
            }

            if (tableModel.getRowCount() <= 0) {
                JOptionPane.showMessageDialog(this, "Không có suất chiếu nào cho ngày này", "Thông báo", JOptionPane.WARNING_MESSAGE);
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    // Hàm để cập nhật tình trạng chỗ ngồi lên giao diện người dùng
    private void updateSeatAvailability(List<List<Boolean>> seats) {
        // Cập nhật tình trạng chỗ ngồi lên giao diện người dùng (ví dụ: JButton)
        for (int i = 0; i < seats.size(); i++) {
            for (int j = 0; j < seats.get(i).size(); j++) {
                boolean state = seats.get(i).get(j);
                JButton button = getButtonByPosition(i, j);

                if (button != null) {
                    System.out.println("Updating button: " + button.getText() + " to " + (state ? "visible" : "hidden"));
                    button.setVisible(state);
                }
            }
        }
    }

    private JButton getButtonByPosition(int row, int col) {
        String buttonName = getButtonNameByPosition(row, col);

        // Duyệt qua tất cả các thành phần trong Container để tìm JButton có tên tương ứng
        for (Component comp : jPanel11.getComponents()) {
            if (comp instanceof JButton) {
                JButton button = (JButton) comp;
                if (buttonName.equals(button.getText())) {
                    return button;
                }
            }
        }
        return null;
    }

    private String getButtonNameByPosition(int row, int col) {
        String buttonName = "";
        switch (row) {
            case 0:
                buttonName = "A";
                break;
            case 1:
                buttonName = "B";
                break;
            case 2:
                buttonName = "C";
                break;
            case 3:
                buttonName = "D";
                break;
            case 4:
                buttonName = "E";
                break;
            case 5:
                buttonName = "F";
                break;
        }
        buttonName += (col + 1);
        return buttonName;
    }

    //Đặt chỗ
    private void bookSeat(String name, String phone, int row, int col) {
        try {
            BookingRequest request = new BookingRequest(name, phone, row, col);
            oos.writeObject(request);
            Object response = ois.readObject();
            if ("BOOKING_SUCCESS".equals(response)) {
                JOptionPane.showMessageDialog(this, "Đặt vé thành công!");
            } else {
                JOptionPane.showMessageDialog(this, "Đặt vé thất bại!");
            }
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi đặt vé: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int getRowFromButtonName(String buttonName) {
        char rowChar = buttonName.charAt(0);
        return rowChar - 'A'; // Chuyển đổi ký tự thành số thứ tự hàng
    }

    private int getColFromButtonName(String buttonName) {
        String colStr = buttonName.substring(1);
        return Integer.parseInt(colStr) - 1; // Chuyển đổi chuỗi thành số thứ tự cột
    }

    //Cập nhật giá vé
    private void initializeButtonListeners() {
        for (Component comp : jPanel11.getComponents()) {
            if (comp instanceof JButton) {
                JButton button = (JButton) comp;
                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String buttonName = button.getText(); // Lấy tên nút, ví dụ: A10
                        int screening = getScreeningFromButtonName(buttonName);
                        if (screening > 0) {
                            updatePriceForScreening(screening);
                        }
                    }
                });
            }
        }
    }

    private int getScreeningFromButtonName(String buttonName) {
        // Lấy ký tự đầu tiên để xác định hàng
        char rowChar = buttonName.charAt(0);
        // Lấy số sau ký tự để xác định cột
        String colNumStr = buttonName.substring(1);

        try {
            int colNum = Integer.parseInt(colNumStr);

            // Xác định khu vực
            if ((rowChar == 'A' || rowChar == 'B' || rowChar == 'C') && (colNum >= 6 && colNum <= 10)) {
                return 1;
            } else if ((rowChar == 'A' || rowChar == 'B' || rowChar == 'C') && (colNum >= 1 && colNum <= 5)) {
                return 2;
            } else if ((rowChar == 'D' || rowChar == 'E' || rowChar == 'F') && (colNum >= 6 && colNum <= 10)) {
                return 3;
            } else if ((rowChar == 'D' || rowChar == 'E' || rowChar == 'F') && (colNum >= 1 && colNum <= 5)) {
                return 4;
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid button name: " + buttonName);
        }

        return 0; // Trường hợp mặc định hoặc lỗi
    }

    private void updatePriceForScreening(int screening) {
        try {
            // Gửi yêu cầu lấy giá vé từ server dựa trên khu vực chiếu phim được chọn
            oos.writeObject("GET_PRICE_FOR_SCREENING");
            oos.writeInt(screening);
            oos.flush();

            Object response = ois.readObject();
            if (response instanceof Integer) {
                int price = (int) response;
                txtPrice.setText(price + " đồng"); // Cập nhật giá vé lên thành phần hiển thị
            } else if (response.equals("INVALID_SCREENING")) {
                txtPrice.setText("Không tìm thấy giá vé cho khu vực này");
            } else {
                txtPrice.setText("Lỗi khi nhận giá vé từ máy chủ");
            }
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi nhận giá vé từ máy chủ", "Thông báo", JOptionPane.ERROR_MESSAGE);
        }
    }

    //Đổi màu nút khi đã đặt
    private void initializeTableListener() {
        tblListMovie.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tblListMovie.getSelectedRow();
                String selectedDate = (String) selectDate.getSelectedItem();
                String time = tblListMovie.getValueAt(row, 1).toString(); // Lấy giờ từ dòng được chọn
                String movieName = tblListMovie.getValueAt(row, 2).toString(); // Lấy tên phim từ dòng được chọn
                checkAndChangeButtonColor(selectedDate, time, movieName);
            }
        });
    }

    // Hàm để xóa màu background của tất cả các nút
    private void clearAllButtonColors() {
        Color grayColor = new Color(204, 204, 204); // Tạo màu xám
        for (Component comp : jPanel11.getComponents()) {
            if (comp instanceof JButton) {
                JButton button = (JButton) comp;
                button.setBackground(grayColor);
            }
        }
    }

    // Hàm xử lý khi người dùng chọn một dòng mới trong bảng
    private void tblListMovieMouseClicked(java.awt.event.MouseEvent evt) {
        int selectedRow = tblListMovie.getSelectedRow(); // Lấy chỉ số của dòng được chọn
        if (selectedRow >= 0) {
            // Lấy ngày, giờ và tên phim từ dòng được chọn
            String selectedDate = (String) tblListMovie.getValueAt(selectedRow, 0);
            String time = (String) tblListMovie.getValueAt(selectedRow, 1);
            String movieName = (String) tblListMovie.getValueAt(selectedRow, 2);

            // Kiểm tra xem có ghế nào đã được đặt ở dòng mới hay không
            boolean seatBooked = checkSeatBooked(selectedDate, time, movieName);

            // Nếu không có ghế nào đã được đặt, đặt lại màu nền của tất cả các nút về null
            if (!seatBooked) {
                clearAllButtonColors();
                // Gán lại giá trị null cho lastSelectedPosition
                lastSelectedPosition = null;
            }

            // Kiểm tra và thay đổi màu cho nút ghế của dòng mới được chọn
            checkAndChangeButtonColor(selectedDate, time, movieName);

            // Lưu trữ vị trí ghế của dòng mới được chọn để sử dụng cho lần chọn tiếp theo
            lastSelectedPosition = getLastSeatPosition(selectedDate, time, movieName);
        }
    }

    private boolean checkSeatBooked(String selectedDate, String time, String movieName) {
        try {
            // Đọc dữ liệu từ file bookings.csv
            BufferedReader reader = new BufferedReader(new FileReader("../Data/bookings.csv"));
            String line;
            while ((line = reader.readLine()) != null) {
                // Phân tích từng dòng để lấy ngày, giờ và tên phim
                String[] parts = line.split(",");
                String date = parts[0];
                String movie = parts[1];
                String screeningTime = parts[2];

                // Kiểm tra xem dữ liệu có khớp với ngày, giờ và tên phim đã chọn không
                if (date.equals(selectedDate) && movie.equals(movieName) && screeningTime.equals(time)) {
                    return true; // Đã có ghế nào được đặt ở dòng mới
                }
            }
            reader.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi đọc dữ liệu từ file bookings.csv: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
        return false; // Không có ghế nào được đặt ở dòng mới
    }

    // Hàm để lấy vị trí ghế cuối cùng từ file bookings.csv cho dòng được chọn
    private String getLastSeatPosition(String selectedDate, String time, String movieName) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("../Data/bookings.csv"));
            String line;
            String lastPosition = null;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                String date = parts[0];
                String movie = parts[1];
                String screeningTime = parts[2];
                String position = parts[5]; // Vị trí ghế

                if (date.equals(selectedDate) && movie.equals(movieName) && screeningTime.equals(time)) {
                    lastPosition = position;
                }
            }
            reader.close();
            return lastPosition;
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi đọc dữ liệu từ file bookings.csv: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    private void checkAndChangeButtonColor(String selectedDate, String time, String movieName) {
        resetAllButtonColors(); // Đặt lại màu sắc của tất cả các nút

        try {
            // Đọc dữ liệu từ file bookings.csv
            BufferedReader reader = new BufferedReader(new FileReader("../Data/bookings.csv"));
            String line;
            String lastPosition = null;
            while ((line = reader.readLine()) != null) {
                // Phân tích từng dòng để lấy ngày, giờ và tên phim
                String[] parts = line.split(",");
                String date = parts[0];
                String movie = parts[1];
                String screeningTime = parts[2];
                String position = parts[5]; // Vị trí ghế

                // Kiểm tra xem dữ liệu có khớp với ngày, giờ và tên phim đã chọn không
                if (date.equals(selectedDate) && movie.equals(movieName) && screeningTime.equals(time)) {
                    lastPosition = position; // Lưu lại vị trí cuối cùng nếu có dữ liệu khớp
                    changeButtonColor(position); // Đổi màu nút tương ứng
                }
            }
            reader.close();

            // Nếu có dữ liệu khớp, đổi màu cho nút tương ứng trong jPanel11
            if (lastPosition != null) {
                changeButtonColor(lastPosition);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi đọc dữ liệu từ file bookings.csv: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void changeButtonColor(String position) {
        JButton button = getButtonByPosition(position);
        if (button != null) {
            button.setBackground(Color.RED); // Đổi màu nút thành màu đỏ
        }
    }

    private JButton getButtonByPosition(String position) {
        // Tách phần chữ cái và phần số từ vị trí ghế
        char rowChar = position.charAt(0);
        int colNum = Integer.parseInt(position.substring(1));

        // Chuyển đổi từ chữ cái thành số thứ tự hàng (0-5)
        int row = rowChar - 'A';

        // Tìm nút tương ứng trong jPanel11
        String buttonName = getButtonNameByPosition(row, colNum - 1);
        for (Component comp : jPanel11.getComponents()) {
            if (comp instanceof JButton) {
                JButton button = (JButton) comp;
                if (button.getText().equals(buttonName)) {
                    return button;
                }
            }
        }
        return null;
    }

    private String getButtonPosition(ActionEvent evt) {
        JButton button = (JButton) evt.getSource(); // Lấy nút được nhấn từ sự kiện
        String buttonName = button.getText(); // Lấy tên của nút
        return buttonName; // Trả về tên của nút
    }

    private void resetAllButtonColors() {
        Color grayColor = new Color(204, 204, 204); // Tạo màu xám
        for (Component comp : jPanel11.getComponents()) {
            if (comp instanceof JButton) {
                JButton button = (JButton) comp;
                button.setBackground(grayColor); // Đặt lại màu xám cho tất cả các nút
            }
        }
        
        btnBook.setBackground(Color.decode("#CCFFFF"));
    }

    public void start() {
        setVisible(true);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel8 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        txtIP = new javax.swing.JTextField();
        txtPort = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        btnConnect = new javax.swing.JButton();
        btnDisconnect = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jPanel10 = new javax.swing.JPanel();
        selectDate = new javax.swing.JComboBox<>();
        jLabel8 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblListMovie = new javax.swing.JTable();
        jPanel11 = new javax.swing.JPanel();
        jPanel12 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jPanel13 = new javax.swing.JPanel();
        jPanel14 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        btnBook = new javax.swing.JButton();
        A9 = new javax.swing.JButton();
        B9 = new javax.swing.JButton();
        C9 = new javax.swing.JButton();
        D9 = new javax.swing.JButton();
        E9 = new javax.swing.JButton();
        E8 = new javax.swing.JButton();
        D8 = new javax.swing.JButton();
        C8 = new javax.swing.JButton();
        B8 = new javax.swing.JButton();
        A8 = new javax.swing.JButton();
        A7 = new javax.swing.JButton();
        B7 = new javax.swing.JButton();
        C7 = new javax.swing.JButton();
        D7 = new javax.swing.JButton();
        E7 = new javax.swing.JButton();
        E6 = new javax.swing.JButton();
        D6 = new javax.swing.JButton();
        C6 = new javax.swing.JButton();
        B6 = new javax.swing.JButton();
        A6 = new javax.swing.JButton();
        A5 = new javax.swing.JButton();
        B5 = new javax.swing.JButton();
        C5 = new javax.swing.JButton();
        D5 = new javax.swing.JButton();
        E5 = new javax.swing.JButton();
        E4 = new javax.swing.JButton();
        D4 = new javax.swing.JButton();
        C4 = new javax.swing.JButton();
        B4 = new javax.swing.JButton();
        A4 = new javax.swing.JButton();
        E3 = new javax.swing.JButton();
        D3 = new javax.swing.JButton();
        C3 = new javax.swing.JButton();
        B3 = new javax.swing.JButton();
        A3 = new javax.swing.JButton();
        A2 = new javax.swing.JButton();
        B2 = new javax.swing.JButton();
        C2 = new javax.swing.JButton();
        D2 = new javax.swing.JButton();
        E2 = new javax.swing.JButton();
        E1 = new javax.swing.JButton();
        D1 = new javax.swing.JButton();
        C1 = new javax.swing.JButton();
        B1 = new javax.swing.JButton();
        A1 = new javax.swing.JButton();
        A10 = new javax.swing.JButton();
        B10 = new javax.swing.JButton();
        C10 = new javax.swing.JButton();
        D10 = new javax.swing.JButton();
        E10 = new javax.swing.JButton();
        F4 = new javax.swing.JButton();
        F1 = new javax.swing.JButton();
        F2 = new javax.swing.JButton();
        F3 = new javax.swing.JButton();
        F10 = new javax.swing.JButton();
        F9 = new javax.swing.JButton();
        F8 = new javax.swing.JButton();
        F7 = new javax.swing.JButton();
        F6 = new javax.swing.JButton();
        F5 = new javax.swing.JButton();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        txtPrice = new javax.swing.JTextField();

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 3, 30)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 0));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Client");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        txtIP.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N

        txtPort.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel5.setText("Nhập IP Server:");

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel6.setText("Nhập Port:");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(txtPort, javax.swing.GroupLayout.DEFAULT_SIZE, 272, Short.MAX_VALUE)
                    .addComponent(txtIP))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(13, 13, 13)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(txtIP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(txtPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 0, 51));
        jLabel7.setText("Chưa kết nối");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(101, 101, 101)
                .addComponent(jLabel7)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel4.setText("Trạng thái");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel3.setText("Kết nối đến Server");

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel9Layout.createSequentialGroup()
                .addGap(147, 147, 147)
                .addComponent(jLabel3)
                .addGap(391, 391, 391)
                .addComponent(jLabel4)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel9Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jLabel3))
                .addContainerGap())
        );

        btnConnect.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        btnConnect.setText("Kết nối");
        btnConnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConnectActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnConnect, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnConnect, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        btnDisconnect.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        btnDisconnect.setText("Ngắt kết nối");
        btnDisconnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDisconnectActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnDisconnect, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(41, 41, 41)
                .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(15, 15, 15)
                                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(10, 10, 10)
                                .addComponent(btnDisconnect, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 4, Short.MAX_VALUE)))
                .addContainerGap())
        );

        jPanel3.setPreferredSize(new java.awt.Dimension(886, 1));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jPanel10.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        selectDate.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        selectDate.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "1/6/2024", "2/6/2024", "3/6/2024", "4/6/2024", "5/6/2024", "6/6/2024", "7/6/2024" }));
        jPanel10.add(selectDate, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 50, 118, -1));

        jLabel8.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel8.setText("Chọn ngày:");
        jPanel10.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 50, -1, 30));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Lịch chiếu phim");
        jPanel10.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 469, 34));

        tblListMovie.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        tblListMovie.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "STT", "Suất chiếu", "Phim"
            }
        ));
        jScrollPane2.setViewportView(tblListMovie);

        jPanel10.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 110, 429, 180));

        jPanel11.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel9.setText("Màn hình");

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 819, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 11, Short.MAX_VALUE))
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        jPanel11.add(jPanel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(1, 4, 830, -1));

        jPanel13.setBackground(new java.awt.Color(255, 0, 51));
        jPanel13.setPreferredSize(new java.awt.Dimension(26, 26));

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jPanel11.add(jPanel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 370, -1, -1));

        jPanel14.setBackground(new java.awt.Color(204, 204, 204));
        jPanel14.setPreferredSize(new java.awt.Dimension(26, 26));

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jPanel11.add(jPanel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 370, -1, -1));

        jLabel10.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel10.setText("Đã đặt");
        jPanel11.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 370, -1, 26));

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel11.setText("Khu 4");
        jPanel11.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 200, -1, 26));

        btnBook.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        btnBook.setText("Đặt vé");
        btnBook.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBookActionPerformed(evt);
            }
        });
        jPanel11.add(btnBook, new org.netbeans.lib.awtextra.AbsoluteConstraints(600, 360, 150, 38));

        A9.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        A9.setText("A9");
        jPanel11.add(A9, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 70, 59, 35));

        B9.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        B9.setText("B9");
        jPanel11.add(B9, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 110, 59, 35));

        C9.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        C9.setText("C9");
        jPanel11.add(C9, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 150, 59, 35));

        D9.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        D9.setText("D9");
        jPanel11.add(D9, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 230, 59, 35));

        E9.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        E9.setText("E9");
        E9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                E9ActionPerformed(evt);
            }
        });
        jPanel11.add(E9, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 270, 59, 35));

        E8.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        E8.setText("E8");
        jPanel11.add(E8, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 270, 59, 35));

        D8.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        D8.setText("D8");
        jPanel11.add(D8, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 230, 59, 35));

        C8.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        C8.setText("C8");
        jPanel11.add(C8, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 150, 59, 35));

        B8.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        B8.setText("B8");
        jPanel11.add(B8, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 110, 59, 35));

        A8.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        A8.setText("A8");
        A8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                A8ActionPerformed(evt);
            }
        });
        jPanel11.add(A8, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 70, 59, 35));

        A7.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        A7.setText("A7");
        jPanel11.add(A7, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 70, 59, 35));

        B7.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        B7.setText("B7");
        jPanel11.add(B7, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 110, 59, 35));

        C7.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        C7.setText("C7");
        jPanel11.add(C7, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 150, 59, 35));

        D7.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        D7.setText("D7");
        jPanel11.add(D7, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 230, 59, 35));

        E7.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        E7.setText("E7");
        jPanel11.add(E7, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 270, 59, 35));

        E6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        E6.setText("E6");
        jPanel11.add(E6, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 270, 59, 35));

        D6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        D6.setText("D6");
        jPanel11.add(D6, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 230, 59, 35));

        C6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        C6.setText("C6");
        jPanel11.add(C6, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 150, 59, 35));

        B6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        B6.setText("B6");
        jPanel11.add(B6, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 110, 59, 35));

        A6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        A6.setText("A6");
        jPanel11.add(A6, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 70, 59, 35));

        A5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        A5.setText("A5");
        jPanel11.add(A5, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 70, 59, 35));

        B5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        B5.setText("B5");
        jPanel11.add(B5, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 110, 59, 35));

        C5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        C5.setText("C5");
        jPanel11.add(C5, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 150, 59, 35));

        D5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        D5.setText("D5");
        jPanel11.add(D5, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 230, 59, 35));

        E5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        E5.setText("E5");
        jPanel11.add(E5, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 270, 59, 35));

        E4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        E4.setText("E4");
        jPanel11.add(E4, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 270, 59, 35));

        D4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        D4.setText("D4");
        jPanel11.add(D4, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 230, 59, 35));

        C4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        C4.setText("C4");
        jPanel11.add(C4, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 150, 59, 35));

        B4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        B4.setText("B4");
        jPanel11.add(B4, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 110, 59, 35));

        A4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        A4.setText("A4");
        jPanel11.add(A4, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 70, 59, 35));

        E3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        E3.setText("E3");
        jPanel11.add(E3, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 270, 59, 35));

        D3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        D3.setText("D3");
        jPanel11.add(D3, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 230, 59, 35));

        C3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        C3.setText("C3");
        jPanel11.add(C3, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 150, 59, 35));

        B3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        B3.setText("B3");
        jPanel11.add(B3, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 110, 59, 35));

        A3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        A3.setText("A3");
        jPanel11.add(A3, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 70, 59, 35));

        A2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        A2.setText("A2");
        jPanel11.add(A2, new org.netbeans.lib.awtextra.AbsoluteConstraints(620, 70, 59, 35));

        B2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        B2.setText("B2");
        jPanel11.add(B2, new org.netbeans.lib.awtextra.AbsoluteConstraints(620, 110, 59, 35));

        C2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        C2.setText("C2");
        jPanel11.add(C2, new org.netbeans.lib.awtextra.AbsoluteConstraints(620, 150, 59, 35));

        D2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        D2.setText("D2");
        jPanel11.add(D2, new org.netbeans.lib.awtextra.AbsoluteConstraints(620, 230, 59, 35));

        E2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        E2.setText("E2");
        jPanel11.add(E2, new org.netbeans.lib.awtextra.AbsoluteConstraints(620, 270, 59, 35));

        E1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        E1.setText("E1");
        jPanel11.add(E1, new org.netbeans.lib.awtextra.AbsoluteConstraints(690, 270, 59, 35));

        D1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        D1.setText("D1");
        jPanel11.add(D1, new org.netbeans.lib.awtextra.AbsoluteConstraints(690, 230, 59, 35));

        C1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        C1.setText("C1");
        jPanel11.add(C1, new org.netbeans.lib.awtextra.AbsoluteConstraints(690, 150, 59, 35));

        B1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        B1.setText("B1");
        jPanel11.add(B1, new org.netbeans.lib.awtextra.AbsoluteConstraints(690, 110, 59, 35));

        A1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        A1.setText("A1");
        jPanel11.add(A1, new org.netbeans.lib.awtextra.AbsoluteConstraints(690, 70, 59, 35));

        A10.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        A10.setText("A10");
        jPanel11.add(A10, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 70, 59, 35));

        B10.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        B10.setText("B10");
        jPanel11.add(B10, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 110, 59, 35));

        C10.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        C10.setText("C10");
        jPanel11.add(C10, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 150, 59, 35));

        D10.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        D10.setText("D10");
        jPanel11.add(D10, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 230, 59, 35));

        E10.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        E10.setText("E10");
        jPanel11.add(E10, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 270, 59, 35));

        F4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        F4.setText("F4");
        F4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                F4ActionPerformed(evt);
            }
        });
        jPanel11.add(F4, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 310, 59, 35));

        F1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        F1.setText("F1");
        jPanel11.add(F1, new org.netbeans.lib.awtextra.AbsoluteConstraints(690, 310, 59, 35));

        F2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        F2.setText("F2");
        jPanel11.add(F2, new org.netbeans.lib.awtextra.AbsoluteConstraints(620, 310, 59, 35));

        F3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        F3.setText("F3");
        jPanel11.add(F3, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 310, 59, 35));

        F10.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        F10.setText("F10");
        F10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                F10ActionPerformed(evt);
            }
        });
        jPanel11.add(F10, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 310, 59, 35));

        F9.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        F9.setText("F9");
        jPanel11.add(F9, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 310, 59, 35));

        F8.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        F8.setText("F8");
        jPanel11.add(F8, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 310, 59, 35));

        F7.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        F7.setText("F7");
        jPanel11.add(F7, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 310, 59, 35));

        F6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        F6.setText("F6");
        jPanel11.add(F6, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 310, 59, 35));

        F5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        F5.setText("F5");
        jPanel11.add(F5, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 310, 59, 35));

        jLabel12.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel12.setText("Giá vé:");
        jPanel11.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 370, -1, 26));

        jLabel13.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel13.setText("Khu 1");
        jPanel11.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 40, -1, 26));

        jLabel14.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel14.setText("Khu 2");
        jPanel11.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 40, -1, 26));

        jLabel15.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel15.setText("Khu 3");
        jPanel11.add(jLabel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 200, -1, 26));

        jLabel16.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel16.setText("Còn trống");
        jPanel11.add(jLabel16, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 370, -1, 26));

        txtPrice.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jPanel11.add(txtPrice, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 370, 170, -1));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, 772, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, 423, Short.MAX_VALUE))
                .addContainerGap(31, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnConnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConnectActionPerformed
        // TODO add your handling code here:
        String serverIP = txtIP.getText();
        int serverPort = Integer.parseInt(txtPort.getText());
        try {
            socket = new Socket(serverIP, serverPort);
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());
            System.out.println("Connected to server");

            connected = true;

            // Lấy dữ liệu từ server
            getDataFromServer();

            JOptionPane.showMessageDialog(this, "Kết nối thành công!");
            jLabel7.setText("Kết nối thành công!");
            jLabel7.setForeground(Color.BLUE);

            // Thêm sự kiện listener cho selectDate
            selectDate.addActionListener(e -> {
                if (connected) {
                    updateEventList(events);
                } else {
                    JOptionPane.showMessageDialog(this, "Chưa kết nối đến server. Vui lòng kết nối trước.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                }
            });

            // Mặc định chọn ngày 1/6/2024 và cập nhật bảng sau khi kết nối thành công
            SwingUtilities.invokeLater(() -> {
                if (connected) {
                    selectDate.setSelectedItem("1/6/2024");
                    updateEventList(events);
                }
            });
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi kết nối đến máy chủ: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnConnectActionPerformed

    private void btnDisconnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDisconnectActionPerformed
        // TODO add your handling code here:
        try {
            if (socket != null) {
                socket.close();
                System.out.println("Disconnected from server");
                // Show success dialog
                JOptionPane.showMessageDialog(this, "Ngắt kết nối thành công!");
                // Update status text and color
                jLabel7.setText("Chưa kết nối");
                jLabel7.setForeground(Color.RED);

                // Đánh dấu trạng thái ngắt kết nối
                connected = false;

                // Xóa dữ liệu khỏi bảng
                DefaultTableModel model = (DefaultTableModel) tblListMovie.getModel();
                model.setRowCount(0);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            // Show error dialog
            JOptionPane.showMessageDialog(this, "Lỗi khi ngắt kết nối: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            // Update status text and color
            jLabel7.setText("Lỗi khi ngắt kết nối!");
            jLabel7.setForeground(Color.RED);
        }
    }//GEN-LAST:event_btnDisconnectActionPerformed

    private void btnBookActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBookActionPerformed
        // TODO add your handling code here:
        // Kiểm tra xem có dòng nào được chọn không
        int selectedRow = tblListMovie.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một phim từ bảng.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Lấy thông tin về phim đã chọn
        String date = (String) selectDate.getSelectedItem();
        String dateAndTime = (String) tblListMovie.getValueAt(selectedRow, 1);
        String movieName = (String) tblListMovie.getValueAt(selectedRow, 2);

        // Tạo cửa sổ Book và truyền thông tin về phim và vị trí nút đã chọn
        Book bookWindow = new Book(date, movieName, dateAndTime);
        bookWindow.setVisible(true);
    }//GEN-LAST:event_btnBookActionPerformed

    private void F10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_F10ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_F10ActionPerformed

    private void F4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_F4ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_F4ActionPerformed

    private void A8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_A8ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_A8ActionPerformed

    private void E9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_E9ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_E9ActionPerformed

    public static void main(String args[]) {

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Client().start();
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton A1;
    private javax.swing.JButton A10;
    private javax.swing.JButton A2;
    private javax.swing.JButton A3;
    private javax.swing.JButton A4;
    private javax.swing.JButton A5;
    private javax.swing.JButton A6;
    private javax.swing.JButton A7;
    private javax.swing.JButton A8;
    private javax.swing.JButton A9;
    private javax.swing.JButton B1;
    private javax.swing.JButton B10;
    private javax.swing.JButton B2;
    private javax.swing.JButton B3;
    private javax.swing.JButton B4;
    private javax.swing.JButton B5;
    private javax.swing.JButton B6;
    private javax.swing.JButton B7;
    private javax.swing.JButton B8;
    private javax.swing.JButton B9;
    private javax.swing.JButton C1;
    private javax.swing.JButton C10;
    private javax.swing.JButton C2;
    private javax.swing.JButton C3;
    private javax.swing.JButton C4;
    private javax.swing.JButton C5;
    private javax.swing.JButton C6;
    private javax.swing.JButton C7;
    private javax.swing.JButton C8;
    private javax.swing.JButton C9;
    private javax.swing.JButton D1;
    private javax.swing.JButton D10;
    private javax.swing.JButton D2;
    private javax.swing.JButton D3;
    private javax.swing.JButton D4;
    private javax.swing.JButton D5;
    private javax.swing.JButton D6;
    private javax.swing.JButton D7;
    private javax.swing.JButton D8;
    private javax.swing.JButton D9;
    private javax.swing.JButton E1;
    private javax.swing.JButton E10;
    private javax.swing.JButton E2;
    private javax.swing.JButton E3;
    private javax.swing.JButton E4;
    private javax.swing.JButton E5;
    private javax.swing.JButton E6;
    private javax.swing.JButton E7;
    private javax.swing.JButton E8;
    private javax.swing.JButton E9;
    private javax.swing.JButton F1;
    private javax.swing.JButton F10;
    private javax.swing.JButton F2;
    private javax.swing.JButton F3;
    private javax.swing.JButton F4;
    private javax.swing.JButton F5;
    private javax.swing.JButton F6;
    private javax.swing.JButton F7;
    private javax.swing.JButton F8;
    private javax.swing.JButton F9;
    private javax.swing.JButton btnBook;
    private javax.swing.JButton btnConnect;
    private javax.swing.JButton btnDisconnect;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JComboBox<String> selectDate;
    private javax.swing.JTable tblListMovie;
    private javax.swing.JTextField txtIP;
    private javax.swing.JTextField txtPort;
    private javax.swing.JTextField txtPrice;
    // End of variables declaration//GEN-END:variables
}
