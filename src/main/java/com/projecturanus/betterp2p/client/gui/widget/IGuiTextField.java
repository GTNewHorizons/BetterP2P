package com.projecturanus.betterp2p.client.gui.widget;

import appeng.client.gui.widgets.MEGuiTextField;
import com.projecturanus.betterp2p.client.gui.InfoWrapper;

public class IGuiTextField extends MEGuiTextField {
    InfoWrapper info = null;
    public void setVisible(boolean visible){
        this.field.setVisible(visible);
    }
    public void setFocus(boolean focus,int position){
        this.field.setFocused(focus);
        this.field.setCursorPosition(position);
    }
    public IGuiTextField(final int width, final int height, final String tooltip){
        super(width,height,tooltip);
    }
    public IGuiTextField(final int width, final int height)
    {
        super(width,height,"");
        this.setVisible(false);
    }
    public void setInfo(InfoWrapper _info){
        info = _info;
    }
    public InfoWrapper getInfo(){
        return info;
    }

    public void setFocus(boolean focus) {
        this.field.setFocused(focus);
    }
}
