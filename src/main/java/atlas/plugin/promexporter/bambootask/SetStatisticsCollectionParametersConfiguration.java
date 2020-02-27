package atlas.plugin.promexporter.bambootask;

import java.util.Map;

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.util.concurrent.NotNull;
import com.atlassian.util.concurrent.Nullable;

public class SetStatisticsCollectionParametersConfiguration extends AbstractTaskConfigurator {
    private String branch;
    
    public Map<String, String> generateTaskConfigMap(@NotNull final ActionParametersMap params, @Nullable final TaskDefinition previousTaskDefinition)
    {
        final Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);

        config.put(SetStatisticsCollectionParametersTask.BRANCH_KEY, params.getString(SetStatisticsCollectionParametersTask.BRANCH_KEY));
      
        return config;
    }
    
    @Override
    public void populateContextForCreate(@NotNull final Map<String, Object> context)
    {
        super.populateContextForCreate(context);
        if (branch != null){
            context.put(SetStatisticsCollectionParametersTask.BRANCH_KEY, branch);
        }
    }
    
    @Override
    public void populateContextForEdit(@NotNull final Map<String, Object> context, @NotNull final TaskDefinition taskDefinition)
    {
        super.populateContextForEdit(context, taskDefinition);
        
        branch = taskDefinition.getConfiguration().get(SetStatisticsCollectionParametersTask.BRANCH_KEY);
        context.put(SetStatisticsCollectionParametersTask.BRANCH_KEY, branch);
     
    }
}
