package com.harasoft.relaunch;

import com.stericson.RootShell.exceptions.RootDeniedException;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootTools.RootTools;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by anat on 08.02.15.
 */
public class SystemSettings {

    public void RunCommand(Command command, boolean useRoot){
        try {
            RootTools.getShell(useRoot).add(command);
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (TimeoutException e1) {
            e1.printStackTrace();
        } catch (RootDeniedException e1) {
            e1.printStackTrace();
        }
    }

    public void RunAppWidgetPickActivity(){
        Command command = new Command(0, "am start -n com.android.settings/.AppWidgetPickActivity\n");
        RunCommand(command, false);
    }
    public void RunBandMode(){
        Command command = new Command(0, "am start -n com.android.settings/.BandMode\n");
        RunCommand(command, false);
    }
    public void RunBatteryHistory(){
        Command command = new Command(0, "am start -n com.android.settings/.battery_history.BatteryHistory\n");
        RunCommand(command, false);
    }
    public void RunBatteryInfo(){
        Command command = new Command(0, "am start -n com.android.settings/.BatteryInfo\n");
        RunCommand(command, false);
    }
    public void RunChooseLockPin(){
        Command command = new Command(0, "am start -n com.android.settings/.ChooseLockPin\n");
        RunCommand(command, false);
    }
    public void RunChooseLockPinExample(){
        Command command = new Command(0, "am start -n com.android.settings/.ChooseLockPinExample\n");
        RunCommand(command, false);
    }
    public void RunChooseLockPinTutorial(){
        Command command = new Command(0, "am start -n com.android.settings/.ChooseLockPinTutorial\n");
        RunCommand(command, false);
    }
    public void RunConfirmLockPin(){
        Command command = new Command(0, "am start -n com.android.settings/.ConfirmLockPin\n");
        RunCommand(command, false);
    }
    public void RunDevelopmentSettings(){
        Command command = new Command(0, "am start -n com.android.settings/.DevelopmentSettings\n");
        RunCommand(command, false);
    }
    public void RunMemory(){
        Command command = new Command(0, "am start -n com.android.settings/.deviceinfo.Memory\n");
        RunCommand(command, false);
    }
    public void RunDisplay(){
        Command command = new Command(0, "am start -n com.android.settings/.Display\n");
        RunCommand(command, false);
    }
    public void RunPowerUsageDetail(){
        Command command = new Command(0, "am start -n com.android.settings/.fuelgauge.PowerUsageDetail\n");
        RunCommand(command, false);
    }
    public void RunPowerUsageSummary(){
        Command command = new Command(0, "am start -n com.android.settings/.fuelgauge.PowerUsageSummary\n");
        RunCommand(command, false);
    }
    public void RunInstalledAppDetails(){
        Command command = new Command(0, "am start -n com.android.settings/.InstalledAppDetails\n");
        RunCommand(command, false);
    }
    public void RunDebugIntentSender(){
        Command command = new Command(0, "am start -n com.android.settings/.DebugIntentSender\n");
        RunCommand(command, false);
    }
    public void RunLauncherAppWidgetBinder(){
        Command command = new Command(0, "am start -n com.android.settings/.LauncherAppWidgetBinder\n");
        RunCommand(command, false);
    }
    public void RunManageApplications(){
        Command command = new Command(0, "am start -n com.android.settings/.ManageApplications\n");
        RunCommand(command, false);
    }
    public void RunMasterClear(){
        Command command = new Command(0, "am start -n com.android.settings/.MasterClear\n");
        RunCommand(command, false);
    }
    public void RunMediaFormat(){
        Command command = new Command(0, "am start -n com.android.settings/.MediaFormat\n");
        RunCommand(command, false);
    }
    public void RunProxySelector(){
        Command command = new Command(0, "am start -n com.android.settings/.ProxySelector\n");
        RunCommand(command, false);
    }
    public void RunRadioInfo(){
        Command command = new Command(0, "am start -n com.android.settings/.RadioInfo\n");
        RunCommand(command, false);
    }
    public void RunRunningServices(){
        Command command = new Command(0, "am start -n com.android.settings/.RunningServices\n");
        RunCommand(command, false);
    }
    public void RunSdCardSettings(){
        Command command = new Command(0, "am start -n com.android.settings/.SdCardSettings\n");
        RunCommand(command, false);
    }
    public void RunSettings_About(){
        Command command = new Command(0, "am start -n com.android.settings/.Settings_About\n");
        RunCommand(command, false);
    }
    public void RunSettings_DateTime(){
        Command command = new Command(0, "am start -n com.android.settings/.Settings_DateTime\n");
        RunCommand(command, false);
    }
}
