////////////////////////////////////////////////////////////////////////////////
// Copyright 2013 Michael Schmalle - Teoti Graphix, LLC
// 
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
// http://www.apache.org/licenses/LICENSE-2.0 
// 
// Unless required by applicable law or agreed to in writing, software 
// distributed under the License is distributed on an "AS IS" BASIS, 
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and 
// limitations under the License
// 
// Author: Michael Schmalle, Principal Architect
// mschmalle at teotigraphix dot com
////////////////////////////////////////////////////////////////////////////////

package com.teotigraphix.caustk.gdx.app;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.esotericsoftware.kryo.KryoException;
import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.teotigraphix.caustk.core.CausticException;
import com.teotigraphix.caustk.core.ICaustkLogger;
import com.teotigraphix.caustk.core.ICaustkRack;
import com.teotigraphix.caustk.core.ICaustkRuntime;
import com.teotigraphix.caustk.core.ISoundGenerator;
import com.teotigraphix.caustk.gdx.app.controller.IFileManager;
import com.teotigraphix.caustk.gdx.app.controller.IFileModel;
import com.teotigraphix.caustk.gdx.app.controller.IViewManager;
import com.teotigraphix.caustk.gdx.app.ui.ICaustkScene;
import com.teotigraphix.caustk.gdx.app.ui.IScene;
import com.teotigraphix.caustk.node.RackNode;
import com.teotigraphix.caustk.utils.core.RuntimeUtils;

/**
 * The base implementation of the {@link ICaustkApplication} API.
 * 
 * @author Michael Schmalle
 * @since 1.0
 */
