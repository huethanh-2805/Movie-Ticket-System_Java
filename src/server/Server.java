package server;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

public class Server extends javax.swing.JFrame {

    private static List<Event> events = new ArrayList<>();
    private static final int PORT = 3000;
    String serverAddress = "127.0.0.1";
    private static ExecutorService threadPool = Executors.newFixedThreadPool(10);
    private Socket socket;
    private ClientHandler clientHandler;

    public Server() throws IOException {
        initComponents();
        loadEventsFromCSV("../Data/listMovie.csv");

        ViewProgram();

        try {
            Socket socket = new Socket(serverAddress, PORT);
            clientHandler = new ClientHandler(socket, events);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // Start server
        startServer();

        ViewTable();

        initializeButtonListeners();

        // Add date selection listener
        selectDate.addActionListener(e -> updateTableForSelectedDate());

        // Set default date to 1/6/2024 and update table
        SwingUtilities.invokeLater(() -> {
            selectDate.setSelectedItem("1/6/2024");
            updateTableForSelectedDate();
        });

        loadAndDisplayButtonStates();
        initializeTableListener();
    }

    //Đổi màu ghế đã đặt
    private void initializeTableListener() {
        tableMovies.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tableMovies.getSelectedRow();
                String selectedDate = (String) selectDate.getSelectedItem();
                String time = tableMovies.getValueAt(row, 1).toString(); // Lấy giờ từ dòng được chọn
                String movieName = tableMovies.getValueAt(row, 2).toString(); // Lấy tên phim từ dòng được chọn
                checkAndChangeButtonColor(selectedDate, time, movieName);
            }
        });
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

    private void resetAllButtonColors() {
        Color grayColor = new Color(204, 204, 204); // Tạo màu xám
        
        for (Component comp : jPanel4.getComponents()) {
            if (comp instanceof JButton) {
                JButton button = (JButton) comp;
                button.setBackground(grayColor); // Đặt lại màu xám cho tất cả các nút
            }
        }
        
        btnEdit.setBackground(Color.decode("#CCFFFF"));
        btnEditPrice.setBackground(Color.decode("#CCFFFF"));
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
        for (Component comp : jPanel4.getComponents()) {
            if (comp instanceof JButton) {
                JButton button = (JButton) comp;
                if (button.getText().equals(buttonName)) {
                    return button;
                }
            }
        }
        return null;
    }

    private void startServer() {
        new Thread(() -> {
            try ( ServerSocket serverSocket = new ServerSocket(PORT)) {
                System.out.println("Server is listening on port " + PORT);

                while (true) {
                    Socket socket = serverSocket.accept();
                    System.out.println("New client connected");
                    threadPool.execute(new ClientHandler(socket, events));
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    public void ViewProgram() {
        setLocationRelativeTo(null);
        getContentPane().setBackground(Color.decode("#EFF8FE"));

        this.jPanel1.setBackground(Color.decode("#2B3467"));
        this.jPanel2.setBackground(Color.decode("#EFF8FE"));
        this.jPanel3.setBackground(Color.decode("#FCFFE7"));
        this.jPanel4.setBackground(Color.decode("#FCFFE7"));

        Border border = BorderFactory.createLineBorder(Color.decode("#2B3467"), 2);
        jPanel3.setBorder(border);
        jPanel4.setBorder(border);
        jPanel5.setBackground(Color.LIGHT_GRAY);

        btnAddFilm.setBackground(Color.decode("#65B741"));
        btnAddFilm.setForeground(Color.WHITE);
        btnDeleteFilm.setBackground(Color.RED);
        btnDeleteFilm.setForeground(Color.WHITE);
        btnEditFilm.setBackground(Color.decode("#387ADF"));
        btnEditFilm.setForeground(Color.WHITE);

        Color grayColor = new Color(204, 204, 204); // Tạo màu xám

        for (Component comp : jPanel4.getComponents()) {
            if (comp instanceof JButton) {
                JButton button = (JButton) comp;
                button.setBackground(grayColor);
            }
        }

        btnEdit.setBackground(Color.decode("#CCFFFF"));
        btnEditPrice.setBackground(Color.decode("#CCFFFF"));
    }

    public void ViewTable() {
        // Customize table header
        JTableHeader header = tableMovies.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 16)); // Set font to bold
        header.setPreferredSize(new Dimension(100, 30)); // Increase header height

        // Set column width for STT
        TableColumn sttColumn = tableMovies.getColumnModel().getColumn(0);
        sttColumn.setPreferredWidth((int) (0.1 * 435)); // 20% tổng độ rộng

        TableColumn scColumn = tableMovies.getColumnModel().getColumn(1);
        scColumn.setPreferredWidth((int) (0.3 * 435)); // 50% tổng độ rộng

        TableColumn movieColumn = tableMovies.getColumnModel().getColumn(2);
        movieColumn.setPreferredWidth((int) (0.6 * 435)); // 30% tổng độ rộng

        // Set row height
        tableMovies.setRowHeight(25); // Increase row height
    }

    private void loadEventsFromCSV(String fileName) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy");
        events.clear(); //Xóa các dữ liệu đã có để load lại tránh trùng lặp
        try ( BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                Date date = dateFormat.parse(values[0]);
                String timeStart = values[1];
                String timeEnd = values[2];
                String movieName = values[3];

                Event event = null;
                for (Event e : events) {
                    if (e.getName().equals(movieName) && e.getDate().equals(date)) {
                        event = e;
                        break;
                    }
                }
                if (event == null) {
                    event = new Event(movieName, date);
                    events.add(event);
                }

                Show show = new Show(timeStart, timeEnd, 5, 5); // Example rows and cols
                event.addShow(show);
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    private void updateTableForSelectedDate() {
        String selectedDateStr = (String) selectDate.getSelectedItem();
        if (selectedDateStr == null) {
            return;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy");
        try {
            Date selectedDate = dateFormat.parse(selectedDateStr);
            DefaultTableModel tableModel = (DefaultTableModel) tableMovies.getModel();
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

    //Cấu hình ghế ngồi
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

    private void updateButtonStates(List<List<Boolean>> data) {
        for (int i = 0; i < data.size(); i++) {
            for (int j = 0; j < data.get(i).size(); j++) {
                boolean state = data.get(i).get(j);
                JButton button = getButtonByPosition(i, j);

                if (button != null) {
                    button.setVisible(state); // Hiển thị nút nếu state là true, ẩn nếu state là false
                } else {
                    System.out.println("Không tìm thấy nút: " + getButtonNameByPosition(i, j));
                }
            }
        }
    }

    private JButton getButtonByPosition(int row, int col) {
        String buttonName = getButtonNameByPosition(row, col);

        // Duyệt qua tất cả các thành phần trong Container để tìm JButton có tên tương ứng
        for (Component comp : jPanel4.getComponents()) {
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

    public void loadAndDisplayButtonStates() {
        try {
            List<List<Boolean>> data = readEditFile("../Data/editSeat.csv");
            updateButtonStates(data);
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi đọc file", "Thông báo", JOptionPane.ERROR_MESSAGE);
        }
    }

    //Hiển thị giá vé
    private void initializeButtonListeners() {
        for (Component comp : jPanel4.getComponents()) {
            if (comp instanceof JButton) {
                JButton button = (JButton) comp;
                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String buttonName = button.getText(); // Lấy tên nút, ví dụ: A10
                        int screening = getScreeningFromButtonName(buttonName);
                        updatePriceForScreening(screening);
                    }
                });
            }
        }
    }

    private int getScreeningFromButtonName(String buttonName) {
        // Ví dụ: nút A10 thuộc khu 1, nút B5 thuộc khu 2, tùy thuộc vào cách bạn đặt tên các nút
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
            List<Integer> prices = readPricesFromCSV("../Data/fare.csv");
            if (screening >= 1 && screening <= prices.size()) {
                int price = prices.get(screening - 1); // Chỉ mục bắt đầu từ 0
                txtPrice.setText(price + " đồng");
            } else {
                txtPrice.setText("Không tìm thấy giá vé cho khu vực này");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi đọc file giá vé", "Thông báo", JOptionPane.ERROR_MESSAGE);
        }
    }

    private List<Integer> readPricesFromCSV(String filePath) throws IOException {
        List<Integer> prices = new ArrayList<>();
        List<String> lines = Files.readAllLines(Paths.get(filePath));

        for (String line : lines) {
            prices.add(Integer.parseInt(line.trim()));
        }

        return prices;
    }

    public void start() {
        setVisible(true);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jPopupMenu1 = new javax.swing.JPopupMenu();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        ipInput = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        portInput = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        selectDate = new javax.swing.JComboBox<>();
        jLabel7 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tableMovies = new javax.swing.JTable();
        btnAddFilm = new javax.swing.JButton();
        btnDeleteFilm = new javax.swing.JButton();
        btnEditFilm = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        A9 = new javax.swing.JButton();
        A8 = new javax.swing.JButton();
        A7 = new javax.swing.JButton();
        A6 = new javax.swing.JButton();
        A5 = new javax.swing.JButton();
        A3 = new javax.swing.JButton();
        A4 = new javax.swing.JButton();
        A2 = new javax.swing.JButton();
        A1 = new javax.swing.JButton();
        B9 = new javax.swing.JButton();
        B8 = new javax.swing.JButton();
        B7 = new javax.swing.JButton();
        B6 = new javax.swing.JButton();
        B5 = new javax.swing.JButton();
        B4 = new javax.swing.JButton();
        B3 = new javax.swing.JButton();
        B2 = new javax.swing.JButton();
        B1 = new javax.swing.JButton();
        C9 = new javax.swing.JButton();
        C8 = new javax.swing.JButton();
        C7 = new javax.swing.JButton();
        C6 = new javax.swing.JButton();
        C5 = new javax.swing.JButton();
        C4 = new javax.swing.JButton();
        C3 = new javax.swing.JButton();
        C2 = new javax.swing.JButton();
        C1 = new javax.swing.JButton();
        D9 = new javax.swing.JButton();
        D8 = new javax.swing.JButton();
        D7 = new javax.swing.JButton();
        D6 = new javax.swing.JButton();
        D5 = new javax.swing.JButton();
        D4 = new javax.swing.JButton();
        D3 = new javax.swing.JButton();
        D2 = new javax.swing.JButton();
        D1 = new javax.swing.JButton();
        jPanel13 = new javax.swing.JPanel();
        jPanel14 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        btnEdit = new javax.swing.JButton();
        E9 = new javax.swing.JButton();
        E8 = new javax.swing.JButton();
        E7 = new javax.swing.JButton();
        E6 = new javax.swing.JButton();
        E5 = new javax.swing.JButton();
        E4 = new javax.swing.JButton();
        E3 = new javax.swing.JButton();
        E2 = new javax.swing.JButton();
        E1 = new javax.swing.JButton();
        F9 = new javax.swing.JButton();
        F8 = new javax.swing.JButton();
        F7 = new javax.swing.JButton();
        F6 = new javax.swing.JButton();
        F5 = new javax.swing.JButton();
        F4 = new javax.swing.JButton();
        F3 = new javax.swing.JButton();
        F2 = new javax.swing.JButton();
        F1 = new javax.swing.JButton();
        A10 = new javax.swing.JButton();
        B10 = new javax.swing.JButton();
        C10 = new javax.swing.JButton();
        D10 = new javax.swing.JButton();
        E10 = new javax.swing.JButton();
        F10 = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        txtPrice = new javax.swing.JTextField();
        btnEditPrice = new javax.swing.JButton();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();

        jMenuItem1.setText("jMenuItem1");

        jMenuItem2.setText("jMenuItem2");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Segoe UI", 3, 30)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 0));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Server");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 1282, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1282, -1));

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel5.setText("IP:");

        ipInput.setEditable(false);
        ipInput.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        ipInput.setText("127.0.0.1");
        ipInput.setFocusable(false);

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel6.setText("Port:");

        portInput.setEditable(false);
        portInput.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        portInput.setText("3000");
        portInput.setFocusable(false);
        portInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                portInputActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ipInput, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(54, 54, 54)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(portInput, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(894, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(ipInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(portInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(11, Short.MAX_VALUE))
        );

        getContentPane().add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 57, 1282, -1));

        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        selectDate.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        selectDate.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "1/6/2024", "2/6/2024", "3/6/2024", "4/6/2024", "5/6/2024", "6/6/2024", "7/6/2024" }));
        jPanel3.add(selectDate, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 50, 118, -1));

        jLabel7.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel7.setText("Chọn ngày:");
        jPanel3.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 50, -1, 30));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Lịch chiếu phim");
        jPanel3.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 475, 34));

        tableMovies.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        tableMovies.setModel(new javax.swing.table.DefaultTableModel(
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
        tableMovies.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableMoviesMouseClicked(evt);
            }
        });
        tableMovies.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tableMoviesKeyReleased(evt);
            }
        });
        jScrollPane1.setViewportView(tableMovies);

        jPanel3.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 110, 435, 202));

        btnAddFilm.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        btnAddFilm.setText("Thêm");
        btnAddFilm.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddFilmActionPerformed(evt);
            }
        });
        jPanel3.add(btnAddFilm, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 350, 100, 35));

        btnDeleteFilm.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        btnDeleteFilm.setText("Xóa");
        btnDeleteFilm.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteFilmActionPerformed(evt);
            }
        });
        jPanel3.add(btnDeleteFilm, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 350, 100, 35));

        btnEditFilm.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        btnEditFilm.setText("Sửa");
        btnEditFilm.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditFilmActionPerformed(evt);
            }
        });
        jPanel3.add(btnEditFilm, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 350, 100, 35));

        getContentPane().add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 108, -1, 490));

        jPanel4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("Màn hình");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        jPanel4.add(jPanel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 767, -1));

        A9.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        A9.setText("A9");
        jPanel4.add(A9, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 72, 59, 38));

        A8.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        A8.setText("A8");
        jPanel4.add(A8, new org.netbeans.lib.awtextra.AbsoluteConstraints(161, 72, 59, 38));

        A7.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        A7.setText("A7");
        jPanel4.add(A7, new org.netbeans.lib.awtextra.AbsoluteConstraints(232, 72, 59, 38));

        A6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        A6.setText("A6");
        jPanel4.add(A6, new org.netbeans.lib.awtextra.AbsoluteConstraints(303, 72, 59, 38));

        A5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        A5.setText("A5");
        jPanel4.add(A5, new org.netbeans.lib.awtextra.AbsoluteConstraints(405, 72, 59, 38));

        A3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        A3.setText("A3");
        jPanel4.add(A3, new org.netbeans.lib.awtextra.AbsoluteConstraints(547, 72, 59, 38));

        A4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        A4.setText("A4");
        jPanel4.add(A4, new org.netbeans.lib.awtextra.AbsoluteConstraints(476, 72, 59, 38));

        A2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        A2.setText("A2");
        jPanel4.add(A2, new org.netbeans.lib.awtextra.AbsoluteConstraints(618, 72, 59, 38));

        A1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        A1.setText("A1");
        jPanel4.add(A1, new org.netbeans.lib.awtextra.AbsoluteConstraints(683, 72, 59, 38));

        B9.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        B9.setText("B9");
        jPanel4.add(B9, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 122, 59, 38));

        B8.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        B8.setText("B8");
        jPanel4.add(B8, new org.netbeans.lib.awtextra.AbsoluteConstraints(161, 122, 59, 38));

        B7.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        B7.setText("B7");
        jPanel4.add(B7, new org.netbeans.lib.awtextra.AbsoluteConstraints(232, 122, 59, 38));

        B6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        B6.setText("B6");
        jPanel4.add(B6, new org.netbeans.lib.awtextra.AbsoluteConstraints(303, 122, 59, 38));

        B5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        B5.setText("B5");
        jPanel4.add(B5, new org.netbeans.lib.awtextra.AbsoluteConstraints(405, 122, 59, 38));

        B4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        B4.setText("B4");
        jPanel4.add(B4, new org.netbeans.lib.awtextra.AbsoluteConstraints(476, 122, 59, 38));

        B3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        B3.setText("B3");
        jPanel4.add(B3, new org.netbeans.lib.awtextra.AbsoluteConstraints(547, 122, 59, 38));

        B2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        B2.setText("B2");
        jPanel4.add(B2, new org.netbeans.lib.awtextra.AbsoluteConstraints(618, 122, 59, 38));

        B1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        B1.setText("B1");
        jPanel4.add(B1, new org.netbeans.lib.awtextra.AbsoluteConstraints(683, 122, 59, 38));

        C9.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        C9.setText("C9");
        jPanel4.add(C9, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 172, 59, 38));

        C8.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        C8.setText("C8");
        jPanel4.add(C8, new org.netbeans.lib.awtextra.AbsoluteConstraints(161, 172, 59, 38));

        C7.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        C7.setText("C7");
        jPanel4.add(C7, new org.netbeans.lib.awtextra.AbsoluteConstraints(232, 172, 59, 38));

        C6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        C6.setText("C6");
        jPanel4.add(C6, new org.netbeans.lib.awtextra.AbsoluteConstraints(303, 172, 59, 38));

        C5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        C5.setText("C5");
        jPanel4.add(C5, new org.netbeans.lib.awtextra.AbsoluteConstraints(405, 172, 59, 38));

        C4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        C4.setText("C4");
        jPanel4.add(C4, new org.netbeans.lib.awtextra.AbsoluteConstraints(476, 172, 59, 38));

        C3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        C3.setText("C3");
        jPanel4.add(C3, new org.netbeans.lib.awtextra.AbsoluteConstraints(547, 172, 59, 38));

        C2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        C2.setText("C2");
        jPanel4.add(C2, new org.netbeans.lib.awtextra.AbsoluteConstraints(618, 172, 59, 38));

        C1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        C1.setText("C1");
        jPanel4.add(C1, new org.netbeans.lib.awtextra.AbsoluteConstraints(683, 172, 59, 38));

        D9.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        D9.setText("D9");
        jPanel4.add(D9, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 250, 59, 38));

        D8.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        D8.setText("D8");
        jPanel4.add(D8, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 250, 59, 38));

        D7.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        D7.setText("D7");
        jPanel4.add(D7, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 250, 59, 38));

        D6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        D6.setText("D6");
        jPanel4.add(D6, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 250, 59, 38));

        D5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        D5.setText("D5");
        jPanel4.add(D5, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 250, 59, 38));

        D4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        D4.setText("D4");
        jPanel4.add(D4, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 250, 59, 38));

        D3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        D3.setText("D3");
        jPanel4.add(D3, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 250, 59, 38));

        D2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        D2.setText("D2");
        jPanel4.add(D2, new org.netbeans.lib.awtextra.AbsoluteConstraints(620, 250, 59, 38));

        D1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        D1.setText("D1");
        jPanel4.add(D1, new org.netbeans.lib.awtextra.AbsoluteConstraints(680, 250, 59, 38));

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
            .addGap(0, 26, Short.MAX_VALUE)
        );

        jPanel4.add(jPanel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 410, -1, -1));

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
            .addGap(0, 26, Short.MAX_VALUE)
        );

        jPanel4.add(jPanel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 410, -1, -1));

        jLabel10.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel10.setText("Đã đặt");
        jPanel4.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 410, -1, 26));

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel11.setText("Khu 4");
        jPanel4.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 220, -1, 26));

        btnEdit.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        btnEdit.setText("Cấu hình sân khấu");
        btnEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditActionPerformed(evt);
            }
        });
        jPanel4.add(btnEdit, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 400, 180, 35));

        E9.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        E9.setText("E9");
        jPanel4.add(E9, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 300, 59, 38));

        E8.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        E8.setText("E8");
        jPanel4.add(E8, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 300, 59, 38));

        E7.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        E7.setText("E7");
        jPanel4.add(E7, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 300, 59, 38));

        E6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        E6.setText("E6");
        jPanel4.add(E6, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 300, 59, 38));

        E5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        E5.setText("E5");
        jPanel4.add(E5, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 300, 59, 38));

        E4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        E4.setText("E4");
        jPanel4.add(E4, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 300, 59, 38));

        E3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        E3.setText("E3");
        jPanel4.add(E3, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 300, 59, 38));

        E2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        E2.setText("E2");
        jPanel4.add(E2, new org.netbeans.lib.awtextra.AbsoluteConstraints(620, 300, 59, 38));

        E1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        E1.setText("E1");
        jPanel4.add(E1, new org.netbeans.lib.awtextra.AbsoluteConstraints(680, 300, 59, 38));

        F9.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        F9.setText("F9");
        jPanel4.add(F9, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 350, 59, 38));

        F8.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        F8.setText("F8");
        jPanel4.add(F8, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 350, 59, 38));

        F7.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        F7.setText("F7");
        jPanel4.add(F7, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 350, 59, 38));

        F6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        F6.setText("F6");
        jPanel4.add(F6, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 350, 59, 38));

        F5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        F5.setText("F5");
        jPanel4.add(F5, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 350, 59, 38));

        F4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        F4.setText("F4");
        jPanel4.add(F4, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 350, 59, 38));

        F3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        F3.setText("F3");
        jPanel4.add(F3, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 350, 59, 38));

        F2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        F2.setText("F2");
        jPanel4.add(F2, new org.netbeans.lib.awtextra.AbsoluteConstraints(620, 350, 59, 38));

        F1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        F1.setText("F1");
        jPanel4.add(F1, new org.netbeans.lib.awtextra.AbsoluteConstraints(680, 350, 59, 38));

        A10.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        A10.setText("A10");
        jPanel4.add(A10, new org.netbeans.lib.awtextra.AbsoluteConstraints(19, 72, 59, 38));

        B10.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        B10.setText("B10");
        jPanel4.add(B10, new org.netbeans.lib.awtextra.AbsoluteConstraints(19, 122, 59, 38));

        C10.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        C10.setText("C10");
        jPanel4.add(C10, new org.netbeans.lib.awtextra.AbsoluteConstraints(19, 172, 59, 38));

        D10.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        D10.setText("D10");
        jPanel4.add(D10, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 250, 59, 38));

        E10.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        E10.setText("E10");
        jPanel4.add(E10, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 300, 59, 38));

        F10.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        F10.setText("F10");
        jPanel4.add(F10, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 350, 59, 38));

        jLabel9.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel9.setText("Số ghế/hàng:");
        jPanel4.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel4.setText("Giá vé:");
        jPanel4.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 440, -1, 30));

        txtPrice.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jPanel4.add(txtPrice, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 440, 170, -1));

        btnEditPrice.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        btnEditPrice.setText("Cấu hình giá vé");
        btnEditPrice.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditPriceActionPerformed(evt);
            }
        });
        jPanel4.add(btnEditPrice, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 440, 180, 35));

        jLabel12.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel12.setText("Còn trống");
        jPanel4.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 410, -1, 26));

        jLabel13.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel13.setText("Khu 1");
        jPanel4.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 40, -1, 26));

        jLabel14.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel14.setText("Khu 2");
        jPanel4.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 40, -1, 26));

        jLabel15.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel15.setText("Khu 3");
        jPanel4.add(jLabel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 220, -1, 26));

        getContentPane().add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(493, 108, -1, 490));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void portInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_portInputActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_portInputActionPerformed

    private void btnAddFilmActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddFilmActionPerformed
        // TODO add your handling code here:
        AddEvent addFrame = new AddEvent();
        addFrame.setVisible(true);
        // Thêm WindowListener để lắng nghe sự kiện đóng cửa sổ AddEvent
        addFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                loadEventsFromCSV("../Data/listMovie.csv");
                updateTableForSelectedDate();
            }
        });
    }//GEN-LAST:event_btnAddFilmActionPerformed

    private void btnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditActionPerformed
        // TODO add your handling code here:
        EditSeats editSeatFrame = new EditSeats();
        editSeatFrame.setVisible(true);

        editSeatFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                try {
                    // Đọc lại file editSeat.csv
                    List<List<Boolean>> newData = readEditFile("../Data/editSeat.csv");

                    // Cập nhật trạng thái hiển thị của các nút
                    updateButtonStates(newData);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Lỗi khi đọc file", "Thông báo", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }//GEN-LAST:event_btnEditActionPerformed

    private void btnDeleteFilmActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteFilmActionPerformed
        // TODO add your handling code here:
        int selectedRow = tableMovies.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một dòng để xóa", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        DefaultTableModel model = (DefaultTableModel) tableMovies.getModel();
        String movieTime = (String) model.getValueAt(selectedRow, 1);
        // Tách chuỗi thời gian thành thời gian bắt đầu và kết thúc
        String[] timeParts = movieTime.split(" - ");
        String startTime = timeParts[0]; // Đảm bảo rằng có dấu hai chấm ở đầu
        String endTime = timeParts[1]; // Đảm bảo rằng có dấu hai chấm ở đầu
        String movieName = (String) model.getValueAt(selectedRow, 2); // Lấy tên phim từ cột thứ 2
        String selectedDateStr = (String) selectDate.getSelectedItem();

        try {
            // Xóa dữ liệu từ file listMovie.csv
            List<String> lines = Files.readAllLines(Paths.get("../Data/listMovie.csv"));
            List<String> updatedLines = new ArrayList<>();

            boolean deleted = false; // Biến để xác định xem đã xóa thành công hay không

            for (String line : lines) {
                String[] values = line.split(",");
                String date = values[0];
                String startTimeInFile = values[1];
                String endTimeInFile = values[2];
                String nameFilmInFile = values[3];

                // So sánh ngày và tên phim để xác định dữ liệu cần xóa
                if (date.equals(selectedDateStr) && startTimeInFile.equals(startTime) && endTimeInFile.equals(endTime) && nameFilmInFile.equals(movieName)) {
                    deleted = true; // Đã xóa thành công
                    continue; // Bỏ qua dòng cần xóa
                }

                updatedLines.add(line);
            }

            Files.write(Paths.get("../Data/listMovie.csv"), updatedLines);

            if (deleted) {
                JOptionPane.showMessageDialog(this, "Đã xóa thành công", "Thông báo", JOptionPane.INFORMATION_MESSAGE);

                loadEventsFromCSV("../Data/listMovie.csv");
                updateTableForSelectedDate();
            } else {
                JOptionPane.showMessageDialog(this, "Không tìm thấy dữ liệu cần xóa", "Thông báo", JOptionPane.WARNING_MESSAGE);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }//GEN-LAST:event_btnDeleteFilmActionPerformed

    private void btnEditFilmActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditFilmActionPerformed
        // TODO add your handling code here:
        int selectedRow = tableMovies.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một dòng để sửa", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        DefaultTableModel model = (DefaultTableModel) tableMovies.getModel();
        String selectedDate = (String) selectDate.getSelectedItem();
        String movieTime = (String) model.getValueAt(selectedRow, 1);
        String movieName = (String) model.getValueAt(selectedRow, 2);

        // Tạo một instance của EditEvent và truyền các giá trị cần thiết
        EditEvent editEventFrame = new EditEvent(selectedDate, movieTime, movieName);
        editEventFrame.setVisible(true);

        editEventFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                loadEventsFromCSV("../Data/listMovie.csv");
                updateTableForSelectedDate();
            }
        });
    }//GEN-LAST:event_btnEditFilmActionPerformed

    private void tableMoviesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableMoviesMouseClicked
        // TODO add your handling code here:

    }//GEN-LAST:event_tableMoviesMouseClicked

    private void tableMoviesKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tableMoviesKeyReleased
        // TODO add your handling code here:

    }//GEN-LAST:event_tableMoviesKeyReleased

    private void btnEditPriceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditPriceActionPerformed
        // TODO add your handling code here:
        EditPrice editPriceFrame = new EditPrice();
        editPriceFrame.setVisible(true);
    }//GEN-LAST:event_btnEditPriceActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try ( ServerSocket serverSocket = new ServerSocket(12345)) {
            System.out.println("Server started");

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Client connected: " + socket.getInetAddress());

                ClientHandler clientHandler = new ClientHandler(socket);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
    private javax.swing.JButton btnAddFilm;
    private javax.swing.JButton btnDeleteFilm;
    private javax.swing.JButton btnEdit;
    private javax.swing.JButton btnEditFilm;
    private javax.swing.JButton btnEditPrice;
    private javax.swing.JTextField ipInput;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField portInput;
    private javax.swing.JComboBox<String> selectDate;
    private javax.swing.JTable tableMovies;
    private javax.swing.JTextField txtPrice;
    // End of variables declaration//GEN-END:variables
}
