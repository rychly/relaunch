package com.harasoft.relaunch;

/**
 * originate from CoolReader
 * http://http://sourceforge.net/projects/crengine/
 */

import android.content.SharedPreferences;
import android.view.View;

public class EinkScreen {

	// / variables
	public static int UpdateMode = -1;
	// 0 - CLEAR_ALL, set only for old_mode == 2
	// 1 - ONESHOT, always set in prepare
	// 2 - ACTIVE, set in prepare
	public static int UpdateModeInterval;
	public static int RefreshNumber = -1;
	public static boolean IsSleep = false;
	// constants
	public final static int cmodeClear = 0;
	public final static int cmodeOneshot = 1;
	public final static int cmodeActive = 2;

	public static void PrepareController(View view, boolean isPartially) {
		if (N2DeviceInfo.EINK_NOOK) {
			if (isPartially || IsSleep != isPartially) {
				SleepController(isPartially, view);
				return;
			}
			if (RefreshNumber == -1) {
				switch (UpdateMode) {
				case cmodeClear:
					SetMode(view, cmodeClear);
					break;
				case cmodeActive:
					if (UpdateModeInterval == 0) {
						SetMode(view, cmodeActive);
					}
					break;
				}
				RefreshNumber = 0;
				return;
			}
			if (UpdateMode == cmodeClear) {
				SetMode(view, cmodeClear);
				return;
			}
			if (UpdateMode > 0 && (UpdateModeInterval > 0 || UpdateMode == 1)) {
				if (RefreshNumber == 0 || (UpdateMode == cmodeOneshot && RefreshNumber < UpdateModeInterval)) {
					switch (UpdateMode) {
					case cmodeActive:
						SetMode(view, cmodeActive);
						break;
					case cmodeOneshot:
						SetMode(view, cmodeOneshot);
						break;
					}
				} else if (UpdateModeInterval <= RefreshNumber) {
					SetMode(view, cmodeClear);
					RefreshNumber = -1;
				}
				if (UpdateModeInterval > 0) {
					RefreshNumber++;
				}
			}
		}
 	}

	public static void ResetController(int mode, View view) {
		if (!N2DeviceInfo.EINK_NOOK) {
			return;
		}
		switch (mode) {
		case cmodeClear:
			if (UpdateMode == cmodeActive) {
				RefreshNumber = -1;
			} else {
				RefreshNumber = 0;
			}
			break;
		case cmodeOneshot:
			RefreshNumber = 0;
			break;
		default:
			RefreshNumber = -1;
		}

		UpdateMode = mode;
	}

	public static void SleepController(boolean toSleep, View view) {
		if (!N2DeviceInfo.EINK_NOOK || toSleep == IsSleep) {
			return;
		}
		IsSleep = toSleep;
		if (IsSleep) {
			switch (UpdateMode) {
			case cmodeClear:
				break;
			case cmodeOneshot:
				break;
			case cmodeActive:
				SetMode(view, cmodeClear);
				RefreshNumber = -1;
			}
		} else {
			ResetController(UpdateMode, view);
		}
	}

	private static void SetMode(View view, int mode) {
		switch (mode) {
		case cmodeClear:
			N2EpdController.setMode(N2EpdController.REGION_APP_3,
					N2EpdController.WAVE_GC, N2EpdController.MODE_ONESHOT_ALL);
			break;
		case cmodeOneshot:
			N2EpdController.setMode(N2EpdController.REGION_APP_3,
					N2EpdController.WAVE_GU, N2EpdController.MODE_ONESHOT_ALL);
			break;
		case cmodeActive:
			N2EpdController.setMode(N2EpdController.REGION_APP_3,
					N2EpdController.WAVE_GL16, N2EpdController.MODE_ACTIVE_ALL);
			break;
		}
	}

    public static void setEinkController(SharedPreferences prefs) {
        if (prefs != null) {
            Integer einkUpdateMode;
            try {
                einkUpdateMode = Integer.parseInt(prefs.getString("einkUpdateMode", "1"));
            } catch (Exception e) {
                einkUpdateMode = 1;
            }
            if (einkUpdateMode < -1 || einkUpdateMode > 2)
                einkUpdateMode = 1;
            if (einkUpdateMode >= 0) {
                EinkScreen.UpdateMode = einkUpdateMode;

                Integer einkUpdateInterval;
                try {
                    einkUpdateInterval = Integer.parseInt(prefs.getString("einkUpdateInterval", "10"));
                } catch (Exception e) {
                    einkUpdateInterval = 10;
                }
                if (einkUpdateInterval < 0 || einkUpdateInterval > 100)
                    einkUpdateInterval = 10;
                EinkScreen.UpdateModeInterval = einkUpdateInterval;

                PrepareController(null, false);
            }
        }
    }
}
