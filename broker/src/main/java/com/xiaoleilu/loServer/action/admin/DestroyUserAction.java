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
import cn.wildfirechat.pojos.InputDestroyUser;
import cn.wildfirechat.proto.WFCMessage;
import com.xiaoleilu.hutool.json.JSONObject;
import com.xiaoleilu.hutool.json.JSONUtil;
import com.xiaoleilu.loServer.RestResult;
import com.xiaoleilu.loServer.annotation.HttpMethod;
import com.xiaoleilu.loServer.annotation.Route;
import com.xiaoleilu.loServer.handler.Request;
import com.xiaoleilu.loServer.handler.Response;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.internal.StringUtil;
import win.liyufan.im.IMTopic;


@Route(APIPath.Destroy_User)
@HttpMethod("POST")
public class DestroyUserAction extends AdminAction {

    @Override
    public boolean isTransactionAction() {
        return true;
    }

    @Override
    public boolean action(Request request, Response response) {
        if (request.getNettyRequest() instanceof FullHttpRequest) {
            InputDestroyUser inputDestroyUser = getRequestBody(request.getNettyRequest(), InputDestroyUser.class);
            if (inputDestroyUser != null
                && !StringUtil.isNullOrEmpty(inputDestroyUser.getUserId())) {
                String userId = inputDestroyUser.getUserId();
                // 这里不知道为啥，里面的userId是 {"userId" : ""} 格式，所以需要转一次
                if (inputDestroyUser.getUserId().contains("userId")) {
                    JSONObject jsonObject = JSONUtil.parseObj(inputDestroyUser.getUserId());
                    userId = jsonObject.getStr("userId");
                }
                WFCMessage.IDBuf idBuf = WFCMessage.IDBuf.newBuilder().setId(userId).build();
                sendApiMessage(response, userId, IMTopic.DestroyUserTopic, idBuf.toByteArray(), result -> {
                    ErrorCode errorCode1 = ErrorCode.fromCode(result[0]);
                    return new Result(errorCode1);
                });
                return false;
            } else {
                setResponseContent(RestResult.resultOf(ErrorCode.INVALID_PARAMETER), response);
            }

        }
        return true;
    }
}
