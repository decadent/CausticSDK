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

package com.teotigraphix.caustk.controller;

import com.teotigraphix.caustic.core.ICausticEngine;
import com.teotigraphix.caustic.core.IDispatcher;
import com.teotigraphix.caustic.device.IDeviceFactory;
import com.teotigraphix.caustk.application.ICaustkApplication;
import com.teotigraphix.caustk.application.ICaustkConfiguration;
import com.teotigraphix.caustk.controller.command.ICommandManager;
import com.teotigraphix.caustk.sound.ICaustkSoundGenerator;
import com.teotigraphix.caustk.sound.ICaustkSoundMixer;
import com.teotigraphix.caustk.sound.ICaustkSoundSource;

/**
 * ISoundSourceAPI api = context.api().get(ISoundSourceAPI.class);
 * api.setMasterVolume(0.5f);
 * 
 * @author Michael Schmalle
 */
public interface ICaustkController extends ICausticEngine {

    void registerAPI(Class<? extends IControllerAPI> clazz, IControllerAPI instance);

    <T extends IControllerAPI> T api(Class<T> clazz);

    ICaustkApplication getApplication();

    ICaustkConfiguration getConfiguration();

    IDispatcher getDispatcher();

    IDeviceFactory getFactory();

    ICaustkSoundGenerator getSoundGenerator();

    ICaustkSoundSource getSoundSource();

    ICaustkSoundMixer getSoundMixer();

    ICommandManager getCommandManager();

    void execute(String message, Object... args);

    void undo();

    void redo();

    /**
     * Called to create all sub components of the {@link ICaustkController}.
     * 
     * @see ICaustkApplication#initialize()
     */
    void initialize();

    /**
     * Called to start all sub components of the {@link ICaustkController}.
     * 
     * @see ICaustkApplication#start()
     */
    void start();

    void close();

}
