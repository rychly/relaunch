package com.harasoft.relaunch;

/**
 * originate from CoolReader
 * http://http://sourceforge.net/projects/crengine/
 */

import android.os.Build;

public class N2DeviceInfo {
    public final static String MANUFACTURER;
    public final static String MODEL;
    public final static String DEVICE;
    public final static String PRODUCT;
    public final static boolean EINK_SCREEN;
    public final static boolean EINK_SCREEN_UPDATE_MODES_SUPPORTED;
    public final static boolean NOOK_NAVIGATION_KEYS;
    public final static boolean EINK_NOOK;
    public final static boolean EINK_NOOK_120;
    public final static boolean EINK_ONYX;
    public final static boolean EINK_SONY;
    public final static boolean EINK_GMINI;
    public final static boolean EINK_BOEYE;
    public final static boolean SONY_NAVIGATION_KEYS;

    static {
        MANUFACTURER = getBuildField("MANUFACTURER");
        MODEL = getBuildField("MODEL");
        DEVICE = getBuildField("DEVICE");
        PRODUCT = getBuildField("PRODUCT");
        EINK_NOOK = MANUFACTURER.toLowerCase().contentEquals("barnesandnoble") &&
                (PRODUCT.contentEquals("NOOK") || MODEL.contentEquals("NOOK") || MODEL.contentEquals("BNRV300") || MODEL.contentEquals("BNRV350") || MODEL.contentEquals("BNRV500")) &&
                DEVICE.toLowerCase().contentEquals("zoom2");
        EINK_NOOK_120 = EINK_NOOK && (MODEL.contentEquals("BNRV300") || MODEL.contentEquals("BNRV350") || MODEL.contentEquals("BNRV500"));
        EINK_SONY = MANUFACTURER.toLowerCase().contentEquals("sony") && MODEL.startsWith("PRS-T");
        // ONYX BOOX i63ML Magellan
        //MANUFACTURER=Onyx, MODEL=C63ML, DEVICE=C63ML, PRODUCT=C63ML
        // ONYX BOOX i63ML Newton
        // MANUFACTURER=Onyx-Intl, MODEL=I63MLP_HD, DEVICE=I63MLP_HD, PRODUCT=I63MLP_HD
        //ONYX BOOX T76ML Cleopatra
        // MANUFACTURER=ONYX, MODEL=T76ML, DEVICE=T76ML, PRODUCT=T76ML
        EINK_ONYX = MANUFACTURER.toLowerCase().contains("onyx") && ("C63ML".equalsIgnoreCase(MODEL) || "I63MLP_HD".equalsIgnoreCase(MODEL) || "T76ML".equalsIgnoreCase(MODEL) || "MC_T76MLPRO".equalsIgnoreCase(MODEL));
        //ro.product.manufacturer=Magicbook
        //ro.product.model=rk30sdk
        //ro.product.brand=Magicbook
        //ro.product.device=T62D
        //ro.build.product=T62D
        //ro.product.name=T62D
        //ro.product.board=rk30sdk
        //ro.product.cpu.abi=armeabi-v7a
        //ro.product.cpu.abi2=armeabi
        EINK_GMINI = MANUFACTURER.toLowerCase().contains("magicbook") && MODEL.contentEquals("rk30sdk") && DEVICE.contentEquals("T62D");
        //ro.product.manufacturer=Boeye
        //ro.product.model=rk30sdk
        //ro.product.brand=Boeye
        //ro.product.device=T62D
        //ro.build.product=T62D
        //ro.product.name=T62D
        //ro.product.board=rk30sdk
        //ro.product.cpu.abi=armeabi-v7a
        //ro.product.cpu.abi2=armeabi
        EINK_BOEYE = MANUFACTURER.toLowerCase().contains("boeye") && MODEL.contentEquals("rk30sdk") && DEVICE.contentEquals("T62D");
        EINK_SCREEN = EINK_SONY || EINK_NOOK || EINK_ONYX || EINK_GMINI || EINK_BOEYE; // TODO: set to true for eink devices like Nook Touch
        NOOK_NAVIGATION_KEYS = EINK_NOOK; // TODO: add autodetect
        SONY_NAVIGATION_KEYS = EINK_SONY;
        EINK_SCREEN_UPDATE_MODES_SUPPORTED = EINK_SCREEN && EINK_NOOK; // TODO: add autodetect
    }
    private static String getBuildField(String fieldName) {
        try {
            return (String)Build.class.getField(fieldName).get(null);
        } catch (Exception e) {
            return "";
        }
    }
    public static String getName() {
        return Build.VERSION.RELEASE;
    }
    public static boolean getSupport() {
        return EINK_SCREEN;
    }

    /*  Код для определения реального разрешения экрана в точках. Ветвление необходимо для разных версий андроида.
        Для API старше 13 предыдущий код выдает размер отнимая занятое системными панелями.
        Необходимо использовать библиотеки поддержки. Надо разбираться как их подключить.

    public Point getScreenSize(Activity context) {
        Display display = context.getWindowManager().getDefaultDisplay();
        Point size = new Point();

        if (android.os.Build.VERSION.SDK_INT >= 13) {
            display.getSize(size);
        } else {
            //noinspection deprecation
            size.set(display.getWidth(), display.getHeight());
        }

        return size;
    }
    */
}