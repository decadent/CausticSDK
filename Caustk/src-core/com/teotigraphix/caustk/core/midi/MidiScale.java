
package com.teotigraphix.caustk.core.midi;

import java.util.List;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;

public class MidiScale {

    @Tag(0)
    private ScaleReference scaleReference;

    @Tag(1)
    private List<Integer> intervals;

    @Tag(2)
    private NoteReference noteReference;

    @Tag(3)
    private int octave;

    @Tag(4)
    private int numColumns;

    @Tag(5)
    private int numRows;

    public String getName() {
        return scaleReference.getName();
    }

    public ScaleReference getScaleReference() {
        return scaleReference;
    }

    /**
     * Scale root note.
     */
    public NoteReference getNoteReference() {
        return noteReference;
    }

    public int getOctave() {
        return octave;
    }

    public List<Integer> getIntervals() {
        return intervals;
    }

    public int getRootPitch() {
        return (octave * 12) + noteReference.getBaseNumber();
    }

    MidiScale() {
    }

    /**
     * Create a new scale with scale intervals, note key, octave (middle C is 5,
     * C4 zero index, octave 0 is C0).
     * 
     * @param scaleReference
     * @param noteReference
     * @param octave
     * @param numColumns
     * @param numRows
     */
    public MidiScale(ScaleReference scaleReference, NoteReference noteReference, int octave,
            int numColumns, int numRows) {
        this.scaleReference = scaleReference;
        this.noteReference = noteReference;
        this.octave = octave;
        this.numColumns = numColumns;
        this.numRows = numRows;
        updateIntervals();
    }

    /**
     * Updates the scale, key note and octave.
     * <p>
     * Fires {@link OnMidiScaleListener#onUpdate(MidiScale)} callback.
     * 
     * @param scaleReference
     * @param noteReference
     * @param octave
     */
    public void set(ScaleReference scaleReference, NoteReference noteReference, int octave) {
        this.scaleReference = scaleReference;
        this.noteReference = noteReference;
        this.octave = octave;
        updateIntervals();
        //        if (listener != null)
        //            listener.onUpdate(this);
    }

    private void updateIntervals() {
        intervals = ScaleMatrixUtils.getIntervals(scaleReference, getRootPitch(), numColumns,
                numRows);
    }

    //    private OnMidiScaleListener listener;
    //
    //    public void setListener(OnMidiScaleListener listener) {
    //        this.listener = listener;
    //    }
    //
    //    public interface OnMidiScaleListener {
    //        void onUpdate(MidiScale midiScale);
    //    }
}
