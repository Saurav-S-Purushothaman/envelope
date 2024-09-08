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


(defn base64->bytes
  "Decodes a Base64-encoded string into a byte array"
  [s]
  (let [decoder (java.util.Base64/getDecoder)]
    (.decode decoder s)))


(defn bs64-image->DataHandler
  "Returns a A javax.activation.DataHandler object for a given
  Base64-encoded image string and specifies the MIME type as
  'image/png'"
  [image]
  (DataHandler. (base64->bytes image) "image/png"))


(defn bs64-image->DataHandler
  [image]
  (DataHandler . image "image/png"))


(defn image-part
  "Creates a MimeBodyPart object containing an image attachment
  Args:
  image - A Base64-encoded string representing an image
  uid - A string to be used as the Content-ID header for the image part
  Returns:
  A javax.mail.internet.MimeBodyPart object that contains the image as
  an inline attachment"
  [image uid]
  (doto (MimeBodyPart.)
    (.setDataHandler (image-data-handler image))
    (.setDisposition MimeBodyPart/INLINE)
    (.addHeader  "Content-ID" uid)))


(defn address
  "Extracts the email addresses of the recipients from a MimeMessage"
  [message]
  (map #(.getAddress %)
       (.getRecipients message Message$RecipientType/TO)))


(defn mime-message
  "Creates a MimeMessage object for an email
  Returns:
  A javax.mail.internet.MimeMessage object with the specified sender,
  recipients, and subject"
  ([from display-name to subject session]
   (let [message (MimeMessage. session)]
     (doto message
       (.setFrom (InternetAddress. from display-name))
       (.setRecipients Message$RecipientType/TO
                       (InternetAddress/parse to))
       (.setSubject subject))))
  ([from to subject session]
   (let [message (MimeMessage. session)]
     (doto message
       (.setFrom (InternetAddress. from))
       (.setRecipients Message$RecipientType/TO
                       (InternetAddress/parse to))
       (.setSubject subject)))))


(defn multi-part
  []
  (MimeMultipart. "Related"))


(defn add-body
  [multi-part mime-body-part]
  (.addBodyPart multi-part mime-body-part))


(defn set-content
  [multi-part message]
  (.setContent message multi-part))


(defn add-bcc
  [message bcc]
  (.setRecipients message Message$RecipientType/BCC
                  (InternetAddress/parse bcc)))


(defn add-cc
  [message cc]
  (.setRecipients message Message$RecipientType/CC
                  (InternetAddress/parse cc)))


(defn add-to
  [message to]
  (.setRecipients message Message$RecipientType/CC
                  (InternetAddress/parse to)))


(defn add-header
  [message key val]
  (.addHeader message key val))


(defn send-email!
  "Sends an email message using the provided MimeMessage object
  Args:
  message - A javax.mail.internet.MimeMessage object representing the email to be sent.
  _connection-string - A placeholder for a connection string or API
    key used for authentication (currently not implemented).
  Returns:
  nil. This function performs side effects by sending an email and
  logging the process."
  [message]
  (Transport/send message))


(comment
  (def username "saurav.kudajadri@gmail.com")
  (def password "nil")

  )
