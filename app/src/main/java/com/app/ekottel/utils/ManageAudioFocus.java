package com.app.ekottel.utils;

import android.content.Context;
import android.media.AudioManager;

import com.ca.wrapper.CSCall;

/**
 * This method handles the audio related callbacks.
 */
public class ManageAudioFocus {

    private AudioManager.OnAudioFocusChangeListener mAudioFocusChangeListener;
    private AudioManager mAudioManager = null;
    private String mCallID = "";
    private String mRemoteID = "";
    private CSCall CSCallObj = new CSCall();

    /**
     * This method request the audio focus
     *
     * @param callID   call id of call
     * @param remoteID remote id of call
     * @return boolean
     */
    public boolean requestAudioFocus(Context context,String callID, String remoteID,boolean isVideoCall) {
        try {


            if (GlobalVariables.incallcount <= 1) {
                mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

                mCallID = callID;
                mRemoteID = remoteID;


                mAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
                    @Override
                    public void onAudioFocusChange(int focusChange) {
                        if (!mCallID.equals("") && !mRemoteID.equals("")) {

                            switch (focusChange) {
                                case AudioManager.AUDIOFOCUS_GAIN:

                                    CSCallObj.holdAVCall(mRemoteID, mCallID, false);
                                    if(isVideoCall){
                                        AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
                                        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                                        audioManager.setSpeakerphoneOn(true);
                                    }

                                    break;
                                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:

                                    CSCallObj.holdAVCall(mRemoteID, mCallID, false);

                                    break;
                                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE:

                                    CSCallObj.holdAVCall(mRemoteID, mCallID, false);
                                    break;
                                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:

                                    break;
                                case AudioManager.AUDIOFOCUS_LOSS:

                                    //hold the call
                                    CSCallObj.holdAVCall(mRemoteID, mCallID, true);

                                    break;
                                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:

                                    //hold the call
                                    CSCallObj.holdAVCall(mRemoteID, mCallID, true);

                                    break;
                                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:

                                    //hold the call

                                    break;
                                default:

                                    break;
                            }
                        }
                    }
                };

                int result = mAudioManager.requestAudioFocus(mAudioFocusChangeListener, AudioManager.STREAM_VOICE_CALL, AudioManager.AUDIOFOCUS_GAIN);
                if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    return true;
                } else {
                    return false;
                }
            }
            return false;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * This method restore the audio focus
     */
    public void abandonAudioFocus() {
        try {
            if (mAudioManager != null && mAudioFocusChangeListener != null) {
                mAudioManager.abandonAudioFocus(mAudioFocusChangeListener);
                mAudioFocusChangeListener = null;
                mAudioManager = null;
                mCallID = "";
                mRemoteID = "";
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


}
