package Components;

public interface MD5CalcProgressListener {
    /**
     * Этот метод устанавливает новое значение процента выполнения
     * рассчета.
     * @param f процент выполнения рассчета
     */
    public void setNewMD5ProgressValue(float f);
}
