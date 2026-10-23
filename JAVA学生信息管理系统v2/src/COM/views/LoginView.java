package COM.views;

import javax.swing.*;
import java.awt.*;

/**
 * 登录界面（演示用视图。主程序使用 JAVA.Main 的单窗口模式，本类保留作扩展）
 */
public class LoginView extends JPanel {

    private final JTextField txtUser = new JTextField(16);
    private final JPasswordField txtPwd = new JPasswordField(16);
    private final JButton btnLogin = new JButton("登 录");

    public LoginView() {
        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);

        JPanel box = new JPanel(new GridBagLayout());
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 220, 235)),
                BorderFactory.createEmptyBorder(24, 32, 24, 32)));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 8, 8, 8);
        gc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("用户登录");
        title.setFont(new Font("微软雅黑", Font.BOLD, 20));
        gc.gridx = 0;
        gc.gridy = 0;
        gc.gridwidth = 2;
        box.add(title, gc);

        gc.gridwidth = 1;
        gc.gridx = 0;
        gc.gridy = 1;
        box.add(new JLabel("用户名："), gc);
        gc.gridx = 1;
        box.add(txtUser, gc);

        gc.gridx = 0;
        gc.gridy = 2;
        box.add(new JLabel("密　码："), gc);
        gc.gridx = 1;
        box.add(txtPwd, gc);

        gc.gridx = 0;
        gc.gridy = 3;
        gc.gridwidth = 2;
        box.add(btnLogin, gc);

        add(box);
    }

    public String getUsername() {
        return txtUser.getText().trim();
    }

    public String getPassword() {
        return new String(txtPwd.getPassword());
    }

    public JButton getLoginButton() {
        return btnLogin;
    }
}
