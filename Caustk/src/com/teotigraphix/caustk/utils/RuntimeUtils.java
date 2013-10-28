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

package com.teotigraphix.caustk.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Locale;

import org.apache.commons.io.FileUtils;

import com.teotigraphix.caustk.machine.MachineType;

/**
 * @author Michael Schmalle
 * @copyright Teoti Graphix, LLC
 * @since 1.0
 */
public class RuntimeUtils {

    private static final String FORWARD_SLASH = "/";

    private static final String DOT = ".";

    private static final String CAUSTIC = "caustic";

    private static final String CAUSTIC_EXTENSION = ".caustic";

    private static final String CAUSTIC_PRESETS = "caustic/presets";

    private static final String CAUSTIC_SAMPLES = "caustic/samples";

    private static final String CAUSTIC_SONGS = "caustic/songs";

    /**
     * This has to be set at Application startup before ANYTHING happens
     * Android; Environment.getExternalStorageDirectory();
     */
    public static String STORAGE_ROOT = null;

    public static String APP_ROOT = null;

    /**
     * /mnt/sdcard/
     */
    public static final File getExternalStorageDirectory() {
        //File file = Environment.getExternalStorageDirectory();
        if (STORAGE_ROOT == null)
            throw new RuntimeException("STORAGE_ROOT is null, set in RuntimeUtils");
        File directory = new File(STORAGE_ROOT);
        return directory;
    }

    public static final File getApplicationDirectory() {
        if (APP_ROOT == null)
            throw new RuntimeException("APP_ROOT is null, set in RuntimeUtils");
        File directory = new File(APP_ROOT);
        return directory;
    }

    public static final File getApplicationDirectory(String path) {
        File directory = new File(getApplicationDirectory(), path);
        return directory;
    }

    /**
     * Returns a File directory that is based off the
     * {@link #getExternalStorageDirectory()} (/mnt/sdcard/) path.
     * 
     * @param path The path to append to the public device directory.
     */
    public static final File getDirectory(String path) {
        File directory = new File(getExternalStorageDirectory(), path);
        return directory;
    }

    /**
     * Returns a File directory based off the application's root directory.
     * 
     * @param applicationDirectory Usually /mnt/sdcard/MyApp
     * @param path The child folder hierarchy.
     */
    public static File getDirectory(File applicationDirectory, String path) {
        File directory = new File(applicationDirectory, path);
        return directory;
    }

