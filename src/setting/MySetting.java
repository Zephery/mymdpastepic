package setting;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * User: Zephery
 * Time: 2017/9/14 14:05
 */
public class MySetting implements Configurable {
    private JTextField accesskey;
    private JTextField secretekey;
    private JPanel jPanel;
    private JTextField callbackurl;

    @Nls
    @Override
    public String getDisplayName() {
        return "PastePic";
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
        PropertiesComponent.getInstance().setValue("ACCESS_KEY", accesskey.getText());
        PropertiesComponent.getInstance().setValue("SECRET_KEY", secretekey.getText());
        PropertiesComponent.getInstance().setValue("CALLBACK_URL", callbackurl.getText());
    }

    @Override
    public void reset() {
        String access_key = PropertiesComponent.getInstance().getValue("ACCESS_KEY");
        String secret_key = PropertiesComponent.getInstance().getValue("SECRET_KEY");
        String callback_url = PropertiesComponent.getInstance().getValue("CALLBACK_URL");
        callbackurl.setText(callback_url);
        accesskey.setText(access_key);
        secretekey.setText(secret_key);
    }

    @Override
    public void disposeUIResources() {
    }
}