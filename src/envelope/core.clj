(ns envelope.core
  ^{:author "Saurav S Purushothaman",
    :doc "A core library for sending messages via SMTP"}
  (:import [java.util
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

(defn properties
  "Creates a Properties object with the necessary SMTP settings.
  Args:
  username - The email username for authentication.
  host - The SMTP server host.
  port - The SMTP server port.
  ssl - A boolean flag indicating if SSL should be enabled.
  Returns:
  A java.util.Properties object with the SMTP settings."
  [username host port]
  (let [properties (Properties.)]
    (doto properties
      (.put "mail.smtp.host" host)
      (.put "mail.smtp.user" username)
      (.put "mail.smtp.port" port)
      (.put "mail.smtp.socketFactory.port" port)
      (.put "mail.smtp.starttls.enable" "true");
      (.put "mail.smtp.auth" "true"))
    properties))

(defn create-session!
  "Creates a new mail session with the given properties and credentials.
  Args:
  properties - A java.util.Properties object containing the session properties.
  username - The username for authentication.
  password - The password for authentication.
  Returns:
  A jakarta.mail.Session object configured with the given properties and
  credentials."
  [properties username password]
  (Session/getInstance properties
                       (proxy [Authenticator] []
                         (getPasswordAuthentication []
                           (PasswordAuthentication. username password)))))

(defn html-part
  "Creates a MimeBodyPart object containing an HTML message body.
  Args:
  html-body - A string containing the HTML content of the email body.
  Returns:
  A javax.mail.internet.MimeBodyPart object that contains the HTML
  content set with UTF-8 encoding."
  [html-body]
  (doto (MimeBodyPart.)
    (.setText html-body "utf-8" "html")))

(defn base64->bytes
  "Decodes a Base64-encoded string into a byte array.
  Args:
  s - A string encoded in Base64.
  Returns:
  A byte array representing the decoded data."
  [s]
  (let [decoder (java.util.Base64/getDecoder)]
    (.decode decoder s)))

(defn bs64-image->DataHandler
  "Creates a DataHandler object from a Base64-encoded image.
  Args:
  image - A Base64-encoded string representing an image.
  Returns:
  A javax.activation.DataHandler object for the given Base64-encoded
  image string, specifying the MIME type as 'image/png'."
  [image]
  (DataHandler. (base64->bytes image) "image/png"))

(defn image-part
  "Creates a MimeBodyPart object containing an image attachment.
  Args:
  image - A Base64-encoded string representing an image.
  uid - A string to be used as the Content-ID header for the image part.
  Returns:
  A javax.mail.internet.MimeBodyPart object that contains the image as
  an inline attachment."
  [image uid]
  (doto (MimeBodyPart.)
    (.setDataHandler (bs64-image->DataHandler image))
    (.setDisposition MimeBodyPart/INLINE)
    (.addHeader "Content-ID" uid)))

(defn address
  "Extracts the email addresses of the recipients from a MimeMessage.
  Args:
  message - A javax.mail.internet.MimeMessage object.
  Returns:
  A sequence of strings representing the email addresses of the
  recipients."
  [message]
  (map #(.getAddress %)
       (.getRecipients message Message$RecipientType/TO)))

(defn mime-message
  "Creates a MimeMessage object for an email.
  Args:
  from - The sender's email address.
  display-name - The sender's display name (optional).
  to - The recipient's email address.
  subject - The subject of the email.
  session - The mail session object.
  Returns:
  A javax.mail.internet.MimeMessage object with the specified sender,
  recipients, and subject."
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
  "Creates a new MimeMultipart object with a related content type.
  Returns:
  A javax.mail.internet.MimeMultipart object."
  []
  (MimeMultipart. "Related"))

(defn attach-body
  "Adds a MimeBodyPart to a MimeMultipart object.
  Args:
  multi-part - A javax.mail.internet.MimeMultipart object.
  mime-body-part - A javax.mail.internet.MimeBodyPart object to be added.
  Returns:
  nil."
  [multi-part mime-body-part]
  (.addBodyPart multi-part mime-body-part))

(defn set-content
  "Sets the content of a MimeMessage to a given MimeMultipart object.
  Args:
  multi-part - A javax.mail.internet.MimeMultipart object containing the
  message content.
  message - A javax.mail.internet.MimeMessage object to set the content
  for.
  Returns:
  nil."
  [message multi-part]
  (.setContent message multi-part))

(defn set-text
  "Sets the text of a MimeMessage
  Args:
  text - string
  message content.
  message - A javax.mail.internet.MimeMessage object to set the content
  for.
  Returns:
  nil."
  [message text]
  (.setText message text))

(defn set-subject
  "Sets the subject of a MimeMessage
  Args:
  subject - text (string).
  message - A javax.mail.internet.MimeMessage object to set the content
  for.
  Returns:
  nil."
  [message text]
  (.setText message text))

(defn set-content-language
  "Sets the subject of a MimeMessage
  Args:
  languages - a string vector of lanugages
  message - A javax.mail.internet.MimeMessage object to set the content
  for.
  Returns:
  nil."
  [message languages]
  (.setContentLanguage message languages))

(defn attach-bcc
  "Adds BCC recipients to a MimeMessage.
  Args:
  message - A javax.mail.internet.MimeMessage object.
  bcc - A comma-separated string of BCC email addresses.
  Returns:
  nil."
  [message bcc]
  (.setRecipients message Message$RecipientType/BCC
                  (InternetAddress/parse bcc)))

(defn attach-cc
  "Adds CC recipients to a MimeMessage.
  Args:
  message - A javax.mail.internet.MimeMessage object.
  cc - A comma-separated string of CC email addresses.
  Returns:
  nil."
  [message cc]
  (.setRecipients message Message$RecipientType/CC
                  (InternetAddress/parse cc)))

(defn attach-to
  "Adds TO recipients to a MimeMessage.
  Args:
  message - A javax.mail.internet.MimeMessage object.
  to - A comma-separated string of TO email addresses.
  Returns:
  nil."
  [message to]
  (.setRecipients message Message$RecipientType/TO
                  (InternetAddress/parse to)))

(defn attach-header
  "Adds a custom header to a MimeMessage.
  Args:
  message - A javax.mail.internet.MimeMessage object.
  key - The header name.
  val - The header value.
  Returns:
  nil."
  [message key val]
  (.addHeader message key val))

(defn send-email!
  "Sends an email message using the provided MimeMessage object.
  Args:
  message - A javax.mail.internet.MimeMessage object representing the
  email to be sent.
  Returns:
  nil. This function performs side effects by sending an email."
  [message]
  (Transport/send message))


(comment
  (def username "saurav.kudajadri@gmail.com")
  (def password "nil")
)
