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
    private final String selectedFileName;
    private final String bmarkDirectory;
    private final FileConnection bmarkFile;
    private long fileSize;
    private final Vector bmarkList = new Vector();

    public BookmarkReader(String selectedFileUrl) {
        this.selectedFileUrl = selectedFileUrl;
        // Figure out the parent directory's name
        int lastSlashPos = this.selectedFileUrl.lastIndexOf('/');
        if ( lastSlashPos == -1 || lastSlashPos == this.selectedFileUrl.length()-1 ){
            throw new IllegalArgumentException("Invalid filename: " + selectedFileUrl);
        }
        bmarkDirectory = this.selectedFileUrl.substring(0, lastSlashPos);
        String bmarkUrl = bmarkDirectory + ".bmark";
        this.selectedFileName = this.selectedFileUrl.substring(
                this.selectedFileUrl.lastIndexOf('/')+1
        );

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

    public long getFileSize(){
        return this.fileSize;
    }

    public String getBmarkDirectory(){
        return this.bmarkDirectory;
    }

    /**
     * Get all the bookmarks in the file
     *
     * @return Vector a vector full of Bookmark items
     */
    public Vector getFullBmarkList(){
        return this.bmarkList;
    }

}
