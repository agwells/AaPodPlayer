package net.iowaline;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import javax.microedition.media.MediaException;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.media.Manager;
import javax.microedition.media.Player;
import javax.microedition.media.control.VolumeControl;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotOpenException;
import org.netbeans.microedition.lcdui.pda.FileBrowser;

/**
 * @author aaron
 */
public class PodPlayerMidlet extends MIDlet implements CommandListener, ItemStateListener {
    private static final String LAST_FILE_RECORDSTORE = "lastFilePlaying";

    private boolean midletPaused = false;

    private FileBrowser screenFileBrowser;
    private Form screenPlayer;
    private Gauge volumeGauge;
    private Gauge timeGauge;
    private Command cmdChangeTrack;
    private Command cmdBack15Sec;
    private Command cmdFileBrowserQuit;
    private Command cmdPlayerQuit;
    private Command cmdPlayerPause;
    private Ticker ticker;

    private Player mp3Player;
    private VolumeControl volumeControl;
    private Timer timeDisplayTimer;
    private UpdateDisplayTask timeTask;
    private String currentFileUrl;
    private long startTime;

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

        private Player mp3Player;
        private Gauge timeGauge;
        private long time = 0;
        private long mediaDuration = 0;
        private int timeGaugeMaxValue;
        
        public UpdateDisplayTask( Player mp3Player, Gauge timeGauge ){
            this.mp3Player = mp3Player;
            this.timeGauge = timeGauge;
            this.mediaDuration = mp3Player.getDuration();
            this.timeGaugeMaxValue = timeGauge.getMaxValue();
        }
        
        public void run() {
            if (mp3Player.getState() != Player.CLOSED){
                time = mp3Player.getMediaTime();
                timeGauge.setLabel(formatMicroseconds(time));
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
            return "0:00:00";
        }

        StringBuffer timeStr = new StringBuffer(10);
//        long deciSeconds = (numMicroseconds / 100000) % 10;
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
//        timeStr.append(".");
//        timeStr.append(deciSeconds);
        return timeStr.toString();
    }

