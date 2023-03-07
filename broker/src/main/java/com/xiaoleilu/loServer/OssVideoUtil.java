package com.xiaoleilu.loServer;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.green.extension.uploader.ClientUploader;
import com.aliyuncs.green.model.v20180509.VideoAsyncScanRequest;
import com.aliyuncs.green.model.v20180509.VideoAsyncScanResultsRequest;
import com.aliyuncs.green.model.v20180509.VideoSyncScanRequest;
import com.aliyuncs.green.model.v20180509.VoiceAsyncScanResultsRequest;
import com.aliyuncs.http.FormatType;
import com.aliyuncs.http.HttpResponse;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.mysql.cj.util.StringUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class OssVideoUtil {

    // 同步
    public static boolean getSyncFlag(File file) {
        try {
            return getSyncScene(file);
        }catch (Exception e){
            System.out.println("校验视频信息异常："+e.getMessage());
            return false;
        }
    }

    public static Boolean getSyncScene(File file) throws Exception{
        IClientProfile profile = DefaultProfile.getProfile("oss-beijing", "LTAI5tN4daQRKSBMx865uP8y", "A28GboYEcZTbPjuuXLxv8lHRdZJyGG");
        DefaultProfile.addEndpoint("oss-beijing", "Green", "green.cn-beijing.aliyuncs.com");
        IAcsClient client = new DefaultAcsClient(profile);

        VideoSyncScanRequest videoSyncScanRequest = new VideoSyncScanRequest();
        videoSyncScanRequest.setAcceptFormat(FormatType.JSON); // 指定API返回格式。
        videoSyncScanRequest.setMethod(com.aliyuncs.http.MethodType.POST); // 指定请求方法。

        /**
         * 如果您要检测的文件存储在本地服务器上，可以通过下述代码生成URL作为视频地址传递到服务端进行检测。
         */
        ClientUploader uploader = ClientUploader.getVideoClientUploader(profile, false);
        byte[] videoBytes = null;
        String url = null;
        try {
            // 读取本地文件作为二进制数据当做输入做为示例。实际使用中请直接替换成您的视频二进制数据。
            videoBytes = FileUtils.readFileToByteArray(file);
            // 上传到服务端。
            url = uploader.uploadBytes(videoBytes);
        } catch (Exception e) {
            System.out.println("upload file to server fail." + e.toString());
            return false;
        }

        List<Map<String, Object>> tasks = new ArrayList<Map<String, Object>>();
        Map<String, Object> task = new LinkedHashMap<String, Object>();
        task.put("dataId", UUID.randomUUID().toString());
        task.put("url", url);

        tasks.add(task);
        /**
         * 设置要检测的场景。计费依据此处传递的场景计算。
         * 视频默认1秒截取一帧，您可以自行控制截帧频率。收费按照视频的截帧数量以及每一帧的检测场景进行计算。
         * 举例：1分钟的视频截帧60张，检测色情（对应场景参数porn）和暴恐涉政（对应场景参数terrorism）2个场景，收费按照60张色情+60张暴恐涉政进行计算。
         */
        JSONObject data = new JSONObject();
        data.put("scenes", Arrays.asList("porn", "terrorism"));
        data.put("tasks", tasks);
        //data.put("callback", "http://www.aliyundoc.com/xxx.json");
        //data.put("seed", "yourPersonalSeed");

        videoSyncScanRequest.setHttpContent(data.toJSONString().getBytes("UTF-8"), "UTF-8", FormatType.JSON);

        /**
         * 请务必设置超时时间。
         */
        videoSyncScanRequest.setConnectTimeout(3000);
        videoSyncScanRequest.setReadTimeout(10000);
        try {
            HttpResponse httpResponse = client.doAction(videoSyncScanRequest);

            if (httpResponse.isSuccess()) {
                JSONObject scrResponse = JSON.parseObject(new String(httpResponse.getHttpContent(), "UTF-8"));
                System.out.println(JSON.toJSONString(scrResponse, true));
                int requestCode = scrResponse.getIntValue("code");
                // 每一个视频的检测结果。
                JSONArray taskResults = scrResponse.getJSONArray("data");
                if (200 == requestCode) {
                    for (Object taskResult : taskResults) {
                        // 单个视频的处理结果。
                        int taskCode = ((JSONObject) taskResult).getIntValue("code");
                        if (200 == taskCode) {
                            String taskId = ((JSONObject) taskResult).getString("taskId");
                            System.out.println("taskId:" + taskId);
                            // 检测结果。
                            JSONArray results = scrResponse.getJSONArray("results");
                            for (Object result : results) {
                                if(!"normal".equals(((JSONObject) result).getString("label")) || "block".equals(((JSONObject) result).getString("suggestion"))){
                                    return false;
                                }
                            }
                        } else {
                            // 单个视频处理失败，原因视具体的情况详细分析。
                            System.out.println("task process fail. task response:" + JSON.toJSONString(taskResult));
                            return false;
                        }
                    }
                } else {
                    /**
                     * 表明请求整体处理失败，原因视具体的情况详细分析。
                     */
                    System.out.println("the whole scan request failed. response:" + JSON.toJSONString(scrResponse));
                    return false;
                }
            } else {
                System.out.println("response not success. status:" + httpResponse.getStatus());
                return false;
            }
        } catch (ServerException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        }
        return true;
    }

    // 异步
    public static boolean getAsyncFlag(File file) {
        try {
            String url = getScene(file);
            if (StringUtils.isNullOrEmpty(url)) {
                return false;
            }
            // 请替换成您的AccessKey ID、AccessKey Secret。
            IClientProfile profile = DefaultProfile.getProfile("oss-beijing", "LTAI5tN4daQRKSBMx865uP8y", "A28GboYEcZTbPjuuXLxv8lHRdZJyGG");
            IAcsClient client = new DefaultAcsClient(profile);
            return pollingScanResult(client, url);
        }catch (Exception e){
            System.out.println("校验视频信息异常："+e.getMessage());
           return false;
        }
    }

    public static boolean pollingScanResult(IAcsClient client, String taskId) throws InterruptedException {
        int failCount = 0;
        boolean stop = false;
        do {
            JSONObject scanResult = getScanResult(client, taskId);
            if (scanResult == null || 200 != scanResult.getInteger("code")) {
                failCount++;
                System.out.println(taskId + ": get result fail, failCount=" + failCount);
                if (scanResult != null) {
                    System.out.println(taskId + ": errorMsg:" + scanResult.getString("msg"));
                }
                if (failCount > 20) {
                    break;
                }
                continue;
            }

            JSONArray taskResults = scanResult.getJSONArray("data");
            if (taskResults.isEmpty()) {
                System.out.println("failed");
                break;
            }

            for (Object taskResult : taskResults) {
                JSONObject result = (JSONObject) taskResult;
                Integer code = result.getInteger("code");
                if (280 == code) {
                    // 检测中
                    System.out.println(taskId + ": processing status: " + result.getString("msg"));
                } else if (200 == code) {
                    //System.out.println(taskId + ": ========== SUCCESS ===========");
                    System.out.println(JSON.toJSONString(scanResult, true));
                    if (taskResult != null) {
                        List<JSONObject> results = JSONArray.parseArray(result.getString("results"), JSONObject.class);
                        //判断返回类型不等于normal正常的都拦截下来了
                        for (JSONObject result1 : results) {
                            if(!"normal".equals(result1.getString("label"))
                                || "block".equals(result1.getString("suggestion"))){
                                stop = true;
                                return false;
                            }
                        }
                    }
                    stop = true;
                    return true;
                } else {
                    System.out.println(taskId + ": ========== FAILED ===========");
                    System.out.println(JSON.toJSONString(scanResult, true));
                    stop = true;
                    return false;
                }
            }
            // 设置每10秒查询一次。
            Thread.sleep(4 * 1000);
        } while (!stop);
        return stop;
    }

    private static JSONObject getScanResult(IAcsClient client, String taskId) {
        VideoAsyncScanResultsRequest getResultsRequest = new VideoAsyncScanResultsRequest();
        getResultsRequest.setAcceptFormat(FormatType.JSON); // 指定API返回格式。
        /*getResultsRequest.setMethod(com.aliyuncs.http.MethodType.POST); // 指定请求方法。
        getResultsRequest.setEncoding("utf-8");
        getResultsRequest.setRegionId("cn-beijing");


        List<Map<String, Object>> tasks = new ArrayList<Map<String, Object>>();
        Map<String, Object> task1 = new LinkedHashMap<String, Object>();
        task1.put("taskId", taskId);
        tasks.add(task1);*/

        /**
         * 请务必设置超时时间。
         */
        List<String> taskList = new ArrayList<String>();
        // 这里添加要查询的taskId。提交任务的时候需要自行保存taskId。
        taskList.add(taskId);


        try {
            getResultsRequest.setHttpContent(JSON.toJSONString(taskList).getBytes("UTF-8"), "UTF-8", FormatType.JSON);
            // getResultsRequest.setHttpContent(JSON.toJSONString(tasks).getBytes("UTF-8"), "UTF-8", FormatType.JSON);
            getResultsRequest.setConnectTimeout(3000);
            getResultsRequest.setReadTimeout(6000);

            HttpResponse httpResponse = client.doAction(getResultsRequest);
            if (httpResponse.isSuccess()) {
                return JSON.parseObject(new String(httpResponse.getHttpContent(), "UTF-8"));
            } else {
                System.out.println("response not success. status: " + httpResponse.getStatus());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getScene(File file) throws Exception{
        IClientProfile profile = DefaultProfile.getProfile("oss-beijing", "LTAI5tN4daQRKSBMx865uP8y", "A28GboYEcZTbPjuuXLxv8lHRdZJyGG");
        DefaultProfile.addEndpoint("oss-beijing", "Green", "green.cn-beijing.aliyuncs.com");
        IAcsClient client = new DefaultAcsClient(profile);

        VideoAsyncScanRequest videoAsyncScanRequest = new VideoAsyncScanRequest();
        videoAsyncScanRequest.setAcceptFormat(FormatType.JSON); // 指定API返回格式。
        videoAsyncScanRequest.setMethod(com.aliyuncs.http.MethodType.POST); // 指定请求方法。

        /**
         * 如果您要检测的文件存储在本地服务器上，可以通过下述代码生成URL作为视频地址传递到服务端进行检测。
         */
        ClientUploader uploader = ClientUploader.getVideoClientUploader(profile, false);
        byte[] videoBytes = null;
        String url = null;
        try {
            // 读取本地文件作为二进制数据当做输入做为示例。实际使用中请直接替换成您的视频二进制数据。
            videoBytes = FileUtils.readFileToByteArray(file);
            // 上传到服务端。
            url = uploader.uploadBytes(videoBytes);
        } catch (Exception e) {
            System.out.println("upload file to server fail." + e.toString());
            return null;
        }

        List<Map<String, Object>> tasks = new ArrayList<Map<String, Object>>();
        Map<String, Object> task = new LinkedHashMap<String, Object>();
        task.put("dataId", UUID.randomUUID().toString());
        task.put("url", url);

        tasks.add(task);
        /**
         * 设置要检测的场景。计费依据此处传递的场景计算。
         * 视频默认1秒截取一帧，您可以自行控制截帧频率。收费按照视频的截帧数量以及每一帧的检测场景进行计算。
         * 举例：1分钟的视频截帧60张，检测色情（对应场景参数porn）和暴恐涉政（对应场景参数terrorism）2个场景，收费按照60张色情+60张暴恐涉政进行计算。
         */
        JSONObject data = new JSONObject();
        data.put("scenes", Arrays.asList("porn", "terrorism"));
        data.put("tasks", tasks);
        data.put("callback", "http://www.aliyundoc.com/xxx.json");
        data.put("seed", "yourPersonalSeed");

        videoAsyncScanRequest.setHttpContent(data.toJSONString().getBytes("UTF-8"), "UTF-8", FormatType.JSON);

        /**
         * 请务必设置超时时间。
         */
        videoAsyncScanRequest.setConnectTimeout(3000);
        videoAsyncScanRequest.setReadTimeout(10000);
        try {
            HttpResponse httpResponse = client.doAction(videoAsyncScanRequest);

            if (httpResponse.isSuccess()) {
                JSONObject scrResponse = JSON.parseObject(new String(httpResponse.getHttpContent(), "UTF-8"));
                System.out.println(JSON.toJSONString(scrResponse, true));
                int requestCode = scrResponse.getIntValue("code");
                // 每一个视频的检测结果。
                JSONArray taskResults = scrResponse.getJSONArray("data");
                if (200 == requestCode) {
                    for (Object taskResult : taskResults) {
                        // 单个视频的处理结果。
                        int taskCode = ((JSONObject) taskResult).getIntValue("code");
                        if (200 == taskCode) {
                            // 保存taskId用于轮询结果。
                            String taskId = ((JSONObject) taskResult).getString("taskId");
                            System.out.println("taskId:" + taskId);
                            return taskId;
                        } else {
                            // 单个视频处理失败，原因视具体的情况详细分析。
                            System.out.println("task process fail. task response:" + JSON.toJSONString(taskResult));
                        }
                    }
                } else {
                    /**
                     * 表明请求整体处理失败，原因视具体的情况详细分析。
                     */
                    System.out.println("the whole scan request failed. response:" + JSON.toJSONString(scrResponse));
                }
            } else {
                System.out.println("response not success. status:" + httpResponse.getStatus());
            }
        } catch (ServerException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        }
        return null;
    }

}