public abstract class CaustkApplication extends Application implements ICaustkApplication,
        ApplicationListener {

    protected final IProjectModelWrite getProjectModelWrittable() {
        return (IProjectModelWrite)getProjectModel();
    }

    public Preferences getGlobalPreferences() {
        return getPreferenceManager().get("com_teotigraphix_caustk_global");
    }

    private static final String TAG = "CaustkApplication";

    @Inject
    private IApplicationConfigurator applicationConfigurator;

    @Inject
    private IApplicationStateHandlers applicationStates;

    @Inject
    private IProjectModel projectModel;

    @Inject
    private IFileManager fileManager;

    @Inject
    private IFileModel fileModel;

    @Inject
    private IViewManager viewManager;

    // Private :: Variables

    private ApplicationPreferences applicationPreferences;

    public ApplicationPreferences getApplicationPreferences() {
        return applicationPreferences;
    }

    protected IApplicationStateHandlers getApplicationStates() {
        return applicationStates;
    }

    protected IProjectModel getProjectModel() {
        return projectModel;
    }

    protected IFileManager getFileManager() {
        return fileManager;
    }

    protected IFileModel getFileModel() {
        return fileModel;
    }

    protected IViewManager getViewManager() {
        return viewManager;
    }

    // --------------------------------------------------------------------------
    // Private :: Variables
    // --------------------------------------------------------------------------

    private ICaustkRuntime runtime;

    private StartupExecutor startupExecutor;

    private Set<Module> modules = new HashSet<Module>();

    private Injector injector;

    protected boolean flushApplicationDirectory;

    public Injector getInjector() {
        return injector;
    }

    // --------------------------------------------------------------------------
    // IGdxApplication API :: Properties
    // --------------------------------------------------------------------------

    @Override
    public ICaustkLogger getLogger() {
        return runtime.getLogger();
    }

    @Override
    public ICaustkRack getRack() {
        return runtime.getRack();
    }

    @Override
    public RackNode getRackNode() {
        return runtime.getRack().getRackNode();
    }

    @Override
    public EventBus getRackEventBus() {
        return runtime.getRack().getEventBus();
    }

    @Override
    public ICaustkScene getScene() {
        return (ICaustkScene)super.getScene();
    }

    //--------------------------------------------------------------------------
    // Protected :: Properties
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    /**
     * Creates a new {@link CaustkApplication} specific to the platform
     * {@link com.teotigraphix.caustk.core.ISoundGenerator}.
     * 
     * @param applicationName The name of the application.
     * @param soundGenerator The platform specific
     *            {@link com.teotigraphix.caustk.core.ISoundGenerator}.
     */
    public CaustkApplication(String applicationName, ISoundGenerator soundGenerator) {
        super(applicationName);
        startupExecutor = new StartupExecutor(this, soundGenerator);
    }

    private void createGuice() {
        Gdx.app.log(TAG, "createGuice()");

        final ICaustkApplication instance = this;

        final Set<Module> additionalModules = new HashSet<Module>();
        modules.add(new AbstractModule() {
            @Override
            protected void configure() {
                bind(ICaustkApplication.class).toInstance(instance);
            }
        });
        modules.add(createApplicationModule());

        // Propagates initialization of additional modules to the specific
        // subclass of this Application instance.
        modules.addAll(additionalModules);

        // Creates an injector with all of the required modules.
        injector = Guice.createInjector(modules);

        // Injects all fields annotated with @Inject into this IGame instance.
        injector.injectMembers(instance);

        applicationPreferences = new ApplicationPreferences(getPreferenceManager().get(
                getApplicationId() + "_application"));
    }

    protected abstract Module createApplicationModule();

    protected final void addModule(Module module) {
        modules.add(module);
    }

    //--------------------------------------------------------------------------
    // ICaustkApplication API :: Methods
    //--------------------------------------------------------------------------

    @Override
    public void onSceneChange(IScene scene) {
        super.onSceneChange(scene);
        injector.injectMembers(scene);
    }

    private File getStorageRoot() throws IOException, CausticException {
        File root = new File(Gdx.files.getExternalStoragePath());
        File caustic = new File(root, "caustic");
        if (!caustic.exists()) {
            File newRoot = StartupExecutor.getContainedDirectory(root, new File("caustic"));
            if (newRoot == null)
                throw new CausticException(
                        "the caustic folder does not exist, is caustic installed?");
            root = newRoot;
        }
        return root;
    }

    private File getApplicationRoot(File root) {
        return new File(root, getApplicationName());
    }

    // for unit testing synchronously
    public final void setup(File storageRoot, File applicationRoot) {
        Gdx.app.log(TAG, "setup()");

        try {
            Gdx.app.log("StartupExecutor", "create()");
            runtime = startupExecutor.create(this, storageRoot, applicationRoot);
            getLogger().log("Rack", "initialize()");
            runtime.getRack().initialize();

            if (flushApplicationDirectory) {
                try {
                    FileUtils.forceDelete(RuntimeUtils.getApplicationDirectory());
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            getLogger().log("Rack", "onStart()");
            runtime.getRack().onStart();
            getLogger().log("SceneManager", "create()");
            getSceneManager().create();
        } catch (CausticException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        createGuice();

        applicationConfigurator.configure(runtime.getRack().getSerializer().getKryo());

        onRegisterScenes();
        onRegisterModels();

        // Not Implemented
        setup();

        // async after load, sets main scene ui
        startup();
    }

    //--------------------------------------------------------------------------
    // IApplicationController API :: Methods
    //--------------------------------------------------------------------------

    public void setup() {
        getLogger().log(TAG, "setup() Not Implemented");
    }

    public void startup() {
        getLogger().log(TAG, "startup( BEG )");
        try {
            //setup();
            getApplicationStates().startup();
            if (!Application.TEST) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        getLogger().log(TAG, "    ------------------------------------");
                        getLogger().log(TAG, "    $$$$ NEXT FRAME");

                        startScene();
                        // this gets called one frame later so behaviors have registered
                        // onAwake(), onStart()
                        Gdx.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                getLogger().log(TAG, "    ------------------------------------");
                                getLogger().log(TAG, "    %%%% NEXT FRAME");

                                getApplicationStates().startUI();
                            }
                        });
                    }
                });
            }
        } catch (KryoException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        getLogger().log(TAG, "startup( END )");
    }

    @Override
    public final void create() {
        Gdx.app.log(TAG, "create()");

        if (!Application.TEST) {
            try {
                File root;
                root = getStorageRoot();
                setup(root, getApplicationRoot(root));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (CausticException e) {
                e.printStackTrace();
            }
        }
        // 1. - startup() - Loads last project state
        // 2. - next frame startScene()
        // 3. - next frame startUI()
        onCreate();
    }

    @Override
    public void startScene() {
        getLogger().log(TAG, "startScene() - Start initial scene");
        // XXX This will be the first thing called after a new Project is loaded
        getSceneManager().reset();
        setScene(getInitialScene());
    }

    @Override
    public void render() {
        if (Application.TEST)
            return;

        getSceneManager().preRender();
        if (runtime.getRack().isLoaded()) {
            runtime.getRack().frameChanged(Gdx.graphics.getDeltaTime());
            int measure = runtime.getRack().getSequencer().getCurrentMeasure();
            float beat = runtime.getRack().getSequencer().getCurrentFloatBeat();
            int sixteenth = runtime.getRack().getSequencer().getCurrentSixteenthStep();
            int thirtysecond = runtime.getRack().getSequencer().getCurrentThritySecondStep();
            if (measure == -1)
                measure = (int)(beat / 4);

            if (runtime.getRack().getSequencer().isThirtysecondChanged()) {
                getScene().onPreCalculate(measure, beat, sixteenth, thirtysecond);
            }
            if (runtime.getRack().getSequencer().isBeatChanged()) {
                getScene().onBeatChange(measure, beat, sixteenth, thirtysecond);
            }
            if (runtime.getRack().getSequencer().isSixteenthChanged()) {
                getScene().onPreSixteenthChange(measure, beat, sixteenth, thirtysecond);
            }
            if (runtime.getRack().getSequencer().isSixteenthChanged()) {
                getScene().onSixteenthChange(measure, beat, sixteenth, thirtysecond);
            }
            if (runtime.getRack().getSequencer().isThirtysecondChanged()) {
                getScene().onThirtysecondChange(measure, beat, sixteenth, thirtysecond);
            }
        }
        getSceneManager().postRender();
    }

    @Override
    public void resize(int width, int height) {
        getLogger().log(TAG, "resize(" + width + ", " + height + ")");
        if (getSceneManager() != null)
            getSceneManager().resize(width, height);
    }

    @Override
    public void pause() {
        runtime.getRack().onPause();
        getLogger().log(TAG, "pause()");
        getSceneManager().pause();
    }

    @Override
    public void resume() {
        runtime.getRack().onResume();
        getLogger().log(TAG, "resume()");
        getSceneManager().resume();
    }

    @Override
    public void dispose() {
        getLogger().log(TAG, "dispose()");

        save();
        getSceneManager().dispose();
        runtime.getRack().onDestroy();
    }

    // From AppModel
    public void save() {
        // Always save preferences
        getPreferenceManager().save();
        if (isDirty()) {
            try {
                getProjectModelWrittable().save();
                setDirty(false);
            } catch (IOException e) {
                getLogger().err(TAG, "ApplicationModel.save(); failed", e);
            }
        }
    }

    //--------------------------------------------------------------------------
    // Project API :: Methods
    //--------------------------------------------------------------------------

    public void newProject(File projectLocation) throws IOException {
        Project project = getFileManager().createProject(projectLocation);
        getProjectModelWrittable().setProject(project);
    }

    public void loadProject(File projectFile) throws IOException {
        Project project = getFileManager().loadProject(projectFile);
        getProjectModelWrittable().setProject(project);
    }

    public File saveProjectAs(String projectName) throws IOException {
        return saveProjectAs(new File(getFileManager().getProjectsDirectory(), projectName));
    }

    /**
     * @param projectLocation The project directory, getName() returns the
     *            project name to copy.
     * @throws java.io.IOException
     */
    public File saveProjectAs(File projectLocation) throws IOException {
        File srcDir = getProjectModel().getProjectDirectory();
        File destDir = projectLocation;
        FileUtils.copyDirectory(srcDir, destDir);

        // load the copied project discreetly
        final File oldProjectFile = new File(projectLocation, srcDir.getName() + ".prj");
        File newProjectFile = new File(projectLocation, destDir.getName() + ".prj");

        Project newProject = getFileManager().readProject(oldProjectFile);
        newProject.setRack(getRack()); // needed for deserialization
        newProject.rename(newProjectFile);

        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                // XXX This is NOTE DELETING
                FileUtils.deleteQuietly(oldProjectFile);
            }
        });

        loadProject(newProject.getFile());

        return newProjectFile;
    }

    public File exportProject(File file, ApplicationExportType exportType) throws IOException {
        File srcDir = getProjectModel().getProjectDirectory();
        File exportedFile = new File(srcDir, "exported/");
        exportedFile.mkdirs();
        // NO .caustic ext
        // XXX Can't have spaces, must replace all spaces with '-'
        exportedFile = new File(exportedFile, file.getName().replaceAll(" ", "-") + ".caustic");

        File savedFile = getRackNode().saveSongAs(exportedFile);
        return savedFile;
    }

    // --------------------------------------------------------------------------
    // Protected :: Methods
    // --------------------------------------------------------------------------

    /**
     * Add {@link com.teotigraphix.caustk.gdx.app.ui.ICaustkScene}s to the
     * application.
     * 
     * @see #onRegisterModels()
     * @see com.teotigraphix.caustk.gdx.app.ui.SceneManager#addScene(int, Class)
     */
    @Override
    protected abstract void onRegisterScenes();

    /**
     * Register application {@link IModel}s.
     * <p>
     * First of the register methods to be called.
     * 
     * @see ApplicationComponentRegistery#putCommand(Class, IModel)
     */
    @Override
    protected abstract void onRegisterModels();

    /**
     * Set the initial {@link com.teotigraphix.caustk.gdx.app.ui.Scene} that
     * starts the application, and perform any other various setup tasks before
     * the main user interface is shown.
     * 
     * @see #onRegisterScenes()
     */
    @Override
    protected abstract void onCreate();

    /**
     * Called by the {@link com.teotigraphix.caustk.gdx.app.ui.SceneManager}
     * during a scene change.
     * 
     * @param scene The active scene.
     */
    @Override
    public void onSceneChange(ICaustkScene scene) {
    }

    //--------------------------------------------------------------------------
    // Classes
    //--------------------------------------------------------------------------

    class ApplicationPreferences {

        private static final String ATT_ROOT_DIRECTORY = "root-directory";

        private Preferences preferences;

        // ApplicationModel creates
        public ApplicationPreferences(Preferences preferences) {
            this.preferences = preferences;
        }

        //----------------------------------
        // ATT_ROOT_DIRECTORY
        //----------------------------------

        /**
         * Returns the {@link #getRootDirectoryPath()} as a File.
         */
        public File getRootDirectoryFile() {
            return new File(getRootDirectoryPath());
        }

        /**
         * Returns the
         * {@link com.teotigraphix.caustk.utils.core.RuntimeUtils#getCausticDirectory()}
         * or the root-directory found within the {@link #preferences}.
         */
        public String getRootDirectoryPath() {
            return preferences.getString(ATT_ROOT_DIRECTORY, RuntimeUtils.getCausticDirectory()
                    .getAbsolutePath());
        }

        /**
         * Sets the system dependent absolute directory path for the
         * root-directory.
         * 
         * @param absolutePath The absolute directory path.
         */
        public void setRootDirectoryPath(String absolutePath) {
            preferences.putString(ATT_ROOT_DIRECTORY, absolutePath);
        }

    }
}
