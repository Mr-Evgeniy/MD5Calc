package Components;


import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;



/**
 * Этот класс создает главную панель программы. На ней расположены
 * компоненты, необходимые для работы с программой.
 */
public class FolderTreeAnalyserPanel extends JPanel
		implements ChangeLanguageListener, ActionListener, Runnable,
		MD5CalcProgressListener, MD5CalcStateListener {
    
    //указатели на основные компоненты программы
    private GridBagConstraints c = null;
    private ResourceBundle messages = null;
    private JPanel folderNamePanel = null;
    private JPanel resultsFileNamePanel = null;
    private JPanel progressPanel = null;
    private JTextField folderName = null;
    private JTextField resultsFileName = null;
    private JButton browseFolder = null;
    private JButton browseResFile = null;
    private JButton run = null;
    private JProgressBar totalProgress = null;
    private JProgressBar curFileProgress = null;
    private JLabel curFile = null;
    private JLabel total = null;
    private JLabel status = null;
    
    private int totalFiles = 0; //общее количество файлов в заданной папке
    private int processedFiles = 0; //количество обработанных файлов
    private long filesSize = 0; //общий размер файлов
    private long curFileSize = 0; //размер файла, обрабатываемого в данный момент
    private double prevPercent = 0; //процент от общего объема файлов который
    					//обработан на данный момент (без учета файла который
    					//обрабатывается в данный момент)
    private double curPercent = 0; //процент обработанного объема текущего файла
    
    private FolderTreeAnalyser analyser = null; //указатель на класс, который
    					//выполняет обработку заданной папки
    private List calculationResults = null; //список с результатами рассчета
    private Thread worker = null; //поток, в котором выполняется обработка папки
    private File srcDir = null; //заданная папка
    private File resFile = null; //файл, в который будут сохранены результаты
    
    private JFileChooser fc = null; //диалоговое окно выбора файла
    
    private JFrame mainFrame = null; //указатель на главный фрейм программы
    
    /**
     * Создает новые экземпляры класса <code>FolderTreeAnalyserPanel</code>
     * @param messages текстовые строки, которые используются при создании интерфейса
     * @param mainFrame указатель на главный фрейм программы
     */
    public FolderTreeAnalyserPanel(ResourceBundle messages, JFrame mainFrame) {
        //присваиваем начальные значения переменным и размещаем компоненты
        super(new GridBagLayout());
        this.messages = messages;
        this.mainFrame = mainFrame;
        fc = new JFileChooser();
        analyser = new FolderTreeAnalyser();
        analyser.addMD5CalculationProgressListener(this);
        analyser.addMD5CalculatorStateListener(this);
        this.setLayout(new GridBagLayout());
        c = new GridBagConstraints();
        
        folderName = new JTextField(30);
        browseFolder = new JButton(messages.getString(
                "FolderTreeAnalyserPanel.browse"));
        browseFolder.setActionCommand("browseFolder");
        browseFolder.addActionListener(this);
        folderNamePanel = new JPanel(new GridBagLayout());
        folderNamePanel.setBorder(BorderFactory.createTitledBorder(
                messages.getString("FolderTreeAnalyserPanel.folderName")));
        GridBagConstraints c1 = new GridBagConstraints();
        c1.weightx = 0.7;
        c1.weighty = 1;
        c1.fill = GridBagConstraints.BOTH;
        folderNamePanel.add(folderName, c1);
        c1.weightx = 0.3;
        folderNamePanel.add(browseFolder, c1);
        
        resultsFileName = new JTextField(30);
        browseResFile = new JButton(messages.getString(
                "FolderTreeAnalyserPanel.browse"));
        browseResFile.setActionCommand("browseResFile");
        browseResFile.addActionListener(this);
        resultsFileNamePanel = new JPanel(new GridBagLayout());
        resultsFileNamePanel.setBorder(BorderFactory.createTitledBorder(
                messages.getString("FolderTreeAnalyserPanel.saveRes")));
        c1.weightx = 0.7;
        resultsFileNamePanel.add(resultsFileName, c1);
        c1.weightx = 0.3;
        resultsFileNamePanel.add(browseResFile, c1);
        
        curFile = new JLabel(messages.getString(
                "FolderTreeAnalyserPanel.processingFile"));
        curFileProgress = new JProgressBar(0, 100);
        curFileProgress.setValue(0);
        total = new JLabel(messages.getString(
                "FolderTreeAnalyserPanel.totalProgress"));
        totalProgress = new JProgressBar(0, 100);
        totalProgress.setValue(0);
        run = new JButton(messages.getString("FolderTreeAnalyserPanel.run"));
        run.setActionCommand("run");
        run.addActionListener(this);
        progressPanel = new JPanel(new GridBagLayout());
        c1.gridx = 0;
        c1.gridy = 0;
        c1.weighty = 0.25;
        c1.weightx = 0.7;
        progressPanel.add(curFile, c1);
        c1.gridy = 1;
        progressPanel.add(curFileProgress, c1);
        c1.gridy = 2;
        progressPanel.add(total, c1);
        c1.gridy = 3;
        progressPanel.add(totalProgress, c1);
        c1.gridy = 0;
        c1.gridx = 1;
        c1.gridheight = 4;
        c1.weightx = 0.3;
        c1.weighty = 1.0;
        progressPanel.add(run, c1);
        
        status = new JLabel(messages.getString("FolderTreeAnalyserPanel.status")
                + "...");
        
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 0.2;
        add(folderNamePanel, c);
        c.gridy = 1;
        add(resultsFileNamePanel, c);
        c.gridy = 2;
        c.weighty = 0.4;
        add(progressPanel, c);
        c.gridy = 3;
        c.weighty = 0.2;
        add(status, c);
    }

    /**
     * Этот метод вызывается, если необходимо изменить язык интерфейса
     * @param mes текстовые строки, которые используются при создании интерфейса
     */
    @Override
    public void changeLanguage(ResourceBundle mes) {
        this.messages = mes;
        //изменяем заголовок главного окна приложения
        if(mainFrame != null) {
            mainFrame.setTitle(messages.getString("title"));
        }
        //изменяем надписи на всех компонентах
        browseFolder.setText(messages.getString("FolderTreeAnalyserPanel.browse"));
        ((TitledBorder)folderNamePanel.getBorder()).setTitle(
                messages.getString("FolderTreeAnalyserPanel.folderName"));
        browseResFile.setText(messages.getString("FolderTreeAnalyserPanel.browse"));
        ((TitledBorder)resultsFileNamePanel.getBorder()).setTitle(
                messages.getString("FolderTreeAnalyserPanel.saveRes"));
        curFile.setText(messages.getString("FolderTreeAnalyserPanel.processingFile"));
        total.setText(messages.getString("FolderTreeAnalyserPanel.totalProgress"));
        run.setText(messages.getString("FolderTreeAnalyserPanel.run"));
        if(totalFiles == 0) {
            status.setText(messages.getString("FolderTreeAnalyserPanel.status")
                    + "...");
        }
        else {
            status.setText(messages.getString("FolderTreeAnalyserPanel.status")
                    + ": " + processedFiles + " "
                    + messages.getString("FolderTreeAnalyserPanel.of")
                    + " " + totalFiles);
            
        }
    }

    /*
     * Вызывается если нажата какая-то из кнопок панели
     */
    @Override
    public void actionPerformed(ActionEvent arg0) {
        //если нажата кнопка "Пуск"
        if(arg0.getActionCommand().equals("run")) {
            srcDir = new File(folderName.getText());
            //выводим сообщение об ошибке, если указанная папка не существует
            if(!srcDir.exists()) {
                JOptionPane.showMessageDialog(mainFrame,
                        messages.getString("FolderTreeAnalyserPanel.noFolder"),
                        messages.getString("FolderTreeAnalyserPanel.error"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            //если задан файл в который нужно сохранить результат...
            if(!resultsFileName.getText().isEmpty()) {
                resFile = new File(resultsFileName.getText());
                Object[] options = {messages.getString("FolderTreeAnalyserPanel.yes"),
                        messages.getString("FolderTreeAnalyserPanel.no")};
                //...проверяем существует ли он на данный момент, и, если
                //существует, спрашиваем у пользователя можно ли его перезаписать
                if(resFile.exists()) {
                    int resVal = JOptionPane.showOptionDialog(mainFrame,
                            messages.getString("FolderTreeAnalyserPanel.file") + " "
                            + resultsFileName.getText() + " "
                            + messages.getString("FolderTreeAnalyserPanel.exists"),
                            messages.getString("FolderTreeAnalyserPanel.fileExists"),
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            options,
                            options[1]);
                    if(resVal == 1) {
                        //выходим, если перезаписывать нельзя
                        return;
                    }
                }
            }
            //создаем и запускаем поток в котором будут выполняться вычисления
            worker = new Thread(this);
            worker.start();
        }
        //если нажата кнопка "Обзор", относящаяся к выбору папки
        if(arg0.getActionCommand().equals("browseFolder")) {
            //создаем диалог выбора папки
            fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            int retVal = fc.showOpenDialog(mainFrame);
            //заносим результат выбора в соответствующее текстовое поле
            if(retVal == JFileChooser.APPROVE_OPTION) {
                folderName.setText(fc.getSelectedFile().toString());
            }
        }
        //если нажата кнопка "Обзор", относящаяся к выбору файла результатов
        if(arg0.getActionCommand().equals("browseResFile")) {
            //создаем диалог выбора файла
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int retVal = fc.showSaveDialog(mainFrame);
            //заносим результат выбора в соответствующее текстовое поле
            if(retVal == JFileChooser.APPROVE_OPTION) {
                resultsFileName.setText(fc.getSelectedFile().toString());
            }
        }
        //если нажата кнопка "Стоп"
        if(arg0.getActionCommand().equals("stop")) {
            //сбрасываем все счетчики
            totalFiles = 0;
            processedFiles = 0;
            filesSize = 0;
            prevPercent = 0;
            curPercent = 0;
            //останавливаем процесс рассчета, и, показываем соответствующее сообщение
            if(analyser != null) {
                analyser.stopAnalyse();
                JOptionPane.showMessageDialog(mainFrame,
                        messages.getString("FolderTreeAnalyserPanel.interruptedByUser"),
                        messages.getString("FolderTreeAnalyserPanel.interrupted"),
                        JOptionPane.INFORMATION_MESSAGE);
            }
            //изменяем назначение кнопки "Стоп" на "Пуск"
            run.setActionCommand("run");
            run.setText(messages.getString("FolderTreeAnalyserPanel.run"));
        }
    }
    
    /*
     * Создает диалоговое окно с результатами расчета
     * @param results список с результатами расчета
     * @param fileName имя файла, в который были сохранены результаты
     */
    private void showResultsDialog(List results, String fileName) {
        //если список результатов не создан, рассказываем об этом пользователю
        if(results == null) {
            JOptionPane.showMessageDialog(mainFrame,
                    messages.getString("FolderTreeAnalyserPanel.resultsAreAbsent"),
                    messages.getString("FolderTreeAnalyserPanel.noResults"),
                    JOptionPane.INFORMATION_MESSAGE);
        }
        else {
            //создаем и показываем диалоговое окно
            ResultsDialog r = new ResultsDialog(mainFrame, true, results, messages,
                    fileName);
            r.pack();
            r.setVisible(true);
        }
    }

    /**
     * Вызывается при создании потока. В этом потоке выполняется расчет MD5 сумм.
     */
    @Override
    public void run() {
        try {
            //запускаем вычисления
            calculationResults = analyser.analyse(srcDir.toString());
            //если результат по каким-либо причинам не получен - выходим
            if(calculationResults == null) {
                return;
            }
            //если задан файл результатов
            if(resFile != null) {
                try (PrintWriter pw = new PrintWriter(new FileOutputStream(resFile))) {
                    for(int i = 0; i < calculationResults.size(); i++) {
                        pw.println((String)calculationResults.get(i));
                    }
                }
                //если возникли ошибки при записи в файл...
                catch(IOException err) {
                    //... показываем соответствующее сообщение
                    JOptionPane.showMessageDialog(mainFrame,
                            messages.getString("FolderTreeAnalyserPanel.fileWriteError"),
                            messages.getString("FolderTreeAnalyserPanel.error"),
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        catch(IOException err) {
            //если возникли ошибки при рассчете MD5 сумм или анализе структуры папок
            //сообщаем о них пользователю
            JOptionPane.showMessageDialog(mainFrame,
                    err.getMessage(),
                    messages.getString("FolderTreeAnalyserPanel.error"),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Этот метод вызывается экземпляром класса MD5Calculator.
     * В качестве параметра передается сколько процентов от общего объема
     * файла обработано.
     * @param f пророцент объема файла, который обработан на данный момент
     */
    @Override
    public void setNewMD5ProgressValue(float f) {
        final int val = (int)f;
        //рассчитываем сколько процентов от общего объема папки обработано
        //на данный момент
        double calculatedBytes = curFileSize*f/100;
        curPercent = calculatedBytes*100/filesSize;
        final int cp = (int)Math.round(curPercent);
        final int pp = (int)Math.round(prevPercent);
        //отображаем информацию о ходе рассчета на панели
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                curFileProgress.setValue(val);
                totalProgress.setValue(pp + cp);
            }});
    }

    /**
     * Этот метод вызывается экземпляром класса FolderTreeAnalyser перед
     * началом анализа структуры папок
     */
    @Override
    public void folderStructureAnalyseBegin() {
        //сбрасываем все счетчики
        totalFiles = 0;
        processedFiles = 0;
        filesSize = 0;
        prevPercent = 0;
        curPercent = 0;
        //отображаем информацию о том, что начат процесс анализа на панели
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                curFileProgress.setValue(0);
                totalProgress.setIndeterminate(true);
                //изменяем назначение кнопки "Пуск" на "Стоп"
                run.setActionCommand("stop");
                run.setText(messages.getString("FolderTreeAnalyserPanel.stop"));
                curFile.setText(messages.getString(
                        "FolderTreeAnalyserPanel.processingFile"));
                status.setText(messages.getString(
                        "FolderTreeAnalyserPanel.totalProgress") + "...");
            }});
    }

    /**
     * Этот метод вызывается экземпляром класса FolderTreeAnalyser после
     * окончания анализа структуры папок. В качестве параметров передаются
     * результаты анализа.
     * @param folderCount количество папок
     * @param filesCount количество файлов
     * @param filesSize общий размер файлов
     */
    @Override
    public void folderStructureAnalyseEnd(int folderCount, int filesCount,
            long filesSize) {
        //сохраняем необходимые нам параметры
        totalFiles = filesCount;
        this.filesSize = filesSize;
        //отображаем результаты анализа на панели
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                totalProgress.setIndeterminate(false);
                totalProgress.setValue(0);
                status.setText(messages.getString(
                        "FolderTreeAnalyserPanel.totalProgress") +
                        " " + processedFiles + " " +
                        messages.getString("FolderTreeAnalyserPanel.of") +
                        " " + totalFiles);
            }});
    }

    /**
     * Этот метод вызывается экземпляром класса FolderTreeAnalyser перед
     * началом рассчета MD5 суммы очередного файла. В качестве параметров
     * передается информация о файле.
     * @param fileName имя файла
     * @param fileLength длина файла
     */
    @Override
    public void MD5SumCalculationBegin(String fileName, long fileLength) {
        //сохраняем параметры
        curFileSize = fileLength;
        final String fN = fileName;
        //отображаем имя файла на панели
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                curFile.setText(messages.getString(
                        "FolderTreeAnalyserPanel.processingFile") + " " + fN);
            }});
    }

    /**
     * Этот метод вызывается экземпляром класса FolderTreeAnalyser после
     * завершения рассчета MD5 суммы очередного файла.
     * @param fileName имя файла
     */
    @Override
    public void MD5SumCalculationEnd(String fileName) {
        if(fileName.isEmpty()) {
            //если папка не содержит файлов
            curPercent = 100;
        }
        else {
            //изменяем значения счетчиков
            processedFiles++;
            prevPercent += curPercent;
            curPercent = 0;
        }
        //отображаем информацию о завершении рассчета на панели
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                status.setText(messages.getString(
                "FolderTreeAnalyserPanel.totalProgress") +
                " " + processedFiles + " " +
                messages.getString("FolderTreeAnalyserPanel.of") +
                " " + totalFiles);
            }});
        //если данный файл является последним, то отображаем диалоговое окно
        //с результатами вычислений
        if(processedFiles == totalFiles) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    //изменяем назначение кнопки "Стоп" на "Пуск"
                    run.setActionCommand("run");
                    run.setText(messages.getString("FolderTreeAnalyserPanel.run"));
                    //если имя файла результатов задано, то отображаем его в
                    //диалоговом окне с результатами
                    if(resFile != null) {
                        //отображаем диалоговое окно с результатами
                        showResultsDialog(calculationResults,
                                resFile.getAbsolutePath());
                    }
                    else {
                        //отображаем диалоговое окно с результатами
                        showResultsDialog(calculationResults,
                                messages.getString("FolderTreeAnalyserPanel.noResFile"));
                    }
                }});
        }
    }
    private static final Logger LOG = Logger.getLogger(FolderTreeAnalyserPanel.class.getName());
}
