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

import java.util.ArrayList;
import java.util.List;

import com.teotigraphix.caustk.controller.ICaustkController;
import com.teotigraphix.caustk.core.CausticException;
import com.teotigraphix.caustk.gs.machine.GrooveStation.GrooveMachineDescriptor;
import com.teotigraphix.caustk.gs.machine.GrooveStation.GrooveMachinePart;
import com.teotigraphix.caustk.gs.machine.part.MachineControls;
import com.teotigraphix.caustk.gs.machine.part.MachineFooter;
import com.teotigraphix.caustk.gs.machine.part.MachineHeader;
import com.teotigraphix.caustk.gs.machine.part.MachineSequencer;
import com.teotigraphix.caustk.gs.machine.part.MachineSound;
import com.teotigraphix.caustk.gs.machine.part.MachineSystem;
import com.teotigraphix.caustk.gs.machine.part.MachineTransport;
import com.teotigraphix.caustk.gs.memory.Memory.Category;
import com.teotigraphix.caustk.gs.memory.Memory.Type;
import com.teotigraphix.caustk.gs.memory.MemoryBank;
import com.teotigraphix.caustk.gs.memory.MemoryManager;
import com.teotigraphix.caustk.gs.memory.item.PatternMemoryItem;
import com.teotigraphix.caustk.gs.memory.item.PhraseMemoryItem;
import com.teotigraphix.caustk.gs.pattern.Part;
import com.teotigraphix.caustk.gs.pattern.PartUtils;
import com.teotigraphix.caustk.gs.pattern.Patch;
import com.teotigraphix.caustk.gs.pattern.Pattern;
import com.teotigraphix.caustk.gs.pattern.Phrase;
import com.teotigraphix.caustk.gs.pattern.RhythmPart;
import com.teotigraphix.caustk.gs.pattern.SynthPart;
import com.teotigraphix.caustk.tone.BeatboxTone;
import com.teotigraphix.caustk.tone.Tone;
import com.teotigraphix.caustk.utils.PatternUtils;

public abstract class GrooveMachine {

    protected final Pattern getPattern() {
        return getMachineSequencer().getPattern();
    }

    public final Phrase getSelectedPhrase() {
        return getMachineSequencer().getPattern().getSelectedPart().getPhrase();
    }

    public final Patch getSelectedPatch() {
        return getMachineSequencer().getPattern().getSelectedPart().getPatch();
    }

    //----------------------------------
    // nextPatternIndex
    //----------------------------------

    private int nextPatternIndex = -1;

    public int getNextPatternIndex() {
        return nextPatternIndex;
    }

    public void setNextPatternIndex(int value) {
        if (value == nextPatternIndex)
            return;
        nextPatternIndex = value;
        getMachineSequencer().setNextPattern(nextPatternIndex);
    }

    //----------------------------------
    // memoryManager
    //----------------------------------

    private MemoryManager memoryManager;

    public MemoryManager getMemoryManager() {
        return memoryManager;
    }

    //----------------------------------
    // machineSound
    //----------------------------------

    private MachineSound machineSound;

    public MachineSound getMachineSound() {
        return machineSound;
    }

    void setMachineSound(MachineSound value) {
        machineSound = value;
    }

    //----------------------------------
    // machineHeader
    //----------------------------------

    private MachineHeader machineHeader;

    public MachineHeader getMachineHeader() {
        return machineHeader;
    }

    void setMachineHeader(MachineHeader value) {
        machineHeader = value;
    }

    //----------------------------------
    // machineSystem
    //----------------------------------

    private MachineSystem machineSystem;

    public MachineSystem getMachineSystem() {
        return machineSystem;
    }

    public void setMachineSystem(MachineSystem value) {
        machineSystem = value;
    }

    //----------------------------------
    // machineTransport
    //----------------------------------

    private MachineTransport machineTransport;

    public MachineTransport getMachineTransport() {
        return machineTransport;
    }

    public void setMachineTransport(MachineTransport value) {
        machineTransport = value;
    }

