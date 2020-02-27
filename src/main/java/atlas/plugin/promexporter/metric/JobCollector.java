package atlas.plugin.promexporter.metric;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class JobCollector {
    private Map<String, Job> jobs = new HashMap<String, Job>();
    private String requiredBranchName;
    private boolean isStarted;

    public String getRequiredBranchName() {
        return requiredBranchName;
    }

    public void setRequiredBranchName(String requiredBranchName) {
        this.requiredBranchName = requiredBranchName;
    }

    public boolean isStarted() {
        return isStarted;
    }

    public void start() {
        this.isStarted = true;
    }
    
    public void finish() {
        this.isStarted = false;
    }

    public Map<String, Job> getJobs() {
        return jobs;
    }
    
    public boolean isFinished(){
        
        if (!isStarted){
            return false;
        }
        
        for (Job job : jobs.values()){
            if (!job.isFinished()){
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "JobCollector [jobs=" + jobs.toString() + ", requiredBranchName="
                + requiredBranchName + ", isStarted=" + isStarted + "]";
    }
    
}
