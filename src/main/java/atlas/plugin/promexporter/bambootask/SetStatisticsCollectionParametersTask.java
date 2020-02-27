package atlas.plugin.promexporter.bambootask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.task.TaskException;
import com.atlassian.bamboo.task.TaskResult;
import com.atlassian.bamboo.task.TaskResultBuilder;
import com.atlassian.bamboo.task.TaskType;

public class SetStatisticsCollectionParametersTask implements TaskType {
    
    private static final Logger LOGGER = LoggerFactory
            .getLogger(SetStatisticsCollectionParametersTask.class);

    public static final String BRANCH_KEY = "branch";
    
    @Override
    public TaskResult execute(TaskContext taskContext) throws TaskException {
        
        LOGGER.info("SetStatisticsCollectionParametersTask: call method of the task execute()");

        return TaskResultBuilder.newBuilder(taskContext).success().build();

    }

}
