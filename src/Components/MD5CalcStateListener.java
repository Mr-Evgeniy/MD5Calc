/**
 * MD5CalcStateListener.java
 */

package Components;

/**
 * Этот интерфейс реализуют классы, которые хотят быть уведомлены
 * о процессе рассчета MD5 сумм для папки с файлами.
 */
public interface MD5CalcStateListener {
    
    /**
     * Этот метод вызывается, если начат процесс анализа структуры
     * папок и файлов в них (составление списка файлов, определение
     * их общего размера и т.д.)
     */
    public void folderStructureAnalyseBegin();
    
    /**
     * Этот метод вызывается после завершения анализа структуры папок.
     * В качестве параметров получает результаты анализа.
     * 
     * @param folderCount количество папок
     * @param filesCount количество файлов
     * @param filesSize общий размер файлов
     */
    public void folderStructureAnalyseEnd(int folderCount,
            int filesCount, long filesSize);
    
    /**
     * Этот метод вызывается перед началом рассчета MD5 суммы
     * очередного файла
     * @param fileName имя файла
     * @param fileLength длина файла
     */
    public void MD5SumCalculationBegin(String fileName, long fileLength);
    
    /**
     * Этот метод вызывается после окончания рассчета MD5 суммы
     * очередного файла
     * @param fileName имя файла
     */
    public void MD5SumCalculationEnd(String fileName);
}
