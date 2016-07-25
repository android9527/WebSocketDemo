package org.websocket.demo.request;

import org.websocket.demo.util.Constant;

/**
 * Created by chenfeiyue on 16/7/18.
 */
public class BaseRequest {
    public BaseRequest() {
        setPkg_id();
    }

    public String pkg_id;

    public String getPkg_id() {
        return pkg_id;
    }

    public void setPkg_id() {
        this.pkg_id = Constant.messageId + "";
        Constant.messageId++;
        if (Constant.messageId >= Short.MAX_VALUE) {
            Constant.messageId = 1;
        }
    }
}
