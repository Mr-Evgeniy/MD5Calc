/*
 * ChangeLanguageListener.java
 *
 */

package Components;

import java.util.ResourceBundle;

/**Этот интерфейс предназначен для объектов, которые используются при создании
 * графического интерфейса и могут изменять его язык
 *
 */
public interface ChangeLanguageListener {
    /**
     * Объект, реализующий данный интерфейс, при вызове этой функции должен
     * прочитать строки из mes, и соответственно настроить язык интерфейса.
     * @param mes текстовые строки, которые используются при создании интерфейса
     */
    public void changeLanguage(ResourceBundle mes);
}
