package com.zephery.img2md;

import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zepherywen
 */
public class QiniuUtil {
    private static Logger log = LoggerFactory.getLogger(QiniuUtil.class);

    public static void putFileBytes(String bucket, String key, byte[] bytes) {
        Configuration cfg = new Configuration(Region.region0());
        UploadManager uploadManager = new UploadManager(cfg);
        System.out.println("qiniuyun");
        String accessKey = "QN3U7hRV4WYTmNSPJLVGCfuthzwN2MsDnPojtaZ4";
        String secretKey = "4qqIC6qDc4-KNfSqbG3WOvgSEN8mZx5zEDOsAdo8";
        Auth auth = Auth.create(accessKey, secretKey);
        String upToken = auth.uploadToken(bucket);
        try {
            Response response = uploadManager.put(bytes, key, upToken);
            //解析上传成功的结果
            DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
            System.out.println(putRet.key);
            System.out.println(putRet.hash);
        } catch (QiniuException ex) {
            Response r = ex.response;
            System.err.println(r.toString());
            try {
                System.err.println(r.bodyString());
            } catch (QiniuException ex2) {
                ex2.printStackTrace();
            }
        }
    }
}
