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
import cn.wildfirechat.pojos.OutputGroupMemberList;
import cn.wildfirechat.pojos.OutputSearchMessageList;
import cn.wildfirechat.pojos.PojoSearchMessage;
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
import java.util.ArrayList;
import java.util.List;


@Route(APIPath.SEARCH_MESSAGE)
@HttpMethod("POST")
public class SearchMessageAction extends AdminAction {

    @Override
    public boolean isTransactionAction() {
        return true;
    }

    @Override
    public boolean action(Request request, Response response) {
        if (request.getNettyRequest() instanceof FullHttpRequest) {
            String content = getRequestBody(request.getNettyRequest(), String.class);
            if (!StringUtils.isNullOrEmpty(content)) {
                JSONObject jsonObject = JSONUtil.parseObj(content);
                List<PojoSearchMessage> list = messagesStore.searchMessage(jsonObject.getStr("message"));
                OutputSearchMessageList out = new OutputSearchMessageList();
                out.setMessages(list);
                RestResult result = RestResult.ok(out);
                setResponseContent(result, response);
            } else {
                setResponseContent(RestResult.resultOf(ErrorCode.INVALID_PARAMETER), response);
            }
        }
        return true;
    }
}
