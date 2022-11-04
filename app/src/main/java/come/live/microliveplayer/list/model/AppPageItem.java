package come.live.microliveplayer.list.model;

import java.util.List;

import come.live.microliveplayer.config.Constants;

/**
 * Copyright@NIO Since 2014
 * CreateTime  : 2022/10/18 14:27
 * Author      : rambo.liu
 * Description :
 */
public class AppPageItem implements ViewDataType {

    private boolean isWholePage;

    private List<AppItemInfo> appItemInfo;

    public List<AppItemInfo> getAppItemInfo() {
        return appItemInfo;
    }
    public void setAppItemInfo(List<AppItemInfo> appItemInfo) {
        this.appItemInfo = appItemInfo;
    }

    public void setWholePage(boolean page) {
        isWholePage = page;
    }

    @Override
    public int getItemType() {
        return isWholePage ? Constants.APP_WHOLE_PAGE : Constants.APP_PAGE_ITEM;
    }
}
