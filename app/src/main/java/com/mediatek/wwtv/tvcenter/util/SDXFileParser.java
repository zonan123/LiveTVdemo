package com.mediatek.wwtv.tvcenter.util;

import com.mediatek.twoworlds.tv.MtkTvChannelListBase;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.SystemClock;

import com.mediatek.twoworlds.tv.model.MtkTvDvbChannelInfo;
import com.mediatek.twoworlds.tv.model.MtkTvChannelInfoBase;
import com.mediatek.twoworlds.tv.common.MtkTvChCommonBase;
import com.mediatek.twoworlds.tv.common.MtkTvIntent;
import com.mediatek.wwtv.setting.scan.EditChannel;
import com.mediatek.wwtv.tvcenter.TvSingletons;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelManager;
import com.mediatek.wwtv.tvcenter.util.tif.TIFChannelManager.SdxChannelListener;

public class SDXFileParser{
    private static final String TAG = "SDXFileParser";
   
    private static SDXFileParser mParser;
    private File mSdxFile;
    private int[] mOrbits;
    private List<Integer> mTargetOrbits = new ArrayList<Integer>();
    private int mSvlId;
    private int[] mSatRecId;
    private Context mContext;
    private TIFChannelManager mChannelDataManager;
    
    private boolean isCanceled = false;
    private boolean isScanning = false;
    
    private MyAsyncTask mTask;
    
    public ResultCallback mCallback;
    
    public void setCallback(ResultCallback callback){
        mCallback = callback;
    }
    
    public interface ResultCallback{
        void onPreExcete();
        void onStartDoInBackground();
        void onCancelled();
        void onProgress(int progress, int channelNum, int scanningSatIndex);
        void onParseError();
        void onParseComplete(List<MtkTvChannelInfoBase> result);
        void onStartStore();
        void onChannelListLoadComplete();
    }
    
    private SDXFileParser(Context context){
        mContext = context;
        mChannelDataManager =
                TvSingletons.getSingletons().getChannelDataManager();
    }
    
    public synchronized static SDXFileParser getInstance(Context context) {
        if(mParser == null){
            mParser = new SDXFileParser(context);
        }
        return mParser;
    }
    
    public void setSDXFile(File file){
        mSdxFile = file;
    }
    
    public AsyncTask.Status getStatus(){
        return mTask.getStatus();
    }
    
