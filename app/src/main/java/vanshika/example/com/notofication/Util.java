package vanshika.example.com.notofication;

import android.app.ActivityManager;
import android.app.Notification;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.service.notification.StatusBarNotification;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.RemoteViews;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * @author FrankkieNL
 */
public class Util {

    public static TextToSpeech textToSpeech = null;
    public static ArrayList<UserRule> rules = new ArrayList<UserRule>();

    /**
     * Filtering a stuff happens here !!
     *
     * @param c
     * @param notification
     * @param packagename
     */
    public static String process(Context c, Notification notification, String packagename) {
        String appName = getAppNameByPackagename(c, packagename);
        String who = "";
        String message = "";
        if (packagename.equals("com.whatsapp")) {
            Bundle extras = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                extras = notification.extras;
            }

            who = extras.getString(Notification.EXTRA_TITLE);
            message = extras.getString(Notification.EXTRA_TEXT);
            if (message == null) {
                return null;

            }
            if (message.equals("Checking for new messages")) {
                return null;
            }
            Log.v("line70", who);
            String tickerText = extras.getString(Notification.EXTRA_SUB_TEXT);
            String yet = extras.getString(Notification.EXTRA_INFO_TEXT);
            if (!message.isEmpty() && message.endsWith("new messages\n")) {
                //multiple messages from 1 person
                tts(c, who + " " + "Sent you " + message);
            } else if (who.endsWith("messages):\n")) {
                who = who.substring(0, who.indexOf("("));
            } else if (!who.isEmpty()) {// && message.endsWith("conversations.\n")) {
                //multiple messages from multiple persons
                boolean readNumbers = false;
                if (readNumbers) {
                    tts(c, who + " Sent you " + message);
                } else {
                    tts(c, who + " Sent you " + message);
                }
            } else {
                //1 message from 1 person
                tts(c, "WhatsApp " + who);
            }
/*
            try {
                Util.CallActivity(c,who);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                }
*/
            if (!who.equals("WhatsApp"))
                return who;
        }
        return " ";
    }

    /**
     * Speak some text
     *
     * @param c   the Context (does not have to be an Activity)
     * @param txt the Text to speak
     */
    public static void tts(final Context c, final String txt) {

        if (textToSpeech == null) {
            //Make TTS
            textToSpeech = new TextToSpeech(c, new TextToSpeech.OnInitListener() {

                public void onInit(int arg0) {
                    if (arg0 == TextToSpeech.SUCCESS) {
                        //When init is done, proceed to talk.
                        ttsOnInit(c, txt);
                    }
                }
            });
            //http://developer.android.com/reference/android/speech/tts/TextToSpeech.html
            //The TTS system has changed since Android 4.0.3 (15)
            if (android.os.Build.VERSION.SDK_INT > 15) {
                textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {

                    @Override
                    public void onStart(String arg0) {
                        //do nothing
                    }

                    @Override
                    public void onDone(String arg0) {
                        try {
                            //Stop TTS engine when speaking is over.
                            textToSpeech.shutdown();
                            textToSpeech = null;
                        } catch (Exception e) {
                        }
                    }

                    @Override
                    public void onError(String arg0) {
                        try {
                            //Stop TTS engine on error
                            textToSpeech.shutdown();
                            textToSpeech = null;
                        } catch (Exception e) {
                        }
                    }
                });
            } else {
                //Older Android version than 4.0.3 (but still Android 1.6+)
                textToSpeech.setOnUtteranceCompletedListener(new TextToSpeech.OnUtteranceCompletedListener() {

                    public void onUtteranceCompleted(String utteranceId) {
                        try {
                            textToSpeech.shutdown();
                            textToSpeech = null;
                        } catch (Exception e) {
                        }
                    }
                });
            }
        } else {
            //Not null, proceed to speaking.
            ttsOnInit(c, txt);
        }
    }

    /**
     * This gets called when the TTS-engine is ready to use. Dont call this
     * directly, use tts(Context, String) instead.
     *
     * @param c   the Context
     * @param txt the Text to speak
     */
    private static void ttsOnInit(Context c, String txt) {
        if (textToSpeech != null) {
            //If TTS engine is ready, speak
            textToSpeech.speak(txt, TextToSpeech.QUEUE_ADD, null);
        } else {
            //Not ready to speak, init it.
            tts(c, txt); //I hope this wont StackOverFlow!!
        }
    }

    public static String processNotification(Context c, StatusBarNotification notification) {
        Notification notification1 = notification.getNotification(); //<-- !
        String packagename = notification.getPackageName().toString();
        return process(c, notification1, packagename);
    }

    public static String processNotification(Context c, AccessibilityEvent notification) {
        Notification notification1 = (Notification) notification.getParcelableData();  //<-- !
        String packagename = notification.getPackageName().toString();
        return process(c, notification1, packagename);
    }

    /**
     * Check if a certain string has been said in the last 15 seconds.
     *
     * @param c Context (does not have to be an Activity, used for
     *          SharedPreferences)
     * @param s String to check
     * @return True when cooldown is over, false otherwise (false=stfu)
     */
    public static boolean checkCooldown(Context c, String s) {
        long cooldown = 1000 * 15; //10 sec
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);

        boolean ans = false;
        long lastTime = prefs.getLong("cooldown_" + s, 0);
        long currentTime = System.currentTimeMillis();
        if (lastTime + cooldown < currentTime) {
            ans = true; //cooldown over
        }

        //set current time
        prefs.edit().putLong("cooldown_" + s, currentTime).commit();

        return ans;
    }

    public static String getNotificationText(Notification notification) {
        //http://stackoverflow.com/questions/9292032/extract-notification-text-from-parcelable-contentview-or-contentintent
        String answer = "";
        try {
            //Notification notification = (Notification) event.getParcelableData();
            RemoteViews views = notification.contentView;
            Class secretClass = views.getClass();

            Field outerFields[] = secretClass.getDeclaredFields();
            for (int i = 0; i < outerFields.length; i++) {
                if (!outerFields[i].getName().equals("mActions")) {
                    continue;
                }

                outerFields[i].setAccessible(true);

                ArrayList<Object> actions
                        = (ArrayList<Object>) outerFields[i].get(views);
                for (Object action : actions) {
                    Field innerFields[] = action.getClass().getDeclaredFields();

                    Object value = null;
                    String methodName = null;
                    for (Field field : innerFields) {
                        field.setAccessible(true);
                        if (field.getName().equals("value")) {
                            value = field.get(action);
                        } else if (field.getName().equals("methodName")) {
                            methodName = field.get(action).toString();
                        }
                    }
                    if (methodName.equals("setText")) {
                        if (!value.toString().equals("")) {
                            answer += value.toString() + "\n";
                        }
                    }
                }
                return answer;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getForegroundAppPackagename(Context context) {
        return ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getRunningTasks(1).get(0).topActivity.getPackageName();
    }

    public static String getAppNameByPackagename(Context c, String packagename) {
        final PackageManager pm = c.getPackageManager();
        ApplicationInfo ai;
        try {
            ai = pm.getApplicationInfo(packagename, 0);
        } catch (final PackageManager.NameNotFoundException e) {
            ai = null;
        }
        String applicationName = (String) (ai != null ? pm.getApplicationLabel(ai) : packagename);
        return applicationName;
    }

    public static void kill(Context context, String packagename) {
        try {
            ActivityManager am = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
            am.killBackgroundProcesses(packagename);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //////
    public static void killThisApp(Context context, String packagename) {
        ActivityManager am = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        am.restartPackage(packagename); //KILL
        try {
            killMethod = ActivityManager.class.getMethod("killBackgroundProcesses", String.class);
            killMethod.invoke(am, packagename);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static Method killMethod = null;
    //////
}