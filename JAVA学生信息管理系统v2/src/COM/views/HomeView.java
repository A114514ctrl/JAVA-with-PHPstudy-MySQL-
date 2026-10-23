package COM.views;
import javax.swing.*;
import java.awt.*;
public class HomeView extends JFrame {
    public HomeView() {
        this.setTitle("系统页面");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.inituI();
    }
    private void inituI(){
        this.setTitle("系统主页");
        this.setSize(800,900);
    }
}
