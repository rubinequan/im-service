package cn.wildfirechat.sdk;

import cn.wildfirechat.common.APIPath;
import cn.wildfirechat.pojos.InputLog;
import cn.wildfirechat.sdk.model.IMResult;
import cn.wildfirechat.sdk.utilities.AdminHttpUtils;

public class LogAdmin {

    public static IMResult<Void> saveLog(InputLog logPojo) throws Exception {
        String path = APIPath.Log_Save;
        return AdminHttpUtils.httpJsonPost(path, logPojo, Void.class);
    }
}
