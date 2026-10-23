package JAVA;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;

/**
 * 学生信息管理系统
 * 运行环境：JDK 8+ / MySQL 5.7 或 8.0 / mysql-connector-java-8.0.30.jar
 * 数据库：student_management（首次运行会自动创建）
 */
public class Main extends JFrame {

    // ==================== 数据库配置（按自己电脑修改这里） ====================
    private static final String DB_HOST = "localhost";
    private static final String DB_PORT = "3306";
    private static final String DB_NAME = "student_management";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "root";   // PHPStudy 默认密码 root

    private static final String PARAMS =
            "?useUnicode=true&characterEncoding=utf8&useSSL=false"
            + "&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true";

    // 不指定库名，用于首次建库
    private static final String SERVER_URL =
            "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + PARAMS;
    private static final String DB_URL =
            "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME + PARAMS;

    // ==================== 界面组件 ====================
    private final JTable table;
    private final DefaultTableModel tableModel;
    private final JTextField txtId = new JTextField();
    private final JTextField txtName = new JTextField();
    private final JTextField txtAge = new JTextField();
    private final JTextField txtScore = new JTextField();
    private final JComboBox<String> cboGender = new JComboBox<>(new String[]{"男", "女"});
    private final JTextField txtMajor = new JTextField();
    private final JTextField searchField = new JTextField(22);
    private final JLabel statusLabel = new JLabel(" 正在连接数据库...");

    private static final Color THEME = new Color(52, 120, 216);
    private static final Color BG = new Color(244, 247, 251);
    private static final String FONT_NAME = "微软雅黑";

    public Main() {
        setTitle("JAVA学生信息管理系统");
        setSize(1080, 720);
        setMinimumSize(new Dimension(960, 640));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG);

        // ---------- 顶部标题栏 ----------
        add(buildHeader(), BorderLayout.NORTH);

