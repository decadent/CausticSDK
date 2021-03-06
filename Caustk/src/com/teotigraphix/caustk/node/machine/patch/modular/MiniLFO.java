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

package com.teotigraphix.caustk.node.machine.patch.modular;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.teotigraphix.caustk.node.machine.Machine;

public class MiniLFO extends ModularComponentBase {

    //--------------------------------------------------------------------------
    // Serialized API
    //--------------------------------------------------------------------------

    @Tag(100)
    private WaveForm waveForm = WaveForm.Sine;

    @Tag(101)
    private int rate;

    @Tag(102)
    private int noteSync;

    @Tag(103)
    private float outGain;

    //--------------------------------------------------------------------------
    // Public API :: Properties
    //--------------------------------------------------------------------------

    //----------------------------------
    // waveForm
    //----------------------------------

    public WaveForm getWaveForm() {
        return waveForm;
    }

    WaveForm getWaveForm(boolean restore) {
        return WaveForm.fromFloat(getValue("waveform"));
    }

    public void setWaveForm(WaveForm value) {
        if (value == null) {
            System.err.println("NULL MiniLFO.setWaveForm()");
            return;
        }
        this.waveForm = value;
        setValue("waveform", value.getValue());
    }

    //----------------------------------
    // rate
    //----------------------------------

    public int getRate() {
        return rate;
    }

    int getRate(boolean restore) {
        return (int)getValue("rate");
    }

    /**
     * @param value (1..12)
     */
    public void setRate(int value) {
        rate = value;
        // 1..12
        setValue("rate", value);
    }

    //----------------------------------
    // noteSync
    //----------------------------------

    public int getNoteSync() {
        return noteSync;
    }

    int getNoteSync(boolean restore) {
        return (int)getValue("note_sync");
    }

    public void setNoteSync(int value) {
        noteSync = value;
        // 0,1
        setValue("note_sync", value);
    }

    //----------------------------------
    // outGain
    //----------------------------------

    public float getOutGain() {
        return outGain;
    }

    float getOutGain(boolean restore) {
        return getValue("out_gain");
    }

    /**
     * @param value (0..1)
     */
    public void setOutGain(float value) {
        if (value == outGain)
            return;
        outGain = value;
        if (value < 0f || value > 1f)
            newRangeException("out_gain", "0..1", value);
        setValue("out_gain", value);
    }

    public MiniLFO() {
    }

    public MiniLFO(Machine machineNode, int bay) {
        super(machineNode, bay);
        setLabel("MiniLFO");
    }

    @Override
    protected int getNumBays() {
        return 1;
    }

    @Override
    protected void restoreComponents() {
        setNoteSync(getNoteSync(true));
        setOutGain(getOutGain(true));
        setRate(getRate(true));
        setWaveForm(getWaveForm(true));
    }

    public enum WaveForm {

        Sine(0),

        Triangle(0),

        Saw(0),

        InvSaw(0),

        Square(0),

        Random(0);

        private int value;

        public final int getValue() {
            return value;
        }

        WaveForm(int value) {
            this.value = value;
        }

        public static WaveForm fromFloat(float value) {
            for (WaveForm waveForm : values()) {
                if (waveForm.getValue() == value)
                    return waveForm;
            }
            return null;
        }

    }

    public enum MiniLFOJack implements IModularJack {

        OutLeft(0),

        OutRight(1);

        private int value;

        @Override
        public final int getValue() {
            return value;
        }

        MiniLFOJack(int value) {
            this.value = value;
        }
    }
}