    /**
     *
     * @param recordStoreName
     * @param fileUrl
     * @param playTime
     */
    private void savePreferences(String recordStoreName, String fileUrl, long playTime ){
        // Delete the saved preferences first, to avoid corruption issues from re-writing
        clearSavedPreferences(recordStoreName);
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream dataStream = new DataOutputStream(byteStream);
        RecordStore store = null;
        try {
            dataStream.writeUTF(fileUrl);
            dataStream.writeLong(playTime);
            dataStream.flush();
            byte[] byteArray = byteStream.toByteArray();
            store = RecordStore.openRecordStore(recordStoreName, true);
            store.addRecord(byteArray, 0, byteArray.length);
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if ( store != null ){
                try {
                    store.closeRecordStore();
                } catch (RecordStoreNotOpenException ex) {
                    ex.printStackTrace();
                } catch (RecordStoreException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     *
     * @param recordStoreName
     */
    private void readPreferences(String recordStoreName){
        this.currentFileUrl = null;
        this.startTime = 0L;
        RecordStore store = null;
        try {
            store = RecordStore.openRecordStore(recordStoreName, true);
            int numRecords = store.getNumRecords();
            System.out.println("numRecords: " + Integer.toString(numRecords));
            if ( store.getNumRecords() > 0 ){
                ByteArrayInputStream byteStream = new ByteArrayInputStream(store.getRecord(1));
                DataInputStream dataStream = new DataInputStream(byteStream);
                currentFileUrl = dataStream.readUTF();
                startTime = dataStream.readLong();
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if ( store != null ){
                try {
                    store.closeRecordStore();
                } catch (RecordStoreNotOpenException ex) {
                    ex.printStackTrace();
                } catch (RecordStoreException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * 
     * @param recordStoreName
     */
    private void clearSavedPreferences( String recordStoreName ){
        try {
            // Clear the RecordStore if it exists, before saving this one
            RecordStore.deleteRecordStore(recordStoreName);
        } catch (RecordStoreException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * The PodPlayerMidlet constructor.
     */
    public PodPlayerMidlet() {
    }

    /**
     * Initilizes the application.
     * It is called only once when the MIDlet is started. The method is called before the <code>startMIDlet</code> method.
     */
    private void initialize() {
        timeDisplayTimer = new Timer();
        screenPlayer = new Form("Paused", new Item[] { getTimeGauge(), getVolumeGauge() });
        screenPlayer.setTicker(getTicker());
        screenPlayer.addCommand(getCmdPlayerPause());
        screenPlayer.addCommand(getCmdPlayerQuit());
        screenPlayer.addCommand(getCmdBack15Sec());
        screenPlayer.addCommand(getCmdChangeTrack());
        screenPlayer.setCommandListener(this);

        screenPlayer.setItemStateListener(this);
        readPreferences(LAST_FILE_RECORDSTORE);
    }

    /**
     * Performs an action assigned to the Mobile Device - MIDlet Started point.
     */
    public void startMIDlet() {
        //System.out.println("Heyo");
        isLastFileNameSaved();
    }

    /**
     * Performs an action assigned to the Mobile Device - MIDlet Resumed point.
     */
    public void resumeMIDlet() {
    }

    /**
     * Switches a current displayable in a display. The <code>display</code> instance is taken from <code>getDisplay</code> method. This method is used by all actions in the design for switching displayable.
     * @param alert the Alert which is temporarily set to the display; if <code>null</code>, then <code>nextDisplayable</code> is set immediately
     * @param nextDisplayable the Displayable to be set
     */
    public void switchDisplayable(Alert alert, Displayable nextDisplayable) {
        Display display = getDisplay();
        if (alert == null) {
            display.setCurrent(nextDisplayable);
        } else {
            display.setCurrent(alert, nextDisplayable);
        }
    }

    /**
     * Called by a system to indicated that a command has been invoked on a particular displayable.
     * @param command the Command that was invoked
     * @param displayable the Displayable where the command was invoked
     */
    public void commandAction(Command command, Displayable displayable) {
        if (displayable == screenFileBrowser) {
            if (command == FileBrowser.SELECT_FILE_COMMAND) {

                currentFileUrl = screenFileBrowser.getSelectedFileURL();
                try {
                    mp3Player = Manager.createPlayer(currentFileUrl);
                    mp3Player.realize();
                    mp3Player.prefetch();
                    volumeControl = (VolumeControl) mp3Player.getControl("VolumeControl");
                    timeTask = new UpdateDisplayTask(mp3Player, timeGauge);

                    timeDisplayTimer.scheduleAtFixedRate(timeTask, 0, 1000);
                } catch (IOException ex) {
                    ex.printStackTrace();
                } catch (MediaException ex) {
                    ex.printStackTrace();
                }

                ticker.setString(currentFileUrl);

                switchDisplayable(null, screenPlayer);
            } else if (command == cmdFileBrowserQuit) {
                exitMIDlet();
            }
        } else if (displayable == screenPlayer) {
            if (command == cmdBack15Sec) {
                adjustPlayerTimeBySeconds(-15);
            } else if (command == cmdChangeTrack) {
                switchDisplayable(null, getScreenFileBrowser());
                mp3Player.close();
                mp3Player = null;
                timeTask.cancel();
                timeTask = null;

                // Clear the saved preferences (since we're going to another track)
                clearSavedPreferences( LAST_FILE_RECORDSTORE );
                try {
                    // Clear the RecordStore if it exists
                    RecordStore.deleteRecordStore(LAST_FILE_RECORDSTORE);
                } catch (RecordStoreException ex) {
                    ex.printStackTrace();
                }
            } else if (command == cmdPlayerPause) {
                try {
                    if ( mp3Player.getState() == Player.STARTED ){
                        mp3Player.stop();
                    } else {
                        mp3Player.start();
                    }
                } catch (MediaException ex){
                    ex.printStackTrace();
                }
            } else if (command == cmdPlayerQuit) {
                savePreferences( LAST_FILE_RECORDSTORE, currentFileUrl, mp3Player.getMediaTime());

                exitMIDlet();
            }
        }
    }

    /**
     * Returns an initiliazed instance of screenFileBrowser component.
     * @return the initialized component instance
     */
    public FileBrowser getScreenFileBrowser() {
        if (screenFileBrowser == null) {
            screenFileBrowser = new FileBrowser(getDisplay());
            screenFileBrowser.setTitle("fileBrowser");
            screenFileBrowser.setCommandListener(this);
            screenFileBrowser.setFilter("");
            screenFileBrowser.addCommand(FileBrowser.SELECT_FILE_COMMAND);
            screenFileBrowser.addCommand(getCmdFileBrowserQuit());
        }
        return screenFileBrowser;
    }

    /**
     * Returns an initiliazed instance of cmdFileBrowserQuit component.
     * @return the initialized component instance
     */
    public Command getCmdFileBrowserQuit() {
        if (cmdFileBrowserQuit == null) {
            cmdFileBrowserQuit = new Command("Exit", Command.EXIT, 0);
        }
        return cmdFileBrowserQuit;
    }

    /**
     * Returns an initiliazed instance of ticker component.
     * @return the initialized component instance
     */
    public Ticker getTicker() {
        if (ticker == null) {
            ticker = new Ticker("");
        }
        return ticker;
    }

    /**
     * Returns an initiliazed instance of timeGauge component.
     * @return the initialized component instance
     */
    public Gauge getTimeGauge() {
        if (timeGauge == null) {
            timeGauge = new Gauge("0:00:00", true, 100, 0);
        }
        return timeGauge;
    }

    /**
     * Returns an initiliazed instance of volumeGauge component.
     * @return the initialized component instance
     */
    public Gauge getVolumeGauge() {
        if (volumeGauge == null) {
            volumeGauge = new Gauge("Volume", true, 25, 12);
        }
        return volumeGauge;
    }

    /**
     * Returns an initiliazed instance of cmdPlayerPause component.
     * @return the initialized component instance
     */
    public Command getCmdPlayerPause() {
        if (cmdPlayerPause == null) {
            cmdPlayerPause = new Command("Play/Pause", Command.OK, 1);
        }
        return cmdPlayerPause;
    }

    /**
     * Returns an initiliazed instance of cmdPlayerQuit component.
     * @return the initialized component instance
     */
    public Command getCmdPlayerQuit() {
        if (cmdPlayerQuit == null) {
            cmdPlayerQuit = new Command("Quit", Command.EXIT, 15);
        }
        return cmdPlayerQuit;
    }

    /**
     * Returns an initiliazed instance of cmdBack15Sec component.
     * @return the initialized component instance
     */
    public Command getCmdBack15Sec() {
        if (cmdBack15Sec == null) {
            cmdBack15Sec = new Command("15s", Command.ITEM, 2);
        }
        return cmdBack15Sec;
    }

    /**
     * Returns an initiliazed instance of cmdChangeTrack component.
     * @return the initialized component instance
     */
    public Command getCmdChangeTrack() {
        if (cmdChangeTrack == null) {
            cmdChangeTrack = new Command("Change Track", Command.BACK, 15);
        }
        return cmdChangeTrack;
    }

    /**
     * Performs an action assigned to the isLastFileNameSaved if-point.
     */
    public void isLastFileNameSaved() {
        if (currentFileUrl == null) {
            switchDisplayable(null, getScreenFileBrowser());
        } else {
                try {
                    mp3Player = Manager.createPlayer(currentFileUrl);
                    mp3Player.realize();
                    mp3Player.prefetch();
                    mp3Player.setMediaTime(startTime);
                    volumeControl = (VolumeControl) mp3Player.getControl("VolumeControl");
                    timeTask = new UpdateDisplayTask(mp3Player, timeGauge);

                    timeDisplayTimer.scheduleAtFixedRate(timeTask, 0, 500);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    switchDisplayable(null, getScreenFileBrowser() );
                    return;
                } catch (MediaException ex) {
                    ex.printStackTrace();
                    switchDisplayable(null, getScreenFileBrowser() );
                    return;
                }

                ticker.setString(currentFileUrl);

                switchDisplayable(null, screenPlayer);
        }
    }

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
