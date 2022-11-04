package come.live.microliveplayer.sdp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

import come.live.microliveplayer.list.model.AppItemInfo;

/**
 * Copyright@NIO Since 2014
 * CreateTime  : 2022/11/2 15:32
 * Author      : rambo.liu
 * Description :
 */
public class AppItemSdp implements Serializable {


    @SerializedName("width")
    private Integer width;
    @SerializedName("height")
    private Integer height;
    @SerializedName("pageCount")
    private Integer pageCount;
    @SerializedName("isPad")
    private Boolean isPad;
    @SerializedName("appInfo")
    private AppInfoDTO appInfo;
    @SerializedName("menuData")
    private List<AppItemInfo> menuData;

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Integer getPageCount() {
        return pageCount;
    }

    public void setPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }

    public Boolean isIsPad() {
        return isPad;
    }

    public void setIsPad(Boolean isPad) {
        this.isPad = isPad;
    }

    public AppInfoDTO getAppInfo() {
        return appInfo;
    }

    public void setAppInfo(AppInfoDTO appInfo) {
        this.appInfo = appInfo;
    }

    public List<AppItemInfo> getMenuData() {
        return menuData;
    }

    public void setMenuData(List<AppItemInfo> menuData) {
        this.menuData = menuData;
    }

    public static class AppInfoDTO {
        @SerializedName("page1")
        private List<AppItemInfo> page1;
        @SerializedName("page2")
        private List<AppItemInfo> page2;

        public List<AppItemInfo> getPage1() {
            return page1;
        }

        public void setPage1(List<AppItemInfo> page1) {
            this.page1 = page1;
        }

        public List<AppItemInfo> getPage2() {
            return page2;
        }

        public void setPage2(List<AppItemInfo> page2) {
            this.page2 = page2;
        }

//        public static class Page1DTO {
//            @SerializedName("pageIdx")
//            private Integer pageIdx;
//            @SerializedName("activityInfo")
//            private String activityInfo;
//            @SerializedName("pkgName")
//            private String pkgName;
//            @SerializedName("name")
//            private String name;
//            @SerializedName("icon")
//            private String icon;
//
//            public Integer getPageIdx() {
//                return pageIdx;
//            }
//
//            public void setPageIdx(Integer pageIdx) {
//                this.pageIdx = pageIdx;
//            }
//
//            public String getActivityInfo() {
//                return activityInfo;
//            }
//
//            public void setActivityInfo(String activityInfo) {
//                this.activityInfo = activityInfo;
//            }
//
//            public String getPkgName() {
//                return pkgName;
//            }
//
//            public void setPkgName(String pkgName) {
//                this.pkgName = pkgName;
//            }
//
//            public String getName() {
//                return name;
//            }
//
//            public void setName(String name) {
//                this.name = name;
//            }
//
//            public String getIcon() {
//                return icon;
//            }
//
//            public void setIcon(String icon) {
//                this.icon = icon;
//            }
//        }
//
//        public static class Page2DTO {
//            @SerializedName("pageIdx")
//            private Integer pageIdx;
//            @SerializedName("activityInfo")
//            private String activityInfo;
//            @SerializedName("pkgName")
//            private String pkgName;
//            @SerializedName("name")
//            private String name;
//            @SerializedName("icon")
//            private String icon;
//
//            public Integer getPageIdx() {
//                return pageIdx;
//            }
//
//            public void setPageIdx(Integer pageIdx) {
//                this.pageIdx = pageIdx;
//            }
//
//            public String getActivityInfo() {
//                return activityInfo;
//            }
//
//            public void setActivityInfo(String activityInfo) {
//                this.activityInfo = activityInfo;
//            }
//
//            public String getPkgName() {
//                return pkgName;
//            }
//
//            public void setPkgName(String pkgName) {
//                this.pkgName = pkgName;
//            }
//
//            public String getName() {
//                return name;
//            }
//
//            public void setName(String name) {
//                this.name = name;
//            }
//
//            public String getIcon() {
//                return icon;
//            }
//
//            public void setIcon(String icon) {
//                this.icon = icon;
//            }
//        }
    }

//    public static class MenuDataDTO {
//        @SerializedName("pageIdx")
//        private Integer pageIdx;
//        @SerializedName("activityInfo")
//        private String activityInfo;
//        @SerializedName("pkgName")
//        private String pkgName;
//        @SerializedName("name")
//        private String name;
//        @SerializedName("icon")
//        private String icon;
//
//        public Integer getPageIdx() {
//            return pageIdx;
//        }
//
//        public void setPageIdx(Integer pageIdx) {
//            this.pageIdx = pageIdx;
//        }
//
//        public String getActivityInfo() {
//            return activityInfo;
//        }
//
//        public void setActivityInfo(String activityInfo) {
//            this.activityInfo = activityInfo;
//        }
//
//        public String getPkgName() {
//            return pkgName;
//        }
//
//        public void setPkgName(String pkgName) {
//            this.pkgName = pkgName;
//        }
//
//        public String getName() {
//            return name;
//        }
//
//        public void setName(String name) {
//            this.name = name;
//        }
//
//        public String getIcon() {
//            return icon;
//        }
//
//        public void setIcon(String icon) {
//            this.icon = icon;
//        }
//    }
}
