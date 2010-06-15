package net.iowaline;

/**
 *
 * @author aaron
 */
public class Bookmark {
    private long byteOffset = 0;
    private int millisecondsElapsed = 0;
    private String playlistName = "";
    private String fileName = "";

    /**
     * Creates a new Bookmark, based on a line from a .bmark file
     *
     * @param bookmarkLine The line from the .bmark file
     */
    public Bookmark( String bookmarkLine ){
        if ( bookmarkLine.length() > 0 ){
            if ( bookmarkLine.charAt(0) == '>' ){
                parseNewFormat( bookmarkLine );
            } else {
                parseOldFormat( bookmarkLine );
            }
        }
    }

    /**
     * Parses the old style of Rockbox bookmarks (bookmark.h r25503 and earlier)
     * It contains the following semicolon-separated fields
     *
     * 0. playlist position
     * 1. byte offset
     * 2. shuffle seed
     * 3. hard-coded to 0
     * 4. time elapsed in milliseconds
     * 5. repeat mode
     * 6. shuffle mode
     * 7. name of playlist
     * 8. name of file
     * 
     * @param bookmarkLine The line from the .bmark file to parse
     */
    private void parseOldFormat(String bookmarkLine) {
        int startOfField = 0;
        int endOfField = 0;
        int i = 0;
        String fieldValue = null;
        do {
            endOfField = bookmarkLine.indexOf(";", startOfField);
            
            if ( endOfField == -1 ){
                fieldValue = bookmarkLine.substring(startOfField);
            } else {
                fieldValue = bookmarkLine.substring(startOfField, endOfField);
            }
            switch( i ){
                case 1:
                    try {
                        byteOffset = Long.parseLong(fieldValue);
                    } catch (NumberFormatException nfe){
                        byteOffset = 0;
                    }
                    break;
                case 4:
                    try {
                        millisecondsElapsed = Integer.parseInt(fieldValue);
                    } catch (NumberFormatException nfe){
                        millisecondsElapsed = 0;
                    }
                    break;
                case 7:
                    playlistName = fieldValue;
                    break;
                case 8:
                    fileName = fieldValue;
                    break;
            }
            startOfField = endOfField + 1;
            i++;
        } while (endOfField > -1);
    }

    /**
     * Parse a line from a new-style .bmark file (bookmark.h r25576 and later)
     * It contains the following semicolon-separated fields
     *
     * 0. a ">" followed by pitch & speed flags
     * 1. playlist position
     * 2. byte offset
     * 3. shuffle seed
     * 4. time elapsed in milliseconds
     * 5. repeat mode
     * 6. shuffle mode
     * 7. pitch
     * 8. timestretch
     * 9. playlist name
     * 10. file name
     *
     * @param bookmarkLine
     */
    private void parseNewFormat(String bookmarkLine) {
        // skip the initial "<"
        int startOfField = 1;
        int endOfField = 0;
        int i = 0;
        String fieldValue = null;
        do {
            endOfField = bookmarkLine.indexOf(";", startOfField);

            if ( endOfField == -1 ){
                fieldValue = bookmarkLine.substring(startOfField);
            } else {
                fieldValue = bookmarkLine.substring(startOfField, endOfField);
            }
            switch( i ){
                case 3:
                    try {
                        byteOffset = Long.parseLong(fieldValue);
                    } catch (NumberFormatException nfe){
                        byteOffset = 0;
                    }
                    break;
                case 4:
                    try {
                        millisecondsElapsed = Integer.parseInt(fieldValue);
                    } catch (NumberFormatException nfe){
                        millisecondsElapsed = 0;
                    }
                    break;
                case 9:
                    playlistName = fieldValue;
                    break;
                case 10:
                    fileName = fieldValue;
                    break;
            }
            startOfField = endOfField + 1;
            i++;
        } while (endOfField > -1);
    }

    public String toString(){
       return Util.formatMilliSeconds( (long) millisecondsElapsed)
               + ", " + fileName;
    }

    public long getByteOffset() {
        return byteOffset;
    }

    public String getFileName() {
        return fileName;
    }

    public int getMillisecondsElapsed() {
        return millisecondsElapsed;
    }

    public String getPlaylistName() {
        return playlistName;
    }
}
