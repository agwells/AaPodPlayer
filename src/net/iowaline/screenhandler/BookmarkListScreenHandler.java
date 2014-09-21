/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.iowaline.screenhandler;

import java.io.IOException;
import java.util.Vector;
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;
import javax.microedition.media.MediaException;
import net.iowaline.Bookmark;
import net.iowaline.BookmarkReader;
import net.iowaline.PodPlayerMidlet;

/**
 *
 * @author aaron
 */
public class BookmarkListScreenHandler implements CommandListener{
    private PodPlayerMidlet midlet;
    private List screen = new List("Bookmarks", Choice.IMPLICIT);
    private Command cmdBack = new Command("Back", Command.BACK, 1);
    private BookmarkReader bookmarkReader = null;

    public BookmarkListScreenHandler( PodPlayerMidlet midlet ){
        this.midlet = midlet;

        screen.addCommand(cmdBack);
        screen.setCommandListener(this);
    }

    public void commandAction(Command command, Displayable dsplbl) {
        if ( command == cmdBack ){
            this.bookmarkReader = null;
            midlet.getDisplay().setCurrent( midlet.getPlayerScreenHandler().getScreen() );
        } else if ( command == List.SELECT_COMMAND ){
            Bookmark b = (Bookmark) this.bookmarkReader.getFullBmarkList().elementAt(screen.getSelectedIndex());
            midlet.getPlayerScreenHandler().setCurrentFileUrl( this.bookmarkReader.getBmarkDirectory() + "/" + b.getFileName() );
            midlet.getPlayerScreenHandler().startMp3Player(b.getMillisecondsElapsed() * 1000);
            this.bookmarkReader = null;
            midlet.getDisplay().setCurrent( midlet.getPlayerScreenHandler().getScreen());
        }
    }

        /**
     * Find the bookmark file for the current file, and list any bookmarks for
     * it
     *
     * @param currentFileUrl
     */
    public void listBookmarks(String currentFileUrl) {
        BookmarkReader reader = new BookmarkReader(currentFileUrl);
        Vector bmarkList = reader.getFullBmarkList();
        screen.deleteAll();
        for( int i = 0; i < bmarkList.size(); i++ ){
            screen.insert(i, ((Bookmark) bmarkList.elementAt(i)).toString(), null);
        }
        this.bookmarkReader = reader;
        midlet.getDisplay().setCurrent( screen);
    }

}
