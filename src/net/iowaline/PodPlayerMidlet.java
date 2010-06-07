/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.iowaline;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import javax.microedition.media.MediaException;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.media.Manager;
import javax.microedition.media.Player;
import javax.microedition.media.control.VolumeControl;
import org.netbeans.microedition.lcdui.pda.FileBrowser;

/**
 * @author aaron
 */
public class PodPlayerMidlet extends MIDlet implements CommandListener, ItemStateListener {

    private boolean midletPaused = false;

    //<editor-fold defaultstate="collapsed" desc=" Generated Fields ">//GEN-BEGIN:|fields|0|
    private FileBrowser screenFileBrowser;
    private Form screenPlayer;
    private Gauge volumeGauge;
    private Gauge timeGauge;
    private Command cmdBack15Sec;
    private Command cmdQuit1;
    private Command cmdPlayerQuit;
    private Command cmdPlayerPause;
    private Ticker ticker;
    //</editor-fold>//GEN-END:|fields|0|

    private Player mp3Player;
    private VolumeControl volumeControl;
    private Timer timeDisplayTimer;
    private UpdateDisplayTask timeTask;

    /**
     * 
     * @param item
     */
    public void itemStateChanged(Item item) {
        if ( item == timeGauge ){
            adjustPlayerTimeByGauge( (Gauge) item );
        }
        if ( item == volumeGauge ){
            adjustPlayerVolumeByGauge( (Gauge) item );
        }
    }

