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

package com.teotigraphix.caustic.internal.controller.application;

import roboguice.inject.ContextSingleton;

import com.teotigraphix.caustic.controller.IApplicationController;
import com.teotigraphix.caustic.internal.command.project.LoadProjectCommand;
import com.teotigraphix.caustic.internal.command.startup.RegisterMainLayoutCommand;
import com.teotigraphix.caustic.internal.command.workspace.StartupWorkspaceCommand;
import com.teotigraphix.caustic.internal.router.BaseRouterClient;

@ContextSingleton
public class ApplicationController extends BaseRouterClient implements IApplicationController {

    @Override
    public final String getName() {
        return DEVICE_ID;
    }

    public ApplicationController() {
    }

    @Override
    protected void registerCommands() {
        super.registerCommands();
        addCommand(REGISTER_MAIN_LAYOUT, RegisterMainLayoutCommand.class);
        addCommand(START_WORKSPACE, StartupWorkspaceCommand.class);
        addCommand(LOAD_PROJECT, LoadProjectCommand.class);
    }
}
