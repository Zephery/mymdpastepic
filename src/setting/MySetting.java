package setting;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Created with IntelliJ IDEA.
 * User: Zephery
 * Time: 2017/9/14 14:05
 * Description:
 */
public class MySetting implements Configurable {
    private JTextField textField1;
    private JTextField textField2;
    private JPanel jPanel;

    @Nls
    @Override
    public String getDisplayName() {
        return "haha";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return jPanel;
    }

    @Override
    public boolean isModified() {
        return true;
    }

    @Override
    public void apply() throws ConfigurationException {
        PropertiesComponent.getInstance().setValue("ACCESS_KEY", textField1.getText());
        PropertiesComponent.getInstance().setValue("SECRET_KEY", textField2.getText());
        System.out.println("hello");
    }

    @Override
    public void reset() {
        String t1 = PropertiesComponent.getInstance().getValue("ACCESS_KEY");
        String t2 = PropertiesComponent.getInstance().getValue("SECRET_KEY");
        if (t1 != null && t2 != null) {
            System.out.println(textField1.getText());
            textField1.setText(t1);
            textField2.setText(t2);
            System.out.println("setdata");
        } else {
            textField1.setText(null);
            textField2.setText(null);
        }
    }

    @Override
    public void disposeUIResources() {
    }
}