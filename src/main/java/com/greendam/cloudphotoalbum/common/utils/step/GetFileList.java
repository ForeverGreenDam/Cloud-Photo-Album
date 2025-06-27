package com.greendam.cloudphotoalbum.common.utils.step;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.greendam.cloudphotoalbum.exception.BusinessException;
import com.greendam.cloudphotoalbum.exception.ErrorCode;
import com.greendam.cloudphotoalbum.model.vo.ImageSearchVO;

import java.util.List;

/**
 * 获取文件列表
 * @author  ForeverGreenDam
 */
public class GetFileList {
    public static List<ImageSearchVO> getFileList(String fileListApi) {
        HttpResponse response = HttpUtil.createGet(fileListApi).execute();
        if(response==null){
            throw new BusinessException(ErrorCode.OPERATION_ERROR , "获取文件列表失败");
        }
        if (!response.isOk()){
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        String body = response.body();
        return processResponseBody(body);
    }

    private static List<ImageSearchVO> processResponseBody(String body) {
        JSONObject jsonObject = JSONUtil.parseObj(body);
        if(!jsonObject.containsKey("data")){
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取文件列表失败");
        }
        JSONObject data = jsonObject.getJSONObject("data");
        if(!jsonObject.containsKey("list")){
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取文件列表失败");
        }
        JSONArray list = data.getJSONArray("list");
        return JSONUtil.toList(list, ImageSearchVO.class);
    }
}
