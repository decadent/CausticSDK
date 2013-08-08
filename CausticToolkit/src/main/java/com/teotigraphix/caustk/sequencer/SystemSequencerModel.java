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

package com.teotigraphix.caustk.sequencer;

import com.teotigraphix.caustk.controller.ICaustkController;
import com.teotigraphix.caustk.controller.SubControllerModel;
import com.teotigraphix.caustk.core.osc.OutputPanelMessage;
import com.teotigraphix.caustk.sequencer.ISystemSequencer.SequencerMode;

public class SystemSequencerModel extends SubControllerModel {

    private boolean isPlaying;

    public void setIsPlaying(boolean value) {
        isPlaying = value;
        OutputPanelMessage.PLAY.send(getController(), isPlaying ? 1 : 0);
    }

    public final boolean isPlaying() {
        return isPlaying;
    }

    private SequencerMode sequencerMode;

    public final SequencerMode getSequencerMode() {
        return sequencerMode;
    }

    public final void setSequencerMode(SequencerMode value) {
        sequencerMode = value;
        OutputPanelMessage.MODE.send(getController(), sequencerMode.getValue());
    }

    private float tempo;

    public void setTempo(float value) {
        tempo = value;
        OutputPanelMessage.BPM.send(getController(), tempo);
    }

    public float getTempo() {
        return tempo;
    }

    public SystemSequencerModel() {
    }

    public SystemSequencerModel(ICaustkController controller) {
        super(controller);
    }

}