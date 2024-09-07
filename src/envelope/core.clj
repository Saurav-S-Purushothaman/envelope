(ns envelope.core
  (:import
   [java.util
    ArrayList
    Date
    Properties
    Base64]
   [jakarta.mail
    Message
    Session
    Authenticator
    PasswordAuthentication
    Transport
    Message$RecipientType]
   [jakarta.mail.internet
    MimeMessage
    MimeBodyPart
    InternetAddress
    MimeMultipart]
   [java.io
    FileOutputStream
    File]
   [jakarta.activation
    DataHandler])
  (:gen-class))


(defn properties [username host port ssl]
  (let [properties (Properties.)]
    (doto properties
      (.put "mail.smtp.host" host)
      (.put "mail.smtp.user" username)
      (.put "mail.smtp.port" port)
      (.put "mail.smtp.socketFactory.port" port)
      (.put "mail.smtp.auth" "true"))
    (when ssl
      (doto properties
        (.put "mail.smtp.starttls.enable" "true")
        (.put "mail.smtp.socketFactory.class" "javax.net.ssl.SSLSocketFactory")
        (.put "mail.smtp.socketFactory.fallback" "false")))
    properties))


(defn create-session!
  [properties username password]
  (Session/getInstance properties
                       (proxy [Authenticator] []
                         (getPasswordAuthentication []
                           (PasswordAuthentication. username password)))))


(defn html-part
  "Creates a MimeBodyPart object containing an HTML message body
  Args:
  html-body - A string containing the HTML content of the email body
  Returns:
  A javax.mail.internet.MimeBodyPart object that contains the HTML
  content set with UTF-8 encoding"
  [html-body]
  (doto (MimeBodyPart.)
    (.setText html-body "utf-8" "html")))
