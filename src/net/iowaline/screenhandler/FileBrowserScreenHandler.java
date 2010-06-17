/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.iowaline.screenhandler;

import java.io.IOException;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.media.MediaException;
import net.iowaline.PodPlayerMidlet;
import org.netbeans.microedition.lcdui.pda.FileBrowser;

/**
 *
 * @author aaron
 */
public class FileBrowserScreenHandler implements CommandListener{
    private FileBrowser screenFileBrowser;
    private PodPlayerMidlet midlet;
    private Command cmdFileBrowserQuit;

    public FileBrowserScreenHandler( PodPlayerMidlet midlet ){
        this.midlet = midlet;
        screenFileBrowser = new FileBrowser(Display.getDisplay(midlet));
        screenFileBrowser.setTitle("fileBrowser");
        screenFileBrowser.setCommandListener(this);
        screenFileBrowser.setFilter("");
        screenFileBrowser.addCommand(FileBrowser.SELECT_FILE_COMMAND);
        cmdFileBrowserQuit = new Command("Exit", Command.EXIT, 0);
        screenFileBrowser.addCommand(cmdFileBrowserQuit);
    }

    public FileBrowser getScreen(){
        return this.screenFileBrowser;
    }

    public void commandAction(Command command, Displayable dsplbl) {
        if (command == FileBrowser.SELECT_FILE_COMMAND) {

            PlayerScreenHandler playerListener = midlet.getPlayerScreenHandler();
            playerListener.setCurrentFileUrl( screenFileBrowser.getSelectedFileURL() );
            playerListener.startMp3Player(0);

            midlet.getPlayerScreenHandler().getTicker().setString(playerListener.getCurrentFileUrl());

            midlet.getDisplay().setCurrent( midlet.getPlayerScreenHandler().getScreen() );
        } else if (command == cmdFileBrowserQuit) {
            midlet.exitMIDlet();
        }
    }
}
