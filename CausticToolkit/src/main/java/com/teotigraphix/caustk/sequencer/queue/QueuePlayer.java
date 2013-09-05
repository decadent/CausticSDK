
package com.teotigraphix.caustk.sequencer.queue;

import java.util.ArrayList;
import java.util.List;

import com.teotigraphix.caustk.controller.ICaustkController;
import com.teotigraphix.caustk.core.CausticException;
import com.teotigraphix.caustk.core.CtkDebug;
import com.teotigraphix.caustk.sequencer.IQueueSequencer.OnQueueSequencerDataChange;
import com.teotigraphix.caustk.sequencer.ISystemSequencer.SequencerMode;
import com.teotigraphix.caustk.sequencer.queue.QueueData.QueueDataState;
import com.teotigraphix.caustk.sequencer.track.TrackChannel;
import com.teotigraphix.caustk.sequencer.track.TrackItem;
import com.teotigraphix.caustk.sequencer.track.TrackPhrase;
import com.teotigraphix.caustk.sequencer.track.TrackSong;

public class QueuePlayer {

    private final ICaustkController getController() {
        return queueSequencer.getController();
    }

    public final TrackSong getTrackSong() {
        return getController().getTrackSequencer().getTrackSong();
    }

    private int currentLocalBeat;

    private List<QueueData> playQueue = new ArrayList<QueueData>();

    private List<QueueData> tempPlayQueue = new ArrayList<QueueData>();

    private List<QueueData> queued = new ArrayList<QueueData>();

    private List<QueueData> flushedQueue = new ArrayList<QueueData>();

    List<QueueData> getPlayQueue() {
        return playQueue;
    }

    List<QueueData> getQueued() {
        return queued;
    }

    List<QueueData> getFlushedQueue() {
        return flushedQueue;
    }

    int getCurrentLocalBeat() {
        return currentLocalBeat;
    }

    private QueueSequencer queueSequencer;

    public QueuePlayer(QueueSequencer queueSequencer) {
        this.queueSequencer = queueSequencer;
    }

    public final boolean isLockBeat() {
        return currentLocalBeat == 3;
    }

    public final boolean isStartBeat() {
        return currentLocalBeat == 0;
    }

    public void play() throws CausticException {
        ArrayList<QueueData> copied = new ArrayList<QueueData>(queued);
        for (QueueData data : copied) {
            if (data.getState() == QueueDataState.Queue) {

                // add to sequencer
                TrackChannel track = getTrackSong().getTrack(data.getViewChannelIndex());
                addPhraseAt(track, 0, data);

                // this is special instance since everything is started
                setState(data, QueueDataState.Play);
                // automatically add to the playQueue
                playQueue.add(data);
                queued.remove(data);
            } else if (data.getState() == QueueDataState.Play) {
            }
        }
        if (queueSequencer.isAudioEnabled())
            getController().getSystemSequencer().play(SequencerMode.SONG);
    }

    public void stop() {
        getController().getSystemSequencer().stop();
    }

    public boolean touch(QueueData data) {

        // QUEUE
        if (isLockBeat())
            return false;
        log("queue(" + data + ")");

        // if the pattern is still playing but got unqueued, just update the state
        if (playQueue.contains(data)) {

            setState(data, QueueDataState.PlayUnqueued);

            //            if (data.getState() == QueueDataState.Queue) {
            //                //playQueue.remove(data);
            //                setState(data, QueueDataState.Idle);
            //            } else {
            //                setState(data, QueueDataState.UnQueued);
            //            }

        } else if (!queued.contains(data)) {

            QueueDataChannel viewChannel = data.getViewChannel();
            QueueData invalidData = channelInvalidatesPlayingData(viewChannel);
            // check for the existence of another channel that shares this track
            if (invalidData != null) {
                setState(invalidData, QueueDataState.UnQueued);
                queued.add(data);
                setState(data, QueueDataState.Queue);
            } else {
                queued.add(data);
                setState(data, QueueDataState.Queue);
            }
        } else {
            // queued but not playing, just remove and set back to Idle
            queued.remove(data);
            setState(data, QueueDataState.Idle);
        }

        return false;
    }

    private boolean queue(QueueData data) {
        if (isLockBeat())
            return false;
        log("queue(" + data + ")");

        // if the pattern is still playing but got unqueued, just update the state
        if (playQueue.contains(data)) {
            setState(data, QueueDataState.Play);
        } else if (!queued.contains(data)) {
            QueueDataChannel viewChannel = data.getViewChannel();
            QueueData invalidData = channelInvalidatesPlayingData(viewChannel);
            // check for the existence of another channel that shares this track
            if (invalidData != null) {
                setState(invalidData, QueueDataState.UnQueued);
                queued.add(data);
                setState(data, QueueDataState.Queue);
            } else {
                queued.add(data);
                setState(data, QueueDataState.Queue);
            }
        }
        return true;
    }

