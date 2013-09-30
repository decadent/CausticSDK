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

package com.teotigraphix.caustk.sequencer.track;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import com.teotigraphix.caustk.controller.IDispatcher;
import com.teotigraphix.caustk.library.item.LibraryPhrase;
import com.teotigraphix.caustk.sequencer.ITrackSequencer.OnPhraseChange;
import com.teotigraphix.caustk.sequencer.ITrackSequencer.PhraseChangeKind;
import com.teotigraphix.caustk.tone.Tone;
import com.teotigraphix.caustk.tone.components.PatternSequencerComponent.Resolution;

/**
 * A {@link Phrase} exists as a "pattern" in a {@link Track} that has a location
 * in a native pattern_sequencer with bank and pattern.
 */
public class Phrase implements Serializable {

    private static final long serialVersionUID = -7976616079239236670L;

    private TriggerMap triggerMap;

    final IDispatcher getDispatcher() {
        return track.getDispatcher();
    }

    //--------------------------------------------------------------------------
    // Public API :: Properties
    //--------------------------------------------------------------------------

    private Track track;

    //----------------------------------
    // track
    //----------------------------------

    /**
     * Returns the {@link Track} that owns this phrase.
     */
    public final Track getTrack() {
        return track;
    }

    //----------------------------------
    // tone
    //----------------------------------

    /**
     * Returns the {@link Tone} for the parent {@link Track}.
     */
    public final Tone getTone() {
        return track.getRack().getTone(toneIndex);
    }

    //----------------------------------
    // phraseId
    //----------------------------------

    private UUID phraseId;

    /**
     * Returns the {@link LibraryPhrase} id that was used to create this phrase.
     */
    public UUID getPhraseId() {
        return phraseId;
    }

    /**
     * Sets the phrase id used to initialize this phrase.
     * 
     * @param value The id.
     */
    public void setPhraseId(UUID value) {
        phraseId = value;
    }

    //----------------------------------
    // toneIndex
    //----------------------------------

    private final int toneIndex;

    public final int getToneIndex() {
        return toneIndex;
    }

    //----------------------------------
    // bank
    //----------------------------------

    private final int bank;

    /**
     * Returns the assigned bank for this phrase in the native pattern
     * sequencer.
     */
    public final int getBank() {
        return bank;
    }

    //----------------------------------
    // pattern
    //----------------------------------

    private final int pattern;

    /**
     * Returns the assigned pattern for this phrase in the native pattern
     * sequencer.
     */
    public int getPattern() {
        return pattern;
    }

    //----------------------------------
    // length
    //----------------------------------

    private int length;

    /**
     * Returns the assigned number of measurees for this phrase in the native
     * pattern sequencer.
     */
    public final int getLength() {
        return length;
    }

    /**
     * @param value 1, 2, 4, 8
     * @see OnTrackPhraseLengthChange
     */
    public void setLength(int value) {
        if (value == length)
            return;
        length = value;

        fireChange(PhraseChangeKind.Length);

        if (position > value)
            setPosition(value);
    }

    //----------------------------------
    // noteData
    //----------------------------------

    String noteData;

    /**
     * Returns the note data serialization when initialized.
     * <p>
     * Clients need to update this with {@link #setNoteData(String)} for correct
     * serialization when a project is saved, if any sequencing commands were
     * issued.
     */
    public final String getNoteData() {
        return noteData;
    }

    /**
     * The client is responsible for setting the correct bank/pattern before
     * this call.
     * 
     * @param value
     * @see OnTrackPhraseNoteDataChange
     */
    public void setNoteData(String value) {
        noteData = value;
        if (noteData == null || noteData.equals(""))
            return; // XXX Clear the note list?

        String[] notes = noteData.split("\\|");
        for (String noteData : notes) {
            String[] split = noteData.split(" ");

            float start = Float.valueOf(split[0]);
            int pitch = Float.valueOf(split[1]).intValue();
            float velocity = Float.valueOf(split[2]);
            float end = Float.valueOf(split[3]);
            int flags = Float.valueOf(split[4]).intValue();
            addNote(pitch, start, end, velocity, flags);
        }

        fireChange(PhraseChangeKind.NoteData);
    }

