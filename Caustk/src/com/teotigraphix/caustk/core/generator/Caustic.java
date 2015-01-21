////////////////////////////////////////////////////////////////////////////////
// Copyright 2012 Michael Schmalle - Teoti Graphix, LLC
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

package com.teotigraphix.caustk.core.generator;

import android.util.Log;

import com.singlecellsoftware.causticcore.CausticCore;
import com.teotigraphix.caustk.core.CaustkEngine;

/**
 * @author Michael Schmalle
 * @copyright Teoti Graphix, LLC
 * @since 1.0
 */
public class Caustic extends CausticCore {

    private static final String TAG = "Caustic";

    public Caustic() {
        super();
    }

    @Override
    public float SendOSCMessage(String message) {
        if (CaustkEngine.DEBUG_MESSAGES) {
            Log.d(TAG, "Message: " + message);
        }
        return super.SendOSCMessage(message);
    }

    @Override
    public String QueryOSC(String message) {
        if (!CaustkEngine.DEBUG_QUERIES) {
            Log.d(TAG, "Query: " + message);
        }
        return super.QueryOSC(message);
    }
}