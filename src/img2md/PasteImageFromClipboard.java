package img2md;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

import static img2md.ImageUtils.*;

public class PasteImageFromClipboard extends AnAction {
    private static final String DOC_BASE_NAME = "{document_name}";

    @Override
    public void actionPerformed(AnActionEvent e) {
        System.out.println("加载action" + new Date().toString());
        Image imageFromClipboard = getImageFromClipboard();
        // deterimine save path for the image
        Editor ed = e.getData(PlatformDataKeys.EDITOR);
        if (ed == null) {
            return;
        }
        if (imageFromClipboard == null) {
            DialogBuilder builder = new DialogBuilder();
            builder.setCenterPanel(new JLabel("Clipboard does not contain any image"));
            builder.setDimensionServiceKey("PasteImageFromClipboard.NoImage");
            builder.setTitle("No image in Clipboard");
            builder.removeAllActions();
            builder.addOkAction();
            builder.show();
            return;
        }
        boolean yes = PropertiesComponent.getInstance().getBoolean("yes");
        String callback_url = PropertiesComponent.getInstance().getValue("CALLBACK_URL");
        if (yes) {
            Document currentDoc = FileEditorManager.getInstance(ed.getProject()).getSelectedTextEditor().getDocument();
            VirtualFile currentFile = FileDocumentManager.getInstance().getFile(currentDoc);
            try {
                File curDocument = new File(currentFile.getPath());
                // add option to rescale image on the fly
                BufferedImage bufferedImage = toBufferedImage(imageFromClipboard);
                if (bufferedImage == null) return;
                String imageName = "";
                String mdBaseName = curDocument.getName().replace(".md", "").replace(".Rmd", "");
                String dirPattern = "images";
                File imageDir = new File(curDocument.getParent(), dirPattern.replace(DOC_BASE_NAME, mdBaseName));
                if (!imageDir.exists() || !imageDir.isDirectory()) imageDir.mkdirs();
                File imageFile = new File(imageDir, imageName + ".png");
                save(bufferedImage, imageFile, "png");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
                String temp = sdf.format(new Date());
                String imagepath = imageName + temp + ".png";
                String imageurl = callback_url + imagepath;
                QiniuUtil.putFile("images", "images/" + imagepath, imageFile.getPath());
                insertImageElement(ed, imageurl);

            } catch (Exception eee) {
                eee.printStackTrace();
            }

        } else {
            // from http://stackoverflow.com/questions/17915688/intellij-plugin-get-code-from-current-open-file
            Document currentDoc = FileEditorManager.getInstance(ed.getProject()).getSelectedTextEditor().getDocument();
            VirtualFile currentFile = FileDocumentManager.getInstance().getFile(currentDoc);
            File curDocument = new File(currentFile.getPath());
            // add option to rescale image on the fly
            BufferedImage bufferedImage = toBufferedImage(imageFromClipboard);
            if (bufferedImage == null) return;
            Dimension dimension = new Dimension(bufferedImage.getWidth(), bufferedImage.getHeight());
            ImageInsertSettingsPanel insertSettingsPanel = showDialog(curDocument, dimension);
            if (insertSettingsPanel == null) return;
            String imageName = insertSettingsPanel.getNameInput().getText();
            boolean whiteAsTransparent = insertSettingsPanel.getWhiteCheckbox().isSelected();
            boolean roundCorners = insertSettingsPanel.getRoundCheckbox().isSelected();
            double scalingFactor = ((Integer) insertSettingsPanel.getScaleSpinner().getValue()) * 0.01;
            if (whiteAsTransparent) {
                bufferedImage = toBufferedImage(whiteToTransparent(bufferedImage));
            }
            if (roundCorners) {
                bufferedImage = toBufferedImage(makeRoundedCorner(bufferedImage, 20));
            }
            if (scalingFactor != 1) {
                bufferedImage = scaleImage(bufferedImage,
                        (int) Math.round(bufferedImage.getWidth() * scalingFactor),
                        (int) Math.round(bufferedImage.getHeight() * scalingFactor));
            }
            // make selectable
//        File imageDir = new File(curDocument.getParent(), ".images");
            String mdBaseName = curDocument.getName().replace(".md", "").replace(".Rmd", "");
//        File imageDir = new File(curDocument.getParent(), "."+ mdBaseName +"_images");
            String dirPattern = insertSettingsPanel.getDirectoryField().getText();
            File imageDir = new File(curDocument.getParent(), dirPattern.replace(DOC_BASE_NAME, mdBaseName));
            if (!imageDir.exists() || !imageDir.isDirectory()) imageDir.mkdirs();
            File imageFile = new File(imageDir, imageName + ".png");
            // todo should we silently override the image if it is already present?
            save(bufferedImage, imageFile, "png");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
            String temp = sdf.format(new Date());
            String imagepath = imageName + temp + ".png";
            QiniuUtil.putFile("images", "images/" + imagepath, imageFile.getPath());
            String imageurl = callback_url + imagepath;
            try {
                File file = new File(System.getProperty("user.dir"));//类路径(包文件上一层)
                URL url = file.toURI().toURL();
                ClassLoader loader = new URLClassLoader(new URL[]{url});//创建类加载器
                System.out.println("loader");
                Class<?> cls = loader.loadClass("Main");//加载指定类，注意一定要带上类的包名
                Object obj = cls.newInstance();//初始化一个实例
                Method method = cls.getMethod("sendpic", String.class);//方法名和对应的参数类型
                method.invoke(obj, imagepath);//调用得到的上边的方法method
            } catch (Exception ee) {
                ee.printStackTrace();
            }

            // inject image element current markdown document
            insertImageElement(ed, imageurl);
            // https://intellij-support.jetbrains.com/hc/en-us/community/posts/206144389-Create-virtual-file-from-file-path
            VirtualFile fileByPath = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(imageFile);
            assert fileByPath != null;
            AbstractVcs usedVcs = ProjectLevelVcsManager.getInstance(ed.getProject()).getVcsFor(fileByPath);
            if (usedVcs != null && usedVcs.getCheckinEnvironment() != null) {
                usedVcs.getCheckinEnvironment().scheduleUnversionedFilesForAddition(Collections.singletonList(fileByPath));
            }
            // update directory pattern preferences for file and globally
            PropertiesComponent.getInstance().setValue("PI__LAST_DIR_PATTERN", dirPattern);
            PropertiesComponent.getInstance().setValue("PI__DIR_PATTERN_FOR_" + currentFile.getPath(), dirPattern);
        }
    }

