/*
 * This file is part of the Wildfire Chat package.
 * (c) Heavyrain2012 <heavyrain.lee@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package com.xiaoleilu.loServer.action.admin;

import cn.wildfirechat.common.APIPath;
import cn.wildfirechat.common.ErrorCode;
import cn.wildfirechat.pojos.InputLog;
import com.mysql.cj.util.StringUtils;
import com.xiaoleilu.hutool.json.JSONObject;
import com.xiaoleilu.hutool.json.JSONUtil;
import com.xiaoleilu.loServer.RestResult;
import com.xiaoleilu.loServer.annotation.HttpMethod;
import com.xiaoleilu.loServer.annotation.Route;
import com.xiaoleilu.loServer.handler.Request;
import com.xiaoleilu.loServer.handler.Response;
import io.moquette.spi.impl.Utils;
import io.netty.handler.codec.http.FullHttpRequest;

import java.nio.charset.StandardCharsets;


@Route(APIPath.Log_Save)
@HttpMethod("POST")
public class SaveLogAction extends AdminAction {

    @Override
    public boolean isTransactionAction() {
        return true;
    }

    @Override
    public boolean action(Request request, Response response) {
        if (request.getNettyRequest() instanceof FullHttpRequest) {
            InputLog logPojo = getRequestBody(request.getNettyRequest(), InputLog.class);
            if (null != logPojo) {
                ErrorCode errorCode = messagesStore.saveLog(logPojo);
                setResponseContent(RestResult.resultOf(errorCode), response);
            } else {
                setResponseContent(RestResult.resultOf(ErrorCode.INVALID_PARAMETER), response);
            }
            /*FullHttpRequest fullHttpRequest = (FullHttpRequest) request.getNettyRequest();
            byte[] bytes = Utils.readBytesAndRewind(fullHttpRequest.content());
            String content = new String(bytes, StandardCharsets.UTF_8);
            if (!StringUtils.isNullOrEmpty(content)) {
                JSONObject jsonObject = JSONUtil.parseObj(content);
                ErrorCode errorCode = messagesStore.saveLog(jsonObject.getInt("type"),
                    jsonObject.getStr("remark"),jsonObject.getInt("port"),
                    jsonObject.getStr("ip"), jsonObject.getStr("serverIp"),
                    jsonObject.getStr("phone"), jsonObject.getStr("mac"),
                    jsonObject.getBool("flag"),jsonObject.getStr("model"),
                    jsonObject.getStr("messageId"));
                setResponseContent(RestResult.resultOf(errorCode), response);
            } else {
                setResponseContent(RestResult.resultOf(ErrorCode.INVALID_PARAMETER), response);
            }*/

        }
        return true;
    }

    public static void main(String[] args) {
        String str = "{\"ip\":\"0:0:0:0:0:0:0:1\",\"port\":8888,\"type\":1,\"flag\":true,\"reason\":\"success\",\"phone\":\"13696134454\"}";
        InputLog logPojo = JSONUtil.toBean(JSONUtil.parseObj(str), InputLog.class);
        System.out.println();
    }
}
