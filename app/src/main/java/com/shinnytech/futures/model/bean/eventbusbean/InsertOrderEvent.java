package com.shinnytech.futures.model.bean.eventbusbean;

/**
 * Created on 7/5/17.
 * Created by chenli.
 * Description: .
 */

public class InsertOrderEvent {
    private boolean isInsertPopup;

    public boolean isInsertPopup() {
        return isInsertPopup;
    }

    public void setInsertPopup(boolean insertPopup) {
        isInsertPopup = insertPopup;
    }
}
