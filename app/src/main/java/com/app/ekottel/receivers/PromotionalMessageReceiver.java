package com.app.ekottel.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import static com.app.ekottel.utils.GlobalVariables.LOG;

import com.app.ekottel.R;
import com.app.ekottel.utils.NotificationMethodHelper;

/**
 * This activity is used to receive promotional messages.
 *
 * @author Ramesh U
 * @version 2017
 */
public class PromotionalMessageReceiver extends BroadcastReceiver {

    private String TAG1;

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            TAG1 = context.getString(R.string.promotional_message_receiver_tag);
            LOG.info(TAG1, "PromotionalMessage Receieved");

            String promotionalmessageid = intent.getStringExtra(context.getString(R.string.promotional_message_receiver_intent_message_id));
            String promotionalmessageapi = intent.getStringExtra(context.getString(R.string.promotional_message_receiver_intent_message_api));
            String promotionalmessagetype = intent.getStringExtra(context.getString(R.string.promotional_message_receiver_intent_message_type));
            String promotionalmessagemessage = intent.getStringExtra(context.getString(R.string.promotional_message_receiver_intent_message_message));
            String promotionalmessageusernotified = intent.getStringExtra(context.getString(R.string.promotional_message_receiver_intent_user_notified));

            if (promotionalmessageapi != null) {
                if (promotionalmessageapi.equals("253") && promotionalmessagetype.equals("1")) {

                    if (promotionalmessageid == null) {
                        promotionalmessageid = "";
                    }
                    if (promotionalmessageapi == null) {
                        promotionalmessageapi = "";
                    }
                    if (promotionalmessagetype == null) {
                        promotionalmessagetype = "";
                    }
                    if (promotionalmessagemessage == null) {
                        promotionalmessagemessage = "";
                    }

                    LOG.info(TAG1, "promotionalmessageid" + promotionalmessageid);
                    LOG.info(TAG1, "promotionalmessageapi" + promotionalmessageapi);
                    LOG.info(TAG1, "promotionalmessagetype" + promotionalmessagetype);
                    LOG.info(TAG1, "promotionalmessagemessage" + promotionalmessagemessage);
                    LOG.info(TAG1, "promotionalmessageusernotified" + promotionalmessageusernotified);
                    context.sendBroadcast(new Intent(context.getString(R.string.promotional_intent_message_received)));
                    NotificationMethodHelper.NotifyUserPromotional(context, context.getString(R.string.notification_title), promotionalmessagemessage, promotionalmessageid, 0);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}