package atlas.plugin.promexporter.bambootask;

import java.util.Map;

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.atlassian.util.concurrent.NotNull;
import com.atlassian.util.concurrent.Nullable;

public class PrometheusTaskConfigurator extends AbstractTaskConfigurator {
	
	private String setting;
	private String testType;
	private String planName;
	private String border;
	private String errorMsg = "The field can't be empty";

	public Map<String, String> generateTaskConfigMap(@NotNull final ActionParametersMap params, @Nullable final TaskDefinition previousTaskDefinition)
	{
	    final Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);

	    config.put(PrometheusTask.BRANCHES_KEY, params.getString(PrometheusTask.BRANCHES_KEY));
	    config.put(PrometheusTask.KEY_TEST_TYPE, params.getString(PrometheusTask.KEY_TEST_TYPE));
	    config.put(PrometheusTask.PLAN_NAME, params.getString(PrometheusTask.PLAN_NAME));
	    config.put(PrometheusTask.BORDER, params.getString(PrometheusTask.BORDER));
	    return config;
	}
	
	@Override
	public void populateContextForCreate(@NotNull final Map<String, Object> context)
	{
	    super.populateContextForCreate(context);
	    if (setting != null){
	    	context.put(PrometheusTask.BRANCHES_KEY, setting);
	    }
	    if (testType != null){
	    	context.put(PrometheusTask.KEY_TEST_TYPE, testType);
	    }
	    if (planName != null){
	    	context.put(PrometheusTask.PLAN_NAME, planName);
	    }
	    
	    if (border != null){
	    	context.put(PrometheusTask.BORDER, border);
	    }
	    
	}
	
	@Override
	public void populateContextForEdit(@NotNull final Map<String, Object> context, @NotNull final TaskDefinition taskDefinition)
	{
	    super.populateContextForEdit(context, taskDefinition);
	    
	    setting = taskDefinition.getConfiguration().get(PrometheusTask.BRANCHES_KEY);
	    context.put(PrometheusTask.BRANCHES_KEY, setting);
	    
	    testType = taskDefinition.getConfiguration().get(PrometheusTask.KEY_TEST_TYPE);
	    context.put(PrometheusTask.KEY_TEST_TYPE, testType);
	    
	    planName = taskDefinition.getConfiguration().get(PrometheusTask.PLAN_NAME);
	    context.put(PrometheusTask.PLAN_NAME, planName);
	    
	    border = taskDefinition.getConfiguration().get(PrometheusTask.BORDER);
	    context.put(PrometheusTask.BORDER, border);
	}

	@Override
	public void validate(ActionParametersMap params,
			ErrorCollection errorCollection) {
		super.validate(params, errorCollection);
		
		final String testTypeValue = params.getString(PrometheusTask.KEY_TEST_TYPE);
	    if (testTypeValue == null || testTypeValue.isEmpty()){
	        errorCollection.addError("testType", errorMsg);
	    }
	    
	    final String planNameValue = params.getString(PrometheusTask.PLAN_NAME);
	    if (planNameValue == null || planNameValue.isEmpty()){
	        errorCollection.addError("planName", errorMsg);
	    }
	}
	
	
}
