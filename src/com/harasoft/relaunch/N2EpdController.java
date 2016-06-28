package com.harasoft.relaunch;

/**
 * originate from CoolReader
 * http://http://sourceforge.net/projects/crengine/
 */

import android.app.Activity;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
/**
 * Nook Touch EPD controller interface wrapper. This class is created by
 * DairyKnight for Nook Touch screen support in FBReaderJ.
 * 
 * @author DairyKnight <dairyknight@gmail.com>
 *         http://forum.xda-developers.com/showthread.php?t=1183173
 */

@SuppressWarnings("rawtypes")
public class N2EpdController {
	public static final int REGION_APP_3 = 2;

	public static final int WAVE_GC = 0;
	public static final int WAVE_GU = 1;
	public static final int WAVE_GL16 = 4;
	public static final int MODE_ACTIVE_ALL = 4;
	public static final int MODE_ONESHOT_ALL = 5;

	private static Method mtSetRegion = null;
	private static Constructor RegionParamsConstructor = null;
	private static Constructor EpdControllerConstructors[] = null;
	public static Activity n2MainActivity =  null;
	private static Object mEpdController = null;

	private static Object[] enumsWave = null;
	private static Object[] enumsRegion = null;
	private static Object[] enumsMode = null;

	static {
		if (N2DeviceInfo.EINK_NOOK) {
			try {
				Class clEpdController = Class.forName("android.hardware.EpdController");
				Class clEpdControllerWave;
                if (N2DeviceInfo.EINK_NOOK_120)
                   clEpdControllerWave = Class.forName("android.hardware.EpdRegionParams$Wave");
                else
                   clEpdControllerWave = Class.forName("android.hardware.EpdController$Wave");
				Class clEpdControllerMode = Class.forName("android.hardware.EpdController$Mode");
				Class clEpdControllerRegion = Class.forName("android.hardware.EpdController$Region");

				Class clEpdControllerRegionParams;
                if (N2DeviceInfo.EINK_NOOK_120)
	                   clEpdControllerRegionParams = Class.forName("android.hardware.EpdRegionParams");
                else
	                   clEpdControllerRegionParams = Class.forName("android.hardware.EpdController$RegionParams");

				enumsWave = clEpdControllerWave.getEnumConstants();

				enumsMode = clEpdControllerMode.getEnumConstants();

				enumsRegion = clEpdControllerRegion.getEnumConstants();

				RegionParamsConstructor = clEpdControllerRegionParams
						.getConstructor(new Class[] { Integer.TYPE,
								Integer.TYPE, Integer.TYPE, Integer.TYPE,
								clEpdControllerWave });
				mtSetRegion = clEpdController.getMethod("setRegion",
						String.class, clEpdControllerRegion,
						clEpdControllerRegionParams, clEpdControllerMode);
	            if (N2DeviceInfo.EINK_NOOK_120)
	                   EpdControllerConstructors = clEpdController.getConstructors();
			} catch (Exception e) {
                //
			}
		}
	}

	public static void setMode(int region, int wave, int mode) {
		if (mtSetRegion != null) {
			try {
	            if (N2DeviceInfo.EINK_NOOK_120 && mEpdController == null)
	                   mEpdController = EpdControllerConstructors[0].newInstance(new Object[] { n2MainActivity });
				Object regionParams = RegionParamsConstructor
						.newInstance(new Object[] { 0, 0, 600, 800,
								enumsWave[wave] });
				mtSetRegion.invoke(mEpdController, "ReLaunch", enumsRegion[region], regionParams, enumsMode[mode]);
			} catch (Exception e) {
				//
			}
		}
	}
}
