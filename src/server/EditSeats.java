package server;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

public class EditSeats extends javax.swing.JFrame {

    public EditSeats() {
        initComponents();
        System.out.println("EditSeats frame initialized.");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    // Đọc dữ liệu từ file CSV
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

    // Ghi dữ liệu vào file CSV
    private void writeEditFile(String filePath, List<List<Boolean>> data) throws IOException {
        try ( BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (List<Boolean> row : data) {
                StringBuilder line = new StringBuilder();
                for (int i = 0; i < row.size(); i++) {
                    line.append(row.get(i));
                    if (i < row.size() - 1) {
                        line.append(",");
                    }
                }
                writer.write(line.toString());
                writer.newLine();
            }
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        selectKhanDai = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        selectHang = new javax.swing.JComboBox<>();
        selectGhe = new javax.swing.JComboBox<>();
        btnSave = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel7.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel7.setText("Số khu:");

        jLabel8.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel8.setText("Số hàng/khu:");

        jLabel9.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel9.setText("Số ghế/hàng:");

        selectKhanDai.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        selectKhanDai.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "4", "3", "2" }));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Cấu hình sân khấu");

        selectHang.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        selectHang.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "3", "2" }));

        selectGhe.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        selectGhe.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "5", "4" }));

        btnSave.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        btnSave.setText("Lưu");
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(43, 43, 43)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addComponent(jLabel8)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 82, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(selectHang, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(selectKhanDai, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(selectGhe, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(62, 62, 62))
            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(132, 132, 132)
                .addComponent(btnSave, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(148, 148, 148))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(31, 31, 31)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(selectKhanDai, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(selectHang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(selectGhe, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(35, 35, 35)
                .addComponent(btnSave, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(51, 51, 51))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        // TODO add your handling code here:
        // Lấy giá trị từ các trường selectKhanDai, selectHang và selectGhe
        int numKhanDai = Integer.parseInt(selectKhanDai.getSelectedItem().toString());
        int numHang = Integer.parseInt(selectHang.getSelectedItem().toString());
        int numGhe = Integer.parseInt(selectGhe.getSelectedItem().toString());

        // Đọc dữ liệu từ file CSV
        List<List<Boolean>> data = null;
        try {
            data = readEditFile("../Data/editSeat.csv");
        } catch (IOException ex) {
            Logger.getLogger(EditSeats.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Set tất cả các giá trị về true
        for (List<Boolean> row : data) {
            Collections.fill(row, true);
        }

        // Kiểm tra điều kiện để sửa file CSV
        if (numKhanDai == 4) {
            if (numHang == 3 && numGhe == 4) {
                for (int i = 0; i < 6; i++) {
                    data.get(i).set(4, false);
                    data.get(i).set(9, false);
                }
            }

            if (numHang == 2) {
                if (numGhe == 5) {
                    for (int i = 0; i < 10; i++) {
                        data.get(2).set(i, false);
                        data.get(5).set(i, false);
                    }
                } else {
                    for (int i = 0; i < 10; i++) {
                        data.get(2).set(i, false);
                        data.get(5).set(i, false);
                    }
                    for (int i = 0; i < 6; i++) {
                        data.get(i).set(4, false);
                        data.get(i).set(9, false);
                    }
                }
            }
        } else if (numKhanDai == 3) {
            for (int i = 3; i < 6; i++) {
                for (int j = 5; j < 10; j++) {
                    data.get(i).set(j, false);
                }
            }
            
            if (numHang == 3 && numGhe == 4) {
                for (int i = 0; i < 6; i++) {
                    data.get(i).set(4, false);
                    data.get(i).set(9, false);
                }
            }

            if (numHang == 2) {
                if (numGhe == 5) {
                    for (int i = 0; i < 10; i++) {
                        data.get(2).set(i, false);
                        data.get(5).set(i, false);
                    }
                } else {
                    for (int i = 0; i < 10; i++) {
                        data.get(2).set(i, false);
                        data.get(5).set(i, false);
                    }
                    for (int i = 0; i < 6; i++) {
                        data.get(i).set(4, false);
                        data.get(i).set(9, false);
                    }
                }
            }
        } else if (numKhanDai == 2) {
            for (int i = 3; i < 6; i++) {
                for (int j = 0; j < 10; j++) {
                    data.get(i).set(j, false);
                }
            }

            if (numHang == 3 && numGhe == 4) {
                for (int i = 0; i < 6; i++) {
                    data.get(i).set(4, false);
                    data.get(i).set(9, false);
                }
            }

            if (numHang == 2) {
                if (numGhe == 5) {
                    for (int i = 0; i < 10; i++) {
                        data.get(2).set(i, false);
                        data.get(5).set(i, false);
                    }
                } else {
                    for (int i = 0; i < 10; i++) {
                        data.get(2).set(i, false);
                        data.get(5).set(i, false);
                    }
                    for (int i = 0; i < 6; i++) {
                        data.get(i).set(4, false);
                        data.get(i).set(9, false);
                    }
                }
            }
        }

        try {
            // Ghi lại dữ liệu vào file CSV
            writeEditFile("../Data/editSeat.csv", data);
        } catch (IOException ex) {
            Logger.getLogger(EditSeats.class.getName()).log(Level.SEVERE, null, ex);
        }

        JOptionPane.showMessageDialog(this, "Đã cập nhật dữ liệu vào file CSV", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }//GEN-LAST:event_btnSaveActionPerformed

    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(EditSeats.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(EditSeats.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(EditSeats.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(EditSeats.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new EditSeats().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnSave;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JComboBox<String> selectGhe;
    private javax.swing.JComboBox<String> selectHang;
    private javax.swing.JComboBox<String> selectKhanDai;
    // End of variables declaration//GEN-END:variables
}
