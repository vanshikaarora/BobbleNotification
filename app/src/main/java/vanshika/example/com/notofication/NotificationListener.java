package vanshika.example.com.notofication;

import android.content.Intent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

public class NotificationListener extends NotificationListenerService {

    @Override
    public void onNotificationPosted(StatusBarNotification arg0) {
        String name = Util.processNotification(this, arg0);
        Intent intent = new Intent(getApplicationContext(), Confirmation.class);
        if (name == null || name.equals("") || name.equals(" ")) {
            return;
        }
        intent.putExtra("Name", name);
        startActivity(intent);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification arg0) {
        //do nothing.
    }

    @Override
    public void onDestroy() {
        try {
            if (Util.textToSpeech != null) {
                //Kill
                Util.textToSpeech.shutdown();
                Util.textToSpeech = null;
            }
        } catch (Exception e) {
        }
    }

}