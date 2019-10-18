package org.sakaiproject.elfinder.sakai.ezproxy;

import cn.bluejoe.elfinder.service.FsItem;
import cn.bluejoe.elfinder.service.FsVolume;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import org.sakaiproject.site.api.SitePage;

/**
 * @author bjones86
 */
@EqualsAndHashCode
public class EZProxyFsItem implements FsItem
{
    @Getter private final FsVolume volume;
    @Getter private final String pageID;
    @EqualsAndHashCode.Exclude @Getter private final SitePage page;

    public EZProxyFsItem( FsVolume volume, SitePage page )
    {
        this.volume = volume;
        this.page = page;
        this.pageID = page == null ? null : page.getId();
    }
}
