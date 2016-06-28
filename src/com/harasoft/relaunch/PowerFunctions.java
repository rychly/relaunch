package com.harasoft.relaunch;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.SystemClock;
import android.widget.Toast;

public class PowerFunctions {

	public static boolean actionLock(Activity act, boolean isRoot) {
        if (isRoot) {
            Process p;
            // Preform su to get root privledges
            try {
                p = Runtime.getRuntime().exec("su");
                // Attempt to write a file to a root-only
                DataOutputStream os = new DataOutputStream(p.getOutputStream());
                SystemClock.sleep(100);
                os.writeChars("sendevent /dev/input/event1 1 116 1\n");
                SystemClock.sleep(100);
                os.writeChars("sendevent /dev/input/event1 0 0 0\n");
                SystemClock.sleep(100);
                os.writeChars("sendevent /dev/input/event1 1 116 0\n");
                SystemClock.sleep(100);
                os.writeChars("sendevent /dev/input/event1 0 0 0\n");
                SystemClock.sleep(100);
                os.flush();
                os.close();
                p.destroy();
                return true;
            } catch (IOException e) {
                getToast(act);
                return false;
            }
        }else{
            getToast(act);
            return false;
        }
	}

	public static void actionReboot(Activity act, boolean isRoot) {
        if (isRoot) {
            final Process p;
            // Preform su to get root privledges
            try {
                p = Runtime.getRuntime().exec("su");
                AlertDialog.Builder builder = new AlertDialog.Builder(act);
                // "Reboot confirmation"
                builder.setTitle(act.getResources().getString(R.string.jv_advanced_reboot_confirm_title));
                // "Are you sure to reboot your device ? "
                builder.setMessage(act.getResources().getString(R.string.jv_advanced_reboot_confirm_text));
                // "YES"
                final Activity fact = act;
                builder.setPositiveButton(act.getResources().getString(R.string.app_yes),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                fact.setContentView(R.layout.reboot);
                                Timer timer = new Timer();
                                timer.schedule(new TimerTask() {
                                    public void run() {
                                        // Attempt to write a file to a root-only
                                        DataOutputStream os = new DataOutputStream(p.getOutputStream());
                                        SystemClock.sleep(100);
                                        try {
                                            os.writeChars("reboot\n");
                                        } catch (IOException e) {
                                            //e.printStackTrace();
                                        }
                                        p.destroy();
                                    }
                                }, 500);
                                dialog.dismiss();
                            }
                        });
                // "NO"
                builder.setNegativeButton(
                        act.getResources().getString(R.string.app_no),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                            }
                        });

                builder.show();
            } catch (IOException e) {
                getToast(act);
            }
        }else{
            getToast(act);
        }

	}

	public static void actionPowerOff(Activity act, boolean isRoot) {
        if (isRoot) {
            final Process p;
            // Preform su to get root privledges
            try {
                p = Runtime.getRuntime().exec("su");
                AlertDialog.Builder builder = new AlertDialog.Builder(act);
                // "Reboot confirmation"
                builder.setTitle(act.getResources().getString(R.string.jv_advanced_poweroff_confirm_title));
                // "Are you sure to reboot your device ? "
                builder.setMessage(act.getResources().getString(R.string.jv_advanced_poweroff_confirm_text));
                // "YES"
                final Activity fact = act;
                builder.setPositiveButton(act.getResources().getString(R.string.app_yes),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                fact.setContentView(R.layout.poweroff);
                                Timer timer = new Timer();
                                timer.schedule(new TimerTask() {
                                    public void run() {
                                        // Attempt to write a file to a root-only
                                        DataOutputStream os = new DataOutputStream(p.getOutputStream());
                                        SystemClock.sleep(100);
                                        try {
                                            os.writeChars("reboot -p\n");
                                        } catch (IOException e) {
                                            //e.printStackTrace();
                                        }
                                        p.destroy();

                                    }
                                }, 500);
                                dialog.dismiss();
                            }
                        });
                // "NO"
                builder.setNegativeButton(
                        act.getResources().getString(R.string.app_no),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                            }
                        });

                builder.show();
            } catch (IOException e) {
                getToast(act);
            }
        } else {
            getToast(act);
        }
    }

    private static void getToast(Activity act){
        Toast.makeText(
                act,
                act.getResources()
                        .getString(R.string.jv_advanced_root_only),
                Toast.LENGTH_LONG).show();
    }
}
