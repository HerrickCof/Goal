package org.sysu.herrick.goal;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

/**
 * Created by Herrick on 2017/1/7.
 */

public class MailAuthenticator extends Authenticator
{
    private static String userName = "goalbackupservice";
    private static String password = "nothingfunny12";
    public MailAuthenticator() {}
    protected PasswordAuthentication getPasswordAuthentication()
    {
        return new PasswordAuthentication(userName, password);
    }

    public static String getPassword() {
        return password;
    }

    public static String getUserName() {
        return userName;
    }
}