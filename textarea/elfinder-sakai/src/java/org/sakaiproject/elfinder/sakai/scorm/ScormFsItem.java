package org.sakaiproject.elfinder.sakai.scorm;

import cn.bluejoe.elfinder.service.FsItem;
import cn.bluejoe.elfinder.service.FsVolume;
import java.util.Objects;
import org.apache.commons.lang.StringUtils;

/**
 * Represents a linkable Scorm module in the elfinder
 */
public class ScormFsItem implements FsItem {
    private final FsVolume fsVolume;
    private final String toolId;
    private final String contentPackageId;
    private final String resourceId;
    private final String title;
    
    public ScormFsItem(FsVolume fsVolume, String toolId, String contentPackageId, String resourceId, String title){
        this.fsVolume = fsVolume;
        this.toolId = toolId;
        this.contentPackageId = contentPackageId;
        this.resourceId = resourceId;
        this.title = title;
    }
    @Override
    public FsVolume getVolume() {
        return fsVolume;
    }

    public String getToolId()
    {
        return toolId;
    }

    public String getContentPackageId()
    {
        return contentPackageId;
    }

    public String getResourceId()
    {
        return resourceId;
    }
    
    public String getTitle(){
        return title;
    }
    
    @Override
    public boolean equals(Object object){
        if(this == object){
            return true;
        }
        if(object == null || getClass() != object.getClass()){
            return false;
        }
        ScormFsItem second = (ScormFsItem)object;
        if (!StringUtils.equals(contentPackageId, second.getContentPackageId()))
        {
            return false;
        }
        return Objects.equals(fsVolume, second.fsVolume);
    }
    
    @Override
    public int hashCode() {
        int result = contentPackageId != null ? contentPackageId.hashCode() : 0;
        result = 31 * result + (fsVolume != null ? fsVolume.hashCode() : 0);
        return result;
    }
}
