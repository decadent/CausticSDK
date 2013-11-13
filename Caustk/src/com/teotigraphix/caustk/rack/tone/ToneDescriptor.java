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

package com.teotigraphix.caustk.rack.tone;

import java.util.UUID;

import com.teotigraphix.caustk.workstation.MachineType;

/**
 * Each application must define the {@link RackTone} instances that will get
 * created at startup.
 * 
 * @author Michael Schmalle
 */
public class ToneDescriptor {

    private int machineIndex;

    private String machineName;

    private MachineType machineType;

    private UUID patchId;

    public int getMachineIndex() {
        return machineIndex;
    }

    public String getMachineName() {
        return machineName;
    }

    public MachineType getMachineType() {
        return machineType;
    }

    public final UUID getPatchId() {
        return patchId;
    }

    public final void setPatchId(UUID value) {
        patchId = value;
    }

    public ToneDescriptor(int machineIndex, String machineName, MachineType machineType) {
        this.machineIndex = machineIndex;
        this.machineName = machineName;
        this.machineType = machineType;
    }

    @Override
    public String toString() {
        return "[ToneDescriptor(" + machineIndex + "," + machineType.getType() + "," + machineName
                + ")]";
    }
}