    public void startParse(int[] orbits, int svlId, int[] satRecId) {
        if(orbits == null || orbits.length == 0 || satRecId == null || satRecId.length == 0){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "startParse no satellite to parse");
            mCallback.onParseError();
            return;
        }
        mTargetOrbits.clear();
        isCanceled = false;
        isScanning = true;
        mOrbits = orbits;
        mSvlId = svlId;
        mSatRecId = satRecId;
        mChannelDataManager.setSdxChannelListener(mChannelListListener);
        mTask = new MyAsyncTask();
        mTask.execute(mSdxFile);
    }
    
    public void setChannelListToDb(List<MtkTvChannelInfoBase> list) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setChannelListToDb list size="+list.size());
        long time = SystemClock.uptimeMillis();
        mContext.sendBroadcast(new Intent(MtkTvIntent.MTK_INTENT_EVENT_PRESET_CHANNEL_LOAD_START));
        new MtkTvChannelListBase().setChannelListWithoutNortify(MtkTvChannelListBase.CHLST_OPERATOR_ADD, list);
        mContext.sendBroadcast(new Intent(MtkTvIntent.MTK_INTENT_EVENT_PRESET_CHANNEL_LOAD_END));


        mContext.sendBroadcast(new Intent(MtkTvIntent.MTK_INTENT_EVENT_CHANNEL_BATCH_INSERT_START));
        mContext.sendBroadcast(new Intent(MtkTvIntent.MTK_INTENT_EVENT_CHANNEL_BATCH_INSERT_END));
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "setChannelListToDb cost time="+(SystemClock.uptimeMillis() - time));
    }
    
    public void cleanSdxChannelListener() {
        mChannelDataManager.setSdxChannelListener(null);
    }
    
    public void cancelTask() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "cancelTask");
        isCanceled = true;
        isScanning = false;
        mChannelDataManager.setSdxChannelListener(null);
        //mTask.cancel(true);
    }
    
    private final SdxChannelListener mChannelListListener = new TIFChannelManager.SdxChannelListener() {
        
        @Override
        public void onChannelLoadFinished(int num) {
            if(mCallback != null && mTask.channelNum <= num) {
                mCallback.onChannelListLoadComplete();
                isScanning = false;
            }
        }
    };
    
    
    class MyAsyncTask extends AsyncTask<File, Integer, List<MtkTvChannelInfoBase>>{
        
        boolean errorHappened = false;
        int channelNum = 0;
        int satelliteIndex = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            errorHappened = false;
            if(mCallback != null) {
                mCallback.onPreExcete();
            }
        }
        
        @Override
        protected List<MtkTvChannelInfoBase> doInBackground(File... arg0) {
            
            if(mTask != null) {
                mCallback.onStartDoInBackground();
            }
            EditChannel.getInstance(mContext).cleanChannelList();
            List<MtkTvChannelInfoBase> channelInfos = new ArrayList<MtkTvChannelInfoBase>();
            
            if(mSdxFile == null || mSdxFile.length() == 0){
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "startParse sdx file null");
                if(mCallback != null) {
                    mCallback.onProgress(100, 0, 0);
                }
                return channelInfos;
            }
            
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "doInBackground mSdxFile="+mSdxFile.getName());
            
            BufferedReader reader = null;
            try {
                long total = mSdxFile.length();
                if(total == 0){
                    return channelInfos;
                }
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "startParse total="+total);
                String charSet = getEncodeSet(mSdxFile);
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(mSdxFile),charSet));
                int parsedLinesBytes = 0;
                int totalSats = mOrbits.length;
                
                while (true) {
                    if(isCanceled){
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "parse task canceled channels size="+channelInfos.size());
                        mTask.cancel(true);
                        break;
                    }
                    String line = reader.readLine();
                    if(line == null || line.trim().length() == 0){
                        break;
                    }
                    parsedLinesBytes += line.getBytes().length;
                    //11~28 satellite name
                    String satName = line.substring(10, 28);

                    //52~55 Orbit value, sdx file orbit range: 0E~360E, satl range: -180W ~ 0E ~180E
                    int orbit = 0;
                    try {
                        orbit = Integer.parseInt(line.substring(51, 55));
                    } catch (Exception e) {
                        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Integer.parseInt error "+e.getMessage());
                        continue;
                    }
                    if(orbit > 1800){
                        orbit = orbit - 3600; //west
                    }
                    if(!mTargetOrbits.contains(orbit)){
                        mTargetOrbits.add(orbit);
                    }
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "target orbit="+orbit);
                    for(int satIdx = 0; satIdx < mOrbits.length; satIdx++){
                        if(orbit == mOrbits[satIdx]){
                            MtkTvDvbChannelInfo channel = new MtkTvDvbChannelInfo(mSvlId, 0/*svlRecId*/);
                            channel.setBrdcstMedium(MtkTvChCommonBase.BRDCST_MEDIUM_DIG_SATELLITE);
                            channel.setFrequency(Integer.parseInt(line.substring(33,39)));//34~42 40~42omited
                            int polar = 0;
                            try {
                                polar = Integer.parseInt(line.substring(42,43));//43 Polarization
                            } catch (Exception e) {
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Polarization Integer.parseInt error "+e.getMessage());
                                break;
                            }
                            if(polar == 0){//vertical
                                polar = 2;
                            }else if(polar == 1){//horizontal
                                polar = 1;
                            }else {
                                polar = polar + 1;//unknow left right
                            }
                            try {
                                channel.setTslNwMask(1 << 16);
                                channel.setPol(polar);
                                channel.setServiceName((line.substring(43, 51) + line.substring(115, 127)).trim()); //44~51 + 116~127
                                channel.setShortName(null);
                                channel.setNwId(Integer.parseInt(line.substring(92, 97)));//93~97
                                channel.setOnId(Integer.parseInt(line.substring(92, 97)));//same with nid
                                channel.setTsId(Integer.parseInt(line.substring(97, 102)));//98~102
                                channel.setProgId(Integer.parseInt(line.substring(87, 92))); //88~92
                                channel.setSymRate(Integer.parseInt(line.substring(69, 74)));//70~74
                                channel.setMod(0);
                                channel.setSatRecId(mSatRecId[satIdx]);
                                channel.setSdtServiceType(0);
                                channel.setHbbtvStatus(0);
                                channel.setSvcProName(null);
                                channel.setBrdcstType(MtkTvChCommonBase.BRDCST_TYPE_DVB);
                                channelNum++;
                                channel.setChannelId(((channelNum << 18) + 1) | 1<<7);
                                channelInfos.add(channel);
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "parsed channel = " + channel);
                            }catch (Exception e){
                                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "Exception "+e.getMessage());
                                break;
                            }
                            break;
                        }
                    }

                    int progress = (int) (parsedLinesBytes * 100 / total);
                    satelliteIndex = (progress - 1) * totalSats / 100;
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "doInBackground progress="+progress+", channels="+channelNum+",satName="+satName+",satelliteIndex="+satelliteIndex);
                    publishProgress(new Integer[]{Integer.valueOf(progress), Integer.valueOf(channelNum), Integer.valueOf(satelliteIndex)});
                }
                if(mCallback != null && channelNum > 0 && !isCanceled) {
                    mCallback.onStartStore();
                }
                if(!isCanceled) {
                    setChannelListToDb(channelInfos);
                }
            } catch (Exception e) {
                e.printStackTrace();
                errorHappened = true;
                if(mCallback != null) {
                    mCallback.onParseError();
                }
            }finally {
                if(reader != null){
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return channelInfos;          
        }
        
        @Override
        protected void onPostExecute(List<MtkTvChannelInfoBase> result) {
            super.onPostExecute(result);
            if(mCallback != null && !errorHappened && !isCanceled) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"onPostExecute result size="+result.size()+",channelNum="+channelNum+",satelliteIndex="+satelliteIndex);
                //publishProgress(new Integer[]{Integer.valueOf(100), Integer.valueOf(channelNum), Integer.valueOf(satelliteIndex)});
                mCallback.onParseComplete(result);
                if(result.isEmpty()){
                    isScanning = false;
                }
            }
        }
        
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            /*if(isCancelled()){
                return;
            }*/
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onProgressUpdate"+values[1]);
            if(mCallback != null) {
                mCallback.onProgress(values[0], values[1], values[2]);
            }
        }
        
        @Override
        protected void onCancelled() {
            super.onCancelled();
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onCancelled");
            if(mCallback != null) {
                mCallback.onCancelled();
            }
        }
        
        String getEncodeSet(File file){
            String charSet = "iso-8859-9";
            FileInputStream fis = null;
            try {
                byte[] head = new byte[3];
                fis = new FileInputStream(file);
                int n = fis.read(head);
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, head[0]+","+head[1]+","+head[2]+","+"size = "+n);
                if(head[0] == -17 && head[1] == -19 && head[2] == -65){
                    charSet = "utf-8";
                }
                fis.close();
            } catch (Exception e) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, e.getMessage());
            }finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return charSet;
        }
    }
    
    public List<Integer> getmTargetOrbits() {
        return mTargetOrbits;
    }

    public boolean isScanning(){
        return isScanning;
    }
    
}
