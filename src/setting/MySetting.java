package setting;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import img2md.Common;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Created with IntelliJ IDEA.
 * User: Zephery
 * Time: 2017/9/14 14:05
 * Description:
 */
public class MySetting implements Configurable {
    private JTextField accesskey;
    private JTextField secretekey;
    private JPanel jPanel;
    private JTextArea customcode;
    private JTextField callbackurl;
    private JCheckBox yesCheckBox;
    private JButton testYourCodeButton;
    private JButton resetCustom_codeButton;

    @Nls
    @Override
    public String getDisplayName() {
        return "PastePic";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        testYourCodeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    File file = new File(System.getProperty("user.dir"));//类路径(包文件上一层)
                    URL url = file.toURI().toURL();
                    ClassLoader loader = new URLClassLoader(new URL[]{url});//创建类加载器
                    Class<?> cls = loader.loadClass("Main");//加载指定类，注意一定要带上类的包名
                    Object obj = cls.newInstance();//初始化一个实例
                    Method method = cls.getMethod("sendpic", String.class);//方法名和对应的参数类型
                    String imagepath = "1.png";
                    String success = method.invoke(obj, imagepath).toString();//调用得到的上边的方法method
                    if (!success.equals("true")) {
                        StringBuilder stringBuilder = new StringBuilder(Common.ERROR_CODE);
                        customcode.setText(stringBuilder.append(customcode.getText()).toString());
                    }
                } catch (Exception ee) {
                    ee.printStackTrace();
                }
            }
        });
        resetCustom_codeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                customcode.setText(Common.ORIGIN_CODE);
            }
        });
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
        PropertiesComponent.getInstance().setValue("CUSTOM_CODE", customcode.getText());
        PropertiesComponent.getInstance().setValue("CALLBACK_URL", callbackurl.getText());
        PropertiesComponent.getInstance().setValue("yes", yesCheckBox.isSelected());
        String temp = customcode.getText();
        try {
            String filepath = System.getProperty("user.dir") + "\\Main.java";
            File file = new File(filepath);
            FileWriter writer = new FileWriter(file);
            writer.write(temp);
            writer.close();
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            int flag = compiler.run(null, null, null, filepath);
            System.out.println(flag == 0 ? "编译成功" : "编译失败");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void reset() {
        String access_key = PropertiesComponent.getInstance().getValue("ACCESS_KEY");
        String secret_key = PropertiesComponent.getInstance().getValue("SECRET_KEY");
        String custom_code = PropertiesComponent.getInstance().getValue("CUSTOM_CODE");
        String callback_url = PropertiesComponent.getInstance().getValue("CALLBACK_URL");
        boolean yes = PropertiesComponent.getInstance().getBoolean("yes");
        if (access_key != null && secret_key != null && callback_url != null && customcode != null) {
            callbackurl.setText(callback_url);
            accesskey.setText(access_key);
            secretekey.setText(secret_key);
            customcode.setText(custom_code);
        } else {
            callbackurl.setText(callback_url);
            accesskey.setText(null);
            secretekey.setText(null);
            customcode.setText(Common.ORIGIN_CODE);
        }
        yesCheckBox.setSelected(yes);
    }

    @Override
    public void disposeUIResources() {
    }
}