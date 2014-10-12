
package com.teotigraphix.gdx.controller;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;

public class ViewBase {

    @Tag(0)
    private int id;

    @Tag(1)
    private int index;

    @Tag(2)
    private String label;

    //--------------------------------------------------------------------------
    // Private :: Variables
    //--------------------------------------------------------------------------

    private boolean canScrollUpFlag;

    private boolean canScrollRightFlag;

    private boolean canScrollLeftFlag;

    private boolean canScrollDownFlag;

    //--------------------------------------------------------------------------
    // Public API :: Properties
    //--------------------------------------------------------------------------

    public int getId() {
        return id;
    }

    public int getIndex() {
        return index;
    }

    public String getLabel() {
        return label;
    }

    //----------------------------------
    // model
    //----------------------------------

    public boolean canScrollRight() {
        return canScrollRightFlag;
    }

    public boolean canScrollUp() {
        return canScrollUpFlag;
    }

    public boolean canScrollLeft() {
        return canScrollLeftFlag;
    }

    public boolean canScrollDown() {
        return canScrollDownFlag;
    }

    protected void setCanScrollDown(boolean canScrollDown) {
        this.canScrollDownFlag = canScrollDown;
    }

    protected void setCanScrollLeft(boolean canScrollLeft) {
        this.canScrollLeftFlag = canScrollLeft;
    }

    protected void setCanScrollRight(boolean canScrollRight) {
        this.canScrollRightFlag = canScrollRight;
    }

    protected void setCanScrollUp(boolean canScrollUp) {
        this.canScrollUpFlag = canScrollUp;
    }

    //--------------------------------------------------------------------------
    // Constructor
    //--------------------------------------------------------------------------

    protected ViewBase() {
    }

    public ViewBase(int id, int index, String label) {
        this.id = id;
        this.index = index;
        this.label = label;
    }

    public void onArrowUp(boolean down) {

    }

    public void onArrowRight(boolean down) {

    }

    public void onArrowLeft(boolean down) {

    }

    public void onArrowDown(boolean down) {

    }

    public void updateArrows() {

    }

    public void onActivate() {
    }

    public void updateDevice() {
        //        Object m = this.surface.getActiveMode ();
        //        if (m != null)
        //        {
        //            m.updateDisplay ();
        //            m.updateFirstRow ();
        //            m.updateSecondRow ();
        //        }
        //        this.updateButtons ();
        //        this.updateArrows ();
    }
}
