package atlas.plugin.promexporter.metric;

import java.util.Optional;

public enum Parameter {
	  
	  CLASS_NAME("ClassName"),
	  TEST_NAME("TestName"),
	  TEST_TYPE("TestType"),
	  BRANCH("Branch"),
	  JOB("Job"),
	  PLAN("Plan");
	  
	  private final String labelName;
	  
	  Parameter(final String labelName){
	   this.labelName = labelName;
	  }
	  
	  public String getLabelName(){
		  return labelName;
	  }
	  
	  public static Optional<Parameter> getParameter(String key){
		  for (Parameter param : values()){
			  if (param.getLabelName().equals(key)){
				  return Optional.of(param);
			  }
		  }
		  return Optional.empty();
	  }
}
