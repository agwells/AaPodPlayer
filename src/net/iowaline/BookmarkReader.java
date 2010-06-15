package net.iowaline;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author aaron
 */
public class BookmarkReader {
    private final String selectedFileUrl;
    private final String bmarkUrl;
    private final FileConnection bmarkFile;
    private long fileSize;
    private final Vector bmarkList = new Vector();

    BookmarkReader(String selectedFileUrl) {
        this.selectedFileUrl = selectedFileUrl;
        // Figure out the parent directory's name
        bmarkUrl = this.selectedFileUrl.substring(0, this.selectedFileUrl.lastIndexOf('/')) + ".bmark";

        FileConnection fc = null;
        try {
            // Get the path to the underlying directory
            fc = (FileConnection) Connector.open(bmarkUrl, Connector.READ);
        } catch (IOException ex) {
            fc = null;
            ex.printStackTrace();
        }
        bmarkFile = fc;

        StringBuffer fileContentBuffer = new StringBuffer();
        InputStream instream = null;
        if ( bmarkFile != null && bmarkFile.exists() && !bmarkFile.isDirectory() && bmarkFile.canRead()){
            try {
                fileSize = bmarkFile.fileSize();
                instream = bmarkFile.openInputStream();

                int ch = 0;
                while ( (ch = instream.read()) != -1 ){
                    if ( (char) ch == '\n' ){
                        bmarkList.addElement( new Bookmark(fileContentBuffer.toString()));
                        fileContentBuffer = new StringBuffer();
                    } else {
                        fileContentBuffer.append( (char) ch );
                    }
                }
            } catch (IOException ex) {
                fileSize = 0;
            } finally {
                try {
                    if ( instream != null ) instream.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        } else {
            fileSize = 0;
        }
    }

    long getFileSize(){
        return this.fileSize;
    }

    String getBmarkUrl(){
        return this.bmarkUrl;
    }

    Vector getBmarkList(){
        return this.bmarkList;
    }
}
