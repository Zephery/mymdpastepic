package img2md;

import com.intellij.ide.util.PropertiesComponent;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QiniuUtil {
    //自己的七牛
//    private static final String ACCESS_KEY = "QN3U7hRV4WYTmNSPJLVGCfuthzwN2MsDnPojtaZ4";
//    private static final String SECRET_KEY = "4qqIC6qDc4-KNfSqbG3WOvgSEN8mZx5zEDOsAdo8";
    private static Logger log = LoggerFactory.getLogger(QiniuUtil.class);
    private static final Configuration cfg = new Configuration(Zone.zone0());
    //...其他参数参考类注释
    private static final UploadManager uploadManager = new UploadManager(cfg);

    private static String getToken(String bucket) {
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

    public static void putFile(String bucket, String key, String filePath) {
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
