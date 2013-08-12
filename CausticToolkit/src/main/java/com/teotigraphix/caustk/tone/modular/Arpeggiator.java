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

package com.teotigraphix.caustk.tone.modular;

import com.teotigraphix.caustk.controller.ICaustkController;

public class Arpeggiator extends ModularComponentBase {

    //----------------------------------
    // active
    //----------------------------------

    private int active;

    public float getActive() {
        return active;
    }

    int getActive(boolean restore) {
        return (int)getValue("active");
    }

    /**
     * @param value (0,1)
     */
    public void setActive(int value) {
        if (value == active)
            return;
        active = value;
        if (value < 0 || value > 1)
            newRangeException("active", "0,1", value);
        setValue("active", value);
    }

    //----------------------------------
    // sequence
    //----------------------------------

    private Sequence sequence;

    public Sequence getSequence() {
        return sequence;
    }

    Sequence getSequence(boolean restore) {
        return Sequence.fromInt(getValue("active"));
    }

    /**
     * @param value (0,1)
     */
    public void setSequence(Sequence value) {
        if (value == sequence)
            return;
        sequence = value;
        setValue("sequence", value.getValue());
    }

    //----------------------------------
    // octaves
    //----------------------------------

    private int octaves;

    public float getOctaves() {
        return octaves;
    }

    int getOctaves(boolean restore) {
        return (int)getValue("octaves");
    }

    /**
     * @param value (0..3)
     */
    public void setOctaves(int value) {
        if (value == octaves)
            return;
        octaves = value;
        if (value < 0 || value > 3)
            newRangeException("octaves", "0..3", value);
        setValue("octaves", value);
    }

    //----------------------------------
    // rate
    //----------------------------------

    private int rate;

    public float getRate() {
        return rate;
    }

    int getRate(boolean restore) {
        return (int)getValue("rate");
    }

    /**
     * @param value (1..12)
     */
    public void setRate(int value) {
        if (value == rate)
            return;
        rate = value;
        if (value < 1 || value > 12)
            newRangeException("rate", "1..12", value);
        setValue("rate", value);
    }

    public Arpeggiator() {
    }

    public Arpeggiator(ICaustkController controller, int bay) {
        super(controller, bay);
    }

    @Override
    protected int getNumBays() {
        return 2;
    }

    public enum Sequence {

        Rising(0),

        Falling(1),

        RiseFall(2),

        FallRise(3),

        RiseHoldFall(4),

        FallHoldRise(5),

        Random(6);

        private int value;

        public final int getValue() {
            return value;
        }

        public static Sequence fromInt(float value) {
            return fromInt(value);
        }

        Sequence(int value) {
            this.value = value;
        }

        public static Sequence fromInt(int value) {
            for (Sequence sequence : values()) {
                if (value == sequence.getValue())
                    return sequence;
            }
            return null;
        }
    }

    public enum ArpeggiatorJack implements IModularJack {

        None(-1);

        private int value;

        @Override
        public final int getValue() {
            return value;
        }

        ArpeggiatorJack(int value) {
            this.value = value;
        }
    }
}