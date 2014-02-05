////////////////////////////////////////////////////////////////////////////////
// Copyright 2014 Michael Schmalle - Teoti Graphix, LLC
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

import com.google.common.eventbus.EventBus;
import com.teotigraphix.gdx.app.internal.ApplicationComponentRegistery;

/**
 * The {@link IModel} is registered with the
 * {@link ApplicationComponentRegistery} for application state.
 * <p>
 * Models will dispatch events through their local {@link EventBus} or global
 * {@link IApplication#getEventBus()}.
 * 
 * @author Michael Schmalle
 * @since 1.0
 */
public interface IModel extends IApplicationComponent {

    /**
     * The model's local {@link EventBus}.
     */
    EventBus getEventBus();

}