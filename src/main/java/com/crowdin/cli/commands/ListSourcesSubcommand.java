package com.crowdin.cli.commands;

import com.crowdin.cli.commands.functionality.DryrunSources;
import com.crowdin.cli.commands.functionality.ProjectProxy;
import com.crowdin.cli.commands.parts.PropertiesBuilderCommandPart;
import com.crowdin.cli.properties.PropertiesBean;
import com.crowdin.cli.utils.PlaceholderUtil;
import com.crowdin.cli.utils.console.ConsoleSpinner;
import com.crowdin.common.Settings;
import picocli.CommandLine;

import static com.crowdin.cli.utils.MessageSource.Messages.FETCHING_PROJECT_INFO;
import static com.crowdin.cli.utils.console.ExecutionStatus.ERROR;
import static com.crowdin.cli.utils.console.ExecutionStatus.OK;

@CommandLine.Command(
    name = "sources",
    customSynopsis = "@|fg(yellow) crowdin list sources|@ [CONFIG OPTIONS] [OPTIONS]",
    description = "List information about the source files that match the wild-card pattern contained in the current project")
public class ListSourcesSubcommand extends PropertiesBuilderCommandPart {

    @CommandLine.Option(names = {"-b", "--branch"}, paramLabel = "...", description = "Specify branch name. Default: none")
    protected String branch;

    @CommandLine.Option(names = {"--tree"}, description = "List contents of directories in a tree-like format")
    protected boolean treeView;

    @Override
    public void run() {
        PropertiesBean pb = this.buildPropertiesBean();
        Settings settings = Settings.withBaseUrl(pb.getApiToken(), pb.getBaseUrl());


        ProjectProxy project = new ProjectProxy(pb.getProjectId(), settings);
        try {
            ConsoleSpinner.start(FETCHING_PROJECT_INFO.getString(), this.noProgress);
            project.downloadProject()
                .downloadSupportedLanguages();
            ConsoleSpinner.stop(OK);
        } catch (Exception e) {
            ConsoleSpinner.stop(ERROR);
            throw new RuntimeException("Exception while gathering project info", e);
        }
        PlaceholderUtil placeholderUtil = new PlaceholderUtil(project.getSupportedLanguages(), project.getProjectLanguages(), pb.getBasePath());

        (new DryrunSources(pb, placeholderUtil)).run(treeView);
    }
}