    public Collection<Note> getEditMeasureNotes() {
        return getNotes(editMeasure);
    }

    //----------------------------------
    // playMeasure
    //----------------------------------

    private int playMeasure;

    /**
     * Returns the current measure of the playhead during a pattern or song
     * play.
     */
    public int getPlayMeasure() {
        return playMeasure;
    }

    /**
     * @param value
     * @see OnTrackPhrasePlayMeasureChange
     */
    public void setPlayMeasure(int value) {
        if (value == playMeasure)
            return;

        playMeasure = value;
        fireChange(PhraseChangeKind.PlayMeasure);
    }

    //----------------------------------
    // editMeasure
    //----------------------------------

    private int editMeasure;

    /**
     * Returns the current editing measure, this value is based of a specific
     * application implementation.
     */
    public int getEditMeasure() {
        return editMeasure;
    }

    /**
     * @param value
     * @see OnTrackPhraseEditMeasureChange
     */
    public void setEditMeasure(int value) {
        if (value == editMeasure)
            return;
        editMeasure = value;
        fireChange(PhraseChangeKind.EditMeasure);
    }

    //----------------------------------
    //  currentMeasure
    //----------------------------------

    private int currentMeasure = 0;

    /**
     * Returns the current measure playing in Song mode.
     * <p>
     * Note: The current bar is divisible by 4, the current measure is the sum
     * of all steps played currently in a song.
     * </p>
     * 
     * @return
     */
    public int getCurrentMeasure() {
        return currentMeasure;
    }

    void setCurrentMeasure(int value) {
        currentMeasure = value;
    }

    //----------------------------------
    //  currentBeat
    //----------------------------------

    private int currentBeat = -1;

    private float floatBeat = -1;

    private int localBeat;

    /**
     * Return the ISong current beat.
     */
    public int getCurrentBeat() {
        return currentBeat;
    }

    void setCurrentBeat(float value) {
        if (value == floatBeat)
            return;

        floatBeat = value;

        getDispatcher().trigger(new OnPhraseChange(PhraseChangeKind.Beat, this, null));
    }

    /**
     * Returns the actual beat in the current measure.
     * <p>
     * Example; measure 4, beat 14 would be beat 2 in the measure (0 index - 3rd
     * beat in measure).
     * </p>
     */
    public int getMeasureBeat() {
        return currentBeat % 4;
    }

    public int getLocalBeat() {
        return localBeat;
    }

    public void onBeatChange(float beat) {
        localBeat = (int)toLocalBeat(beat, getLength());

        float fullMeasure = beat / 4;
        float measure = fullMeasure % getLength();
        setCurrentBeat(beat);

        setPlayMeasure((int)measure);
    }

    //--------------------------------------------------------------------------
    // Constructor
    //--------------------------------------------------------------------------

    public Phrase(Track track, int toneIndex, int bank, int pattern) {
        this.track = track;
        this.toneIndex = toneIndex;
        this.bank = bank;
        this.pattern = pattern;
        triggerMap = new TriggerMap(this);
    }

    //--------------------------------------------------------------------------
    // Public API :: Methods
    //--------------------------------------------------------------------------

    /**
     * Clears all notes from all measures of the phrase.
     */
    public void clear() {
    }

    /**
     * Clears all notes from measure.
     * 
     * @see OnPhraseChange
     * @see PhraseChangeKind#ClearMeasure
     */
    public void clear(int measure) {
        Collection<Note> list = getNotes(measure);
        for (Note note : list) {
            removeNote(note);
        }
        fireChange(PhraseChangeKind.ClearMeasure);
    }

    //--------------------------------------------------------------------------
    // Added from Phrase
    //--------------------------------------------------------------------------

    //----------------------------------
    // scale
    //----------------------------------

    public enum Scale {
        SIXTEENTH, SIXTEENTH_TRIPLET, THIRTYSECOND, THIRTYSECOND_TRIPLET, SIXTYFORTH
    }

    private Scale scale = Scale.SIXTEENTH;

