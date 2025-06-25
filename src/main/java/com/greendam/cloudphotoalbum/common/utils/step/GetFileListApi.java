package com.greendam.cloudphotoalbum.common.utils.step;

import com.greendam.cloudphotoalbum.exception.BusinessException;
import com.greendam.cloudphotoalbum.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 获取文件列表API
 * @author ForeverGreenDam
 */
@Slf4j
public class GetFileListApi {
    public static String getFileListApi(String imagePageUrl) {
        String firstUrl = extractFirstUrl(imagePageUrl);
        if (firstUrl == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"接口调用失败");
        }
        return firstUrl;
    }

    private static String extractFirstUrl(String html) {
        try {
            Document doc = Jsoup.connect(html).get();
            // 查找包含window.cardData的script标签
            for (Element script : doc.select("script")) {
                String scriptContent = script.html();
                if (scriptContent.contains("window.cardData")) {
                    // 使用正则表达式提取firstUrl的值
                    Pattern pattern = Pattern.compile("\"firstUrl\"\\s*:\\s*\"(.*?)\"");
                    Matcher matcher = pattern.matcher(scriptContent);
                    if (matcher.find()) {
                        return matcher.group(1);
                    }
                }
            }
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"接口调用失败");
        }
        return null;
    }

    private static String unescapeUrl(String escapedUrl) {
        return escapedUrl.replace("\\/", "/");
    }

}
