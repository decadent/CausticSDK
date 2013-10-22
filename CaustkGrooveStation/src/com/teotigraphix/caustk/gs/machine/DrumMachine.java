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

package com.teotigraphix.caustk.gs.machine;

import java.io.File;

import com.teotigraphix.caustk.gs.machine.part.bassline.DrumMachineSound;
import com.teotigraphix.caustk.gs.pattern.PartUtils;
import com.teotigraphix.caustk.gs.pattern.RhythmPart.ChannelProperty;
import com.teotigraphix.caustk.rack.tone.BeatboxTone;
import com.teotigraphix.caustk.rack.tone.components.PatternSequencerComponent.Resolution;
import com.teotigraphix.caustk.rack.tone.components.PatternSequencerComponent.ShuffleMode;
import com.teotigraphix.caustk.rack.tone.components.beatbox.WavSamplerChannel;

/*
 * Part1 - Beatbox 
 * Part2 - Beatbox
 * 
 * The DrumMachine has 2 banks of 8 channels. Each bank can be loaded with
 * a .beatbox preset.
 * 
 */
public class DrumMachine extends GrooveMachine {

    private int root = 48;

    //--------------------------------------------------------------------------
    // Constructor
    //--------------------------------------------------------------------------

    public DrumMachine() {
    }

    //--------------------------------------------------------------------------
    // Overridden :: Methods
    //--------------------------------------------------------------------------

    @Override
    protected void createComponentParts() {
        setSound(new DrumMachineSound(this));
    }

    //--------------------------------------------------------------------------
    // Public Method API
    //--------------------------------------------------------------------------

    public void triggerOn(int bank, int channel, int step, float velocity) {
        // need to use add/remove note since the beat box is polyphonic
        // can use "triggers" because we are working with pseudo channels in the sequencer
        float beat = Resolution.toBeat(step, getSound().getParts().get(bank).getPhrase()
                .getResolution());
        getSound().getParts().get(bank).getPhrase()
                .addNote(root + channel, beat, beat + 0.25f, 1f, 0);
    }

    public void triggerOff(int bank, int channel, int step) {
        // same as triggerOn()
        float beat = Resolution.toBeat(step, getSound().getParts().get(bank).getPhrase()
                .getResolution());
        getSound().getParts().get(bank).getPhrase().removeNote(root + channel, beat);
    }

    public void setSwing(int bank, float value) {
        BeatboxTone tone = (BeatboxTone)PartUtils.getTone(getSound().getParts().get(bank));
        tone.getPatternSequencer().setShuffleMode(ShuffleMode.SIXTEENTH);
        tone.getPatternSequencer().setShuffleAmount(value);
    }

    public void loadPreset(int bank, File presetFile) {
        getSound().getParts().get(bank).getPatch().loadPreset(presetFile);
    }

    public void loadChannel(int bank, int channel, File sampleFile) {
        BeatboxTone tone = (BeatboxTone)PartUtils.getTone(getSound().getParts().get(bank));
        tone.getSampler().loadChannel(channel, sampleFile.getAbsolutePath());
    }

    public float getChannelProperty(int bank, int channel, ChannelProperty property) {
        BeatboxTone tone = (BeatboxTone)PartUtils.getTone(getSound().getParts().get(bank));
        WavSamplerChannel samplerChannel = tone.getSampler().getChannel(channel);

        if (samplerChannel == null)
            throw new IllegalStateException("WavSamplerChannel null for channel:" + channel);

        float result = Float.NaN;
        switch (property) {
            case Decay:
                result = samplerChannel.getDecay();
                break;
            case Mute:
                result = samplerChannel.isMute() ? 1f : 0f;
                break;
            case Pan:
                result = samplerChannel.getPan();
                break;
            case Punch:
                result = samplerChannel.getPunch();
                break;
            case Solo:
                result = samplerChannel.isSolo() ? 1f : 0f;
                break;
            case Tune:
                result = samplerChannel.getTune();
                break;
            case Volume:
                result = samplerChannel.getVolume();
                break;
        }
        return result;
    }

    public void setChannelProperty(int bank, int channel, ChannelProperty property, float value) {
        BeatboxTone tone = (BeatboxTone)PartUtils.getTone(getSound().getParts().get(bank));
        WavSamplerChannel samplerChannel = tone.getSampler().getChannel(channel);

        if (samplerChannel == null)
            throw new IllegalStateException("WavSamplerChannel null for channel:" + channel);

        switch (property) {
            case Decay:
                samplerChannel.setDecay(value);
                break;
            case Mute:
                samplerChannel.setMute(value == 0f ? true : false);
                break;
            case Pan:
                samplerChannel.setPan(value);
                break;
            case Punch:
                samplerChannel.setPunch(value);
                break;
            case Solo:
                samplerChannel.setSolo(value == 0f ? true : false);
                break;
            case Tune:
                samplerChannel.setTune(value);
                break;
            case Volume:
                samplerChannel.setVolume(value);
                break;
        }
    }

    //--------------------------------------------------------------------------
    // Private :: Methods 
    //--------------------------------------------------------------------------

}
