package com.mediatek.wwtv.tvcenter.nav.inputpanel;

public interface IInputsPanelView {

  void notifyInputsChanged();
  void notifyFocusChanged();
  void notifyFocusToNext();
  void removeMessageForOnKey();
}
