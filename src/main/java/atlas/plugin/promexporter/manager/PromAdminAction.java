package atlas.plugin.promexporter.manager;

import javax.inject.Inject;
import com.atlassian.struts.Preparable;
import com.atlassian.bamboo.bandana.PlanAwareBandanaContext;
import com.atlassian.bamboo.configuration.GlobalAdminAction;
import com.atlassian.bandana.BandanaManager;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import static com.opensymphony.xwork2.ActionContext.getContext;


@Scanned
public class PromAdminAction extends GlobalAdminAction implements Preparable {

    private static final long serialVersionUID = -4349345187272687015L;
    
    @ComponentImport
    private final BandanaManager bandanaManager;
    private final PromSettingsManager promSettingsManager;
    private String url;
    private String key = "PROMETHEUS_URL";

    @Inject
    public PromAdminAction(PromSettingsManager promSettingsManager, BandanaManager bandanaManager) {
        this.promSettingsManager = promSettingsManager;
        this.bandanaManager = bandanaManager;
        promSettingsManager.setBandanaManager(bandanaManager);
    }

    @Override
    public String execute() {
        if (url == null || url.isEmpty()) {
            promSettingsManager.setUrl(null);
            return SUCCESS;
        }
        bandanaManager.setValue(PlanAwareBandanaContext.GLOBAL_CONTEXT,key,url);
        return SUCCESS;
    }

    @Override
    public void prepare() {
        if (promSettingsManager.getUrl() != null) {
            getContext().put("url", promSettingsManager.getUrl());
        }
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
