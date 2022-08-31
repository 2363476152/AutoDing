package com.pengxh.autodingding.utils;

import android.util.Log;

import com.pengxh.app.multilib.widget.EasyToast;
import com.pengxh.autodingding.bean.MailInfo;

import java.io.File;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;


public class MailSender {
    /**
     * 以文本格式发送邮件
     *
     * @param mailInfo 待发送的邮件的信息
     */
    public void sendTextMail(MailInfo mailInfo) throws MessagingException {
        // 判断是否需要身份认证
        EmailAuthenticator authenticator = null;
        Properties pro = mailInfo.getProperties();
        if (mailInfo.isValidate()) {
            // 如果需要身份认证，则创建一个密码验证器
            authenticator = new EmailAuthenticator(mailInfo.getUserName(), mailInfo.getPassword());
        }
        // 根据邮件会话属性和密码验证器构造一个发送邮件的session
        Session sendMailSession = Session.getDefaultInstance(pro, authenticator);

        // 根据session创建一个邮件消息
        Message mailMessage = new MimeMessage(sendMailSession);
        // 创建邮件发送者地址
        Address from = new InternetAddress(mailInfo.getFromAddress());
        // 设置邮件消息的发送者
        mailMessage.setFrom(from);
        // 创建邮件的接收者地址，并设置到邮件消息中
        Address to = new InternetAddress(mailInfo.getToAddress());
        mailMessage.setRecipient(Message.RecipientType.TO, to);
        // 设置邮件消息的主题
        String mailSubject = mailInfo.getSubject();
        mailMessage.setSubject(mailSubject);
        // 设置邮件消息发送的时间
        mailMessage.setSentDate(new Date());
        // 设置邮件消息的主要内容
        String mailContent = mailInfo.getContent();
        mailMessage.setText(mailContent);
        // 发送邮件
        Transport.send(mailMessage);

    }

    // 发送带附件的邮件
    boolean sendAccessoryMail(MailInfo mailInfo) {
        Log.d("MailSender", "sendAccessoryMail: 发送带附件的邮件");
        // 判断是否需要身份验证
        EmailAuthenticator authenticator = null;
        Properties p = mailInfo.getProperties();
        // 如果需要身份验证，则创建一个密码验证器
        if (mailInfo.isValidate()) {
            authenticator = new EmailAuthenticator(mailInfo.getUserName(), mailInfo.getPassword());
        }
        // 根据邮件会话属性和密码验证器构造一个发送邮件的session
        Session sendMailSession = Session.getDefaultInstance(p, authenticator);
        try {
            // 根据session创建一个邮件消息
            Message mailMessage = new MimeMessage(sendMailSession);
            // 创建邮件发送者的地址
            Address fromAddress = new InternetAddress(mailInfo.getFromAddress());
            // 设置邮件消息的发送者
            mailMessage.setFrom(fromAddress);
            // 创建邮件接收者的地址
            Address toAddress = new InternetAddress(mailInfo.getToAddress());
            // 设置邮件消息的接收者
            mailMessage.setRecipient(Message.RecipientType.TO, toAddress);
            // 设置邮件消息的主题
            mailMessage.setSubject(mailInfo.getSubject());
            // 设置邮件消息的发送时间
            mailMessage.setSentDate(new Date());
            // MimeMultipart类是一个容器类，包含MimeBodyPart类型的对象
            Multipart mainPart = new MimeMultipart();
            File file = mailInfo.getAttachFile();
            if (!file.exists()) {
                EasyToast.showToast("需要导出的表格不存在，请重试", EasyToast.WARING);
                return false;
            } else {
                // 创建一个MimeBodyPart来包含附件
                BodyPart bodyPart = new MimeBodyPart();
                DataSource source = new FileDataSource(file);
                bodyPart.setDataHandler(new DataHandler(source));
                bodyPart.setFileName(MimeUtility.encodeWord(file.getName()));
                mainPart.addBodyPart(bodyPart);
            }
            // 将MimeMultipart对象设置为邮件内容
            mailMessage.setContent(mainPart);
            // 发送邮件
            Transport.send(mailMessage);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
