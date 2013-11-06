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

import com.teotigraphix.caustk.core.CausticException;
import com.teotigraphix.caustk.core.IRestore;
import com.teotigraphix.caustk.live.ICaustkFactory;
import com.teotigraphix.caustk.rack.IRack;

/**
 * @author Michael Schmalle
 */
public interface IRackSerializer extends IRestore {

    /**
     * @param context
     * @throws CausticException
     */
    void create(ICaustkApplicationContext context) throws CausticException;

    /**
     * Loads the rack component from a <code>.caustic</code> file.
     * <p>
     * Calling this method will wipe out all state, rack references and sub
     * components creating new components and loading from a caustic file.
     * <p>
     * The quickest way to wipe state is loading a blank <code>.caustic</code>
     * file where all defaults are loaded by query from a restore() call after
     * the load.
     * <p>
     * Most classes will save their transient reference to the {@link IRack} and
     * {@link ICaustkFactory} if needed in the super method.
     * 
     * @param context The current context.
     * @throws CausticException
     */
    void load(ICaustkApplicationContext context) throws CausticException;

    /**
     * Updates the native rack with instance property state that exists on the
     * rack component, the component will send setter commands to the native
     * rack.
     * <p>
     * Most classes will save their transient reference to the {@link IRack} and
     * {@link ICaustkFactory} if needed in the super method.
     */
    void update(ICaustkApplicationContext context);

    /**
     * Restores the rack, each component implementing the method will use OSC
     * message queries to set instance properties.
     * <p>
     * The restore() method differs from the load() in that the load() method
     * will actually create sub components, where restore() just updates state
     * on the existing components.
     * <p>
     * Most classes will save their transient reference to the {@link IRack} and
     * {@link ICaustkFactory} if needed in the super method.
     */
    @Override
    void restore();

    /**
     * Post-load callback for components and sub components.
     */
    void onLoad();

    /**
     * Pre-save callback for components and their sub components.
     */
    void onSave();

    void disconnect();
}
