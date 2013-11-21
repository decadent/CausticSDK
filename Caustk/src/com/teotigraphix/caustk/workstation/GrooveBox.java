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

package com.teotigraphix.caustk.workstation;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.teotigraphix.caustk.controller.ICaustkApplicationContext;
import com.teotigraphix.caustk.controller.IDispatcher;
import com.teotigraphix.caustk.controller.core.Dispatcher;
import com.teotigraphix.caustk.core.CausticException;
import com.teotigraphix.caustk.rack.tone.RackTone;
import com.teotigraphix.caustk.workstation.GrooveBoxDescriptor.PartDescriptor;

/**
 * @author Michael Schmalle
 */
public class GrooveBox extends CaustkComponent {

    private transient PatternBank patternBank;

    private transient IDispatcher dispatcher;

    public IDispatcher getDispatcher() {
        return dispatcher;
    }

    RackSet getRackSet() {
        return grooveSet.getRackSet();
    }

    public GrooveBoxDescriptor getDescriptor() {
        return descriptor;
    }

    protected void setDescriptor(GrooveBoxDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    public int getMachineIndex() {
        return machineIndex;
    }

    void setMachineIndex(int value) {
        machineIndex = value;
    }

    //--------------------------------------------------------------------------
    // Serialized API
    //--------------------------------------------------------------------------

    @Tag(100)
    private GrooveSet grooveSet;

    @Tag(101)
    Map<Integer, Part> parts = new TreeMap<Integer, Part>();

    @Tag(102)
    private GrooveBoxDescriptor descriptor;

    @Tag(103)
    private int machineIndex = -1;

    @Tag(104)
    private UUID patternBankId;

    @Tag(105)
    private KeyboardMode keyboardMode = KeyboardMode.Off;

    //--------------------------------------------------------------------------
    // Public API :: Properties
    //--------------------------------------------------------------------------

    //----------------------------------
    // defaultName
    //----------------------------------

    @Override
    public String getDefaultName() {
        return null;
    }

    //----------------------------------
    // grooveSet
    //----------------------------------

    public GrooveSet getGrooveSet() {
        return grooveSet;
    }

    //----------------------------------
    // parts
    //----------------------------------

    public Collection<Part> getParts() {
        return parts.values();
    }

    public Part getPart(int index) {
        return parts.get(index);
    }

    //----------------------------------
    // grooveMachineType
    //----------------------------------

    public GrooveBoxType getGrooveMachineType() {
        return descriptor.getGrooveMachineType();
    }

    public String getPatternTypeId() {
        return getGrooveMachineType().getPatternType();
    }

    //----------------------------------
    // patternBankId
    //----------------------------------

    public UUID getPatternBankId() {
        return patternBankId;
    }

    //----------------------------------
    // patternBank
    //----------------------------------

    public PatternBank getPatternBank() {
        return patternBank;
    }

    public void setPatternBank(PatternBank value) {
        patternBank = value;
        patternBankId = patternBank.getInfo().getId();
    }

    //----------------------------------
    // keyboardMode
    //----------------------------------

    public KeyboardMode getKeyboardMode() {
        return keyboardMode;
    }

    /**
     * @param value
     * @see OnGrooveBoxChange
     * @see GrooveBoxChangeKind#KeyboardMode
     */
    public void setKeyboardMode(KeyboardMode value) {
        if (value == keyboardMode)
            return;
        keyboardMode = value;
        trigger(new OnGrooveBoxChange(this, GrooveBoxChangeKind.KeyboardMode));
    }

    //----------------------------------
    // selected items
    //----------------------------------

    public final Pattern getTemporaryPattern() {
        return patternBank.getTemporaryPattern();
    }

    public final Part getSelectedPart() {
        return getTemporaryPattern().getSelectedPart();
    }

    public final Phrase getSelectedPhrase() {
        return getSelectedPart().getPhrase();
    }

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    /*
     * Serialization.
     */
    public GrooveBox() {
        dispatcher = new Dispatcher();
    }

    public GrooveBox(ComponentInfo info, GrooveSet grooveSet) {
        setInfo(info);
        this.grooveSet = grooveSet;
        dispatcher = new Dispatcher();
    }

    /**
     * Creates a new {@link Machine} and wraps it in a {@link Part} instance.
     * <p>
     * The {@link Part} is added to the {@link PatternBank}.
     * <p>
     * Calling this method will implicitly call
     * {@link Machine#create(ICaustkApplicationContext)} through the RackSet's
     * create() and create the {@link RackTone} in the native rack.
     * 
     * @param machineIndex The machine index.
     * @param machineType The {@link MachineType}.
     * @param machineName The native machine name.
     * @return A new {@link Part} instance added tot he {@link PatternBank}.
     * @throws CausticException
     */
    public Part createPart(int machineIndex, MachineType machineType, String machineName)
            throws CausticException {
        ICaustkFactory factory = getRackSet().getFactory();
        ICaustkApplicationContext context = factory.createContext();
        getRackSet().getLogger().log(
                "GrooveBox",
                "Create Part; [" + machineIndex + ", " + machineType.getType() + ", " + machineName
                        + "]");
        // this adds the machine to the rackSet, calls create()
        Machine machine = getRackSet().createMachine(machineIndex, machineName, machineType);
        ComponentInfo info = factory.createInfo(ComponentType.Part, machineName);
        Part part = factory.createPart(info, this, machine);
        part.create(context);
        parts.put(machineIndex, part);
        partAdd(part);
        getRackSet().getComponentDispatcher().trigger(
                new OnGrooveBoxChange(this, GrooveBoxChangeKind.PartAdd, part));
        return part;
    }

    @Override
    public void onLoad(ICaustkApplicationContext context) {
        super.onLoad(context);

        Library library = context.getFactory().getLibrary();
        ComponentInfo info = library.get(patternBankId);
        File location = library.resolveLocation(info);
        try {
            patternBank = context.getFactory().load(location, PatternBank.class);
            patternBank.setGrooveBox(this);
            patternBank.load(context);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (CausticException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSave(ICaustkApplicationContext context) {
        super.onSave(context);

        Library library = context.getFactory().getLibrary();
        try {
            library.refresh(patternBank);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void partAdd(Part part) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void componentPhaseChange(ICaustkApplicationContext context, ComponentPhase phase)
            throws CausticException {
        switch (phase) {
            case Create:
                //machine.create(rackSet.getFactory().createContext());
                // when the part is created it will be named '01_dm2_p1', '02_dm2_p1'
                // 01_dm2_p1 [machineIndex]_[machineType]_[partName]
                // where machineIndex is the index within the GrooveSet
                for (PartDescriptor partDescriptor : getDescriptor().getParts()) {
                    int index = grooveSet.getRackSet().getMachineCount();
                    String type = partDescriptor.getPatternTypeId();
                    String name = partDescriptor.getMachineName();
                    String machineName = machineIndex + "_" + type + "_" + name;

                    createPart(index, partDescriptor.getMachineType(), machineName);
                }
                break;
            case Load:
                // XXX Reload PatternBank from patternBankId
                // Is this from library ?
                // ok there needs to be a decision about how the library is referenced
                // there needs to be a way to resolve to location of the PatternBank here
                break;
            case Update:
                break;
            case Restore:
                break;
            case Disconnect:
                break;
            case Connect:
                break;
        }
    }

    public enum KeyboardMode {
        Off,

        Step,

        Key,

        Shift
    }

    //--------------------------------------------------------------------------
    // Event API
    //--------------------------------------------------------------------------

    protected final void trigger(Object event) {
        dispatcher.trigger(event);
    }

    public enum GrooveBoxChangeKind {

        PartAdd,

        PartRemove,

        PartReplace,

        SelectedIndex,

        KeyboardMode,
    }

    /**
     * @author Michael Schmalle
     * @see GrooveBox#getDispatcher()
     */
    public static class OnGrooveBoxChange {

        private GrooveBox grooveBox;

        private GrooveBoxChangeKind kind;

        private int index;

        private int oldIndex;

        private Part part;

        public GrooveBox getGrooveBox() {
            return grooveBox;
        }

        public GrooveBoxChangeKind getKind() {
            return kind;
        }

        /**
         * @see GrooveBoxChangeKind#SelectedIndex
         */
        public int getIndex() {
            return index;
        }

        /**
         * @see GrooveBoxChangeKind#SelectedIndex
         */
        public int getOldIndex() {
            return oldIndex;
        }

        /**
         * @see GrooveBoxChangeKind#PartAdd
         */
        public Part getPart() {
            return part;
        }

        public OnGrooveBoxChange(GrooveBox grooveBox, GrooveBoxChangeKind kind) {
            this.grooveBox = grooveBox;
            this.kind = kind;
        }

        public OnGrooveBoxChange(GrooveBox grooveBox, GrooveBoxChangeKind kind, Part part) {
            this.grooveBox = grooveBox;
            this.kind = kind;
            this.part = part;
        }
    }

    @Override
    public String toString() {
        return "[GrooveBox(" + descriptor.getPatternTypeId() + ")]";
    }

    public void noteOff(int pitch) {
        getSelectedPart().noteOff(pitch);
    }

    public void noteOn(int pitch, float velocity) {
        getSelectedPart().noteOn(pitch, velocity);
    }

    public void beatChange(int measure, float beat) {
        // CausticCore > IGame > Rack > GrooveStation > GrooveMachine
        getSelectedPhrase().beatChange(measure, beat);
    }

    public void frameChange(float delta, int measure, float beat) {
        getSelectedPhrase().frameChange(delta, measure, beat);
    }
}
