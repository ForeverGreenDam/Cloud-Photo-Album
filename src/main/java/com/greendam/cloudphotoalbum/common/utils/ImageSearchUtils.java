package com.greendam.cloudphotoalbum.common.utils;

import com.greendam.cloudphotoalbum.common.utils.step.GetFileList;
import com.greendam.cloudphotoalbum.common.utils.step.GetFileListApi;
import com.greendam.cloudphotoalbum.common.utils.step.GetImagePageUrlApi;
import com.greendam.cloudphotoalbum.model.vo.ImageSearchVO;

import java.util.List;

/**
 * 以图搜图工具类
 * @author ForeverGreenDam
 */

public class ImageSearchUtils {

    public static List<ImageSearchVO> searchImage(String url) {
        //1.获取以图搜图界面url
        String imagePageUrl = GetImagePageUrlApi.getImagePageUrl(url);
        //2.从上述URL中获取文件列表api
        String fileListApi = GetFileListApi.getFileListApi(imagePageUrl);
        //从文件列表api中获取文件列表
        return GetFileList.getFileList(fileListApi);
    }
}

