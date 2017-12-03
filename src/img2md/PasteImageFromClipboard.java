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
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

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
        String callback_url = PropertiesComponent.getInstance().getValue("CALLBACK_URL");
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

    }

    private void insertImageElement(final @NotNull Editor editor, String imageurl) {
        String pictrueurl = "![](" + imageurl + ")";
        Runnable r = () -> EditorModificationUtil.insertStringAtCaret(editor, Common.COMMON_CODE.replace("PICTUREURL", pictrueurl));
        WriteCommandAction.runWriteCommandAction(editor.getProject(), r);
    }
}
