package org.sysu.herrick.goal;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.ContextCompat;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.AuthenticationFailedException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * Created by Herrick on 2017/1/7.
 */

public class MailUtils {

    private static final String TABLE_NAME = "Diary_Data";
    private DiaryDB diaryDB = null;

    private static String mailServerHost = "smtp.163.com";

    private static String fromAddress = "goalbackupservice@163.com";

    private static String subject = "GOAL-DIARY BACKUP";

    private static String content = "Your diary backup, see attachment for detail";

    private boolean debug = false;

    private String messageContentMimeType ="text/html; charset=gb2312";

    private Context context;
    private int which_diary;

    private static int whole_diaries = -5555;

    private static String[] weekday = new String[] {
            "SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"
    };

    public MailUtils(Context context, int id) {
        diaryDB = DiaryDB.getInstance(context);
        this.context = context;
        which_diary = id;
    }

    private void fillMail(Session session, MimeMessage msg, String toAddress) throws IOException, MessagingException {
        Multipart mPart = new MimeMultipart();
        msg.setFrom(new InternetAddress(fromAddress));
        InternetAddress[] replyAddress = {new InternetAddress(fromAddress)};
        InternetAddress address = new InternetAddress(toAddress);
        msg.setRecipient(Message.RecipientType.TO, address);
        msg.setSubject(subject);
        msg.setReplyTo(replyAddress);

        // create and fill the first message part
        MimeBodyPart mBodyContent = new MimeBodyPart();
        mBodyContent.setContent(content, messageContentMimeType);
        mPart.addBodyPart(mBodyContent);

        // attach the file to the message

        String filepath = zipDiary();
        if (filepath.equals("error"))
            return;

        MimeBodyPart mBodyPart = new MimeBodyPart();
        // attach the file to the message
        FileDataSource fds = new FileDataSource(filepath);
        mBodyPart.setDataHandler(new DataHandler(fds));
        mBodyPart.setFileName("DiaryBackup.zip");
        mPart.addBodyPart(mBodyPart);
        msg.setContent(mPart);
        msg.setSentDate(new Date());
    }
    private String zipDiary() {
        File[] filepath = ContextCompat.getExternalCacheDirs(context);
        String path = filepath[0].getPath() + File.separator +"diaryBackup";
        /*if (filepath[0] == null || !filepath[0].isDirectory())
            return "error";*/
        File zipFileDir = null;
        Cursor cursor = null;
        if (which_diary == whole_diaries)
            cursor = diaryDB.query("SELECT * from " + TABLE_NAME);
        else
            cursor = diaryDB.query("SELECT * from " + TABLE_NAME + " WHERE _id=" + which_diary);
        if (filepath[0].isDirectory()) {
            zipFileDir = new File(path);
            zipFileDir.mkdir();
            deleteFilesByDirectory(zipFileDir);
        }
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            //datePrime INTEGER, weekday INTEGER, editTime INTEGER, diary_content TEXT, weather TEXT
            for (int i = 0; i < cursor.getCount(); i++, cursor.moveToNext()) {
                int datePrime = cursor.getInt(cursor.getColumnIndex("datePrime"));
                try {
                    File diaryTXT = new File(path, datePrime + ".txt");
                    diaryTXT.createNewFile();
                    FileOutputStream fos_single = new FileOutputStream(diaryTXT);
                    fos_single.write(("[ " + datePrime + " ]\n").getBytes());
                    fos_single.write(("[ " + weekday[cursor.getInt(cursor.getColumnIndex("weekday")) - 1] + " ]\n").getBytes());
                    fos_single.write(("[ " + cursor.getString(cursor.getColumnIndex("weather")) + " ]\n").getBytes());
                    fos_single.write("[ LAST EDITED TIME ] ".getBytes());
                    Date currentTime = new Date(cursor.getLong(cursor.getColumnIndex("editTime")));
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String t = formatter.format(currentTime);
                    fos_single.write(t.getBytes());
                    fos_single.write(("\n[ CONTENT ]\n").getBytes());
                    fos_single.write(cursor.getString(cursor.getColumnIndex("diary_content")).getBytes());
                    fos_single.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            cursor.close();
            try {
                ZipUtils.zipFolder(path, path + ".zip");
            } catch (Exception e) {
                e.printStackTrace();
            }
            zipFileDir.delete();
        }
        return path + ".zip";
        /*File zip = new File(path + ".zip");
        if (zip.exists() && zip.isFile())
            return path + ".zip";
        else
            return "error";*/
    }

    public boolean sendMail(String toAdd) throws IOException, MessagingException {

        Properties props = System.getProperties();
        props.put("mail.smtp.host", mailServerHost);
        props.put("mail.smtp.auth", "true");

        MailAuthenticator auth = new MailAuthenticator();

        Session session = Session.getInstance(props, auth);
        session.setDebug(debug);
        MimeMessage msg = new MimeMessage(session);
        Transport trans = null;
        try {

            fillMail(session, msg, toAdd);
            // send the message
            trans = session.getTransport("smtp");
            try {
                trans.connect(mailServerHost, MailAuthenticator.getUserName(), MailAuthenticator.getPassword());
            } catch (AuthenticationFailedException e) {
                e.printStackTrace();
                //System.out.println("连接邮件服务器错误：");
                return false;
            } catch (MessagingException e) {
                //System.out.println("连接邮件服务器错误：");
                return false;
            }

            trans.send(msg);
            trans.close();

        } catch (MessagingException mex) {
            mex.printStackTrace();
            return false;
            /*System.out.println("发送邮件失败：");
            Exception ex = null;
            if ((ex = mex.getNextException()) != null) {
                System.out.println(ex.toString());
                ex.printStackTrace();
            }*/
        } finally {
            try {
                if (trans != null && trans.isConnected())
                    trans.close();
            } catch (Exception e) {
                //System.out.println(e.toString());
                return false;
            }
        }
        return true;
    }
    public static boolean isEmail(String email){
        if (null==email || "".equals(email)) return false;
        //Pattern p = Pattern.compile("\\w+@(\\w+.)+[a-z]{2,3}"); //简单匹配
        Pattern p =  Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");//复杂匹配
        Matcher m = p.matcher(email);
        return m.matches();
    }
    private static void deleteFilesByDirectory(File directory) {
        if (directory != null && directory.exists() && directory.isDirectory()) {
            for (File item : directory.listFiles()) {
                item.delete();
            }
        }
    }
}
