
package com.teotigraphix.libgdx.ui.caustk;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.utils.Array;
import com.teotigraphix.libgdx.ui.ControlTable;
import com.teotigraphix.libgdx.ui.Led;

public class SelectLedControl extends ControlTable {

    private Button decButton;

    private Button incButton;

    private Table ledTable;

    private Array<Led> top = new Array<Led>();

    private Array<Led> bottom = new Array<Led>();

    public enum ActiveSeletor {
        TOP, BOTTOM;
    }

    //--------------------------------------------------------------------------
    // Property :: API
    //--------------------------------------------------------------------------

    //----------------------------------
    // activeSelector
    //----------------------------------

    private ActiveSeletor activeSelector = ActiveSeletor.BOTTOM;

    public ActiveSeletor getActiveSelector() {
        return activeSelector;
    }

    public void setActiveSelector(ActiveSeletor value) {
        activeSelector = value;
    }

    //----------------------------------
    // topIndex
    //----------------------------------

    private int topIndex;

    public int getTopIndex() {
        return topIndex;
    }

    public void setTopIndex(int value) {
        if (value == topIndex)
            return;
        topIndex = value;
        invalidate();
        onSelectLedControlListener.onTopIndexChange(topIndex);
    }

    //----------------------------------
    // bottomIndex
    //----------------------------------

    private int bottomIndex;

    public int getBottomIndex() {
        return bottomIndex;
    }

    public void setBottomIndex(int value) {
        if (value == bottomIndex)
            return;
        bottomIndex = value;
        invalidate();
        onSelectLedControlListener.onBottomIndexChange(bottomIndex);
    }

    //--------------------------------------------------------------------------
    // Constructor
    //--------------------------------------------------------------------------

    public SelectLedControl(Skin skin) {
        super(skin);
        create(skin);
    }

    private void create(Skin skin) {

        ButtonStyle decButtonStyle = new ButtonStyle();
        decButtonStyle.up = skin.getDrawable("pad_up");
        decButtonStyle.down = skin.getDrawable("pad_down");
        skin.add("select-led-control-dec-button", decButtonStyle);

        ButtonStyle incButtonStyle = new ButtonStyle();
        incButtonStyle.up = skin.getDrawable("pad_up");
        incButtonStyle.down = skin.getDrawable("pad_down");
        skin.add("select-led-control-inc-button", incButtonStyle);
    }

    //--------------------------------------------------------------------------
    // Overridden :: Methods
    //--------------------------------------------------------------------------

    @Override
    protected void createChildren() {

        defaults().space(10f);

        Image decImage = new Image(getSkin().getDrawable("select_led_control_dec"));
        decButton = new Button(decImage, getSkin(), "select-led-control-dec-button");
        decButton.addListener(new ActorGestureListener() {
            @Override
            public void touchDown(InputEvent event, float x, float y, int pointer, int button) {
                decrement();
            }
        });
        add(decButton).size(55f, 35f).pad(4f);

        createLedTable();

        Image incImage = new Image(getSkin().getDrawable("select_led_control_inc"));
        incButton = new Button(incImage, getSkin(), "select-led-control-inc-button");
        add(incButton).size(55f, 35f).pad(4f);
        incButton.addListener(new ActorGestureListener() {
            @Override
            public void touchDown(InputEvent event, float x, float y, int pointer, int button) {
                increment();
            }
        });
    }

    @Override
    public void layout() {
        super.layout();

        for (int i = 0; i < 8; i++) {
            top.get(i).turnOff();
        }
        top.get(topIndex).turnOn();

        for (int i = 0; i < 8; i++) {
            bottom.get(i).turnOff();
        }
        bottom.get(bottomIndex).turnOn();
    }

    //--------------------------------------------------------------------------
    // Listeners
    //--------------------------------------------------------------------------

    private OnSelectLedControlListener onSelectLedControlListener;

    public void setOnSelectLedControlListener(OnSelectLedControlListener l) {
        onSelectLedControlListener = l;
    }

    public interface OnSelectLedControlListener {
        void onIncrement(int index);

        void onDecrement(int index);

        void onTopIndexChange(int index);

        void onBottomIndexChange(int index);
    }

    //--------------------------------------------------------------------------
    // Private :: Methods
    //--------------------------------------------------------------------------

    private int getActiveIndex() {
        if (activeSelector == ActiveSeletor.TOP)
            return getTopIndex();
        return getBottomIndex();
    }

    private void createLedTable() {
        ledTable = new Table();
        ledTable.defaults().space(5f);
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 8; j++) {
                Led led = new Led(getSkin());
                if (i == 0) {
                    top.add(led);
                } else {
                    bottom.add(led);
                }
                led.setStyleName(i == 0 ? "default" : "default-green");
                ledTable.add(led);
            }
            ledTable.row();
        }
        add(ledTable);
    }

    private void setActiveIndex(int index) {
        if (activeSelector == ActiveSeletor.TOP)
            setTopIndex(index);
        setBottomIndex(index);
    }

    protected void increment() {
        int index = getActiveIndex() + 1;
        if (index > 7)
            index = 0;
        setActiveIndex(index);
        onSelectLedControlListener.onIncrement(index);
    }

    protected void decrement() {
        int index = getActiveIndex() - 1;
        if (index < 0)
            index = 7;
        setActiveIndex(index);
        onSelectLedControlListener.onDecrement(index);
    }
}
