package org.sakaiproject.elfinder.sakai.ezproxy;

import cn.bluejoe.elfinder.service.FsItem;
import cn.bluejoe.elfinder.service.FsVolume;
import java.util.Objects;
import org.apache.commons.lang.StringUtils;
import org.sakaiproject.site.api.SitePage;

/**
 * Represents a linkable EZProxy module in the elfinder
 */
public class EZProxyFsItem implements FsItem {
    private final FsVolume fsVolume;

    private final SitePage page;
    private final String pageId;
    
    public EZProxyFsItem(FsVolume fsVolume, SitePage page){
        this.fsVolume = fsVolume;
        this.page = page;
        this.pageId = page == null ? null : page.getId();
    }
    @Override
    public FsVolume getVolume() {
        return fsVolume;
    }

    public String getPageId()
    {
        return pageId;
    }

    public SitePage getPage()
    {
        return page;
    }

    @Override
    public boolean equals(Object object){
        if(this == object){
            return true;
        }
        if(object == null || getClass() != object.getClass()){
            return false;
        }
        EZProxyFsItem second = (EZProxyFsItem)object;
        if (!StringUtils.equals(pageId, second.getPageId()))
        {
            return false;
        }
        return Objects.equals(fsVolume, second.fsVolume);
    }
    
    @Override
    public int hashCode() {
        int result = pageId != null ? pageId.hashCode() : 0;
        result = 31 * result + (fsVolume != null ? fsVolume.hashCode() : 0);
        return result;
    }
}
