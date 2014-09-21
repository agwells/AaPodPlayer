/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.iowaline.screenhandler;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemStateListener;
import javax.microedition.lcdui.Ticker;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.control.VolumeControl;
import net.iowaline.PodPlayerMidlet;
import net.iowaline.Util;

/**
 *
 * @author aaron
 */
public class PlayerScreenHandler implements CommandListener, ItemStateListener{
    private PodPlayerMidlet midlet;
    private String currentFileUrl;
    private Form screen;

    private Gauge volumeGauge = new Gauge("Volume", true, 25, 12);;
    private Gauge timeGauge = new Gauge("0:00:00", true, 100, 0);;
    private Command cmdChangeTrack = new Command("Change Track", Command.ITEM, 15);
    private Command cmdBack15Sec = new Command("15s", Command.ITEM, 2);
    private Command cmdPlayerQuit = new Command("Quit", Command.EXIT, 15);
    private Command cmdPlayerPause = new Command("Play/Pause", Command.OK, 1);
    private Command cmdListBookmarks = new Command("List bookmarks", Command.ITEM, 10);
    private Ticker ticker = new Ticker("");

    private Player mp3Player;
    private VolumeControl volumeControl;
    private Timer timeDisplayTimer;
    private UpdateDisplayTask timeTask;

    long startTime = 0L;

    public String getCurrentFileUrl() {
        return this.currentFileUrl;
    }

    public void setStartTime(long microseconds) {
        this.startTime = microseconds;
    }

    public long getStartTime() {
        return this.startTime;
    }

    /**
     * Private class for the task to update the player's time display
     */
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
                timeGauge.setLabel(Util.formatMicroseconds(time));
                timeGauge.setValue((int) ( time * timeGaugeMaxValue / mediaDuration) );
            }
        }
    }
    /*
     * End of class UpdateDisplayTask
     */

    /**
     * Constructor
     * 
     * @param midlet
     */
    public PlayerScreenHandler( PodPlayerMidlet midlet ){
        this.midlet = midlet;
        screen = new Form("Paused", new Item[] { timeGauge, volumeGauge });
        screen.setTicker(ticker);
        screen.addCommand(cmdPlayerPause);
        screen.addCommand(cmdPlayerQuit);
        screen.addCommand(cmdBack15Sec);
        screen.addCommand(cmdChangeTrack);
        screen.setCommandListener(this);
        screen.addCommand(cmdListBookmarks);
        screen.setItemStateListener(this);

        timeDisplayTimer = new Timer();
    }

    public void setCurrentFileUrl(String currentFileUrl) {
        this.currentFileUrl = currentFileUrl;
    }

    public Form getScreen(){
        return this.screen;
    }

    /**
     * Starts the MP3 player
     * @param startTime
     */
    public boolean startMp3Player( long startTime ) {
        try {
            mp3Player = Manager.createPlayer(currentFileUrl);
            mp3Player.realize();
            mp3Player.prefetch();
            mp3Player.setMediaTime(startTime);
            volumeControl = (VolumeControl) mp3Player.getControl("VolumeControl");
            timeTask = new UpdateDisplayTask(mp3Player, timeGauge);
            timeDisplayTimer.scheduleAtFixedRate(timeTask, 0, 500);
        } catch (IOException ex) {
            return false;
        } catch (MediaException ex) {
            return false;
        }
        return true;
    }

    /**
     * Perform tasks to stop the MP3 player
     */
    private void stopMp3Player() {
        try {
            mp3Player.close();
            mp3Player = null;
        } catch (Exception e){
            // todo: something here that won't make Java dudes cry into their beards
            e.printStackTrace();
        }
        try {
            timeTask.cancel();
            timeTask = null;
        } catch (Exception e){
            // todo: see above
            e.printStackTrace();
        }
    }

    public void commandAction(Command command, Displayable dsplbl) {
        if (command == cmdBack15Sec) {
            adjustPlayerTimeBySeconds(-15);
        } else if (command == cmdChangeTrack) {
            midlet.getDisplay().setCurrent( midlet.getFileBrowserScreenHandler().getScreen());
            stopMp3Player();

            // Clear the saved preferences (since we're going to another track)
            midlet.getPreferencesHandler().clearSavedPreferences( PreferencesHandler.LAST_FILE_RECORDSTORE );
        } else if (command == cmdPlayerPause) {
            try {
                if ( mp3Player.getState() == Player.STARTED ){
                    mp3Player.stop();
                } else {
                    mp3Player.setMediaTime(mp3Player.getMediaTime());
                    mp3Player.start();
//                    mp3Player.setMediaTime(l);
                }
            } catch (MediaException ex){
                ex.printStackTrace();
            }
        } else if ( command == cmdListBookmarks ){
            // switch to list of bookmarks
            stopMp3Player();
            midlet.getBookmarkListScreenHandler().listBookmarks(currentFileUrl);
        } else if (command == cmdPlayerQuit) {
            midlet.getPreferencesHandler().savePreferences( PreferencesHandler.LAST_FILE_RECORDSTORE, currentFileUrl, mp3Player.getMediaTime());
            midlet.exitMIDlet();
        }
    }

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
            midlet.getDisplay().setCurrent(new Alert("adjustPlayerTimeBySeconds", ex.toString(), null, AlertType.ERROR), Display.getDisplay(midlet).getCurrent());
        }
    }

    public void adjustPlayerVolumeByGauge( Gauge volumeGauge ){
        volumeControl.setLevel( volumeGauge.getValue() * 100 / volumeGauge.getMaxValue());
    }

    public Ticker getTicker() {
        return ticker;
    }

}
