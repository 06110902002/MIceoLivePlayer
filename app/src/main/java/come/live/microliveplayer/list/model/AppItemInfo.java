package come.live.microliveplayer.list.model;

import android.graphics.drawable.Drawable;

import com.google.gson.annotations.SerializedName;

/**
 * Copyright@NIO Since 2014
 * CreateTime  : 2022/10/17 18:54
 * Author      : rambo.liu
 * Description :
 */
public class AppItemInfo implements ViewDataType {

    @SerializedName("pageIdx")
    private Integer pageIdx;
    @SerializedName("activityInfo")
    private String activityInfo;
    @SerializedName("pkgName")
    private String pkgName;
    @SerializedName("name")
    private String name;
    @SerializedName("icon")
    private String icon;
    @SerializedName("itemType")
    private int itemType;

    public Integer getPageIdx() {
        return pageIdx;
    }

    public void setPageIdx(Integer pageIdx) {
        this.pageIdx = pageIdx;
    }

    public String getActivityInfo() {
        return activityInfo;
    }

    public void setActivityInfo(String activityInfo) {
        this.activityInfo = activityInfo;
    }

    public String getPkgName() {
        return pkgName;
    }

    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }


    public void setItemType(int type) {
        this.itemType = type;
    }

    /**
     * 子类需要重载
     * @return
     */
    @Override
    public int getItemType() {
        return itemType;
    }
}
