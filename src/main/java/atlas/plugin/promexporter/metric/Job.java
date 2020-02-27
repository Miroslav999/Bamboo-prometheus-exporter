package atlas.plugin.promexporter.metric;

public class Job {
    private String projectName;
    private String planName;
    private String jobName;
    private long queueDuration;
    private long processingDuration;
    private long startTime;
    private long endTime;
    private boolean finished;
    private int testNumber;
    
    public long getProcessingDuration() {
        return processingDuration;
    }
    public void setProcessingDuration(long processingDuration) {
        this.processingDuration = processingDuration;
    }
    
    public long getQueueDuration() {
        return queueDuration;
    }
    public void setQueueDuration(long queuedTime) {
        this.queueDuration = queuedTime;
    }
    public long getStartTime() {
        return startTime;
    }
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
    public long getEndTime() {
        return endTime;
    }
    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
    public String getProjectName() {
        return projectName;
    }
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
    public String getPlanName() {
        return planName;
    }
    public void setPlanName(String planName) {
        this.planName = planName;
    }
    public String getJobName() {
        return jobName;
    }
    public void setJobName(String jobName) {
        this.jobName = jobName;
    }
    public boolean isFinished() {
        return finished;
    }
    public void setFinished(boolean finished) {
        this.finished = finished;
    }
    
    public int getTestNumber() {
        return testNumber;
    }
    public void setTestNumber(int testNumber) {
        this.testNumber = testNumber;
    }
    @Override
    public String toString() {
        return "Job [projectName=" + projectName + ", planName=" + planName
                + ", jobName=" + jobName + ", queueDuration=" + queueDuration
                + ", progressDuration=" + processingDuration + ", startTime="
                + startTime + ", endTime=" + endTime + ", finished=" + finished
                + ", testNumber=" + testNumber + "]";
    }
    
}
