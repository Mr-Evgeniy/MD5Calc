/*
 * MD5MessageReader.java
 */

package Components;

import java.io.*;

/**
 * Этот класс выполняет подготовку данных для вычисления MD5
 * суммы. А именно: добавляет дополнительные биты (шаг 1 в 
 * RFC1321), добавляет длину сообщения (шаг 2 в RFC1321).
 * 
 * Исходные данные задаются с помощью метода <CODE>readMessage(File f)</CODE>.
 * 
 * Обработанные данные возвращаются методом <CODE>getNextMessageBlock()</CODE>
 * в виде массивов по 64 байта (16 элементов типа <CODE>int</CODE>).
 */
public class MD5MessageReader {
    
    /* Преобразует массив байт в массив int (по 4 байта). В соответствии
       с RFC1321 первый байт в каждой 4-х байтной группе записывается
       последним.
     */
    private static int[] decode(byte[] mes) {
        int[] res = new int[mes.length / 4];
        for(int i = 0; i < res.length; i++) {
            res[i] = (((int)mes[i*4]) & 0xFF) |
                     (((((int)mes[i*4 + 1]) << 8) & 0xFF00)) | (((((int)mes[i*4 + 2]) << 16) & 0xFF_0000)) | (((((int)mes[i*4 + 3]) << 24) & 0xFF00_0000));
        }
        return res;
    }
    
    /**
     * Переставляет байты в числе типа <CODE>int</CODE> (4 байта)
     * в обратном порядке, т.е. первый байт станет последним.
     * @param val число, которое нужно преобразовать
     * @return преобразованное число
     */
    public static int convertInt(int val) {
        byte[] t = new byte[4];
        t[3] = (byte)(val & 0xFF);
        t[2] = (byte)((val >>> 8) & 0xFF);
        t[1] = (byte)((val >>> 16) & 0xFF);
        t[0] = (byte)((val >>> 24) & 0xFF);
        int[] res = decode(t);
        return res[0];
    }
    
    private byte[] message = null;
    private int[] decodedMessage = null;
    private File dataFile = null;
    private BufferedInputStream is = null;
    private int currentPos = 0;
    private boolean appended = false;
    
    /**
     * Создает новые экземпляры класса <CODE>MD5MessageReader</CODE>
     */
    public MD5MessageReader() {
    }
    
    /**
     * Задает файл для которого необходимо рассчитать MD5 сумму.
     * @param f имя файла
     * @throws java.io.IOException при возникновении ошибок работы с файлом
     */
    public void readMessage(File f) throws IOException {
        dataFile = f;
        currentPos = 0;
        appended = false;
        message = null;
        if(is != null) {
            is.close();
            is = null;
        }
    }
    
    /**
     * Возвращает массив типа <CODE>int</CODE> длиной 16,
     * содержащий данные для рассчета MD5 суммы.
     * Каждый последующий вызов этого метода возвращает новый блок
     * данных. Если достигнут конец исходного сообщения возвращает
     * <CODE>null</CODE>.
     * Для того, чтобы получить данные несколько раз (начиная с
     * начала сообщения) нужно вызвать метод <CODE>readMessage(File f)</CODE>.
     * @throws java.io.IOException если возникли ошибки при работе с файлом исходных данных
     * @return новый блок данных в виде массива типа <CODE>int</CODE>,
     * длиной 16 (16 32-хбитных слов), если достигнут конец
     * сообщения - <CODE>null</CODE>
     */
    public int[] getNextMessageBlock() throws IOException {
        if(dataFile == null || (message == null && appended == true)) {
            return null;
        }
        int[] res = new int[16];
        if(message == null) {
            message = getMessageFromFile();
        }
        if(currentPos >= message.length / 4) {
            currentPos = 0;
            message = getMessageFromFile();
            if(message == null) {
                return null;
            }
        }
        decodedMessage = decode(message);
        System.arraycopy(decodedMessage, currentPos, res, 0, 16);
        currentPos += 16;
        return res;
    }
    
    /*
     Этот метод читает следующий блок данных из файла и преобразует
     его в массив byte[] message. К последнему блоку данных добавляються
     биты в соответствии с шагом 1 и 2 в RFC1321.
     Возвращает указатель на массив прочитанных данных (если они были прочитаны),
     null - если достигнут конец файла.
    */
    private byte[] getMessageFromFile() throws IOException {
        if(is == null) {
            is = new BufferedInputStream(new FileInputStream(dataFile), 1_024 * 1_024);
        }
        final int bufLength = 1_024;
        byte[] buf = new byte[bufLength];
        int bytesNumb = is.read(buf);
        if(bytesNumb == -1 && appended) {
            is.close();
            is = null;
            return null;
        }
        if(bytesNumb == -1) {
            bytesNumb = 0;
        }
        if(bytesNumb != bufLength) {
            byte[] appBlock = getAppendBlock(bytesNumb);
            byte[] messageLength = encode(dataFile.length());
            byte[] res = new byte[bytesNumb + appBlock.length
                    + messageLength.length];
            System.arraycopy(buf, 0, res, 0, bytesNumb);
            System.arraycopy(appBlock, 0, res, bytesNumb, appBlock.length);
            System.arraycopy(messageLength, 0, res,
                    bytesNumb + appBlock.length, messageLength.length);
            appended = true;
            return res;
        }
        return buf;
    }
    
    /* Преобразует значение длины сообщения (mesLength) в массив из 8 байт.
       В соответствии с RFC1321 байты записываются в обратном порядке.
     */
    private byte[] encode(long mesLength) {
        long mesLengthInBits = mesLength * 8;
        byte[] res = new byte[8];
        for(int i = 0; i < 8; i++) {
            res[i] = (byte)((mesLengthInBits >>> (i*8)) & 0xFF);
        }
        return res;
    }
    
    /* Дополняет сообщение таким образом, чтобы остаток от деления его
    длины на 512 бит (64 байт) был равен 448 бит (56 байт).
    Шаг 1 в RFC1321.
     */
    private byte[] getAppendBlock(int length) {
        int blockLength;
        int p = length % 64;
        boolean firstByte = true;
        if (p < 56) {
            blockLength = 56 - p;
        } else {
            blockLength = 120 - p;
        }
        byte[] res = new byte[blockLength];
        for (int i = 0; i < res.length; i++) {
            if (firstByte) {
                firstByte = false;
                res[i] = (byte)(res[i] | 0x80);
            } else {
                res[i] = 0;
            }
        }
        return res;
    }
}    
