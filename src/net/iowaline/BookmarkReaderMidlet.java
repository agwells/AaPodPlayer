package net.iowaline;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.Vector;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;
import javax.microedition.midlet.*;
import org.netbeans.microedition.lcdui.pda.FileBrowser;

/**
 * @author aaron
 */
public class BookmarkReaderMidlet extends MIDlet implements CommandListener {

    private Display display;
    private FileBrowser scrnFileBrowser;
    private Command cmdFileBrowserQuit;
    private Command cmdBmarkListBack;

    public void startApp() {
        display = Display.getDisplay(this);
        scrnFileBrowser = new FileBrowser(display);
        scrnFileBrowser.setCommandListener(this);
        scrnFileBrowser.setTitle("File to check for bookmarks");
        cmdFileBrowserQuit = new Command("Quit", Command.EXIT, 1);
        scrnFileBrowser.addCommand(cmdFileBrowserQuit);

        cmdBmarkListBack = new Command("Back", Command.BACK, 1);
        display.setCurrent(scrnFileBrowser);
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }

    public void commandAction(Command cmnd, Displayable dsplbl) {
try {
        // Exit button on file browser screen
        if ( cmnd == cmdFileBrowserQuit ){
            notifyDestroyed();
        }

        // Selecting a file from file browser
        if ( dsplbl == scrnFileBrowser && cmnd == FileBrowser.SELECT_FILE_COMMAND ){
            BookmarkReader bmark = new BookmarkReader(scrnFileBrowser.getSelectedFileURL());
            List scrnBookmarkList = new List(
                    bmark.getBmarkDirectory(),
                    List.IMPLICIT,
                    new String[0],
                    null
            );
            Vector bmarkList = bmark.getFullBmarkList();
            for( int i = 0; i < bmarkList.size(); i++ ){
                scrnBookmarkList.append( bmarkList.elementAt(i).toString(), null);
            }
            scrnBookmarkList.setCommandListener(this);
            scrnBookmarkList.setSelectCommand(cmdBmarkListBack);
            if ( scrnBookmarkList.size() == 0 ){
                scrnBookmarkList.setSelectCommand(List.SELECT_COMMAND);
                scrnBookmarkList.addCommand(cmdBmarkListBack);
            }
            display.setCurrent(scrnBookmarkList);
        }

        // Back button on file browser
        if ( cmnd == cmdBmarkListBack ){
            display.setCurrent(scrnFileBrowser);
        }
}catch (Exception ex){
    ex.printStackTrace();
}
    }
}
