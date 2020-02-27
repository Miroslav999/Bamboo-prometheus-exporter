package atlas.plugin.promexporter.bambootask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.task.TaskException;
import com.atlassian.bamboo.task.TaskResult;
import com.atlassian.bamboo.task.TaskResultBuilder;
import com.atlassian.bamboo.task.TaskType;
import com.atlassian.util.concurrent.NotNull;

public class PrometheusTask implements TaskType {

	public static final String BRANCHES_KEY = "branches";
	public static final String KEY_TEST_TYPE = "testType";
	public static final String PLAN_NAME = "planName";
	public static final String BORDER = "border";

	private static final Logger LOGGER = LoggerFactory.getLogger(PrometheusTask.class);
	
	@NotNull
	@java.lang.Override
	public TaskResult execute(@NotNull final TaskContext taskContext)
			throws TaskException {

		LOGGER.info("PrometheusTask: call method of the task execute()");
		
		return TaskResultBuilder.newBuilder(taskContext).success().build();
	}

}
