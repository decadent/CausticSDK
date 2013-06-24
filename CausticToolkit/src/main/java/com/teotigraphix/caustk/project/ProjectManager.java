
package com.teotigraphix.caustk.project;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.io.FileUtils;

import com.teotigraphix.caustk.controller.ICaustkController;
import com.teotigraphix.caustk.utls.JsonFormatter;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;

/**
 * The project manager manages the single project loaded for an application.
 * <p>
 * The manager will have a root directory passed to it when it is created. All
 * project related files are stored within this directory.
 */
public class ProjectManager implements IProjectManager {

    private XStream projectStream;

    private ICaustkController controller;

    private Project project;

    private ProjectPreferences projectPreferences;

    public ProjectPreferences getProjectPreferences() {
        return projectPreferences;
    }

    /**
     * The root application directory, all {@link Project}s are stored in the
     * <code>applicationRoot/projects</code> directory.
     */
    private File applicationRoot;

    private File projectDirectory;

    private File preferencesFile;

    private boolean formatJson = false;

    @Override
    public File getApplicationRoot() {
        return applicationRoot;
    }

    public Project getProject() {
        return project;
    }

    //--------------------------------------------------------------------------
    // Constructor
    //--------------------------------------------------------------------------

    public ProjectManager(ICaustkController controller, File applicationRoot) {
        this.controller = controller;
        this.applicationRoot = applicationRoot;
        projectDirectory = new File(applicationRoot, "projects");

        projectStream = new XStream(new JettisonMappedXmlDriver());
        projectStream.setMode(XStream.NO_REFERENCES);

        preferencesFile = new File(applicationRoot, ".settings");
        if (!preferencesFile.exists()) {
            try {
                preferencesFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            projectPreferences = new ProjectPreferences();
        } else {
            projectPreferences = (ProjectPreferences)projectStream.fromXML(preferencesFile);
        }

    }

    @Override
    public boolean isProject(File file) {
        if (file.isAbsolute())
            return file.exists();
        return toProjectFile(file).exists();
    }

    @Override
    public void save() throws IOException {
        projectPreferences.put(ProjectPreferences.LAST_PROJECT, project.getFile().getPath());

        String data = JsonFormatter.toJson(projectStream, project, formatJson);
        FileUtils.writeStringToFile(project.getFile(), data);
        String debug = project.getFile().getAbsolutePath().replace(".clp", "_d.clp");
        FileUtils.writeStringToFile(new File(debug),
                JsonFormatter.toJson(projectStream, project, true));

        data = JsonFormatter.toJson(projectStream, projectPreferences, formatJson);
        FileUtils.writeStringToFile(preferencesFile, data);
    }

    @Override
    public Project load(File file) throws IOException {
        file = toProjectFile(file);
        if (!file.exists())
            throw new IOException("Project file does not exist");

        projectStream = new XStream(new JettisonMappedXmlDriver());
        projectStream.setMode(XStream.NO_REFERENCES);
        projectStream.alias("project", Project.class);

        project = (Project)projectStream.fromXML(file);

        controller.getDispatcher().trigger(new IProjectManager.OnProjectLoad(project));

        return project;
    }

    @Override
    public Project create(File projectFile) throws IOException {
        project = new Project();
        project.setFile(new File(projectDirectory, projectFile.getPath()));
        project.setInfo(createInfo());
        controller.getDispatcher().trigger(new IProjectManager.OnProjectCreate(project));
        return project;
    }

    public void add(File songFile) {
        // TODO Auto-generated method stub

    }

    //--------------------------------------------------------------------------
    // 
    //--------------------------------------------------------------------------

    private ProjectInfo createInfo() {
        ProjectInfo info = new ProjectInfo();
        info.setName("Untitled Project");
        info.setAuthor("Untitled Author");
        info.setCreated(new Date());
        info.setModified(new Date());
        info.setDescription("A new project");
        return info;
    }

    private File toProjectFile(File file) {
        if (file.isAbsolute())
            return file;
        return new File(projectDirectory, file.getPath());
    }
}