        // ---------- 中间表格 ----------
        String[] columns = {"编号", "学号", "姓名", "性别", "年龄", "专业", "成绩"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;   // 表格只读，只能通过表单修改
            }
        };
        table = new JTable(tableModel);
        styleTable();
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new EmptyBorder(10, 14, 0, 14));
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);

        // ---------- 底部表单 + 按钮 + 状态栏 ----------
        add(buildBottom(), BorderLayout.SOUTH);

        // ---------- 事件绑定 ----------
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                fillFormFromTable();
            }
        });

        // ---------- 初始化数据库并加载数据 ----------
        if (initDatabase()) {
            loadData();
        }
    }

    // ==================== 界面构建 ====================

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(THEME);
        header.setBorder(new EmptyBorder(14, 18, 14, 18));

        JLabel title = new JLabel("学生信息管理系统");
        title.setFont(new Font(FONT_NAME, Font.BOLD, 24));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Java Swing + JDBC + MySQL");
        sub.setFont(new Font(FONT_NAME, Font.PLAIN, 13));
        sub.setForeground(new Color(214, 228, 250));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(title);
        left.add(Box.createVerticalStrut(2));
        left.add(sub);

        // 搜索区
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        right.setOpaque(false);

        JLabel lbl = new JLabel("关键字：");
        lbl.setFont(new Font(FONT_NAME, Font.PLAIN, 14));
        lbl.setForeground(Color.WHITE);

        searchField.setFont(new Font(FONT_NAME, Font.PLAIN, 14));
        searchField.setPreferredSize(new Dimension(200, 30));

        JButton searchBtn = makeButton("查 询", new Color(255, 255, 255), THEME);
        JButton resetBtn = makeButton("显示全部", new Color(255, 255, 255), THEME);
        searchBtn.setPreferredSize(new Dimension(78, 30));
        resetBtn.setPreferredSize(new Dimension(90, 30));

        right.add(lbl);
        right.add(searchField);
        right.add(searchBtn);
        right.add(resetBtn);

        searchBtn.addActionListener(e -> searchStudent(searchField.getText().trim()));
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            loadData();
        });
        searchField.addActionListener(e -> searchStudent(searchField.getText().trim()));

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JPanel buildBottom() {
        JPanel south = new JPanel(new BorderLayout());
        south.setBackground(BG);
        south.setBorder(new EmptyBorder(10, 14, 0, 14));

        // ---- 录入表单：2 行 3 列 ----
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(219, 226, 236)),
                new EmptyBorder(12, 16, 12, 16)));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.fill = GridBagConstraints.HORIZONTAL;

        addField(formPanel, gc, 0, 0, "学号：", txtId);
        addField(formPanel, gc, 0, 2, "姓名：", txtName);
        addField(formPanel, gc, 0, 4, "性别：", cboGender);
        addField(formPanel, gc, 1, 0, "年龄：", txtAge);
        addField(formPanel, gc, 1, 2, "专业：", txtMajor);
        addField(formPanel, gc, 1, 4, "成绩：", txtScore);

        // ---- 按钮区 ----
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 12));
        btnPanel.setBackground(BG);

        JButton addBtn = makeButton("添 加", Color.WHITE, new Color(39, 174, 96));
        JButton updateBtn = makeButton("修 改", Color.WHITE, THEME);
        JButton deleteBtn = makeButton("删 除", Color.WHITE, new Color(214, 69, 65));
        JButton refreshBtn = makeButton("刷 新", Color.WHITE, new Color(127, 140, 141));
        JButton clearBtn = makeButton("清 空", Color.WHITE, new Color(142, 68, 173));

        addBtn.addActionListener(e -> addStudent());
        updateBtn.addActionListener(e -> updateStudent());
        deleteBtn.addActionListener(e -> deleteStudent());
        refreshBtn.addActionListener(e -> loadData());
        clearBtn.addActionListener(e -> clearForm());

        btnPanel.add(addBtn);
        btnPanel.add(updateBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(refreshBtn);
        btnPanel.add(clearBtn);

        // ---- 状态栏 ----
        statusLabel.setFont(new Font(FONT_NAME, Font.PLAIN, 13));
        statusLabel.setForeground(new Color(90, 100, 115));
        statusLabel.setBorder(new EmptyBorder(8, 4, 10, 4));

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(formPanel, BorderLayout.CENTER);
        centerPanel.add(btnPanel, BorderLayout.SOUTH);

        south.add(centerPanel, BorderLayout.CENTER);
        south.add(statusLabel, BorderLayout.SOUTH);
        return south;
    }

    private void addField(JPanel panel, GridBagConstraints gc,
                          int row, int col, String label, JComponent field) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font(FONT_NAME, Font.PLAIN, 14));
        lbl.setHorizontalAlignment(SwingConstants.RIGHT);

        field.setFont(new Font(FONT_NAME, Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(180, 30));
        if (field instanceof JTextField) {
            ((JTextField) field).setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 208, 220)),
                    new EmptyBorder(2, 6, 2, 6)));
        }

        gc.weightx = 0;
        gc.gridx = col;
        gc.gridy = row;
        panel.add(lbl, gc);

        gc.weightx = 1;
        gc.gridx = col + 1;
        panel.add(field, gc);
    }

    private JButton makeButton(String text, Color fg, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font(FONT_NAME, Font.BOLD, 14));
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(96, 36));
        return btn;
    }

    private void styleTable() {
        table.setFont(new Font(FONT_NAME, Font.PLAIN, 14));
        table.setRowHeight(32);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(new Color(206, 226, 250));
        table.setSelectionForeground(Color.BLACK);
        table.setGridColor(new Color(230, 235, 242));
        table.setShowVerticalLines(false);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font(FONT_NAME, Font.BOLD, 14));
        header.setPreferredSize(new Dimension(0, 36));
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                l.setBackground(THEME);
                l.setForeground(Color.WHITE);
                l.setFont(new Font(FONT_NAME, Font.BOLD, 14));
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(80, 140, 220)));
                return l;
            }
        });

        // 奇偶行底色 + 居中
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 253));
                }
                return c;
            }
        };
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        int[] widths = {60, 130, 110, 70, 70, 220, 90};
        for (int i = 0; i < widths.length && i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    // ==================== 数据库 ====================

    /**
     * 连接数据库：先连服务器自动建库，再建表。
     *
     * @return 是否连接成功
     */
    private boolean initDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            // 1. 自动创建数据库（避免 Unknown database 错误）
            try (Connection conn = DriverManager.getConnection(SERVER_URL, DB_USER, DB_PASS);
                 Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME
                        + " DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci");
            }

            // 2. 建表
            String sql = "CREATE TABLE IF NOT EXISTS students ("
                    + "id INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',"
                    + "student_id VARCHAR(50) NOT NULL UNIQUE COMMENT '学号',"
                    + "name VARCHAR(50) NOT NULL COMMENT '姓名',"
                    + "gender VARCHAR(10) COMMENT '性别',"
                    + "age INT COMMENT '年龄',"
                    + "major VARCHAR(100) COMMENT '专业',"
                    + "score DOUBLE COMMENT '成绩'"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生信息表'";
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(sql);
            }

            setStatus("数据库连接成功：" + DB_NAME + " @ " + DB_HOST + ":" + DB_PORT, false);
            return true;

        } catch (ClassNotFoundException e) {
            showError("找不到 MySQL 驱动类 com.mysql.cj.jdbc.Driver。\n\n"
                    + "请确认项目已引入 mysql-connector-java-8.0.30.jar：\n"
                    + "File → Project Structure → Modules → Dependencies → + → JARs。");
        } catch (SQLException e) {
            showError("数据库连接失败！\n\n错误信息：" + e.getMessage() + "\n"
                    + "错误代码：" + e.getErrorCode() + "  SQLState：" + e.getSQLState() + "\n\n"
                    + "请依次检查：\n"
                    + "1. PHPStudy 中的 MySQL 是否已启动（端口 " + DB_PORT + "）；\n"
                    + "2. 账号密码是否为 " + DB_USER + " / " + DB_PASS + "；\n"
                    + "3. 源码顶部 DB_PASS 是否与你的 MySQL 密码一致。");
        }
        return false;
    }

    // ==================== 增删改查 ====================

    private void addStudent() {
        try {
            String studentId = txtId.getText().trim();
            String name = txtName.getText().trim();
            String gender = (String) cboGender.getSelectedItem();
            String major = txtMajor.getText().trim();

            if (studentId.isEmpty() || name.isEmpty() || major.isEmpty()) {
                warn("学号、姓名、专业不能为空！");
                return;
            }
            int age = Integer.parseInt(txtAge.getText().trim());
            double score = Double.parseDouble(txtScore.getText().trim());
            if (age < 1 || age > 100) {
                warn("年龄必须在 1 ~ 100 之间！");
                return;
            }
            if (score < 0 || score > 100) {
                warn("成绩必须在 0 ~ 100 之间！");
                return;
            }

            String sql = "INSERT INTO students(student_id, name, gender, age, major, score) "
                    + "VALUES(?, ?, ?, ?, ?, ?)";
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, studentId);
                ps.setString(2, name);
                ps.setString(3, gender);
                ps.setInt(4, age);
                ps.setString(5, major);
                ps.setDouble(6, score);
                ps.executeUpdate();
            }

            info("添加成功！");
            loadData();
            clearForm();

        } catch (NumberFormatException e) {
            warn("年龄、成绩必须是数字！");
        } catch (SQLIntegrityConstraintViolationException e) {
            warn("学号 " + txtId.getText().trim() + " 已存在，不能重复添加！");
        } catch (SQLException e) {
            showError("添加失败：" + e.getMessage());
        }
    }

    private void updateStudent() {
        int row = table.getSelectedRow();
        if (row < 0) {
            warn("请先在表格中选中要修改的记录！");
            return;
        }
        try {
            int dbId = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
            String studentId = txtId.getText().trim();
            String name = txtName.getText().trim();
            String gender = (String) cboGender.getSelectedItem();
            String major = txtMajor.getText().trim();

            if (studentId.isEmpty() || name.isEmpty() || major.isEmpty()) {
                warn("学号、姓名、专业不能为空！");
                return;
            }
            int age = Integer.parseInt(txtAge.getText().trim());
            double score = Double.parseDouble(txtScore.getText().trim());
            if (age < 1 || age > 100) {
                warn("年龄必须在 1 ~ 100 之间！");
                return;
            }
            if (score < 0 || score > 100) {
                warn("成绩必须在 0 ~ 100 之间！");
                return;
            }

            String sql = "UPDATE students SET student_id=?, name=?, gender=?, age=?, major=?, score=? WHERE id=?";
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, studentId);
                ps.setString(2, name);
                ps.setString(3, gender);
                ps.setInt(4, age);
                ps.setString(5, major);
                ps.setDouble(6, score);
                ps.setInt(7, dbId);
                ps.executeUpdate();
            }

            info("修改成功！");
            loadData();
            clearForm();

        } catch (NumberFormatException e) {
            warn("年龄、成绩必须是数字！");
        } catch (SQLIntegrityConstraintViolationException e) {
            warn("学号 " + txtId.getText().trim() + " 已被其他记录占用！");
        } catch (SQLException e) {
            showError("修改失败：" + e.getMessage());
        }
    }

    private void deleteStudent() {
        int row = table.getSelectedRow();
        if (row < 0) {
            warn("请先在表格中选中要删除的记录！");
            return;
        }
        String name = tableModel.getValueAt(row, 2).toString();
        int ok = JOptionPane.showConfirmDialog(this,
                "确定要删除【" + name + "】这条记录吗？", "确认删除",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            int dbId = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM students WHERE id=?")) {
                ps.setInt(1, dbId);
                ps.executeUpdate();
            }
            info("删除成功！");
            loadData();
            clearForm();
        } catch (SQLException e) {
            showError("删除失败：" + e.getMessage());
        }
    }

    private void searchStudent(String keyword) {
        if (keyword.isEmpty()) {
            loadData();
            return;
        }
        String sql = "SELECT * FROM students WHERE student_id LIKE ? OR name LIKE ? OR major LIKE ? ORDER BY id";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);

            fillTable(ps.executeQuery());
            if (tableModel.getRowCount() == 0) {
                info("没有找到与【" + keyword + "】匹配的记录。");
                loadData();
            } else {
                setStatus("查询到 " + tableModel.getRowCount() + " 条记录（关键字：" + keyword + "）", false);
            }
        } catch (SQLException e) {
            showError("查询失败：" + e.getMessage());
        }
    }

    private void loadData() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM students ORDER BY id")) {
            fillTable(rs);
            setStatus("数据库连接正常：" + DB_NAME + " @ " + DB_HOST + ":" + DB_PORT
                    + "    共 " + tableModel.getRowCount() + " 条记录", false);
        } catch (SQLException e) {
            setStatus("读取数据失败：" + e.getMessage(), true);
            showError("读取数据失败：" + e.getMessage());
        }
    }

    private void fillTable(ResultSet rs) throws SQLException {
        tableModel.setRowCount(0);
        while (rs.next()) {
            tableModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("student_id"),
                    rs.getString("name"),
                    rs.getString("gender"),
                    rs.getInt("age"),
                    rs.getString("major"),
                    rs.getDouble("score")
            });
        }
    }

    private void fillFormFromTable() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }
        txtId.setText(str(tableModel.getValueAt(row, 1)));
        txtName.setText(str(tableModel.getValueAt(row, 2)));
        cboGender.setSelectedItem(str(tableModel.getValueAt(row, 3)));
        txtAge.setText(str(tableModel.getValueAt(row, 4)));
        txtMajor.setText(str(tableModel.getValueAt(row, 5)));
        txtScore.setText(str(tableModel.getValueAt(row, 6)));
        setStatus("已选中编号 " + tableModel.getValueAt(row, 0) + " 的记录，可修改后点【修改】", false);
    }

    private String str(Object o) {
        return o == null ? "" : o.toString();
    }

    private void clearForm() {
        txtId.setText("");
        txtName.setText("");
        txtAge.setText("");
        txtMajor.setText("");
        txtScore.setText("");
        cboGender.setSelectedIndex(0);
        table.clearSelection();
        txtId.requestFocusInWindow();
    }

    // ==================== 提示 ====================

    private void setStatus(String text, boolean error) {
        statusLabel.setText("  " + text);
        statusLabel.setForeground(error ? new Color(200, 45, 45) : new Color(70, 105, 150));
    }

    private void info(String msg) {
        JOptionPane.showMessageDialog(this, msg, "提示", JOptionPane.INFORMATION_MESSAGE);
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "提示", JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "错误", JOptionPane.ERROR_MESSAGE);
    }

    // ==================== 入口 ====================

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.put("OptionPane.messageFont", new Font(FONT_NAME, Font.PLAIN, 14));
            UIManager.put("OptionPane.buttonFont", new Font(FONT_NAME, Font.PLAIN, 13));
        } catch (Exception ignored) {
            // 使用默认外观
        }
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}
