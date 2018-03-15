package img2md;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.Random;

import static img2md.ImageUtils.getImageFromClipboard;
import static img2md.ImageUtils.toBufferedImage;

public class PasteImageFromClipboard extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        System.out.println("加载action" + new Date().toString());
        Image imageFromClipboard = getImageFromClipboard();
        // deterimine save path for the image
        Editor ed = e.getData(PlatformDataKeys.EDITOR);
        if (ed == null) {
            return;
        }
        Document currentDoc = Objects.requireNonNull(FileEditorManager.getInstance(Objects.requireNonNull(ed.getProject())).getSelectedTextEditor()).getDocument();
        VirtualFile currentFile = FileDocumentManager.getInstance().getFile(currentDoc);
        try {
            assert currentFile != null;
            BufferedImage bufferedImage = toBufferedImage(imageFromClipboard);
            byte[] bytes = ImageUtils.imageToBytes("png", bufferedImage);
            if (bufferedImage == null) return;
            String imageName = "";
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
            String temp = sdf.format(new Date()) + String.valueOf(new Random().nextInt());
            String imagepath = imageName + temp.replaceAll("-", "") + ".png";
            String callback_url = "http://image.wenzhihuai.com/images/";
            String imageurl = callback_url + imagepath;
            int ranNum = new Random().nextInt(5);   //0,1,2
            if (ranNum < 2) {
                new Thread(() -> QiniuUtil.putFileBytes("images", "images/" + imagepath, bytes)).start();
                insertImageElement(ed, imageurl);
            } else {
                String upyunCallUrl = "https://upyuncdn.wenzhihuai.com/";
                new Thread(() -> {
                    try {
                        UpYunUtil.uploadFileBytes(bytes, imagepath);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }).start();
                insertImageElement(ed, upyunCallUrl + imagepath);
            }
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