    private void insertImageElement(final @NotNull Editor editor, String imageurl) {
        Runnable r = () -> EditorModificationUtil.insertStringAtCaret(editor, "![](" + imageurl + ")");
        WriteCommandAction.runWriteCommandAction(editor.getProject(), r);
    }

    // for more examples see
//    http://www.programcreek.com/java-api-examples/index.php?api=com.intellij.openapi.ui.DialogWrapper
    private static ImageInsertSettingsPanel showDialog(File curDocument, Dimension imgDim) {
        DialogBuilder builder = new DialogBuilder();
        ImageInsertSettingsPanel contentPanel = new ImageInsertSettingsPanel();
        ChangeListener listener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                double scalingFactor = (Integer) contentPanel.getScaleSpinner().getValue() * 0.1;

                JLabel targetSizeLabel = contentPanel.getTargetSizeLabel();

                if (scalingFactor == 100) {
                    targetSizeLabel.setText(imgDim.getWidth() + " x " + imgDim.getHeight());

                } else {
                    long newWidth = Math.round(imgDim.getWidth() * scalingFactor);
                    long newHeight = Math.round(imgDim.getHeight() * scalingFactor);

                    targetSizeLabel.setText(newWidth + " x " + newHeight);
                }
            }
        };
        listener.stateChanged(null);
        contentPanel.getScaleSpinner().addChangeListener(listener);
        String dirPattern = "images"; //TODO 目录直接修改为images
        contentPanel.getDirectoryField().setText(dirPattern);
        contentPanel.getNameInput().setText(UUID.randomUUID().toString().substring(0, 8));
        builder.setCenterPanel(contentPanel);
        builder.setDimensionServiceKey("GrepConsoleSound");
        builder.setTitle("Paste Image Settings");
        builder.removeAllActions();
        builder.addOkAction();
        builder.addCancelAction();
        builder.setPreferredFocusComponent(contentPanel.getNameInput());
        boolean isOk = builder.show() == DialogWrapper.OK_EXIT_CODE;
        if (!isOk) {
            return null;
        }
        return contentPanel;
    }
}
