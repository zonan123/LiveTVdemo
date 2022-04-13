package com.mediatek.wwtv.tvcenter.nav;

//import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;


import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelInfo;
import com.mediatek.tv.recommended.aidl.IChannelMidManager;
import com.mediatek.wwtv.tvcenter.nav.util.ComponentsManager;
import com.mediatek.wwtv.tvcenter.nav.view.ChannelListDialog;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasic;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasicDialog;
public class HomeChannelService extends Service {

    private static final String TAG = "HomeChannelService";
    private boolean serviceDialog = false;
    
    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return stub;
    }
    IChannelMidManager.Stub stub=new IChannelMidManager.Stub(){
        @Override
        public long[] getFavChannelIdList() throws RemoteException {
            NavBasicDialog dialog;
            try{
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"getFavChannelIdList");
                dialog=  (NavBasicDialog) ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_CH_LIST);
                if (dialog == null){
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"getFavChannelIdList dialog is null");
                    if (Looper.myLooper() == null){
                        Looper.prepare();
                    }
                    dialog = new ChannelListDialog(HomeChannelService.this);
                    serviceDialog = true;
                }
                if(dialog instanceof ChannelListDialog){
                    List<TIFChannelInfo> channels= ((ChannelListDialog)dialog).getFavChannelListForHomeChannels();
                    if(channels != null){
                        long[] channelds=new long[channels.size()];
                        int i=0;
                        for (TIFChannelInfo tifChannelInfo : channels) {
                            channelds[i]=tifChannelInfo.mId;
                            i++;
                        }
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"getFavChannelIdList channelds "+channelds+" size:"+channelds.length);
                        deinitDialog(dialog);
                        return channelds;
                    }
                }
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"getFavChannelIdList channelds null");
                deinitDialog(dialog);
                return new long[]{};
            }catch (Throwable throwable){
                throwable.printStackTrace();
                return new long[]{};
            }
        }
        @Override
        public long[] getCrtTypeChannelIdList() throws RemoteException {
            NavBasicDialog dialog;
            try {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"getCrtTypeChannelIdList");
                dialog=  (NavBasicDialog) ComponentsManager.getInstance().getComponentById(NavBasic.NAV_COMP_ID_CH_LIST);
                if (dialog == null){
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"getCrtTypeChannelIdList dialog is null");
                    if (Looper.myLooper() == null){
                        Looper.prepare();
                    }
                    dialog = new ChannelListDialog(HomeChannelService.this);
                    serviceDialog = true;
                    //Looper.loop();
                }
                if(dialog instanceof ChannelListDialog){
                    List<TIFChannelInfo> channels= ((ChannelListDialog)dialog).getChannelListForHomeChannels();
                    if(channels != null){
                        long[] channelds=new long[channels.size()];
                        int i=0;
                        for (TIFChannelInfo tifChannelInfo : channels) {
                            channelds[i]=tifChannelInfo.mId;
                            i++;
                        }
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"getCrtTypeChannelIdList channelds "+channelds+" size:"+channelds.length);
                        deinitDialog(dialog);
                        return channelds;
                    }
                }
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"getCrtTypeChannelIdList channelds null");
                deinitDialog(dialog);
                return new long[]{};
            }catch (Throwable throwable){
                throwable.printStackTrace();
                return new long[]{};
            }
        }

        private void deinitDialog(NavBasicDialog dialog){
            if (serviceDialog){
                dialog.deinitView();
            }
            dialog = null;
            serviceDialog = false;
        }
    };
}