    private boolean unqueue(QueueData data) {
        if (isLockBeat())
            return false;
        log("unqueue(" + data + ")");
        if (playQueue.contains(data)) {
            if (data.getState() == QueueDataState.Queue) {
                //playQueue.remove(data);
                setState(data, QueueDataState.Idle);
            } else {
                setState(data, QueueDataState.UnQueued);
            }
        } else {
            queued.remove(data);
            setState(data, QueueDataState.Idle);
        }

        return true;
    }

    public void beatChange(int measure, float beat) {
        getTrackSong().setPosition(measure, beat);

        //System.out.println("M:" + getTrackSong().getCurrentMeasure() + "B:"
        //        + getTrackSong().getCurrentBeat());

        currentLocalBeat = (int)(beat % 4);

        // start new measure
        if (isStartBeat()) {
            //log("|Start Beat================================================");

            for (QueueData data : tempPlayQueue) {
                setState(data, QueueDataState.Play);
                playQueue.add(data);
            }

            tempPlayQueue.clear();

            for (QueueData data : flushedQueue) {
                if (data.getState() != QueueDataState.Queue) {
                    setState(data, QueueDataState.Idle);
                }
            }

            flushedQueue.clear();
        }

        // last beat in measure
        if (isLockBeat()) {
            //log("Lock measure");
            extendOrRemovePlayingTracks();

            queueTracks();

            //log("|End Beat================================================");
        }
    }

    private void extendOrRemovePlayingTracks() {
        //final float currentBeat = getTrackSong().getCurrentBeat();
        final int currentMeasure = getTrackSong().getCurrentMeasure();

        for (TrackChannel track : getController().getTrackSequencer().getTracks()) {

            // Find all tracks that are ending at the next measure
            TrackItem item = track.getItemAtEndMeasure(currentMeasure + 1);
            // no item queued or playing at the measure
            if (item != null) {
                updateChannel(track, item);
            }
        }
    }

    private void updateChannel(TrackChannel track, TrackItem item) {
        final int currentMeasure = getTrackSong().getCurrentMeasure();

        QueueData data = queueSequencer.getQueueData(item.getBankIndex(), item.getPatternIndex());
        QueueDataChannel channel = data.getChannel(data.getViewChannelIndex());

        // data is the Play state
        if (data.getState() == QueueDataState.UnQueued) {

            stopPlayingUnqueue(data);

        } else if (channel.isLoopEnabled()) {

            if (data.getState() == QueueDataState.Play) {
                // add the phrase at the very next measure
                addPhraseAt(track, currentMeasure + 1, data);
                // channel.setCurrentBeat(0);
            }

        } else {

            // oneshot remove
            stopPlayingUnqueue(data);

        }
    }

    private void queueTracks() {
        //final float currentBeat = getTrackSong().getCurrentBeat();
        final int currentMeasure = getTrackSong().getCurrentMeasure();
        final int nextMeasure = currentMeasure + 1;

        // CtkDebug.log("Setup queued [" + currentBeat + "," + currentMeasure + "]");

        ArrayList<QueueData> copied = new ArrayList<QueueData>(queued);
        // loop through the queue and add queued items
        for (QueueData data : copied) {
            if (data.getState() == QueueDataState.Queue) {
                // add to sequencer
                for (QueueDataChannel channel : data.getChannels()) {
                    TrackChannel track = getTrackSong().getTrack(channel.getToneIndex());
                    addPhraseAt(track, nextMeasure, data);
                }
                startPlaying(data);
            } else if (data.getState() == QueueDataState.Play) {

            }
        }
    }

    private void addPhraseAt(TrackChannel track, int start, QueueData data) {
        try {
            TrackPhrase phrase = track.getPhrase(data.getBankIndex(), data.getPatternIndex());
            track.addPhraseAt(start, 1, phrase, true);
        } catch (CausticException e) {
            e.printStackTrace();
        }
    }

    private QueueData channelInvalidatesPlayingData(QueueDataChannel queuedData) {
        for (QueueData playData : playQueue) {
            if (playData.getViewChannelIndex() == queuedData.getToneIndex())
                return playData;
        }
        return null;
    }

    private void startPlaying(QueueData data) {
        //log("startPlaying" + data);
        queued.remove(data);
        tempPlayQueue.add(data);
    }

    private void stopPlayingUnqueue(QueueData data) {
        //log("stopPlaying" + data);
        flushedQueue.add(data);
        playQueue.remove(data);
        // things will get set to idle in flush if not queued again
        setState(data, QueueDataState.PlayUnqueued);
    }

    private void setState(QueueData data, QueueDataState state) {
        data.setState(state);
        queueSequencer.getDispatcher().trigger(new OnQueueSequencerDataChange(data));
    }

    private void log(String message) {
        CtkDebug.log(message);
    }
}