    /**
     * Converts and {@link InputStream} to a String.
     * 
     * @param is The {@link InputStream} to read into a String.
     * @return THe String read from the stream.
     * @throws IOException
     */
    public static final String convertStreamToString(InputStream is) throws IOException {
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } finally {
            is.close();
        }
        return writer.toString();
    }

    /**
     * Loads a String from the File.
     * 
     * @param file The location of the File to load.
     * @return The String loaded from the File.
     * @throws IOException
     */
    public static final String loadFile(File file) throws IOException {
        FileReader reader = new FileReader(file);
        BufferedReader br = new BufferedReader(reader);
        StringBuffer s = new StringBuffer();
        String line;
        while ((line = br.readLine()) != null) {
            s.append(line);
            s.append("\n");
        }
        reader.close();
        return s.toString();
    }

    /**
     * Saves the String data to the File.
     * 
     * @param file The location to save the String.
     * @param data The String data to save.
     * @throws IOException
     */
    public static final void saveFile(File file, String data) throws IOException {
        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
            writer.write(data);
        } catch (IOException e) {
            throw e;
        } finally {
            writer.close();
        }
    }

    /**
     * Loads an {@link IMemento} from a file.
     * 
     * @param file The file to load.
     * @return An {@link IMemento} load from the file's String data.
     * @throws IOException
     */
    //    public static final IMemento loadMemento(File file) throws IOException {
    //        FileReader reader = new FileReader(file);
    //        IMemento memento = XMLMemento.createReadRoot(reader);
    //        return memento;
    //    }

    /**
     * Saves an {@link IMemento} to a file.
     * 
     * @param file The file to save.
     * @param memento The {@link IMemento} to save to the file.
     * @throws IOException
     */
    //    public static final void saveMemento(File file, IMemento memento) throws IOException {
    //        FileWriter writer = new FileWriter(file);
    //        memento.save(writer);
    //    }

    //--------------------------------------------------------------------------
    //
    // Caustic File Locations
    //
    //--------------------------------------------------------------------------

    /**
     * Returns the <code>/sdcard/caustic</code> directory.
     */
    public static File getCausticDirectory() {
        return new File(getExternalStorageDirectory(), CAUSTIC);
    }

    /**
     * Returns the <code>/sdcard/caustic/presets</code> directory.
     */
    public static File getCausticPresetsDirectory() {
        return new File(getExternalStorageDirectory(), CAUSTIC_PRESETS);
    }

    /**
     * Returns a preset file located in the
     * <code>/sdcard/caustic/presets/[prestType]/[presetName].[presetType]</code>
     * .
     * 
     * @param presetType The lowercase preset type (<code>bassline</code>,
     *            <code>beatbox</code>, <code>pcmsynth</code>,
     *            <code>subsynth</code>).
     * @param presetName The name of the preset file without the preset
     *            extension.
     * @deprecated
     */
    @Deprecated
    public static File getCausticPresetsFile(String presetType, String presetName) {
        final StringBuilder sb = new StringBuilder();
        sb.append(presetType);
        sb.append(FORWARD_SLASH);
        if (presetType.equals("modular"))
            sb.append(presetName.toUpperCase(Locale.US));
        else
            sb.append(presetName);
        sb.append(DOT);
        if (presetType.equals("modular"))
            presetType = "modularsynth";
        sb.append(presetType);
        return new File(getCausticPresetsDirectory(), sb.toString());
    }

    /**
     * Returns a preset file located in the native Caustic app
     * <code>/caustic/presets</code> directory.
     * <p>
     * If a relative path is provided, the prefix preset type directory should
     * not be included, this is added automatically such as
     * <code>subsynth</code> or <code>modular</code>.
     * 
     * @param machineType The {@link MachineType}.
     * @param nameOrRelativePath The preset name or preset name with relative
     *            path without extension. e.g <code>MyPreset</code>,
     *            <code>sub/dir/MyPreset</code>.
     */
    public static File getCausticPresetsFile(MachineType machineType, String nameOrRelativePath) {
        final StringBuilder sb = new StringBuilder();
        sb.append(machineType.getType());
        sb.append(FORWARD_SLASH);
        sb.append(nameOrRelativePath);
        sb.append(DOT);
        sb.append(machineType.getExtension());
        return new File(getCausticPresetsDirectory(), sb.toString());
    }

    /**
     * Returns the <code>/sdcard/caustic/samples</code> directory.
     */
    public static File getCausticSamplesDirectory() {
        return new File(getExternalStorageDirectory(), CAUSTIC_SAMPLES);
    }

    public static File getCausticSamplesFile(final String sampleType, final String sampleName) {
        final StringBuilder sb = new StringBuilder();
        sb.append(sampleType);
        sb.append(FORWARD_SLASH);
        sb.append(sampleName);
        sb.append(".wav");
        return new File(getCausticSamplesDirectory(), sb.toString());
    }

    /**
     * Returns the <code>/sdcard/caustic/songs</code> directory.
     */
    public static File getCausticSongsDirectory() {
        return new File(getExternalStorageDirectory(), CAUSTIC_SONGS);
    }

    /**
     * Returns a <code>.caustic</code> song located in the
     * {@link #getCausticSongsDirectory()}.
     * 
     * @param songName The song name without the <code>.caustic</code>
     *            extension.
     */
    public static File getCausticSongFile(String songName) {
        return new File(getCausticSongsDirectory(), songName + CAUSTIC_EXTENSION);
    }

    public static final void copyDirectory(File src, File dest) throws IOException {
        FileUtils.copyDirectory(src, dest);
    }

    public static final void deleteDirectory(File file) throws IOException {
        FileUtils.deleteDirectory(file);
    }

    public static final String readFileToString(File file) throws IOException {
        return FileUtils.readFileToString(file);
    }

    public static final FileInputStream openInputStream(File file) throws IOException {
        return FileUtils.openInputStream(file);
    }
}
