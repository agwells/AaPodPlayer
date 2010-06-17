/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.iowaline.screenhandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotOpenException;
import net.iowaline.PodPlayerMidlet;

/**
 *
 * @author aaron
 */
public class PreferencesHandler {
    public static final String LAST_FILE_RECORDSTORE = "lastFilePlaying";
    PodPlayerMidlet midlet;

    public PreferencesHandler(PodPlayerMidlet midlet) {
        this.midlet = midlet;
    }

    /**
     *
     * @param recordStoreName
     * @param fileUrl
     * @param playTime
     */
    public void savePreferences(String recordStoreName, String fileUrl, long playTime ){
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
    public void readPreferences(String recordStoreName){
        midlet.getPlayerScreenHandler().setCurrentFileUrl(null);
        midlet.getPlayerScreenHandler().setStartTime( 0L );
        RecordStore store = null;
        try {
            store = RecordStore.openRecordStore(recordStoreName, true);
            int numRecords = store.getNumRecords();
//            System.out.println("numRecords: " + Integer.toString(numRecords));
            if ( store.getNumRecords() > 0 ){
                ByteArrayInputStream byteStream = new ByteArrayInputStream(store.getRecord(1));
                DataInputStream dataStream = new DataInputStream(byteStream);
                midlet.getPlayerScreenHandler().setCurrentFileUrl( dataStream.readUTF() );
                midlet.getPlayerScreenHandler().setStartTime(dataStream.readLong());
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
    public void clearSavedPreferences( String recordStoreName ){
        try {
            // Clear the RecordStore if it exists, before saving this one
            RecordStore.deleteRecordStore(recordStoreName);
        } catch (RecordStoreException ex) {
            ex.printStackTrace();
        }
    }
}
