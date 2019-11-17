package atlas.plugin.promexporter.manager;

import org.springframework.stereotype.Component;
import com.atlassian.bamboo.bandana.PlanAwareBandanaContext;
import com.atlassian.bandana.BandanaManager;

@Component
public class PromSettingsManager {
    private BandanaManager bandanaManager;
    private String key = "PROMETHEUS_URL";

    public String getUrl() {
        Object val = bandanaManager.getValue(PlanAwareBandanaContext.GLOBAL_CONTEXT,key);
        return val!=null? val.toString() : null;
    }

    public void setUrl(String url) {
        bandanaManager.setValue(PlanAwareBandanaContext.GLOBAL_CONTEXT,key,url);
    }
    
    public void setBandanaManager(BandanaManager bandanaManager){
        this.bandanaManager = bandanaManager;
    }
}
