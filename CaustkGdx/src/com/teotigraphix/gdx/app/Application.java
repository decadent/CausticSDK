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

package com.teotigraphix.gdx.app;

import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.google.common.eventbus.EventBus;
import com.teotigraphix.caustk.core.CausticException;
import com.teotigraphix.caustk.core.CaustkRack;
import com.teotigraphix.caustk.core.CaustkRuntime;
import com.teotigraphix.caustk.core.ICaustkLogger;
import com.teotigraphix.caustk.core.ISoundGenerator;
import com.teotigraphix.gdx.app.internal.ApplicationComponentRegistery;
import com.teotigraphix.gdx.app.internal.SceneManager;
import com.teotigraphix.gdx.app.internal.StartupExecutor;

/**
 * The base implementation of the {@link IApplication} API.
 * 
 * @author Michael Schmalle
 * @since 1.0
 */
public abstract class Application implements IApplication {

    // TODO Temp metrics
    static float WIDTH = 800f;

    static float HEIGHT = 480f;

    //--------------------------------------------------------------------------
    // Private :: Variables
    //--------------------------------------------------------------------------

    private CaustkRuntime runtime;

    private StartupExecutor startupExecutor;

    private SceneManager sceneManager;

    private String applicationName;

    private ApplicationComponentRegistery registry;

    private EventBus eventBus;

    //--------------------------------------------------------------------------
    // IGdxApplication API :: Properties
    //--------------------------------------------------------------------------

    @Override
    public String getApplicationName() {
        return applicationName;
    }

    @Override
    public float getWidth() {
        return WIDTH;
    }

    @Override
    public float getHeight() {
        return HEIGHT;
    }

    @Override
    public CaustkRack getRack() {
        return runtime.getRack();
    }

    @Override
    public ICaustkLogger getLogger() {
        return runtime.getLogger();
    }

    @Override
    public final EventBus getEventBus() {
        return eventBus;
    }

    @Override
    public <T extends IModel> T get(Class<T> clazz) {
        return registry.get(clazz);
    }

    @Override
    public void registerComponent(Class<? extends IApplicationComponent> clazz,
            IApplicationComponent component) {
        registry.put(clazz, component);
    }

    //--------------------------------------------------------------------------
    // Protected :: Properties
    //--------------------------------------------------------------------------

    protected ApplicationComponentRegistery getRegistry() {
        return registry;
    }

    protected SceneManager getSceneManager() {
        return sceneManager;
    }

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    /**
     * Creates a new {@link Application} specific to the platform
     * {@link ISoundGenerator}.
     * 
     * @param applicationName The name of the application.
     * @param soundGenerator The platform specific {@link ISoundGenerator}.
     */
    public Application(String applicationName, ISoundGenerator soundGenerator) {
        this.applicationName = applicationName;
        registry = new ApplicationComponentRegistery(this);
        eventBus = new EventBus("application");
        startupExecutor = new StartupExecutor(soundGenerator);
        sceneManager = new SceneManager(this);
    }

    //--------------------------------------------------------------------------
    // IGdxApplication API :: Methods
    //--------------------------------------------------------------------------

    @Override
    public final void create() {
        Gdx.app.log("GdxApplication", "create()");
        try {
            runtime = startupExecutor.create(this);
            runtime.getRack().initialize();
            runtime.getRack().onStart();
            sceneManager.create();
        } catch (CausticException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        onRegisterScenes();
        onRegisterModels();
        registry.awake();
        onCreate();
    }

    @Override
    public void render() {
        //Gdx.app.log("GdxApplication", "render()");
        sceneManager.preRender();

        if (runtime.getRack().isLoaded()) {
            runtime.getRack().frameChanged(Gdx.graphics.getDeltaTime());
        }

        sceneManager.postRender();
    }

    @Override
    public void resize(int width, int height) {
        Gdx.app.log("GdxApplication", "resize(" + width + ", " + height + ")");
        if (sceneManager != null)
            sceneManager.resize(width, height);
    }

    @Override
    public void pause() {
        runtime.getRack().onPause();
        Gdx.app.log("GdxApplication", "pause()");
        sceneManager.pause();
    }

    @Override
    public void resume() {
        runtime.getRack().onResume();
        Gdx.app.log("GdxApplication", "resume()");
        sceneManager.resume();
    }

    @Override
    public void dispose() {
        Gdx.app.log("GdxApplication", "dispose()");
        sceneManager.dispose();
        runtime.getRack().onDestroy();
    }

    //--------------------------------------------------------------------------
    // Protected :: Methods
    //--------------------------------------------------------------------------

    /**
     * Register application {@link IModel}s.
     * <p>
     * First of the register methods to be called.
     * 
     * @see ApplicationComponentRegistery#put(Class, IModel)
     */
    protected abstract void onRegisterModels();

    /**
     * Add {@link IScene}s to the application.
     * 
     * @see #onRegisterModels()
     * @see SceneManager#addScene(int, Class)
     */
    protected abstract void onRegisterScenes();

    /**
     * Set the initial {@link Scene} that starts the application, and perform
     * any other various setup tasks before the main user interface is shown.
     * 
     * @see #onRegisterScenes()
     */
    protected abstract void onCreate();

    public void onSceneChange(IScene scene) {
    }
}