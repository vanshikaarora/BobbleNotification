package vanshika.example.com.notofication;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

/**
 * @author FrankkieNL
 */
public class MyAccessibilityService extends AccessibilityService {

    @Override
    protected void onServiceConnected() {
        //api 16+
        //getServiceInfo().eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
        Log.v("NotificationTTS", "Connected");
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;
        info.notificationTimeout = 0;
        info.flags = AccessibilityServiceInfo.DEFAULT;
        setServiceInfo(info);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent arg0) {
        if (arg0 == null) {
            return;
        }
        if (arg0.getEventType() != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            return;
        }
        //Process Notification

        String name = Util.processNotification(this, arg0);
        if (name == null) {
            return;
        }
        if (!name.isEmpty()) {
            Intent intent = new Intent(this, Confirmation.class);
            startActivity(intent);

            /*Intent intent=new Intent(this,Confirmation.class);
            intent.putExtra("name",name);
            startActivity(intent);*/


            /*Handler h = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    new AlertDialog.Builder(getApplicationContext()) // it wont let you add           dialog bbox                  with context of application. an aactiviiiiiiiity is rrequuuuuuuuuired...so do u mean custom dialog?, i gues yesshalhow u some code of it..that also might not worksl i
                            .setTitle("Your Alert")
                            .setMessage("Your Message")
                            .setCancelable(false)
                            .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
//                            getPermission();
                                    sendMsg(getContact(name));
                                }
                            }).setNegativeButton("no", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    })
                            .show();
                }
            };
h.sendEmptyMessage(0);*/

        }





        /*String phone_no=getPhone_no(name);
        Log.v("line39",phone_no);
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Title")
                .setMessage("Are you sure?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .create();



        alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alertDialog.show();*/
    }

    @Override
    public void onInterrupt() {
        //do nothin'
    }

    @Override
    public void onDestroy() {
        //super.onDestroy(); //To change body of generated methods, choose Tools | Templates.
        if (Util.textToSpeech != null) {
            //Kill
            Util.textToSpeech.shutdown();
            Util.textToSpeech = null;
        }
    }

}