    //----------------------------------
    // machineSequencer
    //----------------------------------

    private MachineSequencer machineSequencer;

    public MachineSequencer getMachineSequencer() {
        return machineSequencer;
    }

    public void setMachineSequencer(MachineSequencer value) {
        machineSequencer = value;
    }

    //----------------------------------
    // machineControls
    //----------------------------------

    private MachineControls machineControls;

    public MachineControls getMachineControls() {
        return machineControls;
    }

    public void setMachineControls(MachineControls value) {
        machineControls = value;
    }

    //----------------------------------
    // machineFooter
    //----------------------------------

    private MachineFooter machineFooter;

    public MachineFooter getMachineFooter() {
        return machineFooter;
    }

    public void setMachineFooter(MachineFooter value) {
        machineFooter = value;
    }

    //----------------------------------
    // machineFooter
    //----------------------------------

    private ICaustkController controller;

    public ICaustkController getController() {
        return controller;
    }

    public void setController(ICaustkController controller) {
        this.controller = controller;
        createMainComponentParts();
        createComponentParts();
    }

    //--------------------------------------------------------------------------
    // Constructor
    //--------------------------------------------------------------------------

    public GrooveMachine() {

    }

    protected void createMainComponentParts() {
        memoryManager = new MemoryManager(this);
        memoryManager.setSelectedMemoryType(Type.USER);
        memoryManager.setSelectedMemoryCategory(Category.PATTERN);
        machineSequencer = new MachineSequencer(this);
    }

    protected abstract void createComponentParts();

    //--------------------------------------------------------------------------
    // Method API
    //--------------------------------------------------------------------------

    public void write() throws CausticException {
        machineSequencer.write();
    }

    /**
     * Mutes/unmutes all parts of the machine.
     * 
     * @param muted Whether the machine is muted.
     */
    public void setMute(boolean muted) {
        for (Part part : getParts()) {
            part.setMute(muted);
        }
    }

    private List<Part> parts = new ArrayList<Part>();

    public List<Part> getParts() {
        return parts;
    }

    public void setup(GrooveMachineDescriptor descriptor) throws CausticException {
        setupParts(descriptor);
        setupPatterns();
    }

    private void setupParts(GrooveMachineDescriptor descriptor) throws CausticException {
        for (GrooveMachinePart partDescriptor : descriptor.getParts()) {

            Tone tone = controller.getSoundSource().createTone(partDescriptor.getName(),
                    partDescriptor.getToneType());

            Part part = createPart(tone);
            parts.add(part);
        }
    }

    protected void setupPatterns() {
        MemoryBank memoryBank = getMemoryManager().getSelectedMemoryBank();
        for (int i = 0; i < 64; i++) {
            Pattern pattern = memoryBank.getPattern(i);
            PatternMemoryItem patternItem = pattern.getMemoryItem();
            memoryBank.getPatternSlot().addItem(patternItem);

            // have to add parts first, set length, then set note data

            for (Part part : parts) {
                pattern.addPart(part);
                Phrase phrase = memoryBank.getPhrase(part);
                PhraseMemoryItem phraseItem = phrase.getMemoryItem();
                memoryBank.getPhraseSlot().addItem(phraseItem);

                int bankIndex = PatternUtils.getBank(i);
                int patternIndex = PatternUtils.getPattern(i);
                PartUtils.setBankPattern(part, bankIndex, patternIndex);
                phrase.configure();

            }
        }

        // reset bank/pattern to 0
        for (Part part : parts) {
            PartUtils.setBankPattern(part, 0, 0);
        }
    }

    protected Part createPart(Tone tone) {
        Part part = null;
        if (tone instanceof BeatboxTone) {
            part = new RhythmPart(tone);
        } else {
            part = new SynthPart(tone);
        }
        return part;
    }

    void beatChange(int measure, float beat) {
        // CausticCore > IGame > ISystemSequencer > GrooveStation > GrooveMachine
        machineSequencer.beatChange(measure, beat);
    }

}