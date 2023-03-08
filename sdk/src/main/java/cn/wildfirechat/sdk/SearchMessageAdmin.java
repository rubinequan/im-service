package cn.wildfirechat.sdk;

import cn.wildfirechat.common.APIPath;
import cn.wildfirechat.pojos.InputLog;
import cn.wildfirechat.pojos.OutputSearchMessageList;
import cn.wildfirechat.sdk.model.IMResult;
import cn.wildfirechat.sdk.utilities.AdminHttpUtils;

public class SearchMessageAdmin {

    public static IMResult<OutputSearchMessageList> searchMessage(String messaged) throws Exception {
        String path = APIPath.SEARCH_MESSAGE;
        return AdminHttpUtils.httpJsonPost(path, messaged, OutputSearchMessageList.class);
    }
}
