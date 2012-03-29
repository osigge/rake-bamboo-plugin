package au.id.wolfe.bamboo.ruby.common;

import au.id.wolfe.bamboo.ruby.RubyLocator;
import au.id.wolfe.bamboo.ruby.rvm.RvmRubyLocator;
import au.id.wolfe.bamboo.ruby.rvm.RvmLocatorService;
import com.atlassian.bamboo.configuration.ConfigurationMap;
import com.atlassian.bamboo.process.EnvironmentVariableAccessor;
import com.atlassian.bamboo.process.ExternalProcessBuilder;
import com.atlassian.bamboo.process.ProcessService;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.task.TaskException;
import com.atlassian.bamboo.task.TaskResult;
import com.atlassian.bamboo.task.TaskResultBuilder;
import com.atlassian.bamboo.v2.build.agent.capability.CapabilityDefaultsHelper;
import com.atlassian.utils.process.ExternalProcess;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Basis for ruby tasks.
 */
public abstract class BaseRubyTask {

    public static final String RUBY_CAPABILITY_PREFIX = CapabilityDefaultsHelper.CAPABILITY_BUILDER_PREFIX + ".ruby";

    protected final Logger log = LoggerFactory.getLogger(BaseRubyTask.class);

    protected ProcessService processService;

    private RubyLocatorServiceFactory rubyLocatorServiceFactory;

    protected EnvironmentVariableAccessor environmentVariableAccessor;

    public BaseRubyTask() {

    }

    public TaskResult execute(@NotNull TaskContext taskContext) throws TaskException {

        final TaskResultBuilder taskResultBuilder = TaskResultBuilder.create(taskContext);

        final ConfigurationMap config = taskContext.getConfigurationMap();
        final String rubyRuntimeLabel = config.get("ruby");

        final RubyLabel rubyLabel = RubyLabel.getLabelFromString(rubyRuntimeLabel);

        Map<String, String> envVars = buildEnvironment(rubyLabel, config);

        List<String> commandsList = buildCommandList(rubyLabel, config);

        ExternalProcess externalProcess = processService.createProcess(taskContext,
                new ExternalProcessBuilder()
                        .env(envVars)
                        .command(commandsList)
                        .workingDirectory(taskContext.getWorkingDirectory()));

        externalProcess.execute();

        return taskResultBuilder.checkReturnCode(externalProcess, 0).build();

    }

    protected abstract Map<String,String> buildEnvironment(RubyLabel rubyRuntimeLabel, ConfigurationMap config);

    protected abstract List<String> buildCommandList(RubyLabel rubyRuntimeLabel, ConfigurationMap config);

    protected RubyLocator getRubyLocator(String rubyRuntimeManager) {
        return rubyLocatorServiceFactory.acquireRubyLocator(rubyRuntimeManager);
    }

    public void setProcessService(ProcessService processService) {
        this.processService = processService;
    }

    public void setRubyLocatorServiceFactory(RubyLocatorServiceFactory rubyLocatorServiceFactory) {
        this.rubyLocatorServiceFactory = rubyLocatorServiceFactory;
    }

    public void setEnvironmentVariableAccessor(EnvironmentVariableAccessor environmentVariableAccessor) {
        this.environmentVariableAccessor = environmentVariableAccessor;
    }
}