    /**
     *
     * @param timeGauge
     */
    private void adjustPlayerTimeByGauge( Gauge timeGauge ){
        long seconds = timeGauge.getValue() * mp3Player.getDuration() / timeGauge.getMaxValue();
        try {
            mp3Player.setMediaTime(seconds);
        } catch (MediaException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Adjust the time of the Player by a number of seconds (positive or negative)
     * Also update the gauge and the display at the same time.
     * @param seconds
     */
    public void adjustPlayerTimeBySeconds( long seconds ){
        try {
            mp3Player.setMediaTime(mp3Player.getMediaTime() + seconds * 1000000);
        } catch (MediaException ex) {
            switchDisplayable(new Alert("adjustPlayerTimeBySeconds", ex.toString(), null, AlertType.ERROR), getDisplay().getCurrent());
        }
    }

    /**
     * Adjust the volume level
     *
     * @param level
     */
    public void adjustPlayerVolumeByGauge( Gauge volumeGauge ){
        volumeControl.setLevel( volumeGauge.getValue() * 100 / volumeGauge.getMaxValue());
    }

    public class UpdateDisplayTask extends TimerTask{

        private Screen playerScreen;
        private Player mp3Player;
        private Gauge timeGauge;
        private long time = 0;
        private long mediaDuration = 0;
        private int timeGaugeMaxValue;
        
        public UpdateDisplayTask( Player mp3Player, Gauge timeGauge, Screen playerScreen ){
            this.mp3Player = mp3Player;
            this.timeGauge = timeGauge;
            this.playerScreen = playerScreen;
            this.mediaDuration = mp3Player.getDuration();
            this.timeGaugeMaxValue = timeGauge.getMaxValue();
        }
        
        public void run() {
            if (mp3Player.getState() != Player.CLOSED){
                time = mp3Player.getMediaTime();
                timeGauge.setLabel(formatMicroseconds(time) + Integer.toString(mp3Player.getState()));
                timeGauge.setValue((int) ( time * timeGaugeMaxValue / mediaDuration) );
            }
        }
    }

    /**
     *
     * @param numMicroseconds
     * @return
     */
    public String formatMicroseconds( long numMicroseconds ){
        if ( numMicroseconds == 0 ){
            return "0:00:00.0";
        }

        StringBuffer timeStr = new StringBuffer(10);
        long deciSeconds = (numMicroseconds / 100000) % 10;
        long seconds = (numMicroseconds / 1000000 ) % 60;
        long minutes = (numMicroseconds / 1000000 / 60 ) % 60;
        long hours = (numMicroseconds / 1000000 / 60 / 60 );
        timeStr.append( hours );
        timeStr.append( ":" );
        if ( minutes < 10 ){
           timeStr.append( "0" ).append(minutes);
        } else {
            timeStr.append(minutes);
        }
        timeStr.append(":");
        if ( seconds < 10 ){
            timeStr.append( "0" ).append(seconds);
        } else {
            timeStr.append(seconds);
        }
        timeStr.append(".");
        timeStr.append(deciSeconds);
        return timeStr.toString();
    }

    /**
     * The PodPlayerMidlet constructor.
     */
    public PodPlayerMidlet() {
    }

    //<editor-fold defaultstate="collapsed" desc=" Generated Methods ">//GEN-BEGIN:|methods|0|
    //</editor-fold>//GEN-END:|methods|0|

    //<editor-fold defaultstate="collapsed" desc=" Generated Method: initialize ">//GEN-BEGIN:|0-initialize|0|0-preInitialize
    /**
     * Initilizes the application.
     * It is called only once when the MIDlet is started. The method is called before the <code>startMIDlet</code> method.
     */
    private void initialize() {//GEN-END:|0-initialize|0|0-preInitialize
        // write pre-initialize user code here
        timeDisplayTimer = new Timer();
        screenPlayer = new Form("Paused", new Item[] { getTimeGauge(), getVolumeGauge() });//GEN-BEGIN:|0-initialize|1|0-postInitialize
        screenPlayer.setTicker(getTicker());
        screenPlayer.addCommand(getCmdPlayerPause());
        screenPlayer.addCommand(getCmdPlayerQuit());
        screenPlayer.addCommand(getCmdBack15Sec());
        screenPlayer.setCommandListener(this);//GEN-END:|0-initialize|1|0-postInitialize
        // write post-initialize user code here
        screenPlayer.setItemStateListener(this);
    }//GEN-BEGIN:|0-initialize|2|
    //</editor-fold>//GEN-END:|0-initialize|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Method: startMIDlet ">//GEN-BEGIN:|3-startMIDlet|0|3-preAction
    /**
     * Performs an action assigned to the Mobile Device - MIDlet Started point.
     */
    public void startMIDlet() {//GEN-END:|3-startMIDlet|0|3-preAction
        // write pre-action user code here
        //System.out.println("Heyo");
        switchDisplayable(null, getScreenFileBrowser());//GEN-LINE:|3-startMIDlet|1|3-postAction
        // write post-action user code here
    }//GEN-BEGIN:|3-startMIDlet|2|
    //</editor-fold>//GEN-END:|3-startMIDlet|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Method: resumeMIDlet ">//GEN-BEGIN:|4-resumeMIDlet|0|4-preAction
    /**
     * Performs an action assigned to the Mobile Device - MIDlet Resumed point.
     */
    public void resumeMIDlet() {//GEN-END:|4-resumeMIDlet|0|4-preAction
        // write pre-action user code here
//GEN-LINE:|4-resumeMIDlet|1|4-postAction
        // write post-action user code here
    }//GEN-BEGIN:|4-resumeMIDlet|2|
    //</editor-fold>//GEN-END:|4-resumeMIDlet|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Method: switchDisplayable ">//GEN-BEGIN:|5-switchDisplayable|0|5-preSwitch
    /**
     * Switches a current displayable in a display. The <code>display</code> instance is taken from <code>getDisplay</code> method. This method is used by all actions in the design for switching displayable.
     * @param alert the Alert which is temporarily set to the display; if <code>null</code>, then <code>nextDisplayable</code> is set immediately
     * @param nextDisplayable the Displayable to be set
     */
    public void switchDisplayable(Alert alert, Displayable nextDisplayable) {//GEN-END:|5-switchDisplayable|0|5-preSwitch
        // write pre-switch user code here
        Display display = getDisplay();//GEN-BEGIN:|5-switchDisplayable|1|5-postSwitch
        if (alert == null) {
            display.setCurrent(nextDisplayable);
        } else {
            display.setCurrent(alert, nextDisplayable);
        }//GEN-END:|5-switchDisplayable|1|5-postSwitch
        // write post-switch user code here
    }//GEN-BEGIN:|5-switchDisplayable|2|
    //</editor-fold>//GEN-END:|5-switchDisplayable|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Method: commandAction for Displayables ">//GEN-BEGIN:|7-commandAction|0|7-preCommandAction
    /**
     * Called by a system to indicated that a command has been invoked on a particular displayable.
     * @param command the Command that was invoked
     * @param displayable the Displayable where the command was invoked
     */
    public void commandAction(Command command, Displayable displayable) {//GEN-END:|7-commandAction|0|7-preCommandAction
        // write pre-action user code here
        if (displayable == screenFileBrowser) {//GEN-BEGIN:|7-commandAction|1|16-preAction
            if (command == FileBrowser.SELECT_FILE_COMMAND) {//GEN-END:|7-commandAction|1|16-preAction

                String mp3FileUrl = screenFileBrowser.getSelectedFileURL();
                try {
                    mp3Player = Manager.createPlayer(mp3FileUrl);
                    mp3Player.realize();
                    mp3Player.prefetch();
                    volumeControl = (VolumeControl) mp3Player.getControl("VolumeControl");
                    timeTask = new UpdateDisplayTask(mp3Player, timeGauge, screenPlayer);
                    timeDisplayTimer.scheduleAtFixedRate(timeTask, 0, 500);
                } catch (IOException ex) {
                    ex.printStackTrace();
                } catch (MediaException ex) {
                    ex.printStackTrace();
                }

                ticker.setString(mp3FileUrl);

                switchDisplayable(null, screenPlayer);//GEN-LINE:|7-commandAction|2|16-postAction
                // write post-action user code here
            } else if (command == cmdQuit1) {//GEN-LINE:|7-commandAction|3|19-preAction
                // write pre-action user code here
                exitMIDlet();//GEN-LINE:|7-commandAction|4|19-postAction
                // write post-action user code here
            }//GEN-BEGIN:|7-commandAction|5|67-preAction
        } else if (displayable == screenPlayer) {
            if (command == cmdBack15Sec) {//GEN-END:|7-commandAction|5|67-preAction
                // write pre-action user code here
                adjustPlayerTimeBySeconds(-15);
//GEN-LINE:|7-commandAction|6|67-postAction
                // write post-action user code here
            } else if (command == cmdPlayerPause) {//GEN-LINE:|7-commandAction|7|62-preAction
                // write pre-action user code here
                try {
                    if ( mp3Player.getState() == Player.STARTED ){
                        mp3Player.stop();
                    } else {
                        mp3Player.start();
                    }
                } catch (MediaException ex){
                    ex.printStackTrace();
                }
//GEN-LINE:|7-commandAction|8|62-postAction
                // write post-action user code here
            } else if (command == cmdPlayerQuit) {//GEN-LINE:|7-commandAction|9|64-preAction
//                // write pre-action user code here
                exitMIDlet();//GEN-LINE:|7-commandAction|10|64-postAction
                // write post-action user code here
            }//GEN-BEGIN:|7-commandAction|11|7-postCommandAction
        }//GEN-END:|7-commandAction|11|7-postCommandAction
        // write post-action user code here
    }//GEN-BEGIN:|7-commandAction|12|
    //</editor-fold>//GEN-END:|7-commandAction|12|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: screenFileBrowser ">//GEN-BEGIN:|14-getter|0|14-preInit
    /**
     * Returns an initiliazed instance of screenFileBrowser component.
     * @return the initialized component instance
     */
    public FileBrowser getScreenFileBrowser() {
        if (screenFileBrowser == null) {//GEN-END:|14-getter|0|14-preInit
            // write pre-init user code here
            screenFileBrowser = new FileBrowser(getDisplay());//GEN-BEGIN:|14-getter|1|14-postInit
            screenFileBrowser.setTitle("fileBrowser");
            screenFileBrowser.setCommandListener(this);
            screenFileBrowser.setFilter("");
            screenFileBrowser.addCommand(FileBrowser.SELECT_FILE_COMMAND);
            screenFileBrowser.addCommand(getCmdQuit1());//GEN-END:|14-getter|1|14-postInit
            // write post-init user code here
        }//GEN-BEGIN:|14-getter|2|
        return screenFileBrowser;
    }
    //</editor-fold>//GEN-END:|14-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: cmdQuit1 ">//GEN-BEGIN:|18-getter|0|18-preInit
    /**
     * Returns an initiliazed instance of cmdQuit1 component.
     * @return the initialized component instance
     */
    public Command getCmdQuit1() {
        if (cmdQuit1 == null) {//GEN-END:|18-getter|0|18-preInit
            // write pre-init user code here
            cmdQuit1 = new Command("Exit", Command.EXIT, 0);//GEN-LINE:|18-getter|1|18-postInit
            // write post-init user code here
        }//GEN-BEGIN:|18-getter|2|
        return cmdQuit1;
    }
    //</editor-fold>//GEN-END:|18-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: ticker ">//GEN-BEGIN:|34-getter|0|34-preInit
    /**
     * Returns an initiliazed instance of ticker component.
     * @return the initialized component instance
     */
    public Ticker getTicker() {
        if (ticker == null) {//GEN-END:|34-getter|0|34-preInit
            // write pre-init user code here
            ticker = new Ticker("");//GEN-LINE:|34-getter|1|34-postInit
            // write post-init user code here
        }//GEN-BEGIN:|34-getter|2|
        return ticker;
    }
    //</editor-fold>//GEN-END:|34-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: timeGauge ">//GEN-BEGIN:|58-getter|0|58-preInit
    /**
     * Returns an initiliazed instance of timeGauge component.
     * @return the initialized component instance
     */
    public Gauge getTimeGauge() {
        if (timeGauge == null) {//GEN-END:|58-getter|0|58-preInit
            // write pre-init user code here
            timeGauge = new Gauge("0:00:00", true, 100, 0);//GEN-LINE:|58-getter|1|58-postInit
            // write post-init user code here
        }//GEN-BEGIN:|58-getter|2|
        return timeGauge;
    }
    //</editor-fold>//GEN-END:|58-getter|2|
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: volumeGauge ">//GEN-BEGIN:|59-getter|0|59-preInit
    /**
     * Returns an initiliazed instance of volumeGauge component.
     * @return the initialized component instance
     */
    public Gauge getVolumeGauge() {
        if (volumeGauge == null) {//GEN-END:|59-getter|0|59-preInit
            // write pre-init user code here
            volumeGauge = new Gauge("Volume", true, 25, 12);//GEN-LINE:|59-getter|1|59-postInit
            // write post-init user code here
        }//GEN-BEGIN:|59-getter|2|
        return volumeGauge;
    }
    //</editor-fold>//GEN-END:|59-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: cmdPlayerPause ">//GEN-BEGIN:|61-getter|0|61-preInit
    /**
     * Returns an initiliazed instance of cmdPlayerPause component.
     * @return the initialized component instance
     */
    public Command getCmdPlayerPause() {
        if (cmdPlayerPause == null) {//GEN-END:|61-getter|0|61-preInit
            // write pre-init user code here
            cmdPlayerPause = new Command("Play/Pause", Command.OK, 0);//GEN-LINE:|61-getter|1|61-postInit
            // write post-init user code here
        }//GEN-BEGIN:|61-getter|2|
        return cmdPlayerPause;
    }
    //</editor-fold>//GEN-END:|61-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: cmdPlayerQuit ">//GEN-BEGIN:|63-getter|0|63-preInit
    /**
     * Returns an initiliazed instance of cmdPlayerQuit component.
     * @return the initialized component instance
     */
    public Command getCmdPlayerQuit() {
        if (cmdPlayerQuit == null) {//GEN-END:|63-getter|0|63-preInit
            // write pre-init user code here
            cmdPlayerQuit = new Command("Quit", Command.EXIT, 0);//GEN-LINE:|63-getter|1|63-postInit
            // write post-init user code here
        }//GEN-BEGIN:|63-getter|2|
        return cmdPlayerQuit;
    }
    //</editor-fold>//GEN-END:|63-getter|2|
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: cmdBack15Sec ">//GEN-BEGIN:|66-getter|0|66-preInit
    /**
     * Returns an initiliazed instance of cmdBack15Sec component.
     * @return the initialized component instance
     */
    public Command getCmdBack15Sec() {
        if (cmdBack15Sec == null) {//GEN-END:|66-getter|0|66-preInit
            // write pre-init user code here
            cmdBack15Sec = new Command("15s", Command.ITEM, 0);//GEN-LINE:|66-getter|1|66-postInit
            // write post-init user code here
        }//GEN-BEGIN:|66-getter|2|
        return cmdBack15Sec;
    }
    //</editor-fold>//GEN-END:|66-getter|2|

    /**
     * Returns a display instance.
     * @return the display instance.
     */
    public Display getDisplay () {
        return Display.getDisplay(this);
    }

    /**
     * Exits MIDlet.
     */
    public void exitMIDlet() {
        switchDisplayable (null, null);
        destroyApp(true);
        notifyDestroyed();
    }

    /**
     * Called when MIDlet is started.
     * Checks whether the MIDlet have been already started and initialize/starts or resumes the MIDlet.
     */
    public void startApp() {
        if (midletPaused) {
            resumeMIDlet ();
        } else {
            initialize ();
            startMIDlet ();
        }
        midletPaused = false;
    }

    /**
     * Called when MIDlet is paused.
     */
    public void pauseApp() {
        midletPaused = true;
    }

    /**
     * Called to signal the MIDlet to terminate.
     * @param unconditional if true, then the MIDlet has to be unconditionally terminated and all resources has to be released.
     */
    public void destroyApp(boolean unconditional) {
    }
}
