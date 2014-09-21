package net.iowaline;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import javax.microedition.media.MediaException;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotOpenException;
import net.iowaline.screenhandler.BookmarkListScreenHandler;
import net.iowaline.screenhandler.FileBrowserScreenHandler;
import net.iowaline.screenhandler.PlayerScreenHandler;
import net.iowaline.screenhandler.PreferencesHandler;

/**
 * @author aaron
 */
public class PodPlayerMidlet extends MIDlet {

    private boolean midletPaused = false;

    private BookmarkListScreenHandler bookmarkScreenHandler;
    private FileBrowserScreenHandler fileBrowserScreenHandler;
    private PlayerScreenHandler playerScreenHandler;
    private PreferencesHandler prefsHandler;

    /**
     * The PodPlayerMidlet constructor.
     */
    public PodPlayerMidlet() {
    }

    /**
     * Initializes the application.
     * It is called only once when the MIDlet is started. The method is called before the <code>startMIDlet</code> method.
     */
    private void initialize() {
        bookmarkScreenHandler = new BookmarkListScreenHandler(this);
        fileBrowserScreenHandler = new FileBrowserScreenHandler(this);
        playerScreenHandler = new PlayerScreenHandler(this);
        prefsHandler = new PreferencesHandler(this);
        prefsHandler.readPreferences(PreferencesHandler.LAST_FILE_RECORDSTORE);

        // Decide which screen to go to based on whether we can resume the last file
        if (playerScreenHandler.getCurrentFileUrl() == null) {
            getDisplay().setCurrent( fileBrowserScreenHandler.getScreen());
        } else if (playerScreenHandler.startMp3Player(playerScreenHandler.getStartTime())){
            playerScreenHandler.getTicker().setString(playerScreenHandler.getCurrentFileUrl());
            getDisplay().setCurrent( playerScreenHandler.getScreen());
        } else {
            getDisplay().setCurrent( fileBrowserScreenHandler.getScreen() );
        }
    }

    /**
     * Performs an action assigned to the Mobile Device - MIDlet Resumed point.
     */
    public void resumeMIDlet() {
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
        getDisplay().setCurrent(null);
        destroyApp(true);
        notifyDestroyed();
    }

    /**
     * Called when MIDlet is started.
     * Checks whether the MIDlet have been already started and initialize/starts or resumes the MIDlet.
     */
    public void startApp() {
        if (midletPaused) {
            resumeMIDlet();
        } else {
            initialize();
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

    public PlayerScreenHandler getPlayerScreenHandler() {
        return playerScreenHandler;
    }

    public BookmarkListScreenHandler getBookmarkListScreenHandler() {
        return bookmarkScreenHandler;
    }

    public FileBrowserScreenHandler getFileBrowserScreenHandler() {
        return fileBrowserScreenHandler;
    }

    public PreferencesHandler getPreferencesHandler(){
        return prefsHandler;
    }
}
