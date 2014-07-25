
package com.teotigraphix.caustk.groove;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import com.teotigraphix.caustk.core.CausticException;

public class LibraryGroup extends LibraryProductItem {

    private Map<Integer, LibrarySound> sounds = new TreeMap<Integer, LibrarySound>();

    public LibrarySound getSound(int index) {
        return sounds.get(index);
    }

    public Collection<LibrarySound> getSounds() {
        return sounds.values();
    }

    public LibraryGroup(UUID id, UUID productId, FileInfo fileInfo, LibraryItemManifest manifest) {
        super(id, productId, fileInfo, manifest);
        setFormat(LibraryItemFormat.Group);
    }

    public void addSound(int index, LibrarySound sound) throws CausticException {
        if (sounds.containsKey(index))
            throw new CausticException("Slot not empty, use replace ");
        sound.setIndex(index);
        sounds.put(index, sound);
        sound.setGroup(this);
    }
}