    /**
     * Returns the current {@link Scale} used to determine the
     * {@link Resolution}.
     */
    public Scale getScale() {
        return scale;
    }

    /**
     * Sets the new scale for the phrase.
     * <p>
     * The scale is used to calculate the {@link Resolution} of input. This
     * property mainly relates the the view of the phrase, where the underlying
     * pattern sequencer can have a higher resolution but the view is showing
     * the scale.
     * <p>
     * So you can have a phrase scale of 16, but the resolution could be 64th,
     * but the view will only show the scale notes which would be 16th.
     * 
     * @param value
     * @see OnPhraseScaleChange
     */
    public void setScale(Scale value) {
        if (scale == value)
            return;
        scale = value;
        fireChange(PhraseChangeKind.Scale);
    }

    //----------------------------------
    // position
    //----------------------------------

    private int position = 1;

    /**
     * Returns the current position in the phrase based on the rules of the
     * view.
     * <p>
     * Depending on the resolution and scale, the position can mean different
     * things.
     * <p>
     * 16th note with a length of 4, has 4 measures and thus 4 positions(1-4).
     * When the position is 2, the view triggers are 16-31(0 index).
     */
    public int getPosition() {
        return position;
    }

    /**
     * Sets the current position of the phrase.
     * 
     * @param value The new phrase position.
     * @see OnPhrasePositionChange
     */
    public void setPosition(int value) {
        if (position == value)
            return;
        // if p = 1 and len = 1
        if (value < 0 || value > getLength())
            return;
        position = value;
        fireChange(PhraseChangeKind.Position);
    }

    //----------------------------------
    // resolution
    //----------------------------------

    public Resolution getResolution() {
        switch (getScale()) {
            case SIXTEENTH:
                return Resolution.SIXTEENTH;
            case THIRTYSECOND:
                return Resolution.THIRTYSECOND;
            default:
                return Resolution.SIXTYFOURTH;
        }
    }

    //----------------------------------
    // stepCount
    //----------------------------------

    /**
     * Returns the full number of steps in all measures.
     * <p>
     * IE 4 measures of 32nd resolution has 128 steps.
     */
    public int getStepCount() {
        int numStepsInMeasure = Resolution.toSteps(getResolution());
        return numStepsInMeasure * getLength();
    }

    /**
     * Increments the internal pointer of the measure position.
     */
    public void incrementPosition() {
        int len = getLength();
        int value = position + 1;
        if (value > len)
            value = len;
        setPosition(value);
    }

    /**
     * Decrement the internal pointer of the measure position.
     */
    public void decrementPosition() {
        int value = position - 1;
        if (value < 1)
            value = 1;
        setPosition(value);
    }

    //--------------------------------------------------------------------------
    // TriggerMap Method :: API
    //--------------------------------------------------------------------------

    /**
     * @see TriggerMap#hasNote(int, float)
     */
    public boolean hasNote(int pitch, float beat) {
        return triggerMap.hasNote(pitch, beat);
    }

    /**
     * @see TriggerMap#getNotes()
     */
    public Collection<Note> getNotes() {
        return triggerMap.getNotes();
    }

    /**
     * @see TriggerMap#getNote(int, float)
     */
    public Note getNote(int pitch, float start) {
        return triggerMap.getNote(pitch, start);
    }

    /**
     * @see TriggerMap#getNotes(int)
     */
    public Collection<Note> getNotes(int measure) {
        return triggerMap.getNotes(measure);
    }

    /**
     * @see TriggerMap#addNote(int, float, float, float, int)
     */
    public Note addNote(int pitch, float beat, float gate, float velocity, int flags) {
        return triggerMap.addNote(pitch, beat, gate, velocity, flags);
    }

    /**
     * @see TriggerMap#addNote(int, int, float, float, int)
     */
    public Note addNote(int pitch, int step, float gate, float velocity, int flags) {
        return triggerMap.addNote(pitch, step, gate, velocity, flags);
    }

    /**
     * @see TriggerMap#addNote(Note)
     */
    public void addNote(Note note) {
        triggerMap.addNote(note);
    }

