/*
 * Run.java
 */

package Run;

import Components.FolderTreeAnalyserPanel;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.*;

/** Это основной класс. Он содержит функцию main(), которая является точкой
 * входа в программу.
 * Тут создается графический интерфейс программы.
 */
public class Run {
    
    /*Эта функция является точкой входа в программу*/
    public static void main(String[] args) {
        //Создаем отдельный поток для работы с графическим интерфейсом
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                //Определяем текущие региональные настройки (используется для
                //начальной установки языка)
                Locale curLocale = Locale.getDefault();
                //Загружаем текстовые строки для данного языка (надписи на
                //кнопках, меню и т.п.)
                ResourceBundle messages = ResourceBundle.getBundle("lang/messages",
                        curLocale);
                createAndShowGUI(messages);
            }
        });
    }
    
    /*Эта функция создает главное окно программы*/
    private static void createAndShowGUI(ResourceBundle messages) {
        try {
            //Устанавливаем системный внешний вид (если это возможно)
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            //Создаем главный фрейм
        }
        JFrame frame = new JFrame(messages.getString("title"));
        //программа должна прекращать работу при закрытии главного окна
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //загружаем иконку и устанавливаем её вместо "кофейной чашки"
        java.net.URL imageURL = Run.class.getResource("images/icon24x24.png");
        if(imageURL != null) {
            ImageIcon icon = new ImageIcon(imageURL);
            frame.setIconImage(icon.getImage());
        }
        //Создаем панель для работы с калькулятором, и устанавливаем её
        //главной панелью окна
        JComponent contentPane = new FolderTreeAnalyserPanel(messages, frame);
        contentPane.setOpaque(true);
        frame.setContentPane(contentPane);
        //Создаем меню и добавляем его к окну
        Components.Menu mainMenu = new Components.Menu(messages);
        frame.setJMenuBar(mainMenu);
        //Регистрируем объекты, которые должны изменять свое содержание
        //если пользователь изменил язык интерфейса
        mainMenu.addChangeLanguageListener(mainMenu);
        mainMenu.addChangeLanguageListener((FolderTreeAnalyserPanel)contentPane);
        //упаковываем фрейм и делаем его видимым
        frame.pack();
        frame.setVisible(true);
    }
    
    /* Создает новые экземпляры класса Run */
    public Run() {
    }
}
