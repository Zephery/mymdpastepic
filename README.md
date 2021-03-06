# Markdown paste image
每次在idea的markdown中要粘贴图片的时候，要么复制链接，要么需要将软件手动上传到七牛云，本人根据了[holgerbrandl/pasteimages](https://github.com/holgerbrandl/pasteimages)这个本地的软件修改了下源码，变成了现在的作品，同时，还能支持扩展，但是这部分还没完成，代码存放[位置](https://github.com/Zephery/mymdpastepic)，插件[下载地址](https://raw.githubusercontent.com/Zephery/mymdpastepic/master/mymdpastepic.zip)
此工具可运行在Intellij、Python、PhpStorm等jetbrains的所有软件中，使用效果如下：
<div align="center">

![](http://ohlrxdl4p.bkt.clouddn.com/1111111113r413523.gif)

</div>



## 插件开发过程
1.搭建环境  
2.实现Action接口  
3.Setting的设置  
4.拓展cdn  
5.插件打包  

## 整体介绍
主要是逻辑的关系，plugin.xml为配置文件、PasteImageHandler控制器，如果是ctrl+v这个动作，则进入PasteImageFromClipboard，然后开始逻辑判断
<div align="center">

![](http://ohlrxdl4p.bkt.clouddn.com/images/20170916072026.png)

</div>

## 1.搭建环境
由于使用的是idea的旗舰版，软件中自带了idea的插件开发包，new->project，选择plugin
<div align="center">

![](http://ohlrxdl4p.bkt.clouddn.com/images/20170916064612.png)

</div>
然后点击下一步，再然后是finish。页面结构如下：
<div align="center">

![](http://ohlrxdl4p.bkt.clouddn.com/images/20170916064847.png)

</div>

配置文件：
```xml
<idea-plugin>
  <id>com.your.company.unique.plugin.id</id><!--插件的id，如果需要上次到idea仓库让别人使用，不能跟其他的一致-->
  <name>Plugin display name here</name><!--插件名字-->
  <version>1.0</version><!--版本名字-->
  <vendor email="support@yourcompany.com" url="http://www.yourcompany.com">YourCompany</vendor>
  <!--插件的简要描述-->
  <description><![CDATA[
      Enter short description for your plugin here.<br>
      <em>most HTML tags may be used</em>
    ]]></description>
  <!--版本变化信息-->
  <change-notes><![CDATA[
      Add change notes here.<br>
      <em>most HTML tags may be used</em>
    ]]>
  </change-notes>
  <!--idea版本-->
  <!-- please see http://www.jetbrains.org/intellij/sdk/docs/basics/getting_started/build_number_ranges.html for description -->
  <idea-version since-build="145.0"/>
  <!--产品选择-->
  <!-- please see http://www.jetbrains.org/intellij/sdk/docs/basics/getting_started/plugin_compatibility.html
       on how to target different products -->
  <!-- uncomment to enable plugin in all products
  <depends>com.intellij.modules.lang</depends>
  -->
  <!--拓展组件注册，本地开发的时候不要冲突，特别先后次序问题-->
  <extensions defaultExtensionNs="com.intellij">
    <!-- Add your extensions here -->
  </extensions>
  <!--Action注册-->
  <actions>
    <!-- Add your actions here -->
  </actions>

</idea-plugin>
```

## 2.实现接口
Hello World的讲解看看这位[作者](http://blog.csdn.net/liuloua/article/details/51917362)的吧。  
（1）首先，在img2md中定义一个PasteImageHandler类，并在xml中注册，该类的意思是是每次在markdown文件中使用ctrl+v(粘贴)的时候，先调用下面这个函数，如果符合条件，则进入：PasteImageFromClipboard。
```java
if ("Markdown".equals(fileType.getName())) {
    Image imageFromClipboard = ImageUtils.getImageFromClipboard();
    if (imageFromClipboard != null) {
        assert caret == null : "Invocation of 'paste' operation for specific caret is not supported";
        PasteImageFromClipboard action = new PasteImageFromClipboard();
        AnActionEvent event = createAnEvent(action, dataContext);
        action.actionPerformed(event);
        return;
    }
}
```
plugin.xml的配置文件如下：
```xml
    <extensions defaultExtensionNs="com.intellij">
        <editorActionHandler action="EditorPaste" implementationClass="img2md.PasteImageHandler" order="first"/>
    </extensions>
```
（2）右键src，新建AnAction的一个继承类：PasteImageFromClipboard，重写actionPerformed方法，该方法声明要做什么。其中，定义了一个ImageInsertSettingPanel来对粘贴之后的弹出的选项。
<div align="center">

![](http://ohlrxdl4p.bkt.clouddn.com/images/20170916065948.png)
</div>
<div align="center">

![](http://ohlrxdl4p.bkt.clouddn.com/images/0dabcc6520170916073807.png)
</div>

随后，plugin.xml中就多了一个action：
```xml
<actions>
    <action id="PastePic" class="img2md.PasteImageFromClipboard" text="PastePic"
            description="Paste an image from clipboard at the current cursor position">
        <add-to-group group-id="EditMenu" anchor="last"/>
        <keyboard-shortcut keymap="$default" first-keystroke="shift meta V"/>
    </action>
</actions>
```
弹出的选项窗如下,可以选择文件名字，文件目录，是否透明化，是否圆角，图片大小,如果不想要此弹窗，我在设置中设置了一个功能按钮，下面会讲到。
<div align="center">

![](http://ohlrxdl4p.bkt.clouddn.com/images/c43a86aa20170916073141.png)
</div>

## 3.Setting的设置  
本来是只做七牛云的，如果是七牛云，中间的那个自定义框不需要管，只需要填好key和secret即可使用。
<div align="center">

![](http://ohlrxdl4p.bkt.clouddn.com/images/c2b0301e20170916074908.png)

</div>

实现过程：  
（1）右键，new>GUI FORM:
<div align="center">

![](http://ohlrxdl4p.bkt.clouddn.com/images/ab5b479520170916075700.png)

</div>

（2）在MySetting.form选好自己需要的按钮，即可在MySetting.java中实现逻辑
<div align="center">

![](http://ohlrxdl4p.bkt.clouddn.com/images/4377483c20170916075826.png)

</div>
（3）需要重写的方法
找出类的实现关系：
<div align="center">

![](http://ohlrxdl4p.bkt.clouddn.com/images/3b9c60a520170916080114.png)

</div>

idea sdk中配置了某些方法是可以不实现的，例如getHelpTopic、createComponent等，常用的方法一般如下：
```java
public interface UnnamedConfigurable {
    @Nullable
    JComponent createComponent();//打开设置的时候页面，如果需要侦听某些button，需要在这里配置，可无
    boolean isModified();//是否可以定义，一般为true，想写死的话就返回false
    void apply() throws ConfigurationException;//设置填好后点击apply或者ok，这里我们保存填写的东西
    default void reset() {//初始化，打开设置的初始化信息
    }
    default void disposeUIResources() {//关闭之后的资源
    }
}

```
保存填写的信息，idea sdk给我们提供了一个api，PropertiesComponent.getInstance()，感觉略像缓存，有人说保存在xml中，具体我也不太了解，有待深入。
（4）配置PasteImageFromClipboard的流程：
- 判断上传的图片是否为空，如果为空，则弹出提示框
- 判断当前文件是不是markdown的文件，如果是，进入编辑阶段
- 判断是否以简洁模式（即ctrl+v后不弹出选项框）
- 讲"![]()"配置到markdown中
- 操作成功。
有兴趣可以看看[代码](https://github.com/Zephery/mymdpastepic/blob/master/src/img2md/PasteImageFromClipboard.java)  

（5）七牛云的使用
使用七牛云的时候，需要将七牛云sdk以及其依赖的一个一个包都手动导进去，用不了maven。

<div align="center">

![](http://ohlrxdl4p.bkt.clouddn.com/images/1a137b8b20170917025333.png)

</div>

然后写一个QiniuUtil，用来上传文件：
```java
public class QiniuUtil {
    //自己的七牛
    private static Logger log = LoggerFactory.getLogger(QiniuUtil.class);
    public static final Configuration cfg = new Configuration(Zone.zone0());
    //...其他参数参考类注释
    public static final UploadManager uploadManager = new UploadManager(cfg);

    public static String getToken(String bucket) {//获取七牛的token
        System.out.println("qiniuyun");
        String access_key = PropertiesComponent.getInstance().getValue("ACCESS_KEY");
        String secret_key = PropertiesComponent.getInstance().getValue("SECRET_KEY");
        if (access_key != null && secret_key != null) {
            Auth auth = Auth.create(access_key, secret_key);
            String token = auth.uploadToken(bucket);
            return token;
        } else {
            return null;
        }

    }

    public static void putFile(String bucket, String key, String filePath) {//上传文件，第一个是bucket，第二个是文件名，第三个是文件的路径
        try {
            Response res = uploadManager.put(filePath, key, getToken(bucket));
            if (!res.isOK()) {
                log.error("Upload to qiniu failed;File path: " + filePath + ";Error: " + res.error);
            }
        } catch (QiniuException e) {
            e.printStackTrace();
            Response r = e.response;
            log.error(r.toString());
            try {
                log.error(r.bodyString());
            } catch (QiniuException e1) {
                log.error(e1.getMessage());
            }
        }
    }
}
```

之后，在PasteImageFromClipboard中添加保存的代码即可。
```java
QiniuUtil.putFile("images", "images/" + imagepath, imageFile.getPath());
```
如果想实现使用其他的，比如腾讯云、阿里云、又拍云这些，添加方式可以像七牛云一样，添加包，写个util即可，但是，当今的做云的越来越多，不能一一实现，我们可以提供一个模板，供开发者使用，只要自己实现了代码添加包即可。


## 4.拓展其他云  
本来是只做七牛云的，但是想了一想，只做七牛云好像没啥意思，想拓展腾讯云、阿里云、又拍云等等，顾提供了一个样本类，供开发者使用。
```java
public class Main {
    public boolean sendpic(String filepath) {//提供文件路径
        return true;//返回结果
    }
}
```

填写完代码之后，还仍需一个添加包的列表，添加完包之后进行调试，这里采用java的动态部署，生成动态类，规定主函数为Main，必须有个sendpic的方法，将图片的路径传过去，自己实现上传的代码
```java
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
            String imagepath = "1.png";//用来测试的图片
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
```
调用此动态类的动作在PasteImageFormClipboard中，
```java
try {
    File file = new File(System.getProperty("user.dir"));//类路径(包文件上一层)
    URL url = file.toURI().toURL();
    ClassLoader loader = new URLClassLoader(new URL[]{url});//创建类加载器
    System.out.println("loader");
    Class<?> cls = loader.loadClass("Main");//加载指定类，注意一定要带上类的包名
    Object obj = cls.newInstance();//初始化一个实例
    Method method = cls.getMethod("sendpic", String.class);//方法名和对应的参数类型
    method.invoke(obj, imagepath);//调用得到的上边的方法method
    //TODO 如果失败则弹出失败框
} catch (Exception ee) {
    ee.printStackTrace();
}
```
当然，拓展使用其他cdn仅仅是我的设想。。。。由于996，实在没时间去实现了，各位有兴趣可以去star或者fork一下，[链接点这](https://github.com/Zephery/mymdpastepic)

## 5.插件打包
写好代码之后，需要打包让自己或者别人使用，右键项目—>prepare plugin module xxx for deployment，然后在项目的目录就可以看到一个zip包，然后，在setting的plugin中install plugin from disk即可。

昨天，发现这个项目已经有人实现了，比我早了三天，还传到了jetbrains的公共仓库，感觉写的比我的好，大家可以使用一下
<div align="center">

![](http://ohlrxdl4p.bkt.clouddn.com/images/ce2378a820170917030909.png)

</div>

同时，欢迎访问我的[个人网站](http://www.wenzhihuai.com),要是能star一下我的网站的代码就更好了[网站代码](https://github.com/Zephery/newblog)，感谢