    /**
     * @see TriggerMap#addNotes(Collection)
     */
    public void addNotes(Collection<Note> notes) {
        triggerMap.addNotes(notes);
    }

    /**
     * @see TriggerMap#removeNote(int, float)
     */
    public Note removeNote(int pitch, float beat) {
        return triggerMap.removeNote(pitch, beat);
    }

    /**
     * @see TriggerMap#removeNote(int, int)
     */
    public Note removeNote(int pitch, int step) {
        return triggerMap.removeNote(pitch, step);
    }

    /**
     * @see TriggerMap#removeNote(Note)
     */
    public void removeNote(Note note) {
        triggerMap.removeNote(note);
    }

    /**
     * @see TriggerMap#containsTrigger(float)
     */
    public final boolean containsTrigger(float beat) {
        return triggerMap.containsTrigger(beat);
    }

    /**
     * @see TriggerMap#containsTrigger(int)
     */
    public final boolean containsTrigger(int step) {
        return triggerMap.containsTrigger(step);
    }

    /**
     * @see TriggerMap#getTrigger(float)
     */
    public final Trigger getTrigger(float beat) {
        return triggerMap.getTrigger(beat);
    }

    /**
     * @see TriggerMap#getTrigger(int)
     */
    public final Trigger getTrigger(int step) {
        return triggerMap.getTrigger(step);
    }

    /**
     * @see TriggerMap#getTriggers()
     */
    public Collection<Trigger> getTriggers() {
        return triggerMap.getTriggers();
    }

    /**
     * @see TriggerMap#getViewTriggers()
     */
    public Collection<Trigger> getViewTriggers() {
        return triggerMap.getViewTriggers();
    }

    /**
     * @see TriggerMap#getViewTriggerMap()
     */
    public Map<Integer, Trigger> getViewTriggerMap() {
        return triggerMap.getViewTriggerMap();
    }

    /**
     * @see TriggerMap#triggerOn(int, int, float, float, int)
     */
    public void triggerOn(int step, int pitch, float gate, float velocity, int flags) {
        triggerMap.triggerOn(step, pitch, gate, velocity, flags);
    }

    /**
     * @see TriggerMap#triggerOn(int)
     */
    public void triggerOn(int step) {
        triggerMap.triggerOn(step);
    }

    /**
     * @see TriggerMap#triggerOff(int)
     */
    public void triggerOff(int step) {
        triggerMap.triggerOff(step);
    }

    /**
     * @see TriggerMap#triggerUpdate(int, int, float, float, int)
     */
    public void triggerUpdate(int step, int pitch, float gate, float velocity, int flags) {
        triggerMap.triggerUpdate(step, pitch, gate, velocity, flags);
    }

    /**
     * @see TriggerMap#triggerUpdatePitch(int, int)
     */
    public void triggerUpdatePitch(int step, int pitch) {
        triggerMap.triggerUpdatePitch(step, pitch);
    }

    /**
     * @see TriggerMap#triggerUpdateGate(int, float)
     */
    public void triggerUpdateGate(int step, float gate) {
        triggerMap.triggerUpdateGate(step, gate);
    }

    /**
     * @see TriggerMap#triggerUpdateVelocity(int, float)
     */
    public void triggerUpdateVelocity(int step, float velocity) {
        triggerMap.triggerUpdateVelocity(step, velocity);
    }

    /**
     * @see TriggerMap#triggerUpdateFlags(int, int)
     */
    public void triggerUpdateFlags(int step, int flags) {
        triggerMap.triggerUpdateFlags(step, flags);
    }

    //--------------------------------------------------------------------------
    // Private Method :: API
    //--------------------------------------------------------------------------

    protected void fireChange(PhraseChangeKind kind) {
        getDispatcher().trigger(new OnPhraseChange(kind, this, null));
    }

    protected void fireChange(PhraseChangeKind kind, Note phraseNote) {
        getDispatcher().trigger(new OnPhraseChange(kind, this, phraseNote));
    }

    public static float toLocalBeat(float beat, int length) {
        float r = (beat % (length * 4));
        return r;
    }

    @Override
    public String toString() {
        return "Bank:" + bank + ",Pattern:" + pattern;
    